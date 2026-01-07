package com.dental.clinic.management.dashboard.service;

import com.dental.clinic.management.dashboard.dto.*;
import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
// import com.dental.clinic.management.payment.enums.InvoiceType;
import com.dental.clinic.management.payment.repository.InvoiceRepository;
import com.dental.clinic.management.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
// import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
// import java.time.format.DateTimeFormatter;
// import java.util.List;

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

    /**
     * Get dashboard overview statistics
     */
    public DashboardOverviewResponse getOverviewStatistics(String month, Boolean compareWithPrevious) {
        log.info("Getting overview statistics for month: {}, compareWithPrevious: {}", month, compareWithPrevious);
        
        YearMonth currentMonth = YearMonth.parse(month);
        LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        // Get current month stats
        BigDecimal totalRevenue = calculateTotalRevenue(startDate, endDate);
        BigDecimal totalExpenses = calculateTotalExpenses(startDate, endDate);
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        
        Long totalInvoices = countInvoices(startDate, endDate);
        Long totalAppointments = countAppointments(startDate, endDate);
        Long totalPatients = countPatients(startDate, endDate);
        Long totalEmployees = countEmployees();
        
        // Summary stats
        DashboardOverviewResponse.SummaryStats summary = DashboardOverviewResponse.SummaryStats.builder()
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .totalInvoices(totalInvoices)
                .totalAppointments(totalAppointments)
                .totalPatients(totalPatients)
                .totalEmployees(totalEmployees)
                .build();
        
        // Build response
        DashboardOverviewResponse.DashboardOverviewResponseBuilder builder = DashboardOverviewResponse.builder()
                .month(month)
                .summary(summary);
        
        // Compare with previous month if requested
        if (Boolean.TRUE.equals(compareWithPrevious)) {
            YearMonth previousMonth = currentMonth.minusMonths(1);
            LocalDateTime prevStartDate = previousMonth.atDay(1).atStartOfDay();
            LocalDateTime prevEndDate = previousMonth.atEndOfMonth().atTime(23, 59, 59);
            
            BigDecimal prevRevenue = calculateTotalRevenue(prevStartDate, prevEndDate);
            BigDecimal prevExpenses = calculateTotalExpenses(prevStartDate, prevEndDate);
            
            builder.previousMonth(previousMonth.toString())
                   .revenue(buildComparisonStats(totalRevenue, prevRevenue))
                   .expenses(buildComparisonStats(totalExpenses, prevExpenses));
        }
        
        // Invoice stats
        builder.invoices(buildInvoiceStats(startDate, endDate));
        
        // Appointment stats
        builder.appointments(buildAppointmentStats(startDate, endDate));
        
        return builder.build();
    }

    /**
     * Get revenue and expenses statistics
     */
    public RevenueExpensesResponse getRevenueExpensesStatistics(String month, Boolean compareWithPrevious) {
        log.info("Getting revenue and expenses statistics for month: {}", month);
        return revenueService.getRevenueExpensesStatistics(month, compareWithPrevious);
    }

    /**
     * Get employee statistics
     */
    public EmployeeStatisticsResponse getEmployeeStatistics(String month, Integer topDoctors) {
        log.info("Getting employee statistics for month: {}, topDoctors: {}", month, topDoctors);
        return employeeService.getEmployeeStatistics(month, topDoctors);
    }

    /**
     * Get warehouse statistics
     */
    public WarehouseStatisticsResponse getWarehouseStatistics(String month) {
        log.info("Getting warehouse statistics for month: {}", month);
        return warehouseService.getWarehouseStatistics(month);
    }

    /**
     * Get transaction statistics
     */
    public TransactionStatisticsResponse getTransactionStatistics(String month) {
        log.info("Getting transaction statistics for month: {}", month);
        return transactionService.getTransactionStatistics(month);
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
        Long partialPaid = invoiceRepository.countByStatusInRange(startDate, endDate, InvoicePaymentStatus.PARTIAL_PAID);
        Long pending = invoiceRepository.countByStatusInRange(startDate, endDate, InvoicePaymentStatus.PENDING_PAYMENT);
        Long cancelled = invoiceRepository.countByStatusInRange(startDate, endDate, InvoicePaymentStatus.CANCELLED);
        
        Long totalPaid = paid + partialPaid;
        Double paidPercent = total == 0 ? 0.0 : (totalPaid.doubleValue() / total.doubleValue()) * 100;
        BigDecimal debt = invoiceRepository.calculateTotalDebt(startDate, endDate);
        
        return DashboardOverviewResponse.InvoiceStats.builder()
                .total(total)
                .paid(totalPaid)
                .pending(pending)
                .cancelled(cancelled)
                .paidPercent(paidPercent)
                .debt(debt)
                .build();
    }

    private DashboardOverviewResponse.AppointmentStats buildAppointmentStats(LocalDateTime startDate, LocalDateTime endDate) {
        Long total = appointmentRepository.countAppointmentsInRange(startDate, endDate);
        Long completed = appointmentRepository.countByStatusInRange(startDate, endDate, 
                com.dental.clinic.management.booking_appointment.enums.AppointmentStatus.COMPLETED);
        Long cancelled = appointmentRepository.countByStatusInRange(startDate, endDate, 
                com.dental.clinic.management.booking_appointment.enums.AppointmentStatus.CANCELLED);
        Long noShow = appointmentRepository.countByStatusInRange(startDate, endDate, 
                com.dental.clinic.management.booking_appointment.enums.AppointmentStatus.NO_SHOW);
        
        Double completionRate = total == 0 ? 0.0 : (completed.doubleValue() / total.doubleValue()) * 100;
        
        return DashboardOverviewResponse.AppointmentStats.builder()
                .total(total)
                .completed(completed)
                .cancelled(cancelled)
                .noShow(noShow)
                .completionRate(completionRate)
                .build();
    }
}
