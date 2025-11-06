package com.dental.clinic.management.exception.fixed_registration;

/**
 * Exception thrown when attempting to create a duplicate fixed shift
 * registration.
 * This occurs when an employee already has an active registration for the same
 * work shift.
 */
public class DuplicateFixedShiftRegistrationException extends RuntimeException {

    public DuplicateFixedShiftRegistrationException(String workShiftName) {
        super(String.format("NhÃƒÂ¢n viÃƒÂªn nÃƒÂ y Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c gÃƒÂ¡n %s. Vui lÃƒÂ²ng cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t bÃ¡ÂºÂ£n ghi cÃ…Â©.", workShiftName));
    }
}
