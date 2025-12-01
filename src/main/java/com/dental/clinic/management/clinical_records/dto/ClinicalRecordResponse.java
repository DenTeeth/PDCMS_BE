package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalRecordResponse {
    private Integer clinicalRecordId;
    private String diagnosis;
    private Map<String, Object> vitalSigns;
    private String chiefComplaint;
    private String examinationFindings;
    private String treatmentNotes;
    private String createdAt;
    private String updatedAt;

    private AppointmentDTO appointment;
    private DoctorDTO doctor;
    private PatientDTO patient;
    private java.util.List<ProcedureDTO> procedures;
    private java.util.List<PrescriptionDTO> prescriptions;
}
