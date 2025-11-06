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


@RestController
@RequestMapping("/api/v3/warehouse")
@RequiredArgsConstructor
@Tag(name = "Warehouse - Transactions", description = "QuÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â£n lÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â½ xuÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t nhÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­p kho (FEFO)")
public class WarehouseController {

    // TODO: Inject WarehouseService when created

    @PostMapping("/import")
    @Operation(summary = "NhÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­p kho", description = "TÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¡o phiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿u nhÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­p kho (tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â± ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ng tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¡o/cÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­p nhÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t batch)")
    public ResponseEntity<StorageTransactionResponse> createImportTransaction(
            @Valid @RequestBody CreateImportTransactionRequest request) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/export")
    @Operation(summary = "XuÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t kho", description = "TÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¡o phiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿u xuÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t kho (FEFO - lÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´ cÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â© nhÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t trÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Âºc)")
    public ResponseEntity<StorageTransactionResponse> createExportTransaction(
            @Valid @RequestBody CreateExportTransactionRequest request) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/adjust")
    @Operation(summary = "ÃƒÆ’Ã¢â‚¬Å¾Ãƒâ€šÃ‚ÂiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Âu chÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â°nh tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œn kho", description = "ÃƒÆ’Ã¢â‚¬Å¾Ãƒâ€šÃ‚ÂiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Âu chÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â°nh sÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ lÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â£ng tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œn (ADJUSTMENT/DESTROY)")
    public ResponseEntity<StorageTransactionResponse> createAdjustment(
            @Valid @RequestBody CreateAdjustmentRequest request) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y thÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´ng tin giao dÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹ch", description = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y chi tiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿t giao dÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹ch kho theo ID")
    public ResponseEntity<StorageTransactionResponse> getTransactionById(@PathVariable Long transactionId) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions/history")
    @Operation(summary = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹ch sÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â­ giao dÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹ch", description = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y lÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹ch sÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â­ giao dÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹ch theo khoÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â£ng thÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Âi gian")
    public ResponseEntity<List<StorageTransactionResponse>> getTransactionHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/batches/item/{itemId}")
    @Operation(summary = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y batch theo vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°", description = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y danh sÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ch batch cÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â§a vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â° (FEFO order)")
    public ResponseEntity<List<ItemBatchResponse>> getBatchesByItem(@PathVariable Long itemId) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/batches/expiring")
    @Operation(summary = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y batch sÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¯p hÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿t hÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¡n", description = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y danh sÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ch batch sÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¯p hÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿t hÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¡n (30 ngÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â y)")
    public ResponseEntity<List<ItemBatchResponse>> getExpiringBatches() {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/inventory")
    @Operation(summary = "ThÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ng kÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âª tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œn kho", description = "TÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ng quan tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œn kho theo danh mÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â¥c")
    public ResponseEntity<InventoryStatsResponse> getInventoryStats() {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/storage")
    @Operation(summary = "ThÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ng kÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âª xuÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t nhÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­p", description = "ThÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ng kÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âª giao dÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹ch xuÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t nhÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­p kho")
    public ResponseEntity<StorageStatsResponse> getStorageStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports/loss")
    @Operation(summary = "BÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡o cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡o hao hÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â¥t", description = "BÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡o cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡o vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â° ADJUSTMENT + DESTROY")
    public ResponseEntity<List<StorageTransactionResponse>> getLossReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        // TODO: Implement when WarehouseService is ready
        return ResponseEntity.ok().build();
    }
}

