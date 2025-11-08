package com.dental.clinic.management.working_schedule.dto.request;

import com.dental.clinic.management.working_schedule.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating overtime request status (Approve/Reject/Cancel).
 * Used in PATCH /api/v1/overtime-requests/{request_id}
 * 
 * Business Rules:
 * - Can only update PENDING requests
 * - reason is required when status is REJECTED or CANCELLED
 * - Only specific statuses are allowed: APPROVED, REJECTED, CANCELLED
 */
public class UpdateOvertimeStatusDTO {

    @NotNull(message = "Status is required")
    private RequestStatus status;

    /**
     * Required when status is REJECTED or CANCELLED.
     * Should contain the reason for rejection or cancellation.
     */
    private String reason;

    // Constructors
    public UpdateOvertimeStatusDTO() {
    }

    public UpdateOvertimeStatusDTO(RequestStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    // Getters and Setters
    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
