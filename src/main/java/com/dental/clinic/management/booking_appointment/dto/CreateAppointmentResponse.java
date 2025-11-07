package com.dental.clinic.management.booking_appointment.dto;

import com.dental.clinic.management.booking_appointment.enums.AppointmentParticipantRole;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for successful appointment creation (P3.2)
 * Returns summary of created appointment with nested resource details
 */
public class CreateAppointmentResponse {

    /**
     * Generated appointment code
     * Example: "APT-20251030-001"
     */
    private String appointmentCode;

    /**
     * Appointment status (always SCHEDULED for new appointments)
     */
    private String status;

    /**
     * Requested start time
     */
    private LocalDateTime appointmentStartTime;

    /**
     * Calculated end time (start + total service duration)
     */
    private LocalDateTime appointmentEndTime;

    /**
     * Total duration in minutes (sum of service durations + buffers)
     */
    private Integer expectedDurationMinutes;

    /**
     * Patient summary
     */
    private PatientSummary patient;

    /**
     * Primary doctor summary
     */
    private DoctorSummary doctor;

    /**
     * Room summary
     */
    private RoomSummary room;

    /**
     * List of services to be performed
     */
    private List<ServiceSummary> services;

    /**
     * List of participants (assistants, secondary doctors)
     */
    private List<ParticipantSummary> participants;

    public CreateAppointmentResponse() {
    }

    public CreateAppointmentResponse(String appointmentCode, String status,
            LocalDateTime appointmentStartTime,
            LocalDateTime appointmentEndTime,
            Integer expectedDurationMinutes, PatientSummary patient,
            DoctorSummary doctor, RoomSummary room,
            List<ServiceSummary> services,
            List<ParticipantSummary> participants) {
        this.appointmentCode = appointmentCode;
        this.status = status;
        this.appointmentStartTime = appointmentStartTime;
        this.appointmentEndTime = appointmentEndTime;
        this.expectedDurationMinutes = expectedDurationMinutes;
        this.patient = patient;
        this.doctor = doctor;
        this.room = room;
        this.services = services;
        this.participants = participants;
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

    public PatientSummary getPatient() {
        return patient;
    }

    public void setPatient(PatientSummary patient) {
        this.patient = patient;
    }

    public DoctorSummary getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorSummary doctor) {
        this.doctor = doctor;
    }

    public RoomSummary getRoom() {
        return room;
    }

    public void setRoom(RoomSummary room) {
        this.room = room;
    }

    public List<ServiceSummary> getServices() {
        return services;
    }

    public void setServices(List<ServiceSummary> services) {
        this.services = services;
    }

    public List<ParticipantSummary> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantSummary> participants) {
        this.participants = participants;
    }

    /**
     * Nested DTO: Patient Summary
     */
    public static class PatientSummary {
        private String patientCode;
        private String fullName;
        private String phone; // For detail view
        private java.time.LocalDate dateOfBirth; // For detail view

        public PatientSummary() {
        }

        public PatientSummary(String patientCode, String fullName, String phone,
                java.time.LocalDate dateOfBirth) {
            this.patientCode = patientCode;
            this.fullName = fullName;
            this.phone = phone;
            this.dateOfBirth = dateOfBirth;
        }

        public String getPatientCode() {
            return patientCode;
        }

        public void setPatientCode(String patientCode) {
            this.patientCode = patientCode;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public java.time.LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(java.time.LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }
    }

    /**
     * Nested DTO: Doctor Summary
     */
    public static class DoctorSummary {
        private String employeeCode;
        private String fullName;

        public DoctorSummary() {
        }

        public DoctorSummary(String employeeCode, String fullName) {
            this.employeeCode = employeeCode;
            this.fullName = fullName;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }

        public void setEmployeeCode(String employeeCode) {
            this.employeeCode = employeeCode;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    /**
     * Nested DTO: Room Summary
     */
    public static class RoomSummary {
        private String roomCode;
        private String roomName;

        public RoomSummary() {
        }

        public RoomSummary(String roomCode, String roomName) {
            this.roomCode = roomCode;
            this.roomName = roomName;
        }

        public String getRoomCode() {
            return roomCode;
        }

        public void setRoomCode(String roomCode) {
            this.roomCode = roomCode;
        }

        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }
    }

    /**
     * Nested DTO: Service Summary
     */
    public static class ServiceSummary {
        private String serviceCode;
        private String serviceName;

        public ServiceSummary() {
        }

        public ServiceSummary(String serviceCode, String serviceName) {
            this.serviceCode = serviceCode;
            this.serviceName = serviceName;
        }

        public String getServiceCode() {
            return serviceCode;
        }

        public void setServiceCode(String serviceCode) {
            this.serviceCode = serviceCode;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
    }

    /**
     * Nested DTO: Participant Summary
     */
    public static class ParticipantSummary {
        private String employeeCode;
        private String fullName;
        private AppointmentParticipantRole role; // Default: ASSISTANT

        public ParticipantSummary() {
        }

        public ParticipantSummary(String employeeCode, String fullName,
                AppointmentParticipantRole role) {
            this.employeeCode = employeeCode;
            this.fullName = fullName;
            this.role = role;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }

        public void setEmployeeCode(String employeeCode) {
            this.employeeCode = employeeCode;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public AppointmentParticipantRole getRole() {
            return role;
        }

        public void setRole(AppointmentParticipantRole role) {
            this.role = role;
        }
    }
}
