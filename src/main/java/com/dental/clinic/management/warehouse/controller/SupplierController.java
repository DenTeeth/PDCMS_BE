package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.warehouse.dto.request.CreateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.response.SupplierResponse;
import com.dental.clinic.management.warehouse.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3/warehouse/suppliers")
@RequiredArgsConstructor
@Tag(name = "Warehouse - Suppliers", description = "Quản lý nhà cung cấp")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @Operation(summary = "Tạo nhà cung cấp mới", description = "Tạo thông tin nhà cung cấp vật tư")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{supplierId}")
    @Operation(summary = "Cập nhật nhà cung cấp", description = "Cập nhật thông tin nhà cung cấp")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable UUID supplierId,
            @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierResponse response = supplierService.updateSupplier(supplierId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{supplierId}")
    @Operation(summary = "Lấy thông tin nhà cung cấp", description = "Lấy chi tiết nhà cung cấp theo ID")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable UUID supplierId) {
        SupplierResponse response = supplierService.getSupplierById(supplierId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả nhà cung cấp", description = "Lấy danh sách tất cả nhà cung cấp")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        List<SupplierResponse> response = supplierService.getAllSuppliers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm nhà cung cấp", description = "Tìm kiếm nhà cung cấp theo tên, email, số điện thoại")
    public ResponseEntity<List<SupplierResponse>> searchSuppliers(@RequestParam String keyword) {
        List<SupplierResponse> response = supplierService.searchSuppliers(keyword);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{supplierId}")
    @Operation(summary = "Xóa nhà cung cấp", description = "Xóa nhà cung cấp (kiểm tra ràng buộc)")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID supplierId) {
        supplierService.deleteSupplier(supplierId);
        return ResponseEntity.noContent().build();
    }
}
