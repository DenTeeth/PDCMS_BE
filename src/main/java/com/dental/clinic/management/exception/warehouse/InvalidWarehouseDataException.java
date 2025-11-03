package com.dental.clinic.management.exception.warehouse;

/**
 * Exception thrown when warehouse type validation fails
 * (e.g., COLD warehouse without expiry date).
 */
public class InvalidWarehouseDataException extends RuntimeException {
    public InvalidWarehouseDataException(String message) {
        super(message);
    }
}
