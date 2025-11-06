package com.dental.clinic.management.exception.fixed_registration;

/**
 * Exception thrown when a fixed registration is not found.
 */
public class FixedRegistrationNotFoundException extends RuntimeException {

    public FixedRegistrationNotFoundException(Integer registrationId) {
        super(String.format("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y lÃ¡Â»â€¹ch cÃ¡Â»â€˜ Ã„â€˜Ã¡Â»â€¹nh vÃ¡Â»â€ºi ID: %d", registrationId));
    }
}
