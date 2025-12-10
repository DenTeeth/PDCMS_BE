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
    private Integer age;
    private String gender;
    private String address;

    // Medical information - critical for clinical records
    private String medicalHistory;
    private String allergies;

    // Emergency contact
    private String emergencyContactName;
    private String emergencyContactPhone;

    // Guardian information (for minors <16 years old)
    private String guardianName;
    private String guardianPhone;
    private String guardianRelationship;
    private String guardianCitizenId;
}
