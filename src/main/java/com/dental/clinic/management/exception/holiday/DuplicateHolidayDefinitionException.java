package com.dental.clinic.management.exception.holiday;

/**
 * Exception thrown when attempting to create a holiday definition that already exists.
 * Error Code: DUPLICATE_HOLIDAY_DEFINITION
 */
public class DuplicateHolidayDefinitionException extends RuntimeException {

    private final String definitionId;

    public DuplicateHolidayDefinitionException(String definitionId) {
        super(String.format("Ã„ÂÃ¡Â»â€¹nh nghÃ„Â©a ngÃƒÂ y nghÃ¡Â»â€° Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i: %s", definitionId));
        this.definitionId = definitionId;
    }

    public String getDefinitionId() {
        return definitionId;
    }
}
