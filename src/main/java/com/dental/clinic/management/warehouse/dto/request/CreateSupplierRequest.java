package com.dental.clinic.management.warehouse.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new supplier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplierRequest {

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Size(max = 100, message = "Tên nhà cung cấp không được vượt quá 100 ký tự")
    @JsonProperty("supplier_name")
    private String supplierName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @NotBlank(message = "Địa chỉ không được để trống")
    @JsonProperty("address")
    private String address;

    @JsonProperty("certification_number")
    private String certificationNumber;
}
