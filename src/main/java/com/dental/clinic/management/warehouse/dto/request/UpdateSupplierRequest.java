package com.dental.clinic.management.warehouse.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing supplier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSupplierRequest {

    @Size(max = 100, message = "Tên nhà cung cấp không được vượt quá 100 ký tự")
    @JsonProperty("supplier_name")
    private String supplierName;

    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("address")
    private String address;

    @JsonProperty("certification_number")
    private String certificationNumber;

    @JsonProperty("is_verified")
    private Boolean isVerified;
}
