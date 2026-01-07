package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 6.15: Update Supplier Request
 * Allows updating supplier profile and risk management flags (isActive,
 * isBlacklisted)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSupplierRequest {

    @NotBlank(message = "Tên nhà cung cấp là bắt buộc")
    @Size(min = 2, max = 255, message = "Tên nhà cung cấp phải từ 2-255 ký tự")
    private String supplierName;

    @Size(max = 255, message = "Người liên hệ không được vượt quá 255 ký tự")
    private String contactPerson;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại phải có 10-11 chữ số và bắt đầu bằng 0")
    private String phoneNumber;

    @Email(message = "Định dạng email không hợp lệ")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;

    private Boolean isActive;

    private Boolean isBlacklisted;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
}
