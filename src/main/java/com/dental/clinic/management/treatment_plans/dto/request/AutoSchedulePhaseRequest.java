package com.dental.clinic.management.treatment_plans.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for automatic appointment scheduling from a specific treatment plan phase.
 * 
 * NEW: Phase-level auto-scheduling (more realistic than whole plan)
 * Date: 2024-12-29
 * 
 * Features:
 * - Schedule appointments for a specific phase only
 * - Automatically skip holidays and weekends
 * - Apply service spacing rules (preparation, recovery, intervals)
 * - Enforce daily appointment limits
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoSchedulePhaseRequest {
    
    /**
     * Preferred employee (doctor) code for all appointments in this phase.
     * If not specified, system will suggest available doctors.
     */
    private String employeeCode;
    
    /**
     * Preferred room code for all appointments in this phase.
     * If not specified, system will suggest available rooms.
     */
    private String roomCode;
    
    /**
     * Preferred time slots for appointments.
     * Options: MORNING (8:00-12:00), AFTERNOON (13:00-17:00), EVENING (17:00-20:00)
     * If empty, system will suggest all available slots.
     */
    private List<String> preferredTimeSlots;
    
    /**
     * Maximum days to look ahead for available slots.
     * Default: 90 days (3 months)
     * Used to limit search range when dates are far in the future.
     */
    @Builder.Default
    private Integer lookAheadDays = 90;
    
    /**
     * Whether to ignore spacing rules (emergency mode).
     * Default: false
     * WARNING: Only use for urgent cases where normal scheduling cannot be followed.
     */
    @Builder.Default
    private Boolean forceSchedule = false;
}
