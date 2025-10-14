package com.dental.clinic.management.dto.request;

import com.dental.clinic.management.domain.enums.AppointmentType;
import com.dental.clinic.management.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public class UpdateAppointmentRequest {

    @Size(max = 36)
    private String patientId;

    @Size(max = 36)
    private String doctorId;

    private LocalDate appointmentDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private AppointmentType type;

    private AppointmentStatus status;

    private String reason;

    private String notes;

    // Getters and setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public AppointmentType getType() { return type; }
    public void setType(AppointmentType type) { this.type = type; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
