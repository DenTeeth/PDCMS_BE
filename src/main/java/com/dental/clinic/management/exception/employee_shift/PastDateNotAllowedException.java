package com.dental.clinic.management.exception.employee_shift;

import java.time.LocalDate;

/**
 * Exception thrown when attempting to create a shift for a past date.
 */
public class PastDateNotAllowedException extends RuntimeException {

    public PastDateNotAllowedException(LocalDate workDate) {
        super(String.format(
                "KhÃƒÂ´ng thÃ¡Â»Æ’ tÃ¡ÂºÂ¡o ca lÃƒÂ m viÃ¡Â»â€¡c cho ngÃƒÂ y %s trong quÃƒÂ¡ khÃ¡Â»Â©. Vui lÃƒÂ²ng chÃ¡Â»Ân ngÃƒÂ y tÃ¡Â»Â« hÃƒÂ´m nay trÃ¡Â»Å¸ Ã„â€˜i.",
                workDate));
    }
}
