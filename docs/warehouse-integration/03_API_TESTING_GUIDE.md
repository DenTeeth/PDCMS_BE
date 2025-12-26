# 🧪 API Testing Guide - Hướng Dẫn Test API Từng Bước

## 🎯 Mục Đích
Hướng dẫn **COPY-PASTE** để test warehouse integration nhanh chóng.

---

## ⚙️ Setup

### 1. Base URL
```
http://localhost:8080/api/v1
```

### 2. Authentication
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

**Lưu token để dùng cho các request sau:**
```
Authorization: Bearer <token>
```

---

## 📋 Test Scenarios

### ✅ SCENARIO 1: Happy Path - Trám Răng

#### Step 1: Tạo Appointment
```http
POST /appointments
Authorization: Bearer <token>
Content-Type: application/json

{
  "patientId": 1,
  "serviceId": 5,
  "employeeId": 1,
  "roomId": 1,
  "appointmentStartTime": "2025-12-27T10:00:00",
  "expectedDurationMinutes": 60,
  "notes": "Test warehouse integration - Trám răng"
}
```

**Expected Response:**
```json
{
  "appointmentId": 150,
  "appointmentCode": "APT-20251227-001",
  "status": "SCHEDULED",
  "serviceId": 5,
  "serviceName": "Trám răng Composite"
}
```

**🔍 Verify:**
```sql
SELECT appointment_id, status FROM appointments WHERE appointment_id = 150;
-- Expected: status = 'SCHEDULED'
```

---

#### Step 2: Check-in Patient
```http
PUT /appointments/150/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "newStatus": "CHECKED_IN"
}
```

**Expected Response:**
```json
{
  "appointmentId": 150,
  "status": "CHECKED_IN",
  "message": "Cập nhật trạng thái thành công"
}
```

**🔍 Verify Warehouse:**
```sql
SELECT quantity_on_hand FROM item_batches WHERE lot_number = 'BATCH-GLOVE-2024-001';
-- Expected: UNCHANGED (chưa trừ kho)
```

---

#### Step 3: Start Treatment
```http
PUT /appointments/150/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "newStatus": "IN_PROGRESS"
}
```

**Expected Response:**
```json
{
  "appointmentId": 150,
  "status": "IN_PROGRESS"
}
```

---

#### Step 4: Create Clinical Record
```http
POST /appointments/clinical-records
Authorization: Bearer <token>
Content-Type: application/json

{
  "appointmentId": 150,
  "chiefComplaint": "Đau răng hàm dưới bên phải",
  "presentIllness": "Đau khi ăn đồ ngọt",
  "diagnosis": "Sâu răng răng số 46",
  "treatmentPlan": "Trám răng Composite",
  "vitalSigns": {
    "blood_pressure": "120/80",
    "heart_rate": 72,
    "temperature": 36.5
  }
}
```

**Expected Response:**
```json
{
  "clinicalRecordId": 75,
  "appointmentId": 150,
  "diagnosis": "Sâu răng răng số 46"
}
```

---

#### Step 5: Add Procedure
```http
POST /clinical-records/75/procedures
Authorization: Bearer <token>
Content-Type: application/json

{
  "serviceId": 5,
  "toothNumber": "46",
  "procedureDescription": "Trám răng Composite răng số 46",
  "notes": "Sâu răng mức độ trung bình"
}
```

**Expected Response:**
```json
{
  "procedureId": 200,
  "clinicalRecordId": 75,
  "serviceId": 5,
  "serviceName": "Trám răng Composite",
  "toothNumber": "46"
}
```

**🔍 Verify:**
```sql
SELECT procedure_id, materials_deducted_at 
FROM clinical_record_procedures 
WHERE procedure_id = 200;

-- Expected: materials_deducted_at = NULL (chưa trừ kho)
```

---

#### Step 6: ⚡ Complete Appointment (TRIGGER DEDUCTION)
```http
PUT /appointments/150/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "newStatus": "COMPLETED"
}
```

**Expected Response:**
```json
{
  "appointmentId": 150,
  "status": "COMPLETED",
  "message": "Hoàn thành lịch hẹn và tự động trừ vật tư"
}
```

**🔍 Verify Materials Deducted:**
```sql
-- 1. Check procedure updated
SELECT 
  procedure_id, 
  materials_deducted_at, 
  materials_deducted_by 
FROM clinical_record_procedures 
WHERE procedure_id = 200;

-- Expected: 
--   materials_deducted_at = NOT NULL
--   materials_deducted_by = 'admin' (hoặc username bạn dùng)

-- 2. Check usage records created
SELECT 
  usage_id, 
  item_master_id, 
  planned_quantity, 
  actual_quantity 
FROM procedure_material_usage 
WHERE procedure_id = 200;

-- Expected: 6 rows (theo BOM của FILLING_COMP)

-- 3. Check warehouse stock decreased
SELECT lot_number, quantity_on_hand 
FROM item_batches 
WHERE item_master_id IN (
  SELECT item_master_id FROM item_masters 
  WHERE item_code IN ('CON-GLOVE-01', 'MAT-COMP-01')
)
ORDER BY lot_number;

-- Expected: quantity_on_hand đã GIẢM
```

---

#### Step 7: View Materials Used
```http
GET /clinical-records/procedures/200/materials
Authorization: Bearer <token>
```

**Expected Response (Admin - with costs):**
```json
{
  "procedureId": 200,
  "serviceName": "Trám răng Composite",
  "serviceCode": "FILLING_COMP",
  "toothNumber": "46",
  "materialsDeducted": true,
  "deductedAt": "2025-12-27T10:30:00",
  "deductedBy": "admin",
  "materials": [
    {
      "usageId": 5001,
      "itemCode": "CON-GLOVE-01",
      "itemName": "Găng tay y tế",
      "plannedQuantity": 1.00,
      "actualQuantity": 1.00,
      "varianceQuantity": 0.00,
      "unitName": "Đôi",
      "unitPrice": 150000.00,
      "totalPlannedCost": 150000.00,
      "totalActualCost": 150000.00,
      "stockStatus": "OK",
      "currentStock": 179
    },
    {
      "usageId": 5002,
      "itemCode": "MAT-COMP-01",
      "itemName": "Trám Composite",
      "plannedQuantity": 8.00,
      "actualQuantity": 8.00,
      "varianceQuantity": 0.00,
      "unitName": "g",
      "unitPrice": 500000.00,
      "totalPlannedCost": 4000000.00,
      "totalActualCost": 4000000.00,
      "stockStatus": "LOW",
      "currentStock": 27
    }
  ],
  "totalPlannedCost": 4500000.00,
  "totalActualCost": 4500000.00,
  "costVariance": 0.00
}
```

---

#### Step 8: Update Actual Quantity
```http
PUT /clinical-records/procedures/200/materials
Authorization: Bearer <token>
Content-Type: application/json

{
  "materials": [
    {
      "usageId": 5002,
      "actualQuantity": 10.0,
      "varianceReason": "ADDITIONAL_USAGE",
      "notes": "Sâu răng sâu hơn dự kiến, cần thêm 2g composite"
    }
  ]
}
```

**Expected Response:**
```json
{
  "message": "Cập nhật số lượng vật tư thành công",
  "procedureId": 200,
  "materialsUpdated": 1,
  "stockAdjustments": [
    {
      "itemName": "Trám Composite",
      "adjustment": 2.0,
      "reason": "Sử dụng thêm"
    }
  ]
}
```

**🔍 Verify Warehouse Adjusted:**
```sql
SELECT lot_number, quantity_on_hand 
FROM item_batches 
WHERE item_master_id = (
  SELECT item_master_id FROM item_masters WHERE item_code = 'MAT-COMP-01'
);

-- Expected: quantity_on_hand giảm thêm 2
```

**🔍 Verify Usage Updated:**
```sql
SELECT 
  actual_quantity, 
  variance_quantity, 
  variance_reason 
FROM procedure_material_usage 
WHERE usage_id = 5002;

-- Expected:
--   actual_quantity = 10.00
--   variance_quantity = 2.00 (auto calculated)
--   variance_reason = 'ADDITIONAL_USAGE'
```

---

### ✅ SCENARIO 2: Test FEFO (First Expired First Out)

#### Setup: Kiểm tra batches hiện tại
```sql
SELECT 
  lot_number, 
  quantity_on_hand, 
  expiry_date 
FROM item_batches 
WHERE item_master_id = (
  SELECT item_master_id FROM item_masters WHERE item_code = 'CON-GLOVE-01'
)
ORDER BY expiry_date NULLS LAST;

-- Expected:
-- BATCH-GLOVE-2023-012 | 30  | 2026-01-15 (expires in 20 days)
-- BATCH-GLOVE-2024-001 | 150 | 2026-03-26 (expires in 90 days)
```

#### Test: Complete appointment cần 1 đôi găng tay
```http
# Steps giống Scenario 1, dùng service_id = 1 (GEN_EXAM)
# Service này chỉ cần 1 đôi găng tay
```

**🔍 Verify FEFO:**
```sql
SELECT 
  lot_number, 
  quantity_on_hand 
FROM item_batches 
WHERE item_master_id = (
  SELECT item_master_id FROM item_masters WHERE item_code = 'CON-GLOVE-01'
)
ORDER BY expiry_date;

-- Expected:
-- BATCH-GLOVE-2023-012 | 29  | ... (GIẢM 1)
-- BATCH-GLOVE-2024-001 | 150 | ... (KHÔNG ĐỔI)
```

**✅ Result:** Batch hết hạn sớm nhất được dùng trước!

---

### ✅ SCENARIO 3: Insufficient Stock

#### Setup: Set stock về 0
```sql
UPDATE item_batches 
SET quantity_on_hand = 0 
WHERE item_master_id = (
  SELECT item_master_id FROM item_masters WHERE item_code = 'MAT-COMP-01'
);
```

#### Test: Complete appointment cần composite
```http
# Steps giống Scenario 1
PUT /appointments/{id}/status
{ "newStatus": "COMPLETED" }
```

**Expected Behavior:**
- ✅ Appointment vẫn chuyển sang COMPLETED
- ❌ Materials KHÔNG được deduct (materials_deducted_at = NULL)
- 📋 Log error: "Insufficient stock for MAT-COMP-01"

**🔍 Verify:**
```sql
SELECT materials_deducted_at 
FROM clinical_record_procedures 
WHERE procedure_id = 200;

-- Expected: NULL
```

**Check Logs:**
```
ERROR: Failed to deduct materials for procedure 200: 
       Insufficient stock for item 504. Needed: 8, Available: 0
```

#### Cleanup: Reset stock
```sql
UPDATE item_batches 
SET quantity_on_hand = 35 
WHERE lot_number = 'BATCH-COMP-2024-001';
```

---

### ✅ SCENARIO 4: Permission Testing

#### Test 1: Doctor (NO cost visibility)
```http
# Login as doctor
POST /auth/login
{ "username": "dr.nguyen", "password": "password123" }

# View materials
GET /clinical-records/procedures/200/materials
Authorization: Bearer <doctor_token>
```

**Expected Response:**
```json
{
  "procedureId": 200,
  "materials": [
    {
      "itemName": "Găng tay y tế",
      "plannedQuantity": 1.00,
      "unitPrice": null,          // ❌ NULL (no permission)
      "totalPlannedCost": null,   // ❌ NULL
      "totalActualCost": null     // ❌ NULL
    }
  ],
  "totalPlannedCost": null,       // ❌ NULL
  "totalActualCost": null         // ❌ NULL
}
```

---

#### Test 2: Accountant (WITH cost visibility)
```http
# Login as accountant
POST /auth/login
{ "username": "accountant.minh", "password": "password123" }

# View materials
GET /clinical-records/procedures/200/materials
Authorization: Bearer <accountant_token>
```

**Expected Response:**
```json
{
  "materials": [
    {
      "itemName": "Găng tay y tế",
      "unitPrice": 150000.00,      // ✅ Visible
      "totalPlannedCost": 150000.00
    }
  ],
  "totalPlannedCost": 4500000.00   // ✅ Visible
}
```

---

#### Test 3: Nurse (CAN update, NO cost)
```http
# Login as nurse
POST /auth/login
{ "username": "nurse.lan", "password": "password123" }

# Update materials (should succeed)
PUT /clinical-records/procedures/200/materials
Authorization: Bearer <nurse_token>

{
  "materials": [
    {
      "usageId": 5001,
      "actualQuantity": 2.0,
      "varianceReason": "ADDITIONAL_USAGE"
    }
  ]
}
```

**Expected Response:**
```json
{
  "message": "Cập nhật số lượng vật tư thành công",
  "materialsUpdated": 1
}
```

**✅ Nurse CAN update quantities**

---

### ✅ SCENARIO 5: View Service BOM

#### Request
```http
GET /warehouse/service-consumables/5
Authorization: Bearer <token>
```

**Expected Response:**
```json
{
  "serviceId": 5,
  "serviceCode": "FILLING_COMP",
  "serviceName": "Trám răng Composite",
  "consumables": [
    {
      "itemMasterId": 501,
      "itemCode": "CON-GLOVE-01",
      "itemName": "Găng tay y tế",
      "quantityRequired": 1.00,
      "unitName": "Đôi",
      "unitPrice": 150000.00,
      "totalCost": 150000.00,
      "stockStatus": "OK",
      "currentStock": 179
    },
    {
      "itemMasterId": 504,
      "itemCode": "MAT-COMP-01",
      "itemName": "Trám Composite",
      "quantityRequired": 8.00,
      "unitName": "g",
      "unitPrice": 500000.00,
      "totalCost": 4000000.00,
      "stockStatus": "LOW",
      "currentStock": 27
    }
  ],
  "totalConsumableCost": 4500000.00,
  "hasInsufficientStock": false
}
```

---

## 📊 Verification Queries

### Query 1: Check All Materials for Procedure
```sql
SELECT 
  pmu.usage_id,
  im.item_code,
  im.item_name,
  pmu.planned_quantity,
  pmu.actual_quantity,
  pmu.variance_quantity,
  pmu.variance_reason,
  u.unit_name,
  pmu.recorded_by
FROM procedure_material_usage pmu
JOIN item_masters im ON pmu.item_master_id = im.item_master_id
JOIN item_units u ON pmu.unit_id = u.unit_id
WHERE pmu.procedure_id = 200
ORDER BY im.item_code;
```

---

### Query 2: Check Warehouse Stock Changes
```sql
SELECT 
  im.item_code,
  im.item_name,
  ib.lot_number,
  ib.quantity_on_hand,
  ib.initial_quantity,
  (ib.initial_quantity - ib.quantity_on_hand) as used_quantity
FROM item_batches ib
JOIN item_masters im ON ib.item_master_id = im.item_master_id
WHERE im.item_code IN ('CON-GLOVE-01', 'MAT-COMP-01', 'CON-MASK-01')
ORDER BY im.item_code, ib.expiry_date;
```

---

### Query 3: Check Recent Deductions
```sql
SELECT 
  p.procedure_id,
  p.tooth_number,
  s.service_name,
  p.materials_deducted_at,
  p.materials_deducted_by,
  COUNT(pmu.usage_id) as material_count
FROM clinical_record_procedures p
JOIN services s ON p.service_id = s.service_id
LEFT JOIN procedure_material_usage pmu ON p.procedure_id = pmu.procedure_id
WHERE p.materials_deducted_at IS NOT NULL
GROUP BY p.procedure_id, s.service_name
ORDER BY p.materials_deducted_at DESC
LIMIT 10;
```

---

## 🐛 Common Issues & Solutions

### Issue 1: "No BOM defined for service"
**Cause:** Service chưa có BOM trong `service_consumables`

**Solution:**
```sql
-- Check if service has BOM
SELECT COUNT(*) FROM service_consumables WHERE service_id = 5;

-- If 0, add BOM manually (see 01_TEST_DATA_SETUP.md)
```

---

### Issue 2: Materials not deducted
**Check:**
1. Appointment status = COMPLETED?
2. Service has BOM?
3. Clinical record created?
4. Procedure added?
5. Check application logs for errors

**Verify:**
```sql
SELECT 
  a.status,
  cr.clinical_record_id,
  p.procedure_id,
  p.materials_deducted_at
FROM appointments a
LEFT JOIN clinical_records cr ON cr.appointment_id = a.appointment_id
LEFT JOIN clinical_record_procedures p ON p.clinical_record_id = cr.clinical_record_id
WHERE a.appointment_id = 150;
```

---

### Issue 3: 401 Unauthorized
**Cause:** Token expired or invalid

**Solution:**
```http
POST /auth/login
{ "username": "admin", "password": "admin123" }
```

Get new token and retry.

---

## 📚 Next Steps

- ➡️ Đọc `04_PERMISSIONS_GUIDE.md` - Chi tiết phân quyền
- ➡️ Đọc `05_SAMPLE_SCENARIOS.md` - Các tình huống phức tạp hơn
