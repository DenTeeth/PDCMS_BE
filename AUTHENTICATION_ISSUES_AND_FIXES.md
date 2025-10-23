# Authentication Issues and Required Fixes

**Date**: October 22, 2025
**Status**: 🔴 CRITICAL - Needs immediate attention

---

## 🔍 Issues Identified

### 1. ❌ Login Response - Permissions không được grouped

**Current Implementation:**

```java
// LoginResponse trả về:
List<String> permissions; // ["CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "VIEW_PATIENT", ...]
```

**Problem:**

- FE nhận được 1 danh sách permissions dài phẳng (flat list)
- Rất khó để FE group và hiển thị theo module
- BE phải chịu trách nhiệm group data trước khi trả về FE

**Solution Required:**

```java
// LoginResponse should return:
Map<String, List<String>> permissions; // {"EMPLOYEE": ["CREATE_EMPLOYEE", "UPDATE_EMPLOYEE"], "PATIENT": ["VIEW_PATIENT"]}
```

---

### 2. ✅ Login Response - BaseRole và HomePath (ALREADY FIXED)

**Current Implementation:** ✅ Đã đúng

```java
response.setBaseRole(baseRoleName); // "admin", "employee", "patient"
response.setHomePath(homePath); // "/dashboard/admin" or "/dashboard/employee"
```

**Status:** ✔️ No action needed - already working correctly

---

### 3. ❌ Permission GetAll - Không trả về grouped format

**Current Problem:**

```java
@GetMapping("")
public ResponseEntity<List<PermissionInfoResponse>> getAllPermissions() {
    // Returns flat list - WRONG for FE consumption
}
```

**Issue:**

- Endpoint `/api/v1/permissions` trả về list phẳng
- FE phải tự group - Vi phạm nguyên tắc BE xử lý data

**Solution:**

- Endpoint mặc định nên trả về grouped format
- Hoặc tạo endpoint riêng cho grouped (đã có: `/api/v1/permissions/grouped`)
- **Recommend:** Change default endpoint to return grouped data

---

### 4. ❌ RefreshToken - Không link đúng với Account table

**Current RefreshToken Entity:**

```java
@Column(name = "account_id", nullable = false)
private String accountId; // ❌ String type - WRONG!
```

**Account Entity:**

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer accountId; // ✅ Integer type
```

**Problems:**

1. Type mismatch: RefreshToken uses `String accountId` but Account uses `Integer accountId`
2. No FK relationship defined
3. accountId trong RefreshToken không được sử dụng khi tạo/xóa token
4. Không thể trace token về user cụ thể

**Required Fixes:**

```java
// Option 1: Direct FK (Recommended)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "account_id", nullable = false)
private Account account;

// Option 2: Keep accountId but fix type
@Column(name = "account_id", nullable = false)
private Integer accountId; // Changed from String to Integer
```

---

### 5. ❌ RefreshToken - Không được lưu khi login

**Current login() method:**

```java
public LoginResponse login(LoginRequest request) {
    // ... authentication ...
    String refreshToken = securityUtil.createRefreshToken(account.getUsername());
    // ❌ Token NOT saved to database!
    return response;
}
```

**Problem:**

- RefreshToken được tạo nhưng KHÔNG được persist vào database
- Table `refresh_tokens` sẽ empty
- Không thể invalidate token khi logout
- Không thể audit user sessions

**Required Fix:**

```java
public LoginResponse login(LoginRequest request) {
    // ... existing code ...

    // Save refresh token to database
    String refreshTokenString = securityUtil.createRefreshToken(account.getUsername());

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setId(UUID.randomUUID().toString());
    refreshToken.setAccount(account); // or setAccountId(account.getAccountId())
    refreshToken.setTokenHash(hashToken(refreshTokenString));
    refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
    refreshToken.setIsActive(true);

    refreshTokenRepository.save(refreshToken);

    // ... return response ...
}
```

---

### 6. ❌ RefreshToken - Rotation không được lưu

**Current refreshToken() method:**

```java
public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
    // ... decode token ...

    String newRefresh = securityUtil.createRefreshToken(username);
    // ❌ New token NOT saved!
    // ❌ Old token NOT invalidated!

    return new RefreshTokenResponse(newAccess, accessExp, newRefresh, refreshExp);
}
```

**Problem:**

- Token rotation không được persist
- Old token vẫn có thể reuse (security risk!)
- Không có audit trail

**Required Fix:**

```java
public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
    // ... decode old token ...

    // 1. Invalidate old token
    String oldTokenHash = hashToken(request.getRefreshToken());
    refreshTokenRepository.findByTokenHash(oldTokenHash)
        .ifPresent(token -> {
            token.setIsActive(false);
            refreshTokenRepository.save(token);
        });

    // 2. Create and save new token
    String newRefreshString = securityUtil.createRefreshToken(username);

    RefreshToken newRefreshToken = new RefreshToken();
    newRefreshToken.setId(UUID.randomUUID().toString());
    newRefreshToken.setAccount(account);
    newRefreshToken.setTokenHash(hashToken(newRefreshString));
    newRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
    newRefreshToken.setIsActive(true);

    refreshTokenRepository.save(newRefreshToken);

    return new RefreshTokenResponse(newAccess, accessExp, newRefreshString, refreshExp);
}
```

---

### 7. ❌ TokenBlacklistService - In-memory only

**Current Implementation:**

```java
@Service
public class TokenBlacklistService {
    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    // ❌ Lost on server restart!
}
```

**Problems:**

1. Tokens lost when server restarts
2. Không scale với multiple instances (load balancer)
3. Memory leak risk với high traffic

**Solutions:**

**Option 1: Use Database (Recommended for current setup)**

```java
@Entity
@Table(name = "blacklisted_tokens")
public class BlacklistedToken {
    @Id
    private String tokenHash;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private LocalDateTime blacklistedAt;
    private LocalDateTime expiresAt;
}
```

**Option 2: Use Redis (Best for production)**

```java
@Service
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;

    public void blacklistToken(String token) {
        // Set with TTL = token expiry
        redisTemplate.opsForValue().set(
            "blacklist:" + tokenHash,
            "true",
            Duration.between(now, expiryTime)
        );
    }
}
```

---

### 8. ❌ Missing Database Table: refresh_tokens

**Problem:**

- RefreshToken entity exists
- Repository exists
- **BUT table doesn't exist in schema.sql!**

**Required SQL:**

```sql
CREATE TABLE refresh_tokens (
    id VARCHAR(36) PRIMARY KEY,
    account_id INTEGER NOT NULL,
    token_hash VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_account_id ON refresh_tokens(account_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
```

---

### 9. ❌ Missing Database Table: blacklisted_tokens

**Required SQL:**

```sql
CREATE TABLE blacklisted_tokens (
    token_hash VARCHAR(512) PRIMARY KEY,
    account_id INTEGER,
    blacklisted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    reason VARCHAR(255), -- 'LOGOUT', 'PASSWORD_CHANGED', 'ADMIN_REVOKED'
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);

CREATE INDEX idx_blacklisted_tokens_account_id ON blacklisted_tokens(account_id);
CREATE INDEX idx_blacklisted_tokens_expires_at ON blacklisted_tokens(expires_at);
```

---

## 📋 Implementation Checklist

### High Priority (Critical for Security)

- [ ] **Fix RefreshToken.accountId type** (String → Integer or use @ManyToOne)
- [ ] **Save RefreshToken on login**
- [ ] **Implement token rotation with persistence**
- [ ] **Create refresh_tokens table in schema.sql**
- [ ] **Link RefreshToken to Account with FK**

### Medium Priority (User Experience)

- [ ] **Group permissions in LoginResponse**
- [ ] **Change default /api/v1/permissions to return grouped data**
- [ ] **Add /api/v1/permissions/flat endpoint for legacy support**

### Low Priority (Future Improvements)

- [ ] **Migrate TokenBlacklistService to database**
- [ ] **Create blacklisted_tokens table**
- [ ] **Add cleanup job for expired tokens**
- [ ] **Consider Redis for production scaling**

---

## 🎯 Recommended Fix Order

1. **Database Schema** (Foundation)

   - Create `refresh_tokens` table
   - Create `blacklisted_tokens` table

2. **RefreshToken Entity** (Fix Type)

   - Change `accountId` from String to Integer OR use @ManyToOne

3. **AuthenticationService.login()** (Save Token)

   - Persist RefreshToken on successful login

4. **AuthenticationService.refreshToken()** (Rotation)

   - Invalidate old token
   - Save new token

5. **AuthenticationService.logout()** (Cleanup)

   - Mark RefreshToken as inactive
   - Add to blacklist

6. **LoginResponse** (Group Permissions)

   - Change permissions from List to Map<String, List<String>>

7. **PermissionController** (Default Endpoint)
   - Return grouped data by default

---

## 🔐 Security Considerations

### Current Risks:

1. ❌ Refresh tokens can be reused after logout (no persistence)
2. ❌ Blacklist lost on restart (access tokens not invalidated)
3. ❌ No audit trail for token usage
4. ❌ Cannot revoke all sessions for a user

### After Fixes:

1. ✅ Tokens properly invalidated in database
2. ✅ Persistent blacklist survives restarts
3. ✅ Full audit trail via database
4. ✅ Can revoke all user sessions via account_id

---

## 📚 Reference Implementation

### Complete login() method:

```java
@Transactional
public LoginResponse login(LoginRequest request) {
    // 1. Authenticate
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword()
        )
    );

    // 2. Load account with role & permissions
    Account account = accountRepository
        .findByUsernameWithRoleAndPermissions(request.getUsername())
        .orElseThrow(() -> new BadCredentialsException("Account not found"));

    Role role = account.getRole();

    // 3. Group permissions by module
    Map<String, List<String>> groupedPermissions = role.getPermissions()
        .stream()
        .collect(Collectors.groupingBy(
            Permission::getModule,
            Collectors.mapping(Permission::getPermissionId, Collectors.toList())
        ));

    // 4. Generate tokens
    String accessToken = securityUtil.createAccessToken(
        account.getUsername(),
        List.of(role.getRoleName()),
        role.getPermissions().stream()
            .map(Permission::getPermissionId)
            .collect(Collectors.toList())
    );

    String refreshTokenString = securityUtil.createRefreshToken(account.getUsername());

    // 5. Save refresh token to database
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setId(UUID.randomUUID().toString());
    refreshToken.setAccount(account); // FK relationship
    refreshToken.setTokenHash(hashToken(refreshTokenString));
    refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
    refreshToken.setIsActive(true);
    refreshTokenRepository.save(refreshToken);

    // 6. Build response
    long now = Instant.now().getEpochSecond();
    long accessExp = now + securityUtil.getAccessTokenValiditySeconds();
    long refreshExp = now + securityUtil.getRefreshTokenValiditySeconds();

    LoginResponse response = new LoginResponse();
    response.setToken(accessToken);
    response.setTokenExpiresAt(accessExp);
    response.setRefreshToken(refreshTokenString);
    response.setRefreshTokenExpiresAt(refreshExp);
    response.setUsername(account.getUsername());
    response.setEmail(account.getEmail());
    response.setRoles(List.of(role.getRoleName()));
    response.setGroupedPermissions(groupedPermissions); // ← GROUPED!
    response.setBaseRole(role.getBaseRole().getBaseRoleName());
    response.setHomePath(role.getEffectiveHomePath());
    response.setSidebar(sidebarService.generateSidebar(role.getRoleId()));

    if (account.getEmployee() != null) {
        response.setEmploymentType(account.getEmployee().getEmploymentType());
    }

    return response;
}
```

---

## ⚠️ Breaking Changes

### For Frontend:

**Before:**

```typescript
interface LoginResponse {
  permissions: string[]; // ["CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", ...]
}
```

**After:**

```typescript
interface LoginResponse {
  groupedPermissions: Record<string, string[]>; // {"EMPLOYEE": ["CREATE_EMPLOYEE", ...]}
}
```

**Migration Guide:**

```typescript
// Old code:
const hasPermission = response.permissions.includes("CREATE_EMPLOYEE");

// New code:
const hasPermission = Object.values(response.groupedPermissions)
  .flat()
  .includes("CREATE_EMPLOYEE");

// Or check by module:
const employeePerms = response.groupedPermissions["EMPLOYEE"] || [];
const canCreateEmployee = employeePerms.includes("CREATE_EMPLOYEE");
```

---

**Priority:** 🔴 CRITICAL
**Estimated Effort:** 4-6 hours
**Risk Level:** HIGH (Security-related)
**Dependencies:** Database migration required

**Recommended:** Fix all High Priority items before deploying to production.
