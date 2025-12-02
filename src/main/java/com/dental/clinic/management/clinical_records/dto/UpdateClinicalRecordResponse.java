package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateClinicalRecordResponse {

    private Integer clinicalRecordId;
    private String updatedAt;
    private String examinationFindings;
    private String treatmentNotes;
    private String followUpDate;
}
