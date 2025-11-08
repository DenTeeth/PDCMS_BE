package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * DTO for creating a new work shift.
 * Note: shiftId and category are auto-generated based on time range.
 * Category is no longer sent in request - it's determined by start time.
 */
public class CreateWorkShiftRequest {

    @NotBlank(message = "Tên ca làm việc không được để trống")
    @Size(max = 100, message = "Tên ca làm việc không được vượt quá 100 ký tự")
    private String shiftName;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    public CreateWorkShiftRequest() {
    }

    public CreateWorkShiftRequest(String shiftName, LocalTime startTime, LocalTime endTime) {
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
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

    // Category is removed - auto-generated based on startTime
    // NORMAL: startTime < 18:00 AND endTime <= 18:00
    // NIGHT: startTime >= 18:00
    // INVALID: shift spans across 18:00 boundary
}
