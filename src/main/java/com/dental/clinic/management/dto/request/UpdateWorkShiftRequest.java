package com.dental.clinic.management.dto.request;

import com.dental.clinic.management.domain.enums.WorkShiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * Request DTO for updating an existing work shift.
 *
 * Business Rules:
 * - Cannot change shiftCode (immutable business identifier)
 * - Can update shiftName, times, type, active status
 * - Duration: 3-8 hours (validated in service layer)
 * - Time range: 08:00 - 21:00 (validated in service layer)
 * - No overlapping with other shifts (validated in service layer)
 */
public class UpdateWorkShiftRequest {

    @NotBlank(message = "Tên ca làm việc không được để trống")
    @Size(max = 50, message = "Tên ca làm việc tối đa 50 ký tự")
    private String shiftName;

    @NotNull(message = "Loại ca làm việc không được để trống")
    private WorkShiftType shiftType;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String notes;

    // Constructors
    public UpdateWorkShiftRequest() {
    }

    public UpdateWorkShiftRequest(String shiftName, WorkShiftType shiftType,
            LocalTime startTime, LocalTime endTime, Boolean isActive) {
        this.shiftName = shiftName;
        this.shiftType = shiftType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = isActive;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "UpdateWorkShiftRequest{" +
                "shiftName='" + shiftName + '\'' +
                ", shiftType=" + shiftType +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", isActive=" + isActive +
                '}';
    }
}
