package com.dental.clinic.management.warehouse.exception;

/**
 * Exception thrown when trying to create a supplier with duplicate name.
 */
public class DuplicateSupplierException extends RuntimeException {

    public DuplicateSupplierException(String supplierName) {
        super(String.format("Supplier with name '%s' already exists", supplierName));
    }

    public DuplicateSupplierException(String message, Throwable cause) {
        super(message, cause);
    }
}
