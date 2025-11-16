# RBAC Permission Fix: VIEW_TREATMENT_PLAN_ALL → VIEW_TREATMENT_PLAN_OWN

**Date**: 2025-11-15
**Priority**: 🔴 **P0 - Critical Security Fix**
**Status**: ✅ **COMPLETED**

---

## 📋 Changes Summary

### 🔒 Security Issue

- **Problem**: Doctor (ROLE_DENTIST) had `VIEW_TREATMENT_PLAN_ALL` permission
- **Impact**: Doctor could see ALL treatment plans (security breach)
- **Root Cause**: Wrong permission assigned in seed data

### ✅ Fix Applied

#### 1. Service Logic Updated ✅

**File**: `TreatmentPlanService.java`

**Changes**:

```java
// EMPLOYEE (Doctor) validation
if (!hasViewOwnPermission) {
    throw new AccessDeniedException("Employee must have VIEW_TREATMENT_PLAN_OWN");
}

// Security warning if doctor has VIEW_ALL
if (hasViewAllPermission) {
    log.warn("🔒 SECURITY WARNING: Employee has VIEW_TREATMENT_PLAN_ALL. " +
            "This should only be for ADMIN/MANAGER. Filtering anyway.");
}
```

#### 2. Seed Data Updated ✅

**File**: `dental-clinic-seed-data.sql`

**ROLE_DENTIST** (Lines 448-457):

```sql
-- ❌ BEFORE:
('ROLE_DENTIST', 'VIEW_TREATMENT_PLAN_ALL'), -- Wrong permission

-- ✅ AFTER:
('ROLE_DENTIST', 'VIEW_TREATMENT_PLAN_OWN'), -- 🔒 Only view OWN plans
```

---

## 🎯 Permission Matrix (After Fix)

| Role             | Permission                | Can See                              |
| ---------------- | ------------------------- | ------------------------------------ |
| **ROLE_DENTIST** | `VIEW_TREATMENT_PLAN_OWN` | ✅ Only own plans (createdBy = self) |
| **ROLE_PATIENT** | `VIEW_TREATMENT_PLAN_OWN` | ✅ Only own plans (patient = self)   |
| **ROLE_MANAGER** | `VIEW_TREATMENT_PLAN_ALL` | ✅ All plans + filters               |
| **ROLE_ADMIN**   | `VIEW_TREATMENT_PLAN_ALL` | ✅ All plans + filters               |
| **ROLE_NURSE**   | ❌ No permission          | ❌ Cannot view                       |

---

## 🧪 Test Plan

### Test Case 1: Doctor with VIEW_TREATMENT_PLAN_OWN

**Endpoint**: `GET /api/v1/patient-treatment-plans`

**Setup**:

```sql
-- Doctor EMP001 creates 2 plans
-- Doctor EMP002 creates 2 plans
-- Total: 4 plans in database
```

**Test Command**:

```bash
# Login as Doctor EMP001
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bsnguyenvana@gmail.com",
    "password": "Doctor@123"
  }'

# Get all treatment plans (as Doctor)
curl -X GET "http://localhost:8080/api/v1/patient-treatment-plans?page=0&size=20" \
  -H "Authorization: Bearer <DOCTOR_TOKEN>"
```

**Expected Result**:

```json
{
  "content": [
    {
      "planCode": "PLAN-001",
      "patientCode": "BN-1001",
      "doctor": {
        "employeeCode": "EMP001",
        "fullName": "Bác sĩ Nguyễn Văn A"
      }
    },
    {
      "planCode": "PLAN-002",
      "patientCode": "BN-1002",
      "doctor": {
        "employeeCode": "EMP001",
        "fullName": "Bác sĩ Nguyễn Văn A"
      }
    }
  ],
  "totalElements": 2
}
```

**Verification**:

- ✅ Returns only 2 plans (not 4)
- ✅ All plans have `doctor.employeeCode = "EMP001"`
- ✅ Log: `🔒 EMPLOYEE mode: Filtering by employeeId=1`

---

### Test Case 2: Doctor attempts admin filter

**Test Command**:

```bash
# Doctor tries to use doctorEmployeeCode filter
curl -X GET "http://localhost:8080/api/v1/patient-treatment-plans?doctorEmployeeCode=EMP002" \
  -H "Authorization: Bearer <DOCTOR_TOKEN>"
```

**Expected Result**:

```json
{
  "content": [
    // ✅ Still returns only EMP001's plans (ignores filter)
    {
      "doctor": { "employeeCode": "EMP001" }
    }
  ],
  "totalElements": 2
}
```

**Verification**:

- ✅ Filter ignored (doctor cannot see EMP002's plans)
- ✅ Log: `🔒 SECURITY: Employee attempting admin-only filters. Ignoring.`

---

### Test Case 3: Manager with VIEW_TREATMENT_PLAN_ALL

**Test Command**:

```bash
# Login as Manager
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "quanlyphongkham@gmail.com",
    "password": "Manager@123"
  }'

# Get all treatment plans (as Manager)
curl -X GET "http://localhost:8080/api/v1/patient-treatment-plans?page=0&size=20" \
  -H "Authorization: Bearer <MANAGER_TOKEN>"
```

**Expected Result**:

```json
{
  "content": [
    { "planCode": "PLAN-001", "doctor": { "employeeCode": "EMP001" } },
    { "planCode": "PLAN-002", "doctor": { "employeeCode": "EMP001" } },
    { "planCode": "PLAN-003", "doctor": { "employeeCode": "EMP002" } },
    { "planCode": "PLAN-004", "doctor": { "employeeCode": "EMP002" } }
  ],
  "totalElements": 4
}
```

**Verification**:

- ✅ Returns all 4 plans
- ✅ Log: `✅ ADMIN mode: Can view all plans`

---

### Test Case 4: Manager uses filter

**Test Command**:

```bash
# Manager filters by doctorEmployeeCode
curl -X GET "http://localhost:8080/api/v1/patient-treatment-plans?doctorEmployeeCode=EMP002" \
  -H "Authorization: Bearer <MANAGER_TOKEN>"
```

**Expected Result**:

```json
{
  "content": [
    { "planCode": "PLAN-003", "doctor": { "employeeCode": "EMP002" } },
    { "planCode": "PLAN-004", "doctor": { "employeeCode": "EMP002" } }
  ],
  "totalElements": 2
}
```

**Verification**:

- ✅ Filter works correctly
- ✅ Returns only EMP002's plans

---

### Test Case 5: Patient with VIEW_TREATMENT_PLAN_OWN

**Test Command**:

```bash
# Login as Patient BN-1001
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "patient1001@gmail.com",
    "password": "Patient@123"
  }'

# Get treatment plans (as Patient)
curl -X GET "http://localhost:8080/api/v1/patient-treatment-plans?page=0&size=20" \
  -H "Authorization: Bearer <PATIENT_TOKEN>"
```

**Expected Result**:

```json
{
  "content": [
    {
      "planCode": "PLAN-001",
      "patientCode": "BN-1001"
    }
  ],
  "totalElements": 1
}
```

**Verification**:

- ✅ Returns only patient's own plans
- ✅ Log: `🔒 PATIENT mode: Filtering by patientId=1`

---

## 📊 Files Changed

| File                           | Lines Changed | Type           |
| ------------------------------ | ------------- | -------------- |
| `TreatmentPlanService.java`    | ~10           | Logic fix      |
| `dental-clinic-seed-data.sql`  | 1             | Permission fix |
| `RBAC_SECURITY_FIX_SUMMARY.md` | New           | Documentation  |

---

## ✅ Verification Checklist

- ✅ Service logic requires `VIEW_TREATMENT_PLAN_OWN` for EMPLOYEE
- ✅ Security warning logged if EMPLOYEE has `VIEW_TREATMENT_PLAN_ALL`
- ✅ Seed data: ROLE_DENTIST has `VIEW_TREATMENT_PLAN_OWN` (not ALL)
- ✅ Seed data: ROLE_MANAGER has `VIEW_TREATMENT_PLAN_ALL`
- ✅ Seed data: ROLE_ADMIN has ALL permissions
- ✅ Seed data: ROLE_PATIENT has `VIEW_TREATMENT_PLAN_OWN`
- ✅ No compilation errors
- ✅ Ready for curl testing

---

## 🚀 Next Steps

1. **Restart Application** (to reload seed data)

   ```bash
   # Stop application
   # Re-run with fresh database (seed data will apply)
   mvn spring-boot:run
   ```

2. **Run Curl Tests** (see test cases above)

   - Test Doctor → Should see only own plans
   - Test Manager → Should see all plans
   - Test Patient → Should see only own plans

3. **Verify Logs**
   ```
   ✅ Expected logs:
   - "🔒 EMPLOYEE mode: Filtering by employeeId=1"
   - "✅ ADMIN mode: Can view all plans"
   - "🔒 PATIENT mode: Filtering by patientId=1"
   ```

---

**Status**: ✅ **READY FOR TESTING**
**Priority**: 🔴 **P0 - Deploy Immediately After Test**
