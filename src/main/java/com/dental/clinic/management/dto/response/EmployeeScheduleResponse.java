package com.dental.clinic.management.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Response DTO for EmployeeSchedule entity.
 * Includes attendance tracking details.
 */
public class EmployeeScheduleResponse {

    private String scheduleId;
    private String scheduleCode;
    private String employeeId;
    private String employeeCode;
    private String employeeName;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer scheduledHours;
    private String status;
    private LocalTime actualStartTime;
    private LocalTime actualEndTime;
    private Integer actualHours;
    private Integer lateMinutes;
    private Integer overtimeMinutes;
    private String notes;
    private String recurringId;
    private String dentistScheduleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public EmployeeScheduleResponse() {
    }

    // Getters and Setters
    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleCode() {
        return scheduleCode;
    }

    public void setScheduleCode(String scheduleCode) {
        this.scheduleCode = scheduleCode;
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

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
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

    public Integer getScheduledHours() {
        return scheduledHours;
    }

    public void setScheduledHours(Integer scheduledHours) {
        this.scheduledHours = scheduledHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public Integer getActualHours() {
        return actualHours;
    }

    public void setActualHours(Integer actualHours) {
        this.actualHours = actualHours;
    }

    public Integer getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(Integer lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public Integer getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public void setOvertimeMinutes(Integer overtimeMinutes) {
        this.overtimeMinutes = overtimeMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRecurringId() {
        return recurringId;
    }

    public void setRecurringId(String recurringId) {
        this.recurringId = recurringId;
    }

    public String getDentistScheduleId() {
        return dentistScheduleId;
    }

    public void setDentistScheduleId(String dentistScheduleId) {
        this.dentistScheduleId = dentistScheduleId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "EmployeeScheduleResponse{" +
                "scheduleCode='" + scheduleCode + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", workDate=" + workDate +
                ", status='" + status + '\'' +
                ", lateMinutes=" + lateMinutes +
                '}';
    }
}
