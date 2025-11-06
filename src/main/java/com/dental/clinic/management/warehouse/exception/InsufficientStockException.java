package com.dental.clinic.management.warehouse.exception;

/**
 * Exception thrown when trying to export more stock than available.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long itemMasterId, Integer requested, Integer available) {
        super(String.format("Insufficient stock for item %s. Requested: %d, Available: %d",
                itemMasterId, requested, available));
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}
