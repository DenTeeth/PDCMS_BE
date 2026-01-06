package com.dental.clinic.management.booking_appointment.repository;

import com.dental.clinic.management.booking_appointment.domain.AppointmentService;
import com.dental.clinic.management.booking_appointment.domain.AppointmentService.AppointmentServiceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AppointmentService Entity
 * Manages service assignments to appointments
 */
@Repository
public interface AppointmentServiceRepository extends JpaRepository<AppointmentService, AppointmentServiceId> {

    /**
     * Find all services for a specific appointment with eagerly loaded service details
     * Using JOIN FETCH to avoid LazyInitializationException
     */
    @Query("SELECT aps FROM AppointmentService aps JOIN FETCH aps.service WHERE aps.id.appointmentId = :appointmentId")
    List<AppointmentService> findByIdAppointmentId(@Param("appointmentId") Integer appointmentId);

    /**
     * Delete all services for an appointment
     */
    void deleteByIdAppointmentId(Integer appointmentId);

    /**
     * Get top services by revenue for dashboard statistics
     * Joins appointments with COMPLETED status and PAID/PARTIAL_PAID invoices
     */
    @Query(value = "SELECT " +
           "sm.service_id, " +
           "sm.service_name, " +
           "COUNT(DISTINCT aps.appointment_id) as usage_count, " +
           "SUM(aps.price * aps.quantity) as total_revenue " +
           "FROM appointment_services aps " +
           "JOIN appointments a ON aps.appointment_id = a.appointment_id " +
           "JOIN service_masters sm ON aps.service_id = sm.service_id " +
           "JOIN invoices i ON a.appointment_id = i.appointment_id " +
           "WHERE a.appointment_date BETWEEN :startDate AND :endDate " +
           "AND a.status = 'COMPLETED' " +
           "AND i.payment_status IN ('PAID', 'PARTIAL_PAID') " +
           "GROUP BY sm.service_id, sm.service_name " +
           "ORDER BY total_revenue DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopServicesByRevenue(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("limit") Integer limit);
}
