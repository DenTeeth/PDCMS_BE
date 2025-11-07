package com.dental.clinic.management.booking_appointment.dto.response;

import com.dental.clinic.management.booking_appointment.dto.AppointmentDetailDTO;

/**
 * Response DTO for reschedule appointment operation.
 * Contains both cancelled and new appointments.
 *
 * This helps frontend display clear message:
 * "Appointment APT-001 has been cancelled and rescheduled to APT-005"
 */
public class RescheduleAppointmentResponse {

    /**
     * Cancelled appointment (old appointment, now CANCELLED status).
     * Contains rescheduled_to_appointment_id linking to new appointment.
     * Includes cancellation reason details.
     */
    private AppointmentDetailDTO cancelledAppointment;

    /**
     * New appointment (SCHEDULED status).
     * This is the active appointment with new time/doctor/room.
     */
    private AppointmentDetailDTO newAppointment;

    public RescheduleAppointmentResponse() {
    }

    public RescheduleAppointmentResponse(AppointmentDetailDTO cancelledAppointment,
            AppointmentDetailDTO newAppointment) {
        this.cancelledAppointment = cancelledAppointment;
        this.newAppointment = newAppointment;
    }

    public AppointmentDetailDTO getCancelledAppointment() {
        return cancelledAppointment;
    }

    public void setCancelledAppointment(AppointmentDetailDTO cancelledAppointment) {
        this.cancelledAppointment = cancelledAppointment;
    }

    public AppointmentDetailDTO getNewAppointment() {
        return newAppointment;
    }

    public void setNewAppointment(AppointmentDetailDTO newAppointment) {
        this.newAppointment = newAppointment;
    }
}
