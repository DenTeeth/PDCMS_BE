package com.dental.clinic.management.repository;

import com.dental.clinic.management.domain.EmployeeSchedule;
import com.dental.clinic.management.domain.enums.EmployeeScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for EmployeeSchedule entity.
 * Manages attendance tracking for all employees.
 */
@Repository
public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, String> {

    /**
     * Find schedule by unique business code.
     * @param scheduleCode Schedule code (e.g., EMP_SCH_20251015_001)
     * @return Optional schedule entity
     */
    Optional<EmployeeSchedule> findByScheduleCode(String scheduleCode);

    /**
     * Check if schedule code exists.
     * @param scheduleCode Schedule code to check
     * @return True if exists
     */
    boolean existsByScheduleCode(String scheduleCode);

    /**
     * Find all schedules for an employee on a specific date.
     * @param employeeId Employee ID
     * @param workDate Work date
     * @return List of schedules
     */
    List<EmployeeSchedule> findByEmployeeIdAndWorkDate(String employeeId, LocalDate workDate);

    /**
     * Find all schedules for an employee within date range.
     * @param employeeId Employee ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param pageable Pagination parameters
     * @return Page of schedules
     */
    Page<EmployeeSchedule> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(
        String employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Find schedules by status within date range.
     * @param status Schedule status
     * @param startDate Start date
     * @param endDate End date
     * @return List of schedules
     */
    List<EmployeeSchedule> findByStatusAndWorkDateBetween(
        EmployeeScheduleStatus status, LocalDate startDate, LocalDate endDate);

    /**
     * Find all schedules for a specific date (all employees).
     * Used for daily attendance tracking.
     * 
     * @param workDate Work date
     * @return List of schedules ordered by employee
     */
    List<EmployeeSchedule> findByWorkDateOrderByEmployeeIdAscStartTimeAsc(LocalDate workDate);

    /**
     * Find schedules by employee and status within date range.
     * @param employeeId Employee ID
     * @param status Schedule status
     * @param startDate Start date
     * @param endDate End date
     * @return List of schedules
     */
    List<EmployeeSchedule> findByEmployeeIdAndStatusAndWorkDateBetween(
        String employeeId, EmployeeScheduleStatus status, LocalDate startDate, LocalDate endDate);

    /**
     * Check for conflicting schedules (overlapping time ranges on same date).
     * Used when manually creating a schedule.
     * 
     * @param employeeId Employee ID
     * @param workDate Work date
     * @param startTime Start time
     * @param endTime End time
     * @param excludeScheduleId Schedule ID to exclude (for updates, null for creates)
     * @return List of conflicting schedules
     */
    @Query("SELECT s FROM EmployeeSchedule s WHERE s.employeeId = :employeeId " +
           "AND s.workDate = :workDate " +
           "AND (:excludeScheduleId IS NULL OR s.scheduleId != :excludeScheduleId) " +
           "AND ((s.startTime <= :startTime AND s.endTime > :startTime) " +
           "OR (s.startTime < :endTime AND s.endTime >= :endTime) " +
           "OR (s.startTime >= :startTime AND s.endTime <= :endTime))")
    List<EmployeeSchedule> findConflictingSchedules(
        @Param("employeeId") String employeeId,
        @Param("workDate") LocalDate workDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeScheduleId") String excludeScheduleId);

    /**
     * Check if schedule already exists for recurring_id and work_date.
     * Prevents duplicate generation from recurring schedules.
     * 
     * @param recurringId Recurring schedule ID
     * @param workDate Work date
     * @return True if exists
     */
    boolean existsByRecurringIdAndWorkDate(String recurringId, LocalDate workDate);

    /**
     * Check if schedule already exists for dentist_schedule_id.
     * Prevents duplicate generation from dentist work schedules.
     * 
     * @param dentistScheduleId Dentist work schedule ID
     * @return True if exists
     */
    boolean existsByDentistScheduleId(String dentistScheduleId);

    /**
     * Get attendance statistics for an employee within date range.
     * Returns breakdown by status: PRESENT, LATE, ABSENT, ON_LEAVE.
     * 
     * @param employeeId Employee ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of [status, count, total_hours_scheduled, total_hours_actual]
     */
    @Query("SELECT s.status, COUNT(s), " +
           "SUM(TIMESTAMPDIFF(HOUR, s.startTime, s.endTime)), " +
           "SUM(CASE WHEN s.actualStartTime IS NOT NULL AND s.actualEndTime IS NOT NULL " +
           "THEN TIMESTAMPDIFF(HOUR, s.actualStartTime, s.actualEndTime) ELSE 0 END) " +
           "FROM EmployeeSchedule s " +
           "WHERE s.employeeId = :employeeId " +
           "AND s.workDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.status")
    List<Object[]> getAttendanceStatistics(
        @Param("employeeId") String employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Find SCHEDULED status schedules for a specific date.
     * Used by attendance system to show who should check in today.
     * 
     * @param workDate Work date
     * @return List of scheduled employees
     */
    List<EmployeeSchedule> findByWorkDateAndStatusOrderByStartTimeAsc(
        LocalDate workDate, EmployeeScheduleStatus status);

    /**
     * Calculate total working hours for an employee within date range.
     * Only counts PRESENT and LATE statuses.
     * 
     * @param employeeId Employee ID
     * @param startDate Start date
     * @param endDate End date
     * @return Total hours worked
     */
    @Query("SELECT SUM(TIMESTAMPDIFF(HOUR, s.startTime, s.endTime)) " +
           "FROM EmployeeSchedule s " +
           "WHERE s.employeeId = :employeeId " +
           "AND s.workDate BETWEEN :startDate AND :endDate " +
           "AND s.status IN ('PRESENT', 'LATE')")
    Long calculateTotalWorkingHours(
        @Param("employeeId") String employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Find late arrivals within date range.
     * Used for HR reports.
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return List of late schedules
     */
    List<EmployeeSchedule> findByStatusAndWorkDateBetweenOrderByWorkDateAsc(
        EmployeeScheduleStatus status, LocalDate startDate, LocalDate endDate);

    /**
     * Count absences for an employee within date range.
     * @param employeeId Employee ID
     * @param status Status (typically ABSENT)
     * @param startDate Start date
     * @param endDate End date
     * @return Number of absences
     */
    long countByEmployeeIdAndStatusAndWorkDateBetween(
        String employeeId, EmployeeScheduleStatus status, LocalDate startDate, LocalDate endDate);
}
