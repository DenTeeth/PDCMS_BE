package com.dental.clinic.management.warehouse.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing actual inventory batches (lô hàng thực tế).
 * This is where ACTUAL stock quantities are stored.
 * 
 * Business Rules:
 * - UNIQUE(item_master_id, lot_number) - One item can have multiple batches
 * with different lot numbers
 * - FEFO (First Expired, First Out) - Sort by expiry_date ASC when exporting
 * - import_price is used to calculate loss (thất thoát)
 * - expiry_date is NULLABLE (for non-perishable items like instruments)
 */
@Entity
@Table(name = "item_batches", uniqueConstraints = {
        @UniqueConstraint(name = "uk_item_lot", columnNames = { "item_master_id", "lot_number" })
}, indexes = {
        @Index(name = "idx_batch_expiry", columnList = "expiry_date"),
        @Index(name = "idx_batch_quantity", columnList = "quantity_on_hand")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    // === RELATIONSHIP: ItemMaster ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_master_id", nullable = false)
    @NotNull(message = "Vật tư không được để trống")
    @JsonIgnore
    private ItemMaster itemMaster;

    // === BATCH INFORMATION ===
    @Column(name = "lot_number", length = 50, nullable = false)
    @NotBlank(message = "Số lô không được để trống")
    private String lotNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // === STOCK TRACKING ===
    @Column(name = "quantity_on_hand", nullable = false)
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @PositiveOrZero(message = "Số lượng tồn kho không được âm")
    @Builder.Default
    private Integer quantityOnHand = 0;

    // === PRICING (for loss calculation) ===
    @Column(name = "import_price", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Giá nhập không được để trống")
    private BigDecimal importPrice;

    // === RELATIONSHIP: Transactions ===
    @OneToMany(mappedBy = "itemBatch", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<StorageTransaction> transactions = new ArrayList<>();

    // === AUDIT FIELDS ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (quantityOnHand == null) {
            quantityOnHand = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === HELPER METHODS ===
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon(int days) {
        if (expiryDate == null)
            return false;
        LocalDate threshold = LocalDate.now().plusDays(days);
        return expiryDate.isBefore(threshold) || expiryDate.isEqual(threshold);
    }

    public boolean hasStock() {
        return quantityOnHand != null && quantityOnHand > 0;
    }

    public void addStock(Integer quantity) {
        this.quantityOnHand = (this.quantityOnHand == null ? 0 : this.quantityOnHand) + quantity;
    }

    public void deductStock(Integer quantity) {
        if (this.quantityOnHand == null || this.quantityOnHand < quantity) {
            throw new IllegalStateException(
                    String.format("Insufficient stock. Available: %d, Requested: %d",
                            this.quantityOnHand == null ? 0 : this.quantityOnHand, quantity));
        }
        this.quantityOnHand -= quantity;
    }
}
