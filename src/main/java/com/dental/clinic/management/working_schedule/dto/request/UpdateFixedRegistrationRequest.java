package com.dental.clinic.management.working_schedule.dto.request;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for updating a fixed shift registration.
 */
public class UpdateFixedRegistrationRequest {

    private String workShiftId; // Optional

    private List<Integer> daysOfWeek; // Optional

    private LocalDate effectiveFrom; // Optional

    private LocalDate effectiveTo; // Optional (null = permanent)

    public UpdateFixedRegistrationRequest() {
    }

    public UpdateFixedRegistrationRequest(String workShiftId, List<Integer> daysOfWeek, LocalDate effectiveFrom,
            LocalDate effectiveTo) {
        this.workShiftId = workShiftId;
        this.daysOfWeek = daysOfWeek;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    public String getWorkShiftId() {
        return workShiftId;
    }

    public void setWorkShiftId(String workShiftId) {
        this.workShiftId = workShiftId;
    }

    public List<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<Integer> daysOfWeek) {
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
