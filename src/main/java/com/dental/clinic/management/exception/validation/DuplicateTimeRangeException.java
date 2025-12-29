package com.dental.clinic.management.exception.validation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when trying to create a work shift with overlapping or
 * duplicate time range.
 * Lỗi 1: Duplicate time range validation
 */
public class DuplicateTimeRangeException extends ErrorResponseException {

    public DuplicateTimeRangeException(String message) {
        super(HttpStatus.CONFLICT, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Khoảng Thời Gian Trùng Lặp");
        problemDetail.setProperty("errorCode", "DUPLICATE_TIME_RANGE");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
