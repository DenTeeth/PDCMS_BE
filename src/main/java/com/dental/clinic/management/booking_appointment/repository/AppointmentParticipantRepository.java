package com.dental.clinic.management.booking_appointment.repository;

import com.dental.clinic.management.booking_appointment.domain.AppointmentParticipant;
import com.dental.clinic.management.booking_appointment.domain.AppointmentParticipant.AppointmentParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AppointmentParticipant Entity
 * Manages assistant/secondary doctor assignments to appointments
 */
@Repository
public interface AppointmentParticipantRepository
        extends JpaRepository<AppointmentParticipant, AppointmentParticipantId> {

    /**
     * Find all participants for a specific appointment
     */
    List<AppointmentParticipant> findByIdAppointmentId(Integer appointmentId);

    /**
     * Find all participants for a specific appointment with employee and account eagerly loaded
     * This is used for sending notifications to participants
     */
    @Query("SELECT ap FROM AppointmentParticipant ap " +
            "JOIN FETCH ap.employee e " +
            "LEFT JOIN FETCH e.account " +
            "WHERE ap.id.appointmentId = :appointmentId")
    List<AppointmentParticipant> findByAppointmentIdWithEmployeeAndAccount(@Param("appointmentId") Integer appointmentId);

    /**
     * Find all appointments where an employee is a participant (not primary doctor)
     * Used for: Checking assistant's busy time slots
     */
    @Query("SELECT ap FROM AppointmentParticipant ap " +
            "JOIN Appointment a ON ap.id.appointmentId = a.appointmentId " +
            "WHERE ap.id.employeeId = :employeeId " +
            "AND a.status IN ('SCHEDULED', 'CHECKED_IN', 'IN_PROGRESS') " +
            "AND ((a.appointmentStartTime >= :startTime AND a.appointmentStartTime < :endTime) " +
            "OR (a.appointmentEndTime > :startTime AND a.appointmentEndTime <= :endTime) " +
            "OR (a.appointmentStartTime <= :startTime AND a.appointmentEndTime >= :endTime))")
    List<AppointmentParticipant> findByEmployeeAndTimeRange(
            @Param("employeeId") Integer employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Check if employee has conflict as participant
     */
    @Query("SELECT COUNT(ap) > 0 FROM AppointmentParticipant ap " +
            "JOIN Appointment a ON ap.id.appointmentId = a.appointmentId " +
            "WHERE ap.id.employeeId = :employeeId " +
            "AND a.status IN ('SCHEDULED', 'CHECKED_IN', 'IN_PROGRESS') " +
            "AND ((a.appointmentStartTime < :endTime AND a.appointmentEndTime > :startTime))")
    boolean existsConflictForParticipant(
            @Param("employeeId") Integer employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Delete all participants for an appointment
     */
    void deleteByIdAppointmentId(Integer appointmentId);

    // ==================== Dashboard Statistics Queries ====================

    /**
     * Get top doctors by performance (appointments count and revenue)
     * Only counts COMPLETED appointments with PAID or PARTIAL_PAID invoices
     */
    @Query("SELECT e.employeeId, e.employeeCode, e.firstName, e.lastName, " +
           "COUNT(DISTINCT ap.id.appointmentId) as appointmentCount, " +
           "COALESCE(SUM(i.totalAmount), 0) as totalRevenue, " +
           "COALESCE(AVG(i.totalAmount), 0) as avgRevenue, " +
           "COUNT(DISTINCT aps.id.serviceId) as serviceCount " +
           "FROM AppointmentParticipant ap " +
           "JOIN ap.employee e " +
           "JOIN ap.appointment a " +
           "LEFT JOIN com.dental.clinic.management.payment.domain.Invoice i ON a.appointmentId = i.appointmentId " +
           "LEFT JOIN com.dental.clinic.management.booking_appointment.domain.AppointmentService aps ON a.appointmentId = aps.id.appointmentId " +
           "WHERE ap.role = 'DOCTOR' " +
           "AND a.appointmentStartTime BETWEEN :startDate AND :endDate " +
           "AND a.status = 'COMPLETED' " +
           "AND (i.paymentStatus IN ('PAID', 'PARTIAL_PAID') OR i.paymentStatus IS NULL) " +
           "GROUP BY e.employeeId, e.employeeCode, e.firstName, e.lastName " +
           "ORDER BY totalRevenue DESC")
    java.util.List<Object[]> getTopDoctorsByPerformance(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            org.springframework.data.domain.Pageable pageable);
}
