package com.dental.clinic.management.exception.schedule;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to cancel/modify already booked schedule.
 *
 * Business Rule: BOOKED schedules cannot be cancelled directly.
 *
 * Workflow:
 * 1. Reschedule or cancel patient appointments first
 * 2. Schedule status will auto-update to AVAILABLE
 * 3. Then dentist can cancel the schedule
 *
 * Rationale:
 * - Patient commitment protection
 * - Payment guarantee ("Ã„â€˜Ã„Æ’ng kÃƒÂ½ lÃƒÂªn ngÃ¡Â»â€œi lÃƒÂ  phÃ¡ÂºÂ£i trÃ¡ÂºÂ£ tiÃ¡Â»Ân")
 * - Prevent revenue loss
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ScheduleAlreadyBookedException extends RuntimeException {

    public ScheduleAlreadyBookedException(String message) {
        super(message);
    }

    public ScheduleAlreadyBookedException(String scheduleCode, int appointmentCount) {
        super(String.format(
                "KhÃƒÂ´ng thÃ¡Â»Æ’ hÃ¡Â»Â§y lÃ¡Â»â€¹ch %s. Ã„ÂÃƒÂ£ cÃƒÂ³ %d lÃ¡Â»â€¹ch hÃ¡ÂºÂ¹n bÃ¡Â»â€¡nh nhÃƒÂ¢n. " +
                        "Vui lÃƒÂ²ng hÃ¡Â»Â§y/chuyÃ¡Â»Æ’n lÃ¡Â»â€¹ch hÃ¡ÂºÂ¹n trÃ†Â°Ã¡Â»â€ºc khi hÃ¡Â»Â§y ca lÃƒÂ m viÃ¡Â»â€¡c.",
                scheduleCode, appointmentCount));
    }

    public ScheduleAlreadyBookedException(String message, Throwable cause) {
        super(message, cause);
    }
}
