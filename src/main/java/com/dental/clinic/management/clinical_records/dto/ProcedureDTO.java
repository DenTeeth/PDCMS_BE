package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureDTO {
    private Integer procedureId;
    private String serviceCode;
    private String serviceName;
    private Integer patientPlanItemId;
    private String toothNumber;
    private String procedureDescription;
    private String notes;
    private String createdAt;
}
