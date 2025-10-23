# Tóm Tắt Các Thay Đổi - Login Response & Permissions API

## 🎯 Vấn Đề Cần Giải Quyết

### 1. Login không trả về đầy đủ thông tin

**Hiện tại response thiếu:**

- ❌ `groupedPermissions` (Map<String, List<String>>) - Permissions grouped theo module
- ❌ `baseRole` (String) - "admin", "employee", hoặc "patient"
- ❌ `homePath` (String) - Đường dẫn trang chủ sau khi login

**Response hiện tại chỉ có:**

```json
{
  "token": "...",
  "tokenExpiresAt": 1761143014,
  "refreshTokenExpiresAt": 0,
  "username": "admin",
  "permissions": ["VIEW_PATIENT", "VIEW_ACCOUNT", ...]  // ← Flat list
}
```

### 2. Cần API mới để FE lấy permissions grouped by module

Để FE có thể hiển thị permissions theo module một cách rõ ràng:

```
Module PATIENT
  - VIEW_PATIENT
  - CREATE_PATIENT
  - EDIT_PATIENT

Module APPOINTMENT
  - VIEW_APPOINTMENT
  - CREATE_APPOINTMENT
```

---

## ✅ Giải Pháp Đã Thực Hiện

### 1. Đã thêm logging vào AuthenticationService.login()

**File:** `AuthenticationService.java`

**Code thêm vào:**

```java
// Debug logging
log.info("Login response prepared for user: {}", account.getUsername());
log.info("  baseRole: {}", response.getBaseRole());
log.info("  homePath: {}", response.getHomePath());
log.info("  groupedPermissions size: {}", groupedPermissions != null ? groupedPermissions.size() : 0);
log.info("  sidebar size: {}", sidebar != null ? sidebar.size() : 0);
```

**Mục đích:**

- Debug xem các fields có được set đúng không
- Kiểm tra baseRole và homePath có null không

**Cách xem logs:**

```bash
# Sau khi login, xem logs
grep "Login response prepared" logs/application.log

# Hoặc xem realtime
tail -f logs/application.log | grep "baseRole\|homePath\|groupedPermissions"
```

---

### 2. Tạo API mới: Get Permissions Grouped by Module (Simple Format)

**File:** `PermissionController.java`

**Endpoint mới:**

```java
@GetMapping("/grouped-simple")
@Operation(summary = "Get permissions grouped by module (simple format)")
public ResponseEntity<Map<String, List<String>>> getGroupedPermissionsSimple() {
    Map<String, List<String>> response = permissionService.getPermissionsGroupedByModuleSimple();
    return ResponseEntity.ok().body(response);
}
```

**URL:** `GET /api/v1/permissions/grouped-simple`

**Response format:**

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
    "DELETE_APPOINTMENT"
  ],
  "EMPLOYEE": [
    "VIEW_EMPLOYEE",
    "CREATE_EMPLOYEE",
    "EDIT_EMPLOYEE",
    "DELETE_EMPLOYEE"
  ],
  "WORK_SHIFTS": [
    "VIEW_WORK_SHIFTS",
    "CREATE_WORK_SHIFTS",
    "UPDATE_WORK_SHIFTS",
    "DELETE_WORK_SHIFTS"
  ],
  "TIME_OFF": [
    "VIEW_TIME_OFF_OWN",
    "VIEW_TIME_OFF_ALL",
    "CREATE_TIME_OFF",
    "APPROVE_TIME_OFF",
    "REJECT_TIME_OFF"
  ],
  ...
}
```

---

### 3. Tạo Service method mới

**File:** `PermissionService.java`

**Method mới:**

```java
@PreAuthorize("hasRole('" + ADMIN + "')")
@Transactional(readOnly = true)
public Map<String, List<String>> getPermissionsGroupedByModuleSimple() {
    List<Permission> permissions = permissionRepository.findAllActivePermissions();

    return permissions.stream()
        .sorted((p1, p2) -> {
            // Sort by module first
            int moduleCompare = p1.getModule().compareTo(p2.getModule());
            if (moduleCompare != 0) return moduleCompare;

            // Then by displayOrder
            if (p1.getDisplayOrder() != null && p2.getDisplayOrder() != null) {
                return p1.getDisplayOrder().compareTo(p2.getDisplayOrder());
            }
            if (p1.getDisplayOrder() != null) return -1;
            if (p2.getDisplayOrder() != null) return 1;

            // Finally by permission ID
            return p1.getPermissionId().compareTo(p2.getPermissionId());
        })
        .collect(Collectors.groupingBy(
            Permission::getModule,
            LinkedHashMap::new,
            Collectors.mapping(Permission::getPermissionId, Collectors.toList())
        ));
}
```

**Đặc điểm:**

- ✅ Return `Map<String, List<String>>` (nhẹ, dễ dùng)
- ✅ Sorted by module name
- ✅ Permissions trong module sorted by displayOrder
- ✅ Maintain insertion order (LinkedHashMap)

---

## 🔍 Cách Sử Dụng API Mới

### Frontend Example (TypeScript/JavaScript)

```typescript
// Gọi API
const response = await fetch("/api/v1/permissions/grouped-simple", {
  headers: {
    Authorization: `Bearer ${accessToken}`,
  },
});

const permissionsByModule = await response.json();

// Hiển thị theo module
console.log("=== PERMISSIONS BY MODULE ===\n");

Object.entries(permissionsByModule).forEach(([module, permissions]) => {
  console.log(`${module}:`);
  permissions.forEach((permission) => {
    console.log(`  - ${permission}`);
  });
  console.log("");
});
```

**Output:**

```
=== PERMISSIONS BY MODULE ===

PATIENT:
  - VIEW_PATIENT
  - CREATE_PATIENT
  - EDIT_PATIENT
  - DELETE_PATIENT

APPOINTMENT:
  - VIEW_APPOINTMENT
  - CREATE_APPOINTMENT
  - EDIT_APPOINTMENT
  - DELETE_APPOINTMENT
  - CANCEL_APPOINTMENT

EMPLOYEE:
  - VIEW_EMPLOYEE
  - CREATE_EMPLOYEE
  - EDIT_EMPLOYEE
  - DELETE_EMPLOYEE

...
```

### Hiển thị UI với React

```tsx
const PermissionList = () => {
  const [permissions, setPermissions] = useState<Record<string, string[]>>({});

  useEffect(() => {
    fetch("/api/v1/permissions/grouped-simple", {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => setPermissions(data));
  }, []);

  return (
    <div>
      {Object.entries(permissions).map(([module, perms]) => (
        <div key={module} className="module-section">
          <h3>{module}</h3>
          <ul>
            {perms.map((perm) => (
              <li key={perm}>{perm}</li>
            ))}
          </ul>
        </div>
      ))}
    </div>
  );
};
```

---

## 🐛 Debug Login Response Issue

### Các bước kiểm tra

#### Step 1: Chạy SQL script để check database

```bash
psql -h localhost -U postgres -d dental_clinic -f debug_login_database_check.sql
```

**Script sẽ kiểm tra:**

1. ✅ Role có `base_role_id` không
2. ✅ BaseRole có `default_home_path` không
3. ✅ Permission có `module` không
4. ✅ Permissions của ROLE_ADMIN có được group đúng không

#### Step 2: Xem logs khi login

```bash
# Login qua API
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Xem logs
grep "Login response prepared" logs/application.log
```

**Expected logs:**

```
INFO - Login response prepared for user: admin
INFO -   baseRole: admin
INFO -   homePath: /app/dashboard
INFO -   groupedPermissions size: 8
INFO -   sidebar size: 8
```

**Nếu thấy `baseRole: null` hoặc `homePath: null`:**
→ Database chưa setup đúng, cần chạy fix scripts trong `debug_login_database_check.sql`

#### Step 3: Fix database nếu cần

```sql
-- Fix role base_role_id
UPDATE roles SET base_role_id = 1 WHERE role_name = 'ROLE_ADMIN';
UPDATE roles SET base_role_id = 2 WHERE role_name = 'ROLE_EMPLOYEE';

-- Fix base_roles default_home_path
UPDATE base_roles SET default_home_path = '/app/dashboard' WHERE base_role_name = 'admin';
UPDATE base_roles SET default_home_path = '/app/shifts' WHERE base_role_name = 'employee';

-- Fix permissions module (ví dụ)
UPDATE permissions SET module = 'PATIENT' WHERE permission_id LIKE '%PATIENT%';
UPDATE permissions SET module = 'APPOINTMENT' WHERE permission_id LIKE '%APPOINTMENT%';
```

---

## 📋 So Sánh Các API Permissions

### 1. GET `/api/v1/permissions` (Flat list)

**Response:** `List<PermissionInfoResponse>`

```json
[
  {
    "permissionId": "VIEW_PATIENT",
    "permissionName": "View Patients",
    "module": "PATIENT",
    "description": "...",
    "path": "/app/patients"
  },
  ...
]
```

**Dùng khi:** Cần đầy đủ thông tin từng permission

### 2. GET `/api/v1/permissions/by-module` (Grouped with details)

**Response:** `Map<String, List<PermissionInfoResponse>>`

```json
{
  "PATIENT": [
    {
      "permissionId": "VIEW_PATIENT",
      "permissionName": "View Patients",
      "module": "PATIENT",
      ...
    }
  ]
}
```

**Dùng khi:** Cần group + full details

### 3. GET `/api/v1/permissions/grouped` (Hierarchy)

**Response:** `Map<String, List<PermissionHierarchyDTO>>`

```json
{
  "PATIENT": [
    {
      "permissionId": "VIEW_PATIENT",
      "permissionName": "View Patients",
      "parentPermissionId": null,
      "hasChildren": false,
      ...
    }
  ]
}
```

**Dùng khi:** Cần hiển thị parent-child relationship

### 4. GET `/api/v1/permissions/grouped-simple` ⭐ (MỚI)

**Response:** `Map<String, List<String>>`

```json
{
  "PATIENT": ["VIEW_PATIENT", "CREATE_PATIENT", "EDIT_PATIENT"],
  "APPOINTMENT": ["VIEW_APPOINTMENT", "CREATE_APPOINTMENT"]
}
```

**Dùng khi:**

- ✅ Chỉ cần permission IDs (không cần full details)
- ✅ Hiển thị simple list grouped by module
- ✅ Response nhẹ nhất, nhanh nhất
- ✅ **→ ĐỀ XUẤT DÙNG CHO FE**

---

## 📝 Files Đã Thay Đổi

### 1. AuthenticationService.java

- **Thêm:** Debug logging để kiểm tra baseRole/homePath/groupedPermissions
- **Line:** 145-150

### 2. PermissionController.java

- **Thêm:** Endpoint `/grouped-simple`
- **Return:** `Map<String, List<String>>`

### 3. PermissionService.java

- **Thêm:** Method `getPermissionsGroupedByModuleSimple()`
- **Logic:** Sort by module + displayOrder, group permissions

### 4. LOGIN_RESPONSE_DEBUG_GUIDE.md (MỚI)

- **Mục đích:** Hướng dẫn debug login response
- **Nội dung:**
  - Phân tích vấn đề
  - Các bước kiểm tra
  - Fix scripts
  - API documentation

### 5. debug_login_database_check.sql (MỚI)

- **Mục đích:** SQL script để check database
- **Nội dung:**
  - Check role base_role_id
  - Check base_roles default_home_path
  - Check permissions module
  - Fix scripts

---

## ✅ Checklist - Những Gì Cần Làm Tiếp

### Bước 1: Kiểm tra Database ⚠️ (QUAN TRỌNG)

```bash
# Chạy script check
psql -h localhost -U postgres -d dental_clinic -f debug_login_database_check.sql

# Xem output và fix nếu cần
```

### Bước 2: Restart Application

```bash
# Stop app hiện tại
pkill -f "DentalClinicManagementApplication"

# Compile lại
./mvnw clean compile -DskipTests

# Start app
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Bước 3: Test Login API

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' | jq .
```

**Kiểm tra response có:**

- ✅ `baseRole`: "admin"
- ✅ `homePath`: "/app/dashboard"
- ✅ `groupedPermissions`: { "PATIENT": [...], "APPOINTMENT": [...] }

### Bước 4: Test API Permissions Mới

```bash
# Login để lấy token
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' | jq -r '.token')

# Gọi API grouped-simple
curl -X GET http://localhost:8080/api/v1/permissions/grouped-simple \
  -H "Authorization: Bearer $TOKEN" | jq .
```

**Expected output:**

```json
{
  "PATIENT": ["VIEW_PATIENT", "CREATE_PATIENT", ...],
  "APPOINTMENT": ["VIEW_APPOINTMENT", ...],
  ...
}
```

### Bước 5: Check Logs

```bash
# Xem logs login
grep "Login response prepared" logs/application.log

# Xem baseRole và homePath
grep "baseRole:\|homePath:" logs/application.log
```

---

## 🎯 Kết Luận

### Đã hoàn thành:

1. ✅ Thêm logging vào login method
2. ✅ Tạo API mới `/api/v1/permissions/grouped-simple`
3. ✅ Tạo service method `getPermissionsGroupedByModuleSimple()`
4. ✅ Tạo debug guide và SQL check script

### Cần làm tiếp:

1. ⚠️ **Chạy SQL script check database** (có thể baseRole/homePath null vì database)
2. ⚠️ **Restart app và test login**
3. ⚠️ **Xem logs để xác định nguyên nhân**

### Nguyên nhân có thể của bug:

- **Database:** Role không có base_role_id hoặc BaseRole không có default_home_path
- **Permissions:** Permissions không có module field
- **Code cũ:** App đang chạy code cũ (chưa restart)

---

**📞 Liên hệ:** Nếu sau khi check vẫn không resolve được, gửi cho tôi:

1. Output của SQL script check
2. Logs sau khi login (grep "Login response prepared")
3. Full login response JSON
