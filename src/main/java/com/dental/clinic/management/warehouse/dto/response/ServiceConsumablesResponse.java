package com.dental.clinic.management.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * API 6.17: Service Consumables Response
 * Returns list of consumable items required for a service with stock and cost info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceConsumablesResponse {
    
    /**
     * Service basic info
     */
    private Long serviceId;
    private String serviceName;
    
    /**
     * Financial info - Total cost of all consumables for this service
     * Helps answer: "How much does this service cost in materials?"
     */
    private BigDecimal totalConsumableCost;
    
    /**
     * Availability flag - Any item out of stock or low?
     * True if ANY consumable has stockStatus = OUT_OF_STOCK or LOW
     * Use this to show warning: "Cannot perform service - insufficient materials"
     */
    private Boolean hasInsufficientStock;
    
    /**
     * List of consumable items required
     */
    private List<ConsumableItemResponse> consumables;
}
