package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when attempting to cancel a completed shift.
 */
public class CannotCancelCompletedException extends ErrorResponseException {

    public CannotCancelCompletedException(String message) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Cannot Cancel Completed");
        problemDetail.setProperty("code", "CANNOT_CANCEL_COMPLETED");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
