package com.dental.clinic.management.exception.employee_shift;

import java.time.LocalTime;

/**
 * Exception thrown when attempting to create overlapping shifts for the same
 * employee.
 */
public class TimeOverlapConflictException extends RuntimeException {

    public TimeOverlapConflictException(LocalTime newStart, LocalTime newEnd, LocalTime existingStart,
            LocalTime existingEnd) {
        super(String.format("NhÃƒÂ¢n viÃƒÂªn Ã„â€˜ÃƒÂ£ cÃƒÂ³ ca lÃƒÂ m viÃ¡Â»â€¡c chÃ¡Â»â€œng lÃ¡ÂºÂ¥n thÃ¡Â»Âi gian. " +
                "Ca mÃ¡Â»â€ºi (%s - %s) trÃƒÂ¹ng vÃ¡Â»â€ºi ca hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i (%s - %s). " +
                "Vui lÃƒÂ²ng chÃ¡Â»Ân ca lÃƒÂ m viÃ¡Â»â€¡c khÃƒÂ¡c.",
                newStart, newEnd, existingStart, existingEnd));
    }
}
