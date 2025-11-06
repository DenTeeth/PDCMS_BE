package com.dental.clinic.management.working_schedule.enums;

/**
 * Enum representing the source/origin of an employee shift assignment.
 */
public enum EmployeeShiftsSource {
    /**
     * Created automatically by batch job for full-time employees.
     * TÃ¡Â»Â« job tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng tÃ¡ÂºÂ¡o cho Full-time.
     */
    BATCH_JOB,

    /**
     * Created automatically by registration job for part-time employees.
     * TÃ¡Â»Â« job tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng tÃ¡ÂºÂ¡o cho Part-time.
     */
    REGISTRATION_JOB,

    /**
     * Created from overtime approval.
     * TÃ¡Â»Â« viÃ¡Â»â€¡c duyÃ¡Â»â€¡t OT.
     */
    OT_APPROVAL,

    /**
     * Created manually by manager/admin.
     * Do quÃ¡ÂºÂ£n lÃƒÂ½/admin tÃ¡ÂºÂ¡o thÃ¡Â»Â§ cÃƒÂ´ng.
     */
    MANUAL_ENTRY
}
