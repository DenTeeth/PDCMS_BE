package com.dental.clinic.management.warehouse.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an item master (danh mÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â¥c
 * vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°).
 * This is the "template" for items, actual stock is tracked in ItemBatch.
 * 
 * Business Rules:
 * - Each item belongs to ONE category
 * - Each item can have MULTIPLE compatible suppliers (N-N relationship)
 * - Actual stock is NOT stored here (stored in item_batches)
 */
@Entity
@Table(name = "item_master", uniqueConstraints = {
        @UniqueConstraint(name = "uk_item_name", columnNames = "item_name")
}, indexes = {
        @Index(name = "idx_item_category", columnList = "category_id"),
        @Index(name = "idx_item_name", columnList = "item_name")
})
public class ItemMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_master_id", nullable = false)
    private Long itemMasterId;

    @Column(name = "item_name", length = 100, nullable = false)
    @NotBlank(message = "TÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªn vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â° khÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´ng ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â£c ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€ Ã¢â‚¬â„¢ trÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ng")
    private String itemName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_stock_level")
    private Integer minStockLevel;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    // === RELATIONSHIP: Category ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Danh mÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â¥c khÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´ng ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â£c ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€ Ã¢â‚¬â„¢ trÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ng")
    @JsonIgnore
    private Category category;

    // === RELATIONSHIP: Suppliers (N-N) ===
    @ManyToMany
    @JoinTable(name = "item_suppliers", joinColumns = @JoinColumn(name = "item_master_id"), inverseJoinColumns = @JoinColumn(name = "supplier_id"))
    @JsonIgnore
    private List<Supplier> compatibleSuppliers = new ArrayList<>();

    // === RELATIONSHIP: Batches (One Item Master -> Many Batches) ===
    @OneToMany(mappedBy = "itemMaster", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ItemBatch> batches = new ArrayList<>();

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === CONSTRUCTORS ===

    public ItemMaster() {
    }

    public ItemMaster(Long itemMasterId, String itemName, String description, Integer minStockLevel,
            Integer maxStockLevel, Category category, List<Supplier> compatibleSuppliers,
            List<ItemBatch> batches, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.itemMasterId = itemMasterId;
        this.itemName = itemName;
        this.description = description;
        this.minStockLevel = minStockLevel;
        this.maxStockLevel = maxStockLevel;
        this.category = category;
        this.compatibleSuppliers = compatibleSuppliers;
        this.batches = batches;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // === GETTERS AND SETTERS ===

    public Long getItemMasterId() {
        return itemMasterId;
    }

    public void setItemMasterId(Long itemMasterId) {
        this.itemMasterId = itemMasterId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public Integer getMaxStockLevel() {
        return maxStockLevel;
    }

    public void setMaxStockLevel(Integer maxStockLevel) {
        this.maxStockLevel = maxStockLevel;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Supplier> getCompatibleSuppliers() {
        return compatibleSuppliers;
    }

    public void setCompatibleSuppliers(List<Supplier> compatibleSuppliers) {
        this.compatibleSuppliers = compatibleSuppliers;
    }

    public List<ItemBatch> getBatches() {
        return batches;
    }

    public void setBatches(List<ItemBatch> batches) {
        this.batches = batches;
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
     * Get ID (alias for itemMasterId for consistency with mappers).
     */
    public Long getId() {
        return itemMasterId;
    }

    public void addSupplier(Supplier supplier) {
        compatibleSuppliers.add(supplier);
        supplier.getCompatibleItems().add(this);
    }

    public void removeSupplier(Supplier supplier) {
        compatibleSuppliers.remove(supplier);
        supplier.getCompatibleItems().remove(this);
    }

    public void addBatch(ItemBatch batch) {
        batches.add(batch);
        batch.setItemMaster(this);
    }

    public void removeBatch(ItemBatch batch) {
        batches.remove(batch);
        batch.setItemMaster(null);
    }
}
