package com.dental.clinic.management.booking_appointment.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Appointment Detail DTO for Single Appointment View
 * Used in GET /api/v1/appointments/{appointmentCode} response
 *
 * Contains all fields from AppointmentSummaryDTO PLUS additional detail fields:
 * - appointmentId (internal PK)
 * - actualStartTime, actualEndTime
 * - cancellationReason (from audit log if status = CANCELLED)
 * - createdBy, createdAt
 */
public class AppointmentDetailDTO {

    /**
     * Internal appointment ID (Primary Key)
     * Example: 123
     */
    private Integer appointmentId;

    /**
     * Appointment code
     * Example: "APT-20251104-001"
     */
    private String appointmentCode;

    /**
     * Appointment status
     * Example: SCHEDULED, CHECKED_IN, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
     */
    private String status;

    /**
     * Computed status based on current time and appointment status
     * Values: UPCOMING, LATE, IN_PROGRESS, CHECKED_IN, COMPLETED, CANCELLED,
     * NO_SHOW
     */
    private String computedStatus;

    /**
     * Minutes late (only applicable if LATE)
     */
    private Long minutesLate;

    /**
     * Appointment start time (scheduled)
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
     * Actual start time (when patient checked in or appointment started)
     * Only set when status = IN_PROGRESS or COMPLETED
     */
    private LocalDateTime actualStartTime;

    /**
     * Actual end time (when appointment completed)
     * Only set when status = COMPLETED
     */
    private LocalDateTime actualEndTime;

    /**
     * Cancellation reason (from appointment_audit_logs)
     * Only populated when status = CANCELLED
     * Example: "Bệnh nhân hủy do Đặt xuất"
     */
    private String cancellationReason;

    /**
     * Optional notes/remarks for this appointment
     */
    private String notes;

    /**
     * Patient information (with phone and DOB)
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
     * Full name of the user who created this appointment
     * Example: "Lê Văn An"
     */
    private String createdBy;

    /**
     * Timestamp when this appointment was created
     */
    private LocalDateTime createdAt;

    public AppointmentDetailDTO() {
    }

    public AppointmentDetailDTO(Integer appointmentId, String appointmentCode, String status,
            String computedStatus, Long minutesLate, LocalDateTime appointmentStartTime,
            LocalDateTime appointmentEndTime, Integer expectedDurationMinutes,
            LocalDateTime actualStartTime, LocalDateTime actualEndTime,
            String cancellationReason, String notes,
            CreateAppointmentResponse.PatientSummary patient,
            CreateAppointmentResponse.DoctorSummary doctor,
            CreateAppointmentResponse.RoomSummary room,
            List<CreateAppointmentResponse.ServiceSummary> services,
            List<CreateAppointmentResponse.ParticipantSummary> participants,
            String createdBy, LocalDateTime createdAt) {
        this.appointmentId = appointmentId;
        this.appointmentCode = appointmentCode;
        this.status = status;
        this.computedStatus = computedStatus;
        this.minutesLate = minutesLate;
        this.appointmentStartTime = appointmentStartTime;
        this.appointmentEndTime = appointmentEndTime;
        this.expectedDurationMinutes = expectedDurationMinutes;
        this.actualStartTime = actualStartTime;
        this.actualEndTime = actualEndTime;
        this.cancellationReason = cancellationReason;
        this.notes = notes;
        this.patient = patient;
        this.doctor = doctor;
        this.room = room;
        this.services = services;
        this.participants = participants;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
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

    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
