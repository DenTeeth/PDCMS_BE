package com.dental.clinic.management.exception.fixed_registration;

/**
 * Exception thrown when employee_id parameter is required but missing.
 */
public class EmployeeIdRequiredException extends RuntimeException {

    public EmployeeIdRequiredException() {
        super("Vui lÃƒÂ²ng cung cÃ¡ÂºÂ¥p employee_id Ã„â€˜Ã¡Â»Æ’ xem.");
    }
}
