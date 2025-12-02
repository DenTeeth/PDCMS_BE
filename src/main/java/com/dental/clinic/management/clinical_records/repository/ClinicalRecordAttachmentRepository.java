package com.dental.clinic.management.clinical_records.repository;

import com.dental.clinic.management.clinical_records.domain.ClinicalRecordAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicalRecordAttachmentRepository extends JpaRepository<ClinicalRecordAttachment, Integer> {

    /**
     * Find all attachments for a specific clinical record
     * Ordered by upload time (newest first)
     */
    @Query("SELECT a FROM ClinicalRecordAttachment a " +
           "WHERE a.clinicalRecord.clinicalRecordId = :recordId " +
           "ORDER BY a.uploadedAt DESC")
    List<ClinicalRecordAttachment> findByClinicalRecord_ClinicalRecordId(@Param("recordId") Integer recordId);
}
