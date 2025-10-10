package com.dental.clinic.management.domain.enums;

/**
 * Status of employee schedules (attendance tracking).
 */
public enum EmployeeScheduleStatus {
    /**
     * Schedule created, not yet checked in.
     */
    SCHEDULED,

    /**
     * Checked in on time.
     */
    PRESENT,

    /**
     * Checked in late.
     */
    LATE,

    /**
     * Did not show up.
     */
    ABSENT,

    /**
     * On approved leave.
     */
    ON_LEAVE
}
