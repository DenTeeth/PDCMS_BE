package com.dental.clinic.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dental.clinic.management.domain.Patient;

/**
 * Spring Data JPA repository for the {@link Patient} entity.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {

  Optional<Patient> findOneByEmail(String email);

  Optional<Patient> findOneByPhone(String phone);

  @Query("SELECT p FROM Patient p WHERE p.isActive = true")
  List<Patient> findAllActivePatients();

  Boolean existsByEmail(String email);

  Boolean existsByPhone(String phone);
}
