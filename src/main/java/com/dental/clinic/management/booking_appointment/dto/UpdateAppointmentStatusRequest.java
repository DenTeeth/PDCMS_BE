package com.dental.clinic.management.booking_appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating appointment status (Check-in, In-Progress,
 * Completed, Cancelled, No-Show).
 * This is the most critical API for daily operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentStatusRequest {

    /**
     * New status for the appointment.
     * Valid values: CHECKED_IN, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
     */
    @NotNull(message = "Status is required")
    private String status;

    /**
     * Reason code for cancellation (REQUIRED when status = CANCELLED).
     * Valid values: PATIENT_REQUEST, DOCTOR_UNAVAILABLE, EMERGENCY, etc.
     */
    private String reasonCode;

    /**
     * Optional notes for the status change.
     * For CANCELLED: Detailed reason
     * For CHECKED_IN: "BÃ¡Â»â€¡nh nhÃƒÂ¢n Ã„â€˜Ã¡ÂºÂ¿n trÃ¡Â»â€¦ 10 phÃƒÂºt"
     * For NO_SHOW: "Ã„ÂÃƒÂ£ gÃ¡Â»Âi 3 cuÃ¡Â»â„¢c khÃƒÂ´ng nghe mÃƒÂ¡y"
     */
    private String notes;
}
