package com.dental.clinic.management.working_schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    
    /**
     * List of months (YYYY-MM format) that have at least one working day available for this slot.
     * Used by frontend month picker to enable/disable months.
     * Example: ["2025-11", "2025-12", "2026-01"]
     */
    private List<String> availableMonths;
}
