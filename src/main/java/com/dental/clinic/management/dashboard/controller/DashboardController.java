package com.dental.clinic.management.dashboard.controller;

import com.dental.clinic.management.dashboard.dto.*;
import com.dental.clinic.management.dashboard.service.DashboardExportService;
import com.dental.clinic.management.dashboard.service.DashboardService;
import com.dental.clinic.management.dashboard.service.DashboardPreferencesService;
import com.dental.clinic.management.dashboard.service.DashboardSavedViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard Statistics API for Admin and Manager")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardExportService dashboardExportService;
    private final DashboardPreferencesService preferencesService;
    private final DashboardSavedViewService savedViewService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get dashboard overview statistics", 
               description = "Get all overview statistics including revenue, expenses, invoices, and appointments. " +
                            "Supports both month-based (month=2026-01) and date range (startDate=2026-01-01&endDate=2026-01-31) filtering. " +
                            "Comparison modes: MONTH (previous month), QUARTER (previous quarter), YEAR (previous year), NONE (no comparison). " +
                            "Advanced filters: employeeId, patientId, serviceId")
    public ResponseEntity<DashboardOverviewResponse> getOverview(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "false") Boolean compareWithPrevious,
            @RequestParam(required = false) String comparisonMode,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) Integer serviceId) {
        DashboardOverviewResponse response = dashboardService.getOverviewStatistics(
                month, startDate, endDate, compareWithPrevious, comparisonMode, 
                employeeId, patientId, serviceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/revenue-expenses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get revenue and expenses statistics",
               description = "Get detailed revenue and expenses statistics with daily breakdown and top services/items. " +
                            "Supports both month-based and date range filtering. " +
                            "Comparison modes: MONTH (previous month), QUARTER (previous quarter), YEAR (previous year), NONE (no comparison). " +
                            "Advanced filters: employeeId, patientId, serviceId")
    public ResponseEntity<RevenueExpensesResponse> getRevenueExpenses(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "false") Boolean compareWithPrevious,
            @RequestParam(required = false) String comparisonMode,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) Integer serviceId) {
        RevenueExpensesResponse response = dashboardService.getRevenueExpensesStatistics(
                month, startDate, endDate, compareWithPrevious, comparisonMode,
                employeeId, patientId, serviceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get employee statistics",
               description = "Get employee statistics including top doctors performance and time-off data. " +
                            "Supports both month-based and date range filtering. " +
                            "Advanced filters: employeeId, serviceId")
    public ResponseEntity<EmployeeStatisticsResponse> getEmployeeStatistics(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "10") Integer topDoctors,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) Integer serviceId) {
        EmployeeStatisticsResponse response = dashboardService.getEmployeeStatistics(
                month, startDate, endDate, topDoctors, employeeId, serviceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/warehouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get warehouse statistics",
               description = "Get warehouse statistics including transactions, inventory, and top items. " +
                            "Supports both month-based and date range filtering")
    public ResponseEntity<WarehouseStatisticsResponse> getWarehouseStatistics(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        WarehouseStatisticsResponse response = dashboardService.getWarehouseStatistics(month, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get transaction statistics",
               description = "Get transaction statistics including invoices and payments. " +
                            "Supports both month-based and date range filtering")
    public ResponseEntity<TransactionStatisticsResponse> getTransactionStatistics(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        TransactionStatisticsResponse response = dashboardService.getTransactionStatistics(month, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/feedbacks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get feedback statistics",
               description = "Get doctor feedback and rating statistics. " +
                            "Supports date range filtering and sorting by rating or feedback count")
    public ResponseEntity<com.dental.clinic.management.feedback.dto.DoctorFeedbackStatisticsResponse> getFeedbackStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int top,
            @RequestParam(defaultValue = "rating") String sortBy) {
        com.dental.clinic.management.feedback.dto.DoctorFeedbackStatisticsResponse response = 
            dashboardService.getFeedbackStatistics(startDate, endDate, top, sortBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/{tab}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Export dashboard statistics",
               description = "Export specific tab statistics. Available tabs: overview, revenue-expenses, employees, warehouse, transactions, feedbacks, all. " +
                            "Formats: excel (default), csv. Supports both month-based and date range filtering")
    public ResponseEntity<?> exportToExcel(
            @PathVariable String tab,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "excel") String format) {
        
        if ("csv".equalsIgnoreCase(format)) {
            // CSV export
            if ("all".equalsIgnoreCase(tab)) {
                throw new IllegalArgumentException("CSV export does not support 'all' tabs. Please export individual tabs.");
            }
            
            String csvData = dashboardExportService.exportToCSV(tab, month, startDate, endDate);
            String filename = month != null 
                ? String.format("dashboard-%s-%s.csv", tab, month)
                : String.format("dashboard-%s-%s-to-%s.csv", tab, startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } else {
            // Excel export (default)
            byte[] excelFile;
            String filename;
            
            if ("all".equalsIgnoreCase(tab)) {
                // Export all tabs to one workbook
                excelFile = dashboardExportService.exportAllTabs(month, startDate, endDate);
                filename = month != null 
                    ? String.format("dashboard-all-%s.xlsx", month)
                    : String.format("dashboard-all-%s-to-%s.xlsx", startDate, endDate);
            } else {
                // Export single tab
                excelFile = dashboardExportService.exportToExcel(tab, month, startDate, endDate);
                filename = month != null 
                    ? String.format("dashboard-%s-%s.xlsx", tab, month)
                    : String.format("dashboard-%s-%s-to-%s.xlsx", tab, startDate, endDate);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
        }
    }

    @GetMapping("/appointment-heatmap")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get appointment heatmap data",
               description = "Get appointment distribution by day of week and hour. " +
                            "Supports both month-based and date range filtering")
    public ResponseEntity<AppointmentHeatmapResponse> getAppointmentHeatmap(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AppointmentHeatmapResponse response = dashboardService.getAppointmentHeatmap(month, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/preferences")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get user dashboard preferences",
               description = "Get current user's dashboard layout and settings preferences")
    public ResponseEntity<DashboardPreferencesDTO> getUserPreferences(
            @RequestParam Integer userId) {
        DashboardPreferencesDTO preferences = preferencesService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    @PostMapping("/preferences")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Save user dashboard preferences",
               description = "Save or update current user's dashboard layout and settings")
    public ResponseEntity<DashboardPreferencesDTO> saveUserPreferences(
            @RequestParam Integer userId,
            @RequestBody DashboardPreferencesDTO preferences) {
        DashboardPreferencesDTO saved = preferencesService.saveUserPreferences(userId, preferences);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/preferences")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Reset user dashboard preferences",
               description = "Reset current user's dashboard preferences to default")
    public ResponseEntity<Void> resetUserPreferences(
            @RequestParam Integer userId) {
        preferencesService.resetUserPreferences(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/saved-views")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get user's saved dashboard views",
               description = "Get all saved views for the current user including public views")
    public ResponseEntity<List<DashboardSavedViewDTO>> getSavedViews(
            @RequestParam Integer userId) {
        List<DashboardSavedViewDTO> views = savedViewService.getUserViews(userId);
        return ResponseEntity.ok(views);
    }

    @GetMapping("/saved-views/{viewId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get a specific saved view",
               description = "Get saved view details by ID")
    public ResponseEntity<DashboardSavedViewDTO> getSavedView(
            @PathVariable Integer viewId) {
        DashboardSavedViewDTO view = savedViewService.getView(viewId);
        return ResponseEntity.ok(view);
    }

    @GetMapping("/saved-views/default")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get user's default view",
               description = "Get the default saved view for current user")
    public ResponseEntity<DashboardSavedViewDTO> getDefaultView(
            @RequestParam Integer userId) {
        DashboardSavedViewDTO view = savedViewService.getUserDefaultView(userId);
        return ResponseEntity.ok(view);
    }

    @PostMapping("/saved-views")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new saved view",
               description = "Save a new dashboard view with filters")
    public ResponseEntity<DashboardSavedViewDTO> createSavedView(
            @RequestParam Integer userId,
            @RequestBody DashboardSavedViewDTO dto) {
        DashboardSavedViewDTO created = savedViewService.createView(userId, dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/saved-views/{viewId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update a saved view",
               description = "Update an existing saved view")
    public ResponseEntity<DashboardSavedViewDTO> updateSavedView(
            @PathVariable Integer viewId,
            @RequestBody DashboardSavedViewDTO dto) {
        DashboardSavedViewDTO updated = savedViewService.updateView(viewId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/saved-views/{viewId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a saved view",
               description = "Delete a saved view by ID")
    public ResponseEntity<Void> deleteSavedView(
            @PathVariable Integer viewId) {
        savedViewService.deleteView(viewId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/saved-views/{viewId}/set-default")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Set view as default",
               description = "Set a saved view as user's default")
    public ResponseEntity<DashboardSavedViewDTO> setDefaultView(
            @RequestParam Integer userId,
            @PathVariable Integer viewId) {
        DashboardSavedViewDTO view = savedViewService.setAsDefault(userId, viewId);
        return ResponseEntity.ok(view);
    }
}
