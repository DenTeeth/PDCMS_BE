package com.dental.clinic.management.warehouse.service;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.repository.PatientRepository;
import com.dental.clinic.management.warehouse.domain.StorageTransaction;
import com.dental.clinic.management.warehouse.dto.response.AppointmentReferenceDto;
import com.dental.clinic.management.warehouse.dto.response.ReferenceCodeSuggestion;
import com.dental.clinic.management.warehouse.dto.response.ReferenceCodeValidation;
import com.dental.clinic.management.warehouse.enums.TransactionType;
import com.dental.clinic.management.warehouse.repository.StorageTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Reference Code Service
 * Provides auto-complete, validation, and suggestion features for reference codes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReferenceCodeService {

    private final StorageTransactionRepository transactionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Get recent used reference codes for auto-complete suggestions
     *
     * @param limit Maximum number of suggestions to return
     * @return List of reference code suggestions
     */
    @Transactional(readOnly = true)
    public List<ReferenceCodeSuggestion> getRecentReferenceCodes(int limit) {
        log.info("Fetching recent {} reference codes", limit);

        // Query recent transactions with non-null reference codes
        List<StorageTransaction> recentTransactions = transactionRepository
                .findRecentReferenceCodesWithLimit(limit);

        // Group by referenceCode and count usage
        return recentTransactions.stream()
                .collect(Collectors.groupingBy(StorageTransaction::getReferenceCode))
                .entrySet().stream()
                .map(entry -> {
                    String code = entry.getKey();
                    List<StorageTransaction> transactions = entry.getValue();
                    StorageTransaction mostRecent = transactions.get(0); // Already sorted by date DESC

                    ReferenceCodeSuggestion.ReferenceCodeSuggestionBuilder builder = ReferenceCodeSuggestion.builder()
                            .code(code)
                            .type(detectReferenceType(code))
                            .lastUsedDate(mostRecent.getTransactionDate().toLocalDate())
                            .usageCount(transactions.size());

                    // If linked to appointment, fetch patient info
                    if (mostRecent.getRelatedAppointment() != null) {
                        Appointment apt = mostRecent.getRelatedAppointment();
                        builder.relatedAppointmentId(apt.getAppointmentId().longValue());

                        if (apt.getPatientId() != null) {
                            patientRepository.findById(apt.getPatientId()).ifPresent(patient -> {
                                String patientName = patient.getFirstName() + " " + patient.getLastName();
                                builder.patientName(patientName);
                                builder.label(String.format("%s (%s - %s)", 
                                        code, 
                                        patientName, 
                                        DATE_FORMATTER.format(mostRecent.getTransactionDate())));
                            });
                        }
                    }

                    // Default label if no patient info
                    if (builder.build().getLabel() == null) {
                        builder.label(String.format("%s (Dùng gần đây: %s)", 
                                code, 
                                DATE_FORMATTER.format(mostRecent.getTransactionDate())));
                    }

                    return builder.build();
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Search appointments for reference code selection
     *
     * @param search Search term (appointment code, patient name)
     * @param status Filter by appointment status
     * @param limit  Maximum results
     * @return List of appointment references
     */
    @Transactional(readOnly = true)
    public List<AppointmentReferenceDto> searchAppointments(String search, AppointmentStatus status, int limit) {
        log.info("Searching appointments - search: {}, status: {}, limit: {}", search, status, limit);

        List<Appointment> appointments;

        if (search != null && !search.trim().isEmpty()) {
            // Search by appointment code or patient
            String statusStr = status != null ? status.name() : null;
            appointments = appointmentRepository.findByAppointmentCodeContainingIgnoreCaseOrPatientNameContaining(
                    search, statusStr, limit);
        } else {
            // Get recent appointments by status
            String statusStr = status != null ? status.name() : null;
            appointments = appointmentRepository.findRecentByStatus(statusStr, limit);
        }

        return appointments.stream()
                .map(this::mapToAppointmentReference)
                .collect(Collectors.toList());
    }

    /**
     * Validate reference code and get related entity info
     *
     * @param code Reference code to validate
     * @return Validation result with related entity details
     */
    @Transactional(readOnly = true)
    public ReferenceCodeValidation validateReferenceCode(String code) {
        log.info("Validating reference code: {}", code);

        if (code == null || code.trim().isEmpty()) {
            return ReferenceCodeValidation.builder()
                    .valid(false)
                    .exists(false)
                    .message("Mã tham chiếu không được để trống")
                    .build();
        }

        String type = detectReferenceType(code);

        // If looks like appointment code, try to find it
        if ("APPOINTMENT".equals(type)) {
            Optional<Appointment> appointmentOpt = appointmentRepository.findByAppointmentCode(code);

            if (appointmentOpt.isPresent()) {
                Appointment apt = appointmentOpt.get();
                ReferenceCodeValidation.RelatedEntityInfo entityInfo = buildEntityInfo(apt);

                return ReferenceCodeValidation.builder()
                        .exists(true)
                        .valid(true)
                        .type(type)
                        .message("Mã ca điều trị hợp lệ")
                        .relatedEntity(entityInfo)
                        .build();
            } else {
                return ReferenceCodeValidation.builder()
                        .exists(false)
                        .valid(false)
                        .type(type)
                        .message("Không tìm thấy ca điều trị với mã: " + code)
                        .build();
            }
        }

        // For other types (REQUEST, CUSTOM), just mark as valid if format is OK
        return ReferenceCodeValidation.builder()
                .exists(false) // Not in system
                .valid(true) // But valid as custom reference
                .type(type)
                .message("Mã tham chiếu tùy chỉnh")
                .build();
    }

    /**
     * Auto-detect reference type based on code pattern
     *
     * @param code Reference code
     * @return Type: APPOINTMENT, REQUEST, or CUSTOM
     */
    private String detectReferenceType(String code) {
        if (code == null) return "CUSTOM";

        code = code.trim().toUpperCase();

        if (code.startsWith("APT-")) {
            return "APPOINTMENT";
        } else if (code.startsWith("REQ-")) {
            return "REQUEST";
        } else {
            return "CUSTOM";
        }
    }

    /**
     * Map Appointment entity to AppointmentReferenceDto
     */
    private AppointmentReferenceDto mapToAppointmentReference(Appointment apt) {
        AppointmentReferenceDto dto = AppointmentReferenceDto.builder()
                .appointmentId(apt.getAppointmentId().longValue())
                .appointmentCode(apt.getAppointmentCode())
                .patientId(apt.getPatientId())
                .appointmentDate(apt.getAppointmentStartTime())
                .status(apt.getStatus().toString())
                .build();

        // Fetch patient name
        if (apt.getPatientId() != null) {
            patientRepository.findById(apt.getPatientId()).ifPresent(patient -> {
                String patientName = patient.getFirstName() + " " + patient.getLastName();
                dto.setPatientName(patientName);
            });
        }

        // Fetch services (simplified - you may need to enhance this)
        List<Object[]> services = appointmentRepository.findServicesByAppointmentId(apt.getAppointmentId());
        if (!services.isEmpty()) {
            String serviceNames = services.stream()
                    .map(svc -> (String) svc[1]) // service_name
                    .collect(Collectors.joining(", "));
            dto.setServices(serviceNames);
        }

        // Build display label
        String displayLabel = String.format("%s - %s (%s)", 
                apt.getAppointmentCode(),
                dto.getPatientName() != null ? dto.getPatientName() : "N/A",
                apt.getAppointmentStartTime() != null ? DATETIME_FORMATTER.format(apt.getAppointmentStartTime()) : "N/A");
        dto.setDisplayLabel(displayLabel);

        return dto;
    }

    /**
     * Build RelatedEntityInfo from Appointment
     */
    private ReferenceCodeValidation.RelatedEntityInfo buildEntityInfo(Appointment apt) {
        ReferenceCodeValidation.RelatedEntityInfo.RelatedEntityInfoBuilder builder = 
                ReferenceCodeValidation.RelatedEntityInfo.builder()
                        .appointmentCode(apt.getAppointmentCode())
                        .appointmentId(apt.getAppointmentId().longValue())
                        .appointmentDate(apt.getAppointmentStartTime())
                        .status(apt.getStatus().toString());

        // Fetch patient name
        if (apt.getPatientId() != null) {
            patientRepository.findById(apt.getPatientId()).ifPresent(patient -> {
                builder.patientName(patient.getFirstName() + " " + patient.getLastName());
            });
        }

        // Fetch services
        List<Object[]> services = appointmentRepository.findServicesByAppointmentId(apt.getAppointmentId());
        if (!services.isEmpty()) {
            String serviceNames = services.stream()
                    .map(svc -> (String) svc[1])
                    .collect(Collectors.joining(", "));
            builder.services(serviceNames);
        }

        return builder.build();
    }
}
