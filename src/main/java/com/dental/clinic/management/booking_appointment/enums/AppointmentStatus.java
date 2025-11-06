package com.dental.clinic.management.booking_appointment.enums;

/**
 * Appointment Status Enum
 * Matches SQL type: appointment_status_enum
 *
 * Workflow: SCHEDULED -> CHECKED_IN -> IN_PROGRESS -> COMPLETED
 * Cancel paths: Any status -> CANCELLED
 * No-show: SCHEDULED -> NO_SHOW (if patient doesn't arrive)
 */
public enum AppointmentStatus {
    /**
     * Initial status - Appointment Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡ÂºÂ·t lÃ¡Â»â€¹ch
     */
    SCHEDULED,

    /**
     * BÃ¡Â»â€¡nh nhÃƒÂ¢n Ã„â€˜ÃƒÂ£ check-in tÃ¡ÂºÂ¡i lÃ¡Â»â€¦ tÃƒÂ¢n
     */
    CHECKED_IN,

    /**
     * BÃƒÂ¡c sÃ„Â© Ã„â€˜ÃƒÂ£ bÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u Ã„â€˜iÃ¡Â»Âu trÃ¡Â»â€¹
     */
    IN_PROGRESS,

    /**
     * HoÃƒÂ n thÃƒÂ nh Ã„â€˜iÃ¡Â»Âu trÃ¡Â»â€¹
     */
    COMPLETED,

    /**
     * Appointment bÃ¡Â»â€¹ hÃ¡Â»Â§y (bÃ¡Â»Å¸i bÃ¡Â»â€¡nh nhÃƒÂ¢n hoÃ¡ÂºÂ·c phÃƒÂ²ng khÃƒÂ¡m)
     */
    CANCELLED,

    /**
     * BÃ¡Â»â€¡nh nhÃƒÂ¢n khÃƒÂ´ng Ã„â€˜Ã¡ÂºÂ¿n (no-show)
     */
    NO_SHOW
}
