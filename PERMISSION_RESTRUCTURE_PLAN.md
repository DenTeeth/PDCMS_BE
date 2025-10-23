# PERMISSION RESTRUCTURE PLAN

## 📋 Tổng Quan

Document này mô tả chi tiết các thay đổi lớn về cấu trúc permissions và authentication flow.

---

## 🎯 Vấn Đề Hiện Tại

### 1. Permission Groups Quá Phân Mảnh

**Hiện tại:**

- `TIME_OFF` (Nghỉ phép): 7 permissions
- `OVERTIME` (Tăng ca): 7 permissions
- `TIME_OFF_MANAGEMENT` (Quản lý nghỉ phép): 6 permissions

**Vấn đề:**

- 3 modules liên quan chặt chẽ nhưng tách rời
- FE phải xử lý 3 groups riêng biệt
- Khó quản lý permissions

**Giải pháp:** Merge thành **1 module LEAVE_MANAGEMENT**

### 2. Path Fields Thừa Không Dùng

**Hiện tại:**

- `permissions.path`: FE không dùng (FE tự routing)
- `base_roles.default_home_path`: Không cần (FE tự quyết định home)
- `roles.home_path_override`: Không cần

**Giải pháp:** Xóa tất cả path fields

### 3. Thiếu Permissions Quan Trọng

**Thiếu:**

- ❌ ROLE module (VIEW_ROLE, CREATE_ROLE, UPDATE_ROLE, DELETE_ROLE)
- ❌ PERMISSION module (VIEW_PERMISSION, CREATE_PERMISSION, ...)
- ❌ SPECIALIZATION module (VIEW_SPECIALIZATION, ...)

**Có controllers nhưng không có permissions!**

---

## 🔄 Thay Đổi Chi Tiết

### Change #1: Merge Modules → LEAVE_MANAGEMENT

#### Before (3 modules):

```
TIME_OFF (module)
├── VIEW_TIME_OFF_ALL
├── VIEW_TIME_OFF_OWN (child of VIEW_TIME_OFF_ALL)
├── CREATE_TIME_OFF
├── APPROVE_TIME_OFF
├── REJECT_TIME_OFF
├── CANCEL_TIME_OFF_OWN
└── CANCEL_TIME_OFF_PENDING

OVERTIME (module)
├── VIEW_OT_ALL
├── VIEW_OT_OWN (child of VIEW_OT_ALL)
├── CREATE_OT
├── APPROVE_OT
├── REJECT_OT
├── CANCEL_OT_OWN
└── CANCEL_OT_PENDING

TIME_OFF_MANAGEMENT (module)
├── VIEW_TIMEOFF_TYPE_ALL
├── CREATE_TIMEOFF_TYPE
├── UPDATE_TIMEOFF_TYPE
├── DELETE_TIMEOFF_TYPE
├── VIEW_LEAVE_BALANCE_ALL
└── ADJUST_LEAVE_BALANCE

Total: 20 permissions trong 3 modules
```

#### After (1 module):

```
LEAVE_MANAGEMENT (module)
├── VIEW_LEAVE_ALL (parent - xem tất cả leave & overtime)
├── VIEW_LEAVE_OWN (child - chỉ xem của mình)
│
├── Time Off Actions:
│   ├── CREATE_TIME_OFF
│   ├── APPROVE_TIME_OFF
│   ├── REJECT_TIME_OFF
│   ├── CANCEL_TIME_OFF_OWN
│   └── CANCEL_TIME_OFF_PENDING
│
├── Overtime Actions:
│   ├── CREATE_OVERTIME
│   ├── APPROVE_OVERTIME
│   ├── REJECT_OVERTIME
│   ├── CANCEL_OVERTIME_OWN
│   └── CANCEL_OVERTIME_PENDING
│
└── Management:
    ├── VIEW_TIMEOFF_TYPE
    ├── CREATE_TIMEOFF_TYPE
    ├── UPDATE_TIMEOFF_TYPE
    ├── DELETE_TIMEOFF_TYPE
    ├── VIEW_LEAVE_BALANCE_ALL
    └── ADJUST_LEAVE_BALANCE

Total: 20 permissions trong 1 module (dễ quản lý hơn)
```

**Benefits:**

- ✅ FE chỉ cần handle 1 group "Leave Management"
- ✅ Logic liên quan (nghỉ phép, tăng ca, số dư) gom 1 chỗ
- ✅ Dễ assign permissions cho roles

---

### Change #2: Add Missing Modules

#### New Modules:

**ROLE Module:**

```sql
INSERT INTO permissions VALUES
('VIEW_ROLE', 'VIEW_ROLE', 'ROLE', 'Xem danh sách vai trò', ...),
('CREATE_ROLE', 'CREATE_ROLE', 'ROLE', 'Tạo vai trò mới', ...),
('UPDATE_ROLE', 'UPDATE_ROLE', 'ROLE', 'Cập nhật vai trò', ...),
('DELETE_ROLE', 'DELETE_ROLE', 'ROLE', 'Xóa vai trò', ...);
```

**PERMISSION Module:**

```sql
INSERT INTO permissions VALUES
('VIEW_PERMISSION', 'VIEW_PERMISSION', 'PERMISSION', 'Xem danh sách quyền', ...),
('CREATE_PERMISSION', 'CREATE_PERMISSION', 'PERMISSION', 'Tạo quyền mới', ...),
('UPDATE_PERMISSION', 'UPDATE_PERMISSION', 'PERMISSION', 'Cập nhật quyền', ...),
('DELETE_PERMISSION', 'DELETE_PERMISSION', 'PERMISSION', 'Xóa quyền', ...);
```

**SPECIALIZATION Module:**

```sql
INSERT INTO permissions VALUES
('VIEW_SPECIALIZATION', 'VIEW_SPECIALIZATION', 'SPECIALIZATION', 'Xem chuyên khoa', ...),
('CREATE_SPECIALIZATION', 'CREATE_SPECIALIZATION', 'SPECIALIZATION', 'Tạo chuyên khoa', ...),
('UPDATE_SPECIALIZATION', 'UPDATE_SPECIALIZATION', 'SPECIALIZATION', 'Cập nhật', ...),
('DELETE_SPECIALIZATION', 'DELETE_SPECIALIZATION', 'SPECIALIZATION', 'Xóa', ...);
```

**Why needed:**

- ✅ RoleController exists → Needs permissions
- ✅ PermissionController exists → Needs permissions
- ✅ SpecializationController exists → Needs permissions

---

### Change #3: Remove Path Columns

#### Database Schema Changes:

```sql
-- Remove from permissions table
ALTER TABLE permissions DROP COLUMN IF EXISTS path;

-- Remove from base_roles table
ALTER TABLE base_roles DROP COLUMN IF EXISTS default_home_path;

-- Remove from roles table
ALTER TABLE roles DROP COLUMN IF EXISTS home_path_override;
```

#### Entity Changes:

**Permission.java:**

```java
// DELETE THIS:
@Column(name = "path", length = 200)
private String path;
```

**BaseRole.java:**

```java
// DELETE THIS:
@Column(name = "default_home_path", length = 100)
private String defaultHomePath;
```

**Role.java:**

```java
// DELETE THIS:
@Column(name = "home_path_override", length = 100)
private String homePathOverride;
```

**Why remove:**

- ❌ FE không dùng path từ BE
- ❌ FE tự handle routing based on permissions
- ❌ Giảm coupling giữa BE và FE

---

### Change #4: Update LoginResponse

#### Before:

```java
public class LoginResponse {
    private String token;
    private Long expiresAt;
    private String refreshToken;
    private Long refreshExpiresIn;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> permissions;

    // Các field này sẽ XÓA:
    private String baseRole;  // ❌ DELETE
    private String homePath;  // ❌ DELETE
    private List<SidebarItem> sidebar;  // ❌ DELETE

    // Giữ lại field này:
    private Map<String, List<String>> groupedPermissions;  // ✅ KEEP
    private EmploymentType employmentType;  // ✅ KEEP
}
```

#### After:

```java
public class LoginResponse {
    private String token;
    private Long expiresAt;
    private String refreshToken;
    private Long refreshExpiresIn;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> permissions;
    private Map<String, List<String>> groupedPermissions;  // ✅ FE dùng này để routing
    private EmploymentType employmentType;
}
```

**FE sẽ xử lý:**

```typescript
// FE logic
const loginResponse = await api.login(credentials);

// FE tự quyết định home path based on permissions
const homePath = determineHomePath(loginResponse.groupedPermissions);

// FE tự build sidebar based on permissions
const sidebar = buildSidebar(loginResponse.groupedPermissions);

// FE tự xác định layout (admin/employee/patient) based on roles
const layout = determineLayout(loginResponse.roles);
```

---

## 📊 Module Grouping Summary

### Final Module Structure:

| Module               | Permissions | Description                     |
| -------------------- | ----------- | ------------------------------- |
| **ACCOUNT**          | 4           | Quản lý tài khoản               |
| **EMPLOYEE**         | 4           | Quản lý nhân viên               |
| **PATIENT**          | 4           | Quản lý bệnh nhân               |
| **CONTACT**          | 4           | Liên hệ khách hàng              |
| **CONTACT_HISTORY**  | 4           | Lịch sử liên hệ                 |
| **APPOINTMENT**      | 4           | Lịch hẹn                        |
| **TREATMENT**        | 3           | Điều trị                        |
| **WORK_SHIFTS**      | 4           | Ca làm việc                     |
| **REGISTRATION**     | 5           | Đăng ký ca                      |
| **LEAVE_MANAGEMENT** | 20          | ✅ Nghỉ phép & Tăng ca (MERGED) |
| **ROLE**             | 4           | ✅ Quản lý vai trò (NEW)        |
| **PERMISSION**       | 4           | ✅ Quản lý quyền (NEW)          |
| **SPECIALIZATION**   | 4           | ✅ Quản lý chuyên khoa (NEW)    |

**Total: 13 modules, ~68 permissions**

---

## 🚀 Implementation Steps

### Step 1: Run Database Migration ✅

```bash
# Connect to PostgreSQL
psql -U root -h localhost -d dental_clinic_db

# Run migration
\i d:/Code/PDCMS_BE/src/main/resources/db/migration_restructure_permissions.sql

# Verify
SELECT module, COUNT(*) as permission_count
FROM permissions
GROUP BY module
ORDER BY module;
```

**Expected output:**

```
module              | permission_count
--------------------|-----------------
ACCOUNT             | 4
EMPLOYEE            | 4
LEAVE_MANAGEMENT    | 20  ← NEW
PATIENT             | 4
PERMISSION          | 4   ← NEW
REGISTRATION        | 5
ROLE                | 4   ← NEW
SPECIALIZATION      | 4   ← NEW
...
```

### Step 2: Update Entity Classes

**Files to update:**

1. `Permission.java` - Remove `path` field
2. `BaseRole.java` - Remove `defaultHomePath` field
3. `Role.java` - Remove `homePathOverride` field

### Step 3: Update AuthenticationService

**Remove logic:**

```java
// DELETE: baseRole calculation
// DELETE: homePath calculation (effective path logic)
// DELETE: sidebar building

// KEEP: groupedPermissions
```

### Step 4: Update LoginResponse

**Remove fields:**

- `baseRole`
- `homePath`
- `sidebar`

**Keep:**

- `groupedPermissions`
- `employmentType`

### Step 5: Test API

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123456"}'
```

**Expected response:**

```json
{
  "token": "...",
  "expiresAt": 1234567890,
  "refreshToken": "...",
  "username": "admin",
  "email": "admin@dentalclinic.com",
  "roles": ["ROLE_ADMIN"],
  "permissions": ["VIEW_ACCOUNT", "CREATE_ACCOUNT", ...],
  "groupedPermissions": {
    "ACCOUNT": ["VIEW_ACCOUNT", "CREATE_ACCOUNT", "UPDATE_ACCOUNT", "DELETE_ACCOUNT"],
    "EMPLOYEE": ["VIEW_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE"],
    "LEAVE_MANAGEMENT": [
      "VIEW_LEAVE_ALL", "CREATE_TIME_OFF", "APPROVE_TIME_OFF",
      "CREATE_OVERTIME", "APPROVE_OVERTIME", "VIEW_TIMEOFF_TYPE", ...
    ],
    "ROLE": ["VIEW_ROLE", "CREATE_ROLE", "UPDATE_ROLE", "DELETE_ROLE"],
    "PERMISSION": ["VIEW_PERMISSION", "CREATE_PERMISSION", ...],
    "SPECIALIZATION": ["VIEW_SPECIALIZATION", "CREATE_SPECIALIZATION", ...]
  },
  "employmentType": "FULL_TIME"
}
```

---

## ⚠️ Breaking Changes

### For Backend:

1. ❌ `Permission.getPath()` → Will be removed
2. ❌ `BaseRole.getDefaultHomePath()` → Will be removed
3. ❌ `Role.getHomePathOverride()` → Will be removed
4. ❌ `LoginResponse.getBaseRole()` → Will be removed
5. ❌ `LoginResponse.getHomePath()` → Will be removed
6. ❌ `LoginResponse.getSidebar()` → Will be removed

### For Frontend:

1. ✅ FE phải tự build sidebar from `groupedPermissions`
2. ✅ FE phải tự quyết định home path based on roles/permissions
3. ✅ FE phải tự xác định layout (admin/employee/patient) based on roles

---

## 🧪 Testing Checklist

### Database Tests:

- [ ] Run migration script successfully
- [ ] Verify LEAVE_MANAGEMENT module has 20 permissions
- [ ] Verify ROLE module has 4 permissions
- [ ] Verify PERMISSION module has 4 permissions
- [ ] Verify SPECIALIZATION module has 4 permissions
- [ ] Verify old modules (TIME_OFF, OVERTIME, TIME_OFF_MANAGEMENT) are deleted
- [ ] Verify path columns are removed from tables

### API Tests:

- [ ] Login returns groupedPermissions with LEAVE_MANAGEMENT
- [ ] Login does NOT return baseRole, homePath, sidebar
- [ ] Manager role has all LEAVE_MANAGEMENT permissions
- [ ] Doctor role has VIEW_LEAVE_OWN + CREATE permissions
- [ ] Admin role has all new permissions (ROLE, PERMISSION, SPECIALIZATION)

### Controller Tests:

- [ ] RoleController endpoints require ROLE permissions
- [ ] PermissionController endpoints require PERMISSION permissions
- [ ] SpecializationController endpoints require SPECIALIZATION permissions

---

## 📝 Next Tasks

### Task #1: Default Password cho tài khoản mới ✅

**Yêu cầu:**

- Customer/Patient mới tạo → password mặc định `123456`
- Gửi email/SMS thông báo
- Bắt buộc đổi password lần đầu login
- Implement email verification

**Files to update:**

- `PatientService.createPatient()`
- `CustomerContactService.createCustomer()`
- Add EmailService
- Add SMSService

### Task #2: Update FE Routing Logic

**FE needs to implement:**

```typescript
function determineHomePath(
  groupedPermissions: Record<string, string[]>
): string {
  // Logic: Based on highest priority permission
  if (groupedPermissions.ACCOUNT) return "/admin/accounts";
  if (groupedPermissions.EMPLOYEE) return "/app/employees";
  if (groupedPermissions.PATIENT) return "/app/patients";
  return "/app/dashboard";
}

function buildSidebar(
  groupedPermissions: Record<string, string[]>
): SidebarItem[] {
  return Object.entries(groupedPermissions).map(([module, perms]) => ({
    module,
    items: perms.filter((p) => isViewPermission(p)),
  }));
}
```

---

## ✅ Benefits Summary

### Improved Organization:

- ✅ 3 modules → 1 module (LEAVE_MANAGEMENT)
- ✅ All leave/overtime logic in one place
- ✅ Easier permission management

### Complete Coverage:

- ✅ All controllers now have permissions
- ✅ No missing CRUD operations
- ✅ Proper role assignments

### Better Separation:

- ✅ BE only returns raw permissions
- ✅ FE handles routing logic
- ✅ Less coupling between BE/FE

### Cleaner Database:

- ✅ No unused `path` columns
- ✅ No redundant `home_path` fields
- ✅ Simpler schema

---

**Ready to proceed?**

1. Review migration script: `migration_restructure_permissions.sql`
2. Backup database before running
3. Run migration
4. Update entity classes
5. Update AuthenticationService
6. Test API responses
