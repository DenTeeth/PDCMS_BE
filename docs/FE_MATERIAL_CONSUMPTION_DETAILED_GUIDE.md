# Material Consumption Feature - Frontend Developer Guide

**Last Updated:** December 27, 2025  
**Backend Version:** V36  
**Status:** ✅ Ready for Integration

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [What Changed](#what-changed)
3. [Breaking Changes](#breaking-changes)
4. [Complete User Workflow](#complete-user-workflow)
5. [API Reference](#api-reference)
6. [Implementation Guide](#implementation-guide)
7. [Error Handling](#error-handling)
8. [Testing Scenarios](#testing-scenarios)

---

## Overview

This feature enables healthcare staff to manage material consumption for dental procedures with two key capabilities:

1. **Empty State Detection**: Know when a procedure doesn't use any materials
2. **Flexible Quantity Control**: Edit material quantities at two stages:
   - **Planning Stage** (BEFORE appointment): Edit `quantity` field
   - **Review Stage** (AFTER appointment): Edit `actualQuantity` field

---

## What Changed

### ✅ New Features

#### 1. `hasConsumables` Flag
- **Purpose**: Explicitly indicates if a procedure uses materials
- **Location**: API 8.7 response
- **Use Case**: Show "No materials" message instead of empty list

#### 2. Editable `quantity` Field
- **Purpose**: Per-material quantity customization (replaces global multiplier)
- **Location**: Each material item in `procedure_material_usage`
- **Use Case**: Edit individual material quantities before warehouse deduction

#### 3. New API Endpoint (API 8.9)
- **Purpose**: Update editable quantity before deduction
- **Endpoint**: `PATCH /api/v1/clinical-records/procedures/{procedureId}/materials/{usageId}/quantity`
- **Use Case**: Nurse adjusts material quantity during preparation

### ❌ Removed Features

#### 1. Global `quantity_multiplier`
- **Old Behavior**: Single multiplier applied to ALL materials
- **Reason Removed**: Not flexible enough for real-world usage
- **Replacement**: Per-material `quantity` field

---

## Breaking Changes

### Database Schema Changes

```sql
-- REMOVED from clinical_record_procedures
quantity_multiplier INTEGER  ❌ DELETED

-- ADDED to procedure_material_usage
quantity NUMERIC(10,2) NOT NULL  ✅ NEW

-- UPDATED variance calculation
-- OLD: variance_quantity = actual_quantity - planned_quantity
-- NEW: variance_quantity = actual_quantity - quantity
```

### API Response Changes

**Old Response Structure:**
```json
{
  "procedureId": 123,
  "materials": [
    {
      "plannedQuantity": 2.00,
      "actualQuantity": 3.00,
      "varianceQuantity": 1.00  // actual - planned
    }
  ]
}
```

**New Response Structure:**
```json
{
  "procedureId": 123,
  "hasConsumables": true,  // ✅ NEW
  "materials": [
    {
      "plannedQuantity": 2.00,   // BOM reference
      "quantity": 3.00,          // ✅ NEW: Editable before deduction
      "actualQuantity": 3.00,    // Updated after procedure
      "varianceQuantity": 0.00   // ✅ CHANGED: actual - quantity
    }
  ]
}
```

---

## Complete User Workflow

### Timeline Overview

```
┌────────────────────────────────────────────────────────────────┐
│                    APPOINTMENT LIFECYCLE                        │
└────────────────────────────────────────────────────────────────┘

📅 PHASE 1: SCHEDULED
   Status: SCHEDULED
   Materials: Not created yet
   Action: N/A

📝 PHASE 2: IN_PROGRESS (Procedure Added)
   Status: IN_PROGRESS
   Materials: Records created
   Fields: 
     - plannedQuantity = 2.00 (from BOM)
     - quantity = 2.00 (defaults to planned)
     - actualQuantity = 2.00 (defaults to quantity)
   Action: Nurse can edit `quantity` ✏️

🏥 PHASE 3: COMPLETED (Deduction Triggered)
   Status: COMPLETED
   Materials: Deducted from warehouse
   Fields: 
     - plannedQuantity = 2.00 (unchanged)
     - quantity = 3.00 (what was planned)
     - actualQuantity = 3.00 (synced with quantity)
   Action: Assistant can edit `actualQuantity` ✏️

📊 PHASE 4: FINAL REVIEW
   Status: COMPLETED
   Materials: Final usage recorded
   Fields:
     - plannedQuantity = 2.00 (BOM reference)
     - quantity = 3.00 (what was planned)
     - actualQuantity = 4.00 (what was used)
     - varianceQuantity = 1.00 (4 - 3)
   Action: View only 👁️
```

### Detailed Step-by-Step Flow

#### **Step 1: Appointment Created** 
**User:** Doctor  
**Action:** Create appointment and clinical record

```
Status: SCHEDULED
Materials: Not created yet
UI: Don't show materials section yet
```

---

#### **Step 2: Procedure Added to Clinical Record**
**User:** Doctor  
**Action:** Add procedure (e.g., "Root Canal Treatment")

**Backend automatically creates material usage records:**

```http
// Backend internal process (no FE action needed)
For each item in service BOM:
  INSERT INTO procedure_material_usage (
    planned_quantity = <from BOM>,
    quantity = <from BOM>,
    actual_quantity = <from BOM>
  )
```

**What FE Should Do:**
```javascript
// When doctor adds procedure, refresh materials
GET /api/v1/clinical-records/procedures/{procedureId}/materials
```

**Expected Response:**
```json
{
  "procedureId": 123,
  "serviceName": "Root Canal Treatment",
  "hasConsumables": true,
  "materialsDeducted": false,  // Not yet deducted
  "materials": [
    {
      "usageId": 1001,
      "itemName": "Lidocaine 2%",
      "plannedQuantity": 2.00,
      "quantity": 2.00,          // Can be edited
      "actualQuantity": 2.00,
      "varianceQuantity": 0.00,
      "unitName": "Ampule",
      "stockStatus": "OK",
      "currentStock": 125
    }
  ]
}
```

**UI Display:**
```
┌────────────────────────────────────────────┐
│ ✅ Vật tư tiêu hao đã sẵn sàng            │
├────────────────────────────────────────────┤
│ 💊 Lidocaine 2%                            │
│ Định mức BOM: 2.00 Ampule                 │
│ Số lượng dự kiến: [2.00] Ampule ✏️        │
│ Tồn kho: 125 Ampule ✅                    │
└────────────────────────────────────────────┘
```

---

#### **Step 3: Nurse Reviews and Edits Quantity (Optional)**
**User:** Nurse  
**Action:** Adjust quantity before appointment  
**Time:** BEFORE appointment completed

**Scenario:** Nurse thinks patient will need more anesthetic

**FE Action:**
```javascript
// User edits quantity from 2.00 to 3.00
const response = await fetch(
  '/api/v1/clinical-records/procedures/123/materials/1001/quantity',
  {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer YOUR_TOKEN'
    },
    body: JSON.stringify({
      usageId: 1001,
      quantity: 3.00
    })
  }
);
```

**Expected Response:**
```json
{
  "usageId": 1001,
  "itemMasterId": 501,
  "itemCode": "MED-LID-2%",
  "itemName": "Lidocaine 2%",
  "categoryName": "Medications",
  "plannedQuantity": 2.00,
  "quantity": 3.00,           // ✅ Updated
  "actualQuantity": 3.00,     // Auto-synced
  "varianceQuantity": 0.00,
  "unitName": "Ampule",
  "unitPrice": 15000.00,
  "totalPlannedCost": 30000.00,
  "totalActualCost": 45000.00,
  "stockStatus": "OK",
  "currentStock": 125,
  "recordedAt": "2025-12-27T09:30:00",
  "recordedBy": "nurse.lan"
}
```

**UI Display After Update:**
```
┌────────────────────────────────────────────┐
│ 💊 Lidocaine 2%                            │
│ Định mức BOM: 2.00 Ampule                 │
│ Số lượng dự kiến: [3.00] Ampule ✅ Đã sửa │
│ Tồn kho: 125 Ampule                       │
└────────────────────────────────────────────┘
```

---

#### **Step 4: Appointment Completed (Automatic Deduction)**
**User:** Doctor/Receptionist  
**Action:** Mark appointment as COMPLETED  
**Backend:** Automatically deducts materials

**What Happens:**
```
1. Status changes: IN_PROGRESS → COMPLETED
2. Backend triggers: deductMaterialsForProcedure()
3. Warehouse stock: 125 → 122 (deducts 3 ampules based on `quantity`)
4. Sets: materials_deducted_at = NOW()
5. Sets: materials_deducted_by = "dr.mai"
```

**FE Action:**
```javascript
// After status update, refresh materials to see deduction
GET /api/v1/clinical-records/procedures/123/materials
```

**Expected Response:**
```json
{
  "procedureId": 123,
  "materialsDeducted": true,  // ✅ Changed to true
  "deductedAt": "2025-12-27T14:30:00",
  "deductedBy": "dr.mai",
  "materials": [
    {
      "usageId": 1001,
      "itemName": "Lidocaine 2%",
      "plannedQuantity": 2.00,
      "quantity": 3.00,          // LOCKED - cannot edit anymore
      "actualQuantity": 3.00,    // Can be edited now
      "varianceQuantity": 0.00,
      "unitName": "Ampule",
      "currentStock": 122        // Updated stock
    }
  ]
}
```

**UI Display:**
```
┌────────────────────────────────────────────┐
│ ✅ Đã trừ kho lúc 14:30 bởi dr.mai        │
├────────────────────────────────────────────┤
│ 💊 Lidocaine 2%                            │
│ Định mức BOM: 2.00 Ampule                 │
│ Đã dự kiến dùng: 3.00 Ampule 🔒           │
│ Thực tế sử dụng: [3.00] Ampule ✏️         │
│ Tồn kho: 122 Ampule                       │
└────────────────────────────────────────────┘
```

---

#### **Step 5: Assistant Reviews Actual Usage**
**User:** Assistant  
**Action:** Update actual quantities used  
**Time:** AFTER procedure completed

**Scenario:** Doctor actually used 4 ampules (more than planned 3)

**FE Action:**
```javascript
const response = await fetch(
  '/api/v1/clinical-records/procedures/123/materials',
  {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer YOUR_TOKEN'
    },
    body: JSON.stringify({
      materials: [
        {
          usageId: 1001,
          actualQuantity: 4.00,
          varianceReason: "ADDITIONAL_USAGE",
          notes: "Patient difficult to anesthetize, needed extra dose"
        }
      ]
    })
  }
);
```

**Expected Response:**
```json
{
  "message": "Cập nhật số lượng vật tư thành công",
  "procedureId": 123,
  "materialsUpdated": 1,
  "stockAdjustments": [
    {
      "itemName": "Lidocaine 2%",
      "adjustment": 1.0,
      "reason": "Sử dụng thêm"
    }
  ]
}
```

**What Happens:**
```
1. System calculates: 4.00 (actual) - 3.00 (quantity) = 1.00 extra
2. Deducts 1 more ampule from warehouse: 122 → 121
3. Updates variance_quantity = 1.00
4. Records variance_reason
```

**Final GET Response:**
```json
{
  "materials": [
    {
      "usageId": 1001,
      "itemName": "Lidocaine 2%",
      "plannedQuantity": 2.00,
      "quantity": 3.00,
      "actualQuantity": 4.00,     // ✅ Updated
      "varianceQuantity": 1.00,   // ✅ Computed: 4 - 3
      "varianceReason": "ADDITIONAL_USAGE",
      "notes": "Patient difficult to anesthetize, needed extra dose",
      "unitName": "Ampule",
      "currentStock": 121          // ✅ Stock adjusted
    }
  ]
}
```

**UI Display:**
```
┌────────────────────────────────────────────┐
│ 💊 Lidocaine 2%                            │
├────────────────────────────────────────────┤
│ Định mức BOM: 2.00 Ampule                 │
│ Đã dự kiến dùng: 3.00 Ampule              │
│ Thực tế sử dụng: 4.00 Ampule ✅           │
│ Chênh lệch: +1.00 Ampule 🔴               │
│ Lý do: Bệnh nhân khó tê                   │
│ Tồn kho: 121 Ampule                       │
└────────────────────────────────────────────┘
```

---

## API Reference

### API 8.7: Get Procedure Materials

**Endpoint:**
```
GET /api/v1/clinical-records/procedures/{procedureId}/materials
```

**Purpose:** Get all materials for a procedure with quantities and stock status

**Authorization:** `VIEW_APPOINTMENT_ALL`, `VIEW_APPOINTMENT_OWN`, or `WRITE_CLINICAL_RECORD`

**Request:**
```http
GET /api/v1/clinical-records/procedures/123/materials HTTP/1.1
Host: api.dentalclinic.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response 200 OK:**
```json
{
  "procedureId": 123,
  "serviceName": "Root Canal Treatment",
  "serviceCode": "RCT-001",
  "toothNumber": "46",
  "hasConsumables": true,
  "materialsDeducted": true,
  "deductedAt": "2025-12-27T14:30:00",
  "deductedBy": "dr.mai",
  "storageTransactionId": 456,
  "materials": [
    {
      "usageId": 1001,
      "itemMasterId": 501,
      "itemCode": "MED-LID-2%",
      "itemName": "Lidocaine 2% (Anesthetic)",
      "categoryName": "Medications",
      "plannedQuantity": 2.00,
      "quantity": 3.00,
      "actualQuantity": 4.00,
      "varianceQuantity": 1.00,
      "varianceReason": "ADDITIONAL_USAGE",
      "unitName": "Ampule",
      "unitPrice": 15000.00,
      "totalPlannedCost": 30000.00,
      "totalActualCost": 60000.00,
      "stockStatus": "OK",
      "currentStock": 121,
      "recordedAt": "2025-12-27T14:30:00",
      "recordedBy": "dr.mai",
      "notes": "Patient difficult to anesthetize"
    }
  ],
  "totalPlannedCost": 30000.00,
  "totalActualCost": 60000.00,
  "costVariance": 30000.00
}
```

**Response 200 OK (No Materials):**
```json
{
  "procedureId": 124,
  "serviceName": "Dental Consultation",
  "serviceCode": "CONSULT-001",
  "hasConsumables": false,
  "materialsDeducted": false,
  "materials": [],
  "totalPlannedCost": null,
  "totalActualCost": null,
  "costVariance": null
}
```

**Response 404 Not Found:**
```json
{
  "timestamp": "2025-12-27T10:15:00",
  "status": 404,
  "error": "Not Found",
  "message": "Procedure not found: 999",
  "path": "/api/v1/clinical-records/procedures/999/materials"
}
```

---

### API 8.9: Update Editable Quantity (NEW)

**Endpoint:**
```
PATCH /api/v1/clinical-records/procedures/{procedureId}/materials/{usageId}/quantity
```

**Purpose:** Update editable quantity BEFORE warehouse deduction

**Authorization:** `WRITE_CLINICAL_RECORD`

**When to Use:** BEFORE appointment completed (before materials deducted)

**Request:**
```http
PATCH /api/v1/clinical-records/procedures/123/materials/1001/quantity HTTP/1.1
Host: api.dentalclinic.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "usageId": 1001,
  "quantity": 3.00
}
```

**Request Body Schema:**
```typescript
interface UpdateMaterialQuantityRequest {
  usageId: number;      // Required
  quantity: number;     // Required, must be > 0
}
```

**Response 200 OK:**
```json
{
  "usageId": 1001,
  "itemMasterId": 501,
  "itemCode": "MED-LID-2%",
  "itemName": "Lidocaine 2%",
  "categoryName": "Medications",
  "plannedQuantity": 2.00,
  "quantity": 3.00,
  "actualQuantity": 3.00,
  "varianceQuantity": 0.00,
  "varianceReason": null,
  "unitName": "Ampule",
  "unitPrice": 15000.00,
  "totalPlannedCost": 30000.00,
  "totalActualCost": 45000.00,
  "stockStatus": "OK",
  "currentStock": 125,
  "recordedAt": "2025-12-27T09:30:00",
  "recordedBy": "nurse.lan",
  "notes": null
}
```

**Response 400 Bad Request (Already Deducted):**
```json
{
  "timestamp": "2025-12-27T10:15:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot update quantity after materials have been deducted",
  "path": "/api/v1/clinical-records/procedures/123/materials/1001/quantity"
}
```

**Response 404 Not Found:**
```json
{
  "timestamp": "2025-12-27T10:15:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usage record not found: 1001",
  "path": "/api/v1/clinical-records/procedures/123/materials/1001/quantity"
}
```

---

### API 8.8: Update Actual Quantities (EXISTING)

**Endpoint:**
```
PUT /api/v1/clinical-records/procedures/{procedureId}/materials
```

**Purpose:** Update actual quantities AFTER procedure completed

**Authorization:** `WRITE_CLINICAL_RECORD`

**When to Use:** AFTER appointment completed (after materials deducted)

**Request:**
```http
PUT /api/v1/clinical-records/procedures/123/materials HTTP/1.1
Host: api.dentalclinic.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "materials": [
    {
      "usageId": 1001,
      "actualQuantity": 4.00,
      "varianceReason": "ADDITIONAL_USAGE",
      "notes": "Patient difficult to anesthetize, needed extra dose"
    },
    {
      "usageId": 1002,
      "actualQuantity": 2.50,
      "varianceReason": "LESS_THAN_PLANNED",
      "notes": "Simpler than expected"
    }
  ]
}
```

**Request Body Schema:**
```typescript
interface UpdateProcedureMaterialsRequest {
  materials: MaterialUpdateDTO[];
}

interface MaterialUpdateDTO {
  usageId: number;           // Required
  actualQuantity: number;    // Required, must be > 0
  varianceReason?: string;   // Optional, but recommended if variance exists
  notes?: string;            // Optional
}
```

**Response 200 OK:**
```json
{
  "message": "Cập nhật số lượng vật tư thành công",
  "procedureId": 123,
  "materialsUpdated": 2,
  "stockAdjustments": [
    {
      "itemName": "Lidocaine 2%",
      "adjustment": 1.0,
      "reason": "Sử dụng thêm"
    },
    {
      "itemName": "K-Files 21mm",
      "adjustment": -0.5,
      "reason": "Sử dụng ít hơn"
    }
  ]
}
```

**Response 404 Not Found:**
```json
{
  "timestamp": "2025-12-27T15:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Procedure not found: 999",
  "path": "/api/v1/clinical-records/procedures/999/materials"
}
```

---

## Implementation Guide

### State Management

```typescript
interface ProcedureMaterialsState {
  procedureId: number;
  hasConsumables: boolean;
  materialsDeducted: boolean;
  deductedAt: string | null;
  deductedBy: string | null;
  materials: MaterialItem[];
}

interface MaterialItem {
  usageId: number;
  itemName: string;
  plannedQuantity: number;    // BOM reference (read-only)
  quantity: number;           // Editable before deduction
  actualQuantity: number;     // Editable after deduction
  varianceQuantity: number;   // Computed
  varianceReason: string | null;
  unitName: string;
  stockStatus: 'OK' | 'LOW' | 'OUT_OF_STOCK';
  currentStock: number;
}
```

### React Component Example

```tsx
import React, { useState, useEffect } from 'react';
import { Card, InputNumber, Button, message, Badge, Empty } from 'antd';

const ProcedureMaterials: React.FC<{ procedureId: number }> = ({ procedureId }) => {
  const [state, setState] = useState<ProcedureMaterialsState | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadMaterials();
  }, [procedureId]);

  const loadMaterials = async () => {
    try {
      const response = await fetch(
        `/api/v1/clinical-records/procedures/${procedureId}/materials`,
        {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        }
      );
      const data = await response.json();
      setState(data);
    } catch (error) {
      message.error('Lỗi khi tải thông tin vật tư');
    } finally {
      setLoading(false);
    }
  };

  // Check if no materials
  if (!loading && !state?.hasConsumables) {
    return (
      <Card>
        <Empty
          description="Thủ thuật không tiêu hao vật tư"
          image={Empty.PRESENTED_IMAGE_SIMPLE}
        />
      </Card>
    );
  }

  // Update quantity before deduction
  const updateQuantity = async (usageId: number, quantity: number) => {
    try {
      await fetch(
        `/api/v1/clinical-records/procedures/${procedureId}/materials/${usageId}/quantity`,
        {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getToken()}`
          },
          body: JSON.stringify({ usageId, quantity })
        }
      );
      message.success('Cập nhật số lượng thành công');
      loadMaterials();
    } catch (error: any) {
      if (error.response?.status === 400) {
        message.error('Không thể cập nhật - vật tư đã được trừ kho');
      }
    }
  };

  // Update actual quantity after procedure
  const updateActual = async (updates: MaterialUpdateDTO[]) => {
    try {
      await fetch(
        `/api/v1/clinical-records/procedures/${procedureId}/materials`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getToken()}`
          },
          body: JSON.stringify({ materials: updates })
        }
      );
      message.success('Cập nhật số lượng thực tế thành công');
      loadMaterials();
    } catch (error) {
      message.error('Lỗi khi cập nhật');
    }
  };

  return (
    <Card title="Vật tư tiêu hao">
      {state?.materials.map(material => (
        <MaterialItemComponent
          key={material.usageId}
          material={material}
          materialsDeducted={state.materialsDeducted}
          onUpdateQuantity={updateQuantity}
          onUpdateActual={updateActual}
        />
      ))}
    </Card>
  );
};
```

---

## Error Handling

### Common Error Scenarios

#### 1. Edit Quantity After Deduction
```javascript
try {
  await updateQuantity(usageId, newQuantity);
} catch (error) {
  if (error.response?.status === 400) {
    // Materials already deducted
    showError('Không thể chỉnh sửa - vật tư đã được trừ kho');
    disableEditButton();
  }
}
```

#### 2. Procedure Not Found
```javascript
try {
  const data = await getMaterials(procedureId);
} catch (error) {
  if (error.response?.status === 404) {
    showError('Không tìm thấy thủ thuật');
    redirectToProcedureList();
  }
}
```

#### 3. Missing Permission
```javascript
try {
  await updateActual(materials);
} catch (error) {
  if (error.response?.status === 403) {
    showError('Bạn không có quyền cập nhật vật tư');
  }
}
```

#### 4. Stock Insufficient (During Deduction)
```javascript
// This happens automatically when appointment completes
// Backend returns error if stock is insufficient
// FE should show the error to user and prevent completion
```

---

## Testing Scenarios

### Test Case 1: Procedure with No Materials

**Setup:**
- Create service with no items in BOM
- Create procedure with that service

**Expected:**
```json
GET /procedures/123/materials
→ hasConsumables: false
→ materials: []
```

**UI Test:**
- [ ] Shows empty state message
- [ ] No material list displayed
- [ ] No edit buttons visible

---

### Test Case 2: Edit Quantity Before Deduction

**Setup:**
- Create procedure with materials
- Materials NOT deducted

**Steps:**
1. GET materials → materialsDeducted: false
2. User edits quantity from 2 to 3
3. PATCH /materials/1001/quantity with quantity: 3
4. GET materials again

**Expected:**
- [ ] PATCH returns 200
- [ ] quantity updated to 3.00
- [ ] actualQuantity synced to 3.00
- [ ] No stock deduction yet

---

### Test Case 3: Prevent Edit After Deduction

**Setup:**
- Create procedure with materials
- Complete appointment (materials deducted)

**Steps:**
1. GET materials → materialsDeducted: true
2. Try to PATCH /materials/1001/quantity

**Expected:**
- [ ] PATCH returns 400
- [ ] Error message: "Cannot update quantity after materials have been deducted"
- [ ] Edit button disabled in UI

---

### Test Case 4: Update Actual Quantity

**Setup:**
- Procedure completed
- Materials deducted

**Steps:**
1. GET materials → materialsDeducted: true
2. User updates actualQuantity from 3 to 4
3. PUT /materials with actualQuantity: 4, varianceReason: "ADDITIONAL_USAGE"

**Expected:**
- [ ] PUT returns 200
- [ ] actualQuantity updated to 4.00
- [ ] varianceQuantity computed as 1.00
- [ ] Stock deducted by 1 more (adjustment)

---

### Test Case 5: Permission-Based Cost Visibility

**Setup:**
- User WITHOUT VIEW_WAREHOUSE_COST permission

**Steps:**
1. GET materials

**Expected:**
```json
{
  "materials": [{
    "unitPrice": null,
    "totalPlannedCost": null,
    "totalActualCost": null
  }],
  "totalPlannedCost": null,
  "totalActualCost": null
}
```

**UI Test:**
- [ ] Price columns hidden
- [ ] Cost summary hidden

---

## Quick Reference

### Field Visibility Matrix

| Field | Before Deduction | After Deduction |
|-------|------------------|-----------------|
| `plannedQuantity` | 📖 Show (read-only) | 📖 Show (read-only) |
| `quantity` | ✏️ Show (EDITABLE) | 🔒 Show (read-only) |
| `actualQuantity` | ❌ Hide | ✏️ Show (EDITABLE) |
| `varianceQuantity` | ❌ Hide | 📊 Show (computed) |

### API Usage Matrix

| Action | API | Method | When |
|--------|-----|--------|------|
| Get materials | API 8.7 | GET | Anytime |
| Edit quantity | API 8.9 | PATCH | Before deduction |
| Update actual | API 8.8 | PUT | After deduction |

### Stock Status Colors

| Status | Color | Condition |
|--------|-------|-----------|
| `OK` | Green | stock > minLevel |
| `LOW` | Yellow | stock ≤ minLevel |
| `OUT_OF_STOCK` | Red | stock = 0 |

---

## Support

**Documentation:**
- API Reference: `/docs/PROCEDURE_MATERIAL_CONSUMPTION_API_GUIDE.md`
- Integration Guide: `/docs/FE_MATERIAL_CONSUMPTION_INTEGRATION_CHANGES.md`

**Contact:**
- Backend Team: For technical implementation
- Product Team: For feature requirements

---

**Version:** 1.0  
**Last Updated:** December 27, 2025
