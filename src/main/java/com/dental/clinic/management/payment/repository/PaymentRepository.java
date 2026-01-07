package com.dental.clinic.management.payment.repository;

import com.dental.clinic.management.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByPaymentCode(String paymentCode);

    List<Payment> findByInvoice_InvoiceIdOrderByPaymentDateDesc(Integer invoiceId);

    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.paymentCode = :paymentCode")
    boolean existsByPaymentCode(@Param("paymentCode") String paymentCode);

    // ==================== Dashboard Statistics Queries ====================

    /**
     * Count payments in date range
     */
    @Query("SELECT COUNT(p) FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    Long countPaymentsInRange(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Calculate total payment value in date range
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalPaymentValue(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Count payments by method
     */
    @Query("SELECT COUNT(p) FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
           "AND p.paymentMethod = :method")
    Long countByMethodInRange(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("method") com.dental.clinic.management.payment.enums.PaymentMethod method);

    /**
     * Calculate payment value by method
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
           "AND p.paymentMethod = :method")
    java.math.BigDecimal calculateValueByMethodInRange(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("method") com.dental.clinic.management.payment.enums.PaymentMethod method);

    /**
     * Calculate total payment value by method (alias for compatibility)
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
           "AND p.paymentMethod = :method")
    java.math.BigDecimal calculateTotalByMethod(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("method") com.dental.clinic.management.payment.enums.PaymentMethod method);

    /**
     * Count payments in date range (alias for compatibility)
     */
    @Query("SELECT COUNT(p) FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    Long countInRange(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Calculate total payment value (alias for compatibility)
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalValue(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Get payment value by day
     */
    @Query("SELECT DATE(p.paymentDate) as date, COUNT(p) as count, COALESCE(SUM(p.amount), 0) as value " +
           "FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(p.paymentDate) " +
           "ORDER BY DATE(p.paymentDate)")
    java.util.List<Object[]> getPaymentsByDay(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}
