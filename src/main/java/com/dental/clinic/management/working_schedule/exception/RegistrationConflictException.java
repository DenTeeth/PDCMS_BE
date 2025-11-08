package com.dental.clinic.management.working_schedule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class RegistrationConflictException extends ErrorResponseException {

    private static final String ERROR_CODE = "REGISTRATION_CONFLICT";

    public RegistrationConflictException(Integer employeeId) {
        super(HttpStatus.CONFLICT, createProblemDetail(employeeId), null);
    }

    private static ProblemDetail createProblemDetail(Integer employeeId) {
        String message = "Bạn đã có đăng ký ca làm việc active khác trùng giờ. Vui lòng hủy đăng ký ca trước đó.";

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Registration Conflict");
        problemDetail.setProperty("errorCode", ERROR_CODE);
        problemDetail.setProperty("message", message);
        problemDetail.setProperty("employeeId", employeeId);

        return problemDetail;
    }
}
