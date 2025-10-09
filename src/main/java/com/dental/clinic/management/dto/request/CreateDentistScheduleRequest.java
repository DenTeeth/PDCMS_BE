package com.dental.clinic.management.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for creating dentist work schedule.
 * 
 * Business Rules (validated in service layer):
 * - Only PART_TIME dentists can create schedules
 * - Max 2 schedules per day
 * - Must register at least 24 hours in advance
 * - Cannot register more than 30 days in advance
 * - Duration: minimum 2 hours, recommended 3-4 hours
 * - Working hours: 08:00 - 21:00
 * - No overlapping with existing schedules
 * 
 * Payment commitment: Dentist will be paid even without patients.
 */
public class CreateDentistScheduleRequest {

    @NotBlank(message = "Mã bác sĩ không được để trống")
    @Size(max = 36, message = "Mã bác sĩ không hợp lệ")
    private String dentistId;

    @NotNull(message = "Ngày làm việc không được để trống")
    @Future(message = "Ngày làm việc phải là ngày trong tương lai")
    private LocalDate workDate;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String notes;

    // Constructors
    public CreateDentistScheduleRequest() {
    }

    public CreateDentistScheduleRequest(String dentistId, LocalDate workDate,
                                        LocalTime startTime, LocalTime endTime) {
        this.dentistId = dentistId;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public String getDentistId() {
        return dentistId;
    }

    public void setDentistId(String dentistId) {
        this.dentistId = dentistId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CreateDentistScheduleRequest{" +
                "dentistId='" + dentistId + '\'' +
                ", workDate=" + workDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
