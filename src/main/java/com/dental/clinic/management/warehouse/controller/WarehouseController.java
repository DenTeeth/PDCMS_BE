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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v3/warehouse")
@Tag(name = "Warehouse - Transactions", description = "Quản lý xuất nhập kho (FEFO)")
public class WarehouseController {

    // TODO: Inject WarehouseService when created
    // private final WarehouseService warehouseService;
    //
    // public WarehouseController(WarehouseService warehouseService) {
    // this.warehouseService = warehouseService;
    // }

    @PostMapping("/import")
    @Operation(summary = "Nhập kho", description = "Tạo phiếu nhập kho (tạo mới hoặc cập nhật batch)")
    public ResponseEntity<StorageTransactionResponse> createImportTransaction(
            @Valid @RequestBody CreateImportTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/export")
    @Operation(summary = "Xuất kho", description = "Tạo phiếu xuất kho (FEFO - lấy hàng theo ngày sản xuất)")
    public ResponseEntity<StorageTransactionResponse> createExportTransaction(
            @Valid @RequestBody CreateExportTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/adjust")
    @Operation(summary = "Điều chỉnh tồn kho", description = "Điều chỉnh số lượng hàng tồn (ADJUSTMENT/DESTROY)")
    public ResponseEntity<StorageTransactionResponse> createAdjustment(
            @Valid @RequestBody CreateAdjustmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Lấy thông tin giao dịch", description = "Lấy chi tiết giao dịch kho theo ID")
    public ResponseEntity<StorageTransactionResponse> getTransactionById(@PathVariable Long transactionId) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions/history")
    @Operation(summary = "Lịch sử giao dịch", description = "Lấy lịch sử giao dịch kho theo khoảng thời gian")
    public ResponseEntity<List<StorageTransactionResponse>> getTransactionHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/batches/item/{itemId}")
    @Operation(summary = "Lấy danh sách batch theo vật tư", description = "Lấy danh sách batch của vật tư (FEFO order)")
    public ResponseEntity<List<ItemBatchResponse>> getBatchesByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/batches/expiring")
    @Operation(summary = "Lấy danh sách batch sắp hết hạn", description = "Lấy danh sách batch sắp hết hạn (30 ngày)")
    public ResponseEntity<List<ItemBatchResponse>> getExpiringBatches() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/inventory")
    @Operation(summary = "Thống kê tồn kho", description = "Thống kê tồn kho theo danh mục")
    public ResponseEntity<InventoryStatsResponse> getInventoryStats() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/storage")
    @Operation(summary = "Thống kê xuất nhập kho", description = "Thống kê xuất nhập kho theo khoảng thời gian")
    public ResponseEntity<StorageStatsResponse> getStorageStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports/loss")
    @Operation(summary = "Báo cáo cáo hao hụt", description = "Báo cáo cáo hao hụt vật tư theo thời gian ADJUSTMENT + DESTROY")
    public ResponseEntity<List<StorageTransactionResponse>> getLossReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok().build();
    }
}
