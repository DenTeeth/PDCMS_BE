package com.dental.clinic.management.working_schedule.repository;

import com.dental.clinic.management.working_schedule.domain.FixedShiftRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FixedShiftRegistration entity.
 */
@Repository
public interface FixedShiftRegistrationRepository extends JpaRepository<FixedShiftRegistration, Integer> {

    /**
     * Find all active registrations for a specific employee.
     *
     * @param employeeId employee ID
     * @return list of active registrations
     */
    @Query("SELECT fsr FROM FixedShiftRegistration fsr " +
            "LEFT JOIN FETCH fsr.registrationDays " +
            "LEFT JOIN FETCH fsr.workShift " +
            "WHERE fsr.employee.employeeId = :employeeId " +
            "AND fsr.isActive = true " +
            "ORDER BY fsr.workShift.startTime ASC")
    List<FixedShiftRegistration> findActiveByEmployeeId(@Param("employeeId") Integer employeeId);

    /**
     * Find a registration by ID with all related data fetched.
     *
     * @param registrationId registration ID
     * @return optional registration
     */
    @Query("SELECT fsr FROM FixedShiftRegistration fsr " +
            "LEFT JOIN FETCH fsr.registrationDays " +
            "LEFT JOIN FETCH fsr.workShift " +
            "LEFT JOIN FETCH fsr.employee " +
            "WHERE fsr.registrationId = :registrationId")
    Optional<FixedShiftRegistration> findByIdWithDetails(@Param("registrationId") Integer registrationId);

    /**
     * Check if an employee already has an active registration for a specific work
     * shift.
     * Used to prevent duplicate registrations.
     *
     * @param employeeId  employee ID
     * @param workShiftId work shift ID
     * @return true if duplicate exists
     */
    @Query("SELECT COUNT(fsr) > 0 FROM FixedShiftRegistration fsr " +
            "WHERE fsr.employee.employeeId = :employeeId " +
            "AND fsr.workShift.workShiftId = :workShiftId " +
            "AND fsr.isActive = true")
    boolean existsActiveByEmployeeAndWorkShift(
            @Param("employeeId") Integer employeeId,
            @Param("workShiftId") String workShiftId);

    /**
     * Find all active registrations (for admin view).
     *
     * @return list of all active registrations
     */
    @Query("SELECT fsr FROM FixedShiftRegistration fsr " +
            "LEFT JOIN FETCH fsr.registrationDays " +
            "LEFT JOIN FETCH fsr.workShift " +
            "LEFT JOIN FETCH fsr.employee " +
            "WHERE fsr.isActive = true " +
            "ORDER BY fsr.employee.employeeId ASC, fsr.workShift.startTime ASC")
    List<FixedShiftRegistration> findAllActive();

    /**
     * Count active registrations for an employee.
     *
     * @param employeeId employee ID
     * @return count of active registrations
     */
    @Query("SELECT COUNT(fsr) FROM FixedShiftRegistration fsr " +
            "WHERE fsr.employee.employeeId = :employeeId " +
            "AND fsr.isActive = true")
    long countActiveByEmployeeId(@Param("employeeId") Integer employeeId);

    /**
     * Find all registrations filtered by active status.
     * 
     * @param isActive filter by active status (null = all, true = active only, false = inactive only)
     * @return list of registrations
     */
    @Query("SELECT fsr FROM FixedShiftRegistration fsr " +
            "LEFT JOIN FETCH fsr.registrationDays " +
            "LEFT JOIN FETCH fsr.workShift " +
            "LEFT JOIN FETCH fsr.employee " +
            "WHERE (:isActive IS NULL OR fsr.isActive = :isActive) " +
            "ORDER BY fsr.employee.employeeId ASC, fsr.workShift.startTime ASC")
    List<FixedShiftRegistration> findAllByActiveStatus(@Param("isActive") Boolean isActive);

    /**
     * Find registrations by employee ID filtered by active status.
     * 
     * @param employeeId employee ID
     * @param isActive filter by active status (null = all, true = active only, false = inactive only)
     * @return list of registrations
     */
    @Query("SELECT fsr FROM FixedShiftRegistration fsr " +
            "LEFT JOIN FETCH fsr.registrationDays " +
            "LEFT JOIN FETCH fsr.workShift " +
            "WHERE fsr.employee.employeeId = :employeeId " +
            "AND (:isActive IS NULL OR fsr.isActive = :isActive) " +
            "ORDER BY fsr.workShift.startTime ASC")
    List<FixedShiftRegistration> findByEmployeeIdAndActiveStatus(
            @Param("employeeId") Integer employeeId,
            @Param("isActive") Boolean isActive);

    /**
     * Check if employee has a fixed schedule on a specific date and shift.
     * Used to detect conflicts before creating overtime requests.
     * Checks both:
     * 1. Registration is active (is_active = true)
     * 2. Effective date range covers the work_date
     * 3. Registration days includes the day of week
     * 4. Work shift matches
     *
     * @param employeeId  employee ID
     * @param workDate    the date to check
     * @param workShiftId work shift ID
     * @return true if employee has a fixed schedule on this date/shift
     */
    @Query("SELECT COUNT(fsr) > 0 FROM FixedShiftRegistration fsr " +
            "JOIN fsr.registrationDays rd " +
            "WHERE fsr.employee.employeeId = :employeeId " +
            "AND fsr.workShift.workShiftId = :workShiftId " +
            "AND fsr.isActive = true " +
            "AND fsr.effectiveFrom <= :workDate " +
            "AND (fsr.effectiveTo IS NULL OR fsr.effectiveTo >= :workDate) " +
            "AND rd.dayOfWeek = UPPER(TRIM(FUNCTION('TO_CHAR', CAST(:workDate AS DATE), 'DAY')))")
    boolean hasFixedScheduleOnDate(
            @Param("employeeId") Integer employeeId,
            @Param("workDate") java.time.LocalDate workDate,
            @Param("workShiftId") String workShiftId);
}
