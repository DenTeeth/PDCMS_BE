package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when shift duration violates business rules.
 *
 * Business Rule: Shifts must be between 3-8 hours.
 *
 * Applies to:
 * - WorkShift creation/update
 * - DentistWorkSchedule with custom times
 * - RecurringSchedule with custom times
 */
public class InvalidShiftDurationException extends ErrorResponseException {

    public InvalidShiftDurationException(String message) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(message), null);
    }

    public InvalidShiftDurationException(int actualHours) {
        this(String.format("ThÃ¡Â»Âi lÃ†Â°Ã¡Â»Â£ng ca lÃƒÂ m viÃ¡Â»â€¡c khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡: %d giÃ¡Â»Â. YÃƒÂªu cÃ¡ÂºÂ§u: 3-8 giÃ¡Â»Â", actualHours));
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Invalid Shift Duration");
        problemDetail.setProperty("errorCode", "INVALID_DURATION");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
