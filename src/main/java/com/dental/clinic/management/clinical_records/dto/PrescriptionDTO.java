package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDTO {
    private Integer prescriptionId;
    private Integer clinicalRecordId;
    private String prescriptionNotes;
    private String createdAt;
    private List<PrescriptionItemDTO> items;
}
