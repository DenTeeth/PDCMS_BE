package com.dental.clinic.management.exception;

/**
 * Exception thrown when attempting to cancel a batch job shift.
 */
public class BatchJobShiftCancellationException extends RuntimeException {

    public BatchJobShiftCancellationException(String message) {
        super(message);
    }
}
