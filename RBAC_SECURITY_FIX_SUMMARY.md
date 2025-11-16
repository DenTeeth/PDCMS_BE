# RBAC Security Fix: Doctor với VIEW_TREATMENT_PLAN_ALL

**Date**: 2025-11-15
**Severity**: 🔴 **HIGH** - Security/Privacy Issue
**Status**: ✅ **FIXED** (Backend)

---

## 📋 Problem Description

**Security Issue**: Doctor (employee) với permission `VIEW_TREATMENT_PLAN_ALL` có thể thấy **TẤT CẢ** treatment plans của tất cả bác sĩ khác, vi phạm quyền riêng tư.

**Root Cause**:
Backend check **permission trước role**:

- Nếu có `VIEW_TREATMENT_PLAN_ALL` → Admin mode → Không filter
- Doctor được coi như Admin → Thấy tất cả plans

---

## ✅ Backend Fix Applied

### Solution: Check Role BEFORE Permission

**File**: `TreatmentPlanService.java` - Method `getAllTreatmentPlans()` (API 5.5)

### Changed Logic:

#### ❌ BEFORE (Insecure):

```java
// Step 3: Apply RBAC Filters
if (hasViewAllPermission) {
    // ❌ PROBLEM: No role check!
    // Admin: Can see all plans
    log.info("Admin mode...");
}
else if (hasViewOwnPermission) {
    // Check role and filter
}
```

**Problem**: Doctor with `VIEW_TREATMENT_PLAN_ALL` enters first `if` block → No role check → See all plans

---

#### ✅ AFTER (Secure):

```java
// Step 3: Apply RBAC Filters (🔒 SECURITY FIX 2025-11-15)
// ✅ FIX: Check ROLE first, then permission

// Get account and base role
Integer baseRoleId = account.getRole().getBaseRole().getBaseRoleId();

if (baseRoleId.equals(BaseRoleConstants.EMPLOYEE)) {
    // 🔒 EMPLOYEE: Always filter by createdBy (regardless of permission)
    specification = specification.and(
        TreatmentPlanSpecification.filterByCreatedByEmployee(employeeId)
    );

    // Ignore admin-only filters for security
    if (request.getDoctorEmployeeCode() != null) {
        log.warn("🔒 SECURITY: Employee attempting admin-only filters. Ignoring.");
    }
}
else if (baseRoleId.equals(BaseRoleConstants.PATIENT)) {
    // 🔒 PATIENT: Always filter by patient
    specification = specification.and(
        TreatmentPlanSpecification.filterByPatient(patientId)
    );
}
else if (baseRoleId.equals(BaseRoleConstants.ADMIN)) {
    // ✅ ADMIN: Can see all plans
    // No additional RBAC filter needed
}
```

**Fix**: Role check happens FIRST, regardless of permission. Doctor always gets filtered by `createdBy`.

---

## 🔒 Security Enhancements

### 1. Role-Based Filtering (Priority 0)

```java
// EMPLOYEE (Doctor): Always filter by createdBy
// - Even with VIEW_TREATMENT_PLAN_ALL, can only see own plans
// - Admin-only filters (doctorEmployeeCode, patientCode) are ignored

// PATIENT: Always filter by patient
// - Can only see own plans
// - Admin-only filters are ignored

// ADMIN: No filtering
// - Can see all plans
// - Can use doctorEmployeeCode and patientCode filters
```

### 2. Security Logging

```java
log.info("🔒 EMPLOYEE mode: Filtering by employeeId={} (regardless of permission)", employeeId);
log.warn("🔒 SECURITY: Employee (id={}) attempting admin-only filters. Ignoring.", employeeId);
```

### 3. Permission Validation

```java
// Employee must have at least one permission
if (!hasViewAllPermission && !hasViewOwnPermission) {
    throw new AccessDeniedException("Employee must have VIEW_TREATMENT_PLAN_ALL or VIEW_TREATMENT_PLAN_OWN");
}

// Admin must have VIEW_TREATMENT_PLAN_ALL
if (!hasViewAllPermission) {
    throw new AccessDeniedException("Admin must have VIEW_TREATMENT_PLAN_ALL");
}
```

---

## 🧪 Test Scenarios

### Test Case 1: Doctor với VIEW_TREATMENT_PLAN_ALL ✅

**Setup**:

```sql
-- Doctor EMP001 with permission VIEW_TREATMENT_PLAN_ALL
-- Plans created by EMP001: PLAN-001, PLAN-002
-- Plans created by EMP002: PLAN-003, PLAN-004
```

**Before Fix**:

```bash
GET /api/v1/patient-treatment-plans
Authorization: Bearer <EMP001_TOKEN>

Response:
❌ Returns 4 plans (PLAN-001, PLAN-002, PLAN-003, PLAN-004)
❌ Doctor sees plans from other doctors
```

**After Fix**:

```bash
GET /api/v1/patient-treatment-plans
Authorization: Bearer <EMP001_TOKEN>

Response:
✅ Returns 2 plans (PLAN-001, PLAN-002)
✅ Doctor only sees own plans
✅ Log: "🔒 EMPLOYEE mode: Filtering by employeeId=1"
```

---

### Test Case 2: Doctor attempts admin-only filter ✅

**Setup**:

```bash
GET /api/v1/patient-treatment-plans?doctorEmployeeCode=EMP002
Authorization: Bearer <EMP001_TOKEN>
```

**Before Fix**:

```bash
❌ Returns plans created by EMP002 (security breach)
```

**After Fix**:

```bash
✅ Ignores doctorEmployeeCode filter
✅ Returns only EMP001's plans
✅ Log: "🔒 SECURITY: Employee (id=1) attempting admin-only filters. Ignoring."
```

---

### Test Case 3: Admin với VIEW_TREATMENT_PLAN_ALL ✅

**Setup**:

```bash
GET /api/v1/patient-treatment-plans
Authorization: Bearer <ADMIN_TOKEN>
```

**Before Fix**:

```bash
✅ Returns all plans (works correctly)
```

**After Fix**:

```bash
✅ Returns all plans (still works)
✅ Log: "✅ ADMIN mode: Can view all plans"
```

---

### Test Case 4: Admin uses filter ✅

**Setup**:

```bash
GET /api/v1/patient-treatment-plans?doctorEmployeeCode=EMP002&patientCode=BN-1001
Authorization: Bearer <ADMIN_TOKEN>
```

**Before Fix**:

```bash
✅ Filters work correctly
```

**After Fix**:

```bash
✅ Filters still work correctly
✅ Admin can use doctorEmployeeCode and patientCode filters
```

---

## 📊 Security Impact

### Before Fix (Security Vulnerabilities):

- 🔴 **Privacy Breach**: Doctor can see other doctors' patients
- 🔴 **Data Leak**: Sensitive medical information exposed
- 🔴 **Compliance Risk**: May violate HIPAA/GDPR regulations
- 🔴 **Audit Risk**: Unauthorized data access not logged properly

### After Fix (Security Hardened):

- ✅ **Role-Based Access**: Enforced at service layer
- ✅ **Privacy Protected**: Doctor can only see own patients
- ✅ **Security Logged**: All access attempts logged with security markers
- ✅ **Filter Ignored**: Admin-only filters ignored for non-admin users
- ✅ **Compliance Ready**: Proper data isolation

---

## 🎯 Code Review Checklist

- ✅ Role check happens BEFORE permission check
- ✅ EMPLOYEE always filtered by `createdBy` (regardless of permission)
- ✅ PATIENT always filtered by `patient`
- ✅ ADMIN is the only role that can see all plans
- ✅ Admin-only filters are ignored for EMPLOYEE and PATIENT
- ✅ Security warnings logged when non-admin attempts admin filters
- ✅ No compilation errors
- ✅ Backward compatible (Admin functionality unchanged)

---

## 🔗 Related Security Issues

1. **Issue 3.3**: `patientCode` missing in JWT - ✅ FIXED
2. **Issue 3.1**: `planCode` missing in DTO - ✅ FIXED
3. **This Issue**: RBAC bypass via permission - ✅ FIXED

---

## 📝 Frontend Impact

### Frontend Workaround (No Longer Needed):

```typescript
// ❌ OLD: Frontend manually adds doctorEmployeeCode filter
// This workaround is no longer needed but can be kept for clarity

// ✅ NEW: Backend enforces RBAC, frontend can rely on it
// No changes required on frontend
```

### What Frontend Can Expect:

1. **Doctor**: Always gets own plans only (no matter what filter they send)
2. **Patient**: Always gets own plans only
3. **Admin**: Gets all plans (filters work as expected)

---

## 🚀 Deployment Notes

### Breaking Changes:

- ❌ None (backend fix is transparent to frontend)

### Behavioral Changes:

- ✅ Doctor with `VIEW_TREATMENT_PLAN_ALL` will now see fewer plans (only own plans)
- ✅ This is the **correct** behavior (security fix, not a regression)

### Migration:

- ✅ No database migration needed
- ✅ No frontend changes required
- ✅ Deploy and test immediately

---

## ✅ Acceptance Criteria (All Met)

- ✅ Doctor with `VIEW_TREATMENT_PLAN_ALL` can only see own plans
- ✅ Doctor with `VIEW_TREATMENT_PLAN_OWN` can only see own plans
- ✅ Patient can only see own plans
- ✅ Admin can see all plans
- ✅ Admin filters (doctorEmployeeCode, patientCode) work only for admin
- ✅ Security warnings logged for unauthorized filter attempts
- ✅ No compilation errors
- ✅ Backward compatible with admin functionality

---

**Fixed By**: Backend Development Team
**Date**: 2025-11-15
**Status**: ✅ **COMPLETED & TESTED**
**Priority**: 🔴 P0 - Critical Security Fix
**Next**: Deploy to production immediately
