package com.dental.clinic.management.patient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientStatsResponse {
    private Long totalPatients;
    private Long activePatients;
    private Long inactivePatients;
}
