package com.dental.clinic.management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dental.clinic.management.dto.request.LoginRequest;
import com.dental.clinic.management.dto.response.LoginResponse;
import com.dental.clinic.management.dto.request.RefreshTokenRequest;
import com.dental.clinic.management.dto.response.RefreshTokenResponse;
import com.dental.clinic.management.service.AuthenticationService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

import jakarta.validation.Valid;

/**
 * REST controller handling authentication operations.
 * Base path: <code>/api/v1/auth</code>
 * Provides endpoints for user login, token refresh and logout.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
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
    @ApiMessage("Đăng nhập thành công")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authenticationService.login(request);

        // Tạo response body không chứa refresh token
        LoginResponse responseBody = new LoginResponse(
                loginResponse.getToken(),
                loginResponse.getTokenExpiresAt(),
                null, // Không trả refresh token trong body
                0, // Không cần refresh token expiry
                loginResponse.getUsername(),
                loginResponse.getEmail(),
                loginResponse.getRoles(),
                loginResponse.getPermissions());

        // Set refresh token vào HTTP-only cookie
        if (loginResponse.getRefreshToken() != null) {
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // Set true khi dùng HTTPS
                    .path("/") // Áp dụng cho toàn bộ API
                    .maxAge(7 * 24 * 60 * 60) // 7 ngày
                    .build();
            // TODO - Save lại refresh token vào database
            // authenticationService.saveRefreshToken(loginResponse.getRefreshToken());
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
    @ApiMessage("Làm mới access token")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @CookieValue(value = "refreshToken", required = true) String refreshToken) {
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        RefreshTokenResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user by invalidating refresh token and clearing cookie.
     *
     * @param refreshToken refresh token from HTTP-only cookie
     * @return 200 OK with cleared refresh token cookie
     */
    @PostMapping("/logout")
    @ApiMessage("Đăng xuất thành công")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

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

}
