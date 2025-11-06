package com.dental.clinic.management.working_schedule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when the expiring fixed registration is inactive/deleted.
 */
public class RegistrationInactiveException extends ErrorResponseException {

    private static final String ERROR_CODE = "REGISTRATION_INACTIVE";

    public RegistrationInactiveException(Integer registrationId) {
        super(HttpStatus.CONFLICT, createProblemDetail(registrationId), null);
    }

    private static ProblemDetail createProblemDetail(Integer registrationId) {
        String message = String.format(
                "LÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c cÃ¡Â»â€˜ Ã„â€˜Ã¡Â»â€¹nh (ID: %d) Ã„â€˜ÃƒÂ£ bÃ¡Â»â€¹ hÃ¡Â»Â§y hoÃ¡ÂºÂ·c vÃƒÂ´ hiÃ¡Â»â€¡u hÃƒÂ³a. KhÃƒÂ´ng thÃ¡Â»Æ’ gia hÃ¡ÂºÂ¡n.",
                registrationId);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Registration Inactive");
        problemDetail.setProperty("errorCode", ERROR_CODE);
        problemDetail.setProperty("message", message);
        problemDetail.setProperty("registrationId", registrationId);

        return problemDetail;
    }
}
