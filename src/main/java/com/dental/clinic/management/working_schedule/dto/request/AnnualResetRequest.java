package com.dental.clinic.management.working_schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for annual leave balance reset (P5.2)
 */
public class AnnualResetRequest {

    @NotNull(message = "Cycle year is required")
    @JsonProperty("cycle_year")
    private Integer cycleYear; // e.g., 2026

    @NotNull(message = "Time-off type ID is required")
    @JsonProperty("apply_to_type_id")
    private String applyToTypeId; // e.g., "ANNUAL_LEAVE"

    @NotNull(message = "Default allowance is required")
    @Positive(message = "Default allowance must be positive")
    @JsonProperty("default_allowance")
    private Double defaultAllowance; // e.g., 12.0

    public AnnualResetRequest() {
    }

    public AnnualResetRequest(Integer cycleYear, String applyToTypeId, Double defaultAllowance) {
        this.cycleYear = cycleYear;
        this.applyToTypeId = applyToTypeId;
        this.defaultAllowance = defaultAllowance;
    }

    public Integer getCycleYear() {
        return cycleYear;
    }

    public void setCycleYear(Integer cycleYear) {
        this.cycleYear = cycleYear;
    }

    public String getApplyToTypeId() {
        return applyToTypeId;
    }

    public void setApplyToTypeId(String applyToTypeId) {
        this.applyToTypeId = applyToTypeId;
    }

    public Double getDefaultAllowance() {
        return defaultAllowance;
    }

    public void setDefaultAllowance(Double defaultAllowance) {
        this.defaultAllowance = defaultAllowance;
    }
}
