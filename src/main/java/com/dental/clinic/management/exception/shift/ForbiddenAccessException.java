package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when access is forbidden.
 */
public class ForbiddenAccessException extends ErrorResponseException {

    public ForbiddenAccessException(String message) {
        super(HttpStatus.FORBIDDEN, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, message);
        problemDetail.setTitle("Forbidden");
        problemDetail.setProperty("code", "FORBIDDEN");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
