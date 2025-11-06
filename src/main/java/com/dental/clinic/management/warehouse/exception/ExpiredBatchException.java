package com.dental.clinic.management.warehouse.exception;

import java.time.LocalDate;

/**
 * Exception thrown when trying to use an expired batch.
 */
public class ExpiredBatchException extends RuntimeException {

    public ExpiredBatchException(Long batchId, LocalDate expiryDate) {
        super(String.format("Batch %s is expired (expiry date: %s)", batchId, expiryDate));
    }

    public ExpiredBatchException(String message) {
        super(message);
    }
}
