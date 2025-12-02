package com.dental.clinic.management.clinical_records.domain;

import com.dental.clinic.management.service.domain.DentalService;
import com.dental.clinic.management.treatment_plans.domain.PatientPlanItem;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "clinical_record_procedures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalRecordProcedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "procedure_id")
    private Integer procedureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinical_record_id", nullable = false)
    @JsonBackReference
    private ClinicalRecord clinicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private DentalService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_plan_item_id")
    private PatientPlanItem patientPlanItem;

    @Column(name = "tooth_number", length = 10)
    private String toothNumber;

    @Column(name = "procedure_description", nullable = false, columnDefinition = "TEXT")
    private String procedureDescription;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
