package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClinicalRecordResponse {

    private Integer clinicalRecordId;
    private Integer appointmentId;
    private String createdAt;
}
