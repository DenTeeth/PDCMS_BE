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
}
