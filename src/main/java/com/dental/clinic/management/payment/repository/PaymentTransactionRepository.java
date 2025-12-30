package com.dental.clinic.management.payment.repository;

import com.dental.clinic.management.payment.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {

    Optional<PaymentTransaction> findByPayosOrderCode(String payosOrderCode);

    Optional<PaymentTransaction> findByPaymentLinkId(String paymentLinkId);

    List<PaymentTransaction> findByPayment_PaymentIdOrderByCreatedAtDesc(Integer paymentId);
}
