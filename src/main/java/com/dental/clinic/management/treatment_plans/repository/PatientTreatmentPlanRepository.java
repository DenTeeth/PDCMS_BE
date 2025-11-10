package com.dental.clinic.management.treatment_plans.repository;

import com.dental.clinic.management.treatment_plans.domain.PatientTreatmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PatientTreatmentPlan entity.
 * Handles database operations for treatment plans.
 */
@Repository
public interface PatientTreatmentPlanRepository extends JpaRepository<PatientTreatmentPlan, Long> {

    /**
     * Find all treatment plans for a specific patient.
     * Uses JOIN FETCH to avoid N+1 problem when loading doctor info.
     *
     * @param patientId ID of the patient
     * @return List of treatment plans with doctor info eagerly loaded
     */
    @Query("SELECT DISTINCT p FROM PatientTreatmentPlan p " +
            "LEFT JOIN FETCH p.createdBy " +
            "WHERE p.patient.patientId = :patientId " +
            "ORDER BY p.createdAt DESC")
    List<PatientTreatmentPlan> findByPatientIdWithDoctor(@Param("patientId") Integer patientId);

    /**
     * Find treatment plan by plan code.
     *
     * @param planCode Unique plan code
     * @return Optional containing the treatment plan if found
     */
    Optional<PatientTreatmentPlan> findByPlanCode(String planCode);

    /**
     * Check if plan code already exists.
     *
     * @param planCode Plan code to check
     * @return true if exists, false otherwise
     */
    boolean existsByPlanCode(String planCode);
}
