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
public class RevenueExpensesResponse {
    private String month;
    private RevenueStats revenue;
    private ExpenseStats expenses;
    private ComparisonData comparison;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueStats {
        private BigDecimal total;
        private RevenueByType byType;
        private List<DailyAmount> byDay;
        private List<TopService> topServices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByType {
        private BigDecimal appointment;
        private BigDecimal treatmentPlan;
        private BigDecimal supplemental;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseStats {
        private BigDecimal total;
        private ExpenseByType byType;
        private List<DailyAmount> byDay;
        private List<TopItem> topItems;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseByType {
        private BigDecimal serviceConsumption;
        private BigDecimal damaged;
        private BigDecimal expired;
        private BigDecimal other;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAmount {
        private LocalDate date;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopService {
        private Long serviceId;
        private String serviceName;
        private BigDecimal revenue;
        private Long count;
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonData {
        private ComparisonItem revenue;
        private ComparisonItem expenses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonItem {
        private BigDecimal previous;
        private BigDecimal change;
        private Double changePercent;
    }
}
