package com.dental.clinic.management.booking_appointment.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Appointment Summary DTO for Dashboard/List View
 * Used in GET /api/v1/appointments response
 *
 * Reuses nested summary classes from CreateAppointmentResponse
 */
public class AppointmentSummaryDTO {

    /**
     * Appointment code
     * Example: "APT-20251030-001"
     */
    private String appointmentCode;

    /**
     * Appointment status
     * Example: SCHEDULED, CHECKED_IN, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
     */
    private String status;

    /**
     * Computed status based on current time and appointment status
     * Values: UPCOMING, LATE, IN_PROGRESS, CHECKED_IN, COMPLETED, CANCELLED
     *
     * Logic:
     * - CANCELLED: status == CANCELLED
     * - COMPLETED: status == COMPLETED
     * - CHECKED_IN: status == CHECKED_IN
     * - IN_PROGRESS: status == IN_PROGRESS
     * - LATE: status == SCHEDULED && currentTime > appointmentStartTime
     * - UPCOMING: status == SCHEDULED && currentTime <= appointmentStartTime
     */
    private String computedStatus;

    /**
     * Minutes late (only applicable if LATE)
     * If appointment is SCHEDULED but current time > start time, calculate delay
     * Otherwise: 0 or null
     */
    private Long minutesLate;

    /**
     * Appointment start time
     */
    private LocalDateTime appointmentStartTime;

    /**
     * Appointment end time (calculated)
     */
    private LocalDateTime appointmentEndTime;

    /**
     * Total expected duration in minutes
     */
    private Integer expectedDurationMinutes;

    /**
     * Patient information
     */
    private CreateAppointmentResponse.PatientSummary patient;

    /**
     * Primary doctor information
     */
    private CreateAppointmentResponse.DoctorSummary doctor;

    /**
     * Room information
     */
    private CreateAppointmentResponse.RoomSummary room;

    /**
     * List of services in this appointment
     */
    private List<CreateAppointmentResponse.ServiceSummary> services;

    /**
     * List of participants (assistants, secondary doctors, observers)
     */
    private List<CreateAppointmentResponse.ParticipantSummary> participants;

    /**
     * Optional: Notes/remarks for this appointment
     */
    private String notes;

    public AppointmentSummaryDTO() {
    }

    public AppointmentSummaryDTO(String appointmentCode, String status, String computedStatus,
            Long minutesLate, LocalDateTime appointmentStartTime,
            LocalDateTime appointmentEndTime, Integer expectedDurationMinutes,
            CreateAppointmentResponse.PatientSummary patient,
            CreateAppointmentResponse.DoctorSummary doctor,
            CreateAppointmentResponse.RoomSummary room,
            List<CreateAppointmentResponse.ServiceSummary> services,
            List<CreateAppointmentResponse.ParticipantSummary> participants,
            String notes) {
        this.appointmentCode = appointmentCode;
        this.status = status;
        this.computedStatus = computedStatus;
        this.minutesLate = minutesLate;
        this.appointmentStartTime = appointmentStartTime;
        this.appointmentEndTime = appointmentEndTime;
        this.expectedDurationMinutes = expectedDurationMinutes;
        this.patient = patient;
        this.doctor = doctor;
        this.room = room;
        this.services = services;
        this.participants = participants;
        this.notes = notes;
    }

    public String getAppointmentCode() {
        return appointmentCode;
    }

    public void setAppointmentCode(String appointmentCode) {
        this.appointmentCode = appointmentCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComputedStatus() {
        return computedStatus;
    }

    public void setComputedStatus(String computedStatus) {
        this.computedStatus = computedStatus;
    }

    public Long getMinutesLate() {
        return minutesLate;
    }

    public void setMinutesLate(Long minutesLate) {
        this.minutesLate = minutesLate;
    }

    public LocalDateTime getAppointmentStartTime() {
        return appointmentStartTime;
    }

    public void setAppointmentStartTime(LocalDateTime appointmentStartTime) {
        this.appointmentStartTime = appointmentStartTime;
    }

    public LocalDateTime getAppointmentEndTime() {
        return appointmentEndTime;
    }

    public void setAppointmentEndTime(LocalDateTime appointmentEndTime) {
        this.appointmentEndTime = appointmentEndTime;
    }

    public Integer getExpectedDurationMinutes() {
        return expectedDurationMinutes;
    }

    public void setExpectedDurationMinutes(Integer expectedDurationMinutes) {
        this.expectedDurationMinutes = expectedDurationMinutes;
    }

    public CreateAppointmentResponse.PatientSummary getPatient() {
        return patient;
    }

    public void setPatient(CreateAppointmentResponse.PatientSummary patient) {
        this.patient = patient;
    }

    public CreateAppointmentResponse.DoctorSummary getDoctor() {
        return doctor;
    }

    public void setDoctor(CreateAppointmentResponse.DoctorSummary doctor) {
        this.doctor = doctor;
    }

    public CreateAppointmentResponse.RoomSummary getRoom() {
        return room;
    }

    public void setRoom(CreateAppointmentResponse.RoomSummary room) {
        this.room = room;
    }

    public List<CreateAppointmentResponse.ServiceSummary> getServices() {
        return services;
    }

    public void setServices(List<CreateAppointmentResponse.ServiceSummary> services) {
        this.services = services;
    }

    public List<CreateAppointmentResponse.ParticipantSummary> getParticipants() {
        return participants;
    }

    public void setParticipants(List<CreateAppointmentResponse.ParticipantSummary> participants) {
        this.participants = participants;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
