# 📘 Frontend Implementation Guide V21.4 & V21.5

**Release Date**: January 2025
**Backend APIs**: 5.12, 5.13, 5.14
**Core Changes**: Optional Price Field, Auto-Submit Control, Finance Pricing, Drag-Drop Reorder

---

## 🎯 Executive Summary

### V21.4: Simplified Pricing Workflow

- **Price field is now OPTIONAL** when creating/editing treatment plans
- **Auto-fill from service default** - doctors no longer manage prices
- **Finance team controls pricing** via new API 5.13
- **Auto-submit control** via `autoSubmit` query parameter

### V21.5: Enhanced UX

- **Drag-drop reordering** for treatment plan items (API 5.14)
- **Concurrent edit protection** with user-friendly error messages
- **Submit for Review** workflow (API 5.12)

---

## 📋 Table of Contents

1. [Breaking Changes](#breaking-changes)
2. [API 5.12: Submit for Review](#api-512-submit-for-review)
3. [API 5.13: Finance Price Management](#api-513-finance-price-management)
4. [API 5.14: Drag-Drop Reordering](#api-514-drag-drop-reordering)
5. [V21.4: Optional Price Field](#v214-optional-price-field)
6. [V21.4: Auto-Submit Control](#v214-auto-submit-control)
7. [TypeScript Examples](#typescript-examples)
8. [Testing Guide](#testing-guide)

---

## ⚠️ Breaking Changes

### 1. Price Field is Now Optional

**Before V21.4:**

```typescript
// Price was REQUIRED
interface AddItemToPhaseRequest {
  serviceCode: string;
  price: number; // ❌ Required, validated ±50%
  quantity?: number;
  notes?: string;
}
```

**After V21.4:**

```typescript
// Price is OPTIONAL
interface AddItemToPhaseRequest {
  serviceCode: string;
  price?: number; // ✅ Optional, auto-fills from service
  quantity?: number;
  notes?: string;
}
```

**Migration Steps:**

1. **Remove price validation** from treatment plan creation forms
2. **Make price field read-only** (doctors cannot edit)
3. **Display auto-filled price** with indicator (e.g., "Giá từ dịch vụ: 500,000 VND")
4. **Remove ±50% validation** error messages
5. **Update TypeScript interfaces** to reflect optional price

### 2. Auto-Submit Behavior Changed

**Before V21.4:**

- Adding items to APPROVED plan → Auto-reverts to PENDING_REVIEW (always)

**After V21.4:**

- Adding items to APPROVED plan → Configurable via `autoSubmit` parameter
  - `autoSubmit=true` (default): Reverts to PENDING_REVIEW (backward compatible)
  - `autoSubmit=false`: Keeps APPROVED status (for DRAFT editing)

**Migration Steps:**

1. **Add query parameter support** to API 5.7 calls
2. **Implement DRAFT editing UI** with `autoSubmit=false`
3. **Show confirmation dialog** when editing APPROVED plans with auto-submit

---

## 🚀 API 5.12: Submit for Review

### Overview

Allows **Doctor** to submit DRAFT treatment plan for Manager approval.

### Endpoint

```http
PATCH /api/v1/patient-treatment-plans/{planCode}/submit
```

### Authentication

```http
Authorization: Bearer <JWT_TOKEN>
```

### Required Permission

- `UPDATE_TREATMENT_PLAN` (Doctor, Manager)

### Business Rules

- ✅ Only DRAFT plans can be submitted
- ✅ Plan must have at least one item
- ✅ Changes status: DRAFT → PENDING_REVIEW
- ❌ Cannot submit PENDING_REVIEW, APPROVED, REJECTED plans

### Request Example

```typescript
// No request body needed
const response = await fetch(
  `${API_BASE}/patient-treatment-plans/PLAN-001/submit`,
  {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  }
);
```

### Response Example (200 OK)

```json
{
  "planCode": "PLAN-001",
  "planName": "Điều trị chỉnh nha",
  "patientName": "Nguyễn Văn A",
  "previousStatus": "DRAFT",
  "newStatus": "PENDING_REVIEW",
  "submittedBy": "Dr. Nguyễn Văn B",
  "submittedAt": "2025-01-15T10:30:00",
  "message": "Treatment plan has been submitted for review"
}
```

### Error Codes

| Code | Message                  | Solution                                |
| ---- | ------------------------ | --------------------------------------- |
| 404  | Treatment plan not found | Check `planCode`                        |
| 409  | Plan not in DRAFT status | Cannot submit non-DRAFT plans           |
| 409  | Plan has no items        | Add at least one item before submitting |

### Frontend Implementation

#### React Example

```typescript
const submitPlanForReview = async (planCode: string) => {
  try {
    setLoading(true);

    const response = await api.patch(
      `/patient-treatment-plans/${planCode}/submit`
    );

    // Success handling
    toast.success(
      `Lộ trình điều trị đã được gửi để phê duyệt!
       Trạng thái: ${response.data.newStatus}`
    );

    // Refresh plan data
    await fetchPlanDetails(planCode);

    // Navigate to pending review list
    router.push("/treatment-plans?status=PENDING_REVIEW");
  } catch (error) {
    if (error.response?.status === 409) {
      toast.error(error.response.data.message);
    } else {
      toast.error("Không thể gửi phê duyệt. Vui lòng thử lại.");
    }
  } finally {
    setLoading(false);
  }
};
```

#### UI Considerations

1. **Show submit button** only for DRAFT plans
2. **Disable button** if plan has no items
3. **Confirmation dialog** before submitting
4. **Success feedback** with status indicator
5. **Navigate to review list** after submission

---

## 💰 API 5.13: Finance Price Management

### Overview

Allows **Finance/Accountant** to adjust treatment plan item prices (Manager can also access).

### Endpoint

```http
PATCH /api/v1/patient-treatment-plans/{planCode}/prices
```

### Authentication & Permission

```http
Authorization: Bearer <JWT_TOKEN>
X-Required-Permission: MANAGE_PLAN_PRICING
```

**Allowed Roles:**

- ✅ ROLE_MANAGER
- ✅ ROLE_ACCOUNTANT
- ❌ ROLE_DOCTOR (cannot access)

### Business Rules

- ✅ Can update prices for NOT_STARTED, IN_PROGRESS, APPROVED plans
- ✅ Automatically recalculates `total_price`, `final_cost`
- ✅ Audit trail: `price_updated_by`, `price_updated_at`, `price_update_reason`
- ❌ Cannot update COMPLETED or CANCELLED plans

### Request Body

```typescript
interface UpdatePricesRequest {
  items: ItemPriceUpdate[];
}

interface ItemPriceUpdate {
  itemId: number; // Patient plan item ID
  newPrice: number; // New price (must be >= 0)
  note?: string; // Optional reason (max 500 chars)
}
```

### Request Example

```json
{
  "items": [
    {
      "itemId": 101,
      "newPrice": 4500000,
      "note": "Điều chỉnh theo giá niêm yết mới 2025"
    },
    {
      "itemId": 102,
      "newPrice": 3200000,
      "note": "Giảm giá cho bệnh nhân thân thiết"
    }
  ]
}
```

### Response Example (200 OK)

```json
{
  "planCode": "PLAN-001",
  "itemsUpdated": 2,
  "financialImpact": {
    "previousTotalCost": 15000000,
    "newTotalCost": 13700000,
    "costDifference": -1300000
  },
  "updatedBy": {
    "employeeCode": "EMP-007",
    "fullName": "Nguyễn Thị Kế Toán"
  },
  "updatedAt": "2025-01-15T14:30:00"
}
```

### Error Codes

| Code | Message                            | Solution                                  |
| ---- | ---------------------------------- | ----------------------------------------- |
| 403  | Access Denied                      | User lacks MANAGE_PLAN_PRICING permission |
| 404  | Plan not found                     | Check `planCode`                          |
| 404  | Item {itemId} not found            | Check item IDs exist in plan              |
| 409  | Plan status is COMPLETED/CANCELLED | Cannot update prices for finished plans   |
| 400  | Price must be >= 0                 | Validate price input                      |

### Frontend Implementation

#### React Component Example

```typescript
import React, { useState } from "react";
import { toast } from "react-toastify";

interface PriceAdjustmentModalProps {
  planCode: string;
  items: PlanItem[];
  onSuccess: () => void;
}

const PriceAdjustmentModal: React.FC<PriceAdjustmentModalProps> = ({
  planCode,
  items,
  onSuccess,
}) => {
  const [adjustments, setAdjustments] = useState<Map<number, ItemAdjustment>>(
    new Map()
  );

  const handlePriceChange = (
    itemId: number,
    newPrice: number,
    note: string
  ) => {
    setAdjustments((prev) => new Map(prev).set(itemId, { newPrice, note }));
  };

  const submitPriceChanges = async () => {
    try {
      const updateRequest = {
        items: Array.from(adjustments.entries()).map(([itemId, adj]) => ({
          itemId,
          newPrice: adj.newPrice,
          note: adj.note,
        })),
      };

      const response = await api.patch(
        `/patient-treatment-plans/${planCode}/prices`,
        updateRequest
      );

      toast.success(
        `Đã cập nhật ${response.data.itemsUpdated} giá!
         Tổng chi phí: ${formatCurrency(
           response.data.financialImpact.newTotalCost
         )}`
      );

      onSuccess();
    } catch (error) {
      if (error.response?.status === 403) {
        toast.error("Bạn không có quyền điều chỉnh giá");
      } else {
        toast.error("Không thể cập nhật giá. Vui lòng thử lại.");
      }
    }
  };

  return (
    <Modal>
      <h2>Điều chỉnh giá Treatment Plan</h2>
      <table>
        <thead>
          <tr>
            <th>Dịch vụ</th>
            <th>Giá hiện tại</th>
            <th>Giá mới</th>
            <th>Lý do</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.itemId}>
              <td>{item.itemName}</td>
              <td>{formatCurrency(item.price)}</td>
              <td>
                <input
                  type="number"
                  min="0"
                  defaultValue={item.price}
                  onChange={(e) =>
                    handlePriceChange(
                      item.itemId,
                      parseFloat(e.target.value),
                      ""
                    )
                  }
                />
              </td>
              <td>
                <input
                  type="text"
                  placeholder="Lý do điều chỉnh..."
                  maxLength={500}
                  onChange={(e) => {
                    const adj = adjustments.get(item.itemId);
                    if (adj) {
                      handlePriceChange(
                        item.itemId,
                        adj.newPrice,
                        e.target.value
                      );
                    }
                  }}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <button onClick={submitPriceChanges}>Cập nhật giá</button>
    </Modal>
  );
};
```

#### Permission Check

```typescript
const canAdjustPrices = (user: User) => {
  return user.permissions.includes("MANAGE_PLAN_PRICING");
};

// In component
{
  canAdjustPrices(currentUser) && (
    <button onClick={openPriceAdjustmentModal}>Điều chỉnh giá</button>
  );
}
```

---

## 🔄 API 5.14: Drag-Drop Reordering

### Overview

Allows **Doctor/Manager** to reorder treatment plan items via drag-and-drop interface.

### Endpoint

```http
PATCH /api/v1/patient-plan-phases/{phaseId}/items/reorder
```

### Authentication

```http
Authorization: Bearer <JWT_TOKEN>
```

### Required Permission

- `UPDATE_TREATMENT_PLAN` (Doctor, Manager)

### Business Rules

- ✅ Supports drag-drop reordering within same phase
- ✅ Set comparison validation (prevents data loss from concurrent edits)
- ✅ SERIALIZABLE transaction isolation (race condition protection)
- ❌ Cannot reorder across different phases
- ❌ Cannot omit or duplicate items

### Request Body

```typescript
interface ReorderItemsRequest {
  itemIds: number[]; // Complete list in new order
}
```

### Request Example

```json
{
  "itemIds": [103, 101, 104, 102]
}
```

**Explanation:** Reorder items so:

- Item 103 → sequence 1
- Item 101 → sequence 2
- Item 104 → sequence 3
- Item 102 → sequence 4

### Response Example (200 OK)

```json
{
  "phaseId": 50,
  "phaseName": "Giai đoạn 1: Nền tảng",
  "itemsReordered": 4,
  "items": [
    {
      "itemId": 103,
      "itemName": "Nhổ răng khôn",
      "oldSequence": 3,
      "newSequence": 1
    },
    {
      "itemId": 101,
      "itemName": "Khám tổng quát",
      "oldSequence": 1,
      "newSequence": 2
    },
    {
      "itemId": 104,
      "itemName": "X-quang toàn hàm",
      "oldSequence": 4,
      "newSequence": 3
    },
    {
      "itemId": 102,
      "itemName": "Cạo vôi răng",
      "oldSequence": 2,
      "newSequence": 4
    }
  ]
}
```

### Error Codes

| Code | Message                          | Solution                              |
| ---- | -------------------------------- | ------------------------------------- |
| 404  | Phase not found                  | Check `phaseId`                       |
| 409  | Item count mismatch              | Must include ALL items (no omissions) |
| 409  | Concurrent modification detected | Reload page and retry                 |

### Concurrent Edit Protection

```json
{
  "status": 409,
  "message": "Danh sách items không khớp với hiện tại. Có thể có người khác đã thay đổi. Vui lòng tải lại trang.",
  "details": {
    "expectedItems": [101, 102, 103],
    "receivedItems": [101, 102]
  }
}
```

### Frontend Implementation

#### React DnD Example

```typescript
import { DragDropContext, Droppable, Draggable } from "react-beautiful-dnd";

interface TreatmentPlanItemsProps {
  phaseId: number;
  items: PlanItem[];
}

const TreatmentPlanItems: React.FC<TreatmentPlanItemsProps> = ({
  phaseId,
  items: initialItems,
}) => {
  const [items, setItems] = useState(initialItems);
  const [isSaving, setIsSaving] = useState(false);

  const handleDragEnd = async (result: DropResult) => {
    if (!result.destination) return;

    const reorderedItems = Array.from(items);
    const [removed] = reorderedItems.splice(result.source.index, 1);
    reorderedItems.splice(result.destination.index, 0, removed);

    // Optimistic update
    setItems(reorderedItems);

    try {
      setIsSaving(true);

      const response = await api.patch(
        `/patient-plan-phases/${phaseId}/items/reorder`,
        {
          itemIds: reorderedItems.map((item) => item.itemId),
        }
      );

      // Update with server response
      setItems(
        response.data.items.map((item) => ({
          ...items.find((i) => i.itemId === item.itemId)!,
          sequenceNumber: item.newSequence,
        }))
      );

      toast.success("Đã lưu thứ tự mới!");
    } catch (error) {
      // Rollback on error
      setItems(initialItems);

      if (error.response?.status === 409) {
        toast.error("Có người khác đã thay đổi danh sách. Đang tải lại...", {
          autoClose: 3000,
        });

        // Reload phase data
        setTimeout(() => window.location.reload(), 3000);
      } else {
        toast.error("Không thể lưu thứ tự. Vui lòng thử lại.");
      }
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <DragDropContext onDragEnd={handleDragEnd}>
      <Droppable droppableId="treatment-items">
        {(provided) => (
          <div
            {...provided.droppableProps}
            ref={provided.innerRef}
            className="treatment-items-list"
          >
            {items.map((item, index) => (
              <Draggable
                key={item.itemId}
                draggableId={String(item.itemId)}
                index={index}
              >
                {(provided, snapshot) => (
                  <div
                    ref={provided.innerRef}
                    {...provided.draggableProps}
                    {...provided.dragHandleProps}
                    className={`
                      treatment-item
                      ${snapshot.isDragging ? "dragging" : ""}
                    `}
                  >
                    <span className="drag-handle">⋮⋮</span>
                    <div className="item-content">
                      <div className="item-name">{item.itemName}</div>
                      <div className="item-price">
                        {formatCurrency(item.price)}
                      </div>
                    </div>
                  </div>
                )}
              </Draggable>
            ))}
            {provided.placeholder}
          </div>
        )}
      </Droppable>
      {isSaving && <Spinner text="Đang lưu..." />}
    </DragDropContext>
  );
};
```

#### CSS Styling

```css
.treatment-items-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.treatment-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: move;
  transition: all 0.2s ease;
}

.treatment-item.dragging {
  opacity: 0.8;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: rotate(3deg);
}

.drag-handle {
  color: #999;
  margin-right: 12px;
  font-size: 18px;
  cursor: grab;
}

.drag-handle:active {
  cursor: grabbing;
}
```

---

## 💵 V21.4: Optional Price Field

### Overview

Price field is now **optional** when creating treatment plans. Backend auto-fills from service default price.

### Affected APIs

- **API 5.1**: Create Custom Treatment Plan
- **API 5.7**: Add Items to Phase

### Frontend Changes Required

#### 1. Update TypeScript Interfaces

```typescript
// Before V21.4
interface AddItemToPhaseRequest {
  serviceCode: string;
  price: number; // ❌ Required
  quantity?: number;
  notes?: string;
}

// After V21.4
interface AddItemToPhaseRequest {
  serviceCode: string;
  price?: number; // ✅ Optional
  quantity?: number;
  notes?: string;
}
```

#### 2. Remove Price Validation

```typescript
// ❌ Delete this validation
const validatePrice = (price: number, servicePrice: number) => {
  const minPrice = servicePrice * 0.5;
  const maxPrice = servicePrice * 1.5;

  if (price < minPrice || price > maxPrice) {
    throw new Error(`Giá phải trong khoảng ${minPrice} - ${maxPrice}`);
  }
};

// ✅ No validation needed - backend handles it
const addItemToPhase = async (request: AddItemToPhaseRequest) => {
  // Just send request, no price validation
  return api.post("/patient-plan-phases/{phaseId}/items", request);
};
```

#### 3. Update UI Components

**Option A: Hide Price Field (Recommended)**

```tsx
const AddItemForm = () => {
  return (
    <form>
      <Select
        label="Dịch vụ"
        options={services}
        onChange={(service) => setSelectedService(service)}
      />

      {/* ❌ Remove price input field */}

      {/* ✅ Show auto-filled price as read-only info */}
      {selectedService && (
        <div className="price-display">
          <label>Giá dịch vụ</label>
          <span className="price-value">
            {formatCurrency(selectedService.price)}
          </span>
          <span className="price-label">(Tự động)</span>
        </div>
      )}

      <Input label="Số lượng" type="number" />
      <TextArea label="Ghi chú" />
    </form>
  );
};
```

**Option B: Show Read-Only Price**

```tsx
const AddItemForm = () => {
  return (
    <form>
      <Select label="Dịch vụ" options={services} />

      <Input
        label="Giá"
        value={selectedService?.price || 0}
        readOnly
        disabled
        hint="Giá tự động lấy từ dịch vụ"
      />

      <Input label="Số lượng" type="number" />
    </form>
  );
};
```

#### 4. Remove Error Messages

```typescript
// ❌ Delete these error handling blocks
catch (error) {
  if (error.message.includes('Giá phải trong khoảng')) {
    toast.error('Giá không hợp lệ! Phải trong khoảng ±50%.');
  }
}

// ✅ Backend won't return price validation errors anymore
```

---

## 🎛️ V21.4: Auto-Submit Control

### Overview

API 5.7 now supports `autoSubmit` query parameter to control status changes when adding items to APPROVED plans.

### Query Parameter

```typescript
interface AddItemsQueryParams {
  autoSubmit?: boolean; // Default: true
}
```

### Behavior Matrix

| Plan Status | autoSubmit | Result                       |
| ----------- | ---------- | ---------------------------- |
| DRAFT       | `true`     | Stays DRAFT ✅               |
| DRAFT       | `false`    | Stays DRAFT ✅               |
| APPROVED    | `true`     | Changes to PENDING_REVIEW ⚠️ |
| APPROVED    | `false`    | Stays APPROVED ✅            |

### Use Cases

#### Use Case 1: DRAFT Plan Editing (Use `autoSubmit=false`)

```typescript
const addItemToDraftPlan = async (phaseId: number, items: AddItemRequest[]) => {
  // Keep plan in DRAFT for continued editing
  return api.post(
    `/patient-plan-phases/${phaseId}/items?autoSubmit=false`,
    items
  );
};
```

#### Use Case 2: APPROVED Plan Modification (Use `autoSubmit=true`)

```typescript
const addItemToApprovedPlan = async (
  phaseId: number,
  items: AddItemRequest[]
) => {
  // Confirmation dialog
  const confirmed = await showConfirmDialog({
    title: "Xác nhận thay đổi",
    message:
      "Thêm dịch vụ sẽ chuyển lộ trình về trạng thái CHỜ DUYỆT. Tiếp tục?",
  });

  if (!confirmed) return;

  // Auto-submit to PENDING_REVIEW
  return api.post(
    `/patient-plan-phases/${phaseId}/items?autoSubmit=true`,
    items
  );
};
```

### Frontend Implementation

#### React Hook Example

```typescript
import { useState } from "react";

const useAddItemsToPhase = () => {
  const [loading, setLoading] = useState(false);

  const addItems = async (
    phaseId: number,
    items: AddItemRequest[],
    planStatus: ApprovalStatus,
    options?: { suppressAutoSubmit?: boolean }
  ) => {
    setLoading(true);

    try {
      // Determine autoSubmit based on plan status and user choice
      const autoSubmit =
        options?.suppressAutoSubmit === true
          ? false
          : planStatus === "APPROVED";

      // Show confirmation for APPROVED plans
      if (planStatus === "APPROVED" && autoSubmit) {
        const confirmed = await confirm(
          "Thêm dịch vụ sẽ gửi lại lộ trình để phê duyệt. Tiếp tục?"
        );
        if (!confirmed) return null;
      }

      const response = await api.post(
        `/patient-plan-phases/${phaseId}/items`,
        items,
        { params: { autoSubmit } }
      );

      return response.data;
    } catch (error) {
      console.error("Failed to add items:", error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  return { addItems, loading };
};
```

#### Usage in Component

```tsx
const TreatmentPlanEditor = ({ plan, phase }) => {
  const { addItems, loading } = useAddItemsToPhase();

  const handleAddItems = async (items: AddItemRequest[]) => {
    try {
      const result = await addItems(phase.phaseId, items, plan.approvalStatus, {
        // Suppress auto-submit for DRAFT editing
        suppressAutoSubmit: plan.approvalStatus === "DRAFT",
      });

      if (result) {
        toast.success("Đã thêm dịch vụ!");
        refreshPlan();
      }
    } catch (error) {
      toast.error("Không thể thêm dịch vụ");
    }
  };

  return (
    <div>
      <AddItemsForm onSubmit={handleAddItems} />
      {loading && <Spinner />}
    </div>
  );
};
```

---

## 📝 TypeScript Examples

### Complete API Client

```typescript
import axios, { AxiosInstance } from "axios";

class TreatmentPlanAPI {
  private client: AxiosInstance;

  constructor(baseURL: string, token: string) {
    this.client = axios.create({
      baseURL,
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
  }

  // API 5.12: Submit for Review
  async submitForReview(planCode: string) {
    return this.client.patch(`/patient-treatment-plans/${planCode}/submit`);
  }

  // API 5.13: Update Prices (Finance only)
  async updatePrices(planCode: string, items: ItemPriceUpdate[]) {
    return this.client.patch(`/patient-treatment-plans/${planCode}/prices`, {
      items,
    });
  }

  // API 5.14: Reorder Items
  async reorderItems(phaseId: number, itemIds: number[]) {
    return this.client.patch(`/patient-plan-phases/${phaseId}/items/reorder`, {
      itemIds,
    });
  }

  // API 5.7: Add Items (V21.4 with autoSubmit)
  async addItemsToPhase(
    phaseId: number,
    items: AddItemToPhaseRequest[],
    autoSubmit: boolean = true
  ) {
    return this.client.post(`/patient-plan-phases/${phaseId}/items`, items, {
      params: { autoSubmit },
    });
  }
}
```

### Type Definitions

```typescript
// V21.4: Updated request types
interface AddItemToPhaseRequest {
  serviceCode: string;
  price?: number; // ✅ Optional
  quantity?: number;
  notes?: string;
}

// API 5.13: Finance price management
interface ItemPriceUpdate {
  itemId: number;
  newPrice: number;
  note?: string;
}

interface UpdatePricesRequest {
  items: ItemPriceUpdate[];
}

interface UpdatePricesResponse {
  planCode: string;
  itemsUpdated: number;
  financialImpact: {
    previousTotalCost: number;
    newTotalCost: number;
    costDifference: number;
  };
  updatedBy: {
    employeeCode: string;
    fullName: string;
  };
  updatedAt: string;
}

// API 5.14: Reorder items
interface ReorderItemsRequest {
  itemIds: number[];
}

interface ReorderItemsResponse {
  phaseId: number;
  phaseName: string;
  itemsReordered: number;
  items: ReorderedItem[];
}

interface ReorderedItem {
  itemId: number;
  itemName: string;
  oldSequence: number;
  newSequence: number;
}

// API 5.12: Submit for review
interface SubmitForReviewResponse {
  planCode: string;
  planName: string;
  patientName: string;
  previousStatus: string;
  newStatus: string;
  submittedBy: string;
  submittedAt: string;
  message: string;
}
```

---

## 🧪 Testing Guide

### Test Case 1: Optional Price Field

```typescript
describe("V21.4: Optional Price Field", () => {
  it("should auto-fill price from service when not provided", async () => {
    const request = {
      serviceCode: "SVC-001",
      // price omitted - should auto-fill
      quantity: 1,
    };

    const response = await api.post("/patient-plan-phases/1/items", [request]);

    expect(response.data.items[0].price).toBe(500000); // Service default
  });

  it("should allow manual price override if provided", async () => {
    const request = {
      serviceCode: "SVC-001",
      price: 600000, // Manual override
      quantity: 1,
    };

    const response = await api.post("/patient-plan-phases/1/items", [request]);

    expect(response.data.items[0].price).toBe(600000);
  });
});
```

### Test Case 2: Auto-Submit Control

```typescript
describe("V21.4: Auto-Submit Parameter", () => {
  it("should keep DRAFT status with autoSubmit=false", async () => {
    const response = await api.post(
      "/patient-plan-phases/1/items?autoSubmit=false",
      [{ serviceCode: "SVC-001" }]
    );

    expect(response.data.plan.approvalStatus).toBe("DRAFT");
  });

  it("should revert APPROVED to PENDING_REVIEW with autoSubmit=true", async () => {
    const response = await api.post(
      "/patient-plan-phases/2/items?autoSubmit=true",
      [{ serviceCode: "SVC-001" }]
    );

    expect(response.data.plan.approvalStatus).toBe("PENDING_REVIEW");
  });
});
```

### Test Case 3: Finance Price Management

```typescript
describe("API 5.13: Finance Price Management", () => {
  it("should allow Finance to update prices", async () => {
    const request = {
      items: [{ itemId: 101, newPrice: 4500000, note: "Giảm giá VIP" }],
    };

    const response = await api.patch(
      "/patient-treatment-plans/PLAN-001/prices",
      request
    );

    expect(response.status).toBe(200);
    expect(response.data.itemsUpdated).toBe(1);
  });

  it("should reject if user lacks MANAGE_PLAN_PRICING permission", async () => {
    try {
      await api.patch("/patient-treatment-plans/PLAN-001/prices", {
        items: [],
      });
      fail("Should have thrown 403");
    } catch (error) {
      expect(error.response.status).toBe(403);
    }
  });
});
```

### Test Case 4: Drag-Drop Reordering

```typescript
describe("API 5.14: Reorder Items", () => {
  it("should reorder items successfully", async () => {
    const response = await api.patch("/patient-plan-phases/50/items/reorder", {
      itemIds: [103, 101, 102],
    });

    expect(response.data.itemsReordered).toBe(3);
    expect(response.data.items[0].newSequence).toBe(1);
  });

  it("should reject if item count mismatch", async () => {
    try {
      await api.patch(
        "/patient-plan-phases/50/items/reorder",
        { itemIds: [101, 102] } // Missing item 103
      );
      fail("Should have thrown 409");
    } catch (error) {
      expect(error.response.status).toBe(409);
      expect(error.response.data.message).toContain("không khớp");
    }
  });
});
```

---

## 📞 Support & Questions

**Backend Developer**: [Your Name]
**API Documentation**: `d:\Code\PDCMS_BE\docs\API_DOCUMENTATION.md`
**Change Log**: `d:\Code\PDCMS_BE\CHANGELOG.md`

**Quick Links**:

- [V21 Technical Summary](./docs/V21_TECHNICAL_SUMMARY.md)
- [V21 Migration Guide](./docs/V21_MIGRATION_GUIDE.md)
- [Treatment Plan API Guide](./docs/api-guides/treatment-plan/)

---

**Last Updated**: January 2025
**Version**: V21.4 + V21.5
