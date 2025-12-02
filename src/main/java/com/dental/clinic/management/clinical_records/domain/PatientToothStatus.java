package com.dental.clinic.management.clinical_records.domain;

import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.domain.ToothConditionEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for Patient Tooth Status (Odontogram)
 * API 8.9 and 8.10
 *
 * @author Dental Clinic System
 * @since API 8.9
 */
@Entity
@Table(name = "patient_tooth_status", uniqueConstraints = @UniqueConstraint(columnNames = { "patient_id",
        "tooth_number" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientToothStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tooth_status_id")
    private Integer toothStatusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "tooth_number", nullable = false, length = 10)
    private String toothNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ToothConditionEnum status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
