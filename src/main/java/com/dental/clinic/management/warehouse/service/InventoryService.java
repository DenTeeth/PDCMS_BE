package com.dental.clinic.management.warehouse.service;

import com.dental.clinic.management.exception.warehouse.DuplicateInventoryException;
import com.dental.clinic.management.exception.warehouse.InventoryNotFoundException;
import com.dental.clinic.management.exception.warehouse.InvalidWarehouseDataException;
import com.dental.clinic.management.warehouse.domain.Inventory;
import com.dental.clinic.management.warehouse.domain.Supplier;
import com.dental.clinic.management.warehouse.dto.request.CreateInventoryRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateInventoryRequest;
import com.dental.clinic.management.warehouse.dto.response.InventoryResponse;
import com.dental.clinic.management.warehouse.mapper.InventoryMapper;
import com.dental.clinic.management.warehouse.repository.InventoryRepository;
import com.dental.clinic.management.warehouse.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Inventory management.
 * Simplified version - removed pricing and certification validation.
 * Handles business logic and RBAC.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryMapper inventoryMapper;

    /**
     * Create a new inventory item.
     * Only ADMIN and INVENTORY_MANAGER can create.
     */
    @Transactional
    @PreAuthorize("hasAuthority('CREATE_INVENTORY')")
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        log.info("Creating new inventory item: {}", request.getItemName());

        // Validate business rules
        validateInventoryData(request.getWarehouseType(), request.getExpiryDate(), request.getStockQuantity());

        // Check for duplicate item name
        if (inventoryRepository.existsByItemName(request.getItemName())) {
            throw new DuplicateInventoryException(
                    "Vật tư với tên '" + request.getItemName() + "' đã tồn tại");
        }

        Inventory inventory = inventoryMapper.toEntity(request);
        Inventory savedInventory = inventoryRepository.save(inventory);

        // Fetch supplier for response
        Supplier supplier = supplierRepository.findById(savedInventory.getSupplierId()).orElse(null);

        log.info("Inventory item created successfully with ID: {}", savedInventory.getInventoryId());
        return inventoryMapper.toResponse(savedInventory, supplier);
    }

    /**
     * Get all inventory items with pagination and optional filters.
     * ADMIN and STAFF can view.
     * 
     * @param warehouseType filter by warehouse type (optional)
     * @param itemName      filter by item name (optional, partial match)
     */
    @PreAuthorize("hasAuthority('VIEW_INVENTORY')")
    public Page<InventoryResponse> getAllInventory(int page, int size, String sortBy, String sortDirection,
            Inventory.WarehouseType warehouseType, String itemName) {
        log.info("Fetching inventory: page={}, size={}, warehouseType={}, itemName={}",
                page, size, warehouseType, itemName);

        Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Inventory> inventoryPage;

        // Apply filters
        if (warehouseType != null && itemName != null && !itemName.trim().isEmpty()) {
            // Both filters
            inventoryPage = inventoryRepository.findByWarehouseTypeAndItemNameContainingIgnoreCase(
                    warehouseType, itemName, pageable);
        } else if (warehouseType != null) {
            // Warehouse type only
            inventoryPage = inventoryRepository.findByWarehouseType(warehouseType, pageable);
        } else if (itemName != null && !itemName.trim().isEmpty()) {
            // Item name only
            inventoryPage = inventoryRepository.findByItemNameContainingIgnoreCase(itemName, pageable);
        } else {
            // No filters
            inventoryPage = inventoryRepository.findAll(pageable);
        }

        // Map to response with supplier info
        return inventoryPage.map(inventory -> {
            Supplier supplier = supplierRepository.findById(inventory.getSupplierId()).orElse(null);
            return inventoryMapper.toResponse(inventory, supplier);
        });
    }

    /**
     * Get inventory by ID.
     */
    @PreAuthorize("hasAuthority('VIEW_INVENTORY')")
    public InventoryResponse getInventoryById(Long inventoryId) {
        log.info("Fetching inventory by ID: {}", inventoryId);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Không tìm thấy vật tư với ID: " + inventoryId));

        // Fetch supplier for response
        Supplier supplier = supplierRepository.findById(inventory.getSupplierId()).orElse(null);

        return inventoryMapper.toResponse(inventory, supplier);
    }

    /**
     * Update inventory item.
     * Only ADMIN and INVENTORY_MANAGER can update.
     */
    @Transactional
    @PreAuthorize("hasAuthority('UPDATE_INVENTORY')")
    public InventoryResponse updateInventory(Long inventoryId, UpdateInventoryRequest request) {
        log.info("Updating inventory with ID: {}", inventoryId);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Không tìm thấy vật tư với ID: " + inventoryId));

        // Validate business rules if warehouse type or expiry date changed
        Inventory.WarehouseType newWarehouseType = request.getWarehouseType() != null
                ? request.getWarehouseType()
                : inventory.getWarehouseType();

        java.time.LocalDate newExpiryDate = request.getExpiryDate() != null
                ? request.getExpiryDate()
                : inventory.getExpiryDate();

        Integer newStockQuantity = request.getStockQuantity() != null
                ? request.getStockQuantity()
                : inventory.getStockQuantity();

        validateInventoryData(newWarehouseType, newExpiryDate, newStockQuantity);

        // Check for duplicate item name (if changed)
        if (request.getItemName() != null && !request.getItemName().equals(inventory.getItemName())) {
            if (inventoryRepository.existsByItemNameAndInventoryIdNot(request.getItemName(), inventoryId)) {
                throw new DuplicateInventoryException(
                        "Vật tư với tên '" + request.getItemName() + "' đã tồn tại");
            }
        }

        inventoryMapper.updateEntity(inventory, request);
        Inventory updatedInventory = inventoryRepository.save(inventory);

        // Fetch supplier for response
        Supplier supplier = supplierRepository.findById(updatedInventory.getSupplierId()).orElse(null);

        log.info("Inventory updated successfully with ID: {}", inventoryId);
        return inventoryMapper.toResponse(updatedInventory, supplier);
    }

    /**
     * Delete inventory item.
     * Only ADMIN can delete.
     */
    @Transactional
    @PreAuthorize("hasAuthority('DELETE_INVENTORY')")
    public void deleteInventory(Long inventoryId) {
        log.info("Deleting inventory with ID: {}", inventoryId);

        if (!inventoryRepository.existsById(inventoryId)) {
            throw new InventoryNotFoundException(
                    "Không tìm thấy vật tư với ID: " + inventoryId);
        }

        inventoryRepository.deleteById(inventoryId);
        log.info("Inventory deleted successfully with ID: {}", inventoryId);
    }

    /**
     * Validate inventory data based on business rules.
     * - COLD warehouse MUST have expiry date
     * - Stock quantity must be greater than 0
     */
    private void validateInventoryData(Inventory.WarehouseType warehouseType,
            java.time.LocalDate expiryDate,
            Integer stockQuantity) {
        // COLD warehouse MUST have expiry date
        if (warehouseType == Inventory.WarehouseType.COLD && expiryDate == null) {
            throw new InvalidWarehouseDataException(
                    "Kho lạnh (COLD) bắt buộc phải có ngày hết hạn (expiryDate)");
        }

        // Stock quantity must be > 0
        if (stockQuantity != null && stockQuantity <= 0) {
            throw new InvalidWarehouseDataException(
                    "Số lượng tồn kho phải lớn hơn 0");
        }
    }
}
