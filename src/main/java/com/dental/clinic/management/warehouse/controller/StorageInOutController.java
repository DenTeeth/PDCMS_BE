package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.utils.annotation.ApiMessage;
import com.dental.clinic.management.warehouse.dto.request.ExportRequest;
import com.dental.clinic.management.warehouse.dto.request.ImportRequest;
import com.dental.clinic.management.warehouse.dto.response.StorageStatsResponse;
import com.dental.clinic.management.warehouse.dto.response.TransactionResponse;
import com.dental.clinic.management.warehouse.enums.TransactionType;
import com.dental.clinic.management.warehouse.service.StorageInOutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  Storage In/Out Controller
 * Quản lý phiếu nhập/xuất kho và thống kê
 */
@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Storage In/Out Management", description = "APIs quản lý nhập/xuất kho")
public class StorageInOutController {

    private final StorageInOutService storageInOutService;

    /**
     * ➕ API: Nhập kho (IMPORT)
     */
    @Operation(summary = "Tạo phiếu nhập kho", description = "Validate: Kho lạnh bắt buộc có HSD. Tự động tạo/cập nhật batch.")
    @ApiMessage("Nhập kho thành công")
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_INVENTORY_MANAGER')")
    public ResponseEntity<TransactionResponse> importItems(
            @Valid @RequestBody ImportRequest request) {
        log.info("POST /api/v1/storage/import - supplier: {}, items: {}",
                request.getSupplierId(), request.getItems().size());
        TransactionResponse response = storageInOutService.importItems(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ➖ API: Xuất kho (EXPORT)
     */
    @Operation(summary = "Tạo phiếu xuất kho", description = "Validate: Kiểm tra số lượng tồn kho. Tự động trừ quantity_on_hand.")
    @ApiMessage("Xuất kho thành công")
    @PostMapping("/export")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_INVENTORY_MANAGER', 'ROLE_DENTIST', 'ROLE_NURSE')")
    public ResponseEntity<TransactionResponse> exportItems(
            @Valid @RequestBody ExportRequest request) {
        log.info("POST /api/v1/storage/export - items: {}", request.getItems().size());
        TransactionResponse response = storageInOutService.exportItems(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     *  API: Lấy thống kê Storage (Import/Export)
     */
    @Operation(summary = "Lấy thống kê xuất/nhập kho", description = "Trả về: Import/Export value, growth percent")
    @ApiMessage("Lấy thống kê storage thành công")
    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_INVENTORY_MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<StorageStatsResponse> getStorageStats(
            @Parameter(description = "Tháng (1-12)") @RequestParam(required = false) Integer month,
            @Parameter(description = "Năm") @RequestParam(required = false) Integer year) {
        log.info("GET /api/v1/storage/stats - month: {}, year: {}", month, year);
        StorageStatsResponse stats = storageInOutService.getStorageStats(month, year);
        return ResponseEntity.ok(stats);
    }

    // ===========================
    //  GET ALL TRANSACTIONS
    // ===========================
    @Operation(summary = "Lấy danh sách phiếu nhập/xuất kho", description = "Lọc theo loại, tháng/năm")
    @ApiMessage("Lấy danh sách transactions thành công")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_INVENTORY_MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @Parameter(description = "Loại giao dịch: IMPORT, EXPORT, ADJUSTMENT, LOSS") @RequestParam(required = false) TransactionType transactionType,
            @Parameter(description = "Tháng (1-12)") @RequestParam(required = false) Integer month,
            @Parameter(description = "Năm") @RequestParam(required = false) Integer year) {
        log.info("GET /api/v1/storage - type: {}, month: {}, year: {}", transactionType, month, year);
        List<TransactionResponse> transactions = storageInOutService.getAllTransactions(transactionType, month, year);
        return ResponseEntity.ok(transactions);
    }

    // ===========================
    //  GET TRANSACTION BY ID
    // ===========================
    @Operation(summary = "Lấy chi tiết phiếu nhập/xuất kho", description = "Trả về thông tin đầy đủ kèm danh sách items")
    @ApiMessage("Lấy chi tiết transaction thành công")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_INVENTORY_MANAGER', 'ROLE_MANAGER', 'ROLE_DENTIST', 'ROLE_NURSE')")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "ID của phiếu nhập/xuất kho") @PathVariable Long id) {
        log.info("GET /api/v1/storage/{}", id);
        TransactionResponse transaction = storageInOutService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    // ===========================
    // ✏ UPDATE TRANSACTION
    // ===========================
    @Operation(summary = "Cập nhật phiếu nhập/xuất kho", description = "Chỉ cho phép cập nhật notes. Không thể sửa items sau khi đã tạo.")
    @ApiMessage("Cập nhật transaction thành công")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_INVENTORY_MANAGER')")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @Parameter(description = "ID của phiếu nhập/xuất kho") @PathVariable Long id,
            @Parameter(description = "Ghi chú mới") @RequestParam String notes) {
        log.info("PUT /api/v1/storage/{} - notes: {}", id, notes);
        TransactionResponse updated = storageInOutService.updateTransactionNotes(id, notes);
        return ResponseEntity.ok(updated);
    }

    // ===========================
    //  DELETE TRANSACTION
    // ===========================
    @Operation(summary = "Xóa phiếu nhập/xuất kho", description = "CẢNH BÁO: Xóa transaction sẽ rollback số lượng tồn kho. Chỉ admin mới được phép.")
    @ApiMessage("Xóa transaction thành công")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "ID của phiếu nhập/xuất kho") @PathVariable Long id) {
        log.info("DELETE /api/v1/storage/{}", id);
        storageInOutService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
