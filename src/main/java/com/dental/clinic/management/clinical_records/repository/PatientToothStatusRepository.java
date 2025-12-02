package com.dental.clinic.management.clinical_records.repository;

import com.dental.clinic.management.clinical_records.domain.PatientToothStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientToothStatusRepository extends JpaRepository<PatientToothStatus, Integer> {
}
