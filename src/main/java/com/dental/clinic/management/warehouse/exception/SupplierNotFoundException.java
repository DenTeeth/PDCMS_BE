package com.dental.clinic.management.warehouse.exception;

import java.util.UUID;

/**
 * Exception thrown when supplier is not found.
 */
public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(UUID id) {
        super(String.format("Supplier not found with ID: %s", id));
    }

    public SupplierNotFoundException(String message) {
        super(message);
    }
}
