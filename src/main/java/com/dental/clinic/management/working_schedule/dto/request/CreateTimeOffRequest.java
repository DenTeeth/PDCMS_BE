package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request DTO for creating new time-off request
 */
public class CreateTimeOffRequest {

    @NotNull(message = "Employee ID is required")
    private Integer employeeId;

    @NotNull(message = "Time-off type ID is required")
    private String timeOffTypeId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String workShiftId; // NULL for full-day off, value for half-day off

    @NotNull(message = "Reason is required")
    private String reason;

    // Constructors
    public CreateTimeOffRequest() {
    }

    public CreateTimeOffRequest(Integer employeeId, String timeOffTypeId, LocalDate startDate,
            LocalDate endDate, String workShiftId, String reason) {
        this.employeeId = employeeId;
        this.timeOffTypeId = timeOffTypeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.workShiftId = workShiftId;
        this.reason = reason;
    }

    // Getters and Setters
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getWorkShiftId() {
        return workShiftId;
    }

    public void setWorkShiftId(String workShiftId) {
        this.workShiftId = workShiftId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "CreateTimeOffRequest{" +
                "employeeId=" + employeeId +
                ", timeOffTypeId='" + timeOffTypeId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", workShiftId='" + workShiftId + '\'' +
                '}';
    }
}
