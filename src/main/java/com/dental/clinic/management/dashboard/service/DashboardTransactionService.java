package com.dental.clinic.management.dashboard.service;

import com.dental.clinic.management.dashboard.dto.TransactionStatisticsResponse;
import com.dental.clinic.management.dashboard.util.DateRangeUtil;
import com.dental.clinic.management.payment.repository.InvoiceRepository;
import com.dental.clinic.management.payment.repository.PaymentRepository;
import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.enums.InvoiceType;
import com.dental.clinic.management.payment.enums.PaymentMethod;
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
public class DashboardTransactionService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public TransactionStatisticsResponse getTransactionStatistics(String month, LocalDate start, LocalDate end) {
        DateRangeUtil.DateRange dateRange = DateRangeUtil.parseDateRange(month, start, end);
        LocalDateTime startDate = dateRange.getStartDate();
        LocalDateTime endDate = dateRange.getEndDate();

        return TransactionStatisticsResponse.builder()
                .month(dateRange.getLabel())
                .invoices(getInvoiceStats(startDate, endDate))
                .payments(getPaymentStats(startDate, endDate))
                .build();
    }

    private TransactionStatisticsResponse.InvoiceStats getInvoiceStats(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        // Total invoices and value
        Long totalInvoices = invoiceRepository.countInvoicesInRange(startDate, endDate);
        BigDecimal totalValue = invoiceRepository.calculateTotalInvoiceValue(startDate, endDate);
        
        // By status
        Long pendingCount = invoiceRepository.countByStatusInRange(startDate, endDate, InvoicePaymentStatus.PENDING_PAYMENT);
        BigDecimal pendingValue = invoiceRepository.calculateTotalByStatusInRange(startDate, endDate, InvoicePaymentStatus.PENDING_PAYMENT);
        
        Long partialCount = invoiceRepository.countByStatusInRange(startDate, endDate, InvoicePaymentStatus.PARTIAL_PAID);
        BigDecimal partialValue = invoiceRepository.calculateTotalByStatusInRange(startDate, endDate, InvoicePaymentStatus.PARTIAL_PAID);
        
        Long paidCount = invoiceRepository.countByStatusInRange(startDate, endDate, InvoicePaymentStatus.PAID);
        BigDecimal paidValue = invoiceRepository.calculateTotalByStatusInRange(startDate, endDate, InvoicePaymentStatus.PAID);
        
        Long cancelledCount = invoiceRepository.countByStatusInRange(startDate, endDate, InvoicePaymentStatus.CANCELLED);
        BigDecimal cancelledValue = invoiceRepository.calculateTotalByStatusInRange(startDate, endDate, InvoicePaymentStatus.CANCELLED);
        
        // By type
        Long appointmentCount = invoiceRepository.countByTypeInRange(startDate, endDate, InvoiceType.APPOINTMENT);
        BigDecimal appointmentValue = invoiceRepository.calculateTotalByTypeInRange(startDate, endDate, InvoiceType.APPOINTMENT);
        
        Long treatmentPlanCount = invoiceRepository.countByTypeInRange(startDate, endDate, InvoiceType.TREATMENT_PLAN);
        BigDecimal treatmentPlanValue = invoiceRepository.calculateTotalByTypeInRange(startDate, endDate, InvoiceType.TREATMENT_PLAN);
        
        Long supplementalCount = invoiceRepository.countByTypeInRange(startDate, endDate, InvoiceType.SUPPLEMENTAL);
        BigDecimal supplementalValue = invoiceRepository.calculateTotalByTypeInRange(startDate, endDate, InvoiceType.SUPPLEMENTAL);
        
        // Payment rate - (paid + partial_paid) / total
        Long paidOrPartial = paidCount + partialCount;
        Double paymentRate = totalInvoices > 0 ? 
                (paidOrPartial.doubleValue() / totalInvoices.doubleValue()) * 100.0 : 0.0;
        
        // Total debt
        BigDecimal debt = invoiceRepository.calculateTotalDebt(startDate, endDate);
        
        return TransactionStatisticsResponse.InvoiceStats.builder()
                .total(totalInvoices)
                .totalValue(totalValue)
                .byStatus(TransactionStatisticsResponse.InvoiceByStatus.builder()
                        .pendingPayment(buildStatusCount(pendingCount, pendingValue))
                        .partialPaid(buildStatusCount(partialCount, partialValue))
                        .paid(buildStatusCount(paidCount, paidValue))
                        .cancelled(buildStatusCount(cancelledCount, cancelledValue))
                        .build())
                .byType(TransactionStatisticsResponse.InvoiceByType.builder()
                        .appointment(buildStatusCount(appointmentCount, appointmentValue))
                        .treatmentPlan(buildStatusCount(treatmentPlanCount, treatmentPlanValue))
                        .supplemental(buildStatusCount(supplementalCount, supplementalValue))
                        .build())
                .paymentRate(Math.round(paymentRate * 100.0) / 100.0)
                .debt(debt)
                .build();
    }

    private TransactionStatisticsResponse.PaymentStats getPaymentStats(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        // Total payments
        Long total = paymentRepository.countPaymentsInRange(startDate, endDate);
        BigDecimal totalValue = paymentRepository.calculateTotalPaymentValue(startDate, endDate);
        
        // By payment method (currently only SEPAY)
        Long sepayCount = paymentRepository.countByMethodInRange(startDate, endDate, PaymentMethod.SEPAY);
        BigDecimal sepayValue = paymentRepository.calculateValueByMethodInRange(startDate, endDate, PaymentMethod.SEPAY);
        
        // By day
        List<Object[]> paymentsByDayRaw = paymentRepository.getPaymentsByDay(startDate, endDate);
        List<TransactionStatisticsResponse.DailyPayment> paymentsByDay = paymentsByDayRaw.stream()
                .map(row -> TransactionStatisticsResponse.DailyPayment.builder()
                        .date(row[0] instanceof Date ? ((Date) row[0]).toLocalDate() : (LocalDate) row[0])
                        .count(((Number) row[1]).longValue())
                        .value((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
        
        return TransactionStatisticsResponse.PaymentStats.builder()
                .total(total)
                .totalValue(totalValue)
                .byMethod(TransactionStatisticsResponse.PaymentByMethod.builder()
                        .bankTransfer(buildStatusCount(sepayCount, sepayValue))
                        .cash(buildStatusCount(0L, BigDecimal.ZERO))
                        .card(buildStatusCount(0L, BigDecimal.ZERO))
                        .other(buildStatusCount(0L, BigDecimal.ZERO))
                        .build())
                .byDay(paymentsByDay)
                .build();
    }

    private TransactionStatisticsResponse.StatusCount buildStatusCount(Long count, BigDecimal value) {
        return TransactionStatisticsResponse.StatusCount.builder()
                .count(count != null ? count : 0L)
                .value(value != null ? value : BigDecimal.ZERO)
                .build();
    }
}
