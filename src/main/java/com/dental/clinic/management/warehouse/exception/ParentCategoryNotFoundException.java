package com.dental.clinic.management.warehouse.exception;

public class ParentCategoryNotFoundException extends RuntimeException {
    public ParentCategoryNotFoundException(Long parentId) {
        super("Parent category with ID " + parentId + " not found");
    }
}
