package com.dental.clinic.management.working_schedule.dto.response;

import java.time.LocalTime;

import com.dental.clinic.management.working_schedule.enums.WorkShiftCategory;

/**
 * DTO for work shift response.
 * Includes the calculated duration_hours field.
 */
public class WorkShiftResponse {

    private String workShiftId;

    private String shiftName;

    private LocalTime startTime;

    private LocalTime endTime;

    private WorkShiftCategory category;

    private Boolean isActive;

    /**
     * Calculated field: duration in hours.
     */
    private Double durationHours;

    public WorkShiftResponse() {
    }

    public WorkShiftResponse(String workShiftId, String shiftName, LocalTime startTime, LocalTime endTime,
            WorkShiftCategory category, Boolean isActive, Double durationHours) {
        this.workShiftId = workShiftId;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.isActive = isActive;
        this.durationHours = durationHours;
    }

    public String getWorkShiftId() {
        return workShiftId;
    }

    public void setWorkShiftId(String workShiftId) {
        this.workShiftId = workShiftId;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
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

    public WorkShiftCategory getCategory() {
        return category;
    }

    public void setCategory(WorkShiftCategory category) {
        this.category = category;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Double getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Double durationHours) {
        this.durationHours = durationHours;
    }
}
