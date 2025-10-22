package com.dental.clinic.management.working_schedule.enums;

/**
 * Source of how an employee shift was created.
 */
public enum ShiftSource {
    /**
     * Created by monthly batch job for full-time employees.
     */
    BATCH_JOB,

    /**
     * Created by weekly job based on part-time employee registration.
     */
    REGISTRATION_JOB,

    /**
     * Manually created by admin/manager.
     */
    MANUAL,

    /**
     * Created from approved overtime request.
     */
    OVERTIME
}
