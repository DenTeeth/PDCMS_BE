# Role Management API

## Overview

Role module quan ly cac vai tro trong he thong (Admin, Dentist, Nurse, Receptionist, etc.) va gan quyen han (permissions) cho tung role.

**Key Features:**

- Cached with Redis (30 minutes TTL) - improved performance
- N+1 query optimization with @EntityGraph
- Soft delete support
- Dynamic permission assignment
- Base role mapping for FE layout routing

## Database Schema

```sql
CREATE TABLE base_roles (
    base_role_id INTEGER PRIMARY KEY,
    base_role_name VARCHAR(20) NOT NULL,  -- admin/employee/patient
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE roles (
    role_id VARCHAR(50) PRIMARY KEY,      -- e.g., ROLE_ADMIN, ROLE_DENTIST
    role_name VARCHAR(50) NOT NULL,
    base_role_id INTEGER REFERENCES base_roles(base_role_id),
    description TEXT,
    requires_specialization BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE role_permissions (
    role_id VARCHAR(50) REFERENCES roles(role_id),
    permission_id VARCHAR(50) REFERENCES permissions(permission_id),
    PRIMARY KEY (role_id, permission_id)
);
```

## Business Rules

1. **Unique Role ID**: Role ID must be unique (e.g., ROLE_DENTIST)
2. **Base Role Mapping**: Each role must link to a base role (admin/employee/patient) for FE routing
3. **Soft Delete**: Use `isActive=false` to preserve historical data
4. **Specialization Flag**: `requiresSpecialization` indicates if role needs specific skill (DENTIST, NURSE)
5. **Cache Strategy**: All read operations are cached for 30 minutes, write operations evict cache

## Cache Keys

- `roles::allRoles` - All active roles
- `roles::employeeAssignable` - Roles for employee assignment (excludes ROLE_PATIENT)
- `roleById::{roleId}` - Individual role details
- `rolePermissions::{roleId}` - Permissions of a role

---

## API ROLE.1: Get All Roles

**Endpoint:** `GET /api/v1/roles`

**Permission:** `ROLE_ADMIN`

**Description:** Retrieve all active roles including ROLE_PATIENT.

**Cache:** Yes (30 minutes)

### Use Cases

- Admin views all available roles in system
- Role selection dropdown in admin panel

### Request

```http
GET /api/v1/roles
Authorization: Bearer <admin_token>
```

### Response 200 OK

```json
[
  {
    "roleId": "ROLE_ADMIN",
    "roleName": "ROLE_ADMIN",
    "description": "Quan tri vien he thong - Toan quyen quan ly",
    "baseRoleId": 1,
    "requiresSpecialization": false,
    "isActive": true,
    "createdAt": "2025-01-01T10:00:00"
  },
  {
    "roleId": "ROLE_DENTIST",
    "roleName": "ROLE_DENTIST",
    "description": "Bac si nha khoa - Kham va dieu tri benh nhan",
    "baseRoleId": 2,
    "requiresSpecialization": true,
    "isActive": true,
    "createdAt": "2025-01-01T10:00:00"
  },
  {
    "roleId": "ROLE_PATIENT",
    "roleName": "ROLE_PATIENT",
    "description": "Benh nhan - Xem ho so va dat lich kham",
    "baseRoleId": 3,
    "requiresSpecialization": false,
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

## API ROLE.2: Get Employee Assignable Roles

**Endpoint:** `GET /api/v1/roles/employee-assignable`

**Permission:** `ROLE_ADMIN`

**Description:** Retrieve roles that can be assigned to employees (excludes ROLE_PATIENT).

**Cache:** Yes (30 minutes)

### Use Cases

- Employee creation form - role selection
- Employee update - change role

### Request

```http
GET /api/v1/roles/employee-assignable
Authorization: Bearer <admin_token>
```

### Response 200 OK

```json
[
  {
    "roleId": "ROLE_ADMIN",
    "roleName": "ROLE_ADMIN",
    "description": "Quan tri vien he thong - Toan quyen quan ly",
    "baseRoleId": 1,
    "requiresSpecialization": false,
    "isActive": true
  },
  {
    "roleId": "ROLE_DENTIST",
    "roleName": "ROLE_DENTIST",
    "description": "Bac si nha khoa - Kham va dieu tri benh nhan",
    "baseRoleId": 2,
    "requiresSpecialization": true,
    "isActive": true
  },
  {
    "roleId": "ROLE_RECEPTIONIST",
    "roleName": "ROLE_RECEPTIONIST",
    "description": "Le tan - Tiep don va quan ly lich hen",
    "baseRoleId": 2,
    "requiresSpecialization": false,
    "isActive": true
  }
]
```

### Notes

- ROLE_PATIENT is excluded from response
- Used for employee management only

---

## API ROLE.3: Get Role By ID

**Endpoint:** `GET /api/v1/roles/{roleId}`

**Permission:** `ROLE_ADMIN`

**Description:** Get detailed information of a specific role.

**Cache:** Yes (30 minutes)

### Request

```http
GET /api/v1/roles/ROLE_DENTIST
Authorization: Bearer <admin_token>
```

### Response 200 OK

```json
{
  "roleId": "ROLE_DENTIST",
  "roleName": "ROLE_DENTIST",
  "description": "Bac si nha khoa - Kham va dieu tri benh nhan",
  "baseRoleId": 2,
  "requiresSpecialization": true,
  "isActive": true,
  "createdAt": "2025-01-01T10:00:00"
}
```

### Response 400 Bad Request

```json
{
  "timestamp": "2025-12-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Role not found with ID: ROLE_INVALID",
  "path": "/api/v1/roles/ROLE_INVALID"
}
```

---

## API ROLE.4: Create Role

**Endpoint:** `POST /api/v1/roles`

**Permission:** `ROLE_ADMIN`

**Description:** Create a new role in the system.

**Cache Eviction:** Evicts `roles` cache

### Request

```http
POST /api/v1/roles
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "roleId": "ROLE_LAB_TECHNICIAN",
  "roleName": "ROLE_LAB_TECHNICIAN",
  "description": "Ky thuat vien phong thi nghiem",
  "baseRoleId": 2,
  "requiresSpecialization": true
}
```

### Response 201 Created

```http
Location: /api/v1/roles/ROLE_LAB_TECHNICIAN
```

```json
{
  "roleId": "ROLE_LAB_TECHNICIAN",
  "roleName": "ROLE_LAB_TECHNICIAN",
  "description": "Ky thuat vien phong thi nghiem",
  "baseRoleId": 2,
  "requiresSpecialization": true,
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
  "message": "Role already exists",
  "path": "/api/v1/roles"
}
```

---

## API ROLE.5: Update Role

**Endpoint:** `PUT /api/v1/roles/{roleId}`

**Permission:** `ROLE_ADMIN`

**Description:** Update role information.

**Cache Eviction:** Evicts `roles` and `roleById::{roleId}` cache

### Request

```http
PUT /api/v1/roles/ROLE_DENTIST
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "roleName": "ROLE_DENTIST",
  "description": "Bac si nha khoa - Updated description",
  "requiresSpecialization": true
}
```

### Response 200 OK

```json
{
  "roleId": "ROLE_DENTIST",
  "roleName": "ROLE_DENTIST",
  "description": "Bac si nha khoa - Updated description",
  "baseRoleId": 2,
  "requiresSpecialization": true,
  "isActive": true,
  "createdAt": "2025-01-01T10:00:00"
}
```

---

## API ROLE.6: Delete Role (Soft Delete)

**Endpoint:** `DELETE /api/v1/roles/{roleId}`

**Permission:** `ROLE_ADMIN`

**Description:** Soft delete role by setting isActive to false.

**Cache Eviction:** Evicts `roles` and `roleById::{roleId}` cache

### Request

```http
DELETE /api/v1/roles/ROLE_LAB_TECHNICIAN
Authorization: Bearer <admin_token>
```

### Response 200 OK

```json
{
  "roleId": "ROLE_LAB_TECHNICIAN",
  "roleName": "ROLE_LAB_TECHNICIAN",
  "description": "Ky thuat vien phong thi nghiem",
  "baseRoleId": 2,
  "requiresSpecialization": true,
  "isActive": false,
  "createdAt": "2025-12-19T10:30:00"
}
```

---

## API ROLE.7: Assign Permissions to Role

**Endpoint:** `POST /api/v1/roles/{roleId}/permissions`

**Permission:** `ROLE_ADMIN`

**Description:** Assign multiple permissions to a role (replaces existing permissions).

**Cache Eviction:** Evicts `roles`, `roleById::{roleId}`, and `rolePermissions::{roleId}` cache

### Request

```http
POST /api/v1/roles/ROLE_RECEPTIONIST/permissions
Authorization: Bearer <admin_token>
Content-Type: application/json

[
  "VIEW_PATIENT",
  "CREATE_PATIENT",
  "UPDATE_PATIENT",
  "VIEW_APPOINTMENT_ALL",
  "CREATE_APPOINTMENT",
  "UPDATE_APPOINTMENT"
]
```

### Response 200 OK

```http
HTTP/1.1 200 OK
```

### Test Case

```bash
# 1. Login as admin
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "123456"
}

# 2. Assign permissions
POST /api/v1/roles/ROLE_RECEPTIONIST/permissions
[
  "VIEW_PATIENT",
  "CREATE_APPOINTMENT"
]

# 3. Verify permissions
GET /api/v1/roles/ROLE_RECEPTIONIST/permissions
```

---

## API ROLE.8: Get Role Permissions

**Endpoint:** `GET /api/v1/roles/{roleId}/permissions`

**Permission:** `ROLE_ADMIN`

**Description:** Get all permissions assigned to a specific role.

**Cache:** Yes (30 minutes)

### Request

```http
GET /api/v1/roles/ROLE_DENTIST/permissions
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
    "permissionId": "UPDATE_PATIENT",
    "permissionName": "UPDATE_PATIENT",
    "module": "PATIENT",
    "description": "Cap nhat ho so benh nhan",
    "displayOrder": 32,
    "parentPermissionId": null,
    "isActive": true
  },
  {
    "permissionId": "VIEW_APPOINTMENT_OWN",
    "permissionName": "VIEW_APPOINTMENT_OWN",
    "module": "APPOINTMENT",
    "description": "Chi xem lich hen LIEN QUAN (Bac si/Y ta/Observer/Benh nhan)",
    "displayOrder": 52,
    "parentPermissionId": "VIEW_APPOINTMENT_ALL",
    "isActive": true
  }
]
```

---

## Performance Optimization

### N+1 Query Fix

Before (N+1 problem):

```java
// 1 query to fetch roles
List<Role> roles = roleRepository.findAll();
// N queries to fetch permissions for each role (EAGER fetch)
roles.forEach(role -> role.getPermissions().size());
```

After (with @EntityGraph):

```java
// 1 query with JOIN FETCH
@EntityGraph(attributePaths = {"permissions", "baseRole"})
List<Role> roles = roleRepository.findAllActiveRoles();
```

### Redis Caching

All read operations are cached for 30 minutes:

- `getAllRoles()` - Cached as `roles::allRoles`
- `getRoleById()` - Cached as `roleById::{roleId}`
- `getRolePermissions()` - Cached as `rolePermissions::{roleId}`

Cache is automatically evicted on:

- `createRole()`
- `updateRole()`
- `deleteRole()`
- `assignPermissionsToRole()`

---

## Error Handling

### 400 Bad Request

```json
{
  "timestamp": "2025-12-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Role already exists",
  "path": "/api/v1/roles"
}
```

### 404 Not Found

```json
{
  "timestamp": "2025-12-19T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Role not found with ID: ROLE_INVALID",
  "path": "/api/v1/roles/ROLE_INVALID"
}
```

### 403 Forbidden

```json
{
  "timestamp": "2025-12-19T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/roles"
}
```
