package com.dental.clinic.management.dashboard.service;

import com.dental.clinic.management.dashboard.dto.RevenueExpensesResponse;
import com.dental.clinic.management.payment.enums.InvoiceType;
import com.dental.clinic.management.payment.repository.InvoiceRepository;
import com.dental.clinic.management.warehouse.repository.StorageTransactionRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardRevenueService {

    private final InvoiceRepository invoiceRepository;
    private final StorageTransactionRepository storageTransactionRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;

    public RevenueExpensesResponse getRevenueExpensesStatistics(String month, Boolean compareWithPrevious) {
        YearMonth currentMonth = YearMonth.parse(month);
        LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        // Get revenue stats
        RevenueExpensesResponse.RevenueStats revenueStats = buildRevenueStats(startDate, endDate);
        
        // Get expense stats
        RevenueExpensesResponse.ExpenseStats expenseStats = buildExpenseStats(startDate, endDate);
        
        RevenueExpensesResponse.RevenueExpensesResponseBuilder builder = RevenueExpensesResponse.builder()
                .month(month)
                .revenue(revenueStats)
                .expenses(expenseStats);
        
        // Add comparison if requested
        if (Boolean.TRUE.equals(compareWithPrevious)) {
            YearMonth previousMonth = currentMonth.minusMonths(1);
            LocalDateTime prevStartDate = previousMonth.atDay(1).atStartOfDay();
            LocalDateTime prevEndDate = previousMonth.atEndOfMonth().atTime(23, 59, 59);
            
            builder.comparison(buildComparison(revenueStats, expenseStats, prevStartDate, prevEndDate));
        }
        
        return builder.build();
    }

    private RevenueExpensesResponse.RevenueStats buildRevenueStats(LocalDateTime startDate, LocalDateTime endDate) {
        // Calculate total revenue with null safety
        BigDecimal totalRevenue = invoiceRepository.calculateTotalRevenue(startDate, endDate);
        totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        
        // Calculate revenue by type with null safety
        BigDecimal appointmentRevenue = invoiceRepository.calculateRevenueByType(startDate, endDate, InvoiceType.APPOINTMENT);
        appointmentRevenue = appointmentRevenue != null ? appointmentRevenue : BigDecimal.ZERO;
        
        BigDecimal treatmentPlanRevenue = invoiceRepository.calculateRevenueByType(startDate, endDate, InvoiceType.TREATMENT_PLAN);
        treatmentPlanRevenue = treatmentPlanRevenue != null ? treatmentPlanRevenue : BigDecimal.ZERO;
        
        BigDecimal supplementalRevenue = invoiceRepository.calculateRevenueByType(startDate, endDate, InvoiceType.SUPPLEMENTAL);
        supplementalRevenue = supplementalRevenue != null ? supplementalRevenue : BigDecimal.ZERO;
        
        // Get revenue by day
        List<Object[]> revenueByDayRaw = invoiceRepository.getRevenueByDay(startDate, endDate);
        List<RevenueExpensesResponse.DailyAmount> revenueByDay = revenueByDayRaw.stream()
                .map(row -> RevenueExpensesResponse.DailyAmount.builder()
                        .date(row[0] instanceof Date ? ((Date) row[0]).toLocalDate() : (LocalDate) row[0])
                        .amount((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());
        
        // Top services by revenue
        List<Object[]> topServicesRaw = appointmentServiceRepository.getTopServicesByRevenue(startDate, endDate, 10);
        List<RevenueExpensesResponse.TopService> topServices = topServicesRaw.stream()
                .map(row -> RevenueExpensesResponse.TopService.builder()
                        .serviceId(((Number) row[0]).longValue())
                        .serviceName((String) row[1])
                        .count(((Number) row[2]).longValue())
                        .revenue((BigDecimal) row[3])
                        .build())
                .collect(Collectors.toList());
        
        return RevenueExpensesResponse.RevenueStats.builder()
                .total(totalRevenue)
                .byType(RevenueExpensesResponse.RevenueByType.builder()
                        .appointment(appointmentRevenue)
                        .treatmentPlan(treatmentPlanRevenue)
                        .supplemental(supplementalRevenue)
                        .build())
                .byDay(revenueByDay)
                .topServices(topServices)
                .build();
    }

    private RevenueExpensesResponse.ExpenseStats buildExpenseStats(LocalDateTime startDate, LocalDateTime endDate) {
        // Calculate total expenses with null safety
        BigDecimal totalExpenses = storageTransactionRepository.calculateTotalExportValue(startDate, endDate);
        totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        
        // Calculate expenses by type with null safety
        BigDecimal usageExpenses = storageTransactionRepository.calculateExportValueByType(startDate, endDate, "USAGE");
        usageExpenses = usageExpenses != null ? usageExpenses : BigDecimal.ZERO;
        
        BigDecimal disposalExpenses = storageTransactionRepository.calculateExportValueByType(startDate, endDate, "DISPOSAL");
        disposalExpenses = disposalExpenses != null ? disposalExpenses : BigDecimal.ZERO;
        
        BigDecimal returnExpenses = storageTransactionRepository.calculateExportValueByType(startDate, endDate, "RETURN");
        returnExpenses = returnExpenses != null ? returnExpenses : BigDecimal.ZERO;
        
        // Separate expired items from damaged items in DISPOSAL category with null safety
        BigDecimal expiredExpenses = storageTransactionRepository.calculateExpiredItemsValue(startDate, endDate);
        expiredExpenses = expiredExpenses != null ? expiredExpenses : BigDecimal.ZERO;
        BigDecimal damagedExpenses = disposalExpenses.subtract(expiredExpenses);
        
        BigDecimal otherExpenses = totalExpenses.subtract(usageExpenses).subtract(disposalExpenses).subtract(returnExpenses);
        
        // Get expenses by day
        List<Object[]> expensesByDayRaw = storageTransactionRepository.getExportValueByDay(startDate, endDate);
        List<RevenueExpensesResponse.DailyAmount> expensesByDay = expensesByDayRaw.stream()
                .map(row -> RevenueExpensesResponse.DailyAmount.builder()
                        .date(row[0] instanceof Date ? ((Date) row[0]).toLocalDate() : (LocalDate) row[0])
                        .amount((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());
        
        // Top exported items by value
        List<Object[]> topItemsRaw = storageTransactionRepository.getTopExportedItems(startDate, endDate, 10);
        List<RevenueExpensesResponse.TopItem> topItems = topItemsRaw.stream()
                .map(row -> RevenueExpensesResponse.TopItem.builder()
                        .itemId(((Number) row[0]).longValue())
                        .itemName((String) row[2])
                        .quantity(((Number) row[3]).longValue())
                        .value((BigDecimal) row[4])
                        .build())
                .collect(Collectors.toList());
        
        return RevenueExpensesResponse.ExpenseStats.builder()
                .total(totalExpenses)
                .byType(RevenueExpensesResponse.ExpenseByType.builder()
                        .serviceConsumption(usageExpenses)
                        .damaged(damagedExpenses)
                        .expired(expiredExpenses)
                        .other(otherExpenses)
                        .build())
                .byDay(expensesByDay)
                .topItems(topItems)
                .build();
    }

    private RevenueExpensesResponse.ComparisonData buildComparison(
            RevenueExpensesResponse.RevenueStats currentRevenue,
            RevenueExpensesResponse.ExpenseStats currentExpenses,
            LocalDateTime prevStartDate,
            LocalDateTime prevEndDate) {
        
        // Calculate previous month revenue and expenses with null safety
        BigDecimal prevRevenue = invoiceRepository.calculateTotalRevenue(prevStartDate, prevEndDate);
        prevRevenue = prevRevenue != null ? prevRevenue : BigDecimal.ZERO;
        
        BigDecimal prevExpenses = storageTransactionRepository.calculateTotalExportValue(prevStartDate, prevEndDate);
        prevExpenses = prevExpenses != null ? prevExpenses : BigDecimal.ZERO;
        
        // Calculate changes
        BigDecimal revenueChange = currentRevenue.getTotal().subtract(prevRevenue);
        BigDecimal expenseChange = currentExpenses.getTotal().subtract(prevExpenses);
        
        // Calculate percentages
        Double revenueChangePercent = prevRevenue.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                revenueChange.divide(prevRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
        
        Double expenseChangePercent = prevExpenses.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                expenseChange.divide(prevExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
        
        return RevenueExpensesResponse.ComparisonData.builder()
                .revenue(RevenueExpensesResponse.ComparisonItem.builder()
                        .previous(prevRevenue)
                        .change(revenueChange)
                        .changePercent(revenueChangePercent)
                        .build())
                .expenses(RevenueExpensesResponse.ComparisonItem.builder()
                        .previous(prevExpenses)
                        .change(expenseChange)
                        .changePercent(expenseChangePercent)
                        .build())
                .build();
    }
}
