package com.dental.clinic.management.warehouse.dto.response;

import com.dental.clinic.management.warehouse.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Storage Transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageTransactionResponse {

    private UUID id;
    private UUID batchId;
    private String itemName;
    private String lotNumber;
    private TransactionType transactionType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    private LocalDateTime transactionDate;
    private UUID performedBy;
    private String performedByName;
    private String notes;
    private LocalDateTime createdAt;
}
