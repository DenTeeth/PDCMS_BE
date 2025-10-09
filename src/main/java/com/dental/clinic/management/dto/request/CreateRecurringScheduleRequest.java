package com.dental.clinic.management.dto.request;

import com.dental.clinic.management.domain.enums.DayOfWeek;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * Request DTO for creating recurring schedule.
 * 
 * Business Rules (validated in service layer):
 * - Only FULL_TIME employees can have recurring schedules
 * - Can use predefined shift (shiftId) OR custom times (startTime/endTime)
 * - If shiftId provided: startTime and endTime must be null
 * - If custom times: shiftId must be null, duration 3-8 hours
 * - No conflicts with other recurring schedules on same day
 * - Used to auto-generate employee_schedules daily
 */
public class CreateRecurringScheduleRequest {

    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 36, message = "Mã nhân viên không hợp lệ")
    private String employeeId;

    @NotNull(message = "Thứ trong tuần không được để trống")
    private DayOfWeek dayOfWeek;

    /**
     * Optional: Use predefined shift.
     * If provided, startTime and endTime must be null.
     */
    @Size(max = 36, message = "Mã ca làm việc không hợp lệ")
    private String shiftId;

    /**
     * Optional: Custom start time.
     * Required if shiftId is null.
     */
    private LocalTime startTime;

    /**
     * Optional: Custom end time.
     * Required if shiftId is null.
     */
    private LocalTime endTime;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String notes;

    // Constructors
    public CreateRecurringScheduleRequest() {
    }

    // With predefined shift
    public CreateRecurringScheduleRequest(String employeeId, DayOfWeek dayOfWeek, String shiftId) {
        this.employeeId = employeeId;
        this.dayOfWeek = dayOfWeek;
        this.shiftId = shiftId;
    }

    // With custom times
    public CreateRecurringScheduleRequest(String employeeId, DayOfWeek dayOfWeek,
                                          LocalTime startTime, LocalTime endTime) {
        this.employeeId = employeeId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

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
        return "CreateRecurringScheduleRequest{" +
                "employeeId='" + employeeId + '\'' +
                ", dayOfWeek=" + dayOfWeek +
                ", shiftId='" + shiftId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
