package com.dental.clinic.management.dashboard.controller;

import com.dental.clinic.management.dashboard.dto.*;
import com.dental.clinic.management.dashboard.service.DashboardExportService;
import com.dental.clinic.management.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard Statistics API for Admin and Manager")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardExportService dashboardExportService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @Operation(summary = "Get dashboard overview statistics", 
               description = "Get all overview statistics including revenue, expenses, invoices, and appointments")
    public ResponseEntity<DashboardOverviewResponse> getOverview(
            @RequestParam String month,
            @RequestParam(required = false, defaultValue = "false") Boolean compareWithPrevious) {
        DashboardOverviewResponse response = dashboardService.getOverviewStatistics(month, compareWithPrevious);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/revenue-expenses")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @Operation(summary = "Get revenue and expenses statistics",
               description = "Get detailed revenue and expenses statistics with daily breakdown and top services/items")
    public ResponseEntity<RevenueExpensesResponse> getRevenueExpenses(
            @RequestParam String month,
            @RequestParam(required = false, defaultValue = "false") Boolean compareWithPrevious) {
        RevenueExpensesResponse response = dashboardService.getRevenueExpensesStatistics(month, compareWithPrevious);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @Operation(summary = "Get employee statistics",
               description = "Get employee statistics including top doctors performance and time-off data")
    public ResponseEntity<EmployeeStatisticsResponse> getEmployeeStatistics(
            @RequestParam String month,
            @RequestParam(required = false, defaultValue = "10") Integer topDoctors) {
        EmployeeStatisticsResponse response = dashboardService.getEmployeeStatistics(month, topDoctors);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/warehouse")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @Operation(summary = "Get warehouse statistics",
               description = "Get warehouse statistics including transactions, inventory, and top items")
    public ResponseEntity<WarehouseStatisticsResponse> getWarehouseStatistics(
            @RequestParam String month) {
        WarehouseStatisticsResponse response = dashboardService.getWarehouseStatistics(month);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @Operation(summary = "Get transaction statistics",
               description = "Get transaction statistics including invoices and payments")
    public ResponseEntity<TransactionStatisticsResponse> getTransactionStatistics(
            @RequestParam String month) {
        TransactionStatisticsResponse response = dashboardService.getTransactionStatistics(month);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/{tab}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @Operation(summary = "Export dashboard statistics to Excel",
               description = "Export specific tab statistics to Excel file. Available tabs: overview, revenue-expenses, employees, warehouse, transactions")
    public ResponseEntity<byte[]> exportToExcel(
            @PathVariable String tab,
            @RequestParam String month) {
        byte[] excelFile = dashboardExportService.exportToExcel(tab, month);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", String.format("dashboard-%s-%s.xlsx", tab, month));
        
        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }
}
