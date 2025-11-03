package com.dental.clinic.management.booking_appointment.repository;

import com.dental.clinic.management.booking_appointment.domain.AppointmentService;
import com.dental.clinic.management.booking_appointment.domain.AppointmentService.AppointmentServiceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AppointmentService Entity
 * Manages service assignments to appointments
 */
@Repository
public interface AppointmentServiceRepository extends JpaRepository<AppointmentService, AppointmentServiceId> {

    /**
     * Find all services for a specific appointment
     */
    List<AppointmentService> findByIdAppointmentId(Integer appointmentId);

    /**
     * Delete all services for an appointment
     */
    void deleteByIdAppointmentId(Integer appointmentId);
}
