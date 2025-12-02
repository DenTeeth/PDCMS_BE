package com.dental.clinic.management.clinical_records.repository;

import com.dental.clinic.management.clinical_records.domain.PatientToothStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Patient Tooth Status History
 * API 8.10
 *
 * @author Dental Clinic System
 * @since API 8.10
 */
@Repository
public interface PatientToothStatusHistoryRepository extends JpaRepository<PatientToothStatusHistory, Integer> {

    /**
     * Find all history records for a specific patient
     *
     * @param patientId Patient ID
     * @return List of history records ordered by changed_at DESC
     */
    List<PatientToothStatusHistory> findByPatient_PatientIdOrderByChangedAtDesc(Integer patientId);

    /**
     * Find history for a specific tooth
     *
     * @param patientId Patient ID
     * @param toothNumber Tooth number
     * @return List of history records for that tooth
     */
    List<PatientToothStatusHistory> findByPatient_PatientIdAndToothNumberOrderByChangedAtDesc(
            Integer patientId, String toothNumber);
}
