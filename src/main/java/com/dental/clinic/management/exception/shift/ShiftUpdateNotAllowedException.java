package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when shift update is not allowed.
 */
public class ShiftUpdateNotAllowedException extends ErrorResponseException {

    public ShiftUpdateNotAllowedException(String message) {
        super(HttpStatus.CONFLICT, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Shift Finalized");
        problemDetail.setProperty("code", "SHIFT_FINALIZED");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
