package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when a related resource (employee or work shift) is not found.
 */
public class RelatedResourceNotFoundException extends ErrorResponseException {

    public RelatedResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setTitle("Related Resource Not Found");
        problemDetail.setProperty("code", "RELATED_RESOURCE_NOT_FOUND");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
