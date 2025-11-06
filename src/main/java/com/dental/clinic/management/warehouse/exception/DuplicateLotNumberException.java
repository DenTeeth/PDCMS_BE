package com.dental.clinic.management.warehouse.exception;

/**
 * Exception thrown when trying to create a batch with duplicate lot number for
 * the same item.
 */
public class DuplicateLotNumberException extends RuntimeException {

    public DuplicateLotNumberException(String itemName, String lotNumber) {
        super(String.format("Batch with lot number '%s' already exists for item '%s'",
                lotNumber, itemName));
    }

    public DuplicateLotNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}
