package com.dental.clinic.management.employee_shift_registrations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dental.clinic.management.employee_shift_registrations.domain.RegistrationDays;
import com.dental.clinic.management.employee_shift_registrations.domain.RegistrationDaysId;

@Repository
public interface RegistrationDaysRepository extends JpaRepository<RegistrationDays, RegistrationDaysId> {
}
