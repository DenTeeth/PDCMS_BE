package com.dental.clinic.management.exception;

/**
 * Exception thrown when an employee shift is not found.
 */
public class EmployeeShiftNotFoundException extends RuntimeException {

    public EmployeeShiftNotFoundException(String message) {
        super(message);
    }
}
