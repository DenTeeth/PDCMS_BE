package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating Supplier.
 * All fields are optional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSupplierRequest {

    @Size(max = 200, message = "Tên nhà cung cấp không được vượt quá 200 ký tự")
    private String supplierName;

    @Size(max = 100, message = "Tên người liên hệ không được vượt quá 100 ký tự")
    private String contactPerson;

    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;
}
