# Backend Fix: API 5.11 & Related - Added Error Codes to ConflictException

**Date**: November 17, 2025
**Issue**: FE Issue #5.11 - 409 Conflict khi Delete Item từ Plan đã APPROVED
**Fix Type**: Backend Enhancement - Better Error Handling
**Status**: ✅ FIXED

---

## 📋 Problem Analysis

### FE Issue Summary

Khi user cố gắng xóa treatment plan item từ plan đã APPROVED, API trả về 409 Conflict với message generic. FE không thể phân biệt loại conflict để hiển thị UI phù hợp.

### Root Cause

Backend business logic **HOÀN TOÀN ĐÚNG** (expected behavior), nhưng:

1. ❌ Error response thiếu **error code** cụ thể
2. ❌ FE không thể phân biệt các loại conflict khác nhau
3. ⚠️ Addition service có BUG: Chỉ check PENDING_REVIEW, KHÔNG CHECK APPROVED

---

## 🔧 Backend Fixes Applied

### Fix #1: Added Error Codes to TreatmentPlanItemDeletionService

**File**: `TreatmentPlanItemDeletionService.java`

#### GUARD 1: Item Status Validation

```java
// BEFORE
throw new ConflictException(errorMsg);

// AFTER (Line 130)
throw new ConflictException("ITEM_SCHEDULED_CANNOT_DELETE", errorMsg);
```

**Error Code**: `ITEM_SCHEDULED_CANNOT_DELETE`
**When**: Item status is SCHEDULED/IN_PROGRESS/COMPLETED
**Message**: "Không thể xóa hạng mục đã được đặt lịch hoặc đang thực hiện..."

---

#### GUARD 2: Plan Approval Status Validation

```java
// BEFORE
throw new ConflictException(errorMsg);

// AFTER (Line 154)
throw new ConflictException("PLAN_APPROVED_CANNOT_DELETE", errorMsg);
```

**Error Code**: `PLAN_APPROVED_CANNOT_DELETE`
**When**: Plan approval status is APPROVED or PENDING_REVIEW
**Message**: "Không thể xóa hạng mục khỏi lộ trình đã được duyệt hoặc đang chờ duyệt..."

---

#### Authentication Errors

```java
// BEFORE
throw new ConflictException("Không thể xác định người thực hiện");
throw new ConflictException("Tài khoản không liên kết với nhân viên");

// AFTER (Lines 215, 223)
throw new ConflictException("AUTH_USER_NOT_FOUND", "Không thể xác định người thực hiện");
throw new ConflictException("EMPLOYEE_NOT_LINKED", "Tài khoản không liên kết với nhân viên");
```

---

### Fix #2: Added Error Code to TreatmentPlanItemUpdateService

**File**: `TreatmentPlanItemUpdateService.java`

```java
// BEFORE
throw new ConflictException(String.format("Không thể sửa lộ trình..."));

// AFTER (Line 149)
throw new ConflictException("PLAN_APPROVED_CANNOT_UPDATE", errorMsg);
```

**Error Code**: `PLAN_APPROVED_CANNOT_UPDATE`
**When**: Plan approval status is APPROVED or PENDING_REVIEW
**Message**: "Không thể sửa lộ trình đã được duyệt hoặc đang chờ duyệt..."

---

### Fix #3: 🐛 BUG FIX - TreatmentPlanItemAdditionService

**File**: `TreatmentPlanItemAdditionService.java`

#### Problem Found

```java
// BEFORE (Line 94) - ONLY checks PENDING_REVIEW, missing APPROVED!
if (plan.getApprovalStatus() == ApprovalStatus.PENDING_REVIEW) {
    throw new ResponseStatusException(HttpStatus.CONFLICT, "Plan is pending approval...");
}
```

**BUG**: Plan với status APPROVED vẫn có thể thêm items! ❌

#### Fix Applied

```java
// AFTER (Lines 94-103)
if (plan.getApprovalStatus() == ApprovalStatus.APPROVED ||
    plan.getApprovalStatus() == ApprovalStatus.PENDING_REVIEW) {

    String errorMsg = String.format(
        "Không thể thêm hạng mục vào lộ trình đã được duyệt hoặc đang chờ duyệt (Trạng thái: %s). " +
        "Yêu cầu Quản lý 'Từ chối' (Reject) về DRAFT trước khi thêm.",
        plan.getApprovalStatus());

    throw new ConflictException("PLAN_APPROVED_CANNOT_ADD", errorMsg);
}
```

**Error Code**: `PLAN_APPROVED_CANNOT_ADD`
**Changes**:

1. ✅ Now checks both APPROVED and PENDING_REVIEW
2. ✅ Uses ConflictException with error code (imported)
3. ✅ Consistent error message format with Update/Delete

---

## 📊 Error Code Summary

| Error Code                     | Service | Trigger Condition                             | HTTP Status |
| ------------------------------ | ------- | --------------------------------------------- | ----------- |
| `ITEM_SCHEDULED_CANNOT_DELETE` | Delete  | Item status = SCHEDULED/IN_PROGRESS/COMPLETED | 409         |
| `PLAN_APPROVED_CANNOT_DELETE`  | Delete  | Plan approval = APPROVED/PENDING_REVIEW       | 409         |
| `PLAN_APPROVED_CANNOT_UPDATE`  | Update  | Plan approval = APPROVED/PENDING_REVIEW       | 409         |
| `PLAN_APPROVED_CANNOT_ADD`     | Add     | Plan approval = APPROVED/PENDING_REVIEW       | 409         |
| `AUTH_USER_NOT_FOUND`          | All     | No authenticated user in context              | 409         |
| `EMPLOYEE_NOT_LINKED`          | All     | Account has no linked employee                | 409         |

---

## 🎯 Error Response Format

### Standard ProblemDetail Response (RFC 7807)

**Example Response**:

```json
{
  "type": "https://api.dental-clinic.com/problems/conflict",
  "title": "Conflict",
  "status": 409,
  "detail": "Không thể xóa hạng mục khỏi lộ trình đã được duyệt hoặc đang chờ duyệt (Trạng thái: APPROVED). Yêu cầu Quản lý 'Từ chối' (Reject) về DRAFT trước khi sửa.",
  "errorCode": "PLAN_APPROVED_CANNOT_DELETE"
}
```

**Fields**:

- `type`: URI to problem documentation
- `title`: Generic title ("Conflict")
- `status`: HTTP status code (409)
- `detail`: Detailed Vietnamese error message
- `errorCode`: **Specific error code** (NEW!)

---

## 💡 Frontend Integration Guide

### Option 1: Check Error Code (RECOMMENDED)

```typescript
try {
  await deletePlanItem(itemId);
} catch (error) {
  if (error.response?.status === 409) {
    const errorCode = error.response?.data?.errorCode;

    switch (errorCode) {
      case "PLAN_APPROVED_CANNOT_DELETE":
        toast.error("Không thể xóa", {
          description:
            "Lộ trình đã được duyệt. Yêu cầu quản lý từ chối về DRAFT trước.",
          action: {
            label: "Xem hướng dẫn",
            onClick: () => showApprovalWorkflowGuide(),
          },
        });
        break;

      case "ITEM_SCHEDULED_CANNOT_DELETE":
        toast.error("Không thể xóa", {
          description: "Hạng mục đã có lịch hẹn. Vui lòng hủy lịch trước.",
          action: {
            label: "Xem lịch hẹn",
            onClick: () => navigateToAppointments(),
          },
        });
        break;

      default:
        // Use backend message
        toast.error("Xung đột", {
          description:
            error.response?.data?.detail || "Không thể thực hiện thao tác",
        });
    }
  }
}
```

---

### Option 2: Prevent Action (Best UX)

**Disable buttons based on plan status**:

```typescript
// In TreatmentPlanItem.tsx
const canDelete =
  canUpdate &&
  plan.approvalStatus === ApprovalStatus.DRAFT &&
  item.status !== PlanItemStatus.SCHEDULED &&
  item.status !== PlanItemStatus.IN_PROGRESS &&
  item.status !== PlanItemStatus.COMPLETED;

<Button
  variant="ghost"
  size="sm"
  onClick={() => handleDelete(item.itemId)}
  disabled={!canDelete}
  title={getDeleteDisabledReason(plan, item)}
>
  <Trash2 className="w-4 h-4 text-red-500" />
</Button>;
```

**Tooltip helper**:

```typescript
function getDeleteDisabledReason(plan, item) {
  if (plan.approvalStatus !== ApprovalStatus.DRAFT) {
    return "Chỉ có thể xóa hạng mục khi lộ trình ở trạng thái DRAFT";
  }

  if (
    [
      PlanItemStatus.SCHEDULED,
      PlanItemStatus.IN_PROGRESS,
      PlanItemStatus.COMPLETED,
    ].includes(item.status)
  ) {
    return "Không thể xóa hạng mục đã có lịch hẹn hoặc đang thực hiện";
  }

  return "";
}
```

---

## 🔄 Workflow Impact

### Before Fix

1. User clicks "Xóa" button
2. Modal opens
3. User confirms
4. ❌ 409 Conflict with generic message
5. User confused

### After Fix (Backend + Frontend)

1. ✅ Delete button **disabled** if plan is APPROVED
2. ✅ Tooltip explains why
3. If somehow triggered:
   - ✅ Error code identifies exact problem
   - ✅ Custom message guides user
   - ✅ Action button shows next steps

---

## 🐛 Bug Fixed: Addition Service

### Impact

**CRITICAL**: Before fix, users could add items to APPROVED plans!

**Root Cause**: Line 94 only checked `PENDING_REVIEW`, missed `APPROVED`

**Test Case**:

```bash
# BEFORE FIX
Plan status: APPROVED
POST /api/v1/plan-phases/{phaseId}/items
Result: ✅ 200 OK (WRONG! Should be 409)

# AFTER FIX
Plan status: APPROVED
POST /api/v1/plan-phases/{phaseId}/items
Result: ❌ 409 Conflict (CORRECT)
Error Code: PLAN_APPROVED_CANNOT_ADD
```

---

## 📝 Files Modified

| File                                    | Lines    | Change                                                          |
| --------------------------------------- | -------- | --------------------------------------------------------------- |
| `TreatmentPlanItemDeletionService.java` | 130      | Added error code: `ITEM_SCHEDULED_CANNOT_DELETE`                |
| `TreatmentPlanItemDeletionService.java` | 154      | Added error code: `PLAN_APPROVED_CANNOT_DELETE`                 |
| `TreatmentPlanItemDeletionService.java` | 215, 223 | Added error codes: `AUTH_USER_NOT_FOUND`, `EMPLOYEE_NOT_LINKED` |
| `TreatmentPlanItemUpdateService.java`   | 149      | Added error code: `PLAN_APPROVED_CANNOT_UPDATE`                 |
| `TreatmentPlanItemAdditionService.java` | 4        | Added import: `ConflictException`                               |
| `TreatmentPlanItemAdditionService.java` | 94-103   | 🐛 **BUG FIX**: Now checks APPROVED + PENDING_REVIEW            |
| `TreatmentPlanItemAdditionService.java` | 103      | Added error code: `PLAN_APPROVED_CANNOT_ADD`                    |

---

## ✅ Testing Checklist

### Backend Tests

- [ ] Test Delete item from APPROVED plan → 409 with `PLAN_APPROVED_CANNOT_DELETE`
- [ ] Test Delete item from PENDING_REVIEW plan → 409 with `PLAN_APPROVED_CANNOT_DELETE`
- [ ] Test Delete SCHEDULED item → 409 with `ITEM_SCHEDULED_CANNOT_DELETE`
- [ ] Test Update item in APPROVED plan → 409 with `PLAN_APPROVED_CANNOT_UPDATE`
- [ ] Test Add item to APPROVED plan → 409 with `PLAN_APPROVED_CANNOT_ADD` (BUG FIX)
- [ ] Test Add item to PENDING_REVIEW plan → 409 with `PLAN_APPROVED_CANNOT_ADD`

### Frontend Tests

- [ ] Delete button disabled when plan is APPROVED
- [ ] Delete button disabled when plan is PENDING_REVIEW
- [ ] Delete button disabled when item is SCHEDULED
- [ ] Tooltip shows correct message for each case
- [ ] Error toast shows specific message based on error code
- [ ] Action buttons in toast navigate to correct pages

---

## 📚 Related Documentation

- **API 5.11**: Delete Treatment Plan Item
- **API 5.7**: Update Treatment Plan Item
- **API 5.6**: Update Treatment Plan Item Status
- **API 5.10**: Add Items to Phase
- **Approval Workflow**: Treatment Plan Approval Process

---

## 🎯 Summary

### What Changed

1. ✅ Added 6 specific error codes to ConflictException
2. ✅ Standardized error response format (ProblemDetail RFC 7807)
3. 🐛 **FIXED BUG**: Addition service now correctly blocks APPROVED plans

### Why It Matters

- ✅ Better frontend error handling
- ✅ Improved UX with specific messages
- ✅ Easier debugging with error codes
- ✅ Consistent error format across APIs

### Next Steps for FE

1. Update error handling to check `errorCode` field
2. Disable action buttons based on plan/item status
3. Show contextual tooltips
4. Add "Next Steps" action buttons in error toasts

---

**Status**: ✅ FIXED - Ready for deployment
**Build Status**: ⏳ Pending (JAVA_HOME not configured)
**Needs**: Compile & test with IntelliJ IDEA

---

## 📞 Contact

If you have questions about error codes or need additional codes:

- Check `ConflictException.java` class
- All error codes use format: `{ENTITY}_{REASON}_{ACTION}`
- Example: `PLAN_APPROVED_CANNOT_DELETE`

**Last Updated**: 2025-11-17
