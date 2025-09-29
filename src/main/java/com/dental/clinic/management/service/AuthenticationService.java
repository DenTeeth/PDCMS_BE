package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.Account;
import com.dental.clinic.management.domain.Permission;
import com.dental.clinic.management.domain.Role;
import com.dental.clinic.management.domain.User;
import com.dental.clinic.management.dto.request.LoginRequest;
import com.dental.clinic.management.dto.response.LoginResponse;
import com.dental.clinic.management.dto.request.RefreshTokenRequest;
import com.dental.clinic.management.dto.response.RefreshTokenResponse;
import com.dental.clinic.management.dto.response.UserInfoResponse;
import com.dental.clinic.management.exception.AccountNotFoundException;
import com.dental.clinic.management.repository.AccountRepository;
import com.dental.clinic.management.repository.RefreshTokenRepository;
import com.dental.clinic.management.utils.security.SecurityUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.security.MessageDigest;

/**
 * Service layer for authentication & user identity operations.
 * <p>
 * Chức năng: xác thực, phát hành access/refresh token, làm mới access token,
 * lấy thông tin người dùng.
 * </p>
 */
@Service
@Transactional
public class AuthenticationService {

        private final AuthenticationManager authenticationManager;
        private final SecurityUtil securityUtil;
        private final AccountRepository accountRepository;
        private final RefreshTokenRepository refreshTokenRepository;

        public AuthenticationService(
                        AuthenticationManager authenticationManager,
                        SecurityUtil securityUtil,
                        AccountRepository accountRepository,
                        RefreshTokenRepository refreshTokenRepository) {
                this.authenticationManager = authenticationManager;
                this.securityUtil = securityUtil;
                this.accountRepository = accountRepository;
                this.refreshTokenRepository = refreshTokenRepository;
        }

        /**
         * Authenticate user credentials and build a {@link LoginResponse} with roles &
         * permissions.
         *
         * @param request login payload (username & password)
         * @return populated {@link LoginResponse}
         * @throws com.dental.clinic.management.exception.BadCredentialsException if
         *                                                                        authentication
         *                                                                        fails
         */
        public LoginResponse login(LoginRequest request) {
                try {
                        // Xác thực thông tin đăng nhập
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(request.getUsername(),
                                                        request.getPassword()));

                        // Lấy thông tin tài khoản kèm roles và quyền hạn
                        Account account = accountRepository.findByUsernameWithRolesAndPermissions(request.getUsername())
                                        .orElseThrow(() -> new com.dental.clinic.management.exception.BadCredentialsException(
                                                        "Account not found"));

                        // Lấy danh sách vai trò của user
                        List<String> roles = account.getRoles().stream()
                                        .map(Role::getRoleName)
                                        .collect(Collectors.toList());

                        // Lấy tất cả quyền hạn từ các vai trò (loại bỏ trùng lặp)
                        List<String> permissions = account.getRoles().stream()
                                        .flatMap(role -> role.getPermissions().stream())
                                        .map(Permission::getPermissionName)
                                        .distinct()
                                        .collect(Collectors.toList());

                        // Tạo JWT token chứa thông tin user
                        String accessToken = securityUtil.createAccessToken(account.getUsername(), roles, permissions);
                        String refreshToken = securityUtil.createRefreshToken(account.getUsername());

                        long now = Instant.now().getEpochSecond();
                        long accessExp = now + securityUtil.getAccessTokenValiditySeconds();
                        long refreshExp = now + securityUtil.getRefreshTokenValiditySeconds();

                        return new LoginResponse(
                                        accessToken,
                                        accessExp,
                                        refreshToken,
                                        refreshExp,
                                        account.getUsername(),
                                        account.getEmail(),
                                        roles,
                                        permissions);

                } catch (AuthenticationException e) {
                        throw new com.dental.clinic.management.exception.BadCredentialsException(
                                        "Invalid username or password");
                }
        }

        /**
         * Refresh the access token using a valid refresh token.
         *
         * @param request the incoming refresh token wrapper
         * @return a response containing a new access token plus existing refresh token
         *         & its expiry
         * @throws com.dental.clinic.management.exception.BadCredentialsException if the
         *                                                                        refresh
         *                                                                        token
         *                                                                        is
         *                                                                        invalid
         *                                                                        or
         *                                                                        expired
         */
        public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
                if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
                        throw new com.dental.clinic.management.exception.BadCredentialsException(
                                        "Invalid refresh token");
                }
                try {
                        // Giải mã và kiểm tra refresh token
                        var jwt = securityUtil.decodeRefreshToken(request.getRefreshToken());
                        String username = jwt.getSubject();

                        // Lấy thông tin tài khoản với roles/permissions
                        Account account = accountRepository.findByUsernameWithRolesAndPermissions(username)
                                        .orElseThrow(() -> new AccountNotFoundException(username));

                        List<String> roles = account.getRoles().stream().map(Role::getRoleName)
                                        .collect(Collectors.toList());
                        List<String> permissions = account.getRoles().stream().flatMap(r -> r.getPermissions().stream())
                                        .map(Permission::getPermissionName).distinct().collect(Collectors.toList());

                        // Tạo access token mới
                        String newAccess = securityUtil.createAccessToken(username, roles, permissions);
                        long now = Instant.now().getEpochSecond();
                        long accessExp = now + securityUtil.getAccessTokenValiditySeconds();

                        // Tạo refresh token mới (thay vì dùng cái cũ)
                        String newRefresh = securityUtil.createRefreshToken(username);
                        long refreshExp = now + securityUtil.getRefreshTokenValiditySeconds();
                        return new RefreshTokenResponse(newAccess, accessExp, newRefresh, refreshExp);
                } catch (Exception e) {
                        throw new com.dental.clinic.management.exception.BadCredentialsException(
                                        "Invalid refresh token");
                }
        }

        /**
         * Load extended user profile info including roles and permissions.
         *
         * @param username account username
         * @return {@link UserInfoResponse} with profile & authorization data
         * @throws AccountNotFoundException if account does not exist
         */
        public UserInfoResponse getUserInfo(String username) {
                Account account = accountRepository.findByUsernameWithRolesAndPermissions(username)
                                .orElseThrow(() -> new AccountNotFoundException(username));

                UserInfoResponse response = new UserInfoResponse();
                response.setId(account.getAccountId());
                response.setUsername(account.getUsername());
                response.setEmail(account.getEmail());
                response.setAccountStatus(account.getStatus() != null ? account.getStatus().name() : null);

                // Lấy danh sách vai trò
                List<String> roles = account.getRoles().stream()
                                .map(Role::getRoleName)
                                .collect(Collectors.toList());
                response.setRoles(roles);

                // Lấy tất cả quyền hạn từ các vai trò
                List<String> permissions = account.getRoles().stream()
                                .flatMap(role -> role.getPermissions().stream())
                                .map(Permission::getPermissionName)
                                .distinct()
                                .collect(Collectors.toList());
                response.setPermissions(permissions);

                // Thông tin chi tiết nếu user có profile
                if (account.getUser() != null) {
                        User profile = account.getUser();
                        response.setFullName(profile.getFullName());
                        response.setPhoneNumber(profile.getPhone());
                        response.setAddress(profile.getAddress());
                        response.setDateOfBirth(
                                        profile.getDateOfBirth() != null ? profile.getDateOfBirth().toString() : null);

                        // Lấy chuyên khoa chính (nếu có)
                        if (!profile.getSpecializations().isEmpty()) {
                                response.setSpecializationName(
                                                profile.getSpecializations().iterator().next().getSpecializationName());
                        }

                        response.setCreatedAt(profile.getCreatedAt());
                }

                return response;
        }

        /**
         * Logout user by invalidating their refresh token.
         *
         * @param refreshToken the refresh token to invalidate
         * @throws com.dental.clinic.management.exception.BadCredentialsException if
         *                                                                        refresh
         *                                                                        token
         *                                                                        is
         *                                                                        invalid
         */
        public void logout(String refreshToken) {
                if (refreshToken == null || refreshToken.isBlank()) {
                        return; // Không có token để xóa
                }

                try {
                        // Hash refresh token để tìm trong database
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hashBytes = digest.digest(refreshToken.getBytes());
                        StringBuilder hexString = new StringBuilder();
                        for (byte b : hashBytes) {
                                String hex = Integer.toHexString(0xff & b);
                                if (hex.length() == 1) {
                                        hexString.append('0');
                                }
                                hexString.append(hex);
                        }
                        String tokenHash = hexString.toString();

                        // Xóa token từ database
                        refreshTokenRepository.deleteByTokenHash(tokenHash);

                } catch (Exception e) {
                        // Token không hợp lệ hoặc đã hết hạn - ignore
                }
        }
}
