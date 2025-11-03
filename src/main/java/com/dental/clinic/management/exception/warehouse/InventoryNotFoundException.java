package com.dental.clinic.management.exception.warehouse;

/**
 * Exception thrown when inventory item not found.
 */
public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String message) {
        super(message);
    }
}
