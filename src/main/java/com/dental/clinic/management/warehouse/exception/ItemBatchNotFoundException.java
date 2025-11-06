package com.dental.clinic.management.warehouse.exception;

import java.util.UUID;

/**
 * Exception thrown when item batch is not found.
 */
public class ItemBatchNotFoundException extends RuntimeException {

    public ItemBatchNotFoundException(UUID id) {
        super(String.format("Item batch not found with ID: %s", id));
    }

    public ItemBatchNotFoundException(String message) {
        super(message);
    }
}
