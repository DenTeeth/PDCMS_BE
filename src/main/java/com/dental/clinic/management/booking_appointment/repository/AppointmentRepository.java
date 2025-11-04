package com.dental.clinic.management.booking_appointment.repository;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Appointment Entity
 * Critical queries for availability checking and resource locking
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

        /**
         * Find appointment by unique code
         */
        Optional<Appointment> findByAppointmentCode(String appointmentCode);

        /**
         * Find all appointments for a specific employee within date range
         * Used for: Checking doctor's busy time slots
         *
         * @param employeeId Bác sĩ chính hoặc participant
         * @param startTime  Range start (inclusive)
         * @param endTime    Range end (inclusive)
         * @param statuses   Filter by statuses (exclude CANCELLED, NO_SHOW)
         * @return List of appointments
         */
        @Query("SELECT a FROM Appointment a " +
                        "WHERE a.employeeId = :employeeId " +
                        "AND a.status IN :statuses " +
                        "AND ((a.appointmentStartTime >= :startTime AND a.appointmentStartTime < :endTime) " +
                        "OR (a.appointmentEndTime > :startTime AND a.appointmentEndTime <= :endTime) " +
                        "OR (a.appointmentStartTime <= :startTime AND a.appointmentEndTime >= :endTime))")
        List<Appointment> findByEmployeeAndTimeRange(
                        @Param("employeeId") Integer employeeId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("statuses") List<AppointmentStatus> statuses);

        /**
         * Find all appointments for a specific room within date range
         * Used for: Checking room availability
         *
         * @param roomId Room ID (VARCHAR matching rooms.room_id)
         */
        @Query("SELECT a FROM Appointment a " +
                        "WHERE a.roomId = :roomId " +
                        "AND a.status IN :statuses " +
                        "AND ((a.appointmentStartTime >= :startTime AND a.appointmentStartTime < :endTime) " +
                        "OR (a.appointmentEndTime > :startTime AND a.appointmentEndTime <= :endTime) " +
                        "OR (a.appointmentStartTime <= :startTime AND a.appointmentEndTime >= :endTime))")
        List<Appointment> findByRoomAndTimeRange(
                        @Param("roomId") String roomId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("statuses") List<AppointmentStatus> statuses);

        /**
         * Find all appointments for a specific patient within date range
         * Used for: Checking patient availability (prevent double booking)
         *
         * @param patientId Patient ID
         * @param startTime Range start (inclusive)
         * @param endTime   Range end (inclusive)
         * @param statuses  Filter by statuses (exclude CANCELLED, NO_SHOW)
         * @return List of appointments
         */
        @Query("SELECT a FROM Appointment a " +
                        "WHERE a.patientId = :patientId " +
                        "AND a.status IN :statuses " +
                        "AND ((a.appointmentStartTime >= :startTime AND a.appointmentStartTime < :endTime) " +
                        "OR (a.appointmentEndTime > :startTime AND a.appointmentEndTime <= :endTime) " +
                        "OR (a.appointmentStartTime <= :startTime AND a.appointmentEndTime >= :endTime))")
        List<Appointment> findByPatientAndTimeRange(
                        @Param("patientId") Integer patientId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("statuses") List<AppointmentStatus> statuses);

        /**
         * Find appointments for a patient
         */
        List<Appointment> findByPatientIdOrderByAppointmentStartTimeDesc(Integer patientId);

        /**
         * Find appointments by status
         */
        List<Appointment> findByStatus(AppointmentStatus status);

        /**
         * Check if time slot conflicts with existing appointments for an employee
         * Used for: Preventing double-booking
         */
        @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
                        "WHERE a.employeeId = :employeeId " +
                        "AND a.status IN ('SCHEDULED', 'CHECKED_IN', 'IN_PROGRESS') " +
                        "AND ((a.appointmentStartTime < :endTime AND a.appointmentEndTime > :startTime))")
        boolean existsConflictForEmployee(
                        @Param("employeeId") Integer employeeId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * Check if time slot conflicts with existing appointments for a room
         *
         * @param roomId Room ID (VARCHAR matching rooms.room_id)
         */
        @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
                        "WHERE a.roomId = :roomId " +
                        "AND a.status IN ('SCHEDULED', 'CHECKED_IN', 'IN_PROGRESS') " +
                        "AND ((a.appointmentStartTime < :endTime AND a.appointmentEndTime > :startTime))")
        boolean existsConflictForRoom(
                        @Param("roomId") String roomId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        // ==================== DASHBOARD QUERIES (P3.3) ====================

        /**
         * Find appointments with filters (for users with VIEW_APPOINTMENT_ALL)
         * Supports: date range, status, patientId, employeeId, roomId
         *
         * CRITICAL IMPROVEMENT: Search by patient name/phone
         * Uses JPQL with JOIN to patients table for LIKE search
         */
        @Query("SELECT DISTINCT a FROM Appointment a " +
                        "LEFT JOIN Patient p ON a.patientId = p.patientId " +
                        "LEFT JOIN Employee e ON a.employeeId = e.employeeId " +
                        "WHERE (:startDate IS NULL OR a.appointmentStartTime >= :startDate) " +
                        "AND (:endDate IS NULL OR a.appointmentStartTime <= :endDate) " +
                        "AND (:statuses IS NULL OR a.status IN :statuses) " +
                        "AND (:patientId IS NULL OR a.patientId = :patientId) " +
                        "AND (:employeeId IS NULL OR a.employeeId = :employeeId) " +
                        "AND (:roomId IS NULL OR a.roomId = :roomId) " +
                        "AND (:patientName IS NULL OR " +
                        "     LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :patientName, '%'))) "
                        +
                        "AND (:patientPhone IS NULL OR p.phone LIKE CONCAT('%', :patientPhone, '%'))")
        Page<Appointment> findByFilters(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("statuses") List<AppointmentStatus> statuses,
                        @Param("patientId") Integer patientId,
                        @Param("employeeId") Integer employeeId,
                        @Param("roomId") String roomId,
                        @Param("patientName") String patientName,
                        @Param("patientPhone") String patientPhone,
                        Pageable pageable);

        /**
         * Find appointments for a specific patient (RBAC: VIEW_APPOINTMENT_OWN)
         * Patient can only see their own appointments
         *
         * IMPROVEMENT: Also supports date/status filters
         */
        @Query("SELECT a FROM Appointment a " +
                        "WHERE a.patientId = :patientId " +
                        "AND (:startDate IS NULL OR a.appointmentStartTime >= :startDate) " +
                        "AND (:endDate IS NULL OR a.appointmentStartTime <= :endDate) " +
                        "AND (:statuses IS NULL OR a.status IN :statuses)")
        Page<Appointment> findByPatientIdWithFilters(
                        @Param("patientId") Integer patientId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("statuses") List<AppointmentStatus> statuses,
                        Pageable pageable);

        /**
         * Find appointments where employee is primary doctor OR participant
         * (RBAC: VIEW_APPOINTMENT_OWN for employees)
         *
         * Logic: WHERE (a.employee_id = :employeeId OR EXISTS participant)
         *
         * CRITICAL: OBSERVER role handling
         * - If participantRole = 'OBSERVER': Can see appointment in list BUT:
         * - Should NOT see full medical history (handled by separate permission)
         * - Can only see basic info: time, doctor, patient name (no sensitive data)
         * - This is controlled by APPOINTMENT:VIEW_OWN permission
         */
        @Query("SELECT DISTINCT a FROM Appointment a " +
                        "LEFT JOIN AppointmentParticipant ap ON ap.appointmentId = a.appointmentId " +
                        "WHERE (a.employeeId = :employeeId OR ap.employeeId = :employeeId) " +
                        "AND (:startDate IS NULL OR a.appointmentStartTime >= :startDate) " +
                        "AND (:endDate IS NULL OR a.appointmentStartTime <= :endDate) " +
                        "AND (:statuses IS NULL OR a.status IN :statuses)")
        Page<Appointment> findByEmployeeIdWithFilters(
                        @Param("employeeId") Integer employeeId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("statuses") List<AppointmentStatus> statuses,
                        Pageable pageable);

        /**
         * NEW: Find appointments by service code
         * Use case: "Cho tôi xem tất cả lịch Implant tuần này"
         *
         * Requires JOIN to appointment_services + services tables
         */
        @Query("SELECT DISTINCT a FROM Appointment a " +
                        "JOIN AppointmentService asvc ON asvc.id.appointmentId = a.appointmentId " +
                        "JOIN Service s ON asvc.id.serviceId = s.serviceId " +
                        "WHERE s.serviceCode = :serviceCode " +
                        "AND (:startDate IS NULL OR a.appointmentStartTime >= :startDate) " +
                        "AND (:endDate IS NULL OR a.appointmentStartTime <= :endDate) " +
                        "AND (:statuses IS NULL OR a.status IN :statuses)")
        Page<Appointment> findByServiceCodeWithFilters(
                        @Param("serviceCode") String serviceCode,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("statuses") List<AppointmentStatus> statuses,
                        Pageable pageable);
}
