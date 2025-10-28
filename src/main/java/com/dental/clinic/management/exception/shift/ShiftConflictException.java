package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when there's a shift conflict.
 */
public class ShiftConflictException extends ErrorResponseException {

    public ShiftConflictException(String message) {
        super(HttpStatus.CONFLICT, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Slot Conflict");
        problemDetail.setProperty("code", "SLOT_CONFLICT");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
