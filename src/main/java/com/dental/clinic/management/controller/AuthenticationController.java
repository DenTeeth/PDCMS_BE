package com.dental.clinic.management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dental.clinic.management.dto.request.LoginRequest;
import com.dental.clinic.management.dto.response.LoginResponse;
import com.dental.clinic.management.dto.request.RefreshTokenRequest;
import com.dental.clinic.management.dto.response.RefreshTokenResponse;
import com.dental.clinic.management.service.AuthenticationService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

import jakarta.validation.Valid;

/**
 * REST controller handling authentication operations.
 * <p>
 * Base path: <code>/api/v1/auth</code>
 * </p>
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
     * Authenticate a user and issue an access token.
     * <p>
     * {@code POST /api/v1/auth/login}
     * </p>
     *
     * @param request login credentials (username & password)
     * @return 200 OK with {@link LoginResponse} including access token, roles and
     *         permissions
     * @throws com.dental.clinic.management.exception.BadCredentialsException if
     *                                                                        authentication
     *                                                                        fails
     */
    @PostMapping("/login")
    @ApiMessage("Đăng nhập thành công")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh an access token using a valid refresh token.
     * <p>
     * {@code POST /api/v1/auth/refresh-token}
     * </p>
     *
     * @param request payload containing a refresh token
     * @return 200 OK with new access token (and optionally new refresh token in
     *         future)
     * @throws com.dental.clinic.management.exception.BadCredentialsException if
     *                                                                        refresh
     *                                                                        token
     *                                                                        is
     *                                                                        invalid
     *                                                                        or
     *                                                                        decoding
     *                                                                        not
     *                                                                        implemented
     */
    @PostMapping("/refresh-token")
    @ApiMessage("Làm mới access token")
    public ResponseEntity<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout a user (stateless).
     * <p>
     * {@code POST /api/v1/auth/logout}
     * </p>
     * Since JWT is stateless, the client should discard its tokens. (Có thể mở
     * rộng: lưu blacklist.)
     *
     * @return 200 OK with empty body
     */
    @PostMapping("/logout")
    @ApiMessage("Đăng xuất thành công")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

}
