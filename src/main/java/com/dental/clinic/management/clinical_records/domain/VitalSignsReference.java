package com.dental.clinic.management.clinical_records.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vital_signs_reference")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSignsReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "vital_type", nullable = false, length = 50)
    private String vitalType;

    @Column(name = "age_min", nullable = false)
    private Integer ageMin;

    @Column(name = "age_max")
    private Integer ageMax;

    @Column(name = "normal_min", precision = 10, scale = 2)
    private BigDecimal normalMin;

    @Column(name = "normal_max", precision = 10, scale = 2)
    private BigDecimal normalMax;

    @Column(name = "low_threshold", precision = 10, scale = 2)
    private BigDecimal lowThreshold;

    @Column(name = "high_threshold", precision = 10, scale = 2)
    private BigDecimal highThreshold;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.effectiveDate == null) {
            this.effectiveDate = LocalDate.now();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
