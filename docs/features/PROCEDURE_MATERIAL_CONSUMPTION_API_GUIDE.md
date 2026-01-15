# Procedure Material Consumption API Guide

## Overview

This integration connects **Clinical Records**, **Dental Services**, and **Warehouse** modules to automatically track and deduct materials used during dental procedures.

### Key Features
- ✅ **Automatic Material Deduction**: Materials automatically deducted from warehouse when appointment is completed
- ✅ **FEFO Algorithm**: First Expired First Out - always uses materials expiring soonest
- ✅ **Variance Tracking**: Compare planned vs actual material usage
- ✅ **Permission-Based Access**: Cost data visible only to Admin/Accountant roles
- ✅ **Stock Alerts**: Real-time stock status (OK/LOW/OUT_OF_STOCK) for each material

---

## Integration Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                        APPOINTMENT LIFECYCLE                         │
└─────────────────────────────────────────────────────────────────────┘

1️⃣ SCHEDULED
   └─ Appointment created
   └─ Warehouse: NOT TOUCHED ✋

2️⃣ CHECKED_IN
   └─ Patient arrives
   └─ Warehouse: NOT TOUCHED ✋

3️⃣ IN_PROGRESS
   └─ Doctor adds procedures to clinical record
   └─ Each procedure has service BOM (Bill of Materials)
   └─ Records created in procedure_material_usage table
   └─ Warehouse: NOT TOUCHED ✋ (materials only PLANNED, not deducted)

4️⃣ COMPLETED ⚡
   └─ AppointmentStatusService detects status change
   └─ Calls ClinicalRecordService.deductMaterialsForAppointment()
   └─ For each procedure:
       ├─ Gets service BOM from service_consumables table
       ├─ Deducts materials using FEFO algorithm from item_batches
       ├─ Creates procedure_material_usage records
       └─ Updates procedure.materials_deducted_at timestamp
   └─ Warehouse: MATERIALS DEDUCTED ✅

5️⃣ POST-COMPLETION
   └─ Assistants review actual usage
   └─ API 8.8: Update actual quantities if different from planned
   └─ Warehouse: STOCK ADJUSTED based on variance
```

---

## Database Schema

### Tables Involved

#### 1. `service_consumables` (Service BOM)
Defines standard materials needed for each dental service.

```sql
service_id          BIGINT         -- FK to dental_services
item_master_id      BIGINT         -- FK to item_master
quantity_required   DECIMAL(10,2)  -- Standard quantity per service
unit_id             BIGINT         -- FK to item_units
notes               TEXT           -- Usage notes
```

**Example:**
```
service_id: 1 (Root Canal Treatment)
├─ Lidocaine 2%: 2 ampules
├─ K-files: 3 pieces
├─ Gutta-percha: 1 set
└─ Root canal sealer: 0.5 tube
```

---

#### 2. `clinical_record_procedures`
Records procedures performed during appointments.

**New Material Tracking Columns:**
```sql
quantity_multiplier      DECIMAL(5,2)   -- Default 1, multiply BOM quantities
storage_transaction_id   BIGINT         -- FK to storage_transactions
materials_deducted_at    TIMESTAMP      -- When materials were deducted
materials_deducted_by    VARCHAR(50)    -- Username who triggered deduction
```

---

#### 3. `procedure_material_usage`
Tracks planned vs actual material consumption per procedure.

```sql
usage_id             BIGSERIAL PRIMARY KEY
procedure_id         INTEGER         -- FK to clinical_record_procedures
item_master_id       BIGINT          -- FK to item_master
planned_quantity     DECIMAL(10,2)   -- From service BOM
actual_quantity      DECIMAL(10,2)   -- Updated by assistants
variance_quantity    DECIMAL(10,2)   -- GENERATED: actual - planned
variance_reason      VARCHAR(100)    -- Why variance occurred
unit_id              BIGINT          -- FK to item_units
recorded_at          TIMESTAMP       -- When record created
recorded_by          VARCHAR(50)     -- Who created record
notes                TEXT            -- Additional notes
```

**Variance Calculation (PostgreSQL):**
```sql
variance_quantity DECIMAL(10,2) GENERATED ALWAYS AS (actual_quantity - planned_quantity) STORED
```

---

#### 4. `item_batches` (Warehouse Stock)
Warehouse inventory with FEFO tracking.

```sql
batch_id            BIGSERIAL PRIMARY KEY
item_master_id      BIGINT          -- FK to item_master
lot_number          VARCHAR(50)     -- Batch/lot identifier
quantity_on_hand    INTEGER         -- Current stock
expiry_date         DATE            -- Expiration date
```

**FEFO Query:**
```sql
SELECT * FROM item_batches 
WHERE item_master_id = ? AND quantity_on_hand > 0
ORDER BY 
  CASE WHEN expiry_date IS NULL THEN 1 ELSE 0 END,
  expiry_date ASC NULLS LAST
```

---

## API Endpoints

### API 8.7: Get Procedure Materials

**Endpoint:** `GET /api/v1/clinical-records/procedures/{procedureId}/materials`

**Description:** Returns all materials used/planned for a procedure with planned vs actual quantities, variance, stock status, and cost data.

**Authorization:**
- `ROLE_ADMIN`: Full access
- `VIEW_CLINICAL_RECORD`: Can view
- `WRITE_CLINICAL_RECORD`: Can view

**Cost Data Visibility:**
- `VIEW_WAREHOUSE_COST`: See prices and costs
- Without permission: Prices/costs are `null`

---

#### Request

**Path Parameters:**
- `procedureId` (Integer, required): Procedure ID

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

---

#### Response 200 OK

```json
{
  "procedureId": 123,
  "serviceName": "Root Canal Treatment",
  "serviceCode": "RCT-001",
  "toothNumber": "46",
  "materialsDeducted": true,
  "deductedAt": "2025-12-25T15:30:00",
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
      "actualQuantity": 3.00,
      "varianceQuantity": 1.00,
      "varianceReason": "ADDITIONAL_USAGE",
      "unitName": "Ampule",
      "unitPrice": 15000.00,
      "totalPlannedCost": 30000.00,
      "totalActualCost": 45000.00,
      "stockStatus": "OK",
      "currentStock": 125,
      "recordedAt": "2025-12-25T15:30:00",
      "recordedBy": "dr.mai",
      "notes": "Cần thêm 1 ống vì bệnh nhân khó tê"
    },
    {
      "usageId": 1002,
      "itemMasterId": 502,
      "itemCode": "TOOL-KFILE-21MM",
      "itemName": "K-Files 21mm (Endodontic File)",
      "categoryName": "Dental Instruments",
      "plannedQuantity": 3.00,
      "actualQuantity": 2.00,
      "varianceQuantity": -1.00,
      "varianceReason": "LESS_THAN_PLANNED",
      "unitName": "Piece",
      "unitPrice": 25000.00,
      "totalPlannedCost": 75000.00,
      "totalActualCost": 50000.00,
      "stockStatus": "LOW",
      "currentStock": 12,
      "recordedAt": "2025-12-25T15:30:00",
      "recordedBy": "dr.mai",
      "notes": "Rơi mất 1 cái khi sử dụng"
    },
    {
      "usageId": 1003,
      "itemMasterId": 503,
      "itemCode": "MAT-GP-POINT",
      "itemName": "Gutta-percha Points Set",
      "categoryName": "Dental Materials",
      "plannedQuantity": 1.00,
      "actualQuantity": 1.00,
      "varianceQuantity": 0.00,
      "varianceReason": null,
      "unitName": "Set",
      "unitPrice": 120000.00,
      "totalPlannedCost": 120000.00,
      "totalActualCost": 120000.00,
      "stockStatus": "OK",
      "currentStock": 45,
      "recordedAt": "2025-12-25T15:30:00",
      "recordedBy": "dr.mai",
      "notes": null
    }
  ],
  "totalPlannedCost": 225000.00,
  "totalActualCost": 215000.00,
  "costVariance": -10000.00
}
```

**Response if user has NO `VIEW_WAREHOUSE_COST` permission:**
```json
{
  "procedureId": 123,
  "serviceName": "Root Canal Treatment",
  "serviceCode": "RCT-001",
  "toothNumber": "46",
  "materialsDeducted": true,
  "deductedAt": "2025-12-25T15:30:00",
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
      "actualQuantity": 3.00,
      "varianceQuantity": 1.00,
      "varianceReason": "ADDITIONAL_USAGE",
      "unitName": "Ampule",
      "unitPrice": null,
      "totalPlannedCost": null,
      "totalActualCost": null,
      "stockStatus": "OK",
      "currentStock": 125,
      "recordedAt": "2025-12-25T15:30:00",
      "recordedBy": "dr.mai",
      "notes": "Cần thêm 1 ống vì bệnh nhân khó tê"
    }
  ],
  "totalPlannedCost": null,
  "totalActualCost": null,
  "costVariance": null
}
```

---

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `procedureId` | Integer | Procedure ID |
| `serviceName` | String | Name of dental service performed |
| `serviceCode` | String | Service code |
| `toothNumber` | String | Tooth number (e.g., "46") |
| `materialsDeducted` | Boolean | Whether materials have been deducted from warehouse |
| `deductedAt` | DateTime | When materials were deducted |
| `deductedBy` | String | Username who triggered deduction |
| `storageTransactionId` | Integer | Warehouse transaction ID |
| `materials` | Array | List of materials used |
| `materials[].usageId` | Integer | Material usage record ID |
| `materials[].itemMasterId` | Long | Item master ID |
| `materials[].itemCode` | String | Item code |
| `materials[].itemName` | String | Item name |
| `materials[].categoryName` | String | Category name |
| `materials[].plannedQuantity` | Decimal | Planned quantity from BOM |
| `materials[].actualQuantity` | Decimal | Actual quantity used |
| `materials[].varianceQuantity` | Decimal | Difference (actual - planned) |
| `materials[].varianceReason` | String | Reason for variance |
| `materials[].unitName` | String | Unit of measurement |
| `materials[].unitPrice` | Decimal | Price per unit (null if no permission) |
| `materials[].totalPlannedCost` | Decimal | Planned cost (null if no permission) |
| `materials[].totalActualCost` | Decimal | Actual cost (null if no permission) |
| `materials[].stockStatus` | String | `OK`, `LOW`, `OUT_OF_STOCK` |
| `materials[].currentStock` | Integer | Current warehouse stock |
| `materials[].recordedAt` | DateTime | When record created |
| `materials[].recordedBy` | String | Who created record |
| `materials[].notes` | String | Additional notes |
| `totalPlannedCost` | Decimal | Total planned cost (null if no permission) |
| `totalActualCost` | Decimal | Total actual cost (null if no permission) |
| `costVariance` | Decimal | Total variance (null if no permission) |

---

#### Error Responses

**404 Not Found - Procedure Not Found:**
```json
{
  "timestamp": "2025-12-25T16:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Procedure not found: 123",
  "path": "/api/v1/clinical-records/procedures/123/materials"
}
```

**403 Forbidden - No Permission:**
```json
{
  "timestamp": "2025-12-25T16:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/clinical-records/procedures/123/materials"
}
```

---

#### Business Rules

1. ✅ Materials are auto-deducted when appointment status changes to `COMPLETED`
2. ✅ If materials already deducted, subsequent completions are skipped
3. ✅ Stock status calculated in real-time:
   - `OUT_OF_STOCK`: quantity = 0
   - `LOW`: quantity ≤ minStockLevel
   - `OK`: quantity > minStockLevel
4. ✅ Cost data visible only to users with `VIEW_WAREHOUSE_COST` permission
5. ✅ Variance calculated as: `actual_quantity - planned_quantity`

---

### API 8.8: Update Procedure Materials

**Endpoint:** `PUT /api/v1/clinical-records/procedures/{procedureId}/materials`

**Description:** Update actual material quantities used during procedure. Adjusts warehouse stock based on variance.

**Authorization:**
- `ROLE_ADMIN`: Full access
- `WRITE_CLINICAL_RECORD`: Can update

**Use Case:** Assistants/nurses review actual material usage after procedure and update quantities that differ from planned.

---

#### Request

**Path Parameters:**
- `procedureId` (Integer, required): Procedure ID

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "materials": [
    {
      "usageId": 1001,
      "actualQuantity": 3.00,
      "varianceReason": "ADDITIONAL_USAGE",
      "notes": "Cần thêm 1 ống vì bệnh nhân khó tê"
    },
    {
      "usageId": 1002,
      "actualQuantity": 2.00,
      "varianceReason": "LESS_THAN_PLANNED",
      "notes": "Rơi mất 1 cái khi sử dụng"
    },
    {
      "usageId": 1003,
      "actualQuantity": 1.00,
      "varianceReason": null,
      "notes": null
    }
  ]
}
```

**Request Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `materials` | Array | Yes | List of material updates |
| `materials[].usageId` | Integer | Yes | Material usage record ID |
| `materials[].actualQuantity` | Decimal | Yes | Actual quantity used (must be ≥ 0) |
| `materials[].varianceReason` | String | No | Reason for variance |
| `materials[].notes` | String | No | Additional notes |

**Variance Reasons:**
- `ADDITIONAL_USAGE`: Used more than planned
- `LESS_THAN_PLANNED`: Used less than planned
- `WASTAGE`: Material wasted/damaged
- `PROCEDURE_COMPLEXITY`: More complex than expected
- `OTHER`: Other reason

---

#### Response 200 OK

```json
{
  "message": "Cập nhật số lượng vật tư thành công",
  "procedureId": 123,
  "materialsUpdated": 3,
  "stockAdjustments": [
    {
      "itemName": "Lidocaine 2% (Anesthetic)",
      "adjustment": 1.0,
      "reason": "Sử dụng thêm"
    },
    {
      "itemName": "K-Files 21mm (Endodontic File)",
      "adjustment": -1.0,
      "reason": "Sử dụng ít hơn"
    },
    {
      "itemName": "Gutta-percha Points Set",
      "adjustment": 0.0,
      "reason": "Không thay đổi"
    }
  ]
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `message` | String | Success message (Vietnamese) |
| `procedureId` | Integer | Procedure ID |
| `materialsUpdated` | Integer | Number of materials updated |
| `stockAdjustments` | Array | Warehouse stock adjustments |
| `stockAdjustments[].itemName` | String | Item name |
| `stockAdjustments[].adjustment` | Decimal | Quantity adjustment (positive = deduct more, negative = return) |
| `stockAdjustments[].reason` | String | Adjustment reason (Vietnamese) |

---

#### Error Responses

**404 Not Found - Procedure Not Found:**
```json
{
  "timestamp": "2025-12-25T16:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Procedure not found: 123",
  "path": "/api/v1/clinical-records/procedures/123/materials"
}
```

**400 Bad Request - Invalid Quantity:**
```json
{
  "timestamp": "2025-12-25T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "actualQuantity must be greater than or equal to 0",
  "path": "/api/v1/clinical-records/procedures/123/materials"
}
```

**403 Forbidden - No Permission:**
```json
{
  "timestamp": "2025-12-25T16:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/clinical-records/procedures/123/materials"
}
```

---

#### Business Rules

1. ✅ **Variance Calculation:**
   - Positive variance: Additional material deducted from warehouse
   - Negative variance: Material returned to warehouse (stock increased)
   - Zero variance: No warehouse adjustment

2. ✅ **FEFO Deduction:**
   - Additional materials deducted using FEFO (First Expired First Out)
   - Always uses batches expiring soonest first

3. ✅ **Audit Trail:**
   - Updates `recorded_at` and `recorded_by` in `procedure_material_usage`
   - Maintains full history of changes

4. ✅ **Idempotency:**
   - Can update same materials multiple times
   - Each update recalculates variance from current state

5. ✅ **Transaction Safety:**
   - All updates in single transaction
   - Rolls back if any material update fails

---

## Related APIs

### Service BOM Configuration

**Endpoint:** `GET /api/v1/warehouse/service-consumables/{serviceId}`

**Description:** Get Bill of Materials (BOM) for a dental service - shows what materials are normally required.

**Sample Response:**
```json
{
  "serviceId": 1,
  "serviceCode": "RCT-001",
  "serviceName": "Root Canal Treatment",
  "consumables": [
    {
      "itemMasterId": 501,
      "itemCode": "MED-LID-2%",
      "itemName": "Lidocaine 2%",
      "quantityRequired": 2.00,
      "unitName": "Ampule",
      "unitPrice": 15000.00,
      "totalCost": 30000.00,
      "stockStatus": "OK",
      "currentStock": 125
    }
  ],
  "totalConsumableCost": 225000.00
}
```

---

## Complete Integration Example

### Scenario: Root Canal Treatment for Patient "Nguyễn Văn An"

---

#### Step 1: Schedule Appointment

**Request:** `POST /api/v1/appointments`
```json
{
  "patientId": 789,
  "serviceId": 1,
  "employeeId": 10,
  "roomId": 3,
  "appointmentStartTime": "2025-12-25T14:00:00",
  "expectedDurationMinutes": 90
}
```

**Warehouse Status:** ❌ NOT TOUCHED

---

#### Step 2: Patient Checks In

**Request:** `PUT /api/v1/appointments/100/status`
```json
{
  "newStatus": "CHECKED_IN"
}
```

**Warehouse Status:** ❌ NOT TOUCHED

---

#### Step 3: Start Treatment

**Request:** `PUT /api/v1/appointments/100/status`
```json
{
  "newStatus": "IN_PROGRESS"
}
```

**Warehouse Status:** ❌ NOT TOUCHED

---

#### Step 4: Doctor Creates Clinical Record

**Request:** `POST /api/v1/appointments/clinical-records`
```json
{
  "appointmentId": 100,
  "chiefComplaint": "Đau răng hàm dưới bên phải",
  "diagnosis": "Viêm tủy có triệu chứng răng số 46",
  "vitalSigns": {
    "blood_pressure": "120/80",
    "heart_rate": 72,
    "temperature": 36.5
  }
}
```

**Response:**
```json
{
  "clinicalRecordId": 50,
  "appointmentId": 100,
  "createdAt": "2025-12-25T14:05:00"
}
```

**Warehouse Status:** ❌ NOT TOUCHED

---

#### Step 5: Doctor Adds Procedure

**Request:** `POST /api/v1/clinical-records/50/procedures`
```json
{
  "serviceId": 1,
  "toothNumber": "46",
  "procedureDescription": "Root canal treatment - tooth #46",
  "notes": "Standard RCT procedure"
}
```

**Response:**
```json
{
  "procedureId": 123,
  "clinicalRecordId": 50,
  "serviceId": 1,
  "serviceName": "Root Canal Treatment",
  "serviceCode": "RCT-001",
  "toothNumber": "46",
  "createdAt": "2025-12-25T14:10:00"
}
```

**Database (Behind the Scenes):**
```sql
-- Records created in procedure_material_usage with planned quantities
INSERT INTO procedure_material_usage (procedure_id, item_master_id, planned_quantity, actual_quantity)
VALUES 
  (123, 501, 2.00, 2.00),  -- Lidocaine
  (123, 502, 3.00, 3.00),  -- K-files  
  (123, 503, 1.00, 1.00);  -- Gutta-percha
```

**Warehouse Status:** ❌ NOT TOUCHED (materials only PLANNED!)

---

#### Step 6: Complete Treatment ⚡

**Request:** `PUT /api/v1/appointments/100/status`
```json
{
  "newStatus": "COMPLETED"
}
```

**Backend Process (Automatic):**
```
AppointmentStatusService detects COMPLETED status
  ↓
ClinicalRecordService.deductMaterialsForAppointment(100)
  ↓
For procedure #123:
  ↓
ProcedureMaterialService.deductMaterialsForProcedure(123)
  ↓
Gets BOM from procedure_material_usage
  ↓
For each material:
  ├─ Lidocaine: Deduct 2 ampules using FEFO
  │   └─ Batch C (expires 2025-12-30): 20 → 18 ampules
  ├─ K-files: Deduct 3 pieces
  │   └─ Batch X (expires 2026-01-15): 100 → 97 pieces
  └─ Gutta-percha: Deduct 1 set
      └─ Batch Y (expires 2026-02-01): 50 → 49 sets
  ↓
Update procedure:
  materials_deducted_at = '2025-12-25 15:30:00'
  materials_deducted_by = 'dr.mai'
  storage_transaction_id = 456
```

**Warehouse Status:** ✅ MATERIALS DEDUCTED!

---

#### Step 7: Review Materials (API 8.7)

**Request:** `GET /api/v1/clinical-records/procedures/123/materials`

**Response:**
```json
{
  "procedureId": 123,
  "serviceName": "Root Canal Treatment",
  "materialsDeducted": true,
  "deductedAt": "2025-12-25T15:30:00",
  "materials": [
    {
      "usageId": 1001,
      "itemName": "Lidocaine 2%",
      "plannedQuantity": 2.00,
      "actualQuantity": 2.00,
      "varianceQuantity": 0.00,
      "currentStock": 18,
      "stockStatus": "OK"
    },
    {
      "usageId": 1002,
      "itemName": "K-Files 21mm",
      "plannedQuantity": 3.00,
      "actualQuantity": 3.00,
      "varianceQuantity": 0.00,
      "currentStock": 97,
      "stockStatus": "OK"
    }
  ]
}
```

---

#### Step 8: Assistant Updates Actual Usage (API 8.8)

Nurse notices: Actually used 3 ampules of Lidocaine (not 2), and only 2 K-files (1 was dropped).

**Request:** `PUT /api/v1/clinical-records/procedures/123/materials`
```json
{
  "materials": [
    {
      "usageId": 1001,
      "actualQuantity": 3.00,
      "varianceReason": "ADDITIONAL_USAGE",
      "notes": "Cần thêm 1 ống vì bệnh nhân khó tê"
    },
    {
      "usageId": 1002,
      "actualQuantity": 2.00,
      "varianceReason": "LESS_THAN_PLANNED",
      "notes": "Rơi mất 1 cái khi sử dụng"
    }
  ]
}
```

**Response:**
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
      "adjustment": -1.0,
      "reason": "Sử dụng ít hơn"
    }
  ]
}
```

**Warehouse Adjustments:**
```
Lidocaine Batch C: 18 → 17 ampules (deduct 1 more)
K-files Batch X: 97 → 98 pieces (return 1)
```

---

#### Step 9: Final Material Report

**Request:** `GET /api/v1/clinical-records/procedures/123/materials`

**Response:**
```json
{
  "procedureId": 123,
  "serviceName": "Root Canal Treatment",
  "materialsDeducted": true,
  "materials": [
    {
      "usageId": 1001,
      "itemName": "Lidocaine 2%",
      "plannedQuantity": 2.00,
      "actualQuantity": 3.00,
      "varianceQuantity": 1.00,
      "varianceReason": "ADDITIONAL_USAGE",
      "currentStock": 17,
      "notes": "Cần thêm 1 ống vì bệnh nhân khó tê"
    },
    {
      "usageId": 1002,
      "itemName": "K-Files 21mm",
      "plannedQuantity": 3.00,
      "actualQuantity": 2.00,
      "varianceQuantity": -1.00,
      "varianceReason": "LESS_THAN_PLANNED",
      "currentStock": 98,
      "notes": "Rơi mất 1 cái khi sử dụng"
    }
  ],
  "totalPlannedCost": 225000.00,
  "totalActualCost": 215000.00,
  "costVariance": -10000.00
}
```

---

## Permission Matrix

| Role | View Materials (API 8.7) | View Costs | Update Quantities (API 8.8) |
|------|--------------------------|------------|----------------------------|
| Admin | ✅ Yes | ✅ Yes | ✅ Yes |
| Doctor | ✅ Yes (own procedures) | ❌ No | ✅ Yes (own procedures) |
| Nurse/Assistant | ✅ Yes (participated procedures) | ❌ No | ✅ Yes (participated procedures) |
| Receptionist | ✅ Yes (all) | ❌ No | ❌ No |
| Accountant | ✅ Yes (all) | ✅ Yes | ❌ No |
| Patient | ✅ Yes (own) | ❌ No | ❌ No |

---

## Stock Status Logic

```java
String stockStatus = "OK";
if (currentStock == null || currentStock == 0) {
    stockStatus = "OUT_OF_STOCK";
} else if (item.getMinStockLevel() != null && currentStock <= item.getMinStockLevel()) {
    stockStatus = "LOW";
}
```

**Status Meanings:**
- `OK`: Stock is sufficient (above minimum level)
- `LOW`: Stock at or below minimum level (reorder needed)
- `OUT_OF_STOCK`: No stock available

---

## FEFO Algorithm

**First Expired First Out** ensures materials expiring soonest are used first to minimize waste.

**Query:**
```sql
SELECT * FROM item_batches 
WHERE item_master_id = :itemId 
  AND quantity_on_hand > 0
ORDER BY 
  CASE WHEN expiry_date IS NULL THEN 1 ELSE 0 END,
  expiry_date ASC NULLS LAST
```

**Deduction Logic:**
```java
List<ItemBatch> batches = itemBatchRepository.findByItemMasterIdFEFO(itemMasterId);

double remaining = quantityNeeded;
for (ItemBatch batch : batches) {
    if (remaining <= 0) break;
    
    int available = batch.getQuantityOnHand();
    int toDeduct = (int) Math.min(available, remaining);
    
    batch.setQuantityOnHand(available - toDeduct);
    itemBatchRepository.save(batch);
    
    remaining -= toDeduct;
}

if (remaining > 0) {
    throw new InsufficientStockException(
        "Not enough stock for " + itemName + 
        ". Need " + quantityNeeded + ", only " + (quantityNeeded - remaining) + " available"
    );
}
```

---

## Error Handling

### Common Error Scenarios

#### 1. Insufficient Stock
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to deduct materials for procedure 123: Insufficient stock for Lidocaine 2%. Need 2, only 1 available"
}
```

**Behavior:**
- Error logged but doesn't fail appointment completion
- Staff can manually adjust materials later using API 8.8

---

#### 2. Materials Already Deducted
```
// Backend automatically skips re-deduction
if (procedure.getMaterialsDeductedAt() != null) {
    log.info("Materials already deducted for procedure {}, skipping", procedureId);
    return;
}
```

**Behavior:**
- Idempotent - safe to complete appointment multiple times
- Only deducts materials once

---

#### 3. No Service BOM
```
// No consumables defined for service
// Skip material deduction
if (consumables.isEmpty()) {
    log.info("No BOM defined for service {}, skipping material deduction", serviceId);
    return;
}
```

**Behavior:**
- Materials not deducted if service has no BOM configured
- No error thrown

---

## Testing Checklist

### Unit Tests
- [ ] FEFO deduction algorithm
- [ ] Variance calculation
- [ ] Stock status determination
- [ ] Permission-based cost visibility

### Integration Tests
- [ ] Material deduction on appointment completion
- [ ] Multiple procedures in single appointment
- [ ] Variance updates with positive/negative adjustments
- [ ] Insufficient stock handling

### E2E Tests
- [ ] Complete appointment flow (scheduled → completed)
- [ ] Material review and adjustment by assistant
- [ ] Cost data visibility based on user role
- [ ] FEFO across multiple batches

---

## Performance Considerations

### Optimizations
1. **Batch Queries**: Single query to load all materials for procedure
2. **JOIN FETCH**: Avoid N+1 queries for item master, category, unit
3. **Computed Columns**: `variance_quantity` calculated by database
4. **Index on FEFO**: Index on `(item_master_id, quantity_on_hand, expiry_date)`

### Recommended Indexes
```sql
CREATE INDEX idx_item_batches_fefo 
ON item_batches(item_master_id, expiry_date NULLS LAST, quantity_on_hand);

CREATE INDEX idx_procedure_materials_procedure 
ON procedure_material_usage(procedure_id);

CREATE INDEX idx_clinical_procedures_record 
ON clinical_record_procedures(clinical_record_id);
```

---

## Future Enhancements

### Phase 2 (Future)
- 📊 Material consumption analytics dashboard
- 📈 Variance trend analysis (which services have high variance)
- 🔔 Low stock alerts when materials fall below minimum
- 📋 Material wastage reports
- 💰 Cost analysis by service/doctor/period
- 🔄 Batch expiry alerts integration

### Phase 3 (Future)
- 🤖 AI-powered BOM optimization based on historical variance
- 📱 Mobile app for assistants to update materials
- 🔐 Digital signatures for material adjustments
- 📦 Automatic reorder suggestions based on consumption patterns

---

## Support & Troubleshooting

### Common Issues

**Q: Materials not deducted after appointment completion?**
- Check if service has BOM configured (`service_consumables` table)
- Verify appointment status is actually `COMPLETED`
- Check logs for errors during deduction
- Ensure `materials_deducted_at` is NULL before completion

**Q: Cost data showing as null?**
- Verify user has `VIEW_WAREHOUSE_COST` permission
- Check user's role assignments
- Admin and Accountant roles should have this permission by default

**Q: Variance updates not affecting warehouse stock?**
- Check FEFO query returns available batches
- Verify sufficient stock for additional deductions
- Check transaction logs for rollbacks

**Q: Stock status showing incorrect values?**
- Re-query `item_batches` table to verify actual stock
- Check if `min_stock_level` is set correctly in `item_master`
- Ensure all batches are summed (not just expired ones)

---

## Conclusion

This integration provides a complete solution for tracking and managing material consumption during dental procedures:

✅ **Automatic Deduction**: Materials auto-deducted when appointments complete  
✅ **Accurate Tracking**: Full variance analysis between planned and actual usage  
✅ **FEFO Compliance**: Always uses materials expiring soonest  
✅ **Permission Security**: Cost data visible only to authorized roles  
✅ **Stock Visibility**: Real-time stock status for all materials  

The system ensures accurate inventory management while maintaining flexibility for staff to adjust quantities based on actual clinical scenarios.
