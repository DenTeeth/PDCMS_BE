package com.dental.clinic.management.exception;

/**
 * Exception thrown when attempting to update a completed or cancelled shift.
 */
public class ShiftUpdateNotAllowedException extends RuntimeException {

    public ShiftUpdateNotAllowedException(String message) {
        super(message);
    }
}
