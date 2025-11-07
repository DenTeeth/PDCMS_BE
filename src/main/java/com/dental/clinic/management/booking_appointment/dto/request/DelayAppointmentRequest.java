package com.dental.clinic.management.booking_appointment.dto.request;

import com.dental.clinic.management.booking_appointment.enums.AppointmentReasonCode;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request DTO for delaying an appointment to a new time slot.
 * API: PATCH /api/v1/appointments/{appointmentCode}/delay
 */
public class DelayAppointmentRequest {

    /**
     * New start time for the appointment.
     * Must be after the original start time.
     * Preferably on the same day as original appointment.
     */
    @NotNull(message = "New start time is required")
    private LocalDateTime newStartTime;

    /**
     * Reason code for delaying the appointment.
     * Examples: PATIENT_REQUEST, DOCTOR_EMERGENCY, EQUIPMENT_FAILURE
     */
    @NotNull(message = "Reason code is required")
    private AppointmentReasonCode reasonCode;

    /**
     * Additional notes explaining the delay.
     * Optional but recommended for audit trail.
     */
    private String notes;

    public DelayAppointmentRequest() {
    }

    public DelayAppointmentRequest(LocalDateTime newStartTime, AppointmentReasonCode reasonCode, String notes) {
        this.newStartTime = newStartTime;
        this.reasonCode = reasonCode;
        this.notes = notes;
    }

    public LocalDateTime getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(LocalDateTime newStartTime) {
        this.newStartTime = newStartTime;
    }

    public AppointmentReasonCode getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(AppointmentReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
