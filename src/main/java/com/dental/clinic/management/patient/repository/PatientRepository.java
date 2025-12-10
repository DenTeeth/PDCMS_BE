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

  /**
   * Find patient by account ID (for RBAC checks in API 5.5).
   * Used when determining current patient from JWT account_id claim.
   *
   * @param accountId Account ID from JWT
   * @return Optional Patient entity
   */
  @Query("SELECT p FROM Patient p WHERE p.account.accountId = :accountId")
  Optional<Patient> findOneByAccountAccountId(@Param("accountId") Integer accountId);

  /**
   * Find patient by account username (for RBAC checks).
   * Used when determining current patient from JWT username.
   *
   * @param username Account username from JWT
   * @return Optional Patient entity
   */
  @Query("SELECT p FROM Patient p WHERE p.account.username = :username")
  Optional<Patient> findByAccount_Username(@Param("username") String username);

  Optional<Patient> findOneByEmail(String email);

  Optional<Patient> findOneByPhone(String phone);

  Boolean existsByEmail(String email);

  Boolean existsByPhone(String phone);

  /**
   * BR-043: Find patients by name and date of birth (for duplicate detection).
   * Case-insensitive name matching.
   *
   * @param firstName First name (case-insensitive)
   * @param lastName Last name (case-insensitive)
   * @param dateOfBirth Date of birth
   * @return List of matching patients
   */
  @Query("SELECT p FROM Patient p WHERE LOWER(p.firstName) = LOWER(:firstName) AND LOWER(p.lastName) = LOWER(:lastName) AND p.dateOfBirth = :dateOfBirth AND p.isActive = true")
  java.util.List<Patient> findByNameAndDateOfBirth(
      @Param("firstName") String firstName,
      @Param("lastName") String lastName,
      @Param("dateOfBirth") java.time.LocalDate dateOfBirth);

  /**
   * BR-043: Find patients by phone number (for duplicate detection).
   *
   * @param phone Phone number
   * @return List of matching patients
   */
  @Query("SELECT p FROM Patient p WHERE p.phone = :phone AND p.isActive = true")
  java.util.List<Patient> findByPhoneNumber(@Param("phone") String phone);

  /**
   * BR-044: Find all blacklisted patients.
   *
   * @return List of blacklisted patients
   */
  @Query("SELECT p FROM Patient p WHERE p.isBlacklisted = true ORDER BY p.blacklistedAt DESC")
  java.util.List<Patient> findAllBlacklisted();
}
