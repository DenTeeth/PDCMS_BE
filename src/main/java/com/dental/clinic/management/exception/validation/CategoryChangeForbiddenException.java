package com.dental.clinic.management.exception.validation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when attempting to change a work shift's category (NORMAL Ã¢â€ â€ NIGHT)
 * which would conflict with the semantic meaning of the shift ID.
 */
public class CategoryChangeForbiddenException extends ErrorResponseException {

    public CategoryChangeForbiddenException(String message) {
        super(HttpStatus.CONFLICT, asProblemDetail(message), null);
    }

    public CategoryChangeForbiddenException(String workShiftId, String fromCategory, String toCategory) {
        this(String.format("KhÃƒÂ´ng thÃ¡Â»Æ’ thay Ã„â€˜Ã¡Â»â€¢i ca tÃ¡Â»Â« %s sang %s vÃƒÂ¬ sÃ¡ÂºÂ½ khÃƒÂ´ng khÃ¡Â»â€ºp vÃ¡Â»â€ºi mÃƒÂ£ ca lÃƒÂ m viÃ¡Â»â€¡c '%s'. " +
                          "Vui lÃƒÂ²ng tÃ¡ÂºÂ¡o ca lÃƒÂ m viÃ¡Â»â€¡c mÃ¡Â»â€ºi thay vÃƒÂ¬ cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t ca hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i.",
                          fromCategory, toCategory, workShiftId));
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Category Change Forbidden");
        problemDetail.setProperty("errorCode", "CATEGORY_CHANGE_FORBIDDEN");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
