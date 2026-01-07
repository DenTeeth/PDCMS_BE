package com.dental.clinic.management.exception.registration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when shift registration conflicts with existing active
 * registration.
 */
public class RegistrationConflictException extends ErrorResponseException {

    public RegistrationConflictException(String workShiftId, String dayOfWeek) {
        super(HttpStatus.CONFLICT, asProblemDetail(workShiftId, dayOfWeek), null);
    }

    public RegistrationConflictException(String message) {
        super(HttpStatus.CONFLICT, asProblemDetailWithMessage(message), null);
    }

    private static ProblemDetail asProblemDetail(String workShiftId, String dayOfWeek) {
        String message = String.format(
                "Đăng ký ca này trùng với đăng ký đang hoạt động cho ca %s vào %s",
                workShiftId, dayOfWeek);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Xung Đột Đăng Ký");
        problemDetail.setProperty("errorCode", "REGISTRATION_CONFLICT");
        problemDetail.setProperty("message", "Đăng ký ca này trùng với đăng ký đang hoạt động.");
        return problemDetail;
    }

    private static ProblemDetail asProblemDetailWithMessage(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Xung Đột Đăng Ký");
        problemDetail.setProperty("errorCode", "REGISTRATION_CONFLICT");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
