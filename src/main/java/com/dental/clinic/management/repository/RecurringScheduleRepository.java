package com.dental.clinic.management.repository;

import com.dental.clinic.management.domain.RecurringSchedule;
import com.dental.clinic.management.domain.enums.DayOfWeek;
import com.dental.clinic.management.domain.enums.WorkShiftType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RecurringSchedule entity.
 * Manages repeating weekly work patterns for full-time employees.
 */
@Repository
public interface RecurringScheduleRepository extends JpaRepository<RecurringSchedule, String> {

    /**
     * Find recurring schedule by unique business code.
     * @param recurringCode Recurring code (e.g., REC_20251015_001)
     * @return Optional recurring schedule entity
     */
    Optional<RecurringSchedule> findByRecurringCode(String recurringCode);

    /**
     * Check if recurring code exists.
     * @param recurringCode Recurring code to check
     * @return True if exists
     */
    boolean existsByRecurringCode(String recurringCode);

    /**
     * Find all active recurring schedules for an employee.
     * @param employeeId Employee ID
     * @param isActive Filter by active status
     * @return List of active recurring schedules
     */
    List<RecurringSchedule> findByEmployeeIdAndIsActiveOrderByDayOfWeekAsc(
        String employeeId, Boolean isActive);

    /**
     * Find all recurring schedules for an employee (including inactive).
     * @param employeeId Employee ID
     * @param pageable Pagination parameters
     * @return Page of recurring schedules
     */
    Page<RecurringSchedule> findByEmployeeIdOrderByDayOfWeekAsc(String employeeId, Pageable pageable);

    /**
     * Find active schedules for a specific day of week.
     * Used for generating daily employee_schedules.
     * 
     * @param dayOfWeek Day of week enum
     * @param isActive Filter by active status
     * @return List of active recurring schedules
     */
    List<RecurringSchedule> findByDayOfWeekAndIsActive(DayOfWeek dayOfWeek, Boolean isActive);

    /**
     * Find employee's schedule for a specific day of week.
     * @param employeeId Employee ID
     * @param dayOfWeek Day of week
     * @return List of schedules
     */
    List<RecurringSchedule> findByEmployeeIdAndDayOfWeek(String employeeId, DayOfWeek dayOfWeek);

    /**
     * Find schedules using a specific work shift.
     * Used when disabling/deleting a shift to check dependencies.
     * 
     * @param shiftId Work shift ID
     * @return List of recurring schedules using this shift
     */
    List<RecurringSchedule> findByShiftId(String shiftId);

    /**
     * Find active schedules using a specific work shift.
     * @param shiftId Work shift ID
     * @param isActive Filter by active status
     * @return List of active recurring schedules
     */
    List<RecurringSchedule> findByShiftIdAndIsActive(String shiftId, Boolean isActive);

    /**
     * Check for conflicting recurring schedules (same employee, same day, overlapping times).
     * Used when creating or updating a recurring schedule.
     * 
     * @param employeeId Employee ID
     * @param dayOfWeek Day of week
     * @param startTime Start time
     * @param endTime End time
     * @param excludeRecurringId Recurring ID to exclude (for updates, null for creates)
     * @return List of conflicting schedules
     */
    @Query("SELECT r FROM RecurringSchedule r WHERE r.employeeId = :employeeId " +
           "AND r.dayOfWeek = :dayOfWeek " +
           "AND r.isActive = true " +
           "AND (:excludeRecurringId IS NULL OR r.recurringId != :excludeRecurringId) " +
           "AND ((r.startTime IS NOT NULL AND r.endTime IS NOT NULL AND " +
           "((r.startTime <= :startTime AND r.endTime > :startTime) " +
           "OR (r.startTime < :endTime AND r.endTime >= :endTime) " +
           "OR (r.startTime >= :startTime AND r.endTime <= :endTime))) " +
           "OR (r.shiftId IS NOT NULL))")
    List<RecurringSchedule> findConflictingSchedules(
        @Param("employeeId") String employeeId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeRecurringId") String excludeRecurringId);

    /**
     * Get weekly schedule pattern for an employee.
     * Returns all active recurring schedules grouped by day.
     * 
     * @param employeeId Employee ID
     * @return List of [day_of_week, shift_type, start_time, end_time]
     */
    @Query("SELECT r.dayOfWeek, r.shiftType, " +
           "COALESCE(r.startTime, s.startTime), COALESCE(r.endTime, s.endTime) " +
           "FROM RecurringSchedule r " +
           "LEFT JOIN WorkShift s ON r.shiftId = s.shiftId " +
           "WHERE r.employeeId = :employeeId AND r.isActive = true " +
           "ORDER BY r.dayOfWeek")
    List<Object[]> getWeeklyPattern(@Param("employeeId") String employeeId);

    /**
     * Count active recurring schedules for an employee.
     * @param employeeId Employee ID
     * @return Number of active recurring schedules
     */
    long countByEmployeeIdAndIsActiveTrue(String employeeId);

    /**
     * Find all active recurring schedules across all employees.
     * Used for system-wide schedule generation.
     * 
     * @return List of all active recurring schedules
     */
    List<RecurringSchedule> findByIsActiveTrueOrderByEmployeeIdAscDayOfWeekAsc();
}
