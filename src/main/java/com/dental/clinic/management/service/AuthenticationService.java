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
import com.dental.clinic.management.utils.security.SecurityUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for authentication and user identity operations.
 * <p>
 * Responsibilities: authenticate credentials, issue tokens, refresh tokens
 * (stub),
 * retrieve user profile data and perform stateless logout semantics.
 * </p>
 */
@Service
@Transactional
public class AuthenticationService {

        private final AuthenticationManager authenticationManager;
        private final SecurityUtil securityUtil;
        private final AccountRepository accountRepository;

        public AuthenticationService(
                        AuthenticationManager authenticationManager,
                        SecurityUtil securityUtil,
                        AccountRepository accountRepository) {
                this.authenticationManager = authenticationManager;
                this.securityUtil = securityUtil;
                this.accountRepository = accountRepository;
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

                        return new LoginResponse(
                                        accessToken,
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
         * Refresh an access token using a refresh token.
         * <p>
         * Hiện tại: chưa implement decode & validation chi tiết; method sẽ ném lỗi cho
         * đến khi bổ sung JwtDecoder.
         * </p>
         *
         * @param request refresh token payload
         * @return new {@link RefreshTokenResponse} (chưa khả dụng)
         * @throws com.dental.clinic.management.exception.BadCredentialsException always
         *                                                                        (not
         *                                                                        implemented)
         */
        public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
                // Giải pháp đơn giản: chỉ xác nhận chuỗi không rỗng.
                // Có thể mở rộng: verify bằng JwtDecoder & kiểm tra claim "type" = refresh.
                if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
                        throw new com.dental.clinic.management.exception.BadCredentialsException(
                                        "Invalid refresh token");
                }
                // (Giả định) parse subject: trong thực tế cần decode token để lấy subject.
                // Ở đây đơn giản: không decode -> yêu cầu nâng cấp nếu cần.
                // => Tạm thời không thể tạo access token mới nếu không decode; implement decode
                // sau.
                // Để functional: tạm throw cho tới khi có JwtDecoder.
                throw new com.dental.clinic.management.exception.BadCredentialsException(
                                "Refresh token decoding not implemented");
        }

        /**
         * Stateless logout placeholder.
         * <p>
         * JWT không cần server state để logout; client chỉ cần xóa token. Có thể mở
         * rộng với blacklist/redis.
         * </p>
         *
         * @param username current username (không dùng hiện tại)
         */
        public void logout(String username) {
                // JWT stateless -> không cần làm gì trừ khi triển khai blacklist.
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
}
