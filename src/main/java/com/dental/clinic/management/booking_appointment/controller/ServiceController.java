package com.dental.clinic.management.booking_appointment.controller;

import com.dental.clinic.management.booking_appointment.dto.request.UpdateServiceRequest;
import com.dental.clinic.management.booking_appointment.dto.response.ServiceResponse;
import com.dental.clinic.management.booking_appointment.service.DentalServiceService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    @ApiMessage("Lấy danh sách dịch vụ thành công")
    public ResponseEntity<Page<ServiceResponse>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "serviceName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Integer specializationId,
            @RequestParam(required = false) String keyword) {

        Page<ServiceResponse> services = serviceService.getAllServices(
                page, size, sortBy, sortDirection, isActive, specializationId, keyword);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{serviceCode}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_SERVICE + "')")
    @Operation(summary = "Get service by code")
    @ApiMessage("Lấy thông tin dịch vụ thành công")
    public ResponseEntity<ServiceResponse> getServiceByCode(@PathVariable String serviceCode) {
        return ResponseEntity.ok(serviceService.getServiceByCode(serviceCode));
    }

    @PutMapping("/{serviceCode}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + UPDATE_SERVICE + "')")
    @Operation(summary = "Update service")
    @ApiMessage("Cập nhật thông tin dịch vụ thành công")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable String serviceCode,
            @Valid @RequestBody UpdateServiceRequest request) {
        return ResponseEntity.ok(serviceService.updateService(serviceCode, request));
    }

    @DeleteMapping("/{serviceCode}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + DELETE_SERVICE + "')")
    @Operation(summary = "Delete service (soft delete)")
    @ApiMessage("Vô hiệu hóa dịch vụ thành công")
    public ResponseEntity<Void> deleteService(@PathVariable String serviceCode) {
        serviceService.deleteService(serviceCode);
        return ResponseEntity.noContent().build();
    }
}
