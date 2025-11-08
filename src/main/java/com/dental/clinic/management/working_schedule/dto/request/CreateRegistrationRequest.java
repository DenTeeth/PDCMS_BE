package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating a part-time registration.
 * 
 * SPECIFICATION:
 * - Employee specifies which days of the week they can work
 * - System calculates all matching dates within effectiveFrom/effectiveTo range
 * - System filters by availability (quota not exceeded)
 * - Request goes to PENDING status, waiting for manager approval
 * - Supports flexible scheduling (different days each month based on employee
 * availability)
 * 
 * Example - November (can work Mondays and Thursdays):
 * {
 * "partTimeSlotId": 1,
 * "effectiveFrom": "2025-11-01",
 * "effectiveTo": "2025-11-30",
 * "dayOfWeek": ["MONDAY", "THURSDAY"]
 * }
 * 
 * Example - December (schedule changed, now can work Tuesdays and Fridays):
 * {
 * "partTimeSlotId": 1,
 * "effectiveFrom": "2025-12-01",
 * "effectiveTo": "2025-12-31",
 * "dayOfWeek": ["TUESDAY", "FRIDAY"]
 * }
 */
public class CreateRegistrationRequest {

    @NotNull(message = "Part-time slot ID is required")
    private Long partTimeSlotId;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    /**
     * NEW: Employee can specify when they want to stop working.
     * Must be within the slot's effectiveFrom/effectiveTo range.
     */
    @NotNull(message = "Effective to date is required")
    private LocalDate effectiveTo;

    /**
     * Required: Days of the week the employee can work.
     * For PART_TIME_FLEX employees to specify which days they are available.
     * System will calculate specific dates within effectiveFrom/effectiveTo that
     * match these days.
     * 
     * Example: ["MONDAY", "THURSDAY"] - employee can work all Mondays and Thursdays
     * in the date range
     * Next month they might send ["TUESDAY", "FRIDAY"] if their schedule changes
     */
    @NotNull(message = "dayOfWeek is required for PART_TIME_FLEX registrations")
    @jakarta.validation.constraints.Size(min = 1, message = "At least one day of week is required")
    private List<String> dayOfWeek;

    // Constructors
    public CreateRegistrationRequest() {
    }

    public CreateRegistrationRequest(Long partTimeSlotId, LocalDate effectiveFrom,
            LocalDate effectiveTo, List<String> dayOfWeek) {
        this.partTimeSlotId = partTimeSlotId;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.dayOfWeek = dayOfWeek;
    }

    // Getters and Setters
    public Long getPartTimeSlotId() {
        return partTimeSlotId;
    }

    public void setPartTimeSlotId(Long partTimeSlotId) {
        this.partTimeSlotId = partTimeSlotId;
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

    public List<String> getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(List<String> dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
