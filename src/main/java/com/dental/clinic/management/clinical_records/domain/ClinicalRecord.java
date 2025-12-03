package com.dental.clinic.management.clinical_records.domain;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "clinical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clinical_record_id")
    private Integer clinicalRecordId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vital_signs", columnDefinition = "jsonb")
    private Map<String, Object> vitalSigns;

    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(name = "examination_findings", columnDefinition = "TEXT")
    private String examinationFindings;

    @Column(name = "treatment_notes", columnDefinition = "TEXT")
    private String treatmentNotes;

    @Column(name = "follow_up_date")
    private java.time.LocalDate followUpDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "clinicalRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<ClinicalRecordProcedure> procedures = new ArrayList<>();

    @OneToMany(mappedBy = "clinicalRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<ClinicalPrescription> prescriptions = new ArrayList<>();

    @OneToMany(mappedBy = "clinicalRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<ClinicalRecordAttachment> attachments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
