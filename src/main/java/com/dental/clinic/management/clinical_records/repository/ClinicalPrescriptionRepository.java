package com.dental.clinic.management.clinical_records.repository;

import com.dental.clinic.management.clinical_records.domain.ClinicalPrescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicalPrescriptionRepository extends JpaRepository<ClinicalPrescription, Integer> {
    Optional<ClinicalPrescription> findByClinicalRecord_ClinicalRecordId(Integer clinicalRecordId);
}
