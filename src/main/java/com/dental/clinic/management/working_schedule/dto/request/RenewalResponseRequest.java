package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Employee can CONFIRM (accept renewal) or DECLINE (reject with reason).
 */
public class RenewalResponseRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "CONFIRMED|DECLINED", message = "Action must be either CONFIRMED or DECLINED")
    private String action; // "CONFIRMED" or "DECLINED"

    /**
     * Required when action = "DECLINED", optional when action = "CONFIRMED".
     * Validation is done in service layer (cannot use @NotNull here due to
     * conditional requirement).
     */
    private String declineReason;

    // Constructors
    public RenewalResponseRequest() {
    }

    public RenewalResponseRequest(String action, String declineReason) {
        this.action = action;
        this.declineReason = declineReason;
    }

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }
}
