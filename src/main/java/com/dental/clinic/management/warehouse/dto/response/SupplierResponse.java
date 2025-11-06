package com.dental.clinic.management.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * Response DTO for Supplier.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    private Long id;
    private String supplierName;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private Integer suppliedItemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

