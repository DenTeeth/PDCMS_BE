package com.dental.clinic.management.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to resend email verification")
public class ResendVerificationRequest {

    @NotBlank(message = "Email khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng")
    @Email(message = "Email khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡")
    @Schema(description = "Email address to resend verification", example = "user@example.com")
    private String email;
}
