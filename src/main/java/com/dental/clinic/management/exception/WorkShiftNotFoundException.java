package com.dental.clinic.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when work shift is not found.
 */
public class WorkShiftNotFoundException extends ErrorResponseException {

    public WorkShiftNotFoundException(String workShiftId) {
        super(HttpStatus.NOT_FOUND, asProblemDetail(workShiftId), null);
    }

    private static ProblemDetail asProblemDetail(String workShiftId) {
        String message = String.format("Work shift not found with ID: %s", workShiftId);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setTitle(message);
        problemDetail.setProperty("message", "error.work.shift.not.found");
        return problemDetail;
    }
}
