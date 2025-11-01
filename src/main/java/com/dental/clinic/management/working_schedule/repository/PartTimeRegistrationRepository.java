package com.dental.clinic.management.working_schedule.repository;

import com.dental.clinic.management.working_schedule.domain.PartTimeRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for part_time_registrations table (Schema V14 - Luồng 2).
 * Used for Part-Time FLEX employees who claim flexible slots.
 */
@Repository
public interface PartTimeRegistrationRepository extends JpaRepository<PartTimeRegistration, Integer> {

    /**
     * Find all active registrations that have expired (effective_to < today).
     * Used by CleanupExpiredFlexRegistrationsJob (P11) to deactivate ghost
     * occupants.
     *
     * This fixes a critical bug where expired registrations still count as
     * "registered"
     * in API GET /api/v1/work-slots, making slots appear full when they're not.
     *
     * SQL Equivalent:
     * SELECT * FROM part_time_registrations
     * WHERE is_active = true AND effective_to < CURRENT_DATE
     *
     * @param isActive    filter by active status (should be true to find expired
     *                    but still active)
     * @param effectiveTo date to compare against (should be today)
     * @return list of expired registrations still marked as active
     */
    List<PartTimeRegistration> findByIsActiveAndEffectiveToLessThan(
            Boolean isActive,
            LocalDate effectiveTo);
}
