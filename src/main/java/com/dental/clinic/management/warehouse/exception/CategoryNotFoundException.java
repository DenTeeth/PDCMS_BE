package com.dental.clinic.management.warehouse.exception;

import java.util.UUID;

/**
 * Exception thrown when category is not found.
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(UUID id) {
        super(String.format("Category not found with ID: %s", id));
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
