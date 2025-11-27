package com.dental.clinic.management.warehouse.controller;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

import com.dental.clinic.management.utils.annotation.ApiMessage;
import com.dental.clinic.management.warehouse.dto.request.ItemFilterRequest;
import com.dental.clinic.management.warehouse.dto.response.ItemMasterPageResponse;
import com.dental.clinic.management.warehouse.service.ItemMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/warehouse/items")
@RequiredArgsConstructor
@Slf4j
public class ItemMasterController {

    private final ItemMasterService itemMasterService;

    @GetMapping
    @ApiMessage("Item masters retrieved successfully")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAnyAuthority('VIEW_ITEMS', 'VIEW_WAREHOUSE', 'MANAGE_WAREHOUSE')")
    public ResponseEntity<ItemMasterPageResponse> getItems(@ModelAttribute ItemFilterRequest filter) {

        log.info("GET /api/v1/warehouse/items - Filter: {}", filter);

        ItemMasterPageResponse response = itemMasterService.getItems(filter);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
