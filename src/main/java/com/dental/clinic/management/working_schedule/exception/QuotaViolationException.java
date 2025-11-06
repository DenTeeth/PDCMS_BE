package com.dental.clinic.management.working_schedule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class QuotaViolationException extends ErrorResponseException {

    private static final String ERROR_CODE = "QUOTA_VIOLATION";

    public QuotaViolationException(Long slotId, int newQuota, long currentRegistered) {
        super(HttpStatus.CONFLICT, createProblemDetail(slotId, newQuota, currentRegistered), null);
    }

    private static ProblemDetail createProblemDetail(Long slotId, int newQuota, long currentRegistered) {
        String message = String.format(
            "KhÃƒÂ´ng thÃ¡Â»Æ’ giÃ¡ÂºÂ£m quota xuÃ¡Â»â€˜ng %d. Ã„ÂÃƒÂ£ cÃƒÂ³ %d nhÃƒÂ¢n viÃƒÂªn Ã„â€˜Ã„Æ’ng kÃƒÂ½ suÃ¡ÂºÂ¥t nÃƒÂ y.",
            newQuota, currentRegistered
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Quota Violation");
        problemDetail.setProperty("errorCode", ERROR_CODE);
        problemDetail.setProperty("message", message);
        problemDetail.setProperty("slotId", slotId);
        problemDetail.setProperty("newQuota", newQuota);
        problemDetail.setProperty("currentRegistered", currentRegistered);

        return problemDetail;
    }
}
