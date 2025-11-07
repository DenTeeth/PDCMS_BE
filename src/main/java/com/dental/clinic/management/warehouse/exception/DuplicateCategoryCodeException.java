package com.dental.clinic.management.warehouse.exception;

public class DuplicateCategoryCodeException extends RuntimeException {
    public DuplicateCategoryCodeException(String categoryCode) {
        super("Category with code '" + categoryCode + "' already exists");
    }
}
