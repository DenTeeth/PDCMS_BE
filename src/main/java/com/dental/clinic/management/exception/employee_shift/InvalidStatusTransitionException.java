package com.dental.clinic.management.exception.employee_shift;

import com.dental.clinic.management.working_schedule.enums.ShiftStatus;

/**
 * Exception thrown when trying to transition to an invalid status.
 * Error Code: INVALID_STATUS_TRANSITION
 */
public class InvalidStatusTransitionException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "KhÃƒÂ´ng thÃ¡Â»Æ’ chuyÃ¡Â»Æ’n trÃ¡ÂºÂ¡ng thÃƒÂ¡i ca lÃƒÂ m viÃ¡Â»â€¡c tÃ¡Â»Â« %s sang %s";

    public InvalidStatusTransitionException(ShiftStatus currentStatus, ShiftStatus newStatus) {
        super(String.format(DEFAULT_MESSAGE, currentStatus, newStatus));
    }

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
