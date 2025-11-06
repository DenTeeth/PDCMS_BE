package com.dental.clinic.management.booking_appointment.enums;

/**
 * Enum for Appointment Participant Roles
 * Matches PostgreSQL custom type: appointment_participant_role_enum
 *
 * Purpose: Define the role of an employee participating in an appointment
 * besides the primary doctor
 */
public enum AppointmentParticipantRole {

    /**
     * PhÃ¡Â»Â¥ tÃƒÂ¡ - Assistant helping during the procedure
     * Default role when creating appointment with participantCodes
     */
    ASSISTANT,

    /**
     * BÃƒÂ¡c sÃ„Â© phÃ¡Â»Â¥ - Secondary doctor assisting the primary doctor
     * Example: Complex surgeries requiring multiple doctors
     */
    SECONDARY_DOCTOR,

    /**
     * Quan sÃƒÂ¡t viÃƒÂªn - Observer (e.g., trainee, student)
     * Does not actively participate in the procedure
     */
    OBSERVER
}
