package com.dental.clinic.management.exception.fixed_registration;

/**
 * Exception thrown when attempting to assign fixed schedule to PART_TIME_FLEX
 * employee.
 */
public class InvalidEmployeeTypeException extends RuntimeException {

    public InvalidEmployeeTypeException() {
        super("KhÃƒÂ´ng thÃ¡Â»Æ’ gÃƒÂ¡n lÃ¡Â»â€¹ch cÃ¡Â»â€˜ Ã„â€˜Ã¡Â»â€¹nh. NhÃƒÂ¢n viÃƒÂªn nÃƒÂ y thuÃ¡Â»â„¢c luÃ¡Â»â€œng Ã„ÂÃ„Æ’ng kÃƒÂ½ Linh hoÃ¡ÂºÂ¡t (Flex).");
    }
}
