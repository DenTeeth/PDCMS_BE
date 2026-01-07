package com.dental.clinic.management.working_schedule.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.dental.clinic.management.working_schedule.enums.DayOfWeek;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for PUT /api/v1/registrations/{registration_id}
 * All fields are required - replaces the entire registration
 */
public class ReplaceShiftRegistrationRequest {

    @NotNull(message = "Mã ca làm việc là bắt buộc")
    private String workShiftId;

    @NotEmpty(message = "Các ngày trong tuần không được để trống")
    private List<DayOfWeek> daysOfWeek;

    @NotNull(message = "Ngày hiệu lực từ là bắt buộc")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @NotNull(message = "Trạng thái hoạt động là bắt buộc")
    private Boolean isActive;

    // Getters and Setters
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "ReplaceShiftRegistrationRequest{" +
                "workShiftId='" + workShiftId + '\'' +
                ", daysOfWeek=" + daysOfWeek +
                ", effectiveFrom=" + effectiveFrom +
                ", effectiveTo=" + effectiveTo +
                ", isActive=" + isActive +
                '}';
    }
}
