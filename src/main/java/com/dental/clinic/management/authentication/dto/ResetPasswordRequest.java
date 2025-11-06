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

    @NotBlank(message = "Token khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng")
    @Schema(description = "Password reset token from email", example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;

    @NotBlank(message = "MÃ¡ÂºÂ­t khÃ¡ÂºÂ©u mÃ¡Â»â€ºi khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng")
    @Size(min = 6, max = 50, message = "MÃ¡ÂºÂ­t khÃ¡ÂºÂ©u phÃ¡ÂºÂ£i tÃ¡Â»Â« 6-50 kÃƒÂ½ tÃ¡Â»Â±")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).+$", message = "MÃ¡ÂºÂ­t khÃ¡ÂºÂ©u phÃ¡ÂºÂ£i chÃ¡Â»Â©a ÃƒÂ­t nhÃ¡ÂºÂ¥t 1 chÃ¡Â»Â¯ cÃƒÂ¡i vÃƒÂ  1 chÃ¡Â»Â¯ sÃ¡Â»â€˜")
    @Schema(description = "New password (6-50 chars, must contain letters and numbers)", example = "NewPass123")
    private String newPassword;

    @NotBlank(message = "XÃƒÂ¡c nhÃ¡ÂºÂ­n mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng")
    @Schema(description = "Confirm new password", example = "NewPass123")
    private String confirmPassword;
}
