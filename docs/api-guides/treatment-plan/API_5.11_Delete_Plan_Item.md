# API 5.11: Xóa Hạng mục khỏi Lộ trình (Delete Treatment Plan Item)

**Version**: V20
**Date**: 2025-11-15
**Module**: Treatment Plans (Bệnh án & Lộ trình Điều trị)

---

## 📋 Overview

API này được sử dụng bởi **Bác sĩ** để xóa vĩnh viễn một hạng mục đã thêm nhầm ra khỏi lộ trình điều trị khi lộ trình đang ở trạng thái **DRAFT** (Nháp).

**Typical Use Case**: Sau khi Quản lý từ chối (REJECT) lộ trình, Bác sĩ nhận ra có item thêm nhầm, dùng API này để xóa item đó trước khi gửi duyệt lại.

---

## 🔑 API Specification

| Property                | Value                                        |
| ----------------------- | -------------------------------------------- |
| **Method**              | `DELETE`                                     |
| **Endpoint**            | `/api/v1/patient-plan-items/{itemId}`        |
| **Content-Type**        | `application/json`                           |
| **Authorization**       | Bearer Token (JWT)                           |
| **Permission Required** | `UPDATE_TREATMENT_PLAN`                      |
| **Roles**               | `ROLE_DENTIST`, `ROLE_MANAGER`, `ROLE_ADMIN` |
| **Request Body**        | None (DELETE method)                         |

---

## 🎯 Business Flow

```
Scenario: Doctor realizes an item was added by mistake

1. Bác sĩ tạo lộ trình với item thêm nhầm (ví dụ: "Cạo vôi răng" thay vì "Trám răng")
   └─> Plan.approvalStatus = PENDING_REVIEW

2. Quản lý REJECT plan (API 5.9)
   └─> Plan.approvalStatus = DRAFT
   └─> Notes: "Item 'Cạo vôi răng' không phù hợp với chẩn đoán"

3. Bác sĩ xem lại và nhận ra item thêm nhầm
   └─> Gọi API 5.11 để XÓA item sai

4. DELETE /api/v1/patient-plan-items/538
   └─> Item deleted
   └─> Plan finances recalculated (decreased by item price)
   └─> Audit log created: "ITEM_DELETED"
   └─> Plan.approvalStatus vẫn là DRAFT

5. Bác sĩ có thể thêm item đúng (API 5.7) hoặc gửi duyệt lại ngay
```

---

## 📦 Request Parameters

### Path Parameter

| Parameter | Type | Required | Description             | Example |
| --------- | ---- | -------- | ----------------------- | ------- |
| `itemId`  | Long | Yes      | ID của hạng mục cần xóa | `538`   |

### Request Body

**No request body required** (DELETE method does not have body)

---

## ⚙️ Business Logic & Validation Guards

### 1️⃣ Find Item and Get Related Data (BEFORE Delete)

```java
PatientPlanItem item = itemRepository.findById(itemId)
    .orElseThrow(() -> new NotFoundException("Hạng mục không tồn tại"));

// Get parent entities BEFORE delete (to avoid lost reference)
PatientPlanPhase phase = item.getPhase();
PatientTreatmentPlan plan = phase.getTreatmentPlan();
BigDecimal deletedPrice = item.getPrice();
String deletedItemName = item.getItemName();
```

**Why get data first?** After delete, we lose reference to item data, so we must capture it before deletion for audit log and response.

### 2️⃣ GUARD 1: Item Status Check (CRITICAL!)

```java
// Item must be PENDING (not scheduled, in-progress, or completed)
if (item.status IN [SCHEDULED, IN_PROGRESS, COMPLETED]) {
    throw new ConflictException(
        String.format(
            "Không thể xóa hạng mục đã được đặt lịch hoặc đang thực hiện (Trạng thái: %s). " +
            "Vui lòng hủy lịch hẹn hoặc đánh dấu 'Bỏ qua' (Skip) nếu cần.",
            item.getStatus()
        )
    );
}
```

**Why?** Ngăn chặn xóa item đã linked với appointment → Tránh data inconsistency và orphaned appointment items.

### 3️⃣ GUARD 2: Approval Status Check (CRITICAL!)

```java
// Plan must be DRAFT (not APPROVED or PENDING_REVIEW)
if (plan.approvalStatus IN [APPROVED, PENDING_REVIEW]) {
    throw new ConflictException(
        String.format(
            "Không thể xóa hạng mục khỏi lộ trình đã được duyệt hoặc đang chờ duyệt (Trạng thái: %s). " +
            "Yêu cầu Quản lý 'Từ chối' (Reject) về DRAFT trước khi sửa.",
            plan.getApprovalStatus()
        )
    );
}
```

**Why?** Enforce approval workflow → Chỉ xóa được khi plan ở DRAFT.

### 4️⃣ Update Finances (BEFORE Delete - Critical!)

```java
// Must update finances BEFORE deleting item to avoid lost reference
BigDecimal oldTotalPrice = plan.getTotalPrice();
BigDecimal oldFinalCost = plan.getFinalCost();

plan.setTotalPrice(plan.getTotalPrice().subtract(deletedPrice));
plan.setFinalCost(plan.getFinalCost().subtract(deletedPrice));

planRepository.save(plan);
```

**Assumption**: Discount amount is fixed (percentage or absolute), so both totalPrice and finalCost decrease by the same amount (deletedPrice).

**Important**: This must happen BEFORE `itemRepository.delete(item)` to ensure we have access to item data.

### 5️⃣ Execute Delete

```java
itemRepository.delete(item);
```

**JPA Cascade**: Không cần xóa manual các relationship (nếu có cascade configured).

### 6️⃣ Create Audit Log (AFTER Delete - Using Saved Data)

```sql
INSERT INTO plan_audit_logs (
    plan_id,
    action_type,             -- 'ITEM_DELETED'
    performed_by,            -- Doctor's employee_id
    notes,                   -- "Item 538 (Cạo vôi răng): -500000 VND"
    old_approval_status,     -- 'DRAFT'
    new_approval_status,     -- 'DRAFT' (no change)
    created_at
) VALUES (...);
```

**Standardized Format** (consistent with API 5.10):

```java
String notes = String.format("Item %d (%s): -%.0f VND", itemId, itemName, price);
// Example: "Item 538 (Cạo vôi răng): -500000 VND"
```

### 7️⃣ Approval Status (Keep DRAFT)

```
Plan.approvalStatus REMAINS DRAFT
(No auto-trigger to PENDING_REVIEW)
```

**Why?** Bác sĩ có thể xóa nhiều items liên tiếp, chỉ submit 1 lần cuối cùng.

---

## ✅ Response Body (200 OK)

### JSON Structure (Option B - Full Response)

```json
{
  "message": "Hạng mục đã được xóa thành công.",
  "deletedItemId": 538,
  "deletedItemName": "Cạo vôi răng",
  "priceReduction": 500000,
  "financialImpact": {
    "planTotalCost": 15000000,
    "planFinalCost": 13500000,
    "priceChange": null
  }
}
```

### Response Fields

| Field                           | Type    | Description                                            |
| ------------------------------- | ------- | ------------------------------------------------------ |
| `message`                       | String  | Confirmation message                                   |
| `deletedItemId`                 | Long    | ID của item đã xóa                                     |
| `deletedItemName`               | String  | **Option B**: Tên của item đã xóa (for FE toast)       |
| `priceReduction`                | Decimal | **Option B**: Mức giảm giá (for FE toast: "-500.000đ") |
| `financialImpact`               | Object  | Tác động tài chính lên toàn bộ plan                    |
| `financialImpact.planTotalCost` | Decimal | Tổng chi phí mới của plan (trước discount)             |
| `financialImpact.planFinalCost` | Decimal | Chi phí cuối cùng mới của plan (sau discount)          |
| `financialImpact.priceChange`   | Decimal | Null for delete (priceReduction is used instead)       |

### FE Usage Example

```javascript
// Toast notification with full context
toast.success(
  `Đã xóa '${response.deletedItemName}' (-${formatCurrency(
    response.priceReduction
  )})`
);
// Result: "Đã xóa 'Cạo vôi răng' (-500.000đ)"
```

---

## 🚫 Error Responses

### 404 NOT FOUND - Item Not Found

```json
{
  "timestamp": "2025-11-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Hạng mục không tồn tại",
  "path": "/api/v1/patient-plan-items/999"
}
```

### 409 CONFLICT - Item Already Scheduled

```json
{
  "timestamp": "2025-11-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Không thể xóa hạng mục đã được đặt lịch hoặc đang thực hiện (Trạng thái: SCHEDULED). Vui lòng hủy lịch hẹn hoặc đánh dấu 'Bỏ qua' (Skip) nếu cần.",
  "path": "/api/v1/patient-plan-items/538"
}
```

### 409 CONFLICT - Plan Already Approved

```json
{
  "timestamp": "2025-11-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Không thể xóa hạng mục khỏi lộ trình đã được duyệt hoặc đang chờ duyệt (Trạng thái: APPROVED). Yêu cầu Quản lý 'Từ chối' (Reject) về DRAFT trước khi sửa.",
  "path": "/api/v1/patient-plan-items/538"
}
```

### 403 FORBIDDEN - Missing Permission

```json
{
  "timestamp": "2025-11-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied - UPDATE_TREATMENT_PLAN permission required",
  "path": "/api/v1/patient-plan-items/538"
}
```

---

## 🧪 Testing Guide

### Prerequisites

1. **Database**: Ensure `plan_audit_logs` table exists (Schema V20)
2. **Permissions**: `UPDATE_TREATMENT_PLAN` assigned to `ROLE_DENTIST`
3. **Test Account**: Login as Doctor
4. **Test Data**: Plan with items in DRAFT status

### Test Scenario 1: Delete Item Successfully ✅

**Setup:**

```sql
-- Create item in DRAFT plan
INSERT INTO patient_plan_items (item_id, phase_id, service_id, sequence_number, item_name, status, price, estimated_time_minutes)
VALUES (538, 12, 7, 7, 'Cạo vôi răng', 'PENDING', 500000, 30);

-- Update plan finances (before delete test)
UPDATE patient_treatment_plans
SET total_price = 16000000, final_cost = 14400000, approval_status = 'DRAFT'
WHERE plan_id = 104;
```

**Request:**

```bash
curl -X DELETE http://localhost:8080/api/v1/patient-plan-items/538 \
  -H "Authorization: Bearer <DOCTOR_TOKEN>"
```

**Expected Result:**

- ✅ Status: 200 OK
- ✅ `deletedItemId`: 538
- ✅ `deletedItemName`: "Cạo vôi răng"
- ✅ `priceReduction`: 500000
- ✅ `financialImpact.planTotalCost`: 15500000 (16000000 - 500000)
- ✅ `financialImpact.planFinalCost`: 13900000 (14400000 - 500000)
- ✅ Audit log created with action_type = "ITEM_DELETED"
- ✅ Item deleted from database

**Verification:**

```sql
-- Check item deleted
SELECT * FROM patient_plan_items WHERE item_id = 538;
-- Result: 0 rows (item deleted)

-- Check plan finances
SELECT plan_id, total_price, final_cost, approval_status
FROM patient_treatment_plans
WHERE plan_id = 104;
-- Result: total_price = 15500000, final_cost = 13900000

-- Check audit log
SELECT * FROM plan_audit_logs
WHERE plan_id = 104 AND action_type = 'ITEM_DELETED'
ORDER BY created_at DESC LIMIT 1;
-- Result: notes = "Item 538 (Cạo vôi răng): -500000 VND"
```

---

### Test Scenario 2: Delete Item Already Scheduled ❌

**Setup:**

```sql
-- Set item status to SCHEDULED
UPDATE patient_plan_items
SET status = 'SCHEDULED'
WHERE item_id = 538;
```

**Request:**

```bash
curl -X DELETE http://localhost:8080/api/v1/patient-plan-items/538 \
  -H "Authorization: Bearer <DOCTOR_TOKEN>"
```

**Expected Result:**

- ❌ Status: 409 CONFLICT
- ❌ Message: "Không thể xóa hạng mục đã được đặt lịch hoặc đang thực hiện (Trạng thái: SCHEDULED)..."
- ❌ Item NOT deleted
- ❌ Plan finances NOT changed
- ❌ No audit log created

---

### Test Scenario 3: Delete from Approved Plan ❌

**Setup:**

```sql
-- Set plan to APPROVED
UPDATE patient_treatment_plans
SET approval_status = 'APPROVED'
WHERE plan_id = 104;

-- Set item to PENDING
UPDATE patient_plan_items
SET status = 'PENDING'
WHERE item_id = 538;
```

**Request:**

```bash
curl -X DELETE http://localhost:8080/api/v1/patient-plan-items/538 \
  -H "Authorization: Bearer <DOCTOR_TOKEN>"
```

**Expected Result:**

- ❌ Status: 409 CONFLICT
- ❌ Message: "Không thể xóa hạng mục khỏi lộ trình đã được duyệt... (Trạng thái: APPROVED)"
- ❌ Item NOT deleted

---

### Test Scenario 4: Delete Non-Existent Item ❌

**Request:**

```bash
curl -X DELETE http://localhost:8080/api/v1/patient-plan-items/999999 \
  -H "Authorization: Bearer <DOCTOR_TOKEN>"
```

**Expected Result:**

- ❌ Status: 404 NOT FOUND
- ❌ Message: "Hạng mục không tồn tại"

---

### Test Scenario 5: Delete Multiple Items Sequentially ✅

**Setup:**

```sql
-- Plan with 3 items to delete
INSERT INTO patient_plan_items (item_id, phase_id, service_id, sequence_number, item_name, status, price, estimated_time_minutes)
VALUES
(538, 12, 7, 7, 'Item A', 'PENDING', 500000, 30),
(539, 12, 8, 8, 'Item B', 'PENDING', 300000, 20),
(540, 12, 9, 9, 'Item C', 'PENDING', 200000, 15);

UPDATE patient_treatment_plans
SET total_price = 16000000, final_cost = 14400000, approval_status = 'DRAFT'
WHERE plan_id = 104;
```

**Requests:**

```bash
# Delete Item A
curl -X DELETE http://localhost:8080/api/v1/patient-plan-items/538 -H "Authorization: Bearer <TOKEN>"
# Expected: totalPrice = 15500000

# Delete Item B
curl -X DELETE http://localhost:8080/api/v1/patient-plan-items/539 -H "Authorization: Bearer <TOKEN>"
# Expected: totalPrice = 15200000

# Delete Item C
curl -X DELETE http://localhost:8080/api/v1/patient-plan-items/540 -H "Authorization: Bearer <TOKEN>"
# Expected: totalPrice = 15000000
```

**Expected Result:**

- ✅ All 3 items deleted
- ✅ Plan totalPrice decreased by 1000000 total (500000 + 300000 + 200000)
- ✅ 3 separate audit log entries
- ✅ Plan.approvalStatus still DRAFT

---

## 🔗 Related APIs

| API          | Endpoint                                                                 | Relationship                                                      |
| ------------ | ------------------------------------------------------------------------ | ----------------------------------------------------------------- |
| **API 5.9**  | `PATCH /api/v1/patient-treatment-plans/{planCode}/approval`              | Manager rejects plan → Doctor uses API 5.11 to delete wrong items |
| **API 5.10** | `PATCH /api/v1/patient-plan-items/{itemId}`                              | Update item vs Delete item (different fix strategies)             |
| **API 5.7**  | `POST /api/v1/patient-treatment-plans/{planCode}/phases/{phaseId}/items` | Add items after deleting wrong ones                               |
| **API 3.x**  | Appointment APIs                                                         | Cannot delete item if already scheduled (GUARD 1)                 |

---

## 📊 Database Impact

### Tables Modified

1. **`patient_plan_items`** - Item deleted (CASCADE if configured)
2. **`patient_treatment_plans`** - Financial totals recalculated (totalPrice and finalCost decreased)
3. **`plan_audit_logs`** - Audit record created

### Sample Audit Log Entry

```sql
SELECT * FROM plan_audit_logs WHERE action_type = 'ITEM_DELETED';

| log_id | plan_id | action_type  | performed_by | notes                                   | old_approval_status | new_approval_status | created_at          |
|--------|---------|--------------|--------------|----------------------------------------|---------------------|---------------------|---------------------|
| 43     | 104     | ITEM_DELETED | 5            | Item 538 (Cạo vôi răng): -500000 VND  | DRAFT               | DRAFT               | 2025-11-15 10:30:00 |
```

---

## 📝 Important Notes

### ❌ What API 5.11 Does NOT Do

1. **Does NOT delete appointments linked to item**

   - GUARD 1 prevents deletion if item is SCHEDULED
   - Must cancel appointment first (API 3.x)

2. **Does NOT change approval status**

   - Plan.approvalStatus stays DRAFT
   - Doctor must explicitly submit for review (future API)

3. **Does NOT support batch delete**
   - Must call API multiple times for multiple items
   - Each call creates separate audit log

### ✅ What API 5.11 DOES Do

1. ✅ Delete item permanently from database
2. ✅ Recalculate plan finances automatically (decrease totalPrice and finalCost)
3. ✅ Create audit trail with standardized format
4. ✅ Enforce guards (status checks)
5. ✅ Return full details (Option B) for FE toast notification

---

## 🎯 Key Design Decisions

### Option A: 204 No Content (NOT CHOSEN ❌)

```
DELETE returns 204 No Content (standard REST practice)
- No response body
- FE only knows "success" but no details
```

**Cons**:

- ❌ FE cannot show meaningful toast: "Đã xóa 'Cạo vôi răng' (-500.000đ)"
- ❌ FE must call GET API to refresh plan finances
- ❌ Poor UX (no immediate feedback)

### Option B: 200 OK with Full Response (CHOSEN ✅)

```
DELETE returns 200 OK with response body containing:
- deletedItemId, deletedItemName, priceReduction
- financialImpact (new plan totals)
```

**Pros**:

- ✅ FE can show rich toast notification with item name and price reduction
- ✅ FE gets updated plan totals immediately (no extra GET call)
- ✅ Better UX with transparency
- ✅ Aligns with healthcare domain requirement (financial transparency for doctor)

---

## 🔒 Security Considerations

### Permission Check

```java
@PreAuthorize("hasRole('ADMIN') or hasAuthority('UPDATE_TREATMENT_PLAN')")
```

**Who can delete?**

- ✅ ROLE_DENTIST (has UPDATE_TREATMENT_PLAN)
- ✅ ROLE_MANAGER (has UPDATE_TREATMENT_PLAN + APPROVE_TREATMENT_PLAN)
- ✅ ROLE_ADMIN (superuser)

**Who cannot delete?**

- ❌ ROLE_NURSE
- ❌ ROLE_RECEPTIONIST
- ❌ ROLE_PATIENT

### Audit Trail

Every deletion is logged with:

- Who deleted (performed_by employee_id)
- What was deleted (item ID, name, price)
- When (created_at timestamp)
- Context (plan_id, approval_status)

---

## 🧩 Integration with Approval Workflow

### Workflow Diagram

```
[Manager REJECTS Plan]
         ↓
   Plan → DRAFT
         ↓
[Doctor Reviews Notes]
         ↓
    ┌─────────────┐
    │ Fix Strategy│
    └─────────────┘
         ↓
    ┌────┴────┐
    │         │
[API 5.10]  [API 5.11]
  Update      Delete
   Item        Item
    │           │
    └─────┬─────┘
          ↓
    Plan stays DRAFT
          ↓
  [Submit for Review]
    (Future API)
          ↓
   Plan → PENDING_REVIEW
```

---

## 💡 Best Practices

### For Doctors:

1. **Review before delete**: Double-check item details before calling API
2. **Batch operations**: Delete all wrong items first, then add correct ones
3. **Check audit logs**: Use audit logs to track what was deleted (for accountability)

### For FE Developers:

1. **Confirmation dialog**: Show "Bạn chắc chắn muốn xóa 'Cạo vôi răng'?" before calling API
2. **Toast notification**: Use Option B response to show rich feedback
3. **Refresh UI**: Update plan totals in UI immediately from response (no extra GET)
4. **Error handling**: Handle 409 CONFLICT gracefully (explain why deletion is blocked)

---

**Implementation Date**: 2025-11-15
**Schema Version**: V20
**Status**: ✅ Implemented & Documented
**Option**: B (Full Response with deletedItemName + priceReduction)
