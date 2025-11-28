package com.dental.clinic.management.warehouse.service;

import com.dental.clinic.management.warehouse.domain.ItemMaster;
import com.dental.clinic.management.warehouse.domain.ItemUnit;
import com.dental.clinic.management.warehouse.dto.request.CreateItemMasterRequest;
import com.dental.clinic.management.warehouse.dto.request.ItemFilterRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateItemMasterRequest;
import com.dental.clinic.management.warehouse.dto.response.CreateItemMasterResponse;
import com.dental.clinic.management.warehouse.dto.response.GetItemUnitsResponse;
import com.dental.clinic.management.warehouse.dto.response.ItemMasterListDto;
import com.dental.clinic.management.warehouse.dto.response.ItemMasterPageResponse;
import com.dental.clinic.management.warehouse.dto.response.UpdateItemMasterResponse;
import com.dental.clinic.management.warehouse.domain.ItemCategory;
import com.dental.clinic.management.warehouse.repository.ItemCategoryRepository;
import com.dental.clinic.management.warehouse.repository.ItemMasterRepository;
import com.dental.clinic.management.warehouse.repository.ItemUnitRepository;
import com.dental.clinic.management.warehouse.specification.ItemMasterSpecification;
import com.dental.clinic.management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemMasterService {

        private final ItemMasterRepository itemMasterRepository;
        private final ItemUnitRepository itemUnitRepository;
        private final ItemCategoryRepository itemCategoryRepository;

        @Transactional
        public CreateItemMasterResponse createItemMaster(CreateItemMasterRequest request) {
                log.info("Creating item master: {}", request.getItemCode());

                // 1. Validate itemCode uniqueness
                if (itemMasterRepository.findByItemCode(request.getItemCode()).isPresent()) {
                        log.warn("Item code already exists: {}", request.getItemCode());
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Item code '" + request.getItemCode() + "' already exists");
                }

                // 2. Validate min < max stock level
                if (request.getMinStockLevel() >= request.getMaxStockLevel()) {
                        log.warn("Invalid stock levels - min: {}, max: {}",
                                        request.getMinStockLevel(), request.getMaxStockLevel());
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Min stock level must be less than max stock level");
                }

                // 3. Validate exactly one base unit
                long baseUnitCount = request.getUnits().stream()
                                .filter(CreateItemMasterRequest.UnitRequest::getIsBaseUnit)
                                .count();

                if (baseUnitCount != 1) {
                        log.warn("Invalid base unit count: {}", baseUnitCount);
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Exactly one base unit is required");
                }

                // 4. Validate base unit has conversion rate = 1
                CreateItemMasterRequest.UnitRequest baseUnitRequest = request.getUnits().stream()
                                .filter(CreateItemMasterRequest.UnitRequest::getIsBaseUnit)
                                .findFirst()
                                .orElseThrow();

                if (baseUnitRequest.getConversionRate() != 1) {
                        log.warn("Base unit must have conversion rate = 1");
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Base unit must have conversion rate = 1");
                }

                // 5. Validate unit name uniqueness
                Set<String> unitNames = new HashSet<>();
                for (CreateItemMasterRequest.UnitRequest unit : request.getUnits()) {
                        if (!unitNames.add(unit.getUnitName().toLowerCase())) {
                                log.warn("Duplicate unit name: {}", unit.getUnitName());
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Unit name '" + unit.getUnitName() + "' is duplicated");
                        }
                }

                // 6. Validate category exists
                ItemCategory category = itemCategoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> {
                                        log.warn("Category not found: {}", request.getCategoryId());
                                        return new ResourceNotFoundException(
                                                        "ITEM_CATEGORY_NOT_FOUND",
                                                        "Item category with ID " + request.getCategoryId()
                                                                        + " not found");
                                });

                // 7. Create ItemMaster entity
                ItemMaster itemMaster = ItemMaster.builder()
                                .itemCode(request.getItemCode())
                                .itemName(request.getItemName())
                                .description(request.getDescription())
                                .category(category)
                                .unitOfMeasure(baseUnitRequest.getUnitName())
                                .warehouseType(request.getWarehouseType())
                                .minStockLevel(request.getMinStockLevel())
                                .maxStockLevel(request.getMaxStockLevel())
                                .currentMarketPrice(java.math.BigDecimal.ZERO)
                                .isPrescriptionRequired(
                                                request.getIsPrescriptionRequired() != null
                                                                ? request.getIsPrescriptionRequired()
                                                                : false)
                                .defaultShelfLifeDays(request.getDefaultShelfLifeDays())
                                .isActive(true)
                                .cachedTotalQuantity(0)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                ItemMaster savedItemMaster = itemMasterRepository.save(itemMaster);
                log.info("Item master saved with ID: {}", savedItemMaster.getItemMasterId());

                // 8. Create ItemUnit entities (batch insert)
                List<ItemUnit> units = new ArrayList<>();
                for (CreateItemMasterRequest.UnitRequest unitRequest : request.getUnits()) {
                        ItemUnit unit = ItemUnit.builder()
                                        .itemMaster(savedItemMaster)
                                        .unitName(unitRequest.getUnitName())
                                        .conversionRate(unitRequest.getConversionRate())
                                        .isBaseUnit(unitRequest.getIsBaseUnit())
                                        .displayOrder(unitRequest.getDisplayOrder())
                                        .isDefaultImportUnit(
                                                        unitRequest.getIsDefaultImportUnit() != null
                                                                        ? unitRequest.getIsDefaultImportUnit()
                                                                        : false)
                                        .isDefaultExportUnit(
                                                        unitRequest.getIsDefaultExportUnit() != null
                                                                        ? unitRequest.getIsDefaultExportUnit()
                                                                        : false)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        units.add(unit);
                }

                itemUnitRepository.saveAll(units);
                log.info("Saved {} units for item: {}", units.size(), savedItemMaster.getItemCode());

                // 9. Build response
                CreateItemMasterResponse response = CreateItemMasterResponse.builder()
                                .itemMasterId(savedItemMaster.getItemMasterId())
                                .itemCode(savedItemMaster.getItemCode())
                                .itemName(savedItemMaster.getItemName())
                                .baseUnitName(baseUnitRequest.getUnitName())
                                .totalQuantity(0)
                                .isActive(true)
                                .createdAt(savedItemMaster.getCreatedAt())
                                .createdBy("SYSTEM")
                                .build();

                log.info("Item master created successfully - ID: {}, Code: {}",
                                response.getItemMasterId(), response.getItemCode());

                return response;
        }

        @Transactional(readOnly = true)
        public ItemMasterPageResponse getItems(ItemFilterRequest filter) {

                log.debug("Getting items with filter: {}", filter);

                Specification<ItemMaster> spec = Specification
                                .where(ItemMasterSpecification.hasSearch(filter.getSearch()))
                                .and(ItemMasterSpecification.hasCategoryId(filter.getCategoryId()))
                                .and(ItemMasterSpecification.hasWarehouseType(filter.getWarehouseType()))
                                .and(ItemMasterSpecification.hasStockStatus(filter.getStockStatus()))
                                .and(ItemMasterSpecification.isActive(filter.getIsActive()));

                Pageable pageable = filter.toPageable();
                Page<ItemMaster> itemPage = itemMasterRepository.findAll(spec, pageable);

                log.debug("Found {} items", itemPage.getTotalElements());

                return ItemMasterPageResponse.builder()
                                .meta(ItemMasterPageResponse.MetaDto.builder()
                                                .page(itemPage.getNumber())
                                                .size(itemPage.getSize())
                                                .totalPages(itemPage.getTotalPages())
                                                .totalElements(itemPage.getTotalElements())
                                                .build())
                                .content(itemPage.getContent().stream()
                                                .map(this::mapToDto)
                                                .toList())
                                .build();
        }

        private ItemMasterListDto mapToDto(ItemMaster item) {
                return ItemMasterListDto.builder()
                                .itemMasterId(item.getItemMasterId())
                                .itemCode(item.getItemCode())
                                .itemName(item.getItemName())
                                .description(item.getDescription())
                                .categoryName(item.getCategory() != null ? item.getCategory().getCategoryName() : null)
                                .warehouseType(item.getWarehouseType())
                                .isActive(item.getIsActive())
                                .baseUnitName(item.getUnitOfMeasure())
                                .minStockLevel(item.getMinStockLevel())
                                .maxStockLevel(item.getMaxStockLevel())
                                .totalQuantity(item.getCachedTotalQuantity())
                                .stockStatus(item.getStockStatus())
                                .lastImportDate(item.getCachedLastImportDate())
                                .createdAt(item.getCreatedAt())
                                .updatedAt(item.getUpdatedAt())
                                .build();
        }

        @Transactional
        public UpdateItemMasterResponse updateItemMaster(Long itemMasterId, UpdateItemMasterRequest request) {
                log.info("Updating item master ID: {}", itemMasterId);

                // 1. Find item master
                ItemMaster itemMaster = itemMasterRepository.findById(itemMasterId)
                                .orElseThrow(() -> {
                                        log.warn("Item master not found: {}", itemMasterId);
                                        return new ResourceNotFoundException(
                                                        "ITEM_MASTER_NOT_FOUND",
                                                        "Item master with ID " + itemMasterId + " not found");
                                });

                // 2. Validate min < max stock level
                if (request.getMinStockLevel() >= request.getMaxStockLevel()) {
                        log.warn("Invalid stock levels - min: {}, max: {}",
                                        request.getMinStockLevel(), request.getMaxStockLevel());
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Min stock level must be less than max stock level");
                }

                // 3. Check if Safety Lock applies (cachedTotalQuantity > 0)
                boolean safetyLockApplied = itemMaster.getCachedTotalQuantity() > 0;
                log.info("Safety Lock status for item {}: {} (stock: {})",
                                itemMaster.getItemCode(), safetyLockApplied, itemMaster.getCachedTotalQuantity());

                // 4. Load existing units
                List<ItemUnit> existingUnits = itemUnitRepository.findByItemMaster_ItemMasterId(itemMasterId);
                Map<Long, ItemUnit> existingUnitMap = existingUnits.stream()
                                .collect(Collectors.toMap(ItemUnit::getUnitId, unit -> unit));

                // 5. Validate exactly one base unit in request
                long baseUnitCount = request.getUnits().stream()
                                .filter(UpdateItemMasterRequest.UnitRequest::getIsBaseUnit)
                                .count();

                if (baseUnitCount != 1) {
                        log.warn("Invalid base unit count: {}", baseUnitCount);
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Exactly one base unit is required");
                }

                // 6. Validate unit name uniqueness in request
                Set<String> unitNames = new HashSet<>();
                for (UpdateItemMasterRequest.UnitRequest unit : request.getUnits()) {
                        if (!unitNames.add(unit.getUnitName().toLowerCase())) {
                                log.warn("Duplicate unit name: {}", unit.getUnitName());
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Unit name '" + unit.getUnitName() + "' is duplicated");
                        }
                }

                // 7. Safety Lock validation (if stock exists)
                if (safetyLockApplied) {
                        List<String> blockedChanges = new ArrayList<>();

                        for (UpdateItemMasterRequest.UnitRequest unitRequest : request.getUnits()) {
                                if (unitRequest.getUnitId() != null) {
                                        ItemUnit existingUnit = existingUnitMap.get(unitRequest.getUnitId());
                                        if (existingUnit != null) {
                                                // Check conversion rate change
                                                if (existingUnit.getConversionRate() != unitRequest
                                                                .getConversionRate()) {
                                                        blockedChanges.add("Cannot change conversion rate for unit '"
                                                                        + existingUnit.getUnitName() + "' (current: "
                                                                        + existingUnit.getConversionRate() + ", new: "
                                                                        + unitRequest.getConversionRate() + ")");
                                                }
                                                // Check isBaseUnit change
                                                if (!existingUnit.getIsBaseUnit().equals(unitRequest.getIsBaseUnit())) {
                                                        blockedChanges.add("Cannot change base unit status for unit '"
                                                                        + existingUnit.getUnitName() + "'");
                                                }
                                        }
                                }
                        }

                        // Check for unit deletions (units not in request)
                        Set<Long> requestedUnitIds = request.getUnits().stream()
                                        .map(UpdateItemMasterRequest.UnitRequest::getUnitId)
                                        .filter(id -> id != null)
                                        .collect(Collectors.toSet());

                        for (ItemUnit existingUnit : existingUnits) {
                                if (!requestedUnitIds.contains(existingUnit.getUnitId())) {
                                        blockedChanges.add("Cannot delete unit '" + existingUnit.getUnitName()
                                                        + "' (use soft delete by setting isActive=false)");
                                }
                        }

                        if (!blockedChanges.isEmpty()) {
                                String errorMessage = "Safety Lock: Cannot modify units when stock exists. Blocked changes: "
                                                + String.join("; ", blockedChanges);
                                log.warn(errorMessage);
                                throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
                        }
                }

                // 8. Validate category exists
                ItemCategory category = itemCategoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> {
                                        log.warn("Category not found: {}", request.getCategoryId());
                                        return new ResourceNotFoundException(
                                                        "ITEM_CATEGORY_NOT_FOUND",
                                                        "Item category with ID " + request.getCategoryId()
                                                                        + " not found");
                                });

                // 9. Update ItemMaster entity
                itemMaster.setItemName(request.getItemName());
                itemMaster.setDescription(request.getDescription());
                itemMaster.setCategory(category);
                itemMaster.setWarehouseType(request.getWarehouseType());
                itemMaster.setMinStockLevel(request.getMinStockLevel());
                itemMaster.setMaxStockLevel(request.getMaxStockLevel());
                itemMaster.setIsPrescriptionRequired(
                                request.getIsPrescriptionRequired() != null ? request.getIsPrescriptionRequired()
                                                : false);
                itemMaster.setDefaultShelfLifeDays(request.getDefaultShelfLifeDays());
                itemMaster.setUpdatedAt(LocalDateTime.now());

                ItemMaster updatedItemMaster = itemMasterRepository.save(itemMaster);
                log.info("Item master updated: {}", updatedItemMaster.getItemCode());

                // 10. Update or create units
                List<ItemUnit> unitsToSave = new ArrayList<>();
                UpdateItemMasterRequest.UnitRequest baseUnitRequest = request.getUnits().stream()
                                .filter(UpdateItemMasterRequest.UnitRequest::getIsBaseUnit)
                                .findFirst()
                                .orElseThrow();

                for (UpdateItemMasterRequest.UnitRequest unitRequest : request.getUnits()) {
                        ItemUnit unit;
                        if (unitRequest.getUnitId() != null) {
                                // Update existing unit
                                unit = existingUnitMap.get(unitRequest.getUnitId());
                                if (unit == null) {
                                        log.warn("Unit not found: {}", unitRequest.getUnitId());
                                        throw new ResourceNotFoundException(
                                                        "ITEM_UNIT_NOT_FOUND",
                                                        "Unit with ID " + unitRequest.getUnitId() + " not found");
                                }
                                unit.setUnitName(unitRequest.getUnitName());
                                unit.setConversionRate(unitRequest.getConversionRate());
                                unit.setIsBaseUnit(unitRequest.getIsBaseUnit());
                                unit.setIsActive(unitRequest.getIsActive() != null ? unitRequest.getIsActive() : true);
                                unit.setDisplayOrder(unitRequest.getDisplayOrder());
                                unit.setIsDefaultImportUnit(
                                                unitRequest.getIsDefaultImportUnit() != null
                                                                ? unitRequest.getIsDefaultImportUnit()
                                                                : false);
                                unit.setIsDefaultExportUnit(
                                                unitRequest.getIsDefaultExportUnit() != null
                                                                ? unitRequest.getIsDefaultExportUnit()
                                                                : false);
                                unit.setUpdatedAt(LocalDateTime.now());
                        } else {
                                // Create new unit
                                unit = ItemUnit.builder()
                                                .itemMaster(updatedItemMaster)
                                                .unitName(unitRequest.getUnitName())
                                                .conversionRate(unitRequest.getConversionRate())
                                                .isBaseUnit(unitRequest.getIsBaseUnit())
                                                .isActive(unitRequest.getIsActive() != null ? unitRequest.getIsActive()
                                                                : true)
                                                .displayOrder(unitRequest.getDisplayOrder())
                                                .isDefaultImportUnit(
                                                                unitRequest.getIsDefaultImportUnit() != null
                                                                                ? unitRequest.getIsDefaultImportUnit()
                                                                                : false)
                                                .isDefaultExportUnit(
                                                                unitRequest.getIsDefaultExportUnit() != null
                                                                                ? unitRequest.getIsDefaultExportUnit()
                                                                                : false)
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build();
                        }
                        unitsToSave.add(unit);
                }

                itemUnitRepository.saveAll(unitsToSave);
                log.info("Updated/created {} units for item: {}", unitsToSave.size(), updatedItemMaster.getItemCode());

                // 11. Update base unit name in item master
                updatedItemMaster.setUnitOfMeasure(baseUnitRequest.getUnitName());
                itemMasterRepository.save(updatedItemMaster);

                // 12. Build response
                List<UpdateItemMasterResponse.UnitInfo> unitInfos = unitsToSave.stream()
                                .map(unit -> UpdateItemMasterResponse.UnitInfo.builder()
                                                .unitId(unit.getUnitId())
                                                .unitName(unit.getUnitName())
                                                .conversionRate(unit.getConversionRate())
                                                .isBaseUnit(unit.getIsBaseUnit())
                                                .isActive(unit.getIsActive())
                                                .build())
                                .toList();

                UpdateItemMasterResponse response = UpdateItemMasterResponse.builder()
                                .itemMasterId(updatedItemMaster.getItemMasterId())
                                .itemCode(updatedItemMaster.getItemCode())
                                .itemName(updatedItemMaster.getItemName())
                                .totalQuantity(updatedItemMaster.getCachedTotalQuantity())
                                .updatedAt(updatedItemMaster.getUpdatedAt())
                                .updatedBy("SYSTEM")
                                .safetyLockApplied(safetyLockApplied)
                                .units(unitInfos)
                                .build();

                log.info("Item master updated successfully - ID: {}, Code: {}, Safety Lock: {}",
                                response.getItemMasterId(), response.getItemCode(), safetyLockApplied);

                return response;
        }

        @Transactional(readOnly = true)
        public GetItemUnitsResponse getItemUnits(Long itemMasterId, String status) {
                log.info("Getting units for item master ID: {} with status: {}", itemMasterId, status);

                // 1. Validate and load item master
                ItemMaster itemMaster = itemMasterRepository.findById(itemMasterId)
                                .orElseThrow(() -> {
                                        log.error("Item master not found with ID: {}", itemMasterId);
                                        return new ResourceNotFoundException("ITEM_NOT_FOUND",
                                                        "Item master not found with ID: " + itemMasterId);
                                });

                // 2. Check if item is inactive (return 410 GONE)
                if (!itemMaster.getIsActive()) {
                        log.warn("Attempted to get units for inactive item: {}", itemMaster.getItemCode());
                        throw new ResponseStatusException(
                                        HttpStatus.GONE,
                                        "Item '" + itemMaster.getItemCode() + "' is no longer active");
                }

                // 3. Get units based on status filter
                List<ItemUnit> units;
                if ("inactive".equalsIgnoreCase(status)) {
                        units = itemUnitRepository.findByItemMaster_ItemMasterIdAndIsActiveFalseOrderByDisplayOrderAsc(
                                        itemMasterId);
                        log.debug("Found {} inactive units", units.size());
                } else if ("all".equalsIgnoreCase(status)) {
                        units = itemUnitRepository.findByItemMaster_ItemMasterIdOrderByDisplayOrderAsc(itemMasterId);
                        log.debug("Found {} total units (active + inactive)", units.size());
                } else {
                        // Default: active only
                        units = itemUnitRepository.findByItemMaster_ItemMasterIdAndIsActiveTrueOrderByDisplayOrderAsc(
                                        itemMasterId);
                        log.debug("Found {} active units", units.size());
                }

                if (units.isEmpty()) {
                        log.warn("No units configured for item master ID: {}", itemMasterId);
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "No units configured for this item");
                }

                // 4. Find base unit
                ItemUnit baseUnit = units.stream()
                                .filter(ItemUnit::getIsBaseUnit)
                                .findFirst()
                                .orElseThrow(() -> {
                                        log.error("Base unit not found for item master ID: {}", itemMasterId);
                                        return new ResponseStatusException(
                                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "Base unit not configured properly");
                                });

                // 5. Build response
                GetItemUnitsResponse.ItemMasterInfo itemMasterInfo = GetItemUnitsResponse.ItemMasterInfo.builder()
                                .itemMasterId(itemMaster.getItemMasterId())
                                .itemCode(itemMaster.getItemCode())
                                .itemName(itemMaster.getItemName())
                                .isActive(itemMaster.getIsActive())
                                .build();

                GetItemUnitsResponse.BaseUnitInfo baseUnitInfo = GetItemUnitsResponse.BaseUnitInfo.builder()
                                .unitId(baseUnit.getUnitId())
                                .unitName(baseUnit.getUnitName())
                                .build();

                List<GetItemUnitsResponse.UnitInfo> unitInfos = units.stream()
                                .map(unit -> {
                                        String description = generateUnitDescription(unit, baseUnit.getUnitName());
                                        return GetItemUnitsResponse.UnitInfo.builder()
                                                        .unitId(unit.getUnitId())
                                                        .unitName(unit.getUnitName())
                                                        .conversionRate(unit.getConversionRate())
                                                        .isBaseUnit(unit.getIsBaseUnit())
                                                        .displayOrder(unit.getDisplayOrder())
                                                        .isActive(unit.getIsActive())
                                                        .description(description)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                log.info("Successfully retrieved {} units for item: {}", unitInfos.size(), itemMaster.getItemCode());

                return GetItemUnitsResponse.builder()
                                .itemMaster(itemMasterInfo)
                                .baseUnit(baseUnitInfo)
                                .units(unitInfos)
                                .build();
        }

        private String generateUnitDescription(ItemUnit unit, String baseUnitName) {
                if (unit.getIsBaseUnit()) {
                        return "Don vi co so";
                }
                return String.format("1 %s = %d %s",
                                unit.getUnitName(),
                                unit.getConversionRate(),
                                baseUnitName);
        }
}
