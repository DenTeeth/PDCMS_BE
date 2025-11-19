# 🔄 API 5.14: Reorder Treatment Plan Items

**Version**: V21.5
**Release Date**: January 2025
**Permission Required**: `UPDATE_TREATMENT_PLAN`
**Allowed Roles**: ROLE_DOCTOR, ROLE_MANAGER

---

## 📋 Overview

Allows **Doctors and Managers** to reorder treatment plan items within a phase via drag-and-drop interface. This API uses **SERIALIZABLE transaction isolation** to prevent concurrent modification issues and includes **set comparison validation** to ensure data integrity.

### Business Context

- **Who uses this**: Doctor, Clinic Manager
- **When to use**:
  - Prioritize urgent treatments
  - Adjust clinical workflow sequence
  - Optimize treatment timeline
  - Respond to patient requests
- **What it does**:
  - Batch update item sequence numbers
  - Validate complete item list (no missing/extra items)
  - Protect against concurrent edits
  - Provide user-friendly error messages

---

## 🔗 Endpoint Details

### HTTP Method & URL

```http
PATCH /api/v1/patient-plan-phases/{phaseId}/items/reorder
```

### Path Parameters

| Parameter | Type    | Required | Description                                 | Example |
| --------- | ------- | -------- | ------------------------------------------- | ------- |
| `phaseId` | Integer | ✅ Yes   | ID of the phase containing items to reorder | `50`    |

### Request Headers

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### Authentication

- **Type**: JWT Bearer Token
- **Required Permission**: `UPDATE_TREATMENT_PLAN`
- **Access Control**:
  - ✅ ROLE_DOCTOR (phase owner only)
  - ✅ ROLE_MANAGER (all phases)
  - ❌ ROLE_NURSE (no access)
  - ❌ ROLE_ACCOUNTANT (no access)

---

## 📥 Request Body

### Schema

```json
{
  "itemIds": [103, 101, 104, 102]
}
```

### Field Descriptions

#### `itemIds` (Array, Required)

Complete list of item IDs in desired order.

| Field     | Type      | Required | Constraints               | Description                                 |
| --------- | --------- | -------- | ------------------------- | ------------------------------------------- |
| `itemIds` | Integer[] | ✅ Yes   | Must match existing items | Patient plan item IDs in new sequence order |

### Validation Rules

1. **Completeness**:
   - Must include ALL items in the phase
   - Cannot omit any existing items
   - Cannot add new items (use API 5.7 instead)
2. **Uniqueness**:
   - No duplicate item IDs
   - Each ID appears exactly once
3. **Existence**:
   - All IDs must exist in the specified phase
   - IDs from other phases are rejected
4. **Order**:
   - Array order determines new sequence numbers
   - First item → sequence 1, second → sequence 2, etc.

### Example Scenarios

#### Scenario 1: Simple Swap

**Before:** [101, 102, 103]
**Request:** `{ "itemIds": [102, 101, 103] }`
**After:** Item 102 moves to first position

#### Scenario 2: Complete Reorder

**Before:** [101, 102, 103, 104]
**Request:** `{ "itemIds": [104, 102, 101, 103] }`
**After:** Item 104 first, 102 second, 101 third, 103 fourth

#### Scenario 3: Move to End

**Before:** [101, 102, 103]
**Request:** `{ "itemIds": [102, 103, 101] }`
**After:** Item 101 moves to last position

---

## 📤 Response

### Success Response (200 OK)

#### Schema

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
    }
  ]
}
```

#### Field Descriptions

| Field                 | Type    | Description                                         |
| --------------------- | ------- | --------------------------------------------------- |
| `phaseId`             | Integer | ID of the phase                                     |
| `phaseName`           | String  | Name of the phase                                   |
| `itemsReordered`      | Integer | Number of items successfully reordered              |
| `items`               | Array   | List of reordered items with before/after sequences |
| `items[].itemId`      | Integer | Patient plan item ID                                |
| `items[].itemName`    | String  | Service name                                        |
| `items[].oldSequence` | Integer | Previous sequence number                            |
| `items[].newSequence` | Integer | New sequence number                                 |

---

## ❌ Error Responses

### 404 Not Found - Phase Not Found

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Patient plan phase not found with ID: 999",
  "path": "/api/v1/patient-plan-phases/999/items/reorder"
}
```

**Cause**: Invalid `phaseId` parameter
**Solution**: Verify phase exists and ID is correct

---

### 409 Conflict - Item Count Mismatch

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Danh sách items không khớp với hiện tại. Có thể có người khác đã thay đổi. Vui lòng tải lại trang.",
  "details": {
    "expectedItems": [101, 102, 103, 104],
    "receivedItems": [101, 102, 103],
    "missingItems": [104],
    "extraItems": []
  }
}
```

**Cause**:

- Missing items in request
- Items added/deleted by another user
- Concurrent modification

**Solution**:

1. Reload phase data from server
2. Get fresh item list
3. Retry reorder operation

**User Message**:

> "Có người khác đã thay đổi danh sách. Vui lòng tải lại trang."

---

### 409 Conflict - Duplicate Items

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Danh sách items không khớp với hiện tại. Có thể có người khác đã thay đổi. Vui lòng tải lại trang.",
  "details": {
    "expectedItems": [101, 102, 103],
    "receivedItems": [101, 102, 102],
    "duplicates": [102]
  }
}
```

**Cause**: Item ID appears multiple times in request
**Solution**: Ensure each item ID appears exactly once

---

### 404 Not Found - Item Not in Phase

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Patient plan item not found in this phase: 999",
  "details": {
    "phaseId": 50,
    "invalidItemId": 999
  }
}
```

**Cause**: Item ID belongs to different phase or doesn't exist
**Solution**: Verify item belongs to the correct phase

---

## 🔄 Business Rules

### Set Comparison Validation

The API uses **set comparison** to ensure data integrity:

```java
Set<Long> expectedIds = existingItems.stream()
    .map(PatientPlanItem::getItemId)
    .collect(Collectors.toSet());

Set<Long> receivedIds = new HashSet<>(request.getItemIds());

if (!expectedIds.equals(receivedIds)) {
    throw new ConflictException("Item list mismatch");
}
```

**Benefits**:

- ✅ Prevents accidental data loss
- ✅ Detects concurrent modifications
- ✅ Validates completeness
- ✅ User-friendly error messages

### Transaction Isolation

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
```

**Purpose**: Prevent race conditions when multiple users reorder simultaneously

**Behavior**:

- ✅ Only one reorder operation executes at a time
- ✅ Second operation sees fresh data from first
- ✅ Prevents "lost update" problem

**Trade-off**:

- Slightly slower performance
- Retries may be needed if conflict detected

### Sequence Number Assignment

Items are assigned sequence numbers based on array order:

| Array Position | Sequence Number | Item ID |
| -------------- | --------------- | ------- |
| 0 (first)      | 1               | 103     |
| 1 (second)     | 2               | 101     |
| 2 (third)      | 3               | 104     |
| 3 (fourth)     | 4               | 102     |

---

## 📝 Examples

### Example 1: Priority Treatment First

**Scenario**: Move urgent treatment to first position

**Current Order:**

1. Khám tổng quát (ID: 101)
2. Cạo vôi răng (ID: 102)
3. Nhổ răng khôn (ID: 103) ⚠️ Urgent

**Request:**

```bash
curl -X PATCH "https://api.dental.com/api/v1/patient-plan-phases/50/items/reorder" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "itemIds": [103, 101, 102]
  }'
```

**Response (200 OK):**

```json
{
  "phaseId": 50,
  "phaseName": "Giai đoạn 1: Chuẩn bị",
  "itemsReordered": 3,
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
      "itemId": 102,
      "itemName": "Cạo vôi răng",
      "oldSequence": 2,
      "newSequence": 3
    }
  ]
}
```

**New Order:**

1. Nhổ răng khôn (ID: 103) ✅ Now first
2. Khám tổng quát (ID: 101)
3. Cạo vôi răng (ID: 102)

---

### Example 2: Reverse Order

**Scenario**: Reverse all items

**Current Order:** [101, 102, 103, 104]

**Request:**

```json
{
  "itemIds": [104, 103, 102, 101]
}
```

**Response:**

```json
{
  "phaseId": 50,
  "itemsReordered": 4,
  "items": [
    { "itemId": 104, "oldSequence": 4, "newSequence": 1 },
    { "itemId": 103, "oldSequence": 3, "newSequence": 2 },
    { "itemId": 102, "oldSequence": 2, "newSequence": 3 },
    { "itemId": 101, "oldSequence": 1, "newSequence": 4 }
  ]
}
```

---

### Example 3: Concurrent Modification Error

**Scenario**: User A and User B both reorder at same time

**Initial State:** [101, 102, 103]

**User A Request:**

```json
{ "itemIds": [102, 101, 103] }
```

**User B Request (slightly later):**

```json
{ "itemIds": [103, 101, 102] }
```

**User A Response:** ✅ Success (200 OK)

**User B Response:** ❌ Conflict (409)

```json
{
  "status": 409,
  "message": "Danh sách items không khớp với hiện tại. Có thể có người khác đã thay đổi. Vui lòng tải lại trang.",
  "details": {
    "expectedItems": [102, 101, 103],
    "receivedItems": [103, 101, 102]
  }
}
```

**User B Action**: Reload page and retry with fresh data

---

## 🧪 Testing Guide

### Manual Testing (Postman)

#### Setup

1. **Get JWT Token** (Doctor or Manager)
2. **Find Phase ID**

   ```http
   GET /api/v1/patient-treatment-plans/{planCode}
   ```

   - Copy `phases[].patientPhaseId`

3. **Get Current Item Order**
   ```json
   {
     "phases": [
       {
         "patientPhaseId": 50,
         "items": [
           { "itemId": 101, "sequenceNumber": 1 },
           { "itemId": 102, "sequenceNumber": 2 },
           { "itemId": 103, "sequenceNumber": 3 }
         ]
       }
     ]
   }
   ```

#### Test Case 1: Valid Reorder

```http
PATCH /api/v1/patient-plan-phases/50/items/reorder
Authorization: Bearer <token>
Content-Type: application/json

{
  "itemIds": [103, 101, 102]
}
```

**Expected Result**:

- Status: 200 OK
- `itemsReordered`: 3
- Items have new sequence numbers

#### Test Case 2: Missing Item Error

```json
{
  "itemIds": [101, 102]
}
```

**Expected Result**:

- Status: 409 Conflict
- Error mentions "không khớp"
- Details show missing item 103

#### Test Case 3: Extra Item Error

```json
{
  "itemIds": [101, 102, 103, 999]
}
```

**Expected Result**:

- Status: 404 or 409
- Error mentions invalid item ID

#### Test Case 4: Duplicate Item Error

```json
{
  "itemIds": [101, 102, 102]
}
```

**Expected Result**:

- Status: 409 Conflict
- Details show duplicate item 102

---

## 🎨 Frontend Integration

### React Component with react-beautiful-dnd

```typescript
import { DragDropContext, Droppable, Draggable } from "react-beautiful-dnd";

const PhaseItemsList = ({ phaseId, items, onReorder }) => {
  const [localItems, setLocalItems] = useState(items);

  const handleDragEnd = async (result) => {
    if (!result.destination) return;

    // Optimistic update
    const reordered = Array.from(localItems);
    const [removed] = reordered.splice(result.source.index, 1);
    reordered.splice(result.destination.index, 0, removed);
    setLocalItems(reordered);

    try {
      const response = await api.patch(
        `/patient-plan-phases/${phaseId}/items/reorder`,
        { itemIds: reordered.map((i) => i.itemId) }
      );

      // Sync with server
      onReorder(response.data.items);
      toast.success("Đã lưu thứ tự mới!");
    } catch (error) {
      // Rollback
      setLocalItems(items);

      if (error.response?.status === 409) {
        toast.error("Có thay đổi từ người khác. Đang tải lại...", {
          autoClose: 3000,
        });
        setTimeout(() => window.location.reload(), 3000);
      } else {
        toast.error("Không thể lưu. Vui lòng thử lại.");
      }
    }
  };

  return (
    <DragDropContext onDragEnd={handleDragEnd}>
      <Droppable droppableId="items">
        {(provided) => (
          <div {...provided.droppableProps} ref={provided.innerRef}>
            {localItems.map((item, index) => (
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
                    className={snapshot.isDragging ? "dragging" : ""}
                  >
                    <span className="drag-handle">⋮⋮</span>
                    <span>{item.itemName}</span>
                    <span>{formatCurrency(item.price)}</span>
                  </div>
                )}
              </Draggable>
            ))}
            {provided.placeholder}
          </div>
        )}
      </Droppable>
    </DragDropContext>
  );
};
```

### Error Handling Best Practices

```typescript
const reorderItems = async (phaseId: number, itemIds: number[]) => {
  try {
    const response = await api.patch(
      `/patient-plan-phases/${phaseId}/items/reorder`,
      { itemIds }
    );

    return { success: true, data: response.data };
  } catch (error) {
    if (error.response?.status === 409) {
      // Concurrent modification - reload required
      return {
        success: false,
        error: "CONCURRENT_MODIFICATION",
        message: "Vui lòng tải lại trang",
        shouldReload: true,
      };
    } else if (error.response?.status === 404) {
      // Phase or items not found
      return {
        success: false,
        error: "NOT_FOUND",
        message: "Không tìm thấy dữ liệu",
      };
    } else {
      // Other errors
      return {
        success: false,
        error: "UNKNOWN",
        message: "Lỗi không xác định",
      };
    }
  }
};
```

---

## 🔐 Security Considerations

### Permission Check

```java
@PreAuthorize("hasAuthority('UPDATE_TREATMENT_PLAN')")
```

- Enforced at controller level
- Checked before any business logic

### Ownership Validation

- Doctors can only reorder items in their own plans
- Managers can reorder any plan

### Data Integrity

- ✅ SERIALIZABLE transaction isolation
- ✅ Set comparison validation
- ✅ Foreign key constraints
- ✅ Optimistic locking on phase updates

---

## 📊 Performance Metrics

### Response Time Targets

- **Expected**: < 300ms for 10 items
- **Acceptable**: < 500ms for 50 items
- **Optimization**: Batch SQL UPDATE with CASE statement

### Database Impact

- **Queries**: 2-3 per request
  1. SELECT items with FOR UPDATE
  2. Batch UPDATE sequence numbers
  3. Optional: Update phase timestamp

### SQL Optimization

```sql
UPDATE patient_plan_items
SET sequence_number = CASE item_id
  WHEN 103 THEN 1
  WHEN 101 THEN 2
  WHEN 104 THEN 3
  WHEN 102 THEN 4
END
WHERE item_id IN (103, 101, 104, 102);
```

Single query instead of N queries.

---

## 🔗 Related APIs

- **API 5.1**: Create Custom Treatment Plan (creates initial item order)
- **API 5.7**: Add Items to Phase (new items appended to end)
- **API 5.13**: Update Prices (Finance, separate from reordering)

---

## 📞 Support

**Technical Issues**: Contact Backend Team
**UX Questions**: Contact Product Manager
**Permission Issues**: Contact System Administrator

---

**Last Updated**: January 2025
**API Version**: V21.5
**Status**: ✅ Production Ready
