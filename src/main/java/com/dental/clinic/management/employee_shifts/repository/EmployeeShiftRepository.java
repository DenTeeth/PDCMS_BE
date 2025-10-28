package com.dental.clinic.management.employee_shifts.repository;

import com.dental.clinic.management.employee_shifts.domain.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for EmployeeShift entity.
 */
@Repository
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, String> {

        /**
         * Check if an employee already has a shift for a specific work shift and date.
         * 
         * @param employeeId  the employee's ID
         * @param workShiftId the work shift ID
         * @param workDate    the work date
         * @return true if a shift already exists, false otherwise
         */
        boolean existsByEmployee_EmployeeIdAndWorkShift_WorkShiftIdAndWorkDate(
                        Long employeeId,
                        String workShiftId,
                        LocalDate workDate);

        /**
         * Find all shifts for an employee within a date range.
         * 
         * @param employeeId the employee's ID
         * @param startDate  start date (inclusive)
         * @param endDate    end date (inclusive)
         * @return List of employee shifts
         */
        List<EmployeeShift> findByEmployee_EmployeeIdAndWorkDateBetween(
                        Long employeeId,
                        LocalDate startDate,
                        LocalDate endDate);

        /**
         * Find all shifts within a date range.
         * 
         * @param startDate start date (inclusive)
         * @param endDate   end date (inclusive)
         * @return List of employee shifts
         */
        List<EmployeeShift> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);

        /**
         * Find all shifts on a specific work date.
         * 
         * @param workDate the work date
         * @return List of employee shifts
         */
        List<EmployeeShift> findByWorkDate(LocalDate workDate);
}
