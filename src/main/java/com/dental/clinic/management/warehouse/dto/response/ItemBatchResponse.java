package com.dental.clinic.management.warehouse.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Item Batch with expiry and stock information.
 */
public class ItemBatchResponse {

    private Long id;
    private Long itemMasterId;
    private String itemName;
    private String lotNumber;
    private LocalDate expiryDate;
    private Integer quantityOnHand;
    private BigDecimal importPrice;
    private LocalDateTime createdAt;

    // Calculated fields
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private Integer daysUntilExpiry;
    private BigDecimal totalValue; // quantityOnHand * importPrice

    public ItemBatchResponse() {
    }

    public ItemBatchResponse(Long id, Long itemMasterId, String itemName, String lotNumber,
            LocalDate expiryDate, Integer quantityOnHand, BigDecimal importPrice,
            LocalDateTime createdAt, Boolean isExpired, Boolean isExpiringSoon,
            Integer daysUntilExpiry, BigDecimal totalValue) {
        this.id = id;
        this.itemMasterId = itemMasterId;
        this.itemName = itemName;
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
        this.quantityOnHand = quantityOnHand;
        this.importPrice = importPrice;
        this.createdAt = createdAt;
        this.isExpired = isExpired;
        this.isExpiringSoon = isExpiringSoon;
        this.daysUntilExpiry = daysUntilExpiry;
        this.totalValue = totalValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public Boolean getIsExpiringSoon() {
        return isExpiringSoon;
    }

    public void setIsExpiringSoon(Boolean isExpiringSoon) {
        this.isExpiringSoon = isExpiringSoon;
    }

    public Integer getDaysUntilExpiry() {
        return daysUntilExpiry;
    }

    public void setDaysUntilExpiry(Integer daysUntilExpiry) {
        this.daysUntilExpiry = daysUntilExpiry;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
}
