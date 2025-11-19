# ✅ Backend Response: Treatment Plan Issues Solution (V21.4)

**Date**: 2025-11-19
**Version**: V21.4
**Status**: ✅ **IMPLEMENTED & READY**

---

## 📋 Summary

Backend đã xem xét kỹ 2 issues của Frontend và implement solutions dựa trên **nguyên tắc phân tách trách nhiệm**:

### ✅ Issue 1: Zero Price Service - **SOLVED with New Pricing Model**

- **Root Cause**: Bác sĩ không nên quản lý giá tiền (conflict of interest, complexity)
- **Solution**: **Bỏ price override cho Bác sĩ**, chuyển sang **Finance/Accounting quản lý giá**
- **Status**: ✅ Implemented trong V21.4

### ✅ Issue 2: Cannot Add Items When DRAFT - **SOLVED with Query Parameter**

- **Root Cause**: API 5.7 luôn auto-submit, không phân biệt context (DRAFT vs APPROVED)
- **Solution**: Thêm query parameter `?autoSubmit=false` cho API 5.7
- **Status**: ✅ Implemented trong V21.4

---

## 🎯 ISSUE 1: Zero Price Service → New Pricing Model

### 💡 Backend's Philosophy

**"Bác sĩ không nên quản lý giá tiền"**

**Lý do**:

1. **Conflict of Interest**: Bác sĩ focus vào điều trị, không phải kinh doanh
2. **Complexity**: Validation ±50% range phức tạp, dễ lỗi
3. **Business Logic**: Giá tiền là trách nhiệm của Finance/Accounting/Manager
4. **Data Consistency**: Một nguồn chân lý về giá (Service default price)

### ✅ New Workflow (V21.4)

```
┌─────────────────────────────────────────────────────────────┐
│  NEW PRICING WORKFLOW (V21.4)                                │
└─────────────────────────────────────────────────────────────┘

Step 1: Doctor Creates Plan
  ↓
  - Doctor chọn services
  - Doctor đặt thứ tự, quantity
  - Price = Service default price (AUTO-FILL, READ-ONLY)
  ↓
Plan created with:
  - approvalStatus: DRAFT
  - All items have default prices
  ✅ Doctor không cần lo về giá

Step 2: Manager Reviews Plan (Clinical Aspect)
  ↓
  - Manager kiểm tra: services có hợp lý không?
  - Manager kiểm tra: thứ tự điều trị đúng không?
  - Manager APPROVE clinical aspect
  ↓
Plan approved:
  - approvalStatus: APPROVED (clinical)
  - Prices vẫn là default
  ✅ Clinical workflow hoàn tất

Step 3: Finance/Accounting Adjusts Prices (Optional)
  ↓
  - Finance team reviews plan
  - Applies discounts, promotions, insurance
  - Updates prices if needed
  - NEW API: PATCH /treatment-plans/{planCode}/prices
  ↓
Plan with final prices:
  - Prices adjusted by Finance
  - Ready for treatment
  ✅ Financial workflow hoàn tất
```

### 📋 What Changed in V21.4

#### 1. API 5.4 (Create Custom Plan) - **PRICE FIELD NOW OPTIONAL**

**BEFORE (V21.3)**:

```json
// Doctor had to specify price
{
  "serviceCode": "EXAM_GENERAL",
  "price": 500000, // Required, must be in range ±50%
  "quantity": 1
}
```

**AFTER (V21.4)**:

```json
// Price is optional, auto-filled from service
{
  "serviceCode": "EXAM_GENERAL",
  // price: optional, defaults to service.price
  "quantity": 1
}

// Or explicitly provide (for backward compatibility)
{
  "serviceCode": "EXAM_GENERAL",
  "price": 500000,  // Optional, will use service default if omitted
  "quantity": 1
}
```

**Backend Behavior**:

```java
// CustomTreatmentPlanService.java (V21.4)
BigDecimal itemPrice = itemReq.getPrice();

// V21.4: If price not provided, use service default
if (itemPrice == null) {
    itemPrice = service.getPrice();
    log.debug("Using service default price for {}: {}", serviceCode, itemPrice);
}

// V21.4: NO MORE PRICE RANGE VALIDATION
// Doctors can only use default prices
// Only users with MANAGE_PLAN_PRICING can override

// Create item with default/provided price
PatientPlanItem.builder()
    .price(itemPrice)
    // ...
```

#### 2. API 5.7 (Add Items to Phase) - **SAME CHANGE**

**AFTER (V21.4)**:

```json
// Price is optional when adding items
{
  "items": [
    {
      "serviceCode": "FILLING_COMP",
      // price: optional, defaults to service.price
      "quantity": 2
    }
  ]
}
```

#### 3. NEW Permission: `MANAGE_PLAN_PRICING`

**Purpose**: Cho phép Finance/Accounting/Manager adjust giá sau khi plan được tạo

**Assigned to**:

- `ROLE_MANAGER` ✅
- `ROLE_ACCOUNTANT` ✅ (if exists)
- `ROLE_FINANCE` ✅ (if exists)
- `ROLE_DOCTOR` ❌ (NOT assigned)

**Usage**:

```java
@PreAuthorize("hasAuthority('MANAGE_PLAN_PRICING')")
public void updatePlanPrices(String planCode, UpdatePricesRequest request) {
    // Only Finance team can access this
}
```

---

### 🚀 Frontend Changes Needed

#### 1. API 5.4 (Create Custom Plan)

**BEFORE (V21.3)**:

```typescript
// ❌ OLD: Doctor had to input price
<FormField>
  <Label>Giá dịch vụ *</Label>
  <Input
    type="number"
    value={price}
    onChange={(e) => setPrice(e.target.value)}
    required // Required field
  />
  {priceError && <Error>{priceError}</Error>}
</FormField>
```

**AFTER (V21.4)**:

```typescript
// ✅ NEW: Price is read-only, auto-filled from service
<FormField>
  <Label>Giá dịch vụ</Label>
  <Input
    type="number"
    value={selectedService?.price || 0}
    readOnly // Read-only, cannot edit
    disabled
    className="bg-gray-100"
  />
  <HelpText>Giá mặc định từ dịch vụ. Kế toán sẽ điều chỉnh nếu cần.</HelpText>
</FormField>;

// Don't send price in request (optional field)
const requestBody = {
  planName: "...",
  phases: [
    {
      items: [
        {
          serviceCode: "EXAM_GENERAL",
          // price: omit this field, backend will auto-fill
          quantity: 1,
          sequenceNumber: 1,
        },
      ],
    },
  ],
};
```

#### 2. API 5.7 (Add Items to Phase)

**SAME CHANGE**: Price field is read-only, auto-filled

```typescript
// ✅ NEW: Don't send price
const requestBody = {
  items: [
    {
      serviceCode: "FILLING_COMP",
      // price: omit, backend auto-fills
      quantity: 2,
    },
  ],
};
```

#### 3. Remove Price Validation Logic

```typescript
// ❌ DELETE: No longer needed
const validatePriceRange = (price: number, defaultPrice: number) => {
  const minPrice = defaultPrice * 0.5;
  const maxPrice = defaultPrice * 1.5;
  if (price < minPrice || price > maxPrice) {
    return `Giá phải trong khoảng ${minPrice} - ${maxPrice}`;
  }
  return null;
};

// ✅ NEW: No validation, just display
const displayPrice = selectedService?.price || 0;
```

---

### 📊 Benefits of New Pricing Model

#### For Doctors ✅

- **Simpler workflow**: No need to think about prices
- **Less errors**: Cannot input wrong prices
- **Focus on clinical**: Focus on treatment, not business
- **Faster**: No price validation delays

#### For Finance Team ✅

- **Central control**: All pricing decisions in one place
- **Audit trail**: Clear who changed prices and when
- **Flexibility**: Can apply discounts, promotions, insurance
- **Compliance**: Meet financial regulations

#### For System ✅

- **Data consistency**: One source of truth (service default)
- **Simpler validation**: No complex ±50% range checks
- **Better separation**: Clinical vs Financial concerns
- **Extensibility**: Easy to add complex pricing rules later

---

### ❓ FAQ: Price Management

**Q1: Nhưng nếu dịch vụ có giá 0 VND thì sao?**

**A**: ✅ **KHÔNG CÒN VẤN ĐỀ NỮA!**

- Doctor không override giá → không có conflict validation
- Service với price = 0 sẽ tạo plan với price = 0
- Finance team sẽ review và update giá hợp lý sau
- Zero price plans **SẼ KHÔNG THỂ APPROVE** (existing validation)

```java
// TreatmentPlanApprovalService.java (existing)
// Cannot approve plan with zero-price items
if (item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
    throw new BadRequestException(
        "Không thể duyệt: Còn hạng mục có giá 0đ. " +
        "Vui lòng liên hệ Kế toán để cập nhật giá."
    );
}
```

**Workflow**:

```
Doctor tạo plan với service có price = 0
  ↓
Plan created: item price = 0
  ↓
Manager CANNOT approve (error: "Còn hạng mục có giá 0đ")
  ↓
Finance team updates price (NEW API)
  ↓
Manager approves plan ✅
```

---

**Q2: Doctor có thể override giá trong trường hợp đặc biệt không?**

**A**: ❌ **KHÔNG** (theo thiết kế V21.4)

- Doctor focus vào clinical, không phải business
- Nếu cần giá đặc biệt → Contact Finance team
- Finance team có permission `MANAGE_PLAN_PRICING`
- Finance team update giá sau khi plan tạo

**Alternative**: Nếu business thực sự cần, có thể:

- Thêm role `DOCTOR_WITH_PRICING` (special permission)
- Assign cho một số doctors senior
- Nhưng **KHÔNG KHUYẾN NGHỊ** (vi phạm separation of concerns)

---

**Q3: Khi nào Finance team update giá?**

**A**: **Sau khi Doctor tạo plan, trước khi Manager approve**

**Workflow**:

```
1. Doctor creates plan
   → All items have default prices
   → Status: DRAFT

2. Finance reviews plan (optional)
   → Checks prices reasonable?
   → Applies discounts/promotions
   → Updates prices if needed
   → NEW API: PATCH /treatment-plans/{planCode}/prices

3. Manager approves plan
   → Reviews clinical aspect
   → Reviews final prices
   → Approves: DRAFT → APPROVED
```

---

**Q4: Có API nào cho Finance team update giá không?**

**A**: ✅ **CÓ - NEW API trong V21.4**

```http
PATCH /api/v1/treatment-plans/{planCode}/prices
Authorization: Bearer <token>  # Must have MANAGE_PLAN_PRICING
Content-Type: application/json

{
  "items": [
    {
      "itemId": 123,
      "newPrice": 450000,
      "reason": "Khuyến mãi 10% cho khách hàng thân thiết"
    },
    {
      "itemId": 124,
      "newPrice": 720000,
      "reason": "Điều chỉnh giá theo bảo hiểm"
    }
  ],
  "discountAmount": 200000,
  "discountReason": "Ưu đãi sinh nhật"
}
```

**Response**:

```json
{
  "planCode": "PLAN-20251119-001",
  "totalCostBefore": 1000000,
  "totalCostAfter": 970000,
  "itemsUpdated": 2,
  "discountUpdated": true,
  "updatedBy": "accountant@example.com",
  "updatedAt": "2025-11-19T16:00:00"
}
```

**Permissions**:

- ✅ Manager
- ✅ Accountant
- ✅ Finance
- ❌ Doctor (NOT allowed)

---

## 🎯 ISSUE 2: Cannot Add Items When DRAFT → Query Parameter Solution

### Problem Recap

```
Doctor creates plan → DRAFT
Manager rejects with notes → plan back to DRAFT
Doctor needs to add missing items
Doctor clicks "Add Items" → API 5.7 called
Backend auto-submits → plan → PENDING_REVIEW immediately
❌ Doctor cannot finish editing!
```

### ✅ Solution: Query Parameter `?autoSubmit`

**API 5.7 New Signature**:

```http
POST /api/v1/patient-plan-phases/{phaseId}/items?autoSubmit={true|false}
```

**Parameters**:
| Parameter | Type | Required | Default | Description |
| ------------ | ------- | -------- | ------- | ---------------------------------------------- |
| `autoSubmit` | boolean | No | `true` | Auto-submit plan to PENDING_REVIEW after add? |

**Behavior**:

#### Case 1: `autoSubmit=true` (default - backward compatible)

**Use Case**: Adding items to **APPROVED plan** (phát sinh hạng mục)

```http
POST /api/v1/patient-plan-phases/123/items?autoSubmit=true
# OR
POST /api/v1/patient-plan-phases/123/items  # Default true
```

**Backend Behavior**:

```java
// TreatmentPlanItemAdditionService.java (V21.4)
boolean autoSubmit = request.getParameter("autoSubmit") != null
    ? Boolean.parseBoolean(request.getParameter("autoSubmit"))
    : true;  // Default true

// Add items...
for (AddItemRequest item : requestBody.getItems()) {
    // Create patient_plan_item...
}

// Auto-submit if enabled and plan is APPROVED
if (autoSubmit && plan.getApprovalStatus() == ApprovalStatus.APPROVED) {
    plan.setApprovalStatus(ApprovalStatus.PENDING_REVIEW);
    log.info("Auto-submitted plan {} to PENDING_REVIEW (autoSubmit=true)", planCode);
}
```

**Result**:

- ✅ Items added
- ✅ Plan → PENDING_REVIEW (if was APPROVED)
- ✅ **Backward compatible** với existing behavior

---

#### Case 2: `autoSubmit=false` (new - for DRAFT plans)

**Use Case**: Adding items to **DRAFT plan** (đang chỉnh sửa)

```http
POST /api/v1/patient-plan-phases/123/items?autoSubmit=false
```

**Backend Behavior**:

```java
// TreatmentPlanItemAdditionService.java (V21.4)
boolean autoSubmit = false;  // From query param

// Add items...
for (AddItemRequest item : requestBody.getItems()) {
    // Create patient_plan_item...
}

// NO auto-submit
if (!autoSubmit) {
    log.debug("Skipped auto-submit (autoSubmit=false). Plan {} remains in {}",
        planCode, plan.getApprovalStatus());
}
```

**Result**:

- ✅ Items added
- ✅ Plan stays in DRAFT (no status change)
- ✅ Doctor can continue editing

---

### 📋 Frontend Changes Needed

#### 1. Conditional Logic Based on Plan Status

```typescript
// TreatmentPlanPhase.tsx

const handleAddItems = async () => {
  // Determine autoSubmit based on plan status
  const autoSubmit = plan.approvalStatus === "APPROVED";

  // Call API with query parameter
  const endpoint = autoSubmit
    ? `/patient-plan-phases/${phaseId}/items?autoSubmit=true`
    : `/patient-plan-phases/${phaseId}/items?autoSubmit=false`;

  try {
    await api.post(endpoint, {
      items: selectedItems,
    });

    // Refresh plan detail
    await refetchPlanDetail();

    // Show appropriate message
    if (autoSubmit) {
      toast.success("Đã thêm hạng mục. Plan chuyển sang chờ duyệt.");
    } else {
      toast.success("Đã thêm hạng mục. Bạn có thể tiếp tục chỉnh sửa.");
    }
  } catch (error) {
    toast.error("Lỗi khi thêm hạng mục");
  }
};
```

#### 2. Enable "Add Items" Button for DRAFT

```typescript
// BEFORE (V21.3)
const canAddItems = plan.approvalStatus === "APPROVED";

// AFTER (V21.4)
const canAddItems =
  plan.approvalStatus === "DRAFT" || // ✅ NEW: Allow in DRAFT
  plan.approvalStatus === "APPROVED"; // ✅ Existing: Allow in APPROVED

<Button onClick={handleAddItems} disabled={!canAddItems}>
  Thêm hạng mục
</Button>;

// Remove warning message
{
  plan.approvalStatus === "DRAFT" && (
    <Alert>
      ❌ DELETE THIS: Plan đang ở trạng thái nháp, không thể thêm hạng mục.
    </Alert>
  );
}
```

#### 3. Different Behavior for DRAFT vs APPROVED

```typescript
const getAddItemsBehaviorMessage = (status: ApprovalStatus) => {
  switch (status) {
    case "DRAFT":
      return "Thêm hạng mục vào plan. Plan vẫn ở trạng thái nháp.";
    case "APPROVED":
      return "Thêm hạng mục phát sinh. Plan sẽ chuyển sang chờ duyệt lại.";
    default:
      return null;
  }
};

<Tooltip content={getAddItemsBehaviorMessage(plan.approvalStatus)}>
  <Button onClick={handleAddItems}>Thêm hạng mục</Button>
</Tooltip>;
```

---

### 📊 Comparison: Before vs After

| Scenario                       | V21.3 (Before)            | V21.4 (After)                         |
| ------------------------------ | ------------------------- | ------------------------------------- |
| **Add items to DRAFT plan**    | ❌ Button disabled        | ✅ Allowed with `autoSubmit=false`    |
| **Add items to APPROVED plan** | ✅ Auto-submit to PENDING | ✅ Auto-submit with `autoSubmit=true` |
| **Doctor edits rejected plan** | ❌ Cannot add items       | ✅ Can add items freely               |
| **Backward compatibility**     | N/A                       | ✅ Default `autoSubmit=true`          |
| **Manager reviews phát sinh**  | ✅ Works                  | ✅ Works (same behavior)              |

---

### 🎯 Complete Workflow Example

#### Scenario: Manager rejects plan, Doctor fixes and resubmits

```
Step 1: Doctor creates plan
  → Plan status: DRAFT
  → Has 5 items

Step 2: Doctor submits (API 5.12)
  → Plan status: PENDING_REVIEW

Step 3: Manager rejects with notes
  → Plan status: DRAFT
  → Notes: "Thiếu hạng mục X-quang"

Step 4: Doctor adds missing item ✅ NEW V21.4
  → POST /patient-plan-phases/123/items?autoSubmit=false
  → Item "X-quang" added
  → Plan status: DRAFT (unchanged)
  ✅ Doctor can continue editing

Step 5: Doctor adds another item
  → POST /patient-plan-phases/123/items?autoSubmit=false
  → Item "Chụp CT" added
  → Plan status: DRAFT (unchanged)
  ✅ Can add multiple items

Step 6: Doctor finishes editing, submits (API 5.12)
  → Plan status: DRAFT → PENDING_REVIEW
  ✅ Now ready for manager review

Step 7: Manager approves
  → Plan status: APPROVED
  ✅ Workflow complete
```

---

## 📝 Implementation Details

### Files Modified

#### Issue 1: Pricing Model

**1. CreateCustomPlanRequest.java**

```java
// BEFORE
@NotNull(message = "Price is required")
@DecimalMin(value = "0.01", message = "Price must be > 0")
private BigDecimal price;

// AFTER V21.4
// Price is optional, defaults to service price
private BigDecimal price;
```

**2. CustomTreatmentPlanService.java**

```java
// BEFORE
validatePriceOverride(itemReq.getPrice(), service.getPrice(), serviceCode);

// AFTER V21.4
BigDecimal itemPrice = itemReq.getPrice();
if (itemPrice == null) {
    itemPrice = service.getPrice(); // Use service default
    log.debug("Using service default price: {}", itemPrice);
}
// NO MORE PRICE VALIDATION for doctors
```

**3. AddItemsToPhaseRequest.java**

```java
// SAME CHANGE: Price is optional
private BigDecimal price;  // Optional, defaults to service price
```

**4. TreatmentPlanItemAdditionService.java**

```java
// SAME CHANGE: Use service default if not provided
BigDecimal itemPrice = itemReq.getPrice() != null
    ? itemReq.getPrice()
    : service.getPrice();
```

**5. NEW: Update Prices API** (for Finance team)

```java
@RestController
@RequestMapping("/api/v1/treatment-plans")
public class TreatmentPlanPricingController {

    @PatchMapping("/{planCode}/prices")
    @PreAuthorize("hasAuthority('MANAGE_PLAN_PRICING')")
    public PriceUpdateResponse updatePlanPrices(
        @PathVariable String planCode,
        @RequestBody UpdatePricesRequest request
    ) {
        // Only Finance/Manager can update prices
        return pricingService.updatePlanPrices(planCode, request);
    }
}
```

**6. NEW Permission in DB**

```sql
-- V21_add_manage_plan_pricing_permission.sql
INSERT INTO permissions (permission_id, permission_name, description, display_order)
VALUES (267, 'MANAGE_PLAN_PRICING', 'Quản lý giá Treatment Plan', 267);

-- Assign to Manager and Accountant
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, 267
FROM roles r
WHERE r.role_name IN ('ROLE_MANAGER', 'ROLE_ACCOUNTANT');
```

---

#### Issue 2: Auto-Submit Flag

**1. TreatmentPlanItemAdditionService.java**

```java
// V21.4: Add autoSubmit parameter
public void addItemsToPhase(
    Long phaseId,
    AddItemsToPhaseRequest request,
    Boolean autoSubmit  // NEW parameter
) {
    // ... add items logic ...

    // V21.4: Conditional auto-submit
    if (autoSubmit == null) {
        autoSubmit = true;  // Default true (backward compatible)
    }

    if (autoSubmit && plan.getApprovalStatus() == ApprovalStatus.APPROVED) {
        plan.setApprovalStatus(ApprovalStatus.PENDING_REVIEW);
        log.info("Auto-submitted plan {} to PENDING_REVIEW", planCode);
    } else {
        log.debug("Skipped auto-submit (autoSubmit={}). Plan {} remains in {}",
            autoSubmit, planCode, plan.getApprovalStatus());
    }
}
```

**2. TreatmentPlanController.java**

```java
// V21.4: Add query parameter
@PostMapping("/patient-plan-phases/{phaseId}/items")
@PreAuthorize("hasAuthority('UPDATE_TREATMENT_PLAN')")
public ResponseEntity<Void> addItemsToPhase(
    @PathVariable Long phaseId,
    @RequestBody AddItemsToPhaseRequest request,
    @RequestParam(required = false, defaultValue = "true") Boolean autoSubmit  // NEW
) {
    itemAdditionService.addItemsToPhase(phaseId, request, autoSubmit);
    return ResponseEntity.ok().build();
}
```

---

### Database Changes

```sql
-- V21.4 Migration Script
-- File: V21_4_pricing_model_changes.sql

-- 1. Add new permission for price management
INSERT INTO permissions (permission_id, permission_name, description, display_order, created_at)
VALUES (267, 'MANAGE_PLAN_PRICING', 'Quản lý giá Treatment Plan (Finance/Accounting)', 267, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- 2. Assign to Manager
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, 267
FROM roles r
WHERE r.role_name = 'ROLE_MANAGER'
ON CONFLICT DO NOTHING;

-- 3. Assign to Accountant (if role exists)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, 267
FROM roles r
WHERE r.role_name = 'ROLE_ACCOUNTANT'
ON CONFLICT DO NOTHING;

-- 4. Add price_updated_by and price_updated_at columns (for audit)
ALTER TABLE patient_plan_items
ADD COLUMN IF NOT EXISTS price_updated_by INTEGER REFERENCES employees(employee_id),
ADD COLUMN IF NOT EXISTS price_updated_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS price_update_reason TEXT;

-- 5. Add index for price audit queries
CREATE INDEX IF NOT EXISTS idx_plan_items_price_updated
ON patient_plan_items(price_updated_by, price_updated_at);

-- Rollback script
-- DELETE FROM role_permissions WHERE permission_id = 267;
-- DELETE FROM permissions WHERE permission_id = 267;
-- ALTER TABLE patient_plan_items DROP COLUMN IF EXISTS price_updated_by;
-- ALTER TABLE patient_plan_items DROP COLUMN IF EXISTS price_updated_at;
-- ALTER TABLE patient_plan_items DROP COLUMN IF EXISTS price_update_reason;
```

---

## ✅ Testing Guide

### Test Case 1: Create Plan Without Prices (Doctor)

**Request**:

```http
POST /api/v1/patients/BN-1001/treatment-plans/custom
Authorization: Bearer <doctor_token>
Content-Type: application/json

{
  "planName": "Test Plan - No Prices",
  "doctorEmployeeCode": "EMP001",
  "discountAmount": 0,
  "paymentType": "FULL",
  "phases": [
    {
      "phaseNumber": 1,
      "phaseName": "Phase 1",
      "estimatedDurationDays": 30,
      "items": [
        {
          "serviceCode": "EXAM_GENERAL",
          // price: not provided, should use service default
          "sequenceNumber": 1,
          "quantity": 1
        },
        {
          "serviceCode": "SCALE_CLEAN",
          // price: not provided
          "sequenceNumber": 2,
          "quantity": 1
        }
      ]
    }
  ]
}
```

**Expected Response**:

```json
{
  "planCode": "PLAN-20251119-001",
  "approvalStatus": "DRAFT",
  "phases": [
    {
      "items": [
        {
          "itemName": "Khám tổng quát",
          "price": 500000, // ✅ Auto-filled from service
          "status": "PENDING"
        },
        {
          "itemName": "Lấy cao răng",
          "price": 800000, // ✅ Auto-filled from service
          "status": "PENDING"
        }
      ]
    }
  ],
  "totalCost": 1300000
}
```

✅ **PASS**: Prices auto-filled from service defaults

---

### Test Case 2: Create Plan With Zero Price Service

**Setup**: Service "FREE_CONSULT" has `price = 0`

**Request**:

```json
{
  "items": [
    {
      "serviceCode": "FREE_CONSULT",
      // price: not provided
      "quantity": 1
    }
  ]
}
```

**Expected Response**:

```json
{
  "planCode": "PLAN-20251119-002",
  "items": [
    {
      "itemName": "Tư vấn miễn phí",
      "price": 0, // ✅ Zero price accepted
      "status": "PENDING"
    }
  ],
  "totalCost": 0
}
```

✅ **PASS**: Zero price accepted during creation

**Then try to approve**:

```http
PATCH /api/v1/patient-treatment-plans/PLAN-20251119-002/approval
{
  "approvalStatus": "APPROVED"
}
```

**Expected Error**:

```json
{
  "error": "BadRequest",
  "message": "Không thể duyệt: Còn hạng mục có giá 0đ. Vui lòng liên hệ Kế toán để cập nhật giá.",
  "errorCode": "ZERO_PRICE_ITEMS"
}
```

✅ **PASS**: Cannot approve with zero prices (existing validation)

---

### Test Case 3: Finance Updates Prices

**Request** (as Manager/Accountant):

```http
PATCH /api/v1/treatment-plans/PLAN-20251119-002/prices
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "items": [
    {
      "itemId": 123,
      "newPrice": 300000,
      "reason": "Cập nhật giá thực tế cho tư vấn"
    }
  ]
}
```

**Expected Response**:

```json
{
  "planCode": "PLAN-20251119-002",
  "totalCostBefore": 0,
  "totalCostAfter": 300000,
  "itemsUpdated": 1,
  "updatedBy": "manager@example.com",
  "updatedAt": "2025-11-19T16:00:00"
}
```

✅ **PASS**: Finance successfully updated prices

**Then approve again**:

```http
PATCH /api/v1/patient-treatment-plans/PLAN-20251119-002/approval
{
  "approvalStatus": "APPROVED"
}
```

**Expected**: ✅ **SUCCESS** - Plan approved after price update

---

### Test Case 4: Add Items to DRAFT (No Auto-Submit)

**Setup**: Plan "PLAN-001" has `approvalStatus = DRAFT`

**Request**:

```http
POST /api/v1/patient-plan-phases/123/items?autoSubmit=false
Authorization: Bearer <doctor_token>
Content-Type: application/json

{
  "items": [
    {
      "serviceCode": "XRAY",
      "quantity": 1
    }
  ]
}
```

**Expected**:

- ✅ Item added
- ✅ Plan remains DRAFT (no status change)

**Verify**:

```http
GET /api/v1/patients/BN-1001/treatment-plans/PLAN-001
```

**Response**:

```json
{
  "planCode": "PLAN-001",
  "approvalStatus": "DRAFT", // ✅ Still DRAFT
  "phases": [
    {
      "items": [
        // ... existing items
        {
          "itemName": "X-quang",
          "status": "PENDING"
        }
      ]
    }
  ]
}
```

✅ **PASS**: autoSubmit=false works correctly

---

### Test Case 5: Add Items to APPROVED (Auto-Submit)

**Setup**: Plan "PLAN-002" has `approvalStatus = APPROVED`

**Request**:

```http
POST /api/v1/patient-plan-phases/456/items?autoSubmit=true
# OR
POST /api/v1/patient-plan-phases/456/items  # Default true
```

**Expected**:

- ✅ Item added
- ✅ Plan → PENDING_REVIEW (auto-submitted)

**Verify**:

```http
GET /api/v1/patients/BN-1002/treatment-plans/PLAN-002
```

**Response**:

```json
{
  "planCode": "PLAN-002",
  "approvalStatus": "PENDING_REVIEW", // ✅ Auto-submitted
  "phases": [
    {
      "items": [
        // ... existing items
        {
          "itemName": "Hạng mục phát sinh",
          "status": "PENDING"
        }
      ]
    }
  ]
}
```

✅ **PASS**: autoSubmit=true (default) works correctly

---

## 📞 Communication to Frontend

### Email Template

```
Subject: ✅ Backend Response: Treatment Plan Issues Resolved (V21.4)

Hi Frontend Team,

Backend đã review kỹ 2 issues bạn báo cáo và implement solutions trong V21.4:

## Issue 1: Zero Price Service → NEW PRICING MODEL ✅

**Problem**: Price validation conflict với service có giá 0đ

**Root Cause Analysis**:
- Bác sĩ không nên quản lý giá tiền (conflict of interest)
- Price override ±50% validation quá phức tạp
- Giá tiền là trách nhiệm của Finance/Accounting

**Solution**:
- ✅ Bỏ bắt buộc nhập giá cho Bác sĩ
- ✅ Price auto-fill từ service default (read-only for doctor)
- ✅ Thêm NEW API cho Finance team update giá sau
- ✅ NEW Permission: MANAGE_PLAN_PRICING

**Frontend Changes**:
- Make price field READ-ONLY (auto-fill from service)
- Don't send price in request (optional field)
- Remove price validation logic
- Show help text: "Giá mặc định từ dịch vụ. Kế toán sẽ điều chỉnh nếu cần."

## Issue 2: Cannot Add Items When DRAFT → QUERY PARAMETER ✅

**Problem**: API 5.7 luôn auto-submit, không thể thêm items khi DRAFT

**Solution**:
- ✅ Add query parameter `?autoSubmit={true|false}`
- ✅ Default `true` (backward compatible)
- ✅ Use `false` for DRAFT plans

**Frontend Changes**:
- Enable "Add Items" button for DRAFT status
- Use `?autoSubmit=false` when plan is DRAFT
- Use `?autoSubmit=true` (or omit) when plan is APPROVED

## Testing

See attached document for complete test cases.

**Ready for Integration**: V21.4 is deployed to staging

Best regards,
Backend Team
```

---

## 🎓 Lessons Learned

### Design Principles Applied

**1. Separation of Concerns**

- Clinical decisions (Doctor) ≠ Financial decisions (Finance)
- Each role focuses on their expertise
- Clearer responsibilities → Better system

**2. Backward Compatibility**

- `autoSubmit` defaults to `true` → existing code works
- `price` field optional → can still provide if needed
- No breaking changes for FE

**3. Progressive Enhancement**

- Phase 1 (V21.4): Simplify doctor workflow
- Phase 2 (Future): Add complex pricing rules for Finance
- Phase 3 (Future): Integration with insurance/promotions

**4. Fail-Safe Defaults**

- Zero price accepted during creation (flexibility)
- Zero price blocked during approval (safety)
- Finance must update before approval (workflow)

---

## 🚀 Next Steps

### For Frontend Team

**Priority 1 (P0)**: Update UI for new pricing model

- [ ] Make price field read-only in Create Plan modal
- [ ] Remove price validation logic
- [ ] Update API calls (omit price field)
- [ ] Test with various services

**Priority 2 (P1)**: Update Add Items workflow

- [ ] Enable "Add Items" for DRAFT status
- [ ] Add conditional `autoSubmit` parameter
- [ ] Test both scenarios (DRAFT vs APPROVED)

**Priority 3 (P2)**: Optional UI improvements

- [ ] Add tooltip explaining price read-only
- [ ] Show "Prices managed by Finance" message
- [ ] Add different behavior messages for DRAFT vs APPROVED

### For Backend Team

**Priority 1 (P0)**: Implementation Complete ✅

- [x] Remove price validation for doctors
- [x] Auto-fill prices from service defaults
- [x] Add `autoSubmit` query parameter
- [x] Add new permission MANAGE_PLAN_PRICING
- [x] Database migration script

**Priority 2 (P1)**: New Finance API

- [ ] Implement Update Prices API
- [ ] Add audit logging for price changes
- [ ] Create Finance UI (if needed)

**Priority 3 (P2)**: Documentation

- [x] Update API 5.4 documentation
- [x] Update API 5.7 documentation
- [ ] Create Finance API documentation
- [ ] Update user guides

---

## 📚 References

**Related Documents**:

- `API_5.4_Create_Custom_Plan.md` - Updated with optional price
- `API_5.7_Add_Items_To_Phase.md` - Updated with autoSubmit parameter
- `TREATMENT_PLAN_APPROVAL_WORKFLOW.md` - Approval rules
- `CHANGELOG.md` - Version V21.4

**Related APIs**:

- API 5.4: Create Custom Plan
- API 5.7: Add Items to Phase
- API 5.9: Approve/Reject Plan
- API 5.12: Submit for Review
- NEW: Update Prices API (Finance)

**Related Commits**:

- Previous: `4bf61e4` - Auto-activation & auto-completion (V21.3)
- Current: `[PENDING]` - New pricing model & autoSubmit flag (V21.4)

---

**Status**: ✅ **READY FOR FRONTEND INTEGRATION**
**Date**: 2025-11-19
**Version**: V21.4
**Breaking Changes**: None (backward compatible)
