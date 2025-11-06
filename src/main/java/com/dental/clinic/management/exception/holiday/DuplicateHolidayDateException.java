package com.dental.clinic.management.exception.holiday;

import java.time.LocalDate;

/**
 * Exception thrown when attempting to create a holiday date that already exists.
 * Error Code: DUPLICATE_HOLIDAY_DATE
 */
public class DuplicateHolidayDateException extends RuntimeException {

    private final LocalDate holidayDate;
    private final String definitionId;

    public DuplicateHolidayDateException(LocalDate holidayDate, String definitionId) {
        super(String.format("NgÃƒÂ y nghÃ¡Â»â€° Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i: %s cho Ã„â€˜Ã¡Â»â€¹nh nghÃ„Â©a %s", holidayDate, definitionId));
        this.holidayDate = holidayDate;
        this.definitionId = definitionId;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public String getDefinitionId() {
        return definitionId;
    }
}
