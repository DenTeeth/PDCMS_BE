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
public class VitalSignAssessment {
    private String vitalType;
    private BigDecimal value;
    private String unit;
    private String status;
    private BigDecimal normalMin;
    private BigDecimal normalMax;
    private String message;
}
