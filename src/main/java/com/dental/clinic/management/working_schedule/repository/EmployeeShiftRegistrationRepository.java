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
    Optional<EmployeeShiftRegistration> findByRegistrationIdAndEmployeeId(String registrationId, Integer employeeId);

    /**
     * Check if there is an active registration for the same employee, slot, and day
     * of week.
     * Used to prevent conflicts when creating new registrations.
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM EmployeeShiftRegistration r JOIN r.registrationDays rd " +
            "WHERE r.employeeId = :employeeId " +
            "AND r.slotId = :slotId " +
            "AND rd.id.dayOfWeek = :dayOfWeek " +
            "AND r.isActive = true")
    boolean existsActiveRegistrationConflict(@Param("employeeId") Integer employeeId,
            @Param("slotId") String slotId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek);

    /**
     * Find all active registrations with conflicts for validation.
     */
    @Query("SELECT DISTINCT r FROM EmployeeShiftRegistration r " +
            "JOIN FETCH r.registrationDays rd " +
            "WHERE r.employeeId = :employeeId " +
            "AND r.slotId = :slotId " +
            "AND rd.id.dayOfWeek IN :daysOfWeek " +
            "AND r.isActive = true")
    List<EmployeeShiftRegistration> findConflictingRegistrations(@Param("employeeId") Integer employeeId,
            @Param("slotId") String slotId,
            @Param("daysOfWeek") List<DayOfWeek> daysOfWeek);
}
