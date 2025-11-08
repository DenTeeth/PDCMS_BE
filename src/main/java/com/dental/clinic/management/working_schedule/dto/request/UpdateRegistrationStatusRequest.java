package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for approving or rejecting a part-time registration.
 * 
 * Used by managers in: PATCH
 * /api/v1/admin/registrations/part-time-flex/{id}/status
 * 
 * Example (Approve):
 * {
 * "status": "APPROVED"
 * }
 * 
 * Example (Reject):
 * {
 * "status": "REJECTED",
 * "reason": "Không chấp nhận nhân sự trong thời gian này"
 * }
 */
public class UpdateRegistrationStatusRequest {

    /**
     * New status: "APPROVED" or "REJECTED"
     * PENDING is not allowed (can't revert to pending).
     */
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "APPROVED|REJECTED", message = "Status must be either APPROVED or REJECTED")
    private String status;

    /**
     * Rejection reason (REQUIRED if status = REJECTED).
     * Example: "Không chấp nhận nhân sự trong thời gian này"
     */
    private String reason;

    // Constructors
    public UpdateRegistrationStatusRequest() {
    }

    public UpdateRegistrationStatusRequest(String status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
