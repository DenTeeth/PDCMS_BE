package com.dental.clinic.management.working_schedule.dto.response;

import java.time.LocalDate;

/**
 * Response DTO for part-time slot information.
 * 
 * NEW SPECIFICATION: Includes effective date range.
 */
public class PartTimeSlotResponse {

    private Long slotId;
    private String workShiftId;
    private String workShiftName;

    /**
     * Days of week (can be multiple, comma-separated).
     * Example: "FRIDAY,SATURDAY"
     */
    private String dayOfWeek;

    /**
     * Number of people needed PER DAY.
     */
    private Integer quota;

    /**
     * Count of APPROVED registrations.
     * NEW: Only counts APPROVED, not PENDING.
     */
    private Long registered;

    /**
     * Whether slot is active/accepting registrations.
     */
    private Boolean isActive;

    // NEW: Effective date range
    /**
     * Start date of slot availability.
     */
    private LocalDate effectiveFrom;

    /**
     * End date of slot availability.
     */
    private LocalDate effectiveTo;

    public PartTimeSlotResponse() {
    }

    public PartTimeSlotResponse(Long slotId, String workShiftId, String workShiftName, String dayOfWeek,
            Integer quota, Long registered, Boolean isActive, LocalDate effectiveFrom, LocalDate effectiveTo) {
        this.slotId = slotId;
        this.workShiftId = workShiftId;
        this.workShiftName = workShiftName;
        this.dayOfWeek = dayOfWeek;
        this.quota = quota;
        this.registered = registered;
        this.isActive = isActive;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getWorkShiftId() {
        return workShiftId;
    }

    public void setWorkShiftId(String workShiftId) {
        this.workShiftId = workShiftId;
    }

    public String getWorkShiftName() {
        return workShiftName;
    }

    public void setWorkShiftName(String workShiftName) {
        this.workShiftName = workShiftName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Long getRegistered() {
        return registered;
    }

    public void setRegistered(Long registered) {
        this.registered = registered;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
