package com.dental.clinic.management.clinical_records.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
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
public class CreateClinicalRecordRequest {

    @NotNull(message = "Appointment ID is required")
    private Integer appointmentId;

    @NotNull(message = "Chief complaint is required")
    @Size(min = 1, max = 1000, message = "Chief complaint must be between 1 and 1000 characters")
    private String chiefComplaint;

    @NotNull(message = "Examination findings is required")
    @Size(min = 1, max = 2000, message = "Examination findings must be between 1 and 2000 characters")
    private String examinationFindings;

    @NotNull(message = "Diagnosis is required")
    @Size(min = 1, max = 500, message = "Diagnosis must be between 1 and 500 characters")
    private String diagnosis;

    @Size(max = 2000, message = "Treatment notes must not exceed 2000 characters")
    private String treatmentNotes;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate followUpDate;

    private Map<String, Object> vitalSigns;
}
