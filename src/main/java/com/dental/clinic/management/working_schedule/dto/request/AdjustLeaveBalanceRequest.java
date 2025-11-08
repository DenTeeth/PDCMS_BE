package com.dental.clinic.management.working_schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adjusting leave balance (P5.2)
 */
public class AdjustLeaveBalanceRequest {

    @NotNull(message = "Employee ID is required")
    @JsonProperty("employee_id")
    private Integer employeeId;

    @NotNull(message = "Time-off type ID is required")
    @JsonProperty("time_off_type_id")
    private String timeOffTypeId;

    @NotNull(message = "Cycle year is required")
    @JsonProperty("cycle_year")
    private Integer cycleYear;

    @NotNull(message = "Change amount is required")
    @JsonProperty("change_amount")
    private Double changeAmount; // Positive to add, negative to subtract

    @JsonProperty("notes")
    private String notes;

    public AdjustLeaveBalanceRequest() {
    }

    public AdjustLeaveBalanceRequest(Integer employeeId, String timeOffTypeId, Integer cycleYear, Double changeAmount,
            String notes) {
        this.employeeId = employeeId;
        this.timeOffTypeId = timeOffTypeId;
        this.cycleYear = cycleYear;
        this.changeAmount = changeAmount;
        this.notes = notes;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getTimeOffTypeId() {
        return timeOffTypeId;
    }

    public void setTimeOffTypeId(String timeOffTypeId) {
        this.timeOffTypeId = timeOffTypeId;
    }

    public Integer getCycleYear() {
        return cycleYear;
    }

    public void setCycleYear(Integer cycleYear) {
        this.cycleYear = cycleYear;
    }

    public Double getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Double changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
