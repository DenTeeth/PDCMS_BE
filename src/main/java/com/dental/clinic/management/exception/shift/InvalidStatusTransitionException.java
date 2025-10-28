package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when an invalid status transition is attempted.
 */
public class InvalidStatusTransitionException extends ErrorResponseException {

    public InvalidStatusTransitionException(String message) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Invalid Status Transition");
        problemDetail.setProperty("code", "INVALID_STATUS_TRANSITION");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
