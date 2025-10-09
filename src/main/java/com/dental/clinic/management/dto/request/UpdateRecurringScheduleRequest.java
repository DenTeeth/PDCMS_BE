package com.dental.clinic.management.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * Request DTO for updating recurring schedule.
 * 
 * Business Rules:
 * - Can update times or shift reference
 * - Cannot change dayOfWeek (delete and create new instead)
 * - Same validation as creation applies
 */
public class UpdateRecurringScheduleRequest {

    /**
     * Optional: Update to use predefined shift.
     * If provided, startTime and endTime must be null.
     */
    @Size(max = 36, message = "Mã ca làm việc không hợp lệ")
    private String shiftId;

    /**
     * Optional: Update to custom start time.
     * Required if shiftId is null.
     */
    private LocalTime startTime;

    /**
     * Optional: Update to custom end time.
     * Required if shiftId is null.
     */
    private LocalTime endTime;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String notes;

    // Constructors
    public UpdateRecurringScheduleRequest() {
    }

    // Getters and Setters
    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "UpdateRecurringScheduleRequest{" +
                "shiftId='" + shiftId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
