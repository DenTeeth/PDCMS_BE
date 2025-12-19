# Permission Management API

## Overview

Permission module quan ly cac quyen han trong he thong (VIEW_PATIENT, CREATE_APPOINTMENT, etc.) va ho tro phan cap quyen (parent-child hierarchy).

**Key Features:**

- Cached with Redis (30 minutes TTL) - improved performance
- N+1 query optimization with @EntityGraph
- Parent-child permission hierarchy (ALL vs OWN)
- Module-based grouping for FE sidebar
- Soft delete support

## Database Schema

```sql
CREATE TABLE permissions (
    permission_id VARCHAR(50) PRIMARY KEY,      -- e.g., VIEW_PATIENT, CREATE_APPOINTMENT
    permission_name VARCHAR(100) NOT NULL,
    module VARCHAR(20) NOT NULL,                -- e.g., PATIENT, APPOINTMENT, EMPLOYEE
    description TEXT,
    display_order INTEGER,                      -- For sorting in UI
    parent_permission_id VARCHAR(50) REFERENCES permissions(permission_id),  -- Hierarchy support
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_permissions_module ON permissions(module);
CREATE INDEX idx_permissions_parent ON permissions(parent_permission_id);
```

## Business Rules

1. **Unique Permission ID**: Permission ID = Permission Name (e.g., VIEW_PATIENT)
2. **Module Grouping**: Permissions are grouped by module for FE sidebar
3. **Parent-Child Hierarchy**:
   - Parent permission (e.g., VIEW_APPOINTMENT_ALL) represents "ALL" access
   - Child permission (e.g., VIEW_APPOINTMENT_OWN) represents "OWN" access
   - If user has parent permission, child is automatically granted
4. **Display Order**: Used for sorting permissions within same module
5. **Soft Delete**: Use `isActive=false` to preserve historical data
6. **Cache Strategy**: All read operations cached for 30 minutes

## Cache Keys

- `permissions::allActive` - All active permissions
- `permissionById::{permissionId}` - Individual permission details
- `permissionsByModule::{module}` - Permissions by module
- `permissionsGrouped::byModule` - Permissions grouped by module
- `permissionsGrouped::hierarchy` - Permissions with hierarchy info

---

## API PERMISSION.1: Get All Active Permissions

**Endpoint:** `GET /api/v1/permissions`

**Permission:** `ROLE_ADMIN`

**Description:** Retrieve all active permissions in the system.

**Cache:** Yes (30 minutes)

### Use Cases

- Admin views all available permissions
- Permission selection for role assignment

### Request

```http
GET /api/v1/permissions
Authorization: Bearer <admin_token>
```

### Response 200 OK

```json
[
  {
    "permissionId": "VIEW_ACCOUNT",
    "permissionName": "VIEW_ACCOUNT",
    "module": "ACCOUNT",
    "description": "Xem danh sach tai khoan",
    "displayOrder": 10,
    "parentPermissionId": null,
    "isActive": true,
    "createdAt": "2025-01-01T10:00:00"
  },
  {
    "permissionId": "CREATE_ACCOUNT",
    "permissionName": "CREATE_ACCOUNT",
    "module": "ACCOUNT",
    "description": "Tao tai khoan moi",
    "displayOrder": 11,
    "parentPermissionId": null,
    "isActive": true,
    "createdAt": "2025-01-01T10:00:00"
  },
  {
    "permissionId": "VIEW_APPOINTMENT_ALL",
    "permissionName": "VIEW_APPOINTMENT_ALL",
    "module": "APPOINTMENT",
    "description": "Xem TAT CA lich hen (Le tan/Quan ly)",
    "displayOrder": 51,
    "parentPermissionId": null,
    "isActive": true,
    "createdAt": "2025-01-01T10:00:00"
  },
  {
    "permissionId": "VIEW_APPOINTMENT_OWN",
    "permissionName": "VIEW_APPOINTMENT_OWN",
    "module": "APPOINTMENT",
    "description": "Chi xem lich hen LIEN QUAN (Bac si/Y ta/Observer/Benh nhan)",
    "displayOrder": 52,
    "parentPermissionId": "VIEW_APPOINTMENT_ALL",
    "isActive": true,
    "createdAt": "2025-01-01T10:00:00"
  }
]
```

### Test Data (from seed data)

```bash
# Login as admin
username: admin
password: 123456
```

---

## API PERMISSION.2: Get Permissions Grouped by Module

**Endpoint:** `GET /api/v1/permissions/by-module`

**Permission:** `ROLE_ADMIN`

**Description:** Get permissions grouped by module (simple format).

**Cache:** Yes (30 minutes)

### Use Cases

- Admin panel - display permissions by category
- Role permission assignment UI

### Request

```http
GET /api/v1/permissions/by-module
Authorization: Bearer <admin_token>
```

### Response 200 OK

```json
{
  "ACCOUNT": [
    {
      "permissionId": "VIEW_ACCOUNT",
      "permissionName": "VIEW_ACCOUNT",
      "module": "ACCOUNT",
      "description": "Xem danh sach tai khoan",
      "displayOrder": 10,
      "parentPermissionId": null,
      "isActive": true
    },
    {
      "permissionId": "CREATE_ACCOUNT",
      "permissionName": "CREATE_ACCOUNT",
      "module": "ACCOUNT",
      "description": "Tao tai khoan moi",
      "displayOrder": 11,
      "parentPermissionId": null,
      "isActive": true
    }
  ],
  "PATIENT": [
    {
      "permissionId": "VIEW_PATIENT",
      "permissionName": "VIEW_PATIENT",
      "module": "PATIENT",
      "description": "Xem danh sach benh nhan",
      "displayOrder": 30,
      "parentPermissionId": null,
      "isActive": true
    },
    {
      "permissionId": "CREATE_PATIENT",
      "permissionName": "CREATE_PATIENT",
      "module": "PATIENT",
      "description": "Tao ho so benh nhan moi",
      "displayOrder": 31,
      "parentPermissionId": null,
      "isActive": true
    }
  ],
  "APPOINTMENT": [
    {
      "permissionId": "VIEW_APPOINTMENT_ALL",
      "permissionName": "VIEW_APPOINTMENT_ALL",
      "module": "APPOINTMENT",
      "description": "Xem TAT CA lich hen",
      "displayOrder": 51,
      "parentPermissionId": null,
      "isActive": true
    },
    {
      "permissionId": "VIEW_APPOINTMENT_OWN",
      "permissionName": "VIEW_APPOINTMENT_OWN",
      "module": "APPOINTMENT",
      "description": "Chi xem lich hen LIEN QUAN",
      "displayOrder": 52,
      "parentPermissionId": "VIEW_APPOINTMENT_ALL",
      "isActive": true
    }
  ]
}
```

---

## API PERMISSION.3: Get Grouped Permissions with Hierarchy

**Endpoint:** `GET /api/v1/permissions/grouped`

**Permission:** `ROLE_ADMIN`

**Description:** Get permissions with parent-child hierarchy information. Used for FE permission management UI with three-level selection (NONE/OWN/ALL).

**Cache:** Yes (30 minutes)

### Use Cases

- Role permission assignment with hierarchy support
- FE displays permission checkboxes with 3 states:
  - NONE: No permission
  - OWN: Child permission only
  - ALL: Parent permission (includes child)

### Request

```http
GET /api/v1/permissions/grouped
Authorization: Bearer <admin_token>
```

### Response 200 OK

```json
{
  "ACCOUNT": [
    {
      "permissionId": "VIEW_ACCOUNT",
      "permissionName": "VIEW_ACCOUNT",
      "displayOrder": 10,
      "parentPermissionId": null,
      "selectionLevel": "NONE"
    },
    {
      "permissionId": "CREATE_ACCOUNT",
      "permissionName": "CREATE_ACCOUNT",
      "displayOrder": 11,
      "parentPermissionId": null,
      "selectionLevel": "NONE"
    }
  ],
  "APPOINTMENT": [
    {
      "permissionId": "VIEW_APPOINTMENT_ALL",
      "permissionName": "VIEW_APPOINTMENT_ALL",
      "displayOrder": 51,
      "parentPermissionId": null,
      "selectionLevel": "ALL"
    },
    {
      "permissionId": "VIEW_APPOINTMENT_OWN",
      "permissionName": "VIEW_APPOINTMENT_OWN",
      "displayOrder": 52,
      "parentPermissionId": "VIEW_APPOINTMENT_ALL",
      "selectionLevel": "OWN"
    }
  ],
  "SCHEDULE_MANAGEMENT": [
    {
      "permissionId": "VIEW_REGISTRATION_ALL",
      "permissionName": "VIEW_REGISTRATION_ALL",
      "displayOrder": 90,
      "parentPermissionId": null,
      "selectionLevel": "ALL"
    },
    {
      "permissionId": "VIEW_REGISTRATION_OWN",
      "permissionName": "VIEW_REGISTRATION_OWN",
      "displayOrder": 91,
      "parentPermissionId": "VIEW_REGISTRATION_ALL",
      "selectionLevel": "OWN"
    }
  ]
}
```

### Selection Level Explanation

- **NONE**: Standalone permission (no children)
- **ALL**: Parent permission (has children) - grants both parent and child permissions
- **OWN**: Child permission - limited access compared to parent

---

## API PERMISSION.4: Get Permissions by Module

**Endpoint:** `GET /api/v1/permissions/module/{module}`

**Permission:** `ROLE_ADMIN`

**Description:** Get all active permissions for a specific module.

**Cache:** Yes (30 minutes)

### Request

```http
GET /api/v1/permissions/module/PATIENT
Authorization: Bearer <admin_token>
```

### Response 200 OK

```json
[
  {
    "permissionId": "VIEW_PATIENT",
    "permissionName": "VIEW_PATIENT",
    "module": "PATIENT",
    "description": "Xem danh sach benh nhan",
    "displayOrder": 30,
    "parentPermissionId": null,
    "isActive": true
  },
  {
    "permissionId": "CREATE_PATIENT",
    "permissionName": "CREATE_PATIENT",
    "module": "PATIENT",
    "description": "Tao ho so benh nhan moi",
    "displayOrder": 31,
    "parentPermissionId": null,
    "isActive": true
  },
  {
    "permissionId": "UPDATE_PATIENT",
    "permissionName": "UPDATE_PATIENT",
    "module": "PATIENT",
    "description": "Cap nhat ho so benh nhan",
    "displayOrder": 32,
    "parentPermissionId": null,
    "isActive": true
  },
  {
    "permissionId": "DELETE_PATIENT",
    "permissionName": "DELETE_PATIENT",
    "module": "PATIENT",
    "description": "Xoa ho so benh nhan",
    "displayOrder": 33,
    "parentPermissionId": null,
    "isActive": true
  }
]
```

---

## API PERMISSION.5: Get Permission by ID

**Endpoint:** `GET /api/v1/permissions/{permissionId}`

**Permission:** `ROLE_ADMIN`

**Description:** Get detailed information of a specific permission.

**Cache:** Yes (30 minutes)

### Request

```http
GET /api/v1/permissions/VIEW_PATIENT
Authorization: Bearer <admin_token>
```

### Response 200 OK

```json
{
  "permissionId": "VIEW_PATIENT",
  "permissionName": "VIEW_PATIENT",
  "module": "PATIENT",
  "description": "Xem danh sach benh nhan",
  "displayOrder": 30,
  "parentPermissionId": null,
  "isActive": true,
  "createdAt": "2025-01-01T10:00:00"
}
```

### Response 404 Not Found

```json
{
  "timestamp": "2025-12-19T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Permission not found with ID: INVALID_PERMISSION",
  "path": "/api/v1/permissions/INVALID_PERMISSION"
}
```

---

## API PERMISSION.6: Create Permission

**Endpoint:** `POST /api/v1/permissions`

**Permission:** `ROLE_ADMIN`

**Description:** Create a new permission in the system.

**Cache Eviction:** Evicts all permission caches

### Request

```http
POST /api/v1/permissions
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "permissionName": "EXPORT_REPORT",
  "module": "REPORT",
  "description": "Xuat bao cao he thong"
}
```

### Response 201 Created

```http
Location: /api/v1/permissions/EXPORT_REPORT
```

```json
{
  "permissionId": "EXPORT_REPORT",
  "permissionName": "EXPORT_REPORT",
  "module": "REPORT",
  "description": "Xuat bao cao he thong",
  "displayOrder": null,
  "parentPermissionId": null,
  "isActive": true,
  "createdAt": "2025-12-19T10:30:00"
}
```

### Response 400 Bad Request

```json
{
  "timestamp": "2025-12-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Permission with name 'EXPORT_REPORT' already exists",
  "path": "/api/v1/permissions"
}
```

---

## API PERMISSION.7: Update Permission

**Endpoint:** `PATCH /api/v1/permissions/{permissionId}`

**Permission:** `ROLE_ADMIN`

**Description:** Update permission information.

**Cache Eviction:** Evicts all permission caches

### Request

```http
PATCH /api/v1/permissions/VIEW_PATIENT
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "description": "Xem danh sach va chi tiet benh nhan - Updated",
  "displayOrder": 30,
  "isActive": true
}
```

### Response 200 OK

```json
{
  "permissionId": "VIEW_PATIENT",
  "permissionName": "VIEW_PATIENT",
  "module": "PATIENT",
  "description": "Xem danh sach va chi tiet benh nhan - Updated",
  "displayOrder": 30,
  "parentPermissionId": null,
  "isActive": true,
  "createdAt": "2025-01-01T10:00:00"
}
```

---

## API PERMISSION.8: Delete Permission (Soft Delete)

**Endpoint:** `DELETE /api/v1/permissions/{permissionId}`

**Permission:** `ROLE_ADMIN`

**Description:** Soft delete permission by setting isActive to false.

**Cache Eviction:** Evicts all permission caches

### Request

```http
DELETE /api/v1/permissions/EXPORT_REPORT
Authorization: Bearer <admin_token>
```

### Response 200 OK

```http
HTTP/1.1 200 OK
```

---

## Modules Available (from seed data)

1. **ACCOUNT** - Quan ly tai khoan
2. **EMPLOYEE** - Quan ly nhan vien
3. **PATIENT** - Quan ly benh nhan
4. **TREATMENT** - Quan ly dieu tri
5. **APPOINTMENT** - Quan ly lich hen
6. **CUSTOMER_MANAGEMENT** - Quan ly lien he khach hang
7. **SCHEDULE_MANAGEMENT** - Quan ly ca lam viec
8. **LEAVE_MANAGEMENT** - Quan ly nghi phep va tang ca
9. **SYSTEM_CONFIGURATION** - Cau hinh he thong
10. **HOLIDAY** - Quan ly ngay nghi le
11. **CACHE_MANAGEMENT** - Quan ly bo nho dem Redis
12. **ROOM_MANAGEMENT** - Quan ly phong kham
13. **SERVICE_MANAGEMENT** - Quan ly dich vu
14. **WAREHOUSE** - Quan ly kho
15. **NOTIFICATION** - Quan ly thong bao

---

## Performance Optimization

### N+1 Query Fix

Before (N+1 problem):

```java
// 1 query to fetch permissions
List<Permission> permissions = permissionRepository.findAll();
// N queries to fetch parent permission for each permission
permissions.forEach(p -> p.getParentPermission().getPermissionId());
```

After (with @EntityGraph):

```java
// 1 query with JOIN FETCH
@EntityGraph(attributePaths = {"parentPermission"})
List<Permission> permissions = permissionRepository.findAllActivePermissions();
```

### Redis Caching

All read operations are cached for 30 minutes:

- `getAllActivePermissions()` - Cached as `permissions::allActive`
- `getPermissionById()` - Cached as `permissionById::{permissionId}`
- `getPermissionsByModule()` - Cached as `permissionsByModule::{module}`
- `getPermissionsGroupedByModule()` - Cached as `permissionsGrouped::byModule`
- `getGroupedPermissions()` - Cached as `permissionsGrouped::hierarchy`

Cache is automatically evicted on:

- `createPermission()`
- `updatePermission()`
- `deletePermission()`

---

## Error Handling

### 400 Bad Request

```json
{
  "timestamp": "2025-12-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Permission with name 'VIEW_PATIENT' already exists",
  "path": "/api/v1/permissions"
}
```

### 404 Not Found

```json
{
  "timestamp": "2025-12-19T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Permission not found with ID: INVALID_PERMISSION",
  "path": "/api/v1/permissions/INVALID_PERMISSION"
}
```

### 403 Forbidden

```json
{
  "timestamp": "2025-12-19T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/permissions"
}
```
