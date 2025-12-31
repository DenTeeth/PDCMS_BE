package com.dental.clinic.management.working_schedule.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.dental.clinic.management.working_schedule.enums.DayOfWeek;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateShiftRegistrationRequest {

    @NotNull(message = "Mã nhân viên là bắt buộc")
    private Integer employeeId;

    @NotNull(message = "Mã ca làm việc là bắt buộc")
    private String workShiftId;

    @NotEmpty(message = "Các ngày trong tuần không được để trống")
    private List<DayOfWeek> daysOfWeek;

    @NotNull(message = "Ngày hiệu lực từ là bắt buộc")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    // Getters and Setters
    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getWorkShiftId() {
        return workShiftId;
    }

    public void setWorkShiftId(String workShiftId) {
        this.workShiftId = workShiftId;
    }

    public List<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<DayOfWeek> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
}
