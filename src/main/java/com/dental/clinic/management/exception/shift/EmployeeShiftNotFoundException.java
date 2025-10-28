package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when an employee shift is not found.
 */
public class EmployeeShiftNotFoundException extends ErrorResponseException {

    public EmployeeShiftNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setTitle("Shift Not Found");
        problemDetail.setProperty("code", "SHIFT_NOT_FOUND");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
