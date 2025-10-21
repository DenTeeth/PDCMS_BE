package com.dental.clinic.management.exception;

/**
 * Exception thrown when a shift conflict is detected.
 */
public class ShiftConflictException extends RuntimeException {

    public ShiftConflictException(String message) {
        super(message);
    }
}
