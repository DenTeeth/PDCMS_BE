package com.dental.clinic.management.exception.work_shift;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when WorkShift has dependencies preventing modification.
 *
 * Dependencies:
 * - RecurringSchedules using this shift
 * - Future EmployeeSchedules generated from recurring patterns
 *
 * Business Rule: Cannot delete/disable shift with active usage.
 *
 * Solution: Set isActive = false instead of deleting, or reassign dependencies
 * first.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class WorkShiftInUseException extends RuntimeException {

    public WorkShiftInUseException(String message) {
        super(message);
    }

    public WorkShiftInUseException(String shiftCode, int recurringCount, int scheduleCount) {
        super(String.format(
                "KhÃƒÂ´ng thÃ¡Â»Æ’ xÃƒÂ³a/vÃƒÂ´ hiÃ¡Â»â€¡u hÃƒÂ³a ca %s. Ã„Âang Ã„â€˜Ã†Â°Ã¡Â»Â£c sÃ¡Â»Â­ dÃ¡Â»Â¥ng bÃ¡Â»Å¸i: " +
                        "%d lÃ¡Â»â€¹ch cÃ¡Â»â€˜ Ã„â€˜Ã¡Â»â€¹nh, %d lÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c. " +
                        "Vui lÃƒÂ²ng chuyÃ¡Â»Æ’n sang ca khÃƒÂ¡c trÃ†Â°Ã¡Â»â€ºc.",
                shiftCode, recurringCount, scheduleCount));
    }

    public WorkShiftInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}
