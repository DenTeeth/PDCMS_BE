package com.dental.clinic.management.booking_appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new appointment (P3.2)
 *
 * Business Rules:
 * - All codes must exist and be active
 * - Doctor must have required specializations for services
 * - Room must support all services (room_services)
 * - appointmentStartTime must be in future and during doctor's shift
 * - No conflicts for doctor, room, patient, or participants
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    /**
     * Patient code (must exist and be active)
     * Example: "BN-1001"
     */
    @NotBlank(message = "Patient code is required")
    private String patientCode;

    /**
     * Employee code of primary doctor (must exist and be active)
     * Example: "DR_AN_KHOA", "BS-001"
     */
    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    /**
     * Room code selected from P3.1 available times API
     * Must exist, be active, and support all services
     * Example: "P-IMPLANT-01"
     */
    @NotBlank(message = "Room code is required")
    private String roomCode;

    /**
     * List of service codes to be performed
     * All must exist and be active
     * Example: ["SV-IMPLANT", "SV-NANGXOANG"]
     */
    @NotEmpty(message = "At least one service code is required")
    private List<String> serviceCodes;

    /**
     * Start time of appointment in ISO 8601 format
     * Must be in future and within doctor's shift
     * Server will calculate end time based on service durations
     * Example: "2025-10-30T09:30:00"
     */
    @NotBlank(message = "Appointment start time is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Start time must be in ISO 8601 format (YYYY-MM-DDTHH:mm:ss)")
    private String appointmentStartTime;

    /**
     * Optional list of participant codes (assistants, secondary doctors)
     * All will be assigned default role: ASSISTANT
     * Example: ["PT-BINH", "PT-AN"]
     */
    private List<String> participantCodes;

    /**
     * Optional notes from receptionist
     * Example: "Bệnh nhân có tiền sử cao huyết áp"
     */
    private String notes;
}
