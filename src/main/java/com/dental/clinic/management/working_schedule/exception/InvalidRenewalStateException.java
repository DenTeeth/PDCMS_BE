package com.dental.clinic.management.working_schedule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when renewal request is not in PENDING_ACTION state.
 */
public class InvalidRenewalStateException extends ErrorResponseException {

    private static final String ERROR_CODE = "INVALID_STATE";

    public InvalidRenewalStateException(String renewalId, String currentStatus) {
        super(HttpStatus.CONFLICT, createProblemDetail(renewalId, currentStatus), null);
    }

    private static ProblemDetail createProblemDetail(String renewalId, String currentStatus) {
        String message = String.format(
                "KhÃƒÂ´ng thÃ¡Â»Æ’ phÃ¡ÂºÂ£n hÃ¡Â»â€œi yÃƒÂªu cÃ¡ÂºÂ§u gia hÃ¡ÂºÂ¡n. YÃƒÂªu cÃ¡ÂºÂ§u %s Ã„â€˜ang Ã¡Â»Å¸ trÃ¡ÂºÂ¡ng thÃƒÂ¡i %s (chÃ¡Â»â€° cho phÃƒÂ©p PENDING_ACTION).",
                renewalId,
                currentStatus);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Invalid Renewal State");
        problemDetail.setProperty("errorCode", ERROR_CODE);
        problemDetail.setProperty("message", message);
        problemDetail.setProperty("renewalId", renewalId);
        problemDetail.setProperty("currentStatus", currentStatus);

        return problemDetail;
    }
}
