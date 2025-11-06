package com.dental.clinic.management.booking_appointment.enums;

/**
 * Appointment Reason Code for Delays, Cancellations, Reschedules
 * Matches SQL type: appointment_reason_code
 *
 * Used for business analytics and operational insights
 */
public enum AppointmentReasonCode {
    /**
     * Ca trÃ†Â°Ã¡Â»â€ºc bÃ¡Â»â€¹ kÃƒÂ©o dÃƒÂ i (overrun)
     */
    PREVIOUS_CASE_OVERRUN,

    /**
     * BÃƒÂ¡c sÃ„Â© Ã„â€˜Ã¡Â»â„¢t ngÃ¡Â»â„¢t khÃƒÂ´ng cÃƒÂ³ mÃ¡ÂºÂ·t
     */
    DOCTOR_UNAVAILABLE,

    /**
     * ThiÃ¡ÂºÂ¿t bÃ¡Â»â€¹ hÃ¡Â»Âng hoÃ¡ÂºÂ·c Ã„â€˜ang bÃ¡ÂºÂ£o trÃƒÂ¬
     */
    EQUIPMENT_FAILURE,

    /**
     * BÃ¡Â»â€¡nh nhÃƒÂ¢n yÃƒÂªu cÃ¡ÂºÂ§u thay Ã„â€˜Ã¡Â»â€¢i
     */
    PATIENT_REQUEST,

    /**
     * Ã„ÂiÃ¡Â»Âu phÃ¡Â»â€˜i vÃ¡ÂºÂ­n hÃƒÂ nh (phÃƒÂ²ng khÃƒÂ¡m chÃ¡Â»Â§ Ã„â€˜Ã¡Â»â„¢ng)
     */
    OPERATIONAL_REDIRECT,

    /**
     * LÃƒÂ½ do khÃƒÂ¡c
     */
    OTHER
}
