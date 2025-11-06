package com.dental.clinic.management.warehouse.exception;

/**
 * Exception thrown when item master is not found.
 */
public class ItemMasterNotFoundException extends RuntimeException {

    public ItemMasterNotFoundException(Long id) {
        super(String.format("Item master not found with ID: %s", id));
    }

    public ItemMasterNotFoundException(String message) {
        super(message);
    }
}
