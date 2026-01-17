package com.dental.clinic.management.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to reset password with token")
public class ResetPasswordRequest {

    @NotBlank(message = "Token không được để trống")
    @Schema(description = "Password reset token from email", example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, max = 50, message = "Mật khẩu phải từ 8-50 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
             message = "Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt")
    @Schema(description = "New password (8-50 chars, must contain uppercase, lowercase, number, and special character)", 
            example = "NewPass123!")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    @Schema(description = "Confirm new password", example = "NewPass123")
    private String confirmPassword;
}
