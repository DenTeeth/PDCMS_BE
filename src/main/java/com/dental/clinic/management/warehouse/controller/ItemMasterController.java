package com.dental.clinic.management.warehouse.controller;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

import com.dental.clinic.management.utils.annotation.ApiMessage;
import com.dental.clinic.management.warehouse.dto.request.CreateItemMasterRequest;
import com.dental.clinic.management.warehouse.dto.request.ItemFilterRequest;
import com.dental.clinic.management.warehouse.dto.response.CreateItemMasterResponse;
import com.dental.clinic.management.warehouse.dto.response.ItemMasterPageResponse;
import com.dental.clinic.management.warehouse.service.ItemMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * API 6.8 & 6.9: Item Master Management Controller
 * 
 * Features:
 * - API 6.8: List all items with advanced filtering
 * - API 6.9: Create new item master with unit hierarchy
 * - Denormalized cache for performance (cached_total_quantity, cached_last_import_date)
 * - RBAC with VIEW_ITEMS, CREATE_ITEMS, MANAGE_WAREHOUSE permissions
 * - Pagination & sorting
 */
@RestController
@RequestMapping("/api/v1/warehouse/items")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Item Master Management", description = "API 6.8 & 6.9 - Item Master List & Create")
public class ItemMasterController {

    private final ItemMasterService itemMasterService;

    @GetMapping
    @ApiMessage("Item masters retrieved successfully")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAnyAuthority('VIEW_ITEMS', 'VIEW_WAREHOUSE', 'MANAGE_WAREHOUSE')")
    @Operation(summary = "Get Item Masters List", description = """
            API 6.8 - Get list of all item masters with advanced filtering
            
            **Main Features:**
            - Search by item name or code
            - Filter by category, stock status
            - Sort by name, total stock, last import date
            - Denormalized cache: cached_total_quantity, cached_last_import_date (auto-updated on import/export)
            - Pagination with complete metadata
            
            **Use Cases:**
            1. Create export transaction: Get list of available items (?inStock=true&sortBy=itemName)
            2. Check inventory: Filter by category and view stock quantities
            3. Search items: Search by name (?search=syringe)
            4. Dashboard overview: View all items summary
            
            **Permissions:**
            - VIEW_ITEMS: View item list (Doctor, Receptionist, Manager)
            - VIEW_WAREHOUSE: Warehouse view permission (Warehouse staff)
            - MANAGE_WAREHOUSE: Warehouse management permission (Admin)
            
            **Performance:**
            - Uses cached_total_quantity instead of SUM(batch.quantity) - Reduces query time from 500ms to 50ms
            - Index on itemName, categoryId, cached_total_quantity
            """)
    public ResponseEntity<ItemMasterPageResponse> getItems(@ModelAttribute ItemFilterRequest filter) {

        log.info("GET /api/v1/warehouse/items - Filter: {}", filter);

        ItemMasterPageResponse response = itemMasterService.getItems(filter);

        log.info("Item masters retrieved - Total: {}, Page: {}/{}", 
                response.getMeta().getTotalElements(),
                response.getMeta().getPage() + 1,
                response.getMeta().getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    @ApiMessage("Item master created successfully")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAnyAuthority('CREATE_ITEMS', 'MANAGE_WAREHOUSE')")
    @Operation(summary = "Create New Item Master", description = """
            API 6.9 - Create new item master with unit hierarchy
            
            **Main Features:**
            - Define SKU code with format validation (A-Z, 0-9, hyphen)
            - Set warehouse type (COLD for refrigerated, NORMAL for regular)
            - Configure stock alerts (min/max levels)
            - Define unit hierarchy (Box -> Strip -> Pill) with conversion rates
            - Healthcare compliance: prescription required flag, default shelf life
            - Batch insert for performance
            
            **Validation Rules:**
            1. Item code must be unique (3-20 chars, uppercase, numbers, hyphens)
            2. Min stock level < Max stock level
            3. Exactly ONE base unit with conversion rate = 1
            4. Other units must have conversion rate > 1
            5. Unit names must be unique within the item
            
            **Use Cases:**
            1. Add new medication: Set prescription flag, shelf life
            2. Add consumables: Set stock alerts for reorder
            3. Add equipment: Set warehouse type for proper storage
            
            **Permissions:**
            - ADMIN: Full access
            - CREATE_ITEMS: Create item masters
            - MANAGE_WAREHOUSE: Warehouse management
            
            **Audit Trail:**
            - Logs CREATE_ITEM action with user info
            """)
    public ResponseEntity<CreateItemMasterResponse> createItemMaster(
            @Valid @RequestBody CreateItemMasterRequest request) {

        log.info("POST /api/v1/warehouse/items - Creating item: {}", request.getItemCode());

        CreateItemMasterResponse response = itemMasterService.createItemMaster(request);

        log.info("Item master created successfully - ID: {}, Code: {}", 
                response.getItemMasterId(), response.getItemCode());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
