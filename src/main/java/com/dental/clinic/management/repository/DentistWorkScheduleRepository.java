package com.dental.clinic.management.repository;

import com.dental.clinic.management.domain.DentistWorkSchedule;
import com.dental.clinic.management.domain.enums.DentistWorkScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DentistWorkSchedule entity.
 * Manages part-time dentist flexible schedules.
 */
@Repository
public interface DentistWorkScheduleRepository extends JpaRepository<DentistWorkSchedule, String> {

    /**
     * Find schedule by unique business code.
     * @param scheduleCode Schedule code (e.g., SCH_20251015_001)
     * @return Optional schedule entity
     */
    Optional<DentistWorkSchedule> findByScheduleCode(String scheduleCode);

    /**
     * Check if schedule code exists.
     * @param scheduleCode Schedule code to check
     * @return True if exists
     */
    boolean existsByScheduleCode(String scheduleCode);

    /**
     * Find all schedules for a dentist on a specific date.
     * @param dentistId Dentist employee ID
     * @param workDate Work date
     * @return List of schedules
     */
    List<DentistWorkSchedule> findByDentistIdAndWorkDate(String dentistId, LocalDate workDate);

    /**
     * Find all schedules for a dentist within date range.
     * @param dentistId Dentist employee ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param pageable Pagination parameters
     * @return Page of schedules
     */
    Page<DentistWorkSchedule> findByDentistIdAndWorkDateBetween(
        String dentistId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Find available schedules for a dentist within date range.
     * @param dentistId Dentist employee ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param status Schedule status
     * @return List of available schedules
     */
    List<DentistWorkSchedule> findByDentistIdAndWorkDateBetweenAndStatus(
        String dentistId, LocalDate startDate, LocalDate endDate, DentistWorkScheduleStatus status);

    /**
     * Find all available schedules for booking (any dentist).
     * @param workDate Work date
     * @param status Schedule status (typically AVAILABLE)
     * @return List of available schedules
     */
    List<DentistWorkSchedule> findByWorkDateAndStatusOrderByStartTimeAsc(
        LocalDate workDate, DentistWorkScheduleStatus status);

    /**
     * Find available schedules within date range for calendar view.
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param status Schedule status
     * @return List of available schedules
     */
    List<DentistWorkSchedule> findByWorkDateBetweenAndStatusOrderByWorkDateAscStartTimeAsc(
        LocalDate startDate, LocalDate endDate, DentistWorkScheduleStatus status);

    /**
     * Check for conflicting schedules (overlapping time ranges on same date).
     * Used when creating or updating a schedule.
     * 
     * @param dentistId Dentist employee ID
     * @param workDate Work date
     * @param startTime Start time
     * @param endTime End time
     * @param excludeScheduleId Schedule ID to exclude (for updates, null for creates)
     * @return List of conflicting schedules
     */
    @Query("SELECT s FROM DentistWorkSchedule s WHERE s.dentistId = :dentistId " +
           "AND s.workDate = :workDate " +
           "AND s.status NOT IN ('CANCELLED', 'EXPIRED') " +
           "AND (:excludeScheduleId IS NULL OR s.scheduleId != :excludeScheduleId) " +
           "AND ((s.startTime <= :startTime AND s.endTime > :startTime) " +
           "OR (s.startTime < :endTime AND s.endTime >= :endTime) " +
           "OR (s.startTime >= :startTime AND s.endTime <= :endTime))")
    List<DentistWorkSchedule> findConflictingSchedules(
        @Param("dentistId") String dentistId,
        @Param("workDate") LocalDate workDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeScheduleId") String excludeScheduleId);

    /**
     * Count schedules for a dentist on a specific date.
     * Used to enforce max 2 schedules per day limit.
     * 
     * @param dentistId Dentist employee ID
     * @param workDate Work date
     * @return Number of schedules (excluding CANCELLED and EXPIRED)
     */
    @Query("SELECT COUNT(s) FROM DentistWorkSchedule s WHERE s.dentistId = :dentistId " +
           "AND s.workDate = :workDate " +
           "AND s.status NOT IN ('CANCELLED', 'EXPIRED')")
    long countActiveSchedulesByDentistAndDate(
        @Param("dentistId") String dentistId,
        @Param("workDate") LocalDate workDate);

    /**
     * Find expired schedules that need status update.
     * Scheduled job will mark these as EXPIRED.
     * 
     * @param currentDate Current date
     * @return List of expired schedules
     */
    @Query("SELECT s FROM DentistWorkSchedule s WHERE s.workDate < :currentDate " +
           "AND s.status IN ('AVAILABLE', 'BOOKED')")
    List<DentistWorkSchedule> findExpiredSchedules(@Param("currentDate") LocalDate currentDate);

    /**
     * Get statistics for a dentist within date range.
     * Returns total hours, total schedules, and breakdown by status.
     * 
     * @param dentistId Dentist employee ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of statistics [status, count, total_hours]
     */
    @Query("SELECT s.status, COUNT(s), " +
           "SUM(TIMESTAMPDIFF(HOUR, s.startTime, s.endTime)) " +
           "FROM DentistWorkSchedule s " +
           "WHERE s.dentistId = :dentistId " +
           "AND s.workDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.status")
    List<Object[]> getScheduleStatistics(
        @Param("dentistId") String dentistId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Find schedules by status, ordered by date and time.
     * @param status Schedule status
     * @param pageable Pagination parameters
     * @return Page of schedules
     */
    Page<DentistWorkSchedule> findByStatusOrderByWorkDateAscStartTimeAsc(
        DentistWorkScheduleStatus status, Pageable pageable);
}
