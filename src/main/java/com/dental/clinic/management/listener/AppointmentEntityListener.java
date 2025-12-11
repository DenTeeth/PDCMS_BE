package com.dental.clinic.management.listener;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.service.AppointmentRedisService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppointmentEntityListener {

    private static AppointmentRedisService appointmentRedisService;

    @Autowired
    public void setAppointmentRedisService(AppointmentRedisService service) {
        AppointmentEntityListener.appointmentRedisService = service;
    }

    @PostPersist
    public void onPostPersist(Appointment appointment) {
        if (appointmentRedisService != null && appointment.getAppointmentId() != null) {
            appointmentRedisService.evictAppointment(appointment.getAppointmentId().longValue());
        }
    }

    @PostUpdate
    public void onPostUpdate(Appointment appointment) {
        if (appointmentRedisService != null && appointment.getAppointmentId() != null) {
            appointmentRedisService.evictAppointment(appointment.getAppointmentId().longValue());
        }
    }

    @PostRemove
    public void onPostRemove(Appointment appointment) {
        if (appointmentRedisService != null && appointment.getAppointmentId() != null) {
            appointmentRedisService.evictAppointment(appointment.getAppointmentId().longValue());
        }
    }
}
