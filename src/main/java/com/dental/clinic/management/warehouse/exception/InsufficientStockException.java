package com.dental.clinic.management.warehouse.exception;

import java.util.UUID;

/**
 * Exception thrown when trying to export more stock than available.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(UUID itemMasterId, Integer requested, Integer available) {
        super(String.format("Insufficient stock for item %s. Requested: %d, Available: %d",
                itemMasterId, requested, available));
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}
