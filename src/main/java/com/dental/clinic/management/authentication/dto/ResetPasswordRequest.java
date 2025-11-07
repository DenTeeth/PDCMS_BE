package com.dental.clinic.management.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to reset password with token")
public class ResetPasswordRequest {

    @NotBlank(message = "Token cannot be empty")
    @Schema(description = "Password reset token from email", example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;

    @NotBlank(message = "New password cannot be empty")
    @Size(min = 6, max = 50, message = "Password must be between 6-50 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).+$", message = "Password must contain at least 1 letter and 1 number")
    @Schema(description = "New password (6-50 chars, must contain letters and numbers)", example = "NewPass123")
    private String newPassword;

    @NotBlank(message = "Confirm password cannot be empty")
    @Schema(description = "Confirm new password", example = "NewPass123")
    private String confirmPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String token, String newPassword, String confirmPassword) {
        this.token = token;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
