package com.dental.clinic.management.clinical_records.repository;

import com.dental.clinic.management.clinical_records.domain.PatientToothStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Patient Tooth Status
 * API 8.9 and 8.10
 *
 * @author Dental Clinic System
 * @since API 8.9
 */
@Repository
public interface PatientToothStatusRepository extends JpaRepository<PatientToothStatus, Integer> {

    /**
     * Find all tooth statuses for a patient
     *
     * @param patientId Patient ID
     * @return List of tooth statuses
     */
    List<PatientToothStatus> findByPatient_PatientId(Integer patientId);

    /**
     * Find specific tooth status for a patient
     *
     * @param patientId Patient ID
     * @param toothNumber Tooth number
     * @return Optional tooth status
     */
    Optional<PatientToothStatus> findByPatient_PatientIdAndToothNumber(Integer patientId, String toothNumber);
}
