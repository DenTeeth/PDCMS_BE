package com.dental.clinic.management.payment.repository;

import com.dental.clinic.management.payment.domain.Invoice;
import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.enums.InvoiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    Optional<Invoice> findByInvoiceCode(String invoiceCode);

    List<Invoice> findByPatientIdOrderByCreatedAtDesc(Integer patientId);

    List<Invoice> findByAppointmentIdOrderByCreatedAtDesc(Integer appointmentId);

    List<Invoice> findByAppointmentIdAndInvoiceTypeOrderByCreatedAtDesc(Integer appointmentId, InvoiceType invoiceType);

    List<Invoice> findByTreatmentPlanIdOrderByCreatedAtDesc(Integer treatmentPlanId);

    List<Invoice> findByPaymentStatus(InvoicePaymentStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.patientId = :patientId AND i.paymentStatus IN ('PENDING_PAYMENT', 'PARTIAL_PAID')")
    List<Invoice> findUnpaidInvoicesByPatientId(@Param("patientId") Integer patientId);

    @Query("SELECT i FROM Invoice i WHERE i.appointmentId = :appointmentId AND i.paymentStatus IN ('PENDING_PAYMENT', 'PARTIAL_PAID')")
    List<Invoice> findUnpaidInvoicesByAppointmentId(@Param("appointmentId") Integer appointmentId);

    Optional<Invoice> findByNotesContaining(String paymentCode);

    @Query("SELECT COUNT(i) > 0 FROM Invoice i WHERE i.invoiceCode = :invoiceCode")
    boolean existsByInvoiceCode(@Param("invoiceCode") String invoiceCode);

    /**
     * Check if an appointment already has an invoice of a specific type
     * Used to determine if a new invoice should be SUPPLEMENTAL
     * 
     * @param appointmentId The appointment ID to check
     * @param invoiceType The invoice type to check for (typically APPOINTMENT)
     * @return true if an invoice of the specified type exists for the appointment
     */
    boolean existsByAppointmentIdAndInvoiceType(Integer appointmentId, InvoiceType invoiceType);

    /**
     * Count invoices created between start and end datetime
     * Used for generating daily payment code sequence
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find all invoices with optional filters and pagination.
     * Supports filtering by payment status, invoice type, patient ID, and date range.
     * 
     * @param status Optional payment status filter
     * @param type Optional invoice type filter
     * @param patientId Optional patient ID filter
     * @param startDate Optional start date filter (inclusive)
     * @param endDate Optional end date filter (inclusive)
     * @param pageable Pagination and sorting parameters
     * @return Page of invoices matching the filters
     */
    @Query("SELECT i FROM Invoice i WHERE " +
           "(:status IS NULL OR i.paymentStatus = :status) AND " +
           "(:type IS NULL OR i.invoiceType = :type) AND " +
           "(:patientId IS NULL OR i.patientId = :patientId) AND " +
           "(:startDate IS NULL OR i.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR i.createdAt <= :endDate)")
    Page<Invoice> findAllWithFilters(
            @Param("status") InvoicePaymentStatus status,
            @Param("type") InvoiceType type,
            @Param("patientId") Integer patientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ==================== Dashboard Statistics Queries ====================

    /**
     * Calculate total revenue for a date range (PAID + PARTIAL_PAID invoices only)
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "AND i.paymentStatus IN ('PAID', 'PARTIAL_PAID')")
    java.math.BigDecimal calculateTotalRevenue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate revenue by invoice type
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "AND i.paymentStatus IN ('PAID', 'PARTIAL_PAID') " +
           "AND i.invoiceType = :type")
    java.math.BigDecimal calculateRevenueByType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("type") InvoiceType type);

    /**
     * Get revenue by day for chart
     */
    @Query("SELECT DATE(i.createdAt) as date, COALESCE(SUM(i.totalAmount), 0) as amount " +
           "FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "AND i.paymentStatus IN ('PAID', 'PARTIAL_PAID') " +
           "GROUP BY DATE(i.createdAt) " +
           "ORDER BY DATE(i.createdAt)")
    List<Object[]> getRevenueByDay(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count invoices in date range
     */
    @Query("SELECT COUNT(i) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate")
    Long countInvoicesInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count invoices by status
     */
    @Query("SELECT COUNT(i) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "AND i.paymentStatus = :status")
    Long countByStatusInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") InvoicePaymentStatus status);

    /**
     * Calculate total invoice value by status
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "AND i.paymentStatus = :status")
    java.math.BigDecimal calculateTotalByStatusInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") InvoicePaymentStatus status);

    /**
     * Count invoices by type
     */
    @Query("SELECT COUNT(i) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "AND i.invoiceType = :type")
    Long countByTypeInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("type") InvoiceType type);

    /**
     * Calculate total invoice value by type
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "AND i.invoiceType = :type")
    java.math.BigDecimal calculateTotalByTypeInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("type") InvoiceType type);

    /**
     * Calculate total invoice value in date range
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalInvoiceValue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total debt in date range (PENDING_PAYMENT and PARTIAL_PAID invoices)
     */
    @Query("SELECT COALESCE(SUM(i.remainingDebt), 0) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "AND i.paymentStatus IN ('PENDING_PAYMENT', 'PARTIAL_PAID')")
    java.math.BigDecimal calculateTotalDebt(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count unique patients in date range
     */
    @Query("SELECT COUNT(DISTINCT i.patientId) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate")
    Long countUniquePatients(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count total services across all invoices in date range
     * Used for calculating average cost per service KPI
     */
    @Query("SELECT COALESCE(SUM(ii.quantity), 0) FROM Invoice i " +
           "JOIN i.items ii " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate")
    Long countTotalServicesInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count overdue invoices in date range
     * Invoices that are past their due date and still unpaid
     */
    @Query("SELECT COUNT(i) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "AND i.paymentStatus IN ('PENDING_PAYMENT', 'PARTIAL_PAID') " +
           "AND i.dueDate < CURRENT_DATE")
    Long countOverdueInvoices(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total paid revenue (PAID invoices only)
     */
    @Query("SELECT COALESCE(SUM(i.paidAmount), 0) FROM Invoice i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "AND i.paymentStatus IN ('PAID', 'PARTIAL_PAID')")
    java.math.BigDecimal calculatePaidRevenue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
