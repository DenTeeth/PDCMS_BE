package com.dental.clinic.management.clinical_records.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateClinicalRecordRequest {

    @Size(max = 2000, message = "Examination findings must not exceed 2000 characters")
    private String examinationFindings;

    @Size(max = 2000, message = "Treatment notes must not exceed 2000 characters")
    private String treatmentNotes;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate followUpDate;

    private Map<String, Object> vitalSigns;
}
