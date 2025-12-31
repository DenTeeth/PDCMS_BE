package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplierRequest {

    @NotBlank(message = "Tên nhà cung cấp là bắt buộc")
    @Size(min = 2, max = 255, message = "Tên nhà cung cấp phải từ 2-255 ký tự")
    private String supplierName;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Size(min = 10, max = 11, message = "Số điện thoại phải từ 10-11 chữ số")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải chỉ chứa chữ số (10-11 ký tự)")
    private String phoneNumber;

    @Email(message = "Định dạng email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;

    private Boolean isBlacklisted;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
}
