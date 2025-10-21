package com.dental.clinic.management.working_schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dental.clinic.management.working_schedule.domain.RegistrationDays;
import com.dental.clinic.management.working_schedule.domain.RegistrationDaysId;

import java.util.List;

@Repository
public interface RegistrationDaysRepository extends JpaRepository<RegistrationDays, RegistrationDaysId> {

    List<RegistrationDays> findByIdRegistrationId(String registrationId);
}
