package com.dental.clinic.management.clinical_records.repository;

import com.dental.clinic.management.clinical_records.domain.ClinicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord, Integer> {

    Optional<ClinicalRecord> findByAppointment_AppointmentId(Integer appointmentId);
}
