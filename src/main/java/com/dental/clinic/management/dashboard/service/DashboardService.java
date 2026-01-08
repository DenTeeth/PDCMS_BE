package com.dental.clinic.management.dashboard.service;

import com.dental.clinic.management.dashboard.dto.*;
import com.dental.clinic.management.dashboard.enums.ComparisonMode;
import com.dental.clinic.management.dashboard.util.DateRangeUtil;
import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.repository.InvoiceRepository;
import com.dental.clinic.management.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    @SuppressWarnings("unused")
    private final PaymentRepository paymentRepository;
    private final DashboardRevenueService revenueService;
    private final DashboardEmployeeService employeeService;
    private final DashboardWarehouseService warehouseService;
    private final DashboardTransactionService transactionService;
    
    // Additional repositories for overview statistics
    private final com.dental.clinic.management.warehouse.repository.StorageTransactionRepository storageTransactionRepository;
    private final com.dental.clinic.management.booking_appointment.repository.AppointmentRepository appointmentRepository;
    private final com.dental.clinic.management.employee.repository.EmployeeRepository employeeRepository;
    private final com.dental.clinic.management.warehouse.repository.ItemMasterRepository itemMasterRepository;
    private final com.dental.clinic.management.warehouse.repository.ItemBatchRepository itemBatchRepository;

    /**
     * Get dashboard overview statistics
     * Supports both month-based and date range filtering
     * Supports multiple comparison modes: MONTH, QUARTER, YEAR
     * Supports advanced filtering by employee, patient, service
     * 
     * Cached for 5 minutes to improve performance
     */
    @Cacheable(value = "dashboardOverview", 
               key = "#month + '_' + #startDate + '_' + #endDate + '_' + #comparisonModeStr + '_' + #employeeId + '_' + #patientId + '_' + #serviceId",
               unless = "#result == null")
    public DashboardOverviewResponse getOverviewStatistics(String month, LocalDate startDate, LocalDate endDate, 
            Boolean compareWithPrevious, String comparisonModeStr,
            Integer employeeId, Integer patientId, Integer serviceId) {
        log.info("Getting overview statistics - month: {}, startDate: {}, endDate: {}, compare: {}, comparisonMode: {}, filters: employee={}, patient={}, service={}", 
                month, startDate, endDate, compareWithPrevious, comparisonModeStr, employeeId, patientId, serviceId);
        
        // Parse comparison mode
        ComparisonMode comparisonMode = ComparisonMode.fromString(comparisonModeStr);
        // Backward compatibility: if compareWithPrevious is true and no mode specified, use MONTH
        if (Boolean.TRUE.equals(compareWithPrevious) && comparisonMode == ComparisonMode.NONE) {
            comparisonMode = ComparisonMode.MONTH;
        }
        
        // Parse date range from parameters (with backward compatibility)
        DateRangeUtil.DateRange dateRange = DateRangeUtil.parseDateRange(month, startDate, endDate);
        LocalDateTime rangeStart = dateRange.getStartDate();
        LocalDateTime rangeEnd = dateRange.getEndDate();
        
        // Get current period stats
        BigDecimal totalRevenue = calculateTotalRevenue(rangeStart, rangeEnd);
        BigDecimal totalExpenses = calculateTotalExpenses(rangeStart, rangeEnd);
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        
        Long totalInvoices = countInvoices(rangeStart, rangeEnd);
        Long totalAppointments = countAppointments(rangeStart, rangeEnd);
        Long totalPatients = countPatients(rangeStart, rangeEnd);
        
        // Summary stats
        DashboardOverviewResponse.SummaryStats summary = DashboardOverviewResponse.SummaryStats.builder()
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .totalInvoices(totalInvoices)
                .totalAppointments(totalAppointments)
                .totalPatients(totalPatients)
                .build();
        
        // Build response
        DashboardOverviewResponse.DashboardOverviewResponseBuilder builder = DashboardOverviewResponse.builder()
                .month(dateRange.getLabel())
                .summary(summary);
        
        // Compare with previous period if requested
        BigDecimal prevRevenue = null;
        if (comparisonMode != ComparisonMode.NONE) {
            DateRangeUtil.DateRange comparisonPeriod = DateRangeUtil.calculateComparisonPeriod(dateRange, comparisonMode);
            if (comparisonPeriod != null) {
                LocalDateTime compStartDate = comparisonPeriod.getStartDate();
                LocalDateTime compEndDate = comparisonPeriod.getEndDate();
                
                prevRevenue = calculateTotalRevenue(compStartDate, compEndDate);
                BigDecimal prevExpenses = calculateTotalExpenses(compStartDate, compEndDate);
                
                builder.previousMonth(comparisonPeriod.getLabel())
                       .revenue(buildComparisonStats(totalRevenue, prevRevenue))
                       .expenses(buildComparisonStats(totalExpenses, prevExpenses));
            }
        }
        
        // Invoice stats
        builder.invoices(buildInvoiceStats(rangeStart, rangeEnd));
        
        // Appointment stats
        builder.appointments(buildAppointmentStats(rangeStart, rangeEnd));
        
        // Alert stats (top 5 only)
        DashboardOverviewResponse.InvoiceStats invoiceStats = buildInvoiceStats(rangeStart, rangeEnd);
        DashboardOverviewResponse.AppointmentStats appointmentStats = buildAppointmentStats(rangeStart, rangeEnd);
        builder.alerts(buildAlertStats(totalRevenue, prevRevenue, invoiceStats, appointmentStats));
        
        return builder.build();
    }

    /**
     * Get revenue and expenses statistics
     * Supports both month-based and date range filtering
     * Supports multiple comparison modes: MONTH, QUARTER, YEAR
     * Supports advanced filtering by employee, patient, service
     */
    @Cacheable(value = "dashboardRevenue", 
               key = "#month + '_' + #startDate + '_' + #endDate + '_' + #comparisonModeStr + '_' + #employeeId + '_' + #patientId + '_' + #serviceId",
               unless = "#result == null")
    public RevenueExpensesResponse getRevenueExpensesStatistics(String month, LocalDate startDate, LocalDate endDate, 
            Boolean compareWithPrevious, String comparisonModeStr,
            Integer employeeId, Integer patientId, Integer serviceId) {
        log.info("Getting revenue and expenses statistics - month: {}, startDate: {}, endDate: {}, filters: employee={}, patient={}, service={}", 
                month, startDate, endDate, employeeId, patientId, serviceId);
        return revenueService.getRevenueExpensesStatistics(month, startDate, endDate, compareWithPrevious, comparisonModeStr);
    }

    /**
     * Get employee statistics
     * Supports both month-based and date range filtering
     * Supports advanced filtering by employee, service
     */
    @Cacheable(value = "dashboardEmployees", 
               key = "#month + '_' + #startDate + '_' + #endDate + '_' + #topDoctors + '_' + #employeeId + '_' + #serviceId",
               unless = "#result == null")
    public EmployeeStatisticsResponse getEmployeeStatistics(String month, LocalDate startDate, LocalDate endDate, 
            Integer topDoctors, Integer employeeId, Integer serviceId) {
        log.info("Getting employee statistics - month: {}, startDate: {}, endDate: {}, topDoctors: {}, filters: employee={}, service={}", 
                month, startDate, endDate, topDoctors, employeeId, serviceId);
        return employeeService.getEmployeeStatistics(month, startDate, endDate, topDoctors);
    }

    /**
     * Get warehouse statistics
     * Supports both month-based and date range filtering
     */
    @Cacheable(value = "dashboardWarehouse", 
               key = "#month + '_' + #startDate + '_' + #endDate",
               unless = "#result == null")
    public WarehouseStatisticsResponse getWarehouseStatistics(String month, LocalDate startDate, LocalDate endDate) {
        log.info("Getting warehouse statistics - month: {}, startDate: {}, endDate: {}", month, startDate, endDate);
        return warehouseService.getWarehouseStatistics(month, startDate, endDate);
    }

    /**
     * Get transaction statistics
     * Supports both month-based and date range filtering
     */
    @Cacheable(value = "dashboardTransactions", 
               key = "#month + '_' + #startDate + '_' + #endDate",
               unless = "#result == null")
    public TransactionStatisticsResponse getTransactionStatistics(String month, LocalDate startDate, LocalDate endDate) {
        log.info("Getting transaction statistics - month: {}, startDate: {}, endDate: {}", month, startDate, endDate);
        return transactionService.getTransactionStatistics(month, startDate, endDate);
    }

    /**
     * Get appointment heatmap data
     * Shows appointment distribution by day of week and hour
     * Cached for 30 minutes (changes less frequently)
     */
    @Cacheable(value = "dashboardHeatmap", 
               key = "#month + '_' + #startDate + '_' + #endDate",
               unless = "#result == null")
    public AppointmentHeatmapResponse getAppointmentHeatmap(String month, LocalDate startDate, LocalDate endDate) {
        log.info("Getting appointment heatmap - month: {}, startDate: {}, endDate: {}", month, startDate, endDate);
        
        DateRangeUtil.DateRange dateRange = DateRangeUtil.parseDateRange(month, startDate, endDate);
        LocalDateTime rangeStart = dateRange.getStartDate();
        LocalDateTime rangeEnd = dateRange.getEndDate();
        
        // Get raw heatmap data from repository
        List<Object[]> rawData = appointmentRepository.getAppointmentHeatmapData(rangeStart, rangeEnd);
        Long totalAppointments = appointmentRepository.countAppointmentsInRange(rangeStart, rangeEnd);
        
        // Transform to response format
        List<AppointmentHeatmapResponse.HeatmapCell> cells = new java.util.ArrayList<>();
        String busiestDay = null;
        Integer busiestHour = null;
        Long peakCount = 0L;
        
        String[] dayNames = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        
        for (Object[] row : rawData) {
            Integer dayOfWeek = ((Number) row[0]).intValue(); // 0=Sunday, 1=Monday, etc.
            Integer hour = ((Number) row[1]).intValue();
            Long count = ((Number) row[2]).longValue();
            Double percentage = totalAppointments > 0 ? (count.doubleValue() / totalAppointments.doubleValue()) * 100 : 0.0;
            
            cells.add(AppointmentHeatmapResponse.HeatmapCell.builder()
                    .dayOfWeek(dayNames[dayOfWeek])
                    .hour(hour)
                    .count(count)
                    .percentage(percentage)
                    .build());
            
            // Track busiest time
            if (count > peakCount) {
                peakCount = count;
                busiestDay = dayNames[dayOfWeek];
                busiestHour = hour;
            }
        }
        
        // Calculate average appointments per slot
        int totalSlots = 7 * 24; // 7 days * 24 hours
        Double avgPerSlot = totalAppointments.doubleValue() / totalSlots;
        
        return AppointmentHeatmapResponse.builder()
                .startDate(rangeStart.toLocalDate().toString())
                .endDate(rangeEnd.toLocalDate().toString())
                .data(cells)
                .statistics(AppointmentHeatmapResponse.Statistics.builder()
                        .totalAppointments(totalAppointments)
                        .busiestDay(busiestDay)
                        .busiestHour(busiestHour)
                        .peakAppointments(peakCount)
                        .averageAppointmentsPerSlot(avgPerSlot)
                        .build())
                .build();
    }

    // ==================== Private Helper Methods ====================

    private BigDecimal calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.calculateTotalRevenue(startDate, endDate);
    }

    private BigDecimal calculateTotalExpenses(LocalDateTime startDate, LocalDateTime endDate) {
        return storageTransactionRepository.calculateTotalExportValue(startDate, endDate);
    }

    private Long countInvoices(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.countInvoicesInRange(startDate, endDate);
    }

    private Long countAppointments(LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.countAppointmentsInRange(startDate, endDate);
    }

    private Long countPatients(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.countUniquePatients(startDate, endDate);
    }

    @SuppressWarnings("unused")
    private Long countEmployees() {
        return employeeRepository.count();
    }

    private DashboardOverviewResponse.ComparisonStats buildComparisonStats(BigDecimal current, BigDecimal previous) {
        BigDecimal change = current.subtract(previous);
        Double changePercent = previous.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                change.divide(previous, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
        
        return DashboardOverviewResponse.ComparisonStats.builder()
                .current(current)
                .previous(previous)
                .change(change)
                .changePercent(changePercent)
                .build();
    }

    private DashboardOverviewResponse.InvoiceStats buildInvoiceStats(LocalDateTime startDate, LocalDateTime endDate) {
        Long total = invoiceRepository.countInvoicesInRange(startDate, endDate);
        Long paid = invoiceRepository.countByStatusInRange(startDate, endDate, InvoicePaymentStatus.PAID);
        Long pending = invoiceRepository.countByStatusInRange(startDate, endDate, InvoicePaymentStatus.PENDING_PAYMENT);
        Long overdue = invoiceRepository.countOverdueInvoices(startDate, endDate);
        
        BigDecimal totalAmount = invoiceRepository.calculateTotalRevenue(startDate, endDate);
        BigDecimal paidAmount = invoiceRepository.calculatePaidRevenue(startDate, endDate);
        
        return DashboardOverviewResponse.InvoiceStats.builder()
                .total(total)
                .paid(paid)
                .pending(pending)
                .overdue(overdue)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .build();
    }

    private DashboardOverviewResponse.AppointmentStats buildAppointmentStats(LocalDateTime startDate, LocalDateTime endDate) {
        Long total = appointmentRepository.countAppointmentsInRange(startDate, endDate);
        
        Long scheduled = appointmentRepository.countByStatusInRange(startDate, endDate, 
                com.dental.clinic.management.booking_appointment.enums.AppointmentStatus.SCHEDULED);
        Long completed = appointmentRepository.countByStatusInRange(startDate, endDate, 
                com.dental.clinic.management.booking_appointment.enums.AppointmentStatus.COMPLETED);
        Long cancelled = appointmentRepository.countByStatusInRange(startDate, endDate, 
                com.dental.clinic.management.booking_appointment.enums.AppointmentStatus.CANCELLED);
        
        return DashboardOverviewResponse.AppointmentStats.builder()
                .total(total)
                .scheduled(scheduled)
                .completed(completed)
                .cancelled(cancelled)
                .build();
    }

    /**
     * Build Alert/Notification statistics
     * Limited to top 5 most critical alerts only
     */
    private DashboardOverviewResponse.AlertStats buildAlertStats(
            BigDecimal currentRevenue,
            BigDecimal previousRevenue,
            DashboardOverviewResponse.InvoiceStats invoiceStats,
            DashboardOverviewResponse.AppointmentStats appointmentStats) {
        
        java.util.List<DashboardOverviewResponse.Alert> alerts = new java.util.ArrayList<>();
        
        // 1. Revenue Drop Alert (>20% decrease vs previous period)
        if (previousRevenue != null && previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal revenueChange = currentRevenue.subtract(previousRevenue);
            BigDecimal changePercent = revenueChange.divide(previousRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            if (changePercent.compareTo(BigDecimal.valueOf(-20)) < 0) {
                alerts.add(DashboardOverviewResponse.Alert.builder()
                        .type("REVENUE_DROP")
                        .severity("CRITICAL")
                        .title("Doanh thu giảm mạnh")
                        .message("Doanh thu giảm " + changePercent.abs().setScale(1, RoundingMode.HALF_UP) + "% so với kỳ trước")
                        .value(String.format("%,.0f VND", currentRevenue))
                        .threshold("Giảm > 20%")
                        .build());
            }
        }
        
        // 2. High Unpaid Amount Alert (>10,000,000 VND)
        BigDecimal unpaidThreshold = new BigDecimal("10000000");
        BigDecimal unpaidAmount = invoiceStats.getTotalAmount().subtract(invoiceStats.getPaidAmount());
        if (unpaidAmount.compareTo(unpaidThreshold) > 0) {
            alerts.add(DashboardOverviewResponse.Alert.builder()
                    .type("HIGH_DEBT")
                    .severity("WARNING")
                    .title("Công nợ cao")
                    .message("Tổng công nợ vượt ngưỡng cảnh báo")
                    .value(String.format("%,.0f VND", unpaidAmount))
                    .threshold("10,000,000 VND")
                    .build());
        }
        
        // 3. High Overdue Invoices Alert
        if (invoiceStats.getOverdue() > 5) {
            alerts.add(DashboardOverviewResponse.Alert.builder()
                    .type("HIGH_OVERDUE")
                    .severity("WARNING")
                    .title("Nhiều hóa đơn quá hạn")
                    .message(invoiceStats.getOverdue() + " hóa đơn đã quá hạn thanh toán")
                    .value(invoiceStats.getOverdue().toString() + " invoices")
                    .threshold("> 5 invoices")
                    .build());
        }
        
        // 4. Low Inventory Alert
        Long lowInventoryCount = countLowInventoryItems();
        if (lowInventoryCount > 5) {
            alerts.add(DashboardOverviewResponse.Alert.builder()
                    .type("LOW_INVENTORY")
                    .severity("WARNING")
                    .title("Hàng tồn kho thấp")
                    .message(lowInventoryCount + " vật tư có số lượng dưới mức tối thiểu")
                    .value(lowInventoryCount.toString() + " items")
                    .threshold("> 5 items")
                    .build());
        }
        
        // 5. Expiring Materials Alert (expiring within 30 days)
        Long expiringCount = countExpiringMaterials(30);
        if (expiringCount > 10) {
            alerts.add(DashboardOverviewResponse.Alert.builder()
                    .type("EXPIRING_MATERIALS")
                    .severity("WARNING")
                    .title("Vật tư sắp hết hạn")
                    .message(expiringCount + " lô hàng sẽ hết hạn trong 30 ngày")
                    .value(expiringCount.toString() + " batches")
                    .threshold("> 10 batches")
                    .build());
        }
        
        // Sort alerts by severity (CRITICAL first, then WARNING) and limit to 5
        alerts.sort((a, b) -> {
            if (a.getSeverity().equals(b.getSeverity())) return 0;
            return a.getSeverity().equals("CRITICAL") ? -1 : 1;
        });
        
        // Limit to top 5 alerts
        java.util.List<DashboardOverviewResponse.Alert> topAlerts = alerts.size() > 5 
            ? alerts.subList(0, 5) 
            : alerts;
        
        return DashboardOverviewResponse.AlertStats.builder()
                .totalAlerts(topAlerts.size())
                .alerts(topAlerts)
                .build();
    }

    /**
     * Count items with low inventory (below minimum stock level)
     */
    private Long countLowInventoryItems() {
        return itemMasterRepository.countLowInventoryItems();
    }

    /**
     * Count materials expiring within specified days
     */
    private Long countExpiringMaterials(int daysThreshold) {
        LocalDate targetDate = LocalDate.now().plusDays(daysThreshold);
        return itemBatchRepository.countExpiringItems(targetDate, null);
    }
}
