package com.dental.clinic.management.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseStatisticsResponse {
    private String month;
    private TransactionStats transactions;
    private InventoryStats inventory;
    private List<TopItem> topImports;
    private List<TopItem> topExports;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionStats {
        private Long total;
        private TransactionByType importData;
        private TransactionByType exportData;
        private TransactionByStatus byStatus;
        private List<DailyTransaction> byDay;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionByType {
        private Long count;
        private BigDecimal totalValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionByStatus {
        private Long pending;
        private Long approved;
        private Long rejected;
        private Long cancelled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTransaction {
        private LocalDate date;
        private Long count;
        private BigDecimal importValue;
        private BigDecimal exportValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryStats {
        private BigDecimal currentTotalValue;
        private Long lowStockItems;
        private Long expiringItems;
        private Double usageRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopItem {
        private Long itemId;
        private String itemName;
        private Long quantity;
        private BigDecimal value;
    }
}
