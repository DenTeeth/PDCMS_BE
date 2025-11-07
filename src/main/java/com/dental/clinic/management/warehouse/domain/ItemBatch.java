package com.dental.clinic.management.warehouse.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing actual inventory batches (lÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´
 * hÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â ng thÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â±c
 * tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿).
 * This is where ACTUAL stock quantities are stored.
 * 
 * Business Rules:
 * - UNIQUE(item_master_id, lot_number) - One item can have multiple batches
 * with different lot numbers
 * - FEFO (First Expired, First Out) - Sort by expiry_date ASC when exporting
 * - import_price is used to calculate loss (thÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t
 * thoÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡t)
 * - expiry_date is NULLABLE (for non-perishable items like instruments)
 */
@Entity
@Table(name = "item_batches", uniqueConstraints = {
        @UniqueConstraint(name = "uk_item_lot", columnNames = { "item_master_id", "lot_number" })
}, indexes = {
        @Index(name = "idx_batch_expiry", columnList = "expiry_date"),
        @Index(name = "idx_batch_quantity", columnList = "quantity_on_hand")
})
public class ItemBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    // === RELATIONSHIP: ItemMaster ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_master_id", nullable = false)
    @NotNull(message = "Item master cannot be empty")
    @JsonIgnore
    private ItemMaster itemMaster;

    // === BATCH INFORMATION ===
    @Column(name = "lot_number", length = 50, nullable = false)
    @NotBlank(message = "Lot number cannot be empty")
    private String lotNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // === STOCK TRACKING ===
    @Column(name = "quantity_on_hand", nullable = false)
    @NotNull(message = "Quantity on hand cannot be empty")
    @PositiveOrZero(message = "Quantity on hand must be non-negative")
    private Integer quantityOnHand = 0;

    // === PRICING (for loss calculation) ===
    @Column(name = "import_price", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Import price cannot be empty")
    private BigDecimal importPrice;

    // === RELATIONSHIP: Transactions ===
    @OneToMany(mappedBy = "itemBatch", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
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

    // === CONSTRUCTORS ===

    public ItemBatch() {
    }

    public ItemBatch(Long batchId, ItemMaster itemMaster, String lotNumber, LocalDate expiryDate,
            Integer quantityOnHand, BigDecimal importPrice, List<StorageTransaction> transactions,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.batchId = batchId;
        this.itemMaster = itemMaster;
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
        this.quantityOnHand = quantityOnHand;
        this.importPrice = importPrice;
        this.transactions = transactions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // === GETTERS AND SETTERS ===

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public ItemMaster getItemMaster() {
        return itemMaster;
    }

    public void setItemMaster(ItemMaster itemMaster) {
        this.itemMaster = itemMaster;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public List<StorageTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<StorageTransaction> transactions) {
        this.transactions = transactions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // === HELPER METHODS ===

    /**
     * Get ID (alias for batchId for consistency with mappers).
     */
    public Long getId() {
        return batchId;
    }

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
