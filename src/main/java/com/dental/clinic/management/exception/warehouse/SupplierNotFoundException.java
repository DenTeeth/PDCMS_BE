package com.dental.clinic.management.exception.warehouse;

/**
 * Exception thrown when a supplier is not found.
 */
public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(String message) {
        super(message);
    }
}
