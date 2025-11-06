package com.dental.clinic.management.exception.authorization;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when employee attempts unauthorized schedule operation.
 *
 * Authorization Rules:
 * - Dentists can only manage their own schedules
 * - Admin can manage all schedules
 * - HR can view and update attendance status only
 *
 * Security:
 * - Prevent schedule manipulation
 * - Audit trail protection
 * - Role-based access control
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class EmployeeNotAuthorizedException extends RuntimeException {

    public EmployeeNotAuthorizedException(String message) {
        super(message);
    }

    public EmployeeNotAuthorizedException(String employeeCode, String operation) {
        super(String.format(
                "NhÃƒÂ¢n viÃƒÂªn %s khÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân thÃ¡Â»Â±c hiÃ¡Â»â€¡n: %s. " +
                        "ChÃ¡Â»â€° cÃƒÂ³ thÃ¡Â»Æ’ quÃ¡ÂºÂ£n lÃƒÂ½ lÃ¡Â»â€¹ch cÃ¡Â»Â§a chÃƒÂ­nh mÃƒÂ¬nh.",
                employeeCode, operation));
    }

    public EmployeeNotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
