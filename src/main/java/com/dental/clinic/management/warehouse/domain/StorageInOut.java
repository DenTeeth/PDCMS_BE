package com.dental.clinic.management.warehouse.domain;

import com.dental.clinic.management.warehouse.enums.TransactionType;
import com.dental.clinic.management.warehouse.enums.WarehouseType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing warehouse import/export transactions.
 */
@Entity
@Table(name = "storage_in_out")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorageInOut {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse_type", nullable = false, length = 20)
    @NotNull(message = "Loại kho không được để trống")
    private WarehouseType warehouseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    @NotNull(message = "Loại giao dịch không được để trống")
    private TransactionType transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    @NotNull(message = "Vật tư không được để trống")
    private Inventory inventory;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn giá phải lớn hơn 0")
    private BigDecimal unitPrice;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Tổng tiền không được để trống")
    private BigDecimal totalAmount;

    @Column(name = "transaction_date", nullable = false)
    @NotNull(message = "Ngày giao dịch không được để trống")
    private LocalDateTime transactionDate;

    @Column(name = "performed_by", length = 50, nullable = false)
    @NotNull(message = "Người thực hiện không được để trống")
    private String performedBy;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isApproved == null) {
            isApproved = false;
        }
        if (totalAmount == null && quantity != null && unitPrice != null) {
            totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
