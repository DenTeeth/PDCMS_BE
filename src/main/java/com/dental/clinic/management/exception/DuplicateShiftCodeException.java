package com.dental.clinic.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when shift code is duplicated.
 */
public class DuplicateShiftCodeException extends ErrorResponseException {

    public DuplicateShiftCodeException(String shiftCode) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(shiftCode), null);
    }

    private static ProblemDetail asProblemDetail(String shiftCode) {
        String message = String.format("Work shift with code '%s' already exists", shiftCode);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle(message);
        problemDetail.setProperty("message", "error.work.shift.duplicate.code");
        return problemDetail;
    }
}
