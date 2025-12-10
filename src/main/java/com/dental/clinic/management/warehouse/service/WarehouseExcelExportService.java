package com.dental.clinic.management.warehouse.service;

import com.dental.clinic.management.warehouse.dto.response.ExpiringAlertDTO;
import com.dental.clinic.management.warehouse.dto.response.ExpiringAlertsResponse;
import com.dental.clinic.management.warehouse.dto.response.InventoryItemDTO;
import com.dental.clinic.management.warehouse.dto.response.InventorySummaryResponse;
import com.dental.clinic.management.warehouse.dto.response.TransactionHistoryResponse;
import com.dental.clinic.management.warehouse.dto.response.TransactionHistoryItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Excel Export Service for Warehouse Reports
 * Issue #50: Export 3 warehouse reports to Excel
 * 
 * Features:
 * - Inventory Summary export (with filters: warehouseType, stockStatus, search, categoryId)
 * - Transaction History export (with filters: type, status, dateRange)
 * - Expiring Alerts export (with filters: days, warehouseType, categoryId)
 * - Formatted headers and auto-sized columns
 * - Freeze panes for better navigation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Export Inventory Summary to Excel
     * API: GET /api/v1/warehouse/summary/export
     */
    public byte[] exportInventorySummary(InventorySummaryResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inventory Summary");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "STT", "Item Code", "Item Name", "Category", "Warehouse Type", 
                "Unit", "Total Quantity", "Min Stock", "Max Stock", 
                "Stock Status", "Nearest Expiry Date"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            if (response.getContent() != null) {
                for (InventoryItemDTO item : response.getContent()) {
                    Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, rowNum - 1, numberStyle);
                createCell(row, 1, item.getItemCode(), dataStyle);
                createCell(row, 2, item.getItemName(), dataStyle);
                createCell(row, 3, item.getCategoryName(), dataStyle);
                createCell(row, 4, item.getWarehouseType() != null ? item.getWarehouseType().name() : "", dataStyle);
                createCell(row, 5, item.getUnitName(), dataStyle);
                createCell(row, 6, item.getTotalQuantity() != null ? item.getTotalQuantity() : 0, numberStyle);
                createCell(row, 7, item.getMinStockLevel() != null ? item.getMinStockLevel() : 0, numberStyle);
                createCell(row, 8, item.getMaxStockLevel() != null ? item.getMaxStockLevel() : 0, numberStyle);
                createCell(row, 9, item.getStockStatus() != null ? item.getStockStatus().name() : "", dataStyle);
                
                // Handle nearest expiry date
                if (item.getNearestExpiryDate() != null) {
                    Cell dateCell = row.createCell(10);
                    dateCell.setCellValue(item.getNearestExpiryDate().format(DATE_FORMATTER));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    createCell(row, 10, "", dataStyle);
                }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Freeze header row
            sheet.createFreezePane(0, 1);

            workbook.write(out);
            log.info("Exported {} inventory items to Excel", response.getContent() != null ? response.getContent().size() : 0);
            return out.toByteArray();
        }
    }

    /**
     * Export Transaction History to Excel
     * API: GET /api/v1/warehouse/transactions/export
     */
    public byte[] exportTransactionHistory(TransactionHistoryResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transaction History");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "STT", "Transaction Code", "Type", "Transaction Date", "Status", 
                "Payment Status", "Invoice Number", "Supplier/Appointment", "Total Value", 
                "Paid Amount", "Remaining Debt", "Created By", "Approved By", "Notes"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            if (response.getContent() != null) {
                for (TransactionHistoryItemDto txn : response.getContent()) {
                    Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, rowNum - 1, numberStyle);
                createCell(row, 1, txn.getTransactionCode(), dataStyle);
                createCell(row, 2, txn.getType() != null ? txn.getType().name() : "", dataStyle);
                
                // Transaction date
                if (txn.getTransactionDate() != null) {
                    Cell dateCell = row.createCell(3);
                    dateCell.setCellValue(txn.getTransactionDate().format(DATETIME_FORMATTER));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    createCell(row, 3, "", dataStyle);
                }
                
                createCell(row, 4, txn.getStatus() != null ? txn.getStatus().name() : "", dataStyle);
                createCell(row, 5, txn.getPaymentStatus() != null ? txn.getPaymentStatus().name() : "", dataStyle);
                createCell(row, 6, txn.getInvoiceNumber(), dataStyle);
                createCell(row, 7, txn.getSupplierName() != null ? txn.getSupplierName() : 
                                   (txn.getRelatedAppointmentCode() != null ? txn.getRelatedAppointmentCode() : ""), dataStyle);
                
                // Currency values
                createCell(row, 8, txn.getTotalValue() != null ? txn.getTotalValue().doubleValue() : 0.0, currencyStyle);
                createCell(row, 9, txn.getPaidAmount() != null ? txn.getPaidAmount().doubleValue() : 0.0, currencyStyle);
                createCell(row, 10, txn.getRemainingDebt() != null ? txn.getRemainingDebt().doubleValue() : 0.0, currencyStyle);
                
                createCell(row, 11, txn.getCreatedByName(), dataStyle);
                createCell(row, 12, txn.getApprovedByName(), dataStyle);
                createCell(row, 13, txn.getNotes(), dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Freeze header row
            sheet.createFreezePane(0, 1);

            workbook.write(out);
            log.info("Exported {} transactions to Excel", response.getContent() != null ? response.getContent().size() : 0);
            return out.toByteArray();
        }
    }

    /**
     * Export Expiring Alerts to Excel
     * API: GET /api/v1/warehouse/expiring-alerts/export
     */
    public byte[] exportExpiringAlerts(ExpiringAlertsResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Expiring Alerts");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle warningStyle = createWarningStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "STT", "Item Code", "Item Name", "Lot Number", "Warehouse Type", 
                "Quantity On Hand", "Unit", "Expiry Date", "Days Remaining", 
                "Status", "Bin Location", "Supplier", "Category"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            if (response.getAlerts() != null) {
                for (ExpiringAlertDTO alert : response.getAlerts()) {
                    Row row = sheet.createRow(rowNum++);
                
                // Choose style based on status
                CellStyle statusStyle = alert.getStatus() == com.dental.clinic.management.warehouse.enums.BatchStatus.EXPIRED ? warningStyle : dataStyle;
                
                createCell(row, 0, rowNum - 1, numberStyle);
                createCell(row, 1, alert.getItemCode(), dataStyle);
                createCell(row, 2, alert.getItemName(), dataStyle);
                createCell(row, 3, alert.getLotNumber(), dataStyle);
                createCell(row, 4, alert.getWarehouseType() != null ? alert.getWarehouseType().name() : "", dataStyle);
                createCell(row, 5, alert.getQuantityOnHand() != null ? alert.getQuantityOnHand() : 0, numberStyle);
                createCell(row, 6, alert.getUnitName(), dataStyle);
                
                // Expiry date
                if (alert.getExpiryDate() != null) {
                    Cell dateCell = row.createCell(7);
                    dateCell.setCellValue(alert.getExpiryDate().format(DATE_FORMATTER));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    createCell(row, 7, "", dataStyle);
                }
                
                createCell(row, 8, alert.getDaysRemaining() != null ? alert.getDaysRemaining().intValue() : 0, numberStyle);
                createCell(row, 9, alert.getStatus() != null ? alert.getStatus().name() : "", statusStyle);
                createCell(row, 10, alert.getBinLocation(), dataStyle);
                createCell(row, 11, alert.getSupplierName(), dataStyle);
                createCell(row, 12, alert.getCategoryName(), dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Freeze header row
            sheet.createFreezePane(0, 1);

            workbook.write(out);
            log.info("Exported {} expiring alerts to Excel", response.getAlerts() != null ? response.getAlerts().size() : 0);
            return out.toByteArray();
        }
    }

    // =============== HELPER METHODS ===============

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00\" ₫\""));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createWarningStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int column, int value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
