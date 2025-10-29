package com.dental.clinic.management.working_schedule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class RegistrationNotFoundException extends ErrorResponseException {

    private static final String ERROR_CODE = "REGISTRATION_NOT_FOUND";

    public RegistrationNotFoundException(String registrationId) {
        super(HttpStatus.NOT_FOUND, createProblemDetail(registrationId), null);
    }

    private static ProblemDetail createProblemDetail(String registrationId) {
        String message = String.format(
            "Không tìm thấy đăng ký với ID: %s.",
            registrationId
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setTitle("Registration Not Found");
        problemDetail.setProperty("errorCode", ERROR_CODE);
        problemDetail.setProperty("message", message);
        problemDetail.setProperty("registrationId", registrationId);

        return problemDetail;
    }
}
