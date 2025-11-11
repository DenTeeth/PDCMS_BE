package com.dental.clinic.management.patient.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.dental.clinic.management.patient.domain.Patient;

/**
 * Spring Data JPA repository for the {@link Patient} entity.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer>, JpaSpecificationExecutor<Patient> {

  Optional<Patient> findOneByPatientCode(String patientCode);

  /**
   * Find patient by code with Account eagerly fetched (for RBAC checks).
   *
   * @param patientCode Patient business key
   * @return Patient with account relationship loaded
   */
  @Query("SELECT p FROM Patient p LEFT JOIN FETCH p.account WHERE p.patientCode = :patientCode")
  Optional<Patient> findOneByPatientCodeWithAccount(@Param("patientCode") String patientCode);

  Optional<Patient> findOneByEmail(String email);

  Optional<Patient> findOneByPhone(String phone);

  Boolean existsByEmail(String email);

  Boolean existsByPhone(String phone);
}
