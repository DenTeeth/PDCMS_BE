package com.dental.clinic.management.working_schedule.enums;

/**
 * Source of how an employee shift was created.
 * Maps to employee_shifts_source ENUM in database.
 */
public enum ShiftSource {
    /**
     * Created by monthly batch job for full-time employees.
     * TÃ¡Â»Â« job tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng tÃ¡ÂºÂ¡o cho Full-time.
     */
    BATCH_JOB,

    /**
     * Created by weekly job based on part-time employee registration.
     * TÃ¡Â»Â« job tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng tÃ¡ÂºÂ¡o cho Part-time.
     */
    REGISTRATION_JOB,

    /**
     * Created from approved overtime request.
     * TÃ¡Â»Â« viÃ¡Â»â€¡c duyÃ¡Â»â€¡t OT.
     */
    OT_APPROVAL,

    /**
     * Manually created by admin/manager.
     * Do quÃ¡ÂºÂ£n lÃƒÂ½/admin tÃ¡ÂºÂ¡o thÃ¡Â»Â§ cÃƒÂ´ng.
     */
    MANUAL_ENTRY
}
