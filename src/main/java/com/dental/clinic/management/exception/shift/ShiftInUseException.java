package com.dental.clinic.management.exception.shift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when attempting to modify or delete a work shift that is currently in use
 * by employee schedules or part-time registrations.
 */
public class ShiftInUseException extends ErrorResponseException {

    public ShiftInUseException(String message) {
        super(HttpStatus.CONFLICT, asProblemDetail(message), null);
    }

    public ShiftInUseException(String workShiftId, String usageDetails) {
        this(String.format("KhÃƒÂ´ng thÃ¡Â»Æ’ thay Ã„â€˜Ã¡Â»â€¢i hoÃ¡ÂºÂ·c xÃƒÂ³a ca lÃƒÂ m viÃ¡Â»â€¡c '%s' vÃƒÂ¬ ca nÃƒÂ y Ã„â€˜ang Ã„â€˜Ã†Â°Ã¡Â»Â£c sÃ¡Â»Â­ dÃ¡Â»Â¥ng bÃ¡Â»Å¸i %s. " +
                          "Vui lÃƒÂ²ng xÃƒÂ³a hoÃ¡ÂºÂ·c thay Ã„â€˜Ã¡Â»â€¢i cÃƒÂ¡c lÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c/Ã„â€˜Ã„Æ’ng kÃƒÂ½ liÃƒÂªn quan trÃ†Â°Ã¡Â»â€ºc.",
                          workShiftId, usageDetails));
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Work Shift In Use");
        problemDetail.setProperty("errorCode", "SHIFT_IN_USE");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
