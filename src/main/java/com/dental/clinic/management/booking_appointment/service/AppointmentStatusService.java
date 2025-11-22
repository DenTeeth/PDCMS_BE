package com.dental.clinic.management.booking_appointment.service;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.domain.AppointmentAuditLog;
import com.dental.clinic.management.booking_appointment.dto.AppointmentDetailDTO;
import com.dental.clinic.management.booking_appointment.dto.UpdateAppointmentStatusRequest;
import com.dental.clinic.management.booking_appointment.enums.AppointmentActionType;
import com.dental.clinic.management.booking_appointment.enums.AppointmentReasonCode;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import com.dental.clinic.management.booking_appointment.repository.AppointmentAuditLogRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.ResourceNotFoundException;
import com.dental.clinic.management.treatment_plans.enums.PlanItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for updating appointment status with state machine validation.
 * This is the MOST CRITICAL API for daily clinic operations.
 *
 * Features:
 * - Pessimistic locking (SELECT FOR UPDATE) to prevent race conditions
 * - State machine validation (SCHEDULED -> CHECKED_IN -> IN_PROGRESS ->
 * COMPLETED)
 * - Auto-update actualStartTime/actualEndTime based on status transitions
 * - Comprehensive audit logging for compliance
 * - Business rule validation (e.g., CANCELLED requires reasonCode)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentStatusService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentAuditLogRepository auditLogRepository;
    private final EmployeeRepository employeeRepository;
    private final AppointmentDetailService detailService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Valid state transitions map.
     * Key: Current status
     * Value: Allowed next statuses
     *
     * State Machine:
     * - SCHEDULED -> CHECKED_IN, CANCELLED, NO_SHOW
     * - CHECKED_IN -> IN_PROGRESS, CANCELLED
     * - IN_PROGRESS -> COMPLETED, CANCELLED
     * - COMPLETED, CANCELLED, NO_SHOW -> No transitions (terminal states)
     */
    private static final Map<AppointmentStatus, Set<AppointmentStatus>> VALID_TRANSITIONS = Map.of(
            AppointmentStatus.SCHEDULED, Set.of(
                    AppointmentStatus.CHECKED_IN,
                    AppointmentStatus.CANCELLED,
                    AppointmentStatus.NO_SHOW),
            AppointmentStatus.CHECKED_IN, Set.of(
                    AppointmentStatus.IN_PROGRESS,
                    AppointmentStatus.CANCELLED),
            AppointmentStatus.IN_PROGRESS, Set.of(
                    AppointmentStatus.COMPLETED,
                    AppointmentStatus.CANCELLED),
            AppointmentStatus.COMPLETED, Collections.emptySet(),
            AppointmentStatus.CANCELLED, Collections.emptySet(),
            AppointmentStatus.NO_SHOW, Collections.emptySet());

    /**
     * Update appointment status with full validation and audit logging.
     *
     * Transaction Flow:
     * 1. Lock appointment row (SELECT FOR UPDATE)
     * 2. Validate state transition
     * 3. Validate business rules (e.g., CANCELLED requires reasonCode)
     * 4. Update actualStartTime/actualEndTime if needed
     * 5. Update status and notes
     * 6. Create audit log
     * 7. Commit transaction
     *
     * @param appointmentCode Unique appointment code
     * @param request         Status update request
     * @return Updated appointment detail DTO (same as API 3.4)
     * @throws ResourceNotFoundException If appointment not found
     * @throws BusinessException         If state transition invalid or business
     *                                   rule violated
     */
    @Transactional
    public AppointmentDetailDTO updateStatus(String appointmentCode, UpdateAppointmentStatusRequest request) {
        log.info("Updating appointment status: code={}, newStatus={}", appointmentCode, request.getStatus());

        // Step 1: Lock appointment (SELECT FOR UPDATE)
        Appointment appointment = appointmentRepository.findByCodeForUpdate(appointmentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "APPOINTMENT_NOT_FOUND",
                        "Appointment not found with code: " + appointmentCode));

        AppointmentStatus currentStatus = appointment.getStatus();
        AppointmentStatus newStatus;
        try {
            newStatus = AppointmentStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + request.getStatus());
        }

        // Step 2: Validate state transition
        validateStateTransition(currentStatus, newStatus);

        // Step 3: Validate business rules
        validateBusinessRules(newStatus, request);

        // Step 4: Update side-effects (actualStartTime/actualEndTime)
        LocalDateTime now = LocalDateTime.now();
        updateTimestamps(appointment, currentStatus, newStatus, now);

        // Step 5: Update status and notes
        appointment.setStatus(newStatus);
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }
        appointmentRepository.save(appointment);

        // Step 6: Create audit log
        createAuditLog(appointment, currentStatus, newStatus, request, now);

        // Step 7: Auto-update linked plan item statuses (V21.5)
        updateLinkedPlanItemsStatus(appointment.getAppointmentId(), newStatus, now);

        log.info("Successfully updated appointment status: code={}, {} -> {}",
                appointmentCode, currentStatus, newStatus);

        // Step 8: Return updated detail (same structure as API 3.4)
        return detailService.getAppointmentDetail(appointmentCode);
    }

    /**
     * Validate state machine transition.
     *
     * @throws IllegalStateException If transition is invalid
     */
    private void validateStateTransition(AppointmentStatus currentStatus, AppointmentStatus newStatus) {
        if (currentStatus == newStatus) {
            throw new IllegalStateException(
                    String.format("Appointment is already in %s status", currentStatus));
        }

        Set<AppointmentStatus> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);
        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s. Allowed transitions: %s",
                            currentStatus, newStatus, allowedTransitions));
        }
    }

    /**
     * Validate business rules for status updates.
     *
     * Rules:
     * - CANCELLED: Must provide reasonCode
     */
    private void validateBusinessRules(AppointmentStatus newStatus, UpdateAppointmentStatusRequest request) {
        if (newStatus == AppointmentStatus.CANCELLED) {
            if (request.getReasonCode() == null || request.getReasonCode().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Reason code is required when cancelling an appointment");
            }
        }
    }

    /**
     * Update actualStartTime/actualEndTime based on status transitions.
     *
     * Rules:
     * - SCHEDULED -> CHECKED_IN: No timestamp update (just check-in)
     * - CHECKED_IN -> IN_PROGRESS: Set actualStartTime = NOW()
     * - IN_PROGRESS -> COMPLETED: Set actualEndTime = NOW()
     *
     * Note: We do NOT set actualStartTime on CHECKED_IN because check-in means
     * "patient arrived" but treatment hasn't started yet. Actual treatment starts
     * when status changes to IN_PROGRESS.
     */
    private void updateTimestamps(Appointment appointment, AppointmentStatus currentStatus,
            AppointmentStatus newStatus, LocalDateTime now) {

        // Set actualStartTime when treatment actually begins (IN_PROGRESS)
        if (currentStatus == AppointmentStatus.CHECKED_IN && newStatus == AppointmentStatus.IN_PROGRESS) {
            appointment.setActualStartTime(now);
            log.info("Set actualStartTime={} for appointment {}", now, appointment.getAppointmentCode());
        }

        // Set actualEndTime when treatment completes
        if (currentStatus == AppointmentStatus.IN_PROGRESS && newStatus == AppointmentStatus.COMPLETED) {
            appointment.setActualEndTime(now);
            log.info("Set actualEndTime={} for appointment {}", now, appointment.getAppointmentCode());
        }
    }

    /**
     * Create audit log for status change.
     */
    private void createAuditLog(Appointment appointment, AppointmentStatus oldStatus,
            AppointmentStatus newStatus, UpdateAppointmentStatusRequest request, LocalDateTime now) {

        Integer changedByEmployeeId = getCurrentEmployeeId();

        // Convert reasonCode String to enum (nullable)
        AppointmentReasonCode reasonCodeEnum = null;
        if (request.getReasonCode() != null && !request.getReasonCode().trim().isEmpty()) {
            try {
                reasonCodeEnum = AppointmentReasonCode.valueOf(request.getReasonCode().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid reason code: {}", request.getReasonCode());
            }
        }

        AppointmentAuditLog auditLog = AppointmentAuditLog.builder()
                .appointment(appointment)  // Set the relationship, not the ID
                .actionType(AppointmentActionType.STATUS_CHANGE)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reasonCode(reasonCodeEnum)
                .notes(request.getNotes())
                .performedByEmployeeId(changedByEmployeeId)
                .createdAt(now)
                .build();

        auditLogRepository.save(auditLog);
        log.info("Created audit log: appointmentId={}, {} -> {}, changedBy={}",
                appointment.getAppointmentId(), oldStatus, newStatus, changedByEmployeeId);
    }

    /**
     * Get current employee ID from security context.
     * Returns 0 (SYSTEM) if not authenticated or employee mapping not found.
     */
    private Integer getCurrentEmployeeId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return 0; // SYSTEM
            }

            // Extract username from JWT token
            String username = null;
            if (auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                username = jwt.getSubject();
            } else if (auth.getPrincipal() instanceof String) {
                username = (String) auth.getPrincipal();
            }

            if (username == null) {
                log.warn("Could not extract username from authentication principal");
                return 0;
            }

            return employeeRepository.findByAccount_Username(username)
                    .map(employee -> employee.getEmployeeId())
                    .orElse(0); // SYSTEM if employee not found
        } catch (Exception e) {
            log.warn("Failed to get employee ID from security context: {}", e.getMessage());
            return 0; // SYSTEM
        }
    }

    /**
     * V21.5: Auto-update linked treatment plan item statuses when appointment status changes.
     * 
     * Status Mapping:
     * - Appointment IN_PROGRESS → Plan items IN_PROGRESS
     * - Appointment COMPLETED → Plan items COMPLETED (with completedAt timestamp)
     * - Appointment CANCELLED → Plan items SCHEDULED (keep as scheduled, not revert to READY_FOR_BOOKING)
     * 
     * This ensures treatment plan items stay synchronized with appointment progress,
     * eliminating manual status updates and preventing data inconsistency.
     * 
     * @param appointmentId The appointment ID (Integer type from Appointment entity)
     * @param appointmentStatus The new appointment status
     * @param timestamp The timestamp to use for completedAt (when status = COMPLETED)
     */
    private void updateLinkedPlanItemsStatus(Integer appointmentId, AppointmentStatus appointmentStatus, LocalDateTime timestamp) {
        // Only update plan items for specific status transitions
        if (appointmentStatus != AppointmentStatus.IN_PROGRESS 
                && appointmentStatus != AppointmentStatus.COMPLETED 
                && appointmentStatus != AppointmentStatus.CANCELLED) {
            log.debug("No plan item update needed for appointment status: {}", appointmentStatus);
            return;
        }

        // Find all plan items linked to this appointment
        // Convert Integer to Long explicitly (appointment_plan_items.appointment_id is bigint)
        Long appointmentIdLong = appointmentId.longValue();
        String findItemsQuery = """
            SELECT item_id FROM appointment_plan_items 
            WHERE appointment_id = ?
            """;
        
        List<Long> itemIds = jdbcTemplate.queryForList(findItemsQuery, Long.class, appointmentIdLong);
        
        if (itemIds.isEmpty()) {
            log.debug("No plan items linked to appointment {}", appointmentId);
            return;
        }

        // Determine target status for plan items
        PlanItemStatus targetStatus;
        switch (appointmentStatus) {
            case IN_PROGRESS:
                targetStatus = PlanItemStatus.IN_PROGRESS;
                break;
            case COMPLETED:
                targetStatus = PlanItemStatus.COMPLETED;
                break;
            case CANCELLED:
                targetStatus = PlanItemStatus.READY_FOR_BOOKING; // Allow re-booking
                break;
            default:
                log.warn("Unexpected appointment status for plan item update: {}", appointmentStatus);
                return;
        }

        // Update plan items based on appointment status
        try {
            if (appointmentStatus == AppointmentStatus.COMPLETED) {
                // For COMPLETED: Update status AND set completedAt timestamp
                // Use proper parameter binding to prevent SQL injection
                String placeholders = String.join(",", java.util.Collections.nCopies(itemIds.size(), "?"));
                String updateQuery = String.format(
                    "UPDATE patient_plan_items SET status = CAST(? AS plan_item_status), completed_at = ? WHERE item_id IN (%s)",
                    placeholders
                );
                
                java.util.List<Object> params = new java.util.ArrayList<>();
                params.add(targetStatus.name());
                params.add(timestamp);
                params.addAll(itemIds);
                
                int updatedRows = jdbcTemplate.update(updateQuery, params.toArray());
                
                if (updatedRows == 0) {
                    log.warn("⚠️ No plan items updated for appointment {} - itemIds: {}", appointmentId, itemIds);
                } else {
                    log.info("✅ Updated {} plan items to COMPLETED with timestamp for appointment {}", 
                        updatedRows, appointmentId);
                }
            } else {
                // For IN_PROGRESS or CANCELLED: Only update status
                // Use proper parameter binding to prevent SQL injection
                String placeholders = String.join(",", java.util.Collections.nCopies(itemIds.size(), "?"));
                String updateQuery = String.format(
                    "UPDATE patient_plan_items SET status = CAST(? AS plan_item_status) WHERE item_id IN (%s)",
                    placeholders
                );
                
                java.util.List<Object> params = new java.util.ArrayList<>();
                params.add(targetStatus.name());
                params.addAll(itemIds);
                
                int updatedRows = jdbcTemplate.update(updateQuery, params.toArray());
                
                if (updatedRows == 0) {
                    log.warn("⚠️ No plan items updated for appointment {} - itemIds: {}", appointmentId, itemIds);
                } else {
                    log.info("✅ Updated {} plan items to {} for appointment {}", 
                        updatedRows, targetStatus, appointmentId);
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to update plan items for appointment {}: {}", appointmentId, e.getMessage(), e);
            throw new RuntimeException("Failed to update linked plan items", e);
        }
    }
}
