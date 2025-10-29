package com.dental.clinic.management.working_schedule.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dental.clinic.management.working_schedule.domain.EmployeeShiftRegistration;
import com.dental.clinic.management.working_schedule.enums.DayOfWeek;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeShiftRegistrationRepository extends JpaRepository<EmployeeShiftRegistration, String> {

        @EntityGraph(attributePaths = "registrationDays")
        Page<EmployeeShiftRegistration> findAll(Pageable pageable);

        @EntityGraph(attributePaths = "registrationDays")
        Page<EmployeeShiftRegistration> findByEmployeeId(Integer employeeId, Pageable pageable);

        @EntityGraph(attributePaths = "registrationDays")
        Optional<EmployeeShiftRegistration> findByRegistrationId(String registrationId);

        @EntityGraph(attributePaths = "registrationDays")
        Optional<EmployeeShiftRegistration> findByRegistrationIdAndEmployeeId(String registrationId,
                        Integer employeeId);

        /**
         * Check if there is an active registration for the same employee, work shift, and day
         * of week.
         * Used to prevent conflicts when creating new registrations.
         */
        @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
                        "FROM EmployeeShiftRegistration r JOIN r.registrationDays rd " +
                        "WHERE r.employeeId = :employeeId " +
                        "AND r.workShiftId = :workShiftId " +
                        "AND rd.id.dayOfWeek = :dayOfWeek " +
                        "AND r.isActive = true")
        boolean existsActiveRegistrationConflict(@Param("employeeId") Integer employeeId,
                        @Param("workShiftId") String workShiftId,
                        @Param("dayOfWeek") DayOfWeek dayOfWeek);

        /**
         * Find all active registrations with conflicts for validation.
         */
        @Query("SELECT DISTINCT r FROM EmployeeShiftRegistration r " +
                        "JOIN FETCH r.registrationDays rd " +
                        "WHERE r.employeeId = :employeeId " +
                        "AND r.workShiftId = :workShiftId " +
                        "AND rd.id.dayOfWeek IN :daysOfWeek " +
                        "AND r.isActive = true")
        List<EmployeeShiftRegistration> findConflictingRegistrations(@Param("employeeId") Integer employeeId,
                        @Param("workShiftId") String workShiftId,
                        @Param("daysOfWeek") List<DayOfWeek> daysOfWeek);

        /**
         * Find all active registrations as of a specific date.
         * Used by weekly part-time schedule job.
         *
         * @param asOfDate the date to check
         * @return list of active registrations
         */
        @EntityGraph(attributePaths = "registrationDays")
        @Query("SELECT r FROM EmployeeShiftRegistration r " +
                        "WHERE r.isActive = true " +
                        "AND r.effectiveFrom <= :asOfDate " +
                        "AND (r.effectiveTo IS NULL OR r.effectiveTo >= :asOfDate)")
        List<EmployeeShiftRegistration> findActiveRegistrations(@Param("asOfDate") java.time.LocalDate asOfDate);

        /**
         * Find registrations expiring within a certain number of days.
         * Used by daily renewal detection job.
         *
         * @param daysAhead number of days to look ahead
         * @param checkDate the date to check from
         * @return list of expiring registrations
         */
        @EntityGraph(attributePaths = "registrationDays")
        @Query("SELECT r FROM EmployeeShiftRegistration r " +
                        "WHERE r.isActive = true " +
                        "AND r.effectiveTo IS NOT NULL " +
                        "AND r.effectiveTo = :targetDate")
        List<EmployeeShiftRegistration> findRegistrationsExpiringOn(
                        @Param("targetDate") java.time.LocalDate targetDate);

        /**
         * Check if a work shift is being used by any employee shift registrations.
         * Used to prevent deletion/modification of shifts that have part-time registrations.
         *
         * @param workShiftId the work shift ID to check
         * @return true if the shift has any registrations, false otherwise
         */
        @Query("SELECT COUNT(r) > 0 FROM EmployeeShiftRegistration r " +
                        "WHERE r.workShiftId = :workShiftId")
        boolean existsByWorkShiftId(@Param("workShiftId") String workShiftId);

        /**
         * Count the number of employee shift registrations using a specific work shift.
         * Used to provide detailed error messages when preventing shift deletion/modification.
         *
         * @param workShiftId the work shift ID to count
         * @return count of registrations using this shift
         */
        @Query("SELECT COUNT(r) FROM EmployeeShiftRegistration r " +
                        "WHERE r.workShiftId = :workShiftId")
        long countByWorkShiftId(@Param("workShiftId") String workShiftId);
}
