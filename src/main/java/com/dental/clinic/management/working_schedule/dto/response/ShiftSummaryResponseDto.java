package com.dental.clinic.management.working_schedule.dto.response;

import com.dental.clinic.management.working_schedule.enums.ShiftStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Map;

/**
 * Response DTO for shift summary by date.
 * Used for GET /api/v1/shifts/summary endpoint.
 */
public class ShiftSummaryResponseDto {

    @JsonProperty("work_date")
    private LocalDate workDate;

    @JsonProperty("total_shifts")
    private Long totalShifts;

    @JsonProperty("status_breakdown")
    private Map<ShiftStatus, Long> statusBreakdown;

    public ShiftSummaryResponseDto() {
    }

    public ShiftSummaryResponseDto(LocalDate workDate, Long totalShifts, Map<ShiftStatus, Long> statusBreakdown) {
        this.workDate = workDate;
        this.totalShifts = totalShifts;
        this.statusBreakdown = statusBreakdown;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public Long getTotalShifts() {
        return totalShifts;
    }

    public void setTotalShifts(Long totalShifts) {
        this.totalShifts = totalShifts;
    }

    public Map<ShiftStatus, Long> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(Map<ShiftStatus, Long> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }
}
