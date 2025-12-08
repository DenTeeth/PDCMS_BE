package com.dental.clinic.management.patient.repository;

import com.dental.clinic.management.patient.domain.PatientImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientImageRepository
        extends JpaRepository<PatientImage, Long>, JpaSpecificationExecutor<PatientImage> {

    List<PatientImage> findByClinicalRecordClinicalRecordIdOrderByCreatedAtDesc(Integer clinicalRecordId);
}
