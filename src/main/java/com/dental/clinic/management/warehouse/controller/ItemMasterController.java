package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.warehouse.dto.response.ItemMasterSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3/warehouse/items")
@RequiredArgsConstructor
@Tag(name = "Warehouse - Item Masters", description = "Quản lý danh mục vật tư (master data)")
public class ItemMasterController {

    // TODO: Inject ItemMasterService when created

    @GetMapping("/{itemId}")
    @Operation(summary = "Lấy thông tin vật tư", description = "Lấy chi tiết vật tư theo ID (bao gồm tồn kho)")
    public ResponseEntity<ItemMasterSummaryResponse> getItemById(@PathVariable UUID itemId) {
        // TODO: Implement when ItemMasterService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả vật tư", description = "Lấy danh sách tất cả vật tư")
    public ResponseEntity<List<ItemMasterSummaryResponse>> getAllItems() {
        // TODO: Implement when ItemMasterService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm vật tư", description = "Tìm kiếm vật tư theo tên, mã")
    public ResponseEntity<List<ItemMasterSummaryResponse>> searchItems(@RequestParam String keyword) {
        // TODO: Implement when ItemMasterService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Lấy vật tư theo danh mục", description = "Lấy danh sách vật tư thuộc danh mục")
    public ResponseEntity<List<ItemMasterSummaryResponse>> getItemsByCategory(@PathVariable UUID categoryId) {
        // TODO: Implement when ItemMasterService is ready
        return ResponseEntity.ok().build();
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Lấy vật tư sắp hết", description = "Lấy danh sách vật tư có tồn kho thấp")
    public ResponseEntity<List<ItemMasterSummaryResponse>> getLowStockItems() {
        // TODO: Implement when ItemMasterService is ready
        return ResponseEntity.ok().build();
    }
}
