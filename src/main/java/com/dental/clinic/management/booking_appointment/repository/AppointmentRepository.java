package com.dental.clinic.management.booking_appointment.repository;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
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
}
