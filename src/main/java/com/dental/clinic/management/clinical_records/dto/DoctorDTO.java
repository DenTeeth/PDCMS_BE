package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDTO {
    private Integer employeeId;
    private String employeeCode;
    private String fullName;
    private String phone;
    private String email;
}
