package com.dental.clinic.management.exception.employee;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when non-part-time employee attempts flexible scheduling.
 *
 * Business Rule: Only PART_TIME dentists can register flexible work schedules.
 *
 * Rationale:
 * - Full-time employees have fixed recurring schedules
 * - Part-time hourly payment model requires flexible registration
 * - System design: dentist_work_schedules table is for part-time only
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotPartTimeDentistException extends RuntimeException {

    public NotPartTimeDentistException(String message) {
        super(message);
    }

    public NotPartTimeDentistException(String employeeCode, String employmentType) {
        super(String.format(
                "NhÃƒÂ¢n viÃƒÂªn %s (loÃ¡ÂºÂ¡i: %s) khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã„Æ’ng kÃƒÂ½ lÃ¡Â»â€¹ch linh hoÃ¡ÂºÂ¡t. " +
                        "ChÃ¡Â»â€° nhÃƒÂ¢n viÃƒÂªn PART_TIME mÃ¡Â»â€ºi cÃƒÂ³ quyÃ¡Â»Ân nÃƒÂ y.",
                employeeCode, employmentType));
    }

    public NotPartTimeDentistException(String message, Throwable cause) {
        super(message, cause);
    }
}
