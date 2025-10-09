package com.dental.clinic.management.dto.request;

import com.dental.clinic.management.domain.enums.EmployeeScheduleStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * Request DTO for updating employee schedule status (attendance tracking).
 * 
 * Business Rules:
 * - SCHEDULED → PRESENT: Normal check-in
 * - SCHEDULED → LATE: Check-in after scheduled start time
 * - SCHEDULED → ABSENT: No check-in by end of day
 * - SCHEDULED → ON_LEAVE: Pre-approved absence
 * 
 * Auto-calculation:
 * - If actualStartTime > startTime: status = LATE
 * - If actualStartTime <= startTime: status = PRESENT
 */
public class UpdateEmployeeScheduleStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private EmployeeScheduleStatus status;

    /**
     * Actual check-in time.
     * Required for PRESENT and LATE status.
     */
    private LocalTime actualStartTime;

    /**
     * Actual check-out time.
     * Optional, for overtime tracking.
     */
    private LocalTime actualEndTime;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String notes;

    // Constructors
    public UpdateEmployeeScheduleStatusRequest() {
    }

    public UpdateEmployeeScheduleStatusRequest(EmployeeScheduleStatus status, LocalTime actualStartTime) {
        this.status = status;
        this.actualStartTime = actualStartTime;
    }

    // Getters and Setters
    public EmployeeScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeScheduleStatus status) {
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "UpdateEmployeeScheduleStatusRequest{" +
                "status=" + status +
                ", actualStartTime=" + actualStartTime +
                ", actualEndTime=" + actualEndTime +
                '}';
    }
}
