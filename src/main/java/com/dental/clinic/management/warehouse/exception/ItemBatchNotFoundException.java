package com.dental.clinic.management.warehouse.exception;

/**
 * Exception thrown when item batch is not found.
 */
public class ItemBatchNotFoundException extends RuntimeException {

    public ItemBatchNotFoundException(Long id) {
        super(String.format("Item batch not found with ID: %s", id));
    }

    public ItemBatchNotFoundException(String message) {
        super(message);
    }
}
