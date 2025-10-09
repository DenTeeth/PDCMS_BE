package com.dental.clinic.management.dto.response;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Response DTO for WorkShift entity.
 * Used in API responses for single shift details.
 */
public class WorkShiftResponse {

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
    public WorkShiftResponse() {
    }

    // Getters and Setters
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
        return "WorkShiftResponse{" +
                "shiftCode='" + shiftCode + '\'' +
                ", shiftName='" + shiftName + '\'' +
                ", shiftType='" + shiftType + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", durationHours=" + durationHours +
                ", isActive=" + isActive +
                '}';
    }
}
