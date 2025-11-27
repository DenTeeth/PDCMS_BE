package com.dental.clinic.management.warehouse.dto.response;

import com.dental.clinic.management.warehouse.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long transactionId;
    private String transactionCode;
    private TransactionType transactionType;
    private LocalDateTime transactionDate;
    private String supplierName;
    private String notes;
    private String createdByName;
    private LocalDateTime createdAt;

    private List<TransactionItemResponse> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionItemResponse {
        private Long transactionItemId;

        //  Item identification - Warehouse staff needs these
        private String itemCode; // VD: "DP002"
        private String itemName; // VD: "Amoxicillin 500mg"
        private String unitName; // VD: "Vỉ" (if unit-based transaction)

        private String lotNumber;
        private Integer quantityChange;
        private LocalDate expiryDate; // Expiry date from batch - critical for FEFO and cold storage
        private String notes;
    }
}
