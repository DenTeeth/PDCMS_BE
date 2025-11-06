package com.dental.clinic.management.warehouse.exception;

/**
 * Exception thrown when category is not found.
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id) {
        super(String.format("Category not found with ID: %s", id));
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
