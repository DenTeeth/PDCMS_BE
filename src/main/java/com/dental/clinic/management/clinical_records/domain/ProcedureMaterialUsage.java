package com.dental.clinic.management.clinical_records.domain;

import com.dental.clinic.management.warehouse.domain.ItemMaster;
import com.dental.clinic.management.warehouse.domain.ItemUnit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks actual material quantities used per procedure
 * Enables variance analysis (planned vs actual) and material consumption reporting
 * 
 * Links: ClinicalRecordProcedure → Service → ServiceConsumables (BOM) → This table (actual usage)
 */
@Entity
@Table(name = "procedure_material_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureMaterialUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private ClinicalRecordProcedure procedure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_master_id", nullable = false)
    private ItemMaster itemMaster;

    /**
     * Expected quantity from service BOM (service_consumables)
     */
    @Column(name = "planned_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal plannedQuantity;

    /**
     * Actual quantity used during procedure (can be adjusted by assistant)
     */
    @Column(name = "actual_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private ItemUnit unit;

    /**
     * Computed field: actual - planned
     * Positive = overuse (used more than planned)
     * Negative = underuse (used less than planned)
     * Generated column in DB, read-only in Java
     */
    @Column(name = "variance_quantity", insertable = false, updatable = false)
    private BigDecimal varianceQuantity;

    /**
     * Explanation for variance if actual differs from planned
     */
    @Column(name = "variance_reason", length = 500)
    private String varianceReason;

    /**
     * Timestamp when usage was recorded/updated
     */
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    /**
     * Employee username who recorded/updated the usage
     */
    @Column(name = "recorded_by", length = 100)
    private String recordedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        recordedAt = LocalDateTime.now();
    }
}
