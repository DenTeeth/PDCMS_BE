package com.dental.clinic.management.dto.response;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Response DTO for RecurringSchedule entity.
 * Includes resolved shift times for display.
 */
public class RecurringScheduleResponse {

    private String recurringId;
    private String recurringCode;
    private String employeeId;
    private String employeeCode;
    private String employeeName;
    private String dayOfWeek;
    private String shiftId;
    private String shiftCode;
    private String shiftName;
    private String shiftType;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationHours;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;

    // Constructors
    public RecurringScheduleResponse() {
    }

    // Getters and Setters
    public String getRecurringId() {
        return recurringId;
    }

    public void setRecurringId(String recurringId) {
        this.recurringId = recurringId;
    }

    public String getRecurringCode() {
        return recurringCode;
    }

    public void setRecurringCode(String recurringCode) {
        this.recurringCode = recurringCode;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getShiftCode() {
        return shiftCode;
    }

    public void setShiftCode(String shiftCode) {
        this.shiftCode = shiftCode;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public String getShiftType() {
        return shiftType;
    }

    public void setShiftType(String shiftType) {
        this.shiftType = shiftType;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "RecurringScheduleResponse{" +
                "recurringCode='" + recurringCode + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", shiftName='" + shiftName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", isActive=" + isActive +
                '}';
    }
}
