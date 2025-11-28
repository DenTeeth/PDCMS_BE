package com.dental.clinic.management.warehouse.service;

import com.dental.clinic.management.warehouse.domain.ItemMaster;
import com.dental.clinic.management.warehouse.domain.ItemUnit;
import com.dental.clinic.management.warehouse.dto.request.CreateItemMasterRequest;
import com.dental.clinic.management.warehouse.dto.request.ItemFilterRequest;
import com.dental.clinic.management.warehouse.dto.response.CreateItemMasterResponse;
import com.dental.clinic.management.warehouse.dto.response.ItemMasterListDto;
import com.dental.clinic.management.warehouse.dto.response.ItemMasterPageResponse;
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
import java.util.Set;

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
                            "Item category with ID " + request.getCategoryId() + " not found");
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
                        request.getIsPrescriptionRequired() != null ? request.getIsPrescriptionRequired() : false)
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
                            unitRequest.getIsDefaultImportUnit() != null ? unitRequest.getIsDefaultImportUnit() : false)
                    .isDefaultExportUnit(
                            unitRequest.getIsDefaultExportUnit() != null ? unitRequest.getIsDefaultExportUnit() : false)
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
}
