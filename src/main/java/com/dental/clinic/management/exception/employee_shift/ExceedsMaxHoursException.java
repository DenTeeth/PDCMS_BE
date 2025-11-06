package com.dental.clinic.management.exception.employee_shift;

import java.time.LocalDate;

/**
 * Exception thrown when total working hours would exceed the 8-hour daily limit.
 */
public class ExceedsMaxHoursException extends RuntimeException {

    public ExceedsMaxHoursException(LocalDate workDate, int totalHours) {
        super(String.format(
                "KhÃƒÂ´ng thÃ¡Â»Æ’ tÃ¡ÂºÂ¡o ca lÃƒÂ m viÃ¡Â»â€¡c. TÃ¡Â»â€¢ng giÃ¡Â»Â lÃƒÂ m viÃ¡Â»â€¡c trong ngÃƒÂ y %s sÃ¡ÂºÂ½ lÃƒÂ  %d giÃ¡Â»Â, " +
                "vÃ†Â°Ã¡Â»Â£t quÃƒÂ¡ giÃ¡Â»â€ºi hÃ¡ÂºÂ¡n tÃ¡Â»â€˜i Ã„â€˜a 8 giÃ¡Â»Â/ngÃƒÂ y. Vui lÃƒÂ²ng chÃ¡Â»Ân ca lÃƒÂ m viÃ¡Â»â€¡c khÃƒÂ¡c hoÃ¡ÂºÂ·c Ã„â€˜iÃ¡Â»Âu chÃ¡Â»â€°nh thÃ¡Â»Âi gian.",
                workDate, totalHours));
    }
}
