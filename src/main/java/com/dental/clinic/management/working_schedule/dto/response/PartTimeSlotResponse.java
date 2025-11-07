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
}
