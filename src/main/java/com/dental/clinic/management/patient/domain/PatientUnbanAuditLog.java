package com.dental.clinic.management.patient.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Audit log for patient unban actions.
 * Tracks who unbanned which patient and why (BR-086).
 */
@Entity
@Table(name = "patient_unban_audit_logs", indexes = {
        @Index(name = "idx_audit_patient", columnList = "patient_id"),
        @Index(name = "idx_audit_performed_by", columnList = "performed_by"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientUnbanAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient;

    @Column(name = "previous_no_show_count", nullable = false)
    private Integer previousNoShowCount;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy; // Username from security context

    @Column(name = "performed_by_role", nullable = false, length = 50)
    private String performedByRole; // RECEPTIONIST, MANAGER, ADMIN

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "PatientUnbanAuditLog{" +
                "auditId=" + auditId +
                ", patientId=" + patientId +
                ", performedBy='" + performedBy + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
