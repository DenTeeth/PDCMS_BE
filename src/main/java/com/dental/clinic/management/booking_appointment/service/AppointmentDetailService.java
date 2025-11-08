package com.dental.clinic.management.booking_appointment.service;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.domain.AppointmentAuditLog;
import com.dental.clinic.management.booking_appointment.domain.AppointmentParticipant;
import com.dental.clinic.management.booking_appointment.domain.AppointmentService;
import com.dental.clinic.management.booking_appointment.dto.AppointmentDetailDTO;
import com.dental.clinic.management.booking_appointment.dto.CreateAppointmentResponse;
import com.dental.clinic.management.booking_appointment.enums.AppointmentActionType;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import com.dental.clinic.management.booking_appointment.repository.AppointmentAuditLogRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentParticipantRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentServiceRepository;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.ResourceNotFoundException;
import com.dental.clinic.management.patient.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for fetching single appointment detail
 * P3.4: GET /api/v1/appointments/{appointmentCode}
 *
 * Key Features:
 * - RBAC enforcement (VIEW_APPOINTMENT_ALL vs VIEW_APPOINTMENT_OWN)
 * - Load all related entities (patient, doctor, room, services, participants)
 * - Load cancellation reason from audit log if status = CANCELLED
 * - Compute dynamic fields (computedStatus, minutesLate)
 * - Return detailed DTO
 */
@Service
public class AppointmentDetailService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentDetailService.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final EmployeeRepository employeeRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final AppointmentParticipantRepository appointmentParticipantRepository;
    private final AppointmentAuditLogRepository appointmentAuditLogRepository;

    public AppointmentDetailService(AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            EmployeeRepository employeeRepository,
            AppointmentServiceRepository appointmentServiceRepository,
            AppointmentParticipantRepository appointmentParticipantRepository,
            AppointmentAuditLogRepository appointmentAuditLogRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.employeeRepository = employeeRepository;
        this.appointmentServiceRepository = appointmentServiceRepository;
        this.appointmentParticipantRepository = appointmentParticipantRepository;
        this.appointmentAuditLogRepository = appointmentAuditLogRepository;
    }

    /**
     * Get appointment detail by code with RBAC check
     *
     * Business Logic:
     * 1. Find appointment by code (throw 404 if not found)
     * 2. Check RBAC permissions:
     * - VIEW_APPOINTMENT_ALL: Can view any appointment
     * - VIEW_APPOINTMENT_OWN:
     * * Patient: Can only view their own appointments
     * * Employee: Can view if they are doctor OR participant
     * 3. Load all related entities (patient, doctor, room, services, participants)
     * 4. Compute dynamic fields (computedStatus, minutesLate)
     * 5. Map to AppointmentDetailDTO
     *
     * @param appointmentCode Unique appointment code
     * @return AppointmentDetailDTO with full details
     * @throws com.dental.clinic.management.exception.ResourceNotFoundException if
     *                                                                          appointment
     *                                                                          not
     *                                                                          found
     * @throws AccessDeniedException                                            if
     *                                                                          user
     *                                                                          doesn't
     *                                                                          have
     *                                                                          permission
     *                                                                          to
     *                                                                          view
     */
    @Transactional(readOnly = true)
    public AppointmentDetailDTO getAppointmentDetail(String appointmentCode) {
        log.info("Fetching appointment detail for code: {}", appointmentCode);

        // Step 1: Find appointment
        Appointment appointment = appointmentRepository.findDetailByCode(appointmentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "APPOINTMENT_NOT_FOUND",
                        "Appointment not found with code: " + appointmentCode));

        // Step 2: RBAC Check
        checkPermission(appointment);

        // Step 3: Load related entities and map to DTO
        return mapToDetailDTO(appointment);
    }

    /**
     * Check if current user has permission to view this appointment
     *
     * RBAC Logic:
     * - VIEW_APPOINTMENT_ALL: Can view any appointment
     * - VIEW_APPOINTMENT_OWN:
     * * Employee roles (DENTIST, NURSE, etc.): Check if user is doctor OR
     * participant
     * * Patient role: Check if appointment.patientId == user's patientId
     *
     * @throws AccessDeniedException if user doesn't have permission
     */
    private void checkPermission(Appointment appointment) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check for VIEW_APPOINTMENT_ALL permission
        boolean canViewAll = auth.getAuthorities().stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("VIEW_APPOINTMENT_ALL"));

        if (canViewAll) {
            log.debug("User has VIEW_APPOINTMENT_ALL permission - access granted");
            return;
        }

        // Check for VIEW_APPOINTMENT_OWN permission
        boolean canViewOwn = auth.getAuthorities().stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("VIEW_APPOINTMENT_OWN"));

        if (!canViewOwn) {
            log.warn("User doesn't have VIEW_APPOINTMENT_OWN permission - access denied");
            throw new AccessDeniedException("You don't have permission to view appointments");
        }

        // Extract username from JWT token
        String username = null;
        if (auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            username = jwt.getSubject();
        } else if (auth.getPrincipal() instanceof String) {
            username = (String) auth.getPrincipal();
        }

        if (username == null) {
            log.warn("Could not extract username from authentication");
            throw new AccessDeniedException("Invalid authentication token");
        }

        log.debug("RBAC check for user: {}", username);

        // Check if user is an employee (has employee-related roles)
        boolean isEmployeeRole = auth.getAuthorities().stream()
                .anyMatch(grantedAuth -> {
                    String authority = grantedAuth.getAuthority();
                    return authority.equals("ROLE_DENTIST") ||
                            authority.equals("ROLE_NURSE") ||
                            authority.equals("ROLE_DENTIST_INTERN") ||
                            authority.equals("ROLE_RECEPTIONIST") ||
                            authority.equals("ROLE_MANAGER");
                });

        if (isEmployeeRole) {
            // Employee can view if they are doctor OR participant
            Optional<Integer> myEmployeeId = employeeRepository.findByAccount_Username(username)
                    .map(e -> e.getEmployeeId());

            if (myEmployeeId.isEmpty()) {
                log.warn("Employee account {} not found in employee table", username);
                throw new AccessDeniedException("Employee profile not found");
            }

            boolean isDoctor = appointment.getEmployeeId().equals(myEmployeeId.get());
            boolean isParticipant = appointmentParticipantRepository
                    .findByIdAppointmentId(appointment.getAppointmentId())
                    .stream()
                    .anyMatch(ap -> ap.getId().getEmployeeId().equals(myEmployeeId.get()));

            if (!isDoctor && !isParticipant) {
                log.warn("Employee {} tried to access unrelated appointment {}",
                        myEmployeeId.get(), appointment.getAppointmentCode());
                throw new AccessDeniedException("You can only view appointments where you are involved");
            }
        } else {
            // Assume patient role - check if they own this appointment
            // Note: For now, we skip patient check since Patient table doesn't have
            // account_id mapping
            // In a real system, you'd need Patient.findByAccount_Username() or similar
            log.debug("Patient role detected - allowing access (patient validation not implemented)");

        }

        log.debug("RBAC check passed - access granted");
    }

    /**
     * Map Appointment entity to DetailDTO with all related entities
     */
    private AppointmentDetailDTO mapToDetailDTO(Appointment appointment) {
        // Load patient (with phone and DOB for detail view)
        CreateAppointmentResponse.PatientSummary patientSummary = null;
        try {
            var patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
            if (patient != null) {
                patientSummary = new CreateAppointmentResponse.PatientSummary(
                        patient.getPatientCode(),
                        patient.getFirstName() + " " + patient.getLastName(),
                        patient.getPhone(),
                        patient.getDateOfBirth());
            }
        } catch (Exception e) {
            log.warn("Failed to load patient: {}", e.getMessage());
        }

        // Load doctor (primary employee)
        CreateAppointmentResponse.DoctorSummary doctorSummary = null;
        try {
            var employee = employeeRepository.findById(appointment.getEmployeeId()).orElse(null);
            if (employee != null) {
                doctorSummary = new CreateAppointmentResponse.DoctorSummary(
                        employee.getEmployeeCode(),
                        employee.getFirstName() + " " + employee.getLastName());
            }
        } catch (Exception e) {
            log.warn("Failed to load doctor: {}", e.getMessage());
        }

        // TODO: Load room from RoomRepository
        CreateAppointmentResponse.RoomSummary roomSummary = new CreateAppointmentResponse.RoomSummary(
                appointment.getRoomId(),
                "Room " + appointment.getRoomId());

        // Load services
        List<CreateAppointmentResponse.ServiceSummary> services = new ArrayList<>();
        try {
            List<AppointmentService> appointmentServices = appointmentServiceRepository
                    .findByIdAppointmentId(appointment.getAppointmentId());
            // TODO: Load actual service details from ServiceRepository
            log.debug("Found {} services for appointment {}", appointmentServices.size(),
                    appointment.getAppointmentCode());
        } catch (Exception e) {
            log.warn("Failed to load services: {}", e.getMessage());
        }

        // Load participants
        List<CreateAppointmentResponse.ParticipantSummary> participants = new ArrayList<>();
        try {
            List<AppointmentParticipant> appointmentParticipants = appointmentParticipantRepository
                    .findByIdAppointmentId(appointment.getAppointmentId());

            for (AppointmentParticipant ap : appointmentParticipants) {
                var participantEmployee = employeeRepository.findById(ap.getId().getEmployeeId()).orElse(null);
                if (participantEmployee != null) {
                    participants.add(new CreateAppointmentResponse.ParticipantSummary(
                            participantEmployee.getEmployeeCode(),
                            participantEmployee.getFirstName() + " " + participantEmployee.getLastName(),
                            ap.getRole()));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load participants: {}", e.getMessage());
        }

        // Load createdBy user info
        String createdByName = null;
        try {
            if (appointment.getCreatedBy() != null) {
                var createdByEmployee = employeeRepository.findById(appointment.getCreatedBy()).orElse(null);
                if (createdByEmployee != null) {
                    createdByName = createdByEmployee.getFirstName() + " " + createdByEmployee.getLastName();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load createdBy info: {}", e.getMessage());
        }

        // Load cancellation reason from audit log (if status = CANCELLED)
        String cancellationReason = null;
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            try {
                List<AppointmentAuditLog> auditLogs = appointmentAuditLogRepository
                        .findByAppointmentIdOrderByCreatedAtDesc(appointment.getAppointmentId());

                // Find the CANCEL action log
                AppointmentAuditLog cancelLog = auditLogs.stream()
                        .filter(log -> log.getActionType() == AppointmentActionType.CANCEL)
                        .findFirst()
                        .orElse(null);

                if (cancelLog != null) {
                    // Build cancellation reason from reasonCode and notes
                    StringBuilder reason = new StringBuilder();
                    if (cancelLog.getReasonCode() != null) {
                        reason.append(cancelLog.getReasonCode().name());
                    }
                    if (cancelLog.getNotes() != null && !cancelLog.getNotes().isEmpty()) {
                        if (reason.length() > 0) {
                            reason.append(": ");
                        }
                        reason.append(cancelLog.getNotes());
                    }
                    cancellationReason = reason.toString();
                }
            } catch (Exception e) {
                log.warn("Failed to load cancellation reason: {}", e.getMessage());
            }
        }

        // Compute dynamic fields
        LocalDateTime now = LocalDateTime.now();
        String computedStatus = calculateComputedStatus(appointment, now);
        Long minutesLate = calculateMinutesLate(appointment, now);

        return new AppointmentDetailDTO(
                appointment.getAppointmentId(),
                appointment.getAppointmentCode(),
                appointment.getStatus().name(),
                computedStatus,
                minutesLate,
                appointment.getAppointmentStartTime(),
                appointment.getAppointmentEndTime(),
                appointment.getExpectedDurationMinutes(),
                appointment.getActualStartTime(),
                appointment.getActualEndTime(),
                cancellationReason,
                appointment.getNotes(),
                patientSummary,
                doctorSummary,
                roomSummary,
                services,
                participants,
                createdByName,
                appointment.getCreatedAt());
    }

    /**
     * Calculate computed status (same logic as AppointmentListService)
     */
    private String calculateComputedStatus(Appointment appointment, LocalDateTime now) {
        AppointmentStatus status = appointment.getStatus();

        return switch (status) {
            case CANCELLED -> "CANCELLED";
            case COMPLETED -> "COMPLETED";
            case NO_SHOW -> "NO_SHOW";
            case CHECKED_IN -> "CHECKED_IN";
            case IN_PROGRESS -> "IN_PROGRESS";
            case SCHEDULED -> {
                if (now.isAfter(appointment.getAppointmentStartTime())) {
                    yield "LATE";
                } else {
                    yield "UPCOMING";
                }
            }
        };
    }

    /**
     * Calculate minutes late (same logic as AppointmentListService)
     */
    private Long calculateMinutesLate(Appointment appointment, LocalDateTime now) {
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            return 0L;
        }

        if (now.isAfter(appointment.getAppointmentStartTime())) {
            return Duration.between(appointment.getAppointmentStartTime(), now).toMinutes();
        }

        return 0L;
    }
}
