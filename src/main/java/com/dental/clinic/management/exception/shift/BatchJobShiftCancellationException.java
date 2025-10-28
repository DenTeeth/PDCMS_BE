package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when attempting to cancel a batch job shift.
 */
public class BatchJobShiftCancellationException extends ErrorResponseException {

    public BatchJobShiftCancellationException(String message) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Cannot Cancel Batch");
        problemDetail.setProperty("code", "CANNOT_CANCEL_BATCH");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
