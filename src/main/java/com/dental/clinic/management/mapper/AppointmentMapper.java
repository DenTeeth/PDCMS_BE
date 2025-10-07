package com.dental.clinic.management.mapper;

import org.springframework.stereotype.Component;

import com.dental.clinic.management.domain.Appointment;
import com.dental.clinic.management.dto.request.CreateAppointmentRequest;
import com.dental.clinic.management.dto.request.UpdateAppointmentRequest;
import com.dental.clinic.management.dto.response.AppointmentResponse;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment a) {
        if (a == null) return null;
        AppointmentResponse r = new AppointmentResponse();
        r.setAppointmentId(a.getAppointmentId());
        r.setAppointmentCode(a.getAppointmentCode());
        r.setPatientId(a.getPatientId());
        r.setDoctorId(a.getDoctorId());
        r.setAppointmentDate(a.getAppointmentDate());
        r.setStartTime(a.getStartTime());
        r.setEndTime(a.getEndTime());
        r.setType(a.getType());
        r.setStatus(a.getStatus());
        r.setReason(a.getReason());
        r.setNotes(a.getNotes());
        r.setCreatedBy(a.getCreatedBy());
        r.setCreatedAt(a.getCreatedAt());
        r.setUpdatedAt(a.getUpdatedAt());
        return r;
    }

    public Appointment toEntity(CreateAppointmentRequest req) {
        if (req == null) return null;
        Appointment a = new Appointment();
        a.setPatientId(req.getPatientId());
        a.setDoctorId(req.getDoctorId());
        a.setAppointmentDate(req.getAppointmentDate());
        a.setStartTime(req.getStartTime());
        a.setEndTime(req.getEndTime());
        a.setType(req.getType());
        a.setStatus(req.getStatus());
        a.setReason(req.getReason());
        a.setNotes(req.getNotes());
        return a;
    }

    public void updateFromRequest(UpdateAppointmentRequest req, Appointment a) {
        if (req == null || a == null) return;
        if (req.getPatientId() != null) a.setPatientId(req.getPatientId());
        if (req.getDoctorId() != null) a.setDoctorId(req.getDoctorId());
        if (req.getAppointmentDate() != null) a.setAppointmentDate(req.getAppointmentDate());
        if (req.getStartTime() != null) a.setStartTime(req.getStartTime());
        if (req.getEndTime() != null) a.setEndTime(req.getEndTime());
        if (req.getType() != null) a.setType(req.getType());
        if (req.getStatus() != null) a.setStatus(req.getStatus());
        if (req.getReason() != null) a.setReason(req.getReason());
        if (req.getNotes() != null) a.setNotes(req.getNotes());
    }
}
