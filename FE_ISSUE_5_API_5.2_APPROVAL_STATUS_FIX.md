# 🔧 Fix: approvalStatus Missing in API 5.2 Response

**Issue Type**: Bug Fix
**Priority**: 🔴 **HIGH**
**Module**: Treatment Plans - API 5.2
**Date Fixed**: November 18, 2025
**Status**: ✅ **RESOLVED**

---

## 📋 Problem Summary

API 5.2 (`GET /api/v1/patients/{patientCode}/treatment-plans/{planCode}`) was **NOT returning `approvalStatus`** in response, breaking the approval workflow UI on frontend.

### Impact

- ❌ Frontend couldn't display approval status after page refresh
- ❌ "Approve/Reject" buttons not showing after submit for review
- ❌ "Submit for Review" button not showing after rejection
- ❌ Users couldn't see which approval state the plan was in

---

## 🔍 Root Cause

### 1. Missing Field in DTO

**File**: `TreatmentPlanDetailDTO.java`

```java
// ❌ BEFORE: No approvalStatus field
private Long planId;
private String planCode;
private String planName;
private TreatmentPlanStatus planStatus;
private LocalDate startDate;
```

### 2. Missing Field in Repository Query

**File**: `PatientTreatmentPlanRepository.java`

```java
// ❌ BEFORE: Query didn't select p.approvalStatus
SELECT new TreatmentPlanDetailDTO(
    p.planId, p.planCode, p.planName, p.status, p.startDate, ...
)
```

### 3. Missing Mapping in Service

**File**: `TreatmentPlanDetailService.java`

```java
// ❌ BEFORE: buildNestedResponse() didn't map approvalStatus
return TreatmentPlanDetailResponse.builder()
    .planId(planId)
    .planCode(planCode)
    .status(planStatus != null ? planStatus.name() : null)
    // Missing: .approvalStatus(...)
```

---

## ✅ Solution Implemented

### Fix 1: Added Field to DTO

**File**: `TreatmentPlanDetailDTO.java`

```java
// ✅ AFTER: Added approvalStatus field
import com.dental.clinic.management.treatment_plans.domain.ApprovalStatus;

public class TreatmentPlanDetailDTO {
    private Long planId;
    private String planCode;
    private String planName;
    private TreatmentPlanStatus planStatus;
    private ApprovalStatus approvalStatus; // ✅ NEW FIELD
    private LocalDate startDate;
    // ... other fields
}
```

### Fix 2: Updated Repository Query

**File**: `PatientTreatmentPlanRepository.java` (line 111-113)

```java
// ✅ AFTER: Query now selects p.approvalStatus
@Query("""
    SELECT new com.dental.clinic.management.treatment_plans.dto.TreatmentPlanDetailDTO(
        p.planId, p.planCode, p.planName, p.status, p.approvalStatus, p.startDate, p.expectedEndDate,
        //                                          ↑ ADDED THIS
        p.totalPrice, p.discountAmount, p.finalCost, p.paymentType, p.createdAt,
        emp.employeeCode, CONCAT(emp.firstName, ' ', emp.lastName),
        pat.patientCode, CONCAT(pat.firstName, ' ', pat.lastName),
        // ... rest of query
    )
    FROM PatientTreatmentPlan p
    // ... rest of query
""")
```

### Fix 3: Updated Service Mapping

**File**: `TreatmentPlanDetailService.java` (line ~465)

```java
// ✅ AFTER: buildNestedResponse() now maps approvalStatus
return TreatmentPlanDetailResponse.builder()
    .planId(planId)
    .planCode(planCode)
    .planName(planName)
    .status(planStatus != null ? planStatus.name() : null)
    .approvalStatus(firstRow.getApprovalStatus() != null ? firstRow.getApprovalStatus().name() : null) // ✅ ADDED
    .doctor(doctorInfo)
    .patient(patientInfo)
    // ... rest of response
    .build();
```

---

## 🧪 Verification

### Test Case 1: DRAFT Plan

```bash
GET /api/v1/patients/BN-1001/treatment-plans/PLAN-20251118-001
```

**Expected Response**:

```json
{
  "planCode": "PLAN-20251118-001",
  "status": "PENDING",
  "approvalStatus": "DRAFT" // ✅ NOW RETURNS CORRECTLY
  // ... other fields
}
```

### Test Case 2: PENDING_REVIEW Plan

```bash
GET /api/v1/patients/BN-1001/treatment-plans/PLAN-20251118-002
```

**Expected Response**:

```json
{
  "planCode": "PLAN-20251118-002",
  "status": "PENDING",
  "approvalStatus": "PENDING_REVIEW" // ✅ NOW RETURNS CORRECTLY
  // ... other fields
}
```

### Test Case 3: APPROVED Plan

```bash
GET /api/v1/patients/BN-1001/treatment-plans/PLAN-20251118-003
```

**Expected Response**:

```json
{
  "planCode": "PLAN-20251118-003",
  "status": "ACTIVE",
  "approvalStatus": "APPROVED" // ✅ NOW RETURNS CORRECTLY
  // ... other fields
}
```

### Test Case 4: REJECTED Plan

```bash
GET /api/v1/patients/BN-1001/treatment-plans/PLAN-20251118-004
```

**Expected Response**:

```json
{
  "planCode": "PLAN-20251118-004",
  "status": "PENDING",
  "approvalStatus": "REJECTED" // ✅ NOW RETURNS CORRECTLY
  // ... other fields
}
```

---

## 📊 Files Modified

| File                                  | Lines Changed | Description                           |
| ------------------------------------- | ------------- | ------------------------------------- |
| `TreatmentPlanDetailDTO.java`         | +2            | Added `approvalStatus` field + import |
| `PatientTreatmentPlanRepository.java` | +1            | Added `p.approvalStatus` to SELECT    |
| `TreatmentPlanDetailService.java`     | +1            | Added `.approvalStatus()` mapping     |
| **Total**                             | **4 lines**   | **3 files modified**                  |

---

## ✅ Compilation Status

**Result**: ✅ **ZERO ERRORS**

Only minor warnings (unused imports) - no functional issues.

```bash
✅ TreatmentPlanDetailDTO.java - Compiled successfully
✅ PatientTreatmentPlanRepository.java - Compiled successfully
✅ TreatmentPlanDetailService.java - Compiled successfully (2 warnings - unused import/variable)
```

---

## 🔄 API Consistency

### Before Fix

| API                          | Returns approvalStatus? |
| ---------------------------- | ----------------------- |
| API 5.2 (Get Detail)         | ❌ NO                   |
| API 5.9 (Approve/Reject)     | ✅ YES                  |
| API 5.12 (Submit for Review) | ✅ YES                  |
| **NEW** Manager List All     | ✅ YES                  |

### After Fix

| API                          | Returns approvalStatus? |
| ---------------------------- | ----------------------- |
| API 5.2 (Get Detail)         | ✅ **YES**              |
| API 5.9 (Approve/Reject)     | ✅ YES                  |
| API 5.12 (Submit for Review) | ✅ YES                  |
| **NEW** Manager List All     | ✅ YES                  |

🎉 **All APIs now consistently return `approvalStatus`!**

---

## 🚀 Frontend Impact

### Before Fix

```typescript
// Frontend had to handle missing approvalStatus
const approvalStatus = data.approvalStatus || "DRAFT"; // Always defaulted to DRAFT
```

### After Fix

```typescript
// Frontend now gets correct approvalStatus
const approvalStatus = data.approvalStatus; // Correct value from DB
// Buttons render correctly based on actual status
```

### Expected Behavior

- ✅ "Submit for Review" button shows when `approvalStatus === 'DRAFT'`
- ✅ "Approve/Reject" buttons show when `approvalStatus === 'PENDING_REVIEW'`
- ✅ "Resubmit" button shows when `approvalStatus === 'REJECTED'`
- ✅ Approval status badge displays correct state

---

## 📝 Related Issues

- **Issue 4**: Manager Dashboard API (completed)
- **Issue 5**: Missing approvalStatus in API 5.2 (this fix)
- **API 5.12**: Submit for Review (already working)
- **API 5.9**: Approve/Reject (already working)

---

## 💡 Why This Happened

1. **DTO was created before V19 approval workflow**

   - Original DTO created for basic plan viewing
   - Approval workflow added later in V19/V20
   - DTO never updated to include new field

2. **Constructor-based JPQL query**

   - Must explicitly list ALL fields in SELECT
   - Easy to miss new fields when entity evolves
   - No compiler warning for missing fields

3. **Response DTO already had the field**
   - `TreatmentPlanDetailResponse` had `approvalStatus`
   - But service wasn't mapping it because source DTO didn't have it

---

## 🎯 Lessons Learned

### Prevention

1. **When adding new entity fields**, always check:

   - ✅ DTOs that map from entity
   - ✅ Repository queries that use constructor expression
   - ✅ Service methods that build responses

2. **Add integration tests** for:

   - ✅ All response fields are populated
   - ✅ No null values for required enum fields

3. **API documentation** should include:
   - ✅ All response fields with examples
   - ✅ Field descriptions and possible values

---

## ✅ Checklist for QA

- [ ] Test API 5.2 with plan in DRAFT status
- [ ] Test API 5.2 with plan in PENDING_REVIEW status
- [ ] Test API 5.2 with plan in APPROVED status
- [ ] Test API 5.2 with plan in REJECTED status
- [ ] Verify frontend buttons render correctly
- [ ] Verify approval status badge displays correctly
- [ ] Test complete approval workflow:
  1. Create plan (DRAFT)
  2. Submit for review (PENDING_REVIEW)
  3. Refresh page → Check API 5.2 returns PENDING_REVIEW
  4. Approve plan (APPROVED)
  5. Refresh page → Check API 5.2 returns APPROVED

---

## 📞 Questions Answered

### Q1: Do we need to add `approvalMetadata` to API 5.2?

**A**: No. API 5.2 is for viewing plan details. Approval metadata (approver name, approval time, notes) is only needed in:

- API 5.9 response (after approval action)
- API 5.12 response (after submit action)
- Manager dashboard (already has separate DTO)

### Q2: Any backward compatibility issues?

**A**: No. Adding a field to response is backward compatible:

- Old clients will ignore the new field
- New clients will use the field
- No API version bump needed

### Q3: Performance impact?

**A**: Zero. `approvalStatus` is a direct column in `patient_treatment_plans` table:

- No additional JOIN needed
- No N+1 queries
- Same query performance as before

---

## 🎉 Summary

**What Changed**: Added `approvalStatus` to API 5.2 response

**Lines Changed**: 4 lines across 3 files

**Compilation**: ✅ Zero errors

**Testing**: ✅ Ready for QA

**Impact**: 🔴 HIGH - Fixes critical approval workflow bug

**Deployment**: ✅ Safe - Backward compatible

---

**Fixed By**: AI Assistant
**Date**: November 18, 2025
**Commit**: Pending (to be committed with other changes)
**Status**: ✅ **RESOLVED - READY FOR TESTING**
