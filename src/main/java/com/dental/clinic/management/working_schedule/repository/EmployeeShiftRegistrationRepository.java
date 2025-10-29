package com.dental.clinic.management.working_schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dental.clinic.management.working_schedule.domain.EmployeeShiftRegistration;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeShiftRegistrationRepository extends JpaRepository<EmployeeShiftRegistration, String> {

        // V2: Removed EntityGraph for registrationDays (no longer exists in V2 schema)

        Optional<EmployeeShiftRegistration> findByRegistrationId(String registrationId);

        Optional<EmployeeShiftRegistration> findByRegistrationIdAndEmployeeId(String registrationId,
                        Integer employeeId);

        // V2: Removed conflict checking queries that referenced registrationDays (V1 schema)

        /**
         * Find all active registrations as of a specific date.
         * Used by weekly part-time schedule job.
         *
         * @param asOfDate the date to check
         * @return list of active registrations
         */
        @Query("SELECT r FROM EmployeeShiftRegistration r " +
                        "WHERE r.isActive = true " +
                        "AND r.effectiveFrom <= :asOfDate " +
                        "AND (r.effectiveTo IS NULL OR r.effectiveTo >= :asOfDate)")
        List<EmployeeShiftRegistration> findActiveRegistrations(@Param("asOfDate") java.time.LocalDate asOfDate);

        /**
         * Find registrations expiring within a certain number of days.
         * Used by daily renewal detection job.
         *
         * @param targetDate the target expiration date
         * @return list of expiring registrations
         */
        @Query("SELECT r FROM EmployeeShiftRegistration r " +
                        "WHERE r.isActive = true " +
                        "AND r.effectiveTo IS NOT NULL " +
                        "AND r.effectiveTo = :targetDate")
        List<EmployeeShiftRegistration> findRegistrationsExpiringOn(
                        @Param("targetDate") java.time.LocalDate targetDate);

        // V2: Removed workShiftId queries (V1 schema - no longer exists in V2)

        /**
         * Find all registrations for an employee (regardless of isActive status).
         */
        List<EmployeeShiftRegistration> findByEmployeeId(Integer employeeId);

        /**
         * Find all active registrations for an employee.
         */
        List<EmployeeShiftRegistration> findByEmployeeIdAndIsActive(Integer employeeId, Boolean isActive);

        /**
         * Check if employee has any active registration.
         */
        boolean existsByEmployeeIdAndIsActive(Integer employeeId, Boolean isActive);
}
