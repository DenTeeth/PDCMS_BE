package com.dental.clinic.management.exception;

/**
 * Exception thrown when a service is not found by ID
 */
public class ServiceNotFoundException extends ResourceNotFoundException {

    private static final String ERROR_CODE = "SERVICE_NOT_FOUND";

    public ServiceNotFoundException(Long serviceId) {
        super(ERROR_CODE, "Không tìm thấy dịch vụ với ID: " + serviceId);
    }

    public ServiceNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
