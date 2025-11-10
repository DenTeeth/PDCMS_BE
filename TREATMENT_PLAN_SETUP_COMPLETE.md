# Treatment Plan Module - Setup Complete ✅

## Summary

Module Treatment Plan đã được cấu hình hoàn chỉnh với seed data và permissions.

## Files Updated

### 1. Seed Data SQL

**File:** `src/main/resources/db/dental-clinic-seed-data.sql`

**Changes:**

- ✅ Thêm MODULE 13: TREATMENT_PLAN (5 permissions)
- ✅ Cập nhật role_permissions cho ROLE_DENTIST (full access)
- ✅ Cập nhật role_permissions cho ROLE_RECEPTIONIST (view-only)
- ✅ Cập nhật role_permissions cho ROLE_PATIENT (own plans only)

**New Permissions:**

```sql
VIEW_TREATMENT_PLAN_ALL   -- Staff view all (Doctor, Receptionist)
VIEW_TREATMENT_PLAN_OWN   -- Patient view own only
CREATE_TREATMENT_PLAN     -- Doctor creates plans
UPDATE_TREATMENT_PLAN     -- Doctor updates plans
DELETE_TREATMENT_PLAN     -- Doctor soft deletes plans
```

### 2. AuthoritiesConstants

**File:** `src/main/java/com/dental/clinic/management/utils/security/AuthoritiesConstants.java`

**Changes:**

- ✅ Thêm 5 constants mới cho Treatment Plan permissions
- ✅ Theo naming convention hiện tại (VIEW_xxx_ALL, VIEW_xxx_OWN)

### 3. API Documentation

**File:** `docs/api-guides/treatment-plan/Treatment_Plan_API_Guide.md`

**Changes:**

- ✅ Thêm phần "Test Users (from Seed Data)"
- ✅ Chi tiết username/password của các test users
- ✅ Bảng mapping User → Patient Code → Account ID
- ✅ 7 test cases với cURL commands đầy đủ
- ✅ Expected responses cho mỗi test case

## Role Permissions Matrix

| Role                  | Permissions                      |
| --------------------- | -------------------------------- |
| **ROLE_ADMIN**        | ALL (inherited)                  |
| **ROLE_DENTIST**      | VIEW_ALL, CREATE, UPDATE, DELETE |
| **ROLE_RECEPTIONIST** | VIEW_ALL (read-only)             |
| **ROLE_PATIENT**      | VIEW_OWN only                    |

## Test Users Ready

### Staff Users (VIEW_TREATMENT_PLAN_ALL)

| Username | Password | Role              | Full Access               |
| -------- | -------- | ----------------- | ------------------------- |
| `bacsi1` | `123456` | ROLE_DENTIST      | ✅ Create, Update, Delete |
| `bacsi2` | `123456` | ROLE_DENTIST      | ✅ Create, Update, Delete |
| `letan1` | `123456` | ROLE_RECEPTIONIST | ✅ View All (read-only)   |

### Patient Users (VIEW_TREATMENT_PLAN_OWN)

| Username    | Password | Patient Code | Can View       |
| ----------- | -------- | ------------ | -------------- |
| `benhnhan1` | `123456` | BN-1001      | Own plans only |
| `benhnhan2` | `123456` | BN-1002      | Own plans only |
| `benhnhan3` | `123456` | BN-1003      | Own plans only |
| `benhnhan4` | `123456` | BN-1004      | Own plans only |

## Next Steps to Test

### 1. Khởi động lại Spring Boot

```bash
./mvnw spring-boot:run
```

**Lưu ý:** Application sẽ tự động tạo schema và load seed data từ `dental-clinic-seed-data.sql`

### 2. Test Login API

```bash
# Login as Doctor
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bacsi1",
    "password": "123456"
  }'

# Lưu access_token từ response để dùng cho các test tiếp theo
```

### 3. Test Treatment Plan API

```bash
# Replace {TOKEN} với access_token từ login response

# Test 1: Doctor xem plans của patient BN-1001 (SUCCESS)
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer {TOKEN}"

# Test 2: Patient xem plans của chính mình (SUCCESS)
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer {PATIENT_TOKEN}"

# Test 3: Patient xem plans của người khác (FORBIDDEN)
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans" \
  -H "Authorization: Bearer {PATIENT_TOKEN_BN1001}"
```

### 4. Verify Permissions in Database (sau khi app chạy)

```bash
# Check permissions table
docker exec -it postgres-dental psql -U root -d dental_clinic_db \
  -c "SELECT permission_id, module, description FROM permissions WHERE module = 'TREATMENT_PLAN';"

# Check role_permissions for ROLE_DENTIST
docker exec -it postgres-dental psql -U root -d dental_clinic_db \
  -c "SELECT rp.role_id, rp.permission_id FROM role_permissions rp WHERE rp.role_id = 'ROLE_DENTIST' AND rp.permission_id LIKE 'VIEW_TREATMENT_PLAN%';"
```

## RBAC Logic Flow

### Scenario 1: Doctor (VIEW_TREATMENT_PLAN_ALL)

```
Request: GET /api/v1/patients/BN-1001/treatment-plans
JWT Token: { account_id: 1, permissions: ["VIEW_TREATMENT_PLAN_ALL", ...] }

1. ✅ Find patient BN-1001 in DB
2. ✅ Check permission: hasAuthority("VIEW_TREATMENT_PLAN_ALL") → TRUE
3. ✅ Return all plans for patient BN-1001
```

### Scenario 2: Patient Views Own Plans (VIEW_TREATMENT_PLAN_OWN)

```
Request: GET /api/v1/patients/BN-1001/treatment-plans
JWT Token: { account_id: 12, permissions: ["VIEW_TREATMENT_PLAN_OWN"] }

1. ✅ Find patient BN-1001 in DB → patient.account_id = 12
2. ✅ Check permission: hasAuthority("VIEW_TREATMENT_PLAN_OWN") → TRUE
3. ✅ Verify: patient.account_id (12) == JWT.account_id (12) → MATCH
4. ✅ Return plans for patient BN-1001
```

### Scenario 3: Patient Views Other's Plans (FORBIDDEN)

```
Request: GET /api/v1/patients/BN-1002/treatment-plans
JWT Token: { account_id: 12, permissions: ["VIEW_TREATMENT_PLAN_OWN"] }

1. ✅ Find patient BN-1002 in DB → patient.account_id = 13
2. ✅ Check permission: hasAuthority("VIEW_TREATMENT_PLAN_OWN") → TRUE
3. ❌ Verify: patient.account_id (13) == JWT.account_id (12) → MISMATCH
4. ❌ Return 403 FORBIDDEN: "You can only view your own treatment plans"
```

## Build Status

```
[INFO] BUILD SUCCESS
[INFO] Total time:  34.255 s
[INFO] Finished at: 2025-11-10T02:40:53-08:00
```

✅ **436 source files compiled successfully**
✅ **No compilation errors**
✅ **All dependencies resolved**

## Documentation References

- **API Guide:** `docs/api-guides/treatment-plan/Treatment_Plan_API_Guide.md`
- **Seed Data:** `src/main/resources/db/dental-clinic-seed-data.sql`
- **Constants:** `src/main/java/com/dental/clinic/management/utils/security/AuthoritiesConstants.java`

## Notes

- Script `reset-and-seed.sh` đã chạy thành công (54 services + 6 categories)
- Tất cả permissions sẽ được load khi Spring Boot khởi động
- JWT token chứa `account_id` và list `permissions` cho RBAC
- N+1 query đã được prevent bằng `JOIN FETCH` trong repository

---

**Status:** ✅ READY FOR TESTING
**Author:** GitHub Copilot
**Date:** 2025-11-10
