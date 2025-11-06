package com.dental.clinic.management.authentication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;

import com.dental.clinic.management.authentication.dto.request.LoginRequest;
import com.dental.clinic.management.authentication.dto.request.RefreshTokenRequest;
import com.dental.clinic.management.authentication.dto.response.LoginResponse;
import com.dental.clinic.management.authentication.dto.response.RefreshTokenResponse;
import com.dental.clinic.management.authentication.service.AuthenticationService;
import com.dental.clinic.management.authentication.service.TokenBlacklistService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller handling authentication operations.
 * Base path: <code>/api/v1/auth</code>
 * Provides endpoints for user login, token refresh and logout.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "APIs for user authentication, token management, and logout")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthenticationController(AuthenticationService authenticationService,
            TokenBlacklistService tokenBlacklistService) {
        this.authenticationService = authenticationService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Authenticate user credentials and issue JWT tokens.
     *
     * @param request login credentials (username & password)
     * @return 200 OK with {@link LoginResponse} containing access token, roles and
     *         permissions.
     *         Refresh token is set in HTTP-only cookie.
     * @throws com.dental.clinic.management.exception.BadCredentialsException if
     *                                                                        authentication
     *                                                                        fails
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with username/password and issue JWT access token with refresh token in HTTP-only cookie")
    @ApiMessage("Ã„ÂÃ„Æ’ng nhÃ¡ÂºÂ­p thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authenticationService.login(request);

        // TÃ¡ÂºÂ¡o response body - COPY ALL FIELDS tÃ¡Â»Â« loginResponse
        LoginResponse responseBody = new LoginResponse(
                loginResponse.getToken(),
                loginResponse.getTokenExpiresAt(),
                null, // KhÃƒÂ´ng trÃ¡ÂºÂ£ refresh token trong body
                0, // KhÃƒÂ´ng cÃ¡ÂºÂ§n refresh token expiry
                loginResponse.getUsername(),
                loginResponse.getEmail(),
                loginResponse.getRoles(),
                loginResponse.getPermissions());

        // Copy cÃƒÂ¡c fields quan trÃ¡Â»Âng khÃƒÂ¡c
        responseBody.setBaseRole(loginResponse.getBaseRole());
        responseBody.setGroupedPermissions(loginResponse.getGroupedPermissions());
        responseBody.setEmploymentType(loginResponse.getEmploymentType());
        responseBody.setMustChangePassword(loginResponse.getMustChangePassword());

        // Set refresh token vÃƒÂ o HTTP-only cookie
        if (loginResponse.getRefreshToken() != null) {
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // Set true khi dÃƒÂ¹ng HTTPS
                    .path("/") // ÃƒÂp dÃ¡Â»Â¥ng cho toÃƒÂ n bÃ¡Â»â„¢ API
                    .maxAge(7 * 24 * 60 * 60) // 7 ngÃƒÂ y
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(responseBody);
        } else {
            return ResponseEntity.ok(responseBody);
        }
    }

    /**
     * Refresh access token using refresh token from HTTP-only cookie.
     *
     * @param refreshToken refresh token automatically sent from HTTP-only cookie
     * @return 200 OK with {@link RefreshTokenResponse} containing new access token
     * @throws com.dental.clinic.management.exception.BadCredentialsException if
     *                                                                        refresh
     *                                                                        token
     *                                                                        is
     *                                                                        invalid
     *                                                                        or
     *                                                                        expired
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Issue new access token using refresh token from HTTP-only cookie")
    @ApiMessage("LÃƒÂ m mÃ¡Â»â€ºi access token")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Parameter(description = "Refresh token from HTTP-only cookie", required = true) @CookieValue(value = "refreshToken", required = true) String refreshToken) {
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        RefreshTokenResponse response = authenticationService.refreshToken(request);

        // Set new refresh token vÃƒÂ o HTTP-only cookie
        if (response.getRefreshToken() != null) {
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // Set true khi dÃƒÂ¹ng HTTPS
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 ngÃƒÂ y
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Logout user by invalidating both access and refresh tokens.
     *
     * @param authHeader   Authorization header containing access token
     * @param refreshToken refresh token from HTTP-only cookie
     * @return 200 OK with cleared refresh token cookie
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate access token and refresh token, clear refresh token cookie")
    @ApiMessage("Ã„ÂÃ„Æ’ng xuÃ¡ÂºÂ¥t thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Authorization header with Bearer token") @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Refresh token from HTTP-only cookie") @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        // Blacklist access token if provided
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(accessToken);
        }

        // VÃƒÂ´ hiÃ¡Â»â€¡u hÃƒÂ³a refresh token trong database
        if (refreshToken != null) {
            authenticationService.logout(refreshToken);
        }

        // XÃƒÂ³a refresh token cookie
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // Set true khi dÃƒÂ¹ng HTTPS
                .path("/")
                .maxAge(0) // XÃƒÂ³a cookie
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .build();
    }

    /**
     * Get grouped permissions for the currently authenticated user.
     *
     * @return Map of module name to list of permission IDs that the user has
     */
    @GetMapping("/my-permissions")
    @Operation(summary = "Get my permissions grouped by module", description = "Get all permissions of the currently authenticated user, grouped by module")
    @ApiMessage("LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch quyÃ¡Â»Ân thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<java.util.Map<String, java.util.List<String>>> getMyPermissions() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        java.util.Map<String, java.util.List<String>> groupedPermissions = authenticationService
                .getMyPermissionsGrouped(username);
        return ResponseEntity.ok(groupedPermissions);
    }

    /**
     * Verify email address using token from email link.
     *
     * @param token verification token from email
     * @return 200 OK if verification successful
     * @throws com.dental.clinic.management.exception.InvalidTokenException if token
     *                                                                      is
     *                                                                      invalid
     * @throws com.dental.clinic.management.exception.TokenExpiredException if token
     *                                                                      has
     *                                                                      expired
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address", description = "Verify email address using token sent via email. This endpoint is called when user clicks verification link in email.")
    @ApiMessage("XÃƒÂ¡c thÃ¡Â»Â±c email thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<Void> verifyEmail(
            @Parameter(description = "Verification token from email", required = true) @RequestParam String token) {
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    /**
     * Resend verification email to user.
     *
     * @param request contains email address
     * @return 200 OK if email sent successfully
     * @throws com.dental.clinic.management.exception.AccountNotFoundException if
     *                                                                         account
     *                                                                         not
     *                                                                         found
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", description = "Resend verification email to user if they didn't receive it or token expired")
    @ApiMessage("Ã„ÂÃƒÂ£ gÃ¡Â»Â­i lÃ¡ÂºÂ¡i email xÃƒÂ¡c thÃ¡Â»Â±c")
    public ResponseEntity<Void> resendVerification(
            @Valid @RequestBody com.dental.clinic.management.authentication.dto.ResendVerificationRequest request) {
        authenticationService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * Initiate password reset process.
     *
     * @param request contains email address
     * @return 200 OK if reset email sent successfully
     * @throws com.dental.clinic.management.exception.AccountNotFoundException if
     *                                                                         account
     *                                                                         not
     *                                                                         found
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Initiate password reset process. Sends password reset email to user.")
    @ApiMessage("Ã„ÂÃƒÂ£ gÃ¡Â»Â­i email Ã„â€˜Ã¡ÂºÂ·t lÃ¡ÂºÂ¡i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody com.dental.clinic.management.authentication.dto.ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * Reset password using token from email.
     *
     * @param request contains token, new password and confirm password
     * @return 200 OK if password reset successful
     * @throws com.dental.clinic.management.exception.InvalidTokenException if
     *                                                                      token
     *                                                                      is
     *                                                                      invalid
     * @throws com.dental.clinic.management.exception.TokenExpiredException if
     *                                                                      token
     *                                                                      has
     *                                                                      expired
     * @throws IllegalArgumentException                                     if
     *                                                                      passwords
     *                                                                      don't
     *                                                                      match
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using token from email. User must provide new password and confirm password.")
    @ApiMessage("Ã„ÂÃ¡ÂºÂ·t lÃ¡ÂºÂ¡i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u thÃƒÂ nh cÃƒÂ´ng")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody com.dental.clinic.management.authentication.dto.ResetPasswordRequest request) {
        authenticationService.resetPassword(request.getToken(), request.getNewPassword(),
                request.getConfirmPassword());
        return ResponseEntity.ok().build();
    }

}
