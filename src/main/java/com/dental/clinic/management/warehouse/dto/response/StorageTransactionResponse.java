package com.dental.clinic.management.warehouse.dto.response;

import com.dental.clinic.management.warehouse.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * Response DTO for Storage Transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}

