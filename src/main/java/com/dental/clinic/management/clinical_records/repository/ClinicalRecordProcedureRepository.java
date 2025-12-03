package com.dental.clinic.management.clinical_records.repository;

import com.dental.clinic.management.clinical_records.domain.ClinicalRecordProcedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicalRecordProcedureRepository extends JpaRepository<ClinicalRecordProcedure, Integer> {

    /**
     * Find all procedures for a clinical record
     * Ordered by created_at DESC (newest first)
     */
    @Query("SELECT p FROM ClinicalRecordProcedure p " +
            "LEFT JOIN FETCH p.service " +
            "WHERE p.clinicalRecord.clinicalRecordId = :recordId " +
            "ORDER BY p.createdAt DESC")
    List<ClinicalRecordProcedure> findByClinicalRecordIdWithService(@Param("recordId") Integer recordId);
}
