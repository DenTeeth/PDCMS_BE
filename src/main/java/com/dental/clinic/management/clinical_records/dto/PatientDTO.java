package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDTO {
    private Integer patientId;
    private String patientCode;
    private String fullName;
    private String phone;
    private String email;
    private String dateOfBirth;
    private String gender;
}
