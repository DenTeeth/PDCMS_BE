package com.dental.clinic.management.payment.repository;

import com.dental.clinic.management.payment.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {

    // Find payment transaction by SePay webhook ID (for duplicate detection)
    Optional<PaymentTransaction> findByPaymentLinkId(String paymentLinkId);

    // Find all transactions for a specific payment, ordered by creation date
    List<PaymentTransaction> findByPayment_PaymentIdOrderByCreatedAtDesc(Integer paymentId);
}
