package com.dental.clinic.management.scheduled;

import com.dental.clinic.management.account.domain.Account;
import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.warehouse.domain.ItemBatch;
import com.dental.clinic.management.warehouse.enums.BatchStatus;
import com.dental.clinic.management.warehouse.repository.ItemBatchRepository;
import com.dental.clinic.management.utils.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Warehouse Expiry Email Notification Job
 *
 * Daily Digest Strategy:
 * - Runs at 8:00 AM every day
 * - Finds items expiring in 30, 15, or 5 days
 * - Groups items by urgency (CRITICAL=5 days, WARNING=15 days, INFO=30 days)
 * - Sends ONE consolidated email per day to warehouse users
 * - Only sends to users with VIEW_WAREHOUSE permission
 *
 * Quality over Quantity:
 * - No real-time spam (one email per day maximum)
 * - Professional HTML format with color-coded urgency
 * - Clear actionable information for warehouse keeper
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WarehouseExpiryEmailJob {

    private final ItemBatchRepository itemBatchRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Cron: 0 0 8 * * ?
     * - Runs at 08:00 AM every day
     * - Format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional(readOnly = true)
    public void sendDailyExpiryReport() {
        log.info("========== START: Warehouse Expiry Email Job ==========");

        LocalDate today = LocalDate.now();

        // Find items expiring in 5, 15, 30 days
        List<ExpiryAlert> criticalAlerts = findExpiringBatches(today, 5);
        List<ExpiryAlert> warningAlerts = findExpiringBatches(today, 15);
        List<ExpiryAlert> infoAlerts = findExpiringBatches(today, 30);

        // If no alerts, skip email
        if (criticalAlerts.isEmpty() && warningAlerts.isEmpty() && infoAlerts.isEmpty()) {
            log.info("No expiring items found. Skipping email.");
            log.info("========== END: Warehouse Expiry Email Job ==========");
            return;
        }

        log.info("Found expiring items - CRITICAL: {}, WARNING: {}, INFO: {}",
                criticalAlerts.size(), warningAlerts.size(), infoAlerts.size());

        // Get warehouse users (with VIEW_WAREHOUSE permission)
        List<Account> warehouseUsers = getWarehouseUsers();

        if (warehouseUsers.isEmpty()) {
            log.warn("No warehouse users found with VIEW_WAREHOUSE permission");
            log.info("========== END: Warehouse Expiry Email Job ==========");
            return;
        }

        log.info("Found {} warehouse users to notify", warehouseUsers.size());

        // Generate HTML email
        String emailContent = generateEmailContent(criticalAlerts, warningAlerts, infoAlerts, today);

        // Send email to each warehouse user
        for (Account user : warehouseUsers) {
            try {
                emailService.sendExpiryAlertEmail(
                        user.getEmail(),
                        user.getUsername(),
                        emailContent,
                        criticalAlerts.size(),
                        warningAlerts.size(),
                        infoAlerts.size());
                log.info("Expiry alert email sent to: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send expiry alert to {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("========== END: Warehouse Expiry Email Job ==========");
    }

    /**
     * Find batches expiring exactly in X days (not "within X days")
     * This prevents duplicate notifications
     */
    private List<ExpiryAlert> findExpiringBatches(LocalDate today, int daysUntilExpiry) {
        LocalDate targetDate = today.plusDays(daysUntilExpiry);

        List<ItemBatch> batches = itemBatchRepository.findAll().stream()
                .filter(batch -> batch.getQuantityOnHand() > 0)
                .filter(batch -> batch.getExpiryDate() != null)
                .filter(batch -> batch.getExpiryDate().equals(targetDate))
                .collect(Collectors.toList());

        List<ExpiryAlert> alerts = new ArrayList<>();
        for (ItemBatch batch : batches) {
            long daysRemaining = ChronoUnit.DAYS.between(today, batch.getExpiryDate());
            BatchStatus status = BatchStatus.fromDaysRemaining(daysRemaining);

            alerts.add(ExpiryAlert.builder()
                    .itemCode(batch.getItemMaster().getItemCode())
                    .itemName(batch.getItemMaster().getItemName())
                    .lotNumber(batch.getLotNumber())
                    .expiryDate(batch.getExpiryDate())
                    .daysRemaining(daysRemaining)
                    .quantityOnHand(batch.getQuantityOnHand())
                    .unitName(batch.getItemMaster().getUnitOfMeasure() != null
                            ? batch.getItemMaster().getUnitOfMeasure()
                            : "Unit")
                    .warehouseType(batch.getItemMaster().getWarehouseType().name())
                    .categoryName(batch.getItemMaster().getCategory() != null
                            ? batch.getItemMaster().getCategory().getCategoryName()
                            : "N/A")
                    .supplierName(batch.getSupplier() != null
                            ? batch.getSupplier().getSupplierName()
                            : "N/A")
                    .status(status)
                    .build());
        }

        return alerts;
    }

    /**
     * Get users with VIEW_WAREHOUSE permission
     * These are the warehouse keepers who need to know about expiring items
     */
    private List<Account> getWarehouseUsers() {
        // Query accounts with roles that have VIEW_WAREHOUSE permission
        // Based on seed data: ROLE_RECEPTIONIST, ROLE_MANAGER, ROLE_ADMIN
        return accountRepository.findAll().stream()
                .filter(account -> account.getRole() != null)
                .filter(account -> {
                    String roleId = account.getRole().getRoleId();
                    return roleId.equals("ROLE_ADMIN") ||
                            roleId.equals("ROLE_MANAGER") ||
                            roleId.equals("ROLE_RECEPTIONIST");
                })
                .filter(account -> account.getEmail() != null && !account.getEmail().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Generate professional HTML email content
     * Groups by urgency with color-coded sections
     */
    private String generateEmailContent(
            List<ExpiryAlert> criticalAlerts,
            List<ExpiryAlert> warningAlerts,
            List<ExpiryAlert> infoAlerts,
            LocalDate today) {
        StringBuilder html = new StringBuilder();

        html.append("<div style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto;'>");
        html.append("<h2 style='color: #333; border-bottom: 3px solid #2196F3; padding-bottom: 10px;'>");
        html.append("Bao cao vat tu sap het han su dung - ").append(today.format(DATE_FORMATTER));
        html.append("</h2>");

        html.append("<p style='color: #666;'>Chao ban,</p>");
        html.append(
                "<p style='color: #666;'>Day la bao cao hang ngay ve cac vat tu sap het han su dung trong kho.</p>");

        // CRITICAL section (5 days - RED)
        if (!criticalAlerts.isEmpty()) {
            html.append(
                    "<div style='margin: 20px 0; padding: 15px; background-color: #ffebee; border-left: 5px solid #f44336;'>");
            html.append("<h3 style='color: #c62828; margin-top: 0;'>KHAN CAP: Het han trong 5 ngay (")
                    .append(criticalAlerts.size()).append(" items)</h3>");
            html.append("<p style='color: #666; margin-bottom: 15px;'>Can xu ly NGAY LAP TUC</p>");
            html.append(generateTable(criticalAlerts));
            html.append("</div>");
        }

        // WARNING section (15 days - ORANGE)
        if (!warningAlerts.isEmpty()) {
            html.append(
                    "<div style='margin: 20px 0; padding: 15px; background-color: #fff3e0; border-left: 5px solid #ff9800;'>");
            html.append("<h3 style='color: #e65100; margin-top: 0;'>CANH BAO: Het han trong 15 ngay (")
                    .append(warningAlerts.size()).append(" items)</h3>");
            html.append("<p style='color: #666; margin-bottom: 15px;'>Uu tien xu ly trong tuan nay</p>");
            html.append(generateTable(warningAlerts));
            html.append("</div>");
        }

        // INFO section (30 days - YELLOW)
        if (!infoAlerts.isEmpty()) {
            html.append(
                    "<div style='margin: 20px 0; padding: 15px; background-color: #fffde7; border-left: 5px solid #ffc107;'>");
            html.append("<h3 style='color: #f57f17; margin-top: 0;'>THONG BAO: Het han trong 30 ngay (")
                    .append(infoAlerts.size()).append(" items)</h3>");
            html.append("<p style='color: #666; margin-bottom: 15px;'>Can chu y va lap ke hoach xu ly</p>");
            html.append(generateTable(infoAlerts));
            html.append("</div>");
        }

        // Footer
        html.append("<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0;'>");
        html.append("<p style='color: #999; font-size: 14px;'>Email nay duoc gui tu dong moi ngay luc 8:00 AM.</p>");
        html.append("<p style='color: #999; font-size: 14px;'>Vui long KHONG REPLY email nay.</p>");
        html.append("</div>");

        html.append("</div>");

        return html.toString();
    }

    /**
     * Generate HTML table for alerts
     */
    private String generateTable(List<ExpiryAlert> alerts) {
        StringBuilder table = new StringBuilder();

        table.append("<table style='width: 100%; border-collapse: collapse; font-size: 14px;'>");
        table.append("<thead>");
        table.append("<tr style='background-color: #f5f5f5;'>");
        table.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Ma VT</th>");
        table.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Ten vat tu</th>");
        table.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Lo so</th>");
        table.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>So luong</th>");
        table.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Han dung</th>");
        table.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Con lai</th>");
        table.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>NCC</th>");
        table.append("</tr>");
        table.append("</thead>");
        table.append("<tbody>");

        for (ExpiryAlert alert : alerts) {
            table.append("<tr>");
            table.append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(alert.getItemCode())
                    .append("</td>");
            table.append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(alert.getItemName())
                    .append("</td>");
            table.append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(alert.getLotNumber())
                    .append("</td>");
            table.append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                    .append(alert.getQuantityOnHand()).append(" ").append(alert.getUnitName()).append("</td>");
            table.append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                    .append(alert.getExpiryDate().format(DATE_FORMATTER)).append("</td>");
            table.append("<td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>")
                    .append(alert.getDaysRemaining()).append(" ngay</td>");
            table.append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(alert.getSupplierName())
                    .append("</td>");
            table.append("</tr>");
        }

        table.append("</tbody>");
        table.append("</table>");

        return table.toString();
    }

    /**
     * Inner class for expiry alert data
     */
    @lombok.Builder
    @lombok.Data
    private static class ExpiryAlert {
        private String itemCode;
        private String itemName;
        private String lotNumber;
        private LocalDate expiryDate;
        private Long daysRemaining;
        private Integer quantityOnHand;
        private String unitName;
        private String warehouseType;
        private String categoryName;
        private String supplierName;
        private BatchStatus status;
    }
}
