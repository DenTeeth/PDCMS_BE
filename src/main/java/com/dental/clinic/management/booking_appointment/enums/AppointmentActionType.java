package com.dental.clinic.management.booking_appointment.enums;

/**
 * Appointment Action Type for Audit Logging
 * Matches SQL type: appointment_action_type
 *
 * Tracks all important actions performed on appointments
 */
public enum AppointmentActionType {
    /**
     * Appointment Ã„â€˜Ã†Â°Ã¡Â»Â£c tÃ¡ÂºÂ¡o mÃ¡Â»â€ºi
     */
    CREATE,

    /**
     * Appointment bÃ¡Â»â€¹ delay (dÃ¡Â»Âi giÃ¡Â»Â trong cÃƒÂ¹ng ngÃƒÂ y)
     */
    DELAY,

    /**
     * Appointment nguÃ¡Â»â€œn bÃ¡Â»â€¹ reschedule (Ã„â€˜Ã¡Â»â€¢i sang ngÃƒÂ y khÃƒÂ¡c)
     */
    RESCHEDULE_SOURCE,

    /**
     * Appointment mÃ¡Â»â€ºi Ã„â€˜Ã†Â°Ã¡Â»Â£c tÃ¡ÂºÂ¡o tÃ¡Â»Â« reschedule
     */
    RESCHEDULE_TARGET,

    /**
     * Appointment bÃ¡Â»â€¹ hÃ¡Â»Â§y
     */
    CANCEL,

    /**
     * Thay Ã„â€˜Ã¡Â»â€¢i status (CHECKED_IN, IN_PROGRESS, COMPLETED, NO_SHOW)
     */
    STATUS_CHANGE
}
