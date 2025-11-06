package com.dental.clinic.management.exception.schedule;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

/**
 * Exception thrown when dentist exceeds max schedules per day limit.
 *
 * Business Rule: Maximum 2 work schedules per day for part-time dentists.
 *
 * Rationale:
 * - Work-life balance
 * - Prevent burnout
 * - Quality of care
 * - Reasonable earning opportunity
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MaxSchedulesExceededException extends RuntimeException {

    private static final int MAX_SCHEDULES_PER_DAY = 2;

    public MaxSchedulesExceededException(String message) {
        super(message);
    }

    public MaxSchedulesExceededException(LocalDate workDate, int currentCount) {
        super(String.format(
                "Ã„ÂÃƒÂ£ Ã„â€˜Ã¡ÂºÂ¡t giÃ¡Â»â€ºi hÃ¡ÂºÂ¡n lÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c cho ngÃƒÂ y %s: %d/%d ca. " +
                        "KhÃƒÂ´ng thÃ¡Â»Æ’ Ã„â€˜Ã„Æ’ng kÃƒÂ½ thÃƒÂªm.",
                workDate, currentCount, MAX_SCHEDULES_PER_DAY));
    }

    public MaxSchedulesExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public static int getMaxSchedulesPerDay() {
        return MAX_SCHEDULES_PER_DAY;
    }
}
