package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request DTO for creating a part-time slot.
 * 
 * NEW SPECIFICATION:
 * - effectiveFrom and effectiveTo define the slot's active period
 * - dayOfWeek supports multiple days (comma-separated: "FRIDAY,SATURDAY")
 * - quota is the number of people needed PER DAY
 * 
 * Example:
 * {
 * "workShiftId": "WKS_MORNING_01",
 * "dayOfWeek": "FRIDAY,SATURDAY",
 * "effectiveFrom": "2025-11-09",
 * "effectiveTo": "2025-11-30",
 * "quota": 2
 * }
 */
public class CreatePartTimeSlotRequest {

    @NotBlank(message = "Work shift ID is required")
    private String workShiftId;

    /**
     * Days of week this slot is available.
     * Can be single day: "FRIDAY"
     * Or multiple days: "FRIDAY,SATURDAY"
     * Valid values: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
     * (SUNDAY is typically not allowed)
     */
    @NotBlank(message = "Day of week is required")
    private String dayOfWeek;

    /**
     * Start date of slot availability.
     * Example: 2025-11-09
     */
    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    /**
     * End date of slot availability.
     * Example: 2025-11-30
     */
    @NotNull(message = "Effective to date is required")
    private LocalDate effectiveTo;

    /**
     * Number of people needed PER DAY for this slot.
     * Example: quota=2 means 2 people needed on EACH working day.
     */
    @NotNull(message = "Quota is required")
    @Min(value = 1, message = "Quota must be at least 1")
    private Integer quota;

    // Constructors
    public CreatePartTimeSlotRequest() {
    }

    public CreatePartTimeSlotRequest(String workShiftId, String dayOfWeek, LocalDate effectiveFrom,
            LocalDate effectiveTo, Integer quota) {
        this.workShiftId = workShiftId;
        this.dayOfWeek = dayOfWeek;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.quota = quota;
    }

    // Getters and Setters
    public String getWorkShiftId() {
        return workShiftId;
    }

    public void setWorkShiftId(String workShiftId) {
        this.workShiftId = workShiftId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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
}
