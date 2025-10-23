# Fix Login Response - Debugging Guide

## Vấn đề hiện tại

Khi login, response không trả về:

- `groupedPermissions` (Map<String, List<String>>)
- `baseRole` (String)
- `homePath` (String)

Response hiện tại chỉ có:

```json
{
  "token": "...",
  "tokenExpiresAt": 1761143014,
  "refreshTokenExpiresAt": 0,  // ← LƯU Ý: Giá trị này = 0 là BẤT THƯỜNG!
  "username": "admin",
  "email": "admin@dentalclinic.com",
  "roles": ["ROLE_ADMIN"],
  "permissions": ["VIEW_PATIENT", "VIEW_ACCOUNT", ...]
}
```

## Phân tích vấn đề

### 1. Code đã được sửa đúng

File `AuthenticationService.java` line 145-150:

```java
response.setBaseRole(baseRoleName);
response.setHomePath(homePath);
response.setSidebar(sidebar);
response.setGroupedPermissions(groupedPermissions);
```

File `LoginResponse.java` có đầy đủ getters/setters:

```java
public Map<String, List<String>> getGroupedPermissions() { ... }
public String getBaseRole() { ... }
public String getHomePath() { ... }
```

### 2. Có thể bị lỗi ở đâu?

**Khả năng 1:** `@JsonInclude(JsonInclude.Include.NON_NULL)` đang ẩn các fields null

**Khả năng 2:** `role.getBaseRole()` hoặc `role.getEffectiveHomePath()` đang trả về null

**Khả năng 3:** Code cũ đang được chạy (chưa restart server sau khi sửa)

## Bước kiểm tra

### Step 1: Kiểm tra logs khi login

Đã thêm logging trong `AuthenticationService.login()`:

```java
log.info("Login response prepared for user: {}", account.getUsername());
log.info("  baseRole: {}", response.getBaseRole());
log.info("  homePath: {}", response.getHomePath());
log.info("  groupedPermissions size: {}", groupedPermissions != null ? groupedPermissions.size() : 0);
```

**Chạy app và login, sau đó xem logs:**

```bash
# Xem logs realtime
tail -f logs/application.log

# Hoặc grep chỉ login logs
grep "Login response prepared" logs/application.log
```

**Expected output:**

```
INFO  - Login response prepared for user: admin
INFO    baseRole: admin
INFO    homePath: /app/dashboard
INFO    groupedPermissions size: 8
INFO    sidebar size: 8
```

**Nếu thấy baseRole = null hoặc homePath = null:**
→ Vấn đề ở database hoặc entity Role

### Step 2: Kiểm tra database

```sql
-- Kiểm tra role có base_role_id không
SELECT r.role_id, r.role_name, r.base_role_id, r.home_path_override,
       br.base_role_name, br.default_home_path
FROM roles r
LEFT JOIN base_roles br ON r.base_role_id = br.base_role_id
WHERE r.role_name = 'ROLE_ADMIN';
```

**Expected output:**

```
role_id | role_name  | base_role_id | home_path_override | base_role_name | default_home_path
--------|------------|--------------|-------------------|----------------|------------------
   1    | ROLE_ADMIN |      1       | NULL              | admin          | /app/dashboard
```

**Nếu base_role_id = NULL:**
→ Cần update database:

```sql
UPDATE roles SET base_role_id = 1 WHERE role_name = 'ROLE_ADMIN';
UPDATE roles SET base_role_id = 2 WHERE role_name = 'ROLE_EMPLOYEE';
UPDATE roles SET base_role_id = 3 WHERE role_name = 'ROLE_PATIENT';
```

### Step 3: Kiểm tra Role entity

```bash
# Xem Role.java có method getEffectiveHomePath() không
grep -n "getEffectiveHomePath" src/main/java/com/dental/clinic/management/role/domain/Role.java
```

**Nếu không có method này:**
→ Cần thêm vào Role.java:

```java
public String getEffectiveHomePath() {
    if (this.homePathOverride != null && !this.homePathOverride.isBlank()) {
        return this.homePathOverride;
    }
    return this.baseRole != null ? this.baseRole.getDefaultHomePath() : null;
}
```

### Step 4: Test API trực tiếp

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }' | jq .

# Kiểm tra response có các fields:
# - baseRole: "admin"
# - homePath: "/app/dashboard"
# - groupedPermissions: { "PATIENT": [...], "APPOINTMENT": [...] }
```

### Step 5: Kiểm tra Permission.module

```sql
-- Kiểm tra tất cả permissions có module không
SELECT permission_id, permission_name, module
FROM permissions
WHERE module IS NULL OR module = '';
```

**Expected:** 0 rows (tất cả permissions phải có module)

**Nếu có permissions thiếu module:**

```sql
-- Update các permission thiếu module
UPDATE permissions SET module = 'PATIENT' WHERE permission_id LIKE '%PATIENT%';
UPDATE permissions SET module = 'APPOINTMENT' WHERE permission_id LIKE '%APPOINTMENT%';
UPDATE permissions SET module = 'EMPLOYEE' WHERE permission_id LIKE '%EMPLOYEE%';
-- ... (tiếp tục cho các module khác)
```

## API mới: Get Permissions Grouped by Module

Đã tạo API mới để FE gọi xem permissions grouped by module:

### Endpoint

```
GET /api/v1/permissions/grouped-simple
```

### Response Format

```json
{
  "PATIENT": [
    "VIEW_PATIENT",
    "CREATE_PATIENT",
    "EDIT_PATIENT",
    "DELETE_PATIENT"
  ],
  "APPOINTMENT": [
    "VIEW_APPOINTMENT",
    "CREATE_APPOINTMENT",
    "EDIT_APPOINTMENT",
    "DELETE_APPOINTMENT",
    "CANCEL_APPOINTMENT"
  ],
  "EMPLOYEE": [
    "VIEW_EMPLOYEE",
    "CREATE_EMPLOYEE",
    "EDIT_EMPLOYEE",
    "DELETE_EMPLOYEE"
  ],
  "ACCOUNT": [
    "VIEW_ACCOUNT",
    "CREATE_ACCOUNT",
    "EDIT_ACCOUNT",
    "DELETE_ACCOUNT"
  ],
  "WORK_SHIFTS": [
    "VIEW_WORK_SHIFTS",
    "CREATE_WORK_SHIFTS",
    "UPDATE_WORK_SHIFTS",
    "DELETE_WORK_SHIFTS"
  ],
  "REGISTRATION": [
    "VIEW_REGISTRATION_OWN",
    "VIEW_REGISTRATION_ALL",
    "CREATE_REGISTRATION",
    "UPDATE_REGISTRATION",
    "DELETE_REGISTRATION"
  ],
  "TIME_OFF": [
    "VIEW_TIME_OFF_OWN",
    "VIEW_TIME_OFF_ALL",
    "CREATE_TIME_OFF",
    "APPROVE_TIME_OFF",
    "REJECT_TIME_OFF",
    "CANCEL_TIME_OFF_OWN",
    "CANCEL_TIME_OFF_PENDING"
  ],
  "OVERTIME": [
    "VIEW_OT_OWN",
    "VIEW_OT_ALL",
    "CREATE_OT",
    "APPROVE_OT",
    "REJECT_OT",
    "CANCEL_OT_OWN",
    "CANCEL_OT_PENDING"
  ],
  "LEAVE_BALANCE": ["VIEW_LEAVE_BALANCE_ALL", "ADJUST_LEAVE_BALANCE"],
  "TIMEOFF_TYPE": [
    "VIEW_TIMEOFF_TYPE_ALL",
    "CREATE_TIMEOFF_TYPE",
    "UPDATE_TIMEOFF_TYPE",
    "DELETE_TIMEOFF_TYPE"
  ],
  "CONTACT": [
    "VIEW_CONTACT",
    "CREATE_CONTACT",
    "UPDATE_CONTACT",
    "DELETE_CONTACT"
  ],
  "CONTACT_HISTORY": [
    "VIEW_CONTACT_HISTORY",
    "CREATE_CONTACT_HISTORY",
    "UPDATE_CONTACT_HISTORY",
    "DELETE_CONTACT_HISTORY"
  ],
  "TREATMENT": ["VIEW_TREATMENT", "CREATE_TREATMENT", "UPDATE_TREATMENT"]
}
```

### Cách dùng ở Frontend

```typescript
// Gọi API để lấy permissions grouped
const response = await fetch("/api/v1/permissions/grouped-simple", {
  headers: {
    Authorization: `Bearer ${accessToken}`,
  },
});

const permissionsByModule = await response.json();

// Hiển thị theo module
Object.entries(permissionsByModule).forEach(([module, permissions]) => {
  console.log(`\n=== ${module} ===`);
  permissions.forEach((permission) => {
    console.log(`  - ${permission}`);
  });
});

// Output:
// === PATIENT ===
//   - VIEW_PATIENT
//   - CREATE_PATIENT
//   - EDIT_PATIENT
//   - DELETE_PATIENT
//
// === APPOINTMENT ===
//   - VIEW_APPOINTMENT
//   - CREATE_APPOINTMENT
//   ...
```

### Permissions hiển thị đã được sắp xếp

Danh sách permissions trong mỗi module được sắp xếp theo:

1. `displayOrder` (nếu có) - Thứ tự hiển thị trong UI
2. `permissionId` (alphabetically) - Nếu không có displayOrder

## Các API Permissions khác

### 1. Get all permissions (flat list)

```
GET /api/v1/permissions
```

### 2. Get permissions grouped by module (with full details)

```
GET /api/v1/permissions/by-module
```

→ Trả về `Map<String, List<PermissionInfoResponse>>` (có đầy đủ info)

### 3. Get permissions with hierarchy

```
GET /api/v1/permissions/grouped
```

→ Trả về `Map<String, List<PermissionHierarchyDTO>>` (có parent-child relationship)

### 4. Get permissions grouped (simple - MỚI)

```
GET /api/v1/permissions/grouped-simple
```

→ Trả về `Map<String, List<String>>` (chỉ permission IDs, nhẹ và dễ dùng)

## Tóm tắt thay đổi

### Files modified:

1. **AuthenticationService.java**

   - Thêm logging để debug login response
   - Log baseRole, homePath, groupedPermissions size

2. **PermissionController.java**

   - Thêm endpoint `/api/v1/permissions/grouped-simple`

3. **PermissionService.java**
   - Thêm method `getPermissionsGroupedByModuleSimple()`
   - Return: `Map<String, List<String>>` (module → permission IDs)
   - Sorted by module name, then by displayOrder

### Next steps:

1. ✅ Restart application
2. ✅ Login và xem logs
3. ✅ Kiểm tra database (base_role_id, permissions.module)
4. ✅ Test login API xem có trả về baseRole/homePath/groupedPermissions chưa
5. ✅ Test API mới: `/api/v1/permissions/grouped-simple`

## Troubleshooting

### Vấn đề: baseRole = null

**Nguyên nhân:** Role không có baseRole relationship
**Fix:**

```sql
UPDATE roles SET base_role_id = 1 WHERE role_name = 'ROLE_ADMIN';
```

### Vấn đề: homePath = null

**Nguyên nhân:** BaseRole không có default_home_path
**Fix:**

```sql
UPDATE base_roles SET default_home_path = '/app/dashboard' WHERE base_role_name = 'admin';
UPDATE base_roles SET default_home_path = '/app/shifts' WHERE base_role_name = 'employee';
UPDATE base_roles SET default_home_path = '/app/appointments' WHERE base_role_name = 'patient';
```

### Vấn đề: groupedPermissions = null hoặc empty

**Nguyên nhân:** Permissions không có module field
**Fix:**

```sql
-- Check
SELECT COUNT(*) FROM permissions WHERE module IS NULL;

-- Update
UPDATE permissions SET module = 'PATIENT' WHERE permission_id LIKE '%PATIENT%';
-- ... (tiếp tục cho các module khác)
```

### Vấn đề: @JsonInclude ẩn fields

**Fix:** Remove annotation hoặc ensure fields không null

```java
// In LoginResponse.java
// @JsonInclude(JsonInclude.Include.NON_NULL)  // ← Comment out để test
public class LoginResponse { ... }
```
