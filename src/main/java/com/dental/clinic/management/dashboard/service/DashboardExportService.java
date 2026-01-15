package com.dental.clinic.management.dashboard.service;

import com.dental.clinic.management.dashboard.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardExportService {

    private final DashboardService dashboardService;
    private final DashboardRevenueService revenueService;
    private final DashboardEmployeeService employeeService;
    private final DashboardWarehouseService warehouseService;
    private final DashboardTransactionService transactionService;
    private final com.dental.clinic.management.feedback.service.AppointmentFeedbackService feedbackService;

    public byte[] exportToExcel(String tab, String month, LocalDate startDate, LocalDate endDate) {
        log.info("Exporting tab: {} - month: {}, startDate: {}, endDate: {}", tab, month, startDate, endDate);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            switch (tab.toLowerCase()) {
                case "overview" -> exportOverview(workbook, month, startDate, endDate);
                case "revenue-expenses" -> exportRevenueExpenses(workbook, month, startDate, endDate);
                case "employees" -> exportEmployees(workbook, month, startDate, endDate);
                case "warehouse" -> exportWarehouse(workbook, month, startDate, endDate);
                case "transactions" -> exportTransactions(workbook, month, startDate, endDate);
                case "feedbacks" -> exportFeedbacks(workbook, month, startDate, endDate);
                default -> throw new IllegalArgumentException("Invalid tab: " + tab);
            }
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("Error exporting dashboard to Excel", e);
            throw new RuntimeException("Failed to export dashboard to Excel", e);
        }
    }

    private void exportOverview(Workbook workbook, String month, LocalDate startDate, LocalDate endDate) {
        DashboardOverviewResponse data = dashboardService.getOverviewStatistics(month, startDate, endDate, false, null, null, null, null);
        Sheet sheet = workbook.createSheet("Overview");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        @SuppressWarnings("unused")
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
        rowNum++;
        
        // Invoice Stats
        createSectionHeader(sheet, rowNum++, "Invoice Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Invoices", data.getInvoices().getTotal());
        rowNum = addDataRow(sheet, rowNum, "Paid Invoices", data.getInvoices().getPaid());
        rowNum = addDataRow(sheet, rowNum, "Pending Invoices", data.getInvoices().getPending());
        rowNum = addDataRow(sheet, rowNum, "Overdue Invoices", data.getInvoices().getOverdue());
        rowNum = addDataRow(sheet, rowNum, "Total Amount", data.getInvoices().getTotalAmount(), currencyStyle);
        rowNum = addDataRow(sheet, rowNum, "Paid Amount", data.getInvoices().getPaidAmount(), currencyStyle);
        rowNum++;
        
        // Appointment Stats
        createSectionHeader(sheet, rowNum++, "Appointment Statistics", headerStyle);
        rowNum = addDataRow(sheet, rowNum, "Total Appointments", data.getAppointments().getTotal());
        rowNum = addDataRow(sheet, rowNum, "Scheduled", data.getAppointments().getScheduled());
        rowNum = addDataRow(sheet, rowNum, "Completed", data.getAppointments().getCompleted());
        rowNum = addDataRow(sheet, rowNum, "Cancelled", data.getAppointments().getCancelled());
        
        autoSizeColumns(sheet, 2);
    }

    private void exportRevenueExpenses(Workbook workbook, String month, LocalDate startDate, LocalDate endDate) {
        RevenueExpensesResponse data = revenueService.getRevenueExpensesStatistics(month, startDate, endDate, false, null);
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

    private void exportEmployees(Workbook workbook, String month, LocalDate startDate, LocalDate endDate) {
        EmployeeStatisticsResponse data = employeeService.getEmployeeStatistics(month, startDate, endDate, 10);
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

    private void exportWarehouse(Workbook workbook, String month, LocalDate startDate, LocalDate endDate) {
        WarehouseStatisticsResponse data = warehouseService.getWarehouseStatistics(month, startDate, endDate);
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

    private void exportTransactions(Workbook workbook, String month, LocalDate startDate, LocalDate endDate) {
        TransactionStatisticsResponse data = transactionService.getTransactionStatistics(month, startDate, endDate);
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

    private void exportFeedbacks(Workbook workbook, String month, LocalDate startDate, LocalDate endDate) {
        com.dental.clinic.management.feedback.dto.DoctorFeedbackStatisticsResponse data = 
            feedbackService.getStatisticsByDoctor(startDate, endDate, 10, "rating");
        Sheet sheet = workbook.createSheet("Feedbacks");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        String dateRange = month != null ? month : 
            (startDate != null && endDate != null ? startDate + " to " + endDate : "All Time");
        titleCell.setCellValue("Feedback & Ratings Statistics - " + dateRange);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        rowNum++;
        
        // Overall Statistics
        createSectionHeader(sheet, rowNum++, "Overall Statistics", headerStyle);
        long totalDoctors = data.getDoctors().size();
        double avgRating = data.getDoctors().stream()
            .mapToDouble(d -> d.getStatistics().getAverageRating())
            .average().orElse(0.0);
        long totalFeedbacks = data.getDoctors().stream()
            .mapToLong(d -> d.getStatistics().getTotalFeedbacks())
            .sum();
        long topRatedCount = data.getDoctors().stream()
            .filter(d -> d.getStatistics().getAverageRating() >= 4.5)
            .count();
        
        rowNum = addDataRow(sheet, rowNum, "Total Doctors", totalDoctors);
        rowNum = addDataRow(sheet, rowNum, "Average Rating", Math.round(avgRating * 100.0) / 100.0);
        rowNum = addDataRow(sheet, rowNum, "Total Feedbacks", totalFeedbacks);
        rowNum = addDataRow(sheet, rowNum, "Top Rated Doctors (≥4.5)", topRatedCount);
        rowNum++;
        
        // Top Doctors by Rating
        if (!data.getDoctors().isEmpty()) {
            createSectionHeader(sheet, rowNum++, "Top Doctors by Rating", headerStyle);
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Rank");
            headerRow.createCell(1).setCellValue("Employee Code");
            headerRow.createCell(2).setCellValue("Doctor Name");
            headerRow.createCell(3).setCellValue("Specialization");
            headerRow.createCell(4).setCellValue("Average Rating");
            headerRow.createCell(5).setCellValue("Total Feedbacks");
            headerRow.createCell(6).setCellValue("5-Star");
            headerRow.createCell(7).setCellValue("4-Star");
            headerRow.createCell(8).setCellValue("3-Star");
            headerRow.createCell(9).setCellValue("2-Star");
            headerRow.createCell(10).setCellValue("1-Star");
            
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }
            
            int rank = 1;
            for (com.dental.clinic.management.feedback.dto.DoctorFeedbackStatisticsResponse.DoctorStatistics doctor : data.getDoctors()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rank++);
                row.createCell(1).setCellValue(doctor.getEmployeeCode());
                row.createCell(2).setCellValue(doctor.getEmployeeName());
                row.createCell(3).setCellValue(doctor.getSpecialization());
                
                Cell ratingCell = row.createCell(4);
                ratingCell.setCellValue(doctor.getStatistics().getAverageRating());
                
                row.createCell(5).setCellValue(doctor.getStatistics().getTotalFeedbacks());
                
                // Rating distribution
                java.util.Map<String, Long> dist = doctor.getStatistics().getRatingDistribution();
                row.createCell(6).setCellValue(dist.getOrDefault("5", 0L));
                row.createCell(7).setCellValue(dist.getOrDefault("4", 0L));
                row.createCell(8).setCellValue(dist.getOrDefault("3", 0L));
                row.createCell(9).setCellValue(dist.getOrDefault("2", 0L));
                row.createCell(10).setCellValue(dist.getOrDefault("1", 0L));
            }
        }
        
        autoSizeColumns(sheet, 11);
    }

    private void exportFeedbacksCSV(CSVPrinter csv, String month, LocalDate startDate, LocalDate endDate) throws IOException {
        com.dental.clinic.management.feedback.dto.DoctorFeedbackStatisticsResponse data = 
            feedbackService.getStatisticsByDoctor(startDate, endDate, 10, "rating");
        
        String dateRange = month != null ? month : 
            (startDate != null && endDate != null ? startDate + " to " + endDate : "All Time");
        csv.printRecord("Feedback & Ratings Statistics", dateRange);
        csv.println();
        
        // Overall Statistics
        long totalDoctors = data.getDoctors().size();
        double avgRating = data.getDoctors().stream()
            .mapToDouble(d -> d.getStatistics().getAverageRating())
            .average().orElse(0.0);
        long totalFeedbacks = data.getDoctors().stream()
            .mapToLong(d -> d.getStatistics().getTotalFeedbacks())
            .sum();
        long topRatedCount = data.getDoctors().stream()
            .filter(d -> d.getStatistics().getAverageRating() >= 4.5)
            .count();
        
        csv.printRecord("Overall Statistics");
        csv.printRecord("Total Doctors", totalDoctors);
        csv.printRecord("Average Rating", String.format("%.2f", avgRating));
        csv.printRecord("Total Feedbacks", totalFeedbacks);
        csv.printRecord("Top Rated Doctors (≥4.5)", topRatedCount);
        csv.println();
        
        // Top Doctors by Rating
        csv.printRecord("Top Doctors by Rating");
        csv.printRecord("Rank", "Employee Code", "Doctor Name", "Specialization", "Avg Rating", 
                       "Total Feedbacks", "5-Star", "4-Star", "3-Star", "2-Star", "1-Star");
        
        int rank = 1;
        for (com.dental.clinic.management.feedback.dto.DoctorFeedbackStatisticsResponse.DoctorStatistics doctor : data.getDoctors()) {
            java.util.Map<String, Long> dist = doctor.getStatistics().getRatingDistribution();
            csv.printRecord(
                rank++,
                doctor.getEmployeeCode(),
                doctor.getEmployeeName(),
                doctor.getSpecialization(),
                String.format("%.2f", doctor.getStatistics().getAverageRating()),
                doctor.getStatistics().getTotalFeedbacks(),
                dist.getOrDefault("5", 0L),
                dist.getOrDefault("4", 0L),
                dist.getOrDefault("3", 0L),
                dist.getOrDefault("2", 0L),
                dist.getOrDefault("1", 0L)
            );
        }
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

    /**
     * Export all tabs to a single Excel file
     */
    public byte[] exportAllTabs(String month, LocalDate startDate, LocalDate endDate) {
        log.info("Exporting all dashboard tabs - month: {}, startDate: {}, endDate: {}", month, startDate, endDate);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // Export all 6 tabs to one workbook
            exportOverview(workbook, month, startDate, endDate);
            exportRevenueExpenses(workbook, month, startDate, endDate);
            exportEmployees(workbook, month, startDate, endDate);
            exportWarehouse(workbook, month, startDate, endDate);
            exportTransactions(workbook, month, startDate, endDate);
            exportFeedbacks(workbook, month, startDate, endDate);
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("Error exporting all dashboard tabs to Excel", e);
            throw new RuntimeException("Failed to export all dashboard tabs to Excel", e);
        }
    }

    /**
     * Export dashboard data to CSV format
     */
    public String exportToCSV(String tab, String month, LocalDate startDate, LocalDate endDate) {
        log.info("Exporting tab: {} to CSV - month: {}, startDate: {}, endDate: {}", tab, month, startDate, endDate);
        
        try (StringWriter sw = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            
            switch (tab.toLowerCase()) {
                case "overview" -> exportOverviewCSV(csvPrinter, month, startDate, endDate);
                case "revenue-expenses" -> exportRevenueExpensesCSV(csvPrinter, month, startDate, endDate);
                case "employees" -> exportEmployeesCSV(csvPrinter, month, startDate, endDate);
                case "warehouse" -> exportWarehouseCSV(csvPrinter, month, startDate, endDate);
                case "transactions" -> exportTransactionsCSV(csvPrinter, month, startDate, endDate);
                case "feedbacks" -> exportFeedbacksCSV(csvPrinter, month, startDate, endDate);
                default -> throw new IllegalArgumentException("Invalid tab: " + tab);
            }
            
            csvPrinter.flush();
            return sw.toString();
            
        } catch (IOException e) {
            log.error("Error exporting dashboard to CSV", e);
            throw new RuntimeException("Failed to export dashboard to CSV", e);
        }
    }

    private void exportOverviewCSV(CSVPrinter csv, String month, LocalDate startDate, LocalDate endDate) throws IOException {
        DashboardOverviewResponse data = dashboardService.getOverviewStatistics(month, startDate, endDate, false, null, null, null, null);
        
        csv.printRecord("Dashboard Overview", month);
        csv.println();
        
        csv.printRecord("Summary Statistics");
        csv.printRecord("Total Revenue", data.getSummary().getTotalRevenue());
        csv.printRecord("Total Expenses", data.getSummary().getTotalExpenses());
        csv.printRecord("Net Profit", data.getSummary().getNetProfit());
        csv.printRecord("Total Invoices", data.getSummary().getTotalInvoices());
        csv.printRecord("Total Appointments", data.getSummary().getTotalAppointments());
        csv.printRecord("Total Patients", data.getSummary().getTotalPatients());
        csv.println();
        
        csv.printRecord("Invoice Statistics");
        csv.printRecord("Total", data.getInvoices().getTotal());
        csv.printRecord("Paid", data.getInvoices().getPaid());
        csv.printRecord("Pending", data.getInvoices().getPending());
        csv.printRecord("Overdue", data.getInvoices().getOverdue());
        csv.printRecord("Total Amount", data.getInvoices().getTotalAmount());
        csv.printRecord("Paid Amount", data.getInvoices().getPaidAmount());
    }

    private void exportRevenueExpensesCSV(CSVPrinter csv, String month, LocalDate startDate, LocalDate endDate) throws IOException {
        RevenueExpensesResponse data = revenueService.getRevenueExpensesStatistics(month, startDate, endDate, false, null);
        
        csv.printRecord("Revenue & Expenses", month);
        csv.println();
        
        csv.printRecord("Revenue Statistics");
        csv.printRecord("Total Revenue", data.getRevenue().getTotal());
        csv.printRecord("Appointment Revenue", data.getRevenue().getByType().getAppointment());
        csv.printRecord("Treatment Plan Revenue", data.getRevenue().getByType().getTreatmentPlan());
        csv.println();
        
        csv.printRecord("Expense Statistics");
        csv.printRecord("Total Expenses", data.getExpenses().getTotal());
    }

    private void exportEmployeesCSV(CSVPrinter csv, String month, LocalDate startDate, LocalDate endDate) throws IOException {
        EmployeeStatisticsResponse data = employeeService.getEmployeeStatistics(month, startDate, endDate, 10);
        
        csv.printRecord("Employee Statistics", month);
        csv.println();
        
        csv.printRecord("Top Doctors");
        csv.printRecord("Employee Code", "Full Name", "Appointments", "Revenue", "Avg Revenue/Apt");
        for (var doctor : data.getTopDoctors()) {
            csv.printRecord(doctor.getEmployeeCode(), doctor.getFullName(), 
                doctor.getAppointmentCount(), doctor.getTotalRevenue(), 
                doctor.getAverageRevenuePerAppointment());
        }
    }

    private void exportWarehouseCSV(CSVPrinter csv, String month, LocalDate startDate, LocalDate endDate) throws IOException {
        WarehouseStatisticsResponse data = warehouseService.getWarehouseStatistics(month, startDate, endDate);
        
        csv.printRecord("Warehouse Statistics", month);
        csv.println();
        
        csv.printRecord("Transaction Statistics");
        csv.printRecord("Total Transactions", data.getTransactions().getTotal());
        csv.println();
        
        csv.printRecord("Import Data");
        csv.printRecord("Import Count", data.getTransactions().getImportData().getCount());
        csv.printRecord("Import Value", data.getTransactions().getImportData().getTotalValue());
        csv.println();
        
        csv.printRecord("Export Data");
        csv.printRecord("Export Count", data.getTransactions().getExportData().getCount());
        csv.printRecord("Export Value", data.getTransactions().getExportData().getTotalValue());
        csv.println();
        
        csv.printRecord("Transactions by Status");
        csv.printRecord("Pending", data.getTransactions().getByStatus().getPending());
        csv.printRecord("Approved", data.getTransactions().getByStatus().getApproved());
        csv.printRecord("Rejected", data.getTransactions().getByStatus().getRejected());
        csv.printRecord("Cancelled", data.getTransactions().getByStatus().getCancelled());
        csv.println();
        
        csv.printRecord("Inventory Statistics");
        csv.printRecord("Current Total Value", data.getInventory().getCurrentTotalValue());
        csv.printRecord("Low Stock Items", data.getInventory().getLowStockItems());
        csv.printRecord("Expiring Items", data.getInventory().getExpiringItems());
        csv.printRecord("Usage Rate (%)", data.getInventory().getUsageRate());
        csv.println();
        
        csv.printRecord("Top Import Items");
        csv.printRecord("Item ID", "Item Name", "Quantity", "Value");
        for (var item : data.getTopImports()) {
            csv.printRecord(item.getItemId(), item.getItemName(), item.getQuantity(), item.getValue());
        }
        csv.println();
        
        csv.printRecord("Top Export Items");
        csv.printRecord("Item ID", "Item Name", "Quantity", "Value");
        for (var item : data.getTopExports()) {
            csv.printRecord(item.getItemId(), item.getItemName(), item.getQuantity(), item.getValue());
        }
    }

    private void exportTransactionsCSV(CSVPrinter csv, String month, LocalDate startDate, LocalDate endDate) throws IOException {
        TransactionStatisticsResponse data = transactionService.getTransactionStatistics(month, startDate, endDate);
        
        csv.printRecord("Transaction Statistics", month);
        csv.println();
        
        csv.printRecord("Invoice Statistics");
        csv.printRecord("Total Invoices", data.getInvoices().getTotal());
        csv.printRecord("Total Value", data.getInvoices().getTotalValue());
        csv.printRecord("Payment Rate (%)", data.getInvoices().getPaymentRate());
        csv.printRecord("Total Debt", data.getInvoices().getDebt());
        csv.println();
        
        csv.printRecord("Invoices by Status");
        csv.printRecord("Pending Payment - Count", data.getInvoices().getByStatus().getPendingPayment().getCount());
        csv.printRecord("Pending Payment - Value", data.getInvoices().getByStatus().getPendingPayment().getValue());
        csv.printRecord("Partial Paid - Count", data.getInvoices().getByStatus().getPartialPaid().getCount());
        csv.printRecord("Partial Paid - Value", data.getInvoices().getByStatus().getPartialPaid().getValue());
        csv.printRecord("Paid - Count", data.getInvoices().getByStatus().getPaid().getCount());
        csv.printRecord("Paid - Value", data.getInvoices().getByStatus().getPaid().getValue());
        csv.printRecord("Cancelled - Count", data.getInvoices().getByStatus().getCancelled().getCount());
        csv.printRecord("Cancelled - Value", data.getInvoices().getByStatus().getCancelled().getValue());
        csv.println();
        
        csv.printRecord("Invoices by Type");
        csv.printRecord("Appointment - Count", data.getInvoices().getByType().getAppointment().getCount());
        csv.printRecord("Appointment - Value", data.getInvoices().getByType().getAppointment().getValue());
        csv.printRecord("Treatment Plan - Count", data.getInvoices().getByType().getTreatmentPlan().getCount());
        csv.printRecord("Treatment Plan - Value", data.getInvoices().getByType().getTreatmentPlan().getValue());
        csv.printRecord("Supplemental - Count", data.getInvoices().getByType().getSupplemental().getCount());
        csv.printRecord("Supplemental - Value", data.getInvoices().getByType().getSupplemental().getValue());
        csv.println();
        
        csv.printRecord("Payment Statistics");
        csv.printRecord("Total Payments", data.getPayments().getTotal());
        csv.printRecord("Total Value", data.getPayments().getTotalValue());
        csv.println();
        
        csv.printRecord("Payments by Method");
        csv.printRecord("Bank Transfer - Count", data.getPayments().getByMethod().getBankTransfer().getCount());
        csv.printRecord("Bank Transfer - Value", data.getPayments().getByMethod().getBankTransfer().getValue());
        csv.printRecord("Cash - Count", data.getPayments().getByMethod().getCash().getCount());
        csv.printRecord("Cash - Value", data.getPayments().getByMethod().getCash().getValue());
        csv.printRecord("Card - Count", data.getPayments().getByMethod().getCard().getCount());
        csv.printRecord("Card - Value", data.getPayments().getByMethod().getCard().getValue());
        csv.printRecord("Other - Count", data.getPayments().getByMethod().getOther().getCount());
        csv.printRecord("Other - Value", data.getPayments().getByMethod().getOther().getValue());
    }
}
