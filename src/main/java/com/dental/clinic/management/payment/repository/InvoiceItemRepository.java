package com.dental.clinic.management.payment.repository;

import com.dental.clinic.management.payment.domain.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Integer> {

    List<InvoiceItem> findByInvoice_InvoiceId(Integer invoiceId);
}
