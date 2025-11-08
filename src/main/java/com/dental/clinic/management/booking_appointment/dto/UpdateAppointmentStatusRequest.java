package com.dental.clinic.management.booking_appointment.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating appointment status (Check-in, In-Progress,
 * Completed, Cancelled, No-Show).
 * This is the most critical API for daily operations.
 */
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
     * For CHECKED_IN: "Bệnh nhân đã đến trễ 10 phút"
     * For NO_SHOW: "Đã gọi 3 cuộc không nghe máy"
     */
    private String notes;

    public UpdateAppointmentStatusRequest() {
    }

    public UpdateAppointmentStatusRequest(String status, String reasonCode, String notes) {
        this.status = status;
        this.reasonCode = reasonCode;
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
