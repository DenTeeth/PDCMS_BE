package com.dental.clinic.management.treatment_plans.service;

import com.dental.clinic.management.exception.ResourceNotFoundException;
import com.dental.clinic.management.booking_appointment.repository.PatientPlanItemRepository;
import com.dental.clinic.management.treatment_plans.domain.PatientPlanItem;
import com.dental.clinic.management.treatment_plans.domain.PatientPlanPhase;
import com.dental.clinic.management.treatment_plans.domain.PatientTreatmentPlan;
import com.dental.clinic.management.treatment_plans.dto.LinkedAppointmentDTO;
import com.dental.clinic.management.treatment_plans.dto.request.UpdateItemStatusRequest;
import com.dental.clinic.management.treatment_plans.dto.response.PatientPlanItemResponse;
import com.dental.clinic.management.treatment_plans.enums.PhaseStatus;
import com.dental.clinic.management.treatment_plans.enums.PlanItemStatus;
import com.dental.clinic.management.treatment_plans.repository.PatientTreatmentPlanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing treatment plan item status transitions.
 * Implements API 5.6: PATCH /api/v1/patient-plan-items/{itemId}/status
 *
 * Core Responsibilities:
 * 1. State Machine Validation (11 transition rules)
 * 2. Appointment Validation (cannot skip if SCHEDULED/IN_PROGRESS)
 * 3. Financial Recalculation (adjust total_cost/final_cost when skip/unskip)
 * 4. Auto-activate next item in phase
 * 5. Auto-complete phase when all items done
 * 6. Audit logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanItemService {

    private final PatientPlanItemRepository itemRepository;
    private final PatientTreatmentPlanRepository planRepository;
    private final EntityManager entityManager;

    /**
     * State Machine Map: current_status → allowed_next_statuses
     */
    private static final Map<PlanItemStatus, Set<PlanItemStatus>> STATE_TRANSITIONS = Map.of(
            PlanItemStatus.PENDING, Set.of(
                    PlanItemStatus.READY_FOR_BOOKING,
                    PlanItemStatus.SKIPPED,
                    PlanItemStatus.COMPLETED),
            PlanItemStatus.READY_FOR_BOOKING, Set.of(
                    PlanItemStatus.SCHEDULED,
                    PlanItemStatus.SKIPPED,
                    PlanItemStatus.COMPLETED),
            PlanItemStatus.SCHEDULED, Set.of(
                    PlanItemStatus.IN_PROGRESS,
                    PlanItemStatus.COMPLETED
            // CANNOT skip if scheduled
            ),
            PlanItemStatus.IN_PROGRESS, Set.of(
                    PlanItemStatus.COMPLETED
            // CANNOT skip if in progress
            ),
            PlanItemStatus.SKIPPED, Set.of(
                    PlanItemStatus.READY_FOR_BOOKING, // Allow undo
                    PlanItemStatus.COMPLETED),
            PlanItemStatus.COMPLETED, Set.of()
    // No transitions from COMPLETED
    );

    /**
     * Main method: Update item status with full business logic
     *
     * @param itemId  ID of the item to update
     * @param request Status update request
     * @return Updated item details
     */
    @Transactional
    public PatientPlanItemResponse updateItemStatus(Long itemId, UpdateItemStatusRequest request) {
        log.info("🔄 Updating item {} to status {}", itemId, request.getStatus());

        // STEP 1: Find item with phase and plan data
        PatientPlanItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TREATMENT_PLAN_ITEM_NOT_FOUND",
                        "Treatment plan item not found with ID: " + itemId));

        PatientPlanPhase phase = item.getPhase();
        PatientTreatmentPlan plan = phase.getTreatmentPlan();

        PlanItemStatus currentStatus = item.getStatus();
        PlanItemStatus newStatus = request.getStatus();

        log.info("📊 Item current status: {}, requested: {}", currentStatus, newStatus);

        // STEP 2: Validate state transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    String.format("Invalid status transition: %s → %s. Allowed transitions from %s are: %s",
                            currentStatus, newStatus, currentStatus,
                            STATE_TRANSITIONS.getOrDefault(currentStatus, Set.of())));
        }

        // STEP 3: Check appointment constraints (cannot skip if SCHEDULED/IN_PROGRESS)
        if (newStatus == PlanItemStatus.SKIPPED &&
                (currentStatus == PlanItemStatus.SCHEDULED || currentStatus == PlanItemStatus.IN_PROGRESS)) {
            // This path should be blocked by state machine, but double-check
            validateNoActiveAppointments(itemId);
        }

        // Additional check: if trying to skip from READY_FOR_BOOKING, ensure no
        // scheduled appointments exist
        if (newStatus == PlanItemStatus.SKIPPED && currentStatus == PlanItemStatus.READY_FOR_BOOKING) {
            List<Map<String, Object>> appointments = findAppointmentsForItem(itemId);
            long activeAppointments = appointments.stream()
                    .filter(apt -> {
                        String status = (String) apt.get("status");
                        return "SCHEDULED".equals(status) || "IN_PROGRESS".equals(status)
                                || "CHECKED_IN".equals(status);
                    })
                    .count();

            if (activeAppointments > 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cannot skip item: " + activeAppointments + " active appointment(s) found. " +
                                "Please cancel appointments first.");
            }
        }

        // STEP 4: Calculate financial impact BEFORE updating status
        boolean financialImpact = false;
        String financialMessage = null;

        if (currentStatus != PlanItemStatus.SKIPPED && newStatus == PlanItemStatus.SKIPPED) {
            // Skipping: reduce costs
            financialImpact = true;
            recalculatePlanFinances(plan, item.getPrice(), true); // subtract
            financialMessage = String.format("Item skipped: Plan total cost reduced by %,d VND",
                    item.getPrice().longValue());
            log.info("💰 Financial impact: SKIP - Reduced {} VND", item.getPrice());

        } else if (currentStatus == PlanItemStatus.SKIPPED && newStatus == PlanItemStatus.READY_FOR_BOOKING) {
            // Unskipping: add costs back
            financialImpact = true;
            recalculatePlanFinances(plan, item.getPrice(), false); // add
            financialMessage = String.format("Item re-activated: Plan total cost increased by %,d VND",
                    item.getPrice().longValue());
            log.info("💰 Financial impact: UNSKIP - Added back {} VND", item.getPrice());
        }

        // STEP 5: Update item status and metadata
        item.setStatus(newStatus);
        if (request.getNotes() != null) {
            // Note: PatientPlanItem doesn't have notes field in current schema
            // If needed, add notes field to entity or log to audit table
            log.info("📝 Notes: {}", request.getNotes());
        }

        if (newStatus == PlanItemStatus.COMPLETED) {
            item.setCompletedAt(request.getCompletedAt() != null ? request.getCompletedAt() : LocalDateTime.now());
            log.info("✅ Item marked as COMPLETED at {}", item.getCompletedAt());
        } else {
            item.setCompletedAt(null); // Clear if not completed
        }

        PatientPlanItem savedItem = itemRepository.save(item);

        // STEP 6: Auto-activate next item in phase
        if (newStatus == PlanItemStatus.COMPLETED) {
            activateNextItemInPhase(phase, item.getSequenceNumber());
        }

        // STEP 7: Check and auto-complete phase
        checkAndCompletePhase(phase);

        // STEP 8: Audit log (implement if audit table exists)
        String currentUser = getCurrentUsername();
        log.info("📋 Audit: User {} changed item {} from {} to {}",
                currentUser, itemId, currentStatus, newStatus);

        // STEP 9: Build response
        List<LinkedAppointmentDTO> linkedAppointments = findAppointmentsForItem(itemId).stream()
                .map(apt -> LinkedAppointmentDTO.builder()
                        .code((String) apt.get("code"))
                        .scheduledDate((LocalDateTime) apt.get("scheduled_date"))
                        .status((String) apt.get("status"))
                        .build())
                .toList();

        return PatientPlanItemResponse.builder()
                .itemId(savedItem.getItemId())
                .sequenceNumber(savedItem.getSequenceNumber())
                .itemName(savedItem.getItemName())
                .serviceId(savedItem.getServiceId())
                .price(savedItem.getPrice())
                .estimatedTimeMinutes(savedItem.getEstimatedTimeMinutes())
                .status(savedItem.getStatus().name())
                .completedAt(savedItem.getCompletedAt())
                .notes(request.getNotes())
                .phaseId(phase.getPatientPhaseId())
                .phaseName(phase.getPhaseName())
                .phaseSequenceNumber(phase.getPhaseNumber())
                .linkedAppointments(linkedAppointments)
                .financialImpact(financialImpact)
                .financialImpactMessage(financialMessage)
                .updatedAt(LocalDateTime.now())
                .updatedBy(currentUser)
                .build();
    }

    /**
     * Validate state transition using state machine
     */
    private boolean isValidTransition(PlanItemStatus current, PlanItemStatus next) {
        if (current == next) {
            return true; // Idempotent
        }

        Set<PlanItemStatus> allowedNext = STATE_TRANSITIONS.getOrDefault(current, Set.of());
        return allowedNext.contains(next);
    }

    /**
     * Validate no active appointments before skipping
     * Active = SCHEDULED, IN_PROGRESS, CHECKED_IN
     */
    private void validateNoActiveAppointments(Long itemId) {
        List<Map<String, Object>> appointments = findAppointmentsForItem(itemId);

        long activeCount = appointments.stream()
                .filter(apt -> {
                    String status = (String) apt.get("status");
                    return "SCHEDULED".equals(status) ||
                            "IN_PROGRESS".equals(status) ||
                            "CHECKED_IN".equals(status);
                })
                .count();

        if (activeCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot skip item: " + activeCount + " active appointment(s) found. " +
                            "Current status does not allow skipping while appointments are scheduled or in progress.");
        }
    }

    /**
     * Find all appointments linked to this item
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> findAppointmentsForItem(Long itemId) {
        String sql = """
                SELECT a.code, a.scheduled_date, a.status
                FROM appointments a
                JOIN appointment_plan_items api ON a.appointment_id = api.appointment_id
                WHERE api.item_id = :itemId
                ORDER BY a.scheduled_date DESC
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("itemId", itemId);

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", row[0]);
                    map.put("scheduled_date", row[1]);
                    map.put("status", row[2]);
                    return map;
                })
                .toList();
    }

    /**
     * Recalculate plan finances when item is skipped/unskipped
     *
     * @param plan     Treatment plan to update
     * @param amount   Item price
     * @param subtract true = subtract (skip), false = add (unskip)
     */
    private void recalculatePlanFinances(PatientTreatmentPlan plan, BigDecimal amount, boolean subtract) {
        BigDecimal currentTotal = plan.getTotalPrice() != null ? plan.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal currentFinal = plan.getFinalCost() != null ? plan.getFinalCost() : BigDecimal.ZERO;

        if (subtract) {
            plan.setTotalPrice(currentTotal.subtract(amount));
            plan.setFinalCost(currentFinal.subtract(amount));
        } else {
            plan.setTotalPrice(currentTotal.add(amount));
            plan.setFinalCost(currentFinal.add(amount));
        }

        planRepository.save(plan);
        log.info("💸 Plan finances updated: total_cost={}, final_cost={}",
                plan.getTotalPrice(), plan.getFinalCost());
    }

    /**
     * Auto-activate next item in phase (change PENDING → READY_FOR_BOOKING)
     */
    private void activateNextItemInPhase(PatientPlanPhase phase, Integer completedSequence) {
        List<PatientPlanItem> items = phase.getItems();

        items.stream()
                .filter(item -> item.getSequenceNumber() == completedSequence + 1)
                .filter(item -> item.getStatus() == PlanItemStatus.PENDING)
                .findFirst()
                .ifPresent(nextItem -> {
                    nextItem.setStatus(PlanItemStatus.READY_FOR_BOOKING);
                    itemRepository.save(nextItem);
                    log.info("🚀 Auto-activated next item {} (sequence {}) → READY_FOR_BOOKING",
                            nextItem.getItemId(), nextItem.getSequenceNumber());
                });
    }

    /**
     * Check if all items in phase are completed/skipped, then mark phase as
     * COMPLETED
     */
    private void checkAndCompletePhase(PatientPlanPhase phase) {
        List<PatientPlanItem> items = phase.getItems();

        boolean allDone = items.stream()
                .allMatch(item -> item.getStatus() == PlanItemStatus.COMPLETED ||
                        item.getStatus() == PlanItemStatus.SKIPPED);

        if (allDone && phase.getStatus() != PhaseStatus.COMPLETED) {
            phase.setStatus(PhaseStatus.COMPLETED);
            phase.setCompletionDate(java.time.LocalDate.now());
            entityManager.merge(phase); // Update phase
            log.info("🎯 Phase {} auto-completed: all items are done", phase.getPatientPhaseId());
        }
    }

    /**
     * Get current authenticated username
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}
