package com.dental.clinic.management.working_schedule.enums;

/**
 * Enum representing the status of an employee's shift.
 * Used to track shift lifecycle and attendance.
 */
public enum ShiftStatus {
    /**
     * Shift has been scheduled (initial state).
     * Ã„ÂÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c xÃ¡ÂºÂ¿p lÃ¡Â»â€¹ch (trÃ¡ÂºÂ¡ng thÃƒÂ¡i khÃ¡Â»Å¸i tÃ¡ÂºÂ¡o).
     */
    SCHEDULED,

    /**
     * Employee is on leave.
     * NghÃ¡Â»â€° phÃƒÂ©p.
     */
    ON_LEAVE,

    /**
     * Employee has completed the shift.
     * NhÃƒÂ¢n viÃƒÂªn Ã„â€˜ÃƒÂ£ hoÃƒÂ n thÃƒÂ nh ca lÃƒÂ m.
     */
    COMPLETED,

    /**
     * Employee was absent without permission.
     * NhÃƒÂ¢n viÃƒÂªn vÃ¡ÂºÂ¯ng mÃ¡ÂºÂ·t khÃƒÂ´ng phÃƒÂ©p.
     */
    ABSENT,

    /**
     * Shift was cancelled by management before it occurred.
     * Ca lÃƒÂ m Ã„â€˜ÃƒÂ£ bÃ¡Â»â€¹ quÃ¡ÂºÂ£n lÃƒÂ½ hÃ¡Â»Â§y trÃ†Â°Ã¡Â»â€ºc khi diÃ¡Â»â€¦n ra.
     */
    CANCELLED
}
