package com.dental.clinic.management.warehouse.exception;

/**
 * Exception thrown when supplier is not found.
 */
public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(Long id) {
        super(String.format("Supplier not found with ID: %s", id));
    }

    public SupplierNotFoundException(String message) {
        super(message);
    }
}
