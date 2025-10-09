package com.dental.clinic.management.dto.request;

import com.dental.clinic.management.domain.enums.WorkShiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * Request DTO for creating a new work shift.
 * 
 * Business Rules:
 * - Shift code: SHIFT_XXX format (e.g., SHIFT_MORNING)
 * - Shift name: Max 50 chars, Vietnamese supported
 * - Start time: Between 08:00 - 21:00
 * - End time: Between 08:00 - 21:00
 * - Duration: 3-8 hours (validated in service layer)
 * - No overlapping shifts (validated in service layer)
 */
public class CreateWorkShiftRequest {

    @NotBlank(message = "Mã ca làm việc không được để trống")
    @Size(max = 20, message = "Mã ca làm việc tối đa 20 ký tự")
    @Pattern(regexp = "^SHIFT_[A-Z_]+$", message = "Mã ca phải theo định dạng SHIFT_XXX")
    private String shiftCode;

    @NotBlank(message = "Tên ca làm việc không được để trống")
    @Size(max = 50, message = "Tên ca làm việc tối đa 50 ký tự")
    private String shiftName;

    @NotNull(message = "Loại ca làm việc không được để trống")
    private WorkShiftType shiftType;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String notes;

    // Constructors
    public CreateWorkShiftRequest() {
    }

    public CreateWorkShiftRequest(String shiftCode, String shiftName, WorkShiftType shiftType,
                                  LocalTime startTime, LocalTime endTime) {
        this.shiftCode = shiftCode;
        this.shiftName = shiftName;
        this.shiftType = shiftType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
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

    public WorkShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(WorkShiftType shiftType) {
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CreateWorkShiftRequest{" +
                "shiftCode='" + shiftCode + '\'' +
                ", shiftName='" + shiftName + '\'' +
                ", shiftType=" + shiftType +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
