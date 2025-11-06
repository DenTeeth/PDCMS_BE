package com.dental.clinic.management.exception.employee_shift;

import java.time.LocalDate;

/**
 * Exception thrown when trying to create a shift that conflicts with an existing shift slot.
 * Error Code: SLOT_CONFLICT
 */
public class SlotConflictException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Ca lÃƒÂ m viÃ¡Â»â€¡c Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i cho nhÃƒÂ¢n viÃƒÂªn %d vÃƒÂ o ngÃƒÂ y %s, ca %s";

    public SlotConflictException(Integer employeeId, LocalDate workDate, String workShiftName) {
        super(String.format(DEFAULT_MESSAGE, employeeId, workDate, workShiftName));
    }

    public SlotConflictException(String message) {
        super(message);
    }

    public SlotConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
