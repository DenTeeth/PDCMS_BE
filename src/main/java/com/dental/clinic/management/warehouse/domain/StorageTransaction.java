package com.dental.clinic.management.warehouse.domain;

import com.dental.clinic.management.warehouse.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing storage transactions (sổ cái kho).
 * Records all warehouse movements: IMPORT, EXPORT, ADJUST, DESTROY.
 * 
 * Business Rules:
 * - Every stock movement MUST create a transaction record (audit trail)
 * - transaction_type determines the operation type
 * - performed_by tracks who performed the action (username or employee_id)
 * - notes is REQUIRED for ADJUST and DESTROY types
 */
@Entity
@Table(name = "storage_transactions", indexes = {
        @Index(name = "idx_transaction_batch", columnList = "batch_id"),
        @Index(name = "idx_transaction_type", columnList = "transaction_type"),
        @Index(name = "idx_transaction_date", columnList = "transaction_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    // === RELATIONSHIP: ItemBatch ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @NotNull(message = "Lô hàng không được để trống")
    @JsonIgnore
    private ItemBatch itemBatch;

    // === TRANSACTION INFO ===
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    @NotNull(message = "Loại giao dịch không được để trống")
    private TransactionType transactionType;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @Column(name = "transaction_date", nullable = false)
    @NotNull(message = "Ngày giao dịch không được để trống")
    private LocalDateTime transactionDate;

    // === TRACKING ===
    @Column(name = "performed_by", length = 50, nullable = false)
    @NotNull(message = "Người thực hiện không được để trống")
    private String performedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // === PRICING (for loss calculation) ===
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_value", precision = 12, scale = 2)
    private BigDecimal totalValue;

    // === AUDIT FIELDS ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
        // Auto-calculate total value
        if (unitPrice != null && quantity != null) {
            totalValue = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // === HELPER METHODS ===
    public boolean isImport() {
        return transactionType == TransactionType.IMPORT;
    }

    public boolean isExport() {
        return transactionType == TransactionType.EXPORT;
    }

    public boolean isAdjustment() {
        return transactionType == TransactionType.ADJUSTMENT;
    }

    public boolean isDestroy() {
        return transactionType == TransactionType.DESTROY;
    }

    public boolean requiresNotes() {
        return transactionType == TransactionType.ADJUSTMENT || transactionType == TransactionType.DESTROY;
    }
}
