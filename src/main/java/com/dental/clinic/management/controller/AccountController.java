package com.dental.clinic.management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dental.clinic.management.dto.response.UserInfoResponse;
import com.dental.clinic.management.dto.response.UserPermissionsResponse;
import com.dental.clinic.management.dto.response.UserProfileResponse;
import com.dental.clinic.management.service.AuthenticationService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

/**
 * REST controller for account profile operations.
 * <p>
 * Base path: <code>/api/v1/account</code>
 * </p>
 * Provides endpoints to retrieve authenticated user profile information.
 */
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AuthenticationService authenticationService;

    public AccountController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Get the personal profile of the currently authenticated user.
     * <p>
     * {@code GET /api/v1/account/profile}
     * </p>
     *
     * @param jwt injected JWT bearer token (lấy username từ claim "sub")
     * @return 200 OK with {@link UserProfileResponse} containing personal info and roles
     * @throws com.dental.clinic.management.exception.AccountNotFoundException if the account no longer exists
     */
    @GetMapping("/profile")
    @ApiMessage("Lấy thông tin profile cá nhân thành công")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("sub");
        UserProfileResponse userProfile = authenticationService.getUserProfile(username);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Get the permissions of the currently authenticated user.
     * <p>
     * {@code GET /api/v1/account/permissions}
     * </p>
     *
     * @param jwt injected JWT bearer token (lấy username từ claim "sub")
     * @return 200 OK with {@link UserPermissionsResponse} containing permissions
     *         only
     * @throws com.dental.clinic.management.exception.AccountNotFoundException if
     *                                                                         the
     *                                                            account
     *          no
     *                                                                         longer
     *                                                                         exists
     */
    @GetMapping("/permissions")
    @ApiMessage("Lấy quyền hạn người dùng thành công")
    public ResponseEntity<UserPermissionsResponse> getPermissions(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("sub");
        UserPermissionsResponse userPermissions = authenticationService.getUserPermissions(username);
        return ResponseEntity.ok(userPermissions);
    }

    /**
     * Get complete user information including roles and permissions.
     * <p>
     * {@code GET /api/v1/account/info}
     * </p>
     *
     * @param jwt injected JWT bearer token (lấy username từ claim "sub")
     * @return 200 OK with {@link UserInfoResponse} containing complete user info
     * @throws com.dental.clinic.management.exception.AccountNotFoundException if the account no longer exists
     */
    @GetMapping("/info")
    @ApiMessage("Lấy thông tin đầy đủ người dùng thành công")
    public ResponseEntity<UserInfoResponse> getInfo(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("sub");
        UserInfoResponse userInfo = authenticationService.getUserInfo(username);
        return ResponseEntity.ok(userInfo);
    }
}
