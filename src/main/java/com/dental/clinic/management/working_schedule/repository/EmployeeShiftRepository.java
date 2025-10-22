package com.dental.clinic.management.working_schedule.repository;

import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for EmployeeShift entity.
 */
@Repository
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {

    /**
     * Find all shifts for an employee.
     *
     * @param employeeId the employee ID
     * @return list of shifts
     */
    List<EmployeeShift> findByEmployeeEmployeeIdOrderByWorkDateAsc(Integer employeeId);

    /**
     * Find shifts for an employee within a date range.
     *
     * @param employeeId the employee ID
     * @param startDate  start date (inclusive)
     * @param endDate    end date (inclusive)
     * @return list of shifts
     */
    @Query("SELECT es FROM EmployeeShift es " +
            "WHERE es.employee.employeeId = :employeeId " +
            "AND es.workDate BETWEEN :startDate AND :endDate " +
            "ORDER BY es.workDate ASC")
    List<EmployeeShift> findByEmployeeAndDateRange(
            @Param("employeeId") Integer employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Check if a shift already exists for employee on a specific date and shift.
     *
     * @param employeeId the employee ID
     * @param workDate   the work date
     * @param shiftId    the work shift ID
     * @return true if exists
     */
    @Query("SELECT COUNT(es) > 0 FROM EmployeeShift es " +
            "WHERE es.employee.employeeId = :employeeId " +
            "AND es.workDate = :workDate " +
            "AND es.workShift.shiftId = :shiftId")
    boolean existsByEmployeeAndDateAndShift(
            @Param("employeeId") Integer employeeId,
            @Param("workDate") LocalDate workDate,
            @Param("shiftId") String shiftId);

    /**
     * Delete shifts for a specific employee within a date range.
     * Used for regenerating schedules.
     *
     * @param employeeId the employee ID
     * @param startDate  start date
     * @param endDate    end date
     */
    @Query("DELETE FROM EmployeeShift es " +
            "WHERE es.employee.employeeId = :employeeId " +
            "AND es.workDate BETWEEN :startDate AND :endDate")
    void deleteByEmployeeAndDateRange(
            @Param("employeeId") Integer employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
