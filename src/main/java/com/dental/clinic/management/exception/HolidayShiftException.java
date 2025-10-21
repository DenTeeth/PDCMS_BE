package com.dental.clinic.management.exception;

/**
 * Exception thrown when attempting to create a shift on a holiday.
 */
public class HolidayShiftException extends RuntimeException {

    public HolidayShiftException(String message) {
        super(message);
    }
}
