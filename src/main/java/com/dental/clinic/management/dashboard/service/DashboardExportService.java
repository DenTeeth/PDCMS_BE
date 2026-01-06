package com.dental.clinic.management.dashboard.service;

import com.dental.clinic.management.dashboard.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardExportService {

    private final DashboardService dashboardService;
    private final DashboardRevenueService revenueService;
    private final DashboardEmployeeService employeeService;
    private final DashboardWarehouseService warehouseService;
    private final DashboardTransactionService transactionService;

    public byte[] exportToExcel(String tab, String month) {
        log.info("Exporting tab: {} for month: {}", tab, month);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            switch (tab.toLowerCase()) {
                case "overview" -> exportOverview(workbook, month);
                case "revenue-expenses" -> exportRevenueExpenses(workbook, month);
                case "employees" -> exportEmployees(workbook, month);
                case "warehouse" -> exportWarehouse(workbook, month);
                case "transactions" -> exportTransactions(workbook, month);
                default -> throw new IllegalArgumentException("Invalid tab: " + tab);
            }
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("Error exporting dashboard to Excel", e);
            throw new RuntimeException("Failed to export dashboard to Excel", e);
        }
    }

    private void exportOverview(Workbook workbook, String month) {
        DashboardOverviewResponse data = dashboardService.getOverviewStatistics(month, false);
        Sheet sheet = workbook.createSheet("Overview");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle percentStyle = createPercentStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Dashboard Overview - " + month);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        rowNum++;
        
        // Summary Section
        createSectionHeader(sheet, rowNum++, "Summary Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Revenue", data.getSummary().getTotalRevenue(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Expenses", data.getSummary().getTotalExpenses(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Net Profit", data.getSummary().getNetProfit(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Invoices", data.getSummary().getTotalInvoices());
        rowNum = addDataRow(sheet, rowNum, "Total Appointments", data.getSummary().getTotalAppointments());
        rowNum = addDataRow(sheet, rowNum, "Total Patients", data.getSummary().getTotalPatients());
        rowNum = addDataRow(sheet, rowNum, "Total Employees", data.getSummary().getTotalEmployees());
        rowNum++;
        
        // Invoice Stats
        createSectionHeader(sheet, rowNum++, "Invoice Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Invoices", data.getInvoices().getTotal());
        rowNum = addDataRow(sheet, rowNum, "Paid Invoices", data.getInvoices().getPaid());
        rowNum = addDataRow(sheet, rowNum, "Pending Invoices", data.getInvoices().getPending());
        rowNum = addDataRow(sheet, rowNum, "Cancelled Invoices", data.getInvoices().getCancelled());
        rowNum = addDataRow(sheet, rowNum, "Payment Rate", data.getInvoices().getPaidPercent(), percentStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Debt", data.getInvoices().getDebt(), currencyStyle);
        rowNum++;
        
        // Appointment Stats
        createSectionHeader(sheet, rowNum++, "Appointment Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Appointments", data.getAppointments().getTotal());
        rowNum = addDataRow(sheet, rowNum, "Completed", data.getAppointments().getCompleted());
        rowNum = addDataRow(sheet, rowNum, "Cancelled", data.getAppointments().getCancelled());
        rowNum = addDataRow(sheet, rowNum, "No Show", data.getAppointments().getNoShow());
        rowNum = addDataRow(sheet, rowNum, "Completion Rate", data.getAppointments().getCompletionRate(), percentStyle);
        
        autoSizeColumns(sheet, 2);
    }

    private void exportRevenueExpenses(Workbook workbook, String month) {
        RevenueExpensesResponse data = revenueService.getRevenueExpensesStatistics(month, false);
        Sheet sheet = workbook.createSheet("Revenue & Expenses");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Revenue & Expenses - " + month);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        rowNum++;
        
        // Revenue Section
        createSectionHeader(sheet, rowNum++, "Revenue Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Revenue", data.getRevenue().getTotal(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Appointment Revenue", data.getRevenue().getByType().getAppointment(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Treatment Plan Revenue", data.getRevenue().getByType().getTreatmentPlan(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Supplemental Revenue", data.getRevenue().getByType().getSupplemental(), currencyStyle);
        rowNum++;
        
        // Top Services
        if (!data.getRevenue().getTopServices().isEmpty()) {
            createSectionHeader(sheet, rowNum++, "Top Services by Revenue", headerStyle);
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Service Name");
            headerRow.createCell(1).setCellValue("Count");
            headerRow.createCell(2).setCellValue("Revenue");
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }
            
            for (RevenueExpensesResponse.TopService service : data.getRevenue().getTopServices()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(service.getServiceName());
                row.createCell(1).setCellValue(service.getCount());
                Cell revenueCell = row.createCell(2);
                revenueCell.setCellValue(service.getRevenue().doubleValue());
                revenueCell.setCellStyle(currencyStyle);
            }
            rowNum++;
        }
        
        // Expense Section
        createSectionHeader(sheet, rowNum++, "Expense Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Expenses", data.getExpenses().getTotal(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Service Consumption", data.getExpenses().getByType().getServiceConsumption(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Damaged Items", data.getExpenses().getByType().getDamaged(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Expired Items", data.getExpenses().getByType().getExpired(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Other Expenses", data.getExpenses().getByType().getOther(), currencyStyle);
        rowNum++;
        
        // Top Items
        if (!data.getExpenses().getTopItems().isEmpty()) {
            createSectionHeader(sheet, rowNum++, "Top Exported Items", headerStyle);
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Item Name");
            headerRow.createCell(1).setCellValue("Quantity");
            headerRow.createCell(2).setCellValue("Value");
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }
            
            for (RevenueExpensesResponse.TopItem item : data.getExpenses().getTopItems()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getItemName());
                row.createCell(1).setCellValue(item.getQuantity());
                Cell valueCell = row.createCell(2);
                valueCell.setCellValue(item.getValue().doubleValue());
                valueCell.setCellStyle(currencyStyle);
            }
        }
        
        autoSizeColumns(sheet, 3);
    }

    private void exportEmployees(Workbook workbook, String month) {
        EmployeeStatisticsResponse data = employeeService.getEmployeeStatistics(month, 10);
        Sheet sheet = workbook.createSheet("Employees");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Employee Statistics - " + month);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        rowNum++;
        
        // Top Doctors
        createSectionHeader(sheet, rowNum++, "Top Doctors by Revenue", headerStyle);
        Row doctorHeaderRow = sheet.createRow(rowNum++);
        doctorHeaderRow.createCell(0).setCellValue("Doctor Name");
        doctorHeaderRow.createCell(1).setCellValue("Revenue");
        doctorHeaderRow.createCell(2).setCellValue("Appointments");
        doctorHeaderRow.createCell(3).setCellValue("Patients");
        for (Cell cell : doctorHeaderRow) {
            cell.setCellStyle(headerStyle);
        }
        
        for (EmployeeStatisticsResponse.DoctorPerformance doctor : data.getTopDoctors()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(doctor.getFullName());
            Cell revenueCell = row.createCell(1);
            revenueCell.setCellValue(doctor.getTotalRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);
            row.createCell(2).setCellValue(doctor.getAppointmentCount());
            row.createCell(3).setCellValue(doctor.getServiceCount());
        }
        rowNum++;
        
        // Time Off Stats
        createSectionHeader(sheet, rowNum++, "Time-Off Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Requests", data.getTimeOff().getTotalRequests());
        rowNum = addDataRow(sheet, rowNum, "Total Days", data.getTimeOff().getTotalDays());
        rowNum++;
        
        createSectionHeader(sheet, rowNum++, "By Type", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Paid Leave", data.getTimeOff().getByType().getPaidLeave().getDays());
        rowNum = addDataRow(sheet, rowNum, "Sick Leave", data.getTimeOff().getByType().getSickLeave().getDays());
        rowNum = addDataRow(sheet, rowNum, "Emergency Leave", data.getTimeOff().getByType().getEmergencyLeave().getDays());
        rowNum = addDataRow(sheet, rowNum, "Unpaid Leave", data.getTimeOff().getByType().getUnpaidLeave().getDays());
        rowNum = addDataRow(sheet, rowNum, "Other", data.getTimeOff().getByType().getOther().getDays());
        rowNum++;
        
        createSectionHeader(sheet, rowNum++, "By Status", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Pending", data.getTimeOff().getByStatus().getPending());
        rowNum = addDataRow(sheet, rowNum, "Approved", data.getTimeOff().getByStatus().getApproved());
        rowNum = addDataRow(sheet, rowNum, "Rejected", data.getTimeOff().getByStatus().getRejected());
        rowNum = addDataRow(sheet, rowNum, "Cancelled", data.getTimeOff().getByStatus().getCancelled());
        
        autoSizeColumns(sheet, 4);
    }

    private void exportWarehouse(Workbook workbook, String month) {
        WarehouseStatisticsResponse data = warehouseService.getWarehouseStatistics(month);
        Sheet sheet = workbook.createSheet("Warehouse");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Warehouse Statistics - " + month);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        rowNum++;
        
        // Transaction Stats
        createSectionHeader(sheet, rowNum++, "Transaction Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Transactions", data.getTransactions().getTotal());
        rowNum = addDataRow(sheet, rowNum, "Import Count", data.getTransactions().getImportData().getCount());
        rowNum = addDataRow(sheet, rowNum, "Import Value", data.getTransactions().getImportData().getTotalValue(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Export Count", data.getTransactions().getExportData().getCount());
        rowNum = addDataRow(sheet, rowNum, "Export Value", data.getTransactions().getExportData().getTotalValue(), currencyStyle);
        rowNum++;
        
        // Inventory Stats
        createSectionHeader(sheet, rowNum++, "Inventory Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Current Total Value", data.getInventory().getCurrentTotalValue(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Low Stock Items", data.getInventory().getLowStockItems());
        rowNum = addDataRow(sheet, rowNum, "Expiring Items (30 days)", data.getInventory().getExpiringItems());
        rowNum++;
        
        // Top Imports
        if (!data.getTopImports().isEmpty()) {
            createSectionHeader(sheet, rowNum++, "Top Imported Items", headerStyle);
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Item Name");
            headerRow.createCell(1).setCellValue("Quantity");
            headerRow.createCell(2).setCellValue("Value");
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }
            
            for (WarehouseStatisticsResponse.TopItem item : data.getTopImports()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getItemName());
                row.createCell(1).setCellValue(item.getQuantity());
                Cell valueCell = row.createCell(2);
                valueCell.setCellValue(item.getValue().doubleValue());
                valueCell.setCellStyle(currencyStyle);
            }
            rowNum++;
        }
        
        // Top Exports
        if (!data.getTopExports().isEmpty()) {
            createSectionHeader(sheet, rowNum++, "Top Exported Items", headerStyle);
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Item Name");
            headerRow.createCell(1).setCellValue("Quantity");
            headerRow.createCell(2).setCellValue("Value");
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }
            
            for (WarehouseStatisticsResponse.TopItem item : data.getTopExports()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getItemName());
                row.createCell(1).setCellValue(item.getQuantity());
                Cell valueCell = row.createCell(2);
                valueCell.setCellValue(item.getValue().doubleValue());
                valueCell.setCellStyle(currencyStyle);
            }
        }
        
        autoSizeColumns(sheet, 3);
    }

    private void exportTransactions(Workbook workbook, String month) {
        TransactionStatisticsResponse data = transactionService.getTransactionStatistics(month);
        Sheet sheet = workbook.createSheet("Transactions");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle percentStyle = createPercentStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Transaction Statistics - " + month);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        rowNum++;
        
        // Invoice Stats
        createSectionHeader(sheet, rowNum++, "Invoice Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Invoices", data.getInvoices().getTotal());
        rowNum = addDataRow(sheet, rowNum, "Total Value", data.getInvoices().getTotalValue(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Payment Rate", data.getInvoices().getPaymentRate(), percentStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Debt", data.getInvoices().getDebt(), currencyStyle);
        rowNum++;
        
        createSectionHeader(sheet, rowNum++, "By Status", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Pending Payment", data.getInvoices().getByStatus().getPendingPayment().getCount());
        rowNum = addDataRow(sheet, rowNum, "Partial Paid", data.getInvoices().getByStatus().getPartialPaid().getCount());
        rowNum = addDataRow(sheet, rowNum, "Paid", data.getInvoices().getByStatus().getPaid().getCount());
        rowNum = addDataRow(sheet, rowNum, "Cancelled", data.getInvoices().getByStatus().getCancelled().getCount());
        rowNum++;
        
        createSectionHeader(sheet, rowNum++, "By Type", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Appointment", data.getInvoices().getByType().getAppointment().getCount());
        rowNum = addDataRow(sheet, rowNum, "Treatment Plan", data.getInvoices().getByType().getTreatmentPlan().getCount());
        rowNum = addDataRow(sheet, rowNum, "Supplemental", data.getInvoices().getByType().getSupplemental().getCount());
        rowNum++;
        
        // Payment Stats
        createSectionHeader(sheet, rowNum++, "Payment Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Payments", data.getPayments().getTotal());
        rowNum = addDataRow(sheet, rowNum, "Total Value", data.getPayments().getTotalValue(), currencyStyle);
        rowNum++;
        
        createSectionHeader(sheet, rowNum++, "By Method", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Bank Transfer (SEPAY)", data.getPayments().getByMethod().getBankTransfer().getCount());
        rowNum = addDataRow(sheet, rowNum, "Cash", data.getPayments().getByMethod().getCash().getCount());
        rowNum = addDataRow(sheet, rowNum, "Card", data.getPayments().getByMethod().getCard().getCount());
        rowNum = addDataRow(sheet, rowNum, "Other", data.getPayments().getByMethod().getOther().getCount());
        
        autoSizeColumns(sheet, 2);
    }

    // Helper methods for styling and formatting
    
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        return style;
    }

    private void createSectionHeader(Sheet sheet, int rowNum, String title, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
    }

    private int addDataRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        
        if (value instanceof Number) {
            valueCell.setCellValue(((Number) value).doubleValue());
        } else {
            valueCell.setCellValue(value != null ? value.toString() : "");
        }
        
        return rowNum + 1;
    }

    private int addDataRow(Sheet sheet, int rowNum, String label, BigDecimal value, CellStyle currencyStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value != null ? value.doubleValue() : 0.0);
        valueCell.setCellStyle(currencyStyle);
        return rowNum + 1;
    }

    private int addDataRow(Sheet sheet, int rowNum, String label, Double value, CellStyle percentStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value != null ? value / 100.0 : 0.0);
        valueCell.setCellStyle(percentStyle);
        return rowNum + 1;
    }

    private void autoSizeColumns(Sheet sheet, int numColumns) {
        for (int i = 0; i < numColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
