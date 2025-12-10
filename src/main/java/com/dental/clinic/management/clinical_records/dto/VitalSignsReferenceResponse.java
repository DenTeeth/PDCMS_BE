package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSignsReferenceResponse {
    private Integer referenceId;
    private String vitalType;
    private Integer ageMin;
    private Integer ageMax;
    private BigDecimal normalMin;
    private BigDecimal normalMax;
    private BigDecimal lowThreshold;
    private BigDecimal highThreshold;
    private String unit;
    private String description;
    private String effectiveDate;
    private Boolean isActive;
}
