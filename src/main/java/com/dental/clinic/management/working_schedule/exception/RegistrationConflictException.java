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
        String message = "BÃ¡ÂºÂ¡n Ã„â€˜ÃƒÂ£ cÃƒÂ³ Ã„â€˜Ã„Æ’ng kÃƒÂ½ ca lÃƒÂ m viÃ¡Â»â€¡c active khÃƒÂ¡c trÃƒÂ¹ng giÃ¡Â»Â. Vui lÃƒÂ²ng hÃ¡Â»Â§y Ã„â€˜Ã„Æ’ng kÃƒÂ½ cÃ…Â© trÃ†Â°Ã¡Â»â€ºc.";

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Registration Conflict");
        problemDetail.setProperty("errorCode", ERROR_CODE);
        problemDetail.setProperty("message", message);
        problemDetail.setProperty("employeeId", employeeId);

        return problemDetail;
    }
}
