package com.dental.clinic.management.clinical_records.domain;

import com.dental.clinic.management.patient.domain.Patient;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }
}
