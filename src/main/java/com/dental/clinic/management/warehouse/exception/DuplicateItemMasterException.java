package com.dental.clinic.management.warehouse.exception;

/**
 * Exception thrown when trying to create an item master with duplicate name.
 */
public class DuplicateItemMasterException extends RuntimeException {

    public DuplicateItemMasterException(String itemName) {
        super(String.format("Item master with name '%s' already exists", itemName));
    }

    public DuplicateItemMasterException(String message, Throwable cause) {
        super(message, cause);
    }
}
