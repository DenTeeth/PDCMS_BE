package com.dental.clinic.management.exception.overtime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.time.LocalDate;

/**
 * Exception thrown when attempting to create a duplicate overtime request.
 * A duplicate occurs when there's already a PENDING or APPROVED request
 * for the same employee, work date, and shift.
 * Returns 409 CONFLICT status.
 */
public class DuplicateOvertimeRequestException extends ErrorResponseException {

    public DuplicateOvertimeRequestException(Integer employeeId, LocalDate workDate, String shiftId) {
        super(HttpStatus.CONFLICT, asProblemDetail(employeeId, workDate, shiftId), null);
    }

    public DuplicateOvertimeRequestException(String message) {
        super(HttpStatus.CONFLICT, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(Integer employeeId, LocalDate workDate, String shiftId) {
        String message = String.format(
            "Ã„ÂÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i mÃ¡Â»â„¢t yÃƒÂªu cÃ¡ÂºÂ§u OT cho nhÃƒÂ¢n viÃƒÂªn %d vÃƒÂ o ngÃƒÂ y %s ca %s.",
            employeeId, workDate, shiftId
        );
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Duplicate Overtime Request");
        problemDetail.setProperty("code", "DUPLICATE_OT_REQUEST");
        problemDetail.setProperty("message", "Ã„ÂÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i mÃ¡Â»â„¢t yÃƒÂªu cÃ¡ÂºÂ§u OT cho nhÃƒÂ¢n viÃƒÂªn vÃƒÂ o ngÃƒÂ y vÃƒÂ  ca nÃƒÂ y.");
        return problemDetail;
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Duplicate Overtime Request");
        problemDetail.setProperty("code", "DUPLICATE_OT_REQUEST");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
