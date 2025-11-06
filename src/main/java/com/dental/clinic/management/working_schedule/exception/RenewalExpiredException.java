package com.dental.clinic.management.working_schedule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Exception thrown when renewal request has expired (past deadline).
 */
public class RenewalExpiredException extends ErrorResponseException {

    private static final String ERROR_CODE = "REQUEST_EXPIRED";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public RenewalExpiredException(String renewalId, LocalDateTime expiresAt) {
        super(HttpStatus.CONFLICT, createProblemDetail(renewalId, expiresAt), null);
    }

    private static ProblemDetail createProblemDetail(String renewalId, LocalDateTime expiresAt) {
        String expiredDate = expiresAt != null ? expiresAt.format(FORMATTER) : "khÃƒÂ´ng xÃƒÂ¡c Ã„â€˜Ã¡Â»â€¹nh";
        String message = String.format(
                "YÃƒÂªu cÃ¡ÂºÂ§u gia hÃ¡ÂºÂ¡n %s Ã„â€˜ÃƒÂ£ hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n phÃ¡ÂºÂ£n hÃ¡Â»â€œi (deadline: %s). Vui lÃƒÂ²ng liÃƒÂªn hÃ¡Â»â€¡ quÃ¡ÂºÂ£n lÃƒÂ½.",
                renewalId,
                expiredDate);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Renewal Request Expired");
        problemDetail.setProperty("errorCode", ERROR_CODE);
        problemDetail.setProperty("message", message);
        problemDetail.setProperty("renewalId", renewalId);
        problemDetail.setProperty("expiresAt", expiresAt != null ? expiresAt.toString() : null);

        return problemDetail;
    }
}
