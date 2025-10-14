package com.dental.clinic.management.dto.response;

import java.time.LocalTime;

public class AvailableSlotResponse {
    private String doctorId;
    private java.time.LocalDate date;
    private java.time.LocalTime startTime;

    public AvailableSlotResponse() {}

    public AvailableSlotResponse(String doctorId, java.time.LocalDate date, LocalTime startTime) {
        this.doctorId = doctorId;
        this.date = date;
        this.startTime = startTime;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public java.time.LocalDate getDate() {
        return date;
    }

    public void setDate(java.time.LocalDate date) {
        this.date = date;
    }

    public java.time.LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(java.time.LocalTime startTime) {
        this.startTime = startTime;
    }
}
