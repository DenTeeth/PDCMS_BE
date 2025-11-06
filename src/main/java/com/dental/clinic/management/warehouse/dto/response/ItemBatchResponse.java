package com.dental.clinic.management.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Response DTO for Item Batch with expiry and stock information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}

