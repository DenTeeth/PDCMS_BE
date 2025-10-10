package com.dental.clinic.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for cancelling dentist work schedule.
 *
 * Business Rules:
 * - Can only cancel AVAILABLE schedules
 * - BOOKED schedules require different workflow (reschedule patients first)
 * - Reason is required for audit trail
 * - Status will be set to CANCELLED
 */
public class CancelDentistScheduleRequest {

    @NotBlank(message = "Lý do hủy không được để trống")
    @Size(min = 10, max = 500, message = "Lý do hủy phải từ 10-500 ký tự")
    private String cancelReason;

    // Constructors
    public CancelDentistScheduleRequest() {
    }

    public CancelDentistScheduleRequest(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    // Getters and Setters
    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    @Override
    public String toString() {
        return "CancelDentistScheduleRequest{" +
                "cancelReason='" + cancelReason + '\'' +
                '}';
    }
}
