package com.dental.clinic.management.working_schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for individual leave balance detail (P5.2)
 */
public class LeaveBalanceDetailResponse {

    @JsonProperty("balance_id")
    private Long balanceId;

    @JsonProperty("time_off_type")
    private TimeOffTypeInfoResponse timeOffType;

    @JsonProperty("total_days_allowed")
    private Double totalDaysAllowed;

    @JsonProperty("days_taken")
    private Double daysTaken;

    @JsonProperty("days_remaining")
    private Double daysRemaining;

    public LeaveBalanceDetailResponse() {
    }

    public LeaveBalanceDetailResponse(Long balanceId, TimeOffTypeInfoResponse timeOffType, Double totalDaysAllowed,
            Double daysTaken, Double daysRemaining) {
        this.balanceId = balanceId;
        this.timeOffType = timeOffType;
        this.totalDaysAllowed = totalDaysAllowed;
        this.daysTaken = daysTaken;
        this.daysRemaining = daysRemaining;
    }

    public Long getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(Long balanceId) {
        this.balanceId = balanceId;
    }

    public TimeOffTypeInfoResponse getTimeOffType() {
        return timeOffType;
    }

    public void setTimeOffType(TimeOffTypeInfoResponse timeOffType) {
        this.timeOffType = timeOffType;
    }

    public Double getTotalDaysAllowed() {
        return totalDaysAllowed;
    }

    public void setTotalDaysAllowed(Double totalDaysAllowed) {
        this.totalDaysAllowed = totalDaysAllowed;
    }

    public Double getDaysTaken() {
        return daysTaken;
    }

    public void setDaysTaken(Double daysTaken) {
        this.daysTaken = daysTaken;
    }

    public Double getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Double daysRemaining) {
        this.daysRemaining = daysRemaining;
    }
}
