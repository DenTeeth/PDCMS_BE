package com.dental.clinic.management.exception.holiday;

import java.time.LocalDate;

/**
 * Exception thrown when date range query has invalid parameters (start date > end date).
 * Error Code: INVALID_DATE_RANGE
 */
public class InvalidDateRangeException extends RuntimeException {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public InvalidDateRangeException(LocalDate startDate, LocalDate endDate) {
        super("NgÃƒÂ y bÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u phÃ¡ÂºÂ£i nhÃ¡Â»Â hÃ†Â¡n hoÃ¡ÂºÂ·c bÃ¡ÂºÂ±ng ngÃƒÂ y kÃ¡ÂºÂ¿t thÃƒÂºc");
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
