package com.dental.clinic.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when shift duration violates business rules.
 *
 * Business Rule: Shifts must be between 3-8 hours.
 *
 * Applies to:
 * - WorkShift creation/update
 * - DentistWorkSchedule with custom times
 * - RecurringSchedule with custom times
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidShiftDurationException extends RuntimeException {

    public InvalidShiftDurationException(String message) {
        super(message);
    }

    public InvalidShiftDurationException(int actualHours) {
        super(String.format("Thời lượng ca làm việc không hợp lệ: %d giờ. Yêu cầu: 3-8 giờ", actualHours));
    }

    public InvalidShiftDurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
