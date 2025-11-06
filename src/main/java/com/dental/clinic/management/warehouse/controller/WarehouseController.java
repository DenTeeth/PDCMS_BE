package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.warehouse.dto.request.CreateAdjustmentRequest;
import com.dental.clinic.management.warehouse.dto.request.CreateExportTransactionRequest;
import com.dental.clinic.management.warehouse.dto.request.CreateImportTransactionRequest;
import com.dental.clinic.management.warehouse.dto.response.InventoryStatsResponse;
import com.dental.clinic.management.warehouse.dto.response.ItemBatchResponse;
import com.dental.clinic.management.warehouse.dto.response.StorageStatsResponse;
import com.dental.clinic.management.warehouse.dto.response.StorageTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3/warehouse")
@RequiredArgsConstructor
@Tag(name = "Warehouse - Transactions", description = "Quản lý xuất nhập kho (FEFO)")
public class WarehouseController {

    // TODO: Inject WarehouseService when created

    @PostMapping("/import")
    @Operation(summary = "Nhập kho", description = "Tạo phiếu nhập kho (tự động tạo/cập nhật batch)")
    public ResponseEntity<StorageTransactionResponse> createImportTransaction(
            @Valid @RequestBody CreateImportTransactionRequest request) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/export")
    @Operation(summary = "Xuất kho", description = "Tạo phiếu xuất kho (FEFO - lô cũ nhất trước)")
    public ResponseEntity<StorageTransactionResponse> createExportTransaction(
            @Valid @RequestBody CreateExportTransactionRequest request) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/adjust")
    @Operation(summary = "Điều chỉnh tồn kho", description = "Điều chỉnh số lượng tồn (ADJUSTMENT/DESTROY)")
    public ResponseEntity<StorageTransactionResponse> createAdjustment(
            @Valid @RequestBody CreateAdjustmentRequest request) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Lấy thông tin giao dịch", description = "Lấy chi tiết giao dịch kho theo ID")
    public ResponseEntity<StorageTransactionResponse> getTransactionById(@PathVariable UUID transactionId) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions/history")
    @Operation(summary = "Lịch sử giao dịch", description = "Lấy lịch sử giao dịch theo khoảng thời gian")
    public ResponseEntity<List<StorageTransactionResponse>> getTransactionHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/batches/item/{itemId}")
    @Operation(summary = "Lấy batch theo vật tư", description = "Lấy danh sách batch của vật tư (FEFO order)")
    public ResponseEntity<List<ItemBatchResponse>> getBatchesByItem(@PathVariable UUID itemId) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/batches/expiring")
    @Operation(summary = "Lấy batch sắp hết hạn", description = "Lấy danh sách batch sắp hết hạn (30 ngày)")
    public ResponseEntity<List<ItemBatchResponse>> getExpiringBatches() {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/inventory")
    @Operation(summary = "Thống kê tồn kho", description = "Tổng quan tồn kho theo danh mục")
    public ResponseEntity<InventoryStatsResponse> getInventoryStats() {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/storage")
    @Operation(summary = "Thống kê xuất nhập", description = "Thống kê giao dịch xuất nhập kho")
    public ResponseEntity<StorageStatsResponse> getStorageStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports/loss")
    @Operation(summary = "Báo cáo hao hụt", description = "Báo cáo vật tư ADJUSTMENT + DESTROY")
    public ResponseEntity<List<StorageTransactionResponse>> getLossReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }
}
