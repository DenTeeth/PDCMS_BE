package com.dental.clinic.management.working_schedule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import java.time.Instant;

/**
 * Exception thrown when trying to update a shift's time range to a different time-of-day
 * that conflicts with the shift ID prefix.
 * Example: WKS_MORNING_03 cannot be updated to afternoon hours (14:00-18:00).
 */
public class TimeOfDayMismatchException extends ErrorResponseException {

    private static final String ERROR_CODE = "TIME_OF_DAY_MISMATCH";

    public TimeOfDayMismatchException(String workShiftId, String expectedTimeOfDay, String actualTimeOfDay) {
        super(HttpStatus.CONFLICT, createProblemDetail(workShiftId, expectedTimeOfDay, actualTimeOfDay), null);
    }

    private static ProblemDetail createProblemDetail(String workShiftId, String expectedTimeOfDay, String actualTimeOfDay) {
        String message = String.format(
            "KhÃƒÂ´ng thÃ¡Â»Æ’ cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t ca lÃƒÂ m viÃ¡Â»â€¡c '%s' vÃƒÂ¬ thÃ¡Â»Âi gian mÃ¡Â»â€ºi (%s) khÃƒÂ´ng khÃ¡Â»â€ºp vÃ¡Â»â€ºi thÃ¡Â»Âi gian Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»â€¹nh nghÃ„Â©a trong mÃƒÂ£ ca (%s). " +
            "VÃƒÂ­ dÃ¡Â»Â¥: ca cÃƒÂ³ mÃƒÂ£ WKS_MORNING_* chÃ¡Â»â€° Ã„â€˜Ã†Â°Ã¡Â»Â£c cÃƒÂ³ giÃ¡Â»Â bÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u tÃ¡Â»Â« 08:00-11:59, " +
            "WKS_AFTERNOON_* tÃ¡Â»Â« 12:00-17:59, WKS_EVENING_* tÃ¡Â»Â« 18:00-20:59.",
            workShiftId, actualTimeOfDay, expectedTimeOfDay
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setType(URI.create("https://api.dentalclinic.com/errors/time-of-day-mismatch"));
        problemDetail.setTitle("Time of Day Mismatch");
        problemDetail.setProperty("errorCode", ERROR_CODE);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("workShiftId", workShiftId);
        problemDetail.setProperty("expectedTimeOfDay", expectedTimeOfDay);
        problemDetail.setProperty("actualTimeOfDay", actualTimeOfDay);

        return problemDetail;
    }
}
