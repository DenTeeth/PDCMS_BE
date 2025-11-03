package com.dental.clinic.management.exception.warehouse;

/**
 * Exception thrown when trying to create inventory with duplicate item name.
 */
public class DuplicateInventoryException extends RuntimeException {
    public DuplicateInventoryException(String message) {
        super(message);
    }
}
