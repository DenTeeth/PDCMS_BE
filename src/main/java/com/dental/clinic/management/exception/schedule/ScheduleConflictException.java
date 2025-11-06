package com.dental.clinic.management.exception.schedule;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Exception thrown when schedule has time conflicts with existing schedules.
 *
 * Conflict Definition: Overlapping time ranges on the same date.
 *
 * Checked for:
 * - DentistWorkSchedule (same dentist, same date)
 * - RecurringSchedule (same employee, same day of week)
 * - WorkShift (any overlapping shift templates)
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ScheduleConflictException extends RuntimeException {

    public ScheduleConflictException(String message) {
        super(message);
    }

    public ScheduleConflictException(LocalDate date, LocalTime startTime, LocalTime endTime,
            String conflictingScheduleCode) {
        super(String.format(
                "TrÃƒÂ¹ng lÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c ngÃƒÂ y %s (%s - %s) vÃ¡Â»â€ºi lÃ¡Â»â€¹ch %s. " +
                        "Vui lÃƒÂ²ng chÃ¡Â»Ân thÃ¡Â»Âi gian khÃƒÂ¡c.",
                date, startTime, endTime, conflictingScheduleCode));
    }

    public ScheduleConflictException(String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        super(String.format(
                "TrÃƒÂ¹ng lÃ¡Â»â€¹ch cÃ¡Â»â€˜ Ã„â€˜Ã¡Â»â€¹nh thÃ¡Â»Â© %s (%s - %s). " +
                        "Vui lÃƒÂ²ng kiÃ¡Â»Æ’m tra lÃ¡ÂºÂ¡i lÃ¡Â»â€¹ch tuÃ¡ÂºÂ§n.",
                dayOfWeek, startTime, endTime));
    }

    public ScheduleConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
