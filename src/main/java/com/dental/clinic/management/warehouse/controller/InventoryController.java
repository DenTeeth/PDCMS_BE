package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.utils.ResponseMessage;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import com.dental.clinic.management.warehouse.domain.Inventory;
import com.dental.clinic.management.warehouse.dto.request.CreateInventoryRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateInventoryRequest;
import com.dental.clinic.management.warehouse.dto.response.InventoryResponse;
import com.dental.clinic.management.warehouse.service.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Inventory management.
 * 
 * Base URL: /api/v1/inventory
 * 
 * Permissions:
 * - CREATE_INVENTORY: Create new inventory (ADMIN and INVENTORY_MANAGER)
 * - VIEW_INVENTORY: View inventory (ADMIN and STAFF)
 * - UPDATE_INVENTORY: Update inventory (ADMIN and INVENTORY_MANAGER)
 * - DELETE_INVENTORY: Delete inventory (ADMIN only)
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Warehouse - Inventory", description = "APIs for managing warehouse inventory (ADMIN and STAFF)")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Create a new inventory item.
     * 
     * POST /api/v1/inventory
     * 
     * @param request CreateInventoryRequest body
     * @return 201 Created with InventoryResponse
     */
    @PostMapping
    @Operation(summary = "Create new inventory item", description = "Create a new inventory item (ADMIN and INVENTORY_MANAGER)")
    @ApiMessage("Tạo vật tư thành công")
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody CreateInventoryRequest request) {
        log.info("REST request to create inventory: {}", request.getItemName());
        InventoryResponse response = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all inventory items with pagination and filters.
     * 
     * GET
     * /api/v1/inventory?page=0&size=20&sortBy=itemName&sortDirection=ASC&warehouseType=COLD&itemName=thuốc
     * 
     * @param page          page number (0-indexed)
     * @param size          page size
     * @param sortBy        sort field name
     * @param sortDirection ASC or DESC
     * @param warehouseType filter by warehouse type (optional)
     * @param itemName      filter by item name (optional, partial match)
     * @return 200 OK with Page<InventoryResponse>
     */
    @GetMapping
    @Operation(summary = "Get all inventory items with filters", description = "Retrieve paginated list of inventory items with optional filters (ADMIN and STAFF)")
    @ApiMessage("Lấy danh sách vật tư thành công")
    public ResponseEntity<Page<InventoryResponse>> getAllInventory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "itemName") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "ASC") String sortDirection,
            @RequestParam(name = "warehouseType", required = false) Inventory.WarehouseType warehouseType,
            @RequestParam(name = "itemName", required = false) String itemName) {

        log.info("REST request to get all inventory: page={}, size={}, warehouseType={}, itemName={}",
                page, size, warehouseType, itemName);
        Page<InventoryResponse> response = inventoryService.getAllInventory(
                page, size, sortBy, sortDirection, warehouseType, itemName);
        return ResponseEntity.ok(response);
    }

    /**
     * Get inventory by ID.
     * 
     * GET /api/v1/inventory/{inventoryId}
     * 
     * @param inventoryId inventory ID
     * @return 200 OK with InventoryResponse
     */
    @GetMapping("/{inventoryId}")
    @Operation(summary = "Get inventory by ID", description = "Get inventory details by ID (ADMIN and STAFF)")
    @ApiMessage("Lấy thông tin vật tư thành công")
    public ResponseEntity<InventoryResponse> getInventoryById(
            @PathVariable Long inventoryId) {
        log.info("REST request to get inventory by ID: {}", inventoryId);
        InventoryResponse response = inventoryService.getInventoryById(inventoryId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing inventory item.
     * 
     * PUT /api/v1/inventory/{inventoryId}
     * 
     * @param inventoryId inventory ID
     * @param request     UpdateInventoryRequest body
     * @return 200 OK with InventoryResponse
     */
    @PutMapping("/{inventoryId}")
    @Operation(summary = "Update inventory", description = "Update existing inventory item (ADMIN and INVENTORY_MANAGER)")
    @ApiMessage("Cập nhật vật tư thành công")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long inventoryId,
            @Valid @RequestBody UpdateInventoryRequest request) {
        log.info("REST request to update inventory: {}", inventoryId);
        InventoryResponse response = inventoryService.updateInventory(inventoryId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an inventory item.
     * 
     * DELETE /api/v1/inventory/{inventoryId}
     * 
     * @param inventoryId inventory ID
     * @return 200 OK with success message
     */
    @DeleteMapping("/{inventoryId}")
    @Operation(summary = "Delete inventory", description = "Delete inventory item by ID (ADMIN only)")
    @ApiMessage("Xóa vật tư thành công")
    public ResponseEntity<ResponseMessage> deleteInventory(@PathVariable Long inventoryId) {
        log.info("REST request to delete inventory: {}", inventoryId);
        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.ok(new ResponseMessage("Xóa vật tư thành công"));
    }
}
