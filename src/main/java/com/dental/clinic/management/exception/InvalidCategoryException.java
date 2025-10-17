package com.dental.clinic.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when shift category doesn't match time requirements.
 */
public class InvalidCategoryException extends ErrorResponseException {

    public InvalidCategoryException(String message) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle(message);
        problemDetail.setProperty("message", "error.work.shift.invalid.category");
        return problemDetail;
    }
}
