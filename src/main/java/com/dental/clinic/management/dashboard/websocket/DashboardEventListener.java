package com.dental.clinic.management.dashboard.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * Dashboard Event Listener
 * Listens for domain events and broadcasts WebSocket updates
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardEventListener {

    private final DashboardWebSocketService webSocketService;

    /**
     * Listen for invoice creation/update events
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvoiceChanged(InvoiceChangedEvent event) {
        log.info("Invoice changed event received: {}", event.getInvoiceId());
        webSocketService.broadcastDataUpdate("REVENUE");
        webSocketService.broadcastDataUpdate("OVERVIEW");
        webSocketService.broadcastDataUpdate("TRANSACTIONS");
    }

    /**
     * Listen for appointment creation/update events
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAppointmentChanged(AppointmentChangedEvent event) {
        log.info("Appointment changed event received: {}", event.getAppointmentId());
        webSocketService.broadcastDataUpdate("APPOINTMENTS");
        webSocketService.broadcastDataUpdate("OVERVIEW");
        webSocketService.broadcastDataUpdate("HEATMAP");
    }

    /**
     * Listen for payment events
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentReceived(PaymentReceivedEvent event) {
        log.info("Payment received event: {}", event.getPaymentId());
        webSocketService.broadcastDataUpdate("REVENUE");
        webSocketService.broadcastDataUpdate("TRANSACTIONS");
    }

    /**
     * Listen for warehouse transaction events
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWarehouseTransaction(WarehouseTransactionEvent event) {
        log.info("Warehouse transaction event: {}", event.getTransactionId());
        webSocketService.broadcastDataUpdate("WAREHOUSE");
        webSocketService.broadcastDataUpdate("EXPENSES");
    }

    /**
     * Listen for alert events
     */
    @EventListener
    public void onDashboardAlert(DashboardAlertEvent event) {
        log.info("Dashboard alert event: {}", event.getMessage());
        webSocketService.broadcastAlert(
            event.getSeverity(),
            event.getMessage(),
            event.getAlertType()
        );
    }

    // Event classes (to be created in their respective modules)
    
    public static class InvoiceChangedEvent {
        private final Integer invoiceId;
        public InvoiceChangedEvent(Integer invoiceId) { this.invoiceId = invoiceId; }
        public Integer getInvoiceId() { return invoiceId; }
    }

    public static class AppointmentChangedEvent {
        private final Integer appointmentId;
        public AppointmentChangedEvent(Integer appointmentId) { this.appointmentId = appointmentId; }
        public Integer getAppointmentId() { return appointmentId; }
    }

    public static class PaymentReceivedEvent {
        private final Integer paymentId;
        public PaymentReceivedEvent(Integer paymentId) { this.paymentId = paymentId; }
        public Integer getPaymentId() { return paymentId; }
    }

    public static class WarehouseTransactionEvent {
        private final Integer transactionId;
        public WarehouseTransactionEvent(Integer transactionId) { this.transactionId = transactionId; }
        public Integer getTransactionId() { return transactionId; }
    }

    public static class DashboardAlertEvent {
        private final String severity;
        private final String message;
        private final String alertType;
        
        public DashboardAlertEvent(String severity, String message, String alertType) {
            this.severity = severity;
            this.message = message;
            this.alertType = alertType;
        }
        
        public String getSeverity() { return severity; }
        public String getMessage() { return message; }
        public String getAlertType() { return alertType; }
    }
}
