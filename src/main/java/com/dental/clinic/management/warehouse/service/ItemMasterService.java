package com.dental.clinic.management.warehouse.service;

import com.dental.clinic.management.warehouse.domain.ItemMaster;
import com.dental.clinic.management.warehouse.dto.request.ItemFilterRequest;
import com.dental.clinic.management.warehouse.dto.response.ItemMasterListDto;
import com.dental.clinic.management.warehouse.dto.response.ItemMasterPageResponse;
import com.dental.clinic.management.warehouse.repository.ItemMasterRepository;
import com.dental.clinic.management.warehouse.specification.ItemMasterSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemMasterService {

    private final ItemMasterRepository itemMasterRepository;

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
