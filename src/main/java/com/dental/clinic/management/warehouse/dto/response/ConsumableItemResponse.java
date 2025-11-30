package com.dental.clinic.management.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * API 6.17: Consumable Item Response
 * Individual consumable item required for a service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableItemResponse {
    
    // Basic item info
    private Long itemMasterId;
    private String itemCode;
    private String itemName;
    
    // Requirement info
    private BigDecimal quantity;      // Required quantity per service
    private String unitName;
    
    // Stock info (CRITICAL - Most important for clinic operations)
    private Integer currentStock;     // Current stock in warehouse
    private String stockStatus;       // OK | LOW | OUT_OF_STOCK
    
    // Cost info (For financial planning)
    private BigDecimal unitPrice;     // Unit price (from latest import or market price)
    private BigDecimal totalCost;     // quantity × unitPrice
}
