package com.dental.clinic.management.working_schedule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class SlotIsFullException extends ErrorResponseException {

    private static final String ERROR_CODE = "SLOT_IS_FULL";

    public SlotIsFullException(Long slotId, String shiftName, String dayOfWeek) {
        super(HttpStatus.CONFLICT, createProblemDetail(slotId, shiftName, dayOfWeek), null);
    }

    private static ProblemDetail createProblemDetail(Long slotId, String shiftName, String dayOfWeek) {
        String message = String.format(
            "SuÃ¡ÂºÂ¥t [%s - %s] Ã„â€˜ÃƒÂ£ Ã„â€˜Ã¡Â»Â§ ngÃ†Â°Ã¡Â»Âi Ã„â€˜Ã„Æ’ng kÃƒÂ½. Vui lÃƒÂ²ng chÃ¡Â»Ân suÃ¡ÂºÂ¥t khÃƒÂ¡c.",
            shiftName, dayOfWeek
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Slot Is Full");
        problemDetail.setProperty("errorCode", ERROR_CODE);
        problemDetail.setProperty("message", message);
        problemDetail.setProperty("slotId", slotId);

        return problemDetail;
    }
}
