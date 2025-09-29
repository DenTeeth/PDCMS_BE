package com.dental.clinic.management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dental.clinic.management.dto.response.UserInfoResponse;
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
     * Get the profile of the currently authenticated user.
     * <p>
     * {@code GET /api/v1/account/profile}
     * </p>
     *
     * @param jwt injected JWT bearer token (lấy username từ claim "sub")
     * @return 200 OK with {@link UserInfoResponse} containing roles, permissions
     *         and basic profile info
     * @throws com.dental.clinic.management.exception.AccountNotFoundException if
     *                                                                         the
     *                                                                         account
     *                                                                         no
     *                                                                         longer
     *                                                                         exists
     */
    @GetMapping("/profile")
    @ApiMessage("Lấy thông tin profile thành công")
    public ResponseEntity<UserInfoResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("sub");
        UserInfoResponse userInfo = authenticationService.getUserInfo(username);
        return ResponseEntity.ok(userInfo);
    }

}
