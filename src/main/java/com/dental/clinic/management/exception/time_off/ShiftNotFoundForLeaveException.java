package com.dental.clinic.management.exception.time_off;

/**
 * Exception thrown when an employee tries to request time-off
 * but doesn't have a scheduled shift for that date/shift.
 * (V14 Hybrid Validation - P5.1)
 */
public class ShiftNotFoundForLeaveException extends RuntimeException {

    public ShiftNotFoundForLeaveException(String message) {
        super(message);
    }

    public ShiftNotFoundForLeaveException(Integer employeeId, String date, String workShiftId) {
        super(String.format(
                "NhÃƒÂ¢n viÃƒÂªn khÃƒÂ´ng cÃƒÂ³ lÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c vÃƒÂ o ngÃƒÂ y nÃƒÂ y. Vui lÃƒÂ²ng kiÃ¡Â»Æ’m tra lÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c trÃ†Â°Ã¡Â»â€ºc khi Ã„â€˜Ã„Æ’ng kÃƒÂ½ nghÃ¡Â»â€° phÃƒÂ©p.%s",
                workShiftId != null ? String.format(" (Ca lÃƒÂ m viÃ¡Â»â€¡c: %s, NgÃƒÂ y: %s)", workShiftId, date)
                        : String.format(" (NgÃƒÂ y: %s)", date)));
    }

    public ShiftNotFoundForLeaveException(Integer employeeId, String date, String workShiftId, String shiftName) {
        super(String.format(
                "NhÃƒÂ¢n viÃƒÂªn khÃƒÂ´ng cÃƒÂ³ lÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c vÃƒÂ o ngÃƒÂ y nÃƒÂ y. Vui lÃƒÂ²ng kiÃ¡Â»Æ’m tra lÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c trÃ†Â°Ã¡Â»â€ºc khi Ã„â€˜Ã„Æ’ng kÃƒÂ½ nghÃ¡Â»â€° phÃƒÂ©p. (NgÃƒÂ y: %s, Ca: %s)",
                date,
                shiftName != null ? shiftName : workShiftId));
    }
}
