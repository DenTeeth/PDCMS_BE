package com.dental.clinic.management.exception;

/**
 * Exception thrown when a service exists but has no consumables defined
 */
public class NoConsumablesDefinedException extends ResourceNotFoundException {

    private static final String ERROR_CODE = "NO_CONSUMABLES_DEFINED";

    public NoConsumablesDefinedException(Long serviceId) {
        super(ERROR_CODE, "No consumables defined for service ID: " + serviceId
                + ". Please configure consumables in service management.");
    }

    public NoConsumablesDefinedException(String message) {
        super(ERROR_CODE, message);
    }
}
