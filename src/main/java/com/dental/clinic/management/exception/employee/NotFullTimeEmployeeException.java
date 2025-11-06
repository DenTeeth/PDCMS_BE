package com.dental.clinic.management.exception.employee;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when non-full-time employee attempts recurring schedule.
 *
 * Business Rule: Only FULL_TIME employees can have recurring schedules.
 *
 * Rationale:
 * - Full-time = fixed monthly salary = fixed weekly pattern
 * - Part-time = hourly payment = flexible self-registration
 * - System design: recurring_schedules table is for full-time only
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotFullTimeEmployeeException extends RuntimeException {

    public NotFullTimeEmployeeException(String message) {
        super(message);
    }

    public NotFullTimeEmployeeException(String employeeCode, String employmentType) {
        super(String.format(
                "NhÃƒÂ¢n viÃƒÂªn %s (loÃ¡ÂºÂ¡i: %s) khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c tÃ¡ÂºÂ¡o lÃ¡Â»â€¹ch cÃ¡Â»â€˜ Ã„â€˜Ã¡Â»â€¹nh. " +
                        "ChÃ¡Â»â€° nhÃƒÂ¢n viÃƒÂªn FULL_TIME mÃ¡Â»â€ºi cÃƒÂ³ lÃ¡Â»â€¹ch tuÃ¡ÂºÂ§n cÃ¡Â»â€˜ Ã„â€˜Ã¡Â»â€¹nh.",
                employeeCode, employmentType));
    }

    public NotFullTimeEmployeeException(String message, Throwable cause) {
        super(message, cause);
    }
}
