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
}
