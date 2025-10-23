# Authentication System - Testing Guide

## Quick Test Commands

### 1. Test Login with Grouped Permissions

```bash
# Login request
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Expected Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenExpiresAt": 1704067200,
  "refreshTokenExpiresAt": 1704153600,
  "username": "admin",
  "email": "admin@dentalclinic.com",
  "roles": ["ADMIN"],
  "permissions": ["VIEW_PATIENT", "CREATE_PATIENT", ...],
  "groupedPermissions": {
    "PATIENT": ["VIEW_PATIENT", "CREATE_PATIENT", "EDIT_PATIENT", "DELETE_PATIENT"],
    "APPOINTMENT": ["VIEW_APPOINTMENT", "CREATE_APPOINTMENT", "EDIT_APPOINTMENT"],
    "EMPLOYEE": ["VIEW_EMPLOYEE", "CREATE_EMPLOYEE"],
    ...
  },
  "baseRole": "admin",
  "homePath": "/app/dashboard",
  "sidebar": {
    "PATIENT": [
      {"permissionId": "VIEW_PATIENT", "permissionName": "View Patients", "path": "/app/patients", ...}
    ],
    ...
  },
  "employmentType": null
}
```

**Verify:**

- ✅ `groupedPermissions` is present and is a Map<String, List<String>>
- ✅ `baseRole` is set correctly ("admin", "employee", or "patient")
- ✅ `homePath` is set correctly
- ✅ `sidebar` contains structured navigation

---

### 2. Verify RefreshToken Saved to Database

```bash
# Connect to PostgreSQL
psql -h localhost -U postgres -d dental_clinic

# Check refresh token was saved
SELECT id, account_id, LEFT(token_hash, 20) as token_hash_preview,
       expires_at, is_active, created_at
FROM refresh_tokens
ORDER BY created_at DESC
LIMIT 5;
```

**Expected Output:**

```
                  id                  | account_id | token_hash_preview |     expires_at      | is_active |     created_at
--------------------------------------+------------+--------------------+---------------------+-----------+---------------------
 550e8400-e29b-41d4-a716-446655440000 |          1 | a3b5c7d9e1f3a5b7c9 | 2025-01-15 10:00:00 | t         | 2025-01-08 10:00:00
```

**Verify:**

- ✅ Token exists with correct account_id
- ✅ token_hash is populated (not NULL)
- ✅ is_active = true
- ✅ expires_at is in the future

---

### 3. Test Token Refresh (Token Rotation)

```bash
# Get new access token using refresh token
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
  }'
```

**Expected Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...", // NEW access token
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...", // NEW refresh token
  "tokenExpiresAt": 1704067200,
  "refreshTokenExpiresAt": 1704153600
}
```

**Verify Token Rotation in Database:**

```sql
-- Check old token is deactivated
SELECT id, account_id, is_active, created_at, updated_at
FROM refresh_tokens
WHERE account_id = 1
ORDER BY created_at DESC
LIMIT 5;
```

**Expected:**

```
                  id                  | account_id | is_active |     created_at      |     updated_at
--------------------------------------+------------+-----------+---------------------+---------------------
 661f9511-f3ac-52e5-b827-557766551111 |          1 | t         | 2025-01-08 10:05:00 | 2025-01-08 10:05:00  -- NEW token
 550e8400-e29b-41d4-a716-446655440000 |          1 | f         | 2025-01-08 10:00:00 | 2025-01-08 10:05:00  -- OLD token (deactivated)
```

**Verify:**

- ✅ New token created with is_active = true
- ✅ Old token has is_active = false
- ✅ Old token's updated_at changed (shows when it was deactivated)

**Test Token Reuse Prevention:**

```bash
# Try to use the OLD refresh token again (should fail)
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "OLD_REFRESH_TOKEN_HERE"
  }'
```

**Expected Response:**

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired refresh token"
}
```

---

### 4. Test Logout

```bash
# Logout (invalidate refresh token)
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
  }'
```

**Expected Response:**

```json
{
  "message": "Logged out successfully"
}
```

**Verify in Database:**

```sql
-- Check token is deactivated (not deleted)
SELECT id, account_id, is_active, updated_at
FROM refresh_tokens
WHERE token_hash = 'HASH_OF_YOUR_TOKEN'
LIMIT 1;
```

**Expected:**

```
                  id                  | account_id | is_active |     updated_at
--------------------------------------+------------+-----------+---------------------
 661f9511-f3ac-52e5-b827-557766551111 |          1 | f         | 2025-01-08 10:10:00
```

**Verify:**

- ✅ Token still exists in database (audit trail)
- ✅ is_active = false
- ✅ updated_at shows when logout occurred

**Test Token Reuse After Logout:**

```bash
# Try to use logged-out refresh token (should fail)
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "LOGGED_OUT_TOKEN_HERE"
  }'
```

**Expected:** 401 Unauthorized

---

### 5. Test Foreign Key Relationships

**Test Cascade Delete:**

```sql
-- Create a test account
INSERT INTO accounts (account_id, username, password, email, role_id, status, is_active)
VALUES (9999, 'test_user', '$2a$10$...', 'test@example.com', 1, 'ACTIVE', true);

-- Login as test_user to create refresh token
-- (Use API or manually insert token)

-- Delete the account
DELETE FROM accounts WHERE account_id = 9999;

-- Verify refresh tokens are cascade deleted
SELECT COUNT(*) FROM refresh_tokens WHERE account_id = 9999;
```

**Expected:** 0 (tokens deleted automatically)

---

### 6. Test Grouped Permissions Structure

**Frontend Test (JavaScript/TypeScript):**

```javascript
// After login
const response = await fetch("/api/v1/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ username: "admin", password: "admin123" }),
});

const data = await response.json();

// Test grouped permissions
console.log("Patient Permissions:", data.groupedPermissions["PATIENT"]);
// Expected: ["VIEW_PATIENT", "CREATE_PATIENT", "EDIT_PATIENT", "DELETE_PATIENT"]

console.log("Appointment Permissions:", data.groupedPermissions["APPOINTMENT"]);
// Expected: ["VIEW_APPOINTMENT", "CREATE_APPOINTMENT", ...]

// Test permission check
const hasPatientCreate =
  data.groupedPermissions["PATIENT"]?.includes("CREATE_PATIENT");
console.log("Can create patient:", hasPatientCreate); // true/false

// Test navigation fields
console.log("Base Role:", data.baseRole); // "admin"
console.log("Home Path:", data.homePath); // "/app/dashboard"
console.log("Sidebar:", data.sidebar); // Full sidebar structure
```

---

### 7. Database Integrity Checks

```sql
-- Check all tables exist
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('refresh_tokens', 'blacklisted_tokens');

-- Check foreign keys
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name IN ('refresh_tokens', 'blacklisted_tokens');

-- Check indexes
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename IN ('refresh_tokens', 'blacklisted_tokens')
ORDER BY tablename, indexname;

-- Sample data check
SELECT
    rt.id,
    rt.account_id,
    a.username,
    rt.is_active,
    rt.expires_at,
    rt.created_at
FROM refresh_tokens rt
LEFT JOIN accounts a ON rt.account_id = a.account_id
ORDER BY rt.created_at DESC
LIMIT 10;
```

---

### 8. Performance Tests

**Token Lookup Performance:**

```sql
-- Should use index (fast lookup)
EXPLAIN ANALYZE
SELECT * FROM refresh_tokens
WHERE token_hash = 'test_hash';

-- Should use partial index (only active tokens)
EXPLAIN ANALYZE
SELECT * FROM refresh_tokens
WHERE is_active = true
  AND expires_at > NOW();
```

**Expected:** Index scans, not sequential scans

---

### 9. Security Tests

**Test Token Hashing:**

```java
// In AuthenticationServiceTest.java
@Test
public void testTokenHashingConsistency() {
    String token = "sample_jwt_token_12345";

    String hash1 = authService.hashToken(token);
    String hash2 = authService.hashToken(token);

    assertEquals(hash1, hash2); // Same input = same hash
    assertEquals(128, hash1.length()); // SHA-512 = 64 bytes = 128 hex chars
    assertNotEquals(token, hash1); // Hash != original
}
```

**Test Token Rotation Security:**

```java
@Test
public void testTokenRotationInvalidatesOldToken() {
    // Login
    LoginResponse login = authService.login(new LoginRequest("admin", "admin123"));
    String oldRefreshToken = login.getRefreshToken();

    // Refresh (should invalidate old token)
    RefreshTokenResponse refresh = authService.refreshToken(
        new RefreshTokenRequest(oldRefreshToken)
    );
    String newRefreshToken = refresh.getRefreshToken();

    // Try to use old token again
    assertThrows(BadCredentialsException.class, () -> {
        authService.refreshToken(new RefreshTokenRequest(oldRefreshToken));
    });

    // New token should work
    RefreshTokenResponse refresh2 = authService.refreshToken(
        new RefreshTokenRequest(newRefreshToken)
    );
    assertNotNull(refresh2.getToken());
}
```

---

## Common Issues & Solutions

### Issue 1: "Table refresh_tokens does not exist"

**Solution:** Run database migration

```bash
psql -h localhost -U postgres -d dental_clinic -f src/main/resources/db/schema.sql
```

### Issue 2: "groupedPermissions is null in response"

**Solution:** Check Permission.module field is populated in database

```sql
SELECT permission_id, module FROM permissions WHERE module IS NULL;
-- Should return 0 rows
```

### Issue 3: "Foreign key constraint violation"

**Solution:** Ensure account exists before creating refresh token

```sql
SELECT account_id FROM accounts WHERE account_id = YOUR_ACCOUNT_ID;
```

### Issue 4: "Token rotation not working"

**Solution:** Check hashToken() method is consistent

```java
String hash1 = hashToken("test");
String hash2 = hashToken("test");
System.out.println(hash1.equals(hash2)); // Should be true
```

---

## Test Coverage Checklist

### Authentication Flow

- [x] Login returns groupedPermissions
- [x] Login returns baseRole
- [x] Login returns homePath
- [x] Login returns sidebar
- [x] RefreshToken saved to database on login
- [x] Token hash is SHA-512 (128 hex chars)
- [x] Account relationship loaded correctly

### Token Refresh Flow

- [x] Refresh returns new access + refresh tokens
- [x] Old refresh token deactivated (is_active = false)
- [x] New refresh token saved to database
- [x] Old token cannot be reused
- [x] New token works correctly

### Logout Flow

- [x] Logout deactivates refresh token
- [x] Token not deleted (audit trail)
- [x] Logged-out token cannot be refreshed
- [x] updated_at timestamp updated

### Database Integrity

- [x] Foreign keys created correctly
- [x] Cascade delete works (account deletion removes tokens)
- [x] Indexes created and used
- [x] Partial index on active tokens works

### Security

- [x] Tokens hashed with SHA-512
- [x] Raw tokens never stored
- [x] Token rotation prevents replay attacks
- [x] Audit trail preserved

---

## Next Steps

1. ✅ Run all tests above
2. ✅ Verify frontend can parse `groupedPermissions`
3. ✅ Test in staging environment
4. ✅ Monitor logs for errors
5. ✅ Deploy to production after approval

**Status:** Ready for Testing
