package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdatePartTimeSlotRequest {

    @NotNull(message = "Quota is required")
    @Min(value = 1, message = "Quota must be at least 1")
    private Integer quota;

    @NotNull(message = "isActive is required")
    private Boolean isActive;

    // Constructors
    public UpdatePartTimeSlotRequest() {
    }

    public UpdatePartTimeSlotRequest(Integer quota, Boolean isActive) {
        this.quota = quota;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
