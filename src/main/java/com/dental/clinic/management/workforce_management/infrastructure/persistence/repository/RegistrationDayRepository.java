package com.dental.clinic.management.workforce_management.infrastructure.persistence.repository;

import com.dental.clinic.management.workforce_management.infrastructure.persistence.entity.RegistrationDay;
import com.dental.clinic.management.workforce_management.domain.model.RegistrationDayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for RegistrationDay entity.
 */
@Repository
public interface RegistrationDayRepository extends JpaRepository<RegistrationDay, RegistrationDay.RegistrationDayId> {

    /**
     * Find all registration days by registration ID.
     * @param registrationId the registration ID
     * @return List of registration days
     */
    List<RegistrationDay> findByIdRegistrationId(String registrationId);

    /**
     * Find registration days by day of week.
     * @param dayOfWeek the day of week
     * @return List of registration days
     */
    List<RegistrationDay> findByIdDayOfWeek(RegistrationDayOfWeek dayOfWeek);

    /**
     * Delete all registration days by registration ID.
     * @param registrationId the registration ID
     */
    void deleteByIdRegistrationId(String registrationId);
}