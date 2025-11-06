package com.dental.clinic.management.warehouse.exception;

import java.util.UUID;

/**
 * Exception thrown when item master is not found.
 */
public class ItemMasterNotFoundException extends RuntimeException {

    public ItemMasterNotFoundException(UUID id) {
        super(String.format("Item master not found with ID: %s", id));
    }

    public ItemMasterNotFoundException(String message) {
        super(message);
    }
}
