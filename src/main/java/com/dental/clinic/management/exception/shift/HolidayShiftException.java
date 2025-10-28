package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when attempting to create a shift on a holiday.
 */
public class HolidayShiftException extends ErrorResponseException {

    public HolidayShiftException(String message) {
        super(HttpStatus.CONFLICT, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Holiday Conflict");
        problemDetail.setProperty("code", "HOLIDAY_CONFLICT");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
