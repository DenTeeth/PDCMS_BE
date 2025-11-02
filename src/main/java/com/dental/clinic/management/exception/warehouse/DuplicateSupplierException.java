package com.dental.clinic.management.exception.warehouse;

/**
 * Exception thrown when trying to create a supplier with duplicate name or
 * phone.
 */
public class DuplicateSupplierException extends RuntimeException {
    public DuplicateSupplierException(String message) {
        super(message);
    }
}
