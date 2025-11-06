package com.dental.clinic.management.booking_appointment.controller;

import com.dental.clinic.management.booking_appointment.dto.request.CreateServiceRequest;
import com.dental.clinic.management.booking_appointment.dto.request.UpdateServiceRequest;
import com.dental.clinic.management.booking_appointment.dto.response.ServiceResponse;
import com.dental.clinic.management.booking_appointment.service.DentalServiceService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Tag(name = "Service Management", description = "APIs for managing dental clinic services")
public class ServiceController {

    private final DentalServiceService serviceService;

    @GetMapping
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_SERVICE + "')")
    @Operation(summary = "Get all services with filters")
    @ApiMessage("LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch dÃ¡Â»â€¹ch vÃ¡Â»Â¥ thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<Page<ServiceResponse>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "serviceId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Integer specializationId,
            @RequestParam(required = false) String keyword) {

        Page<ServiceResponse> services = serviceService.getAllServices(
                page, size, sortBy, sortDirection, isActive, specializationId, keyword);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/code/{serviceCode}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_SERVICE + "')")
    @Operation(summary = "Get service by code")
    @ApiMessage("LÃ¡ÂºÂ¥y thÃƒÂ´ng tin dÃ¡Â»â€¹ch vÃ¡Â»Â¥ theo mÃƒÂ£ thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<ServiceResponse> getServiceByCode(@PathVariable String serviceCode) {
        return ResponseEntity.ok(serviceService.getServiceByCode(serviceCode));
    }

    @GetMapping("/{serviceId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_SERVICE + "')")
    @Operation(summary = "Get service by ID")
    @ApiMessage("LÃ¡ÂºÂ¥y thÃƒÂ´ng tin dÃ¡Â»â€¹ch vÃ¡Â»Â¥ theo ID thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Integer serviceId) {
        return ResponseEntity.ok(serviceService.getServiceById(serviceId));
    }

    @PostMapping
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + CREATE_SERVICE + "')")
    @Operation(summary = "Create new service")
    @ApiMessage("TÃ¡ÂºÂ¡o dÃ¡Â»â€¹ch vÃ¡Â»Â¥ mÃ¡Â»â€ºi thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody CreateServiceRequest request) {
        ServiceResponse response = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{serviceCode}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + UPDATE_SERVICE + "')")
    @Operation(summary = "Update service")
    @ApiMessage("CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t thÃƒÂ´ng tin dÃ¡Â»â€¹ch vÃ¡Â»Â¥ thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable String serviceCode,
            @Valid @RequestBody UpdateServiceRequest request) {
        return ResponseEntity.ok(serviceService.updateService(serviceCode, request));
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + DELETE_SERVICE + "')")
    @Operation(summary = "Delete service by ID (soft delete - set isActive = false)", description = "Deactivate service using service ID. RESTful soft delete.")
    @ApiMessage("VÃƒÂ´ hiÃ¡Â»â€¡u hÃƒÂ³a dÃ¡Â»â€¹ch vÃ¡Â»Â¥ thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<Void> deleteService(@PathVariable Integer serviceId) {
        serviceService.deleteServiceById(serviceId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/code/{serviceCode}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + DELETE_SERVICE + "')")
    @Operation(summary = "Delete service by code (soft delete - set isActive = false)", description = "Deactivate service using service code. RESTful soft delete.")
    @ApiMessage("VÃƒÂ´ hiÃ¡Â»â€¡u hÃƒÂ³a dÃ¡Â»â€¹ch vÃ¡Â»Â¥ thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<Void> deleteServiceByCode(@PathVariable String serviceCode) {
        serviceService.deleteServiceByCode(serviceCode);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{serviceId}/toggle")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + UPDATE_SERVICE + "')")
    @Operation(summary = "Toggle service active status (activate Ã¢â€ â€ deactivate)", description = "RESTful way to activate or deactivate service. If active Ã¢â€ â€™ set inactive, if inactive Ã¢â€ â€™ set active. Returns updated service.")
    @ApiMessage("ChuyÃ¡Â»Æ’n Ã„â€˜Ã¡Â»â€¢i trÃ¡ÂºÂ¡ng thÃƒÂ¡i dÃ¡Â»â€¹ch vÃ¡Â»Â¥ thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<ServiceResponse> toggleServiceStatus(@PathVariable Integer serviceId) {
        ServiceResponse response = serviceService.toggleServiceStatus(serviceId);
        return ResponseEntity.ok(response);
    }
}
