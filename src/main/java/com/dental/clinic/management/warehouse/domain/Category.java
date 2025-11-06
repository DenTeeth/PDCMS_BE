package com.dental.clinic.management.warehouse.domain;

import com.dental.clinic.management.warehouse.enums.WarehouseType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a product category with hierarchical structure.
 * Supports parent-child relationships (e.g., "ThuÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“c" -> "ThuÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“c khÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ng sinh").
 * Categories are specific to warehouse types (COLD or NORMAL).
 */
@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_name_warehouse", columnNames = { "category_name", "warehouse_type" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "category_name", length = 100, nullable = false)
    @NotNull(message = "TÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªn danh mÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â¥c khÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´ng ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â£c ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€ Ã¢â‚¬â„¢ trÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ng")
    private String categoryName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse_type", nullable = false, length = 20)
    @NotNull(message = "LoÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¡i kho khÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´ng ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â£c ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€ Ã¢â‚¬â„¢ trÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ng")
    private WarehouseType warehouseType;

    // === HIERARCHICAL STRUCTURE ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @JsonIgnore
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Category> subCategories = new ArrayList<>();

    // === RELATIONSHIP WITH ITEM MASTERS ===
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<com.dental.clinic.management.warehouse.domain.ItemMaster> itemMasters = new ArrayList<>();

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

    // === HELPER METHODS ===

    /**
     * Get ID (alias for categoryId for consistency with mappers).
     */
    public Long getId() {
        return categoryId;
    }

    public boolean isRootCategory() {
        return parentCategory == null;
    }

    public boolean hasSubCategories() {
        return subCategories != null && !subCategories.isEmpty();
    }

    public void addSubCategory(Category subCategory) {
        subCategories.add(subCategory);
        subCategory.setParentCategory(this);
    }

    public void removeSubCategory(Category subCategory) {
        subCategories.remove(subCategory);
        subCategory.setParentCategory(null);
    }
}
