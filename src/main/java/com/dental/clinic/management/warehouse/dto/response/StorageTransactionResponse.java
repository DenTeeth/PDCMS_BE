package com.dental.clinic.management.warehouse.dto.response;

import com.dental.clinic.management.warehouse.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Storage Transaction.
 */
public class StorageTransactionResponse {

    private Long id;
    private Long batchId;
    private String itemName;
    private String lotNumber;
    private TransactionType transactionType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    private LocalDateTime transactionDate;
    private Long performedBy;
    private String performedByName;
    private String notes;
    private LocalDateTime createdAt;

    public StorageTransactionResponse() {
    }

    public StorageTransactionResponse(Long id, Long batchId, String itemName, String lotNumber,
            TransactionType transactionType, Integer quantity, BigDecimal unitPrice,
            BigDecimal totalValue, LocalDateTime transactionDate, Long performedBy,
            String performedByName, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.batchId = batchId;
        this.itemName = itemName;
        this.lotNumber = lotNumber;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalValue = totalValue;
        this.transactionDate = transactionDate;
        this.performedBy = performedBy;
        this.performedByName = performedByName;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
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

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Long performedBy) {
        this.performedBy = performedBy;
    }

    public String getPerformedByName() {
        return performedByName;
    }

    public void setPerformedByName(String performedByName) {
        this.performedByName = performedByName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
