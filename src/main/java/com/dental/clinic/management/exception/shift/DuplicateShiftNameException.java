package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when trying to create a work shift with a duplicate name.
 * LÃ¡Â»â€”i 2: Duplicate shift name validation
 */
public class DuplicateShiftNameException extends ErrorResponseException {

    public DuplicateShiftNameException(String shiftName) {
        super(HttpStatus.CONFLICT, asProblemDetail(shiftName), null);
    }

    private static ProblemDetail asProblemDetail(String shiftName) {
        String message = String.format("TÃƒÂªn ca lÃƒÂ m viÃ¡Â»â€¡c '%s' Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i. Vui lÃƒÂ²ng chÃ¡Â»Ân tÃƒÂªn khÃƒÂ¡c.", shiftName);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Duplicate Shift Name");
        problemDetail.setProperty("errorCode", "DUPLICATE_SHIFT_NAME");
        problemDetail.setProperty("message", message);
        problemDetail.setProperty("shiftName", shiftName);
        return problemDetail;
    }
}
