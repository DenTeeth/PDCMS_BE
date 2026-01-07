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
     * Uses invoice_items to get actual revenue with price and quantity
     */
    @Query(value = "SELECT " +
           "ii.service_id, " +
           "ii.service_name, " +
           "COUNT(DISTINCT a.appointment_id) as usage_count, " +
           "SUM(ii.unit_price * ii.quantity) as total_revenue " +
           "FROM invoice_items ii " +
           "JOIN invoices i ON ii.invoice_id = i.invoice_id " +
           "JOIN appointments a ON i.appointment_id = a.appointment_id " +
           "WHERE a.appointment_start_time BETWEEN :startDate AND :endDate " +
           "AND a.status = 'COMPLETED' " +
           "AND i.payment_status IN ('PAID', 'PARTIAL_PAID') " +
           "GROUP BY ii.service_id, ii.service_name " +
           "ORDER BY total_revenue DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopServicesByRevenue(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("limit") Integer limit);
}
