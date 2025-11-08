package com.dental.clinic.management.working_schedule.dto.request;

import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating time-off request status (PATCH)
 * Used for APPROVE, REJECT, CANCEL actions
 */
public class UpdateTimeOffStatusRequest {

    @NotNull(message = "Status is required")
    private TimeOffStatus status;

    private String reason; // Required for REJECTED and CANCELLED

    // Constructors
    public UpdateTimeOffStatusRequest() {
    }

    public UpdateTimeOffStatusRequest(TimeOffStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    // Getters and Setters
    public TimeOffStatus getStatus() {
        return status;
    }

    public void setStatus(TimeOffStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "UpdateTimeOffStatusRequest{" +
                "status=" + status +
                ", reason='" + reason + '\'' +
                '}';
    }
}
