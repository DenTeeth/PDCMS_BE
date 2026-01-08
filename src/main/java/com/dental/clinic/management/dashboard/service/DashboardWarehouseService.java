package com.dental.clinic.management.dashboard.service;

import com.dental.clinic.management.dashboard.dto.WarehouseStatisticsResponse;
import com.dental.clinic.management.dashboard.util.DateRangeUtil;
import com.dental.clinic.management.warehouse.repository.StorageTransactionRepository;
import com.dental.clinic.management.warehouse.repository.ItemBatchRepository;
import com.dental.clinic.management.warehouse.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardWarehouseService {

    private final StorageTransactionRepository storageTransactionRepository;
    private final ItemBatchRepository itemBatchRepository;

    public WarehouseStatisticsResponse getWarehouseStatistics(String month, LocalDate start, LocalDate end) {
        DateRangeUtil.DateRange dateRange = DateRangeUtil.parseDateRange(month, start, end);
        LocalDateTime startDate = dateRange.getStartDate();
        LocalDateTime endDate = dateRange.getEndDate();

        return WarehouseStatisticsResponse.builder()
                .month(dateRange.getLabel())
                .transactions(getTransactionStats(startDate, endDate))
                .inventory(getInventoryStats())
                .topImports(getTopImports(startDate, endDate))
                .topExports(getTopExports(startDate, endDate))
                .build();
    }

    private WarehouseStatisticsResponse.TransactionStats getTransactionStats(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        // Count total transactions with null safety
        Long importCount = storageTransactionRepository.countByTypeInRange(
                startDate, endDate, TransactionType.IMPORT);
        importCount = importCount != null ? importCount : 0L;
        
        Long exportCount = storageTransactionRepository.countByTypeInRange(
                startDate, endDate, TransactionType.EXPORT);
        exportCount = exportCount != null ? exportCount : 0L;
        
        Long total = importCount + exportCount;
        
        // Calculate values with null safety
        BigDecimal importValue = storageTransactionRepository.calculateTotalValueByType(
                startDate, endDate, TransactionType.IMPORT);
        importValue = importValue != null ? importValue : BigDecimal.ZERO;
        
        BigDecimal exportValue = storageTransactionRepository.calculateTotalValueByType(
                startDate, endDate, TransactionType.EXPORT);
        exportValue = exportValue != null ? exportValue : BigDecimal.ZERO;
        
        // By status with null safety
        Long pending = storageTransactionRepository.countByStatusInRange(startDate, endDate, "PENDING_APPROVAL");
        pending = pending != null ? pending : 0L;
        
        Long approved = storageTransactionRepository.countByStatusInRange(startDate, endDate, "APPROVED");
        approved = approved != null ? approved : 0L;
        
        Long rejected = storageTransactionRepository.countByStatusInRange(startDate, endDate, "REJECTED");
        rejected = rejected != null ? rejected : 0L;
        
        Long cancelled = storageTransactionRepository.countByStatusInRange(startDate, endDate, "CANCELLED");
        cancelled = cancelled != null ? cancelled : 0L;
        
        // By day
        List<Object[]> transactionsByDayRaw = storageTransactionRepository.getTransactionsByDay(startDate, endDate);
        List<WarehouseStatisticsResponse.DailyTransaction> transactionsByDay = transactionsByDayRaw.stream()
                .map(row -> WarehouseStatisticsResponse.DailyTransaction.builder()
                        .date(row[0] instanceof Date ? ((Date) row[0]).toLocalDate() : (LocalDate) row[0])
                        .count(((Number) row[1]).longValue())
                        .importValue((BigDecimal) row[2])
                        .exportValue((BigDecimal) row[3])
                        .build())
                .collect(Collectors.toList());
        
        return WarehouseStatisticsResponse.TransactionStats.builder()
                .total(total)
                .importData(WarehouseStatisticsResponse.TransactionByType.builder()
                        .count(importCount)
                        .totalValue(importValue)
                        .build())
                .exportData(WarehouseStatisticsResponse.TransactionByType.builder()
                        .count(exportCount)
                        .totalValue(exportValue)
                        .build())
                .byStatus(WarehouseStatisticsResponse.TransactionByStatus.builder()
                        .pending(pending)
                        .approved(approved)
                        .rejected(rejected)
                        .cancelled(cancelled)
                        .build())
                .byDay(transactionsByDay)
                .build();
    }

    private WarehouseStatisticsResponse.InventoryStats getInventoryStats() {
        // Current total inventory value with null safety
        BigDecimal currentTotalValue = itemBatchRepository.calculateTotalInventoryValue();
        currentTotalValue = currentTotalValue != null ? currentTotalValue : BigDecimal.ZERO;
        
        // Low stock items count with null safety
        Long lowStockItems = itemBatchRepository.countLowStockItems();
        lowStockItems = lowStockItems != null ? lowStockItems : 0L;
        
        // Expiring items (within 30 days) with null safety
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        Long expiringItems = itemBatchRepository.countExpiringItems(today, thirtyDaysLater);
        expiringItems = expiringItems != null ? expiringItems : 0L;
        
        // Usage rate calculation - simplified version
        // We can calculate this as: export value / import value over a period
        // For now, returning a placeholder
        Double usageRate = 0.0;
        
        return WarehouseStatisticsResponse.InventoryStats.builder()
                .currentTotalValue(currentTotalValue)
                .lowStockItems(lowStockItems)
                .expiringItems(expiringItems)
                .usageRate(usageRate)
                .build();
    }

    private List<WarehouseStatisticsResponse.TopItem> getTopImports(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        List<Object[]> topImportsRaw = storageTransactionRepository.getTopImportedItems(
                startDate, endDate, 10);
        
        return topImportsRaw.stream()
                .map(row -> WarehouseStatisticsResponse.TopItem.builder()
                        .itemId(((Number) row[0]).longValue())
                        .itemName((String) row[2])  // item_name is at index 2
                        .quantity(((Number) row[3]).longValue())
                        .value((BigDecimal) row[4])
                        .build())
                .collect(Collectors.toList());
    }

    private List<WarehouseStatisticsResponse.TopItem> getTopExports(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        List<Object[]> topExportsRaw = storageTransactionRepository.getTopExportedItems(
                startDate, endDate, 10);
        
        return topExportsRaw.stream()
                .map(row -> WarehouseStatisticsResponse.TopItem.builder()
                        .itemId(((Number) row[0]).longValue())
                        .itemName((String) row[2])  // item_name is at index 2
                        .quantity(((Number) row[3]).longValue())
                        .value((BigDecimal) row[4])
                        .build())
                .collect(Collectors.toList());
    }
}
