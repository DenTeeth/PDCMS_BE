package com.dental.clinic.management.clinical_records.repository;

import com.dental.clinic.management.clinical_records.domain.ClinicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord, Integer> {

    @Query("SELECT cr FROM ClinicalRecord cr " +
            "LEFT JOIN FETCH cr.appointment a " +
            "LEFT JOIN FETCH a.patient p " +
            "LEFT JOIN FETCH a.employee e " +
            "LEFT JOIN FETCH cr.procedures proc " +
            "LEFT JOIN FETCH proc.service " +
            "LEFT JOIN FETCH proc.patientPlanItem " +
            "LEFT JOIN FETCH cr.prescriptions presc " +
            "LEFT JOIN FETCH presc.items items " +
            "LEFT JOIN FETCH items.itemMaster " +
            "WHERE a.appointmentId = :appointmentId")
    Optional<ClinicalRecord> findByAppointmentIdWithDetails(@Param("appointmentId") Integer appointmentId);

    Optional<ClinicalRecord> findByAppointment_AppointmentId(Integer appointmentId);
}
