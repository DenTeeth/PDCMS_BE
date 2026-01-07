package com.dental.clinic.management.payment.domain;

import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.enums.InvoiceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoice Entity - Hoa don
 * Lien ket voi Appointment hoac Treatment Plan
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Integer invoiceId;

    @Column(name = "invoice_code", unique = true, nullable = false, length = 30)
    private String invoiceCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false, length = 20)
    private InvoiceType invoiceType;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Column(name = "appointment_id")
    private Integer appointmentId;

    @Column(name = "treatment_plan_id")
    private Integer treatmentPlanId;

    @Column(name = "phase_number")
    private Integer phaseNumber;

    @Column(name = "installment_number")
    private Integer installmentNumber;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "remaining_debt", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal remainingDebt = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private InvoicePaymentStatus paymentStatus = InvoicePaymentStatus.PENDING_PAYMENT;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (remainingDebt == null || remainingDebt.compareTo(BigDecimal.ZERO) == 0) {
            remainingDebt = totalAmount;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Tinh toan lai remaining_debt va cap nhat payment_status
     */
    public void recalculatePaymentStatus() {
        this.remainingDebt = this.totalAmount.subtract(this.paidAmount);

        if (this.paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.paymentStatus = InvoicePaymentStatus.PENDING_PAYMENT;
        } else if (this.paidAmount.compareTo(this.totalAmount) >= 0) {
            this.paymentStatus = InvoicePaymentStatus.PAID;
            this.remainingDebt = BigDecimal.ZERO;
        } else {
            this.paymentStatus = InvoicePaymentStatus.PARTIAL_PAID;
        }
    }
}
