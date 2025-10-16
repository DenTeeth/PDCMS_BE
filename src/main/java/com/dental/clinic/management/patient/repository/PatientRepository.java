package com.dental.clinic.management.patient.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.dental.clinic.management.patient.domain.Patient;

/**
 * Spring Data JPA repository for the {@link Patient} entity.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, String>, JpaSpecificationExecutor<Patient> {

  Optional<Patient> findOneByPatientCode(String patientCode);

  Optional<Patient> findOneByEmail(String email);

  Optional<Patient> findOneByPhone(String phone);

  Boolean existsByEmail(String email);

  Boolean existsByPhone(String phone);
}
