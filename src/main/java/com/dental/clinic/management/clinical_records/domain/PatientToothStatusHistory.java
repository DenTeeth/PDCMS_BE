package com.dental.clinic.management.clinical_records.domain;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.domain.ToothConditionEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for tracking Patient Tooth Status History (Audit Trail)
 * API 8.10 - Automatic history tracking on status changes
 *
 * @author Dental Clinic System
 * @since API 8.10
 */
@Entity
@Table(name = "patient_tooth_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientToothStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "tooth_number", nullable = false, length = 10)
    private String toothNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private ToothConditionEnum oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private ToothConditionEnum newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private Employee changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
