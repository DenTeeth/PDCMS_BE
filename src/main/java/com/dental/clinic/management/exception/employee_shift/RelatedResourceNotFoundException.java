package com.dental.clinic.management.exception.employee_shift;

/**
 * Exception thrown when related resources (Employee, WorkShift) are not found.
 * Error Code: RELATED_RESOURCE_NOT_FOUND
 */
public class RelatedResourceNotFoundException extends RuntimeException {

    public RelatedResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y %s vÃ¡Â»â€ºi ID: %s", resourceType, resourceId));
    }

    public RelatedResourceNotFoundException(String message) {
        super(message);
    }

    public RelatedResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
