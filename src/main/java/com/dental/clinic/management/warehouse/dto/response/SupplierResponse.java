package com.dental.clinic.management.warehouse.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for supplier details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierResponse {

    @JsonProperty("supplier_id")
    private UUID supplierId;

    @JsonProperty("supplier_name")
    private String supplierName;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("address")
    private String address;

    @JsonProperty("certification_number")
    private String certificationNumber;

    @JsonProperty("is_verified")
    private Boolean isVerified;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
