package com.dental.clinic.management.domain.enums;

/**
 * Status of dentist work schedules.
 */
public enum DentistWorkScheduleStatus {
    /**
     * Schedule registered, no appointments yet.
     */
    AVAILABLE,

    /**
     * Has appointments booked.
     */
    BOOKED,

    /**
     * Dentist cancelled the schedule.
     */
    CANCELLED,

    /**
     * Schedule date has passed.
     */
    EXPIRED
}
