package com.dental.clinic.management.authentication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;

import com.dental.clinic.management.authentication.dto.request.LoginRequest;
import com.dental.clinic.management.authentication.dto.request.RefreshTokenRequest;
import com.dental.clinic.management.authentication.dto.response.LoginResponse;
import com.dental.clinic.management.authentication.dto.response.RefreshTokenResponse;
import com.dental.clinic.management.authentication.service.AuthenticationService;
import com.dental.clinic.management.authentication.service.TokenBlacklistService;
import com.dental.clinic.management.exception.authentication.RateLimitExceededException;
import com.dental.clinic.management.utils.RateLimiter;
import com.dental.clinic.management.utils.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    private final RateLimiter rateLimiter;

    public AuthenticationController(AuthenticationService authenticationService,
            TokenBlacklistService tokenBlacklistService,
            RateLimiter rateLimiter) {
        this.authenticationService = authenticationService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.rateLimiter = rateLimiter;
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
    @ApiMessage("Đăng nhập thành công")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authenticationService.login(request);

        // Tạo response body - COPY ALL FIELDS từ loginResponse
        LoginResponse responseBody = new LoginResponse(
                loginResponse.getToken(),
                loginResponse.getTokenExpiresAt(),
                null, // Không trả refresh token trong body
                0, // Không cần refresh token expiry
                loginResponse.getUsername(),
                loginResponse.getEmail(),
                loginResponse.getRoles(),
                loginResponse.getPermissions());

        // Copy các fields quan trọng khác
        responseBody.setBaseRole(loginResponse.getBaseRole());
        responseBody.setGroupedPermissions(loginResponse.getGroupedPermissions());
        responseBody.setEmploymentType(loginResponse.getEmploymentType());
        responseBody.setMustChangePassword(loginResponse.getMustChangePassword());

        // Set refresh token vào HTTP-only cookie
        if (loginResponse.getRefreshToken() != null) {
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // Set true khi dùng HTTPS
                    .path("/") // Áp dụng cho toàn bộ API
                    .maxAge(7 * 24 * 60 * 60) // 7 ngày
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
    @ApiMessage("Làm mới access token")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Parameter(description = "Refresh token from HTTP-only cookie", required = true) @CookieValue(value = "refreshToken", required = true) String refreshToken) {
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        RefreshTokenResponse response = authenticationService.refreshToken(request);

        // Set new refresh token vào HTTP-only cookie
        if (response.getRefreshToken() != null) {
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // Set true khi dùng HTTPS
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 ngày
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
    @ApiMessage("Đăng xuất thành công")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Authorization header with Bearer token") @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Refresh token from HTTP-only cookie") @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        // Blacklist access token if provided
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(accessToken);
        }

        // Vô hiệu hóa refresh token trong database
        if (refreshToken != null) {
            authenticationService.logout(refreshToken);
        }

        // Xóa refresh token cookie
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // Set true khi dùng HTTPS
                .path("/")
                .maxAge(0) // Xóa cookie
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
    @ApiMessage("Lấy danh sách quyền thành công")
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
    @ApiMessage("Xác thực email thành công")
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
    @ApiMessage("Đã gửi lại email xác thực")
    public ResponseEntity<Void> resendVerification(
            @Valid @RequestBody com.dental.clinic.management.authentication.dto.ResendVerificationRequest request) {
        authenticationService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * Initiate password reset process.
     * For security reasons, always returns success even if email doesn't exist.
     * Rate limited: 3 requests per 15 minutes per IP address.
     *
     * @param request contains email address
     * @param httpRequest to extract client IP address
     * @return 200 OK with success message
     * @throws RateLimitExceededException if too many requests
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Initiate password reset process. Sends password reset email to user if account exists. Always returns success to prevent email enumeration. Rate limited to 3 requests per 15 minutes.")
    @ApiMessage("Đã gửi email đặt lại mật khẩu")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody com.dental.clinic.management.authentication.dto.ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        
        // Rate limiting: 3 requests per 15 minutes per IP
        String clientIp = getClientIp(httpRequest);
        String rateLimitKey = clientIp + ":forgot-password";
        
        if (!rateLimiter.isAllowed(rateLimitKey, 3, 15)) {
            long retryAfter = rateLimiter.getSecondsUntilReset(rateLimitKey, 15);
            org.slf4j.LoggerFactory.getLogger(AuthenticationController.class)
                .warn("Rate limit exceeded for forgot-password from IP: {} - Retry after {} seconds", clientIp, retryAfter);
            throw new RateLimitExceededException(
                "Bạn đã vượt quá số lần yêu cầu cho phép. Vui lòng thử lại sau " + (retryAfter / 60 + 1) + " phút.",
                retryAfter
            );
        }
        
        org.slf4j.LoggerFactory.getLogger(AuthenticationController.class)
            .info("Forgot password request from IP: {} for email: {}", clientIp, request.getEmail());
        
        authenticationService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * Reset password using token from email.
     * Rate limited: 5 attempts per 10 minutes per IP address to prevent brute force.
     *
     * @param request contains token, new password and confirm password
     * @param httpRequest to extract client IP address
     * @return 200 OK if password reset successful
     * @throws com.dental.clinic.management.exception.authentication.InvalidTokenException if token is invalid
     * @throws com.dental.clinic.management.exception.authentication.TokenExpiredException if token has expired
     * @throws IllegalArgumentException if passwords don't match
     * @throws RateLimitExceededException if too many attempts
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using token from email. User must provide new password and confirm password. Rate limited to 5 attempts per 10 minutes to prevent brute force.")
    @ApiMessage("Đặt lại mật khẩu thành công")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody com.dental.clinic.management.authentication.dto.ResetPasswordRequest request,
            HttpServletRequest httpRequest) {
        
        // Rate limiting: 5 attempts per 10 minutes per IP
        String clientIp = getClientIp(httpRequest);
        String rateLimitKey = clientIp + ":reset-password";
        
        if (!rateLimiter.isAllowed(rateLimitKey, 5, 10)) {
            long retryAfter = rateLimiter.getSecondsUntilReset(rateLimitKey, 10);
            org.slf4j.LoggerFactory.getLogger(AuthenticationController.class)
                .warn("Rate limit exceeded for reset-password from IP: {} - Retry after {} seconds", clientIp, retryAfter);
            throw new RateLimitExceededException(
                "Bạn đã vượt quá số lần thử cho phép. Vui lòng thử lại sau " + (retryAfter / 60 + 1) + " phút.",
                retryAfter
            );
        }
        
        org.slf4j.LoggerFactory.getLogger(AuthenticationController.class)
            .info("Reset password attempt from IP: {} with token: {}", clientIp, request.getToken());
        
        authenticationService.resetPassword(request.getToken(), request.getNewPassword(),
                request.getConfirmPassword());
        return ResponseEntity.ok().build();
    }

    /**
     * Extract client IP address from request.
     * Handles X-Forwarded-For header for proxy/load balancer scenarios.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For may contain multiple IPs, use the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
