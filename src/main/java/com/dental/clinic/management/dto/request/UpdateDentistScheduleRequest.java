package com.dental.clinic.management.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * Request DTO for updating dentist work schedule.
 *
 * Business Rules:
 * - Can only update AVAILABLE schedules
 * - BOOKED schedules cannot be modified (patients scheduled)
 * - Cannot change workDate (create new instead)
 * - Same validation rules as creation apply
 */
public class UpdateDentistScheduleRequest {

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String notes;

    // Constructors
    public UpdateDentistScheduleRequest() {
    }

    public UpdateDentistScheduleRequest(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
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
        return "UpdateDentistScheduleRequest{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
