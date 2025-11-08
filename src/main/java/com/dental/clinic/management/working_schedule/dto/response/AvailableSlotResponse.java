package com.dental.clinic.management.working_schedule.dto.response;

import java.time.LocalDate;

public class AvailableSlotResponse {

    private Long slotId;
    private String shiftName;
    private String dayOfWeek;

    // Date availability counts
    private Integer totalDatesAvailable; // Count of dates with space (registered < quota)
    private Integer totalDatesEmpty; // Count of dates with no registrations (registered = 0)
    private Integer totalDatesFull; // Count of dates at quota (registered = quota)

    // Additional context
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Integer quota;
    private String availabilitySummary; // e.g., "December FULL, January has space"

    // Constructors
    public AvailableSlotResponse() {
    }

    public AvailableSlotResponse(Long slotId, String shiftName, String dayOfWeek,
            Integer totalDatesAvailable, Integer totalDatesEmpty, Integer totalDatesFull,
            LocalDate effectiveFrom, LocalDate effectiveTo, Integer quota, String availabilitySummary) {
        this.slotId = slotId;
        this.shiftName = shiftName;
        this.dayOfWeek = dayOfWeek;
        this.totalDatesAvailable = totalDatesAvailable;
        this.totalDatesEmpty = totalDatesEmpty;
        this.totalDatesFull = totalDatesFull;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.quota = quota;
        this.availabilitySummary = availabilitySummary;
    }

    // Getters and Setters
    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getTotalDatesAvailable() {
        return totalDatesAvailable;
    }

    public void setTotalDatesAvailable(Integer totalDatesAvailable) {
        this.totalDatesAvailable = totalDatesAvailable;
    }

    public Integer getTotalDatesEmpty() {
        return totalDatesEmpty;
    }

    public void setTotalDatesEmpty(Integer totalDatesEmpty) {
        this.totalDatesEmpty = totalDatesEmpty;
    }

    public Integer getTotalDatesFull() {
        return totalDatesFull;
    }

    public void setTotalDatesFull(Integer totalDatesFull) {
        this.totalDatesFull = totalDatesFull;
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

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public String getAvailabilitySummary() {
        return availabilitySummary;
    }

    public void setAvailabilitySummary(String availabilitySummary) {
        this.availabilitySummary = availabilitySummary;
    }
}
