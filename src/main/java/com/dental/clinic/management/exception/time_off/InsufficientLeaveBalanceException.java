package com.dental.clinic.management.exception.time_off;

public class InsufficientLeaveBalanceException extends RuntimeException {
    public InsufficientLeaveBalanceException(double remaining, double requested) {
        super("SÃ¡Â»â€˜ dÃ†Â° nghÃ¡Â»â€° phÃƒÂ©p khÃƒÂ´ng Ã„â€˜Ã¡Â»Â§. CÃƒÂ²n lÃ¡ÂºÂ¡i: " + remaining + " ngÃƒÂ y, yÃƒÂªu cÃ¡ÂºÂ§u: " + requested + " ngÃƒÂ y");
    }
}
