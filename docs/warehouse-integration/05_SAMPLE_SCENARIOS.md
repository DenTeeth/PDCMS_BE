# 📋 Sample Scenarios - Các Tình Huống Thực Tế

## 🎯 Mục Đích
Các tình huống thực tế trong phòng khám nha khoa để test và hiểu rõ hơn về warehouse integration.

---

## 🟢 SCENARIO 1: Trám Răng Đơn Giản

### 📖 Tình Huống
Bệnh nhân Nguyễn Văn An đến khám, bác sĩ phát hiện sâu răng số 46, cần trám Composite.

### 👥 Nhân Vật
- **Bệnh nhân:** Nguyễn Văn An (Patient ID: 1)
- **Bác sĩ:** Dr. Khoa (Employee ID: 1)
- **Phòng:** Phòng khám 1 (Room ID: 1)
- **Dịch vụ:** Trám răng Composite (Service ID: 5)

### 📊 Dữ Liệu Mẫu

#### BOM của dịch vụ
```
Trám răng Composite (FILLING_COMP):
├─ 1 đôi găng tay
├─ 1 cái khẩu trang
├─ 2 gói gạc
├─ 8g Composite
├─ 3ml Etching Gel
└─ 5 giọt Bonding Agent
```

#### Tồn kho trước khi điều trị
```
Composite (MAT-COMP-01):
├─ Batch 1: 35g (expires 200 days)
└─ Total: 35g

Găng tay (CON-GLOVE-01):
├─ Batch 1: 30 đôi (expires 20 days)
├─ Batch 2: 150 đôi (expires 90 days)
└─ Total: 180 đôi
```

### 🔄 Luồng Xử Lý

#### Bước 1: Lễ tân tạo appointment
```http
POST /api/v1/appointments
{
  "patientId": 1,
  "serviceId": 5,
  "employeeId": 1,
  "roomId": 1,
  "appointmentStartTime": "2025-12-27T10:00:00",
  "notes": "Sâu răng số 46"
}
```

**Kho:** 💤 Không thay đổi

---

#### Bước 2: Bệnh nhân check-in
```http
PUT /api/v1/appointments/{id}/status
{ "newStatus": "CHECKED_IN" }
```

**Kho:** 💤 Không thay đổi

---

#### Bước 3: Bác sĩ bắt đầu điều trị
```http
PUT /api/v1/appointments/{id}/status
{ "newStatus": "IN_PROGRESS" }
```

**Kho:** 💤 Không thay đổi

---

#### Bước 4: Bác sĩ tạo clinical record
```http
POST /api/v1/appointments/clinical-records
{
  "appointmentId": 150,
  "chiefComplaint": "Đau răng khi ăn đồ ngọt",
  "diagnosis": "Sâu răng răng số 46 mức độ trung bình",
  "treatmentPlan": "Trám răng Composite"
}
```

**Kho:** 💤 Không thay đổi

---

#### Bước 5: Bác sĩ thêm procedure
```http
POST /api/v1/clinical-records/75/procedures
{
  "serviceId": 5,
  "toothNumber": "46",
  "procedureDescription": "Trám răng Composite răng số 46"
}
```

**Database:**
```sql
INSERT INTO clinical_record_procedures (
  procedure_id: 200,
  service_id: 5,
  tooth_number: '46',
  materials_deducted_at: NULL  -- ⚠️ Chưa trừ kho
)
```

**Kho:** 💤 Không thay đổi

---

#### Bước 6: ⚡ Hoàn thành điều trị
```http
PUT /api/v1/appointments/{id}/status
{ "newStatus": "COMPLETED" }
```

**Backend Process:**
```
1. Detect status change → COMPLETED
2. Get procedures (procedure_id = 200)
3. Get BOM for service_id = 5
4. Deduct materials using FEFO:
   
   Găng tay (1 đôi):
   ├─ Batch 1 (expires 20 days): 30 → 29 ✅
   └─ Batch 2 (không dùng)
   
   Composite (8g):
   └─ Batch 1: 35 → 27 ✅
   
5. Create 6 usage records
6. Update procedure.materials_deducted_at
```

**Database Changes:**
```sql
-- Update batches
UPDATE item_batches SET quantity_on_hand = 29 WHERE lot_number = 'BATCH-GLOVE-2023-012';
UPDATE item_batches SET quantity_on_hand = 27 WHERE lot_number = 'BATCH-COMP-2024-001';

-- Create usage records
INSERT INTO procedure_material_usage VALUES
  (5001, 200, 501, 1.00, 1.00, ...),  -- Găng tay
  (5002, 200, 504, 8.00, 8.00, ...),  -- Composite
  ... (4 more)

-- Update procedure
UPDATE clinical_record_procedures 
SET materials_deducted_at = NOW(), materials_deducted_by = 'dr.khoa'
WHERE procedure_id = 200;
```

**Kho:** ✅ Đã trừ vật tư!

---

#### Bước 7: Y tá kiểm tra lại số lượng
Y tá Lan nhận ra: "Thực tế dùng 10g composite, không phải 8g"

```http
PUT /api/v1/clinical-records/procedures/200/materials
Authorization: Bearer <nurse_token>

{
  "materials": [
    {
      "usageId": 5002,
      "actualQuantity": 10.0,
      "varianceReason": "ADDITIONAL_USAGE",
      "notes": "Sâu răng sâu hơn dự kiến, cần thêm 2g"
    }
  ]
}
```

**Database Changes:**
```sql
-- Trừ thêm 2g từ kho
UPDATE item_batches SET quantity_on_hand = 25 WHERE lot_number = 'BATCH-COMP-2024-001';

-- Update usage record
UPDATE procedure_material_usage 
SET 
  actual_quantity = 10.0,
  variance_quantity = 2.0,  -- Auto calculated
  variance_reason = 'ADDITIONAL_USAGE',
  notes = 'Sâu răng sâu hơn dự kiến'
WHERE usage_id = 5002;
```

**Kho:** ✅ Trừ thêm 2g composite

---

### 📊 Kết Quả Cuối Cùng

#### Tồn kho sau điều trị
```
Composite: 35g → 25g (-10g)
Găng tay Batch 1: 30 → 29 đôi (-1 đôi)
Găng tay Batch 2: 150 đôi (không đổi)
```

#### Báo cáo vật tư
```http
GET /api/v1/clinical-records/procedures/200/materials
```

```json
{
  "procedureId": 200,
  "materials": [
    {
      "itemName": "Trám Composite",
      "plannedQuantity": 8.00,
      "actualQuantity": 10.00,
      "varianceQuantity": 2.00,
      "varianceReason": "ADDITIONAL_USAGE"
    }
  ],
  "totalPlannedCost": 4500000,
  "totalActualCost": 4700000,
  "costVariance": 200000
}
```

---

## 🔴 SCENARIO 2: Thiếu Vật Tư Trong Kho

### 📖 Tình Huống
Bệnh nhân cần trám răng nhưng kho hết composite.

### 🔄 Luồng Xử Lý

#### Trạng thái kho
```sql
-- Composite hết hàng
UPDATE item_batches 
SET quantity_on_hand = 0 
WHERE item_code = 'MAT-COMP-01';
```

#### Thử complete appointment
```http
PUT /api/v1/appointments/{id}/status
{ "newStatus": "COMPLETED" }
```

#### Backend Process
```
1. Detect COMPLETED status
2. Get BOM: cần 8g composite
3. Get batches: FEFO query
   └─ All batches have quantity_on_hand = 0
4. ❌ Throw InsufficientStockException
5. Log error
6. materials_deducted_at = NULL
```

#### Log Output
```
ERROR: Failed to deduct materials for procedure 200
Insufficient stock for item MAT-COMP-01 (Trám Composite)
Needed: 8g, Available: 0g
```

### 📊 Kết Quả

**Appointment:**
- ✅ Status = COMPLETED (vẫn complete được!)
- ❌ materials_deducted_at = NULL

**Procedure:**
- ❌ Không có usage records
- ⚠️ Cần nhập vật tư và deduct manual sau

### 🔧 Giải Pháp

#### Option 1: Nhập vật tư và retry
```sql
-- 1. Nhập vật tư vào kho
INSERT INTO item_batches (...) VALUES (...);

-- 2. Manually deduct materials
-- Call: POST /api/v1/clinical-records/procedures/{id}/deduct-materials
```

#### Option 2: Ghi nhận thiếu vật tư
```http
POST /api/v1/warehouse/shortage-reports
{
  "procedureId": 200,
  "itemMasterId": 504,
  "shortageQuantity": 8.0,
  "notes": "Thiếu composite khi điều trị"
}
```

---

## 🟡 SCENARIO 3: Đa Procedure Trong 1 Appointment

### 📖 Tình Huống
Bệnh nhân cần:
1. Lấy cao răng (SCALING_L1)
2. Trám 2 răng (FILLING_COMP × 2)

### 🔄 Luồng Xử Lý

#### Tạo clinical record với 3 procedures
```http
POST /api/v1/clinical-records/75/procedures
{ "serviceId": 3, "toothNumber": "ALL" }  -- Lấy cao răng

POST /api/v1/clinical-records/75/procedures
{ "serviceId": 5, "toothNumber": "16" }   -- Trám răng 1

POST /api/v1/clinical-records/75/procedures
{ "serviceId": 5, "toothNumber": "26" }   -- Trám răng 2
```

**Database:**
```sql
INSERT INTO clinical_record_procedures VALUES
  (201, 75, 3, 'ALL', ...),   -- Scaling
  (202, 75, 5, '16', ...),    -- Filling 1
  (203, 75, 5, '26', ...);    -- Filling 2
```

#### Complete appointment
```http
PUT /api/v1/appointments/{id}/status
{ "newStatus": "COMPLETED" }
```

#### Backend Process
```
For each procedure in clinical_record:
  
  Procedure 201 (Scaling):
  ├─ Get BOM: 2 găng tay, 1 khẩu trang, 3 gạc, 15g sò đánh bóng
  ├─ Deduct using FEFO
  └─ Create usage records
  
  Procedure 202 (Filling răng 16):
  ├─ Get BOM: 1 găng tay, 1 khẩu trang, 8g composite, ...
  ├─ Deduct using FEFO
  └─ Create usage records
  
  Procedure 203 (Filling răng 26):
  ├─ Get BOM: 1 găng tay, 1 khẩu trang, 8g composite, ...
  ├─ Deduct using FEFO
  └─ Create usage records
```

### 📊 Tổng Vật Tư Sử Dụng

```
Găng tay: 2 + 1 + 1 = 4 đôi
Khẩu trang: 1 + 1 + 1 = 3 cái
Composite: 0 + 8 + 8 = 16g
Gạc: 3 + 2 + 2 = 7 gói
```

### 🔍 Verify
```sql
SELECT 
  p.procedure_id,
  p.tooth_number,
  s.service_name,
  p.materials_deducted_at,
  COUNT(pmu.usage_id) as material_count
FROM clinical_record_procedures p
JOIN services s ON p.service_id = s.service_id
LEFT JOIN procedure_material_usage pmu ON p.procedure_id = pmu.procedure_id
WHERE p.clinical_record_id = 75
GROUP BY p.procedure_id, s.service_name;

-- Expected: 3 procedures, all deducted
```

---

## 🟣 SCENARIO 4: FEFO Depletion (Dùng Hết Batch)

### 📖 Tình Huống
Dùng hết batch hết hạn sớm, tự động chuyển sang batch tiếp theo.

### 📊 Dữ Liệu

#### Tồn kho
```
Găng tay:
├─ Batch A: 2 đôi (expires in 10 days)
├─ Batch B: 5 đôi (expires in 30 days)
└─ Batch C: 100 đôi (expires in 90 days)
```

#### Cần dùng
```
Service cần: 3 đôi găng tay
```

### 🔄 FEFO Algorithm

```
Remaining to deduct: 3

Batch A (expires soonest):
├─ Available: 2
├─ Deduct: min(2, 3) = 2
├─ New quantity: 2 - 2 = 0
└─ Remaining: 3 - 2 = 1

Batch B (expires next):
├─ Available: 5
├─ Deduct: min(5, 1) = 1
├─ New quantity: 5 - 1 = 4
└─ Remaining: 1 - 1 = 0

DONE! ✅
```

### 📊 Kết Quả

```sql
SELECT lot_number, quantity_on_hand FROM item_batches 
WHERE item_code = 'CON-GLOVE-01'
ORDER BY expiry_date;

-- Before:
-- Batch A: 2
-- Batch B: 5
-- Batch C: 100

-- After:
-- Batch A: 0  ✅ (depleted)
-- Batch B: 4  ✅ (partially used)
-- Batch C: 100 (not touched)
```

---

## 🔵 SCENARIO 5: Negative Variance (Dùng Ít Hơn Dự Kiến)

### 📖 Tình Huống
Planned: 3 K-files, Actual: 2 K-files (rơi mất 1 cái)

### 🔄 Luồng

#### Ban đầu (Complete appointment)
```
Planned: 3 K-files
Actual: 3 K-files (mặc định = planned)
Warehouse: -3 K-files
```

#### Y tá cập nhật
```http
PUT /api/v1/clinical-records/procedures/200/materials
{
  "materials": [
    {
      "usageId": 5003,
      "actualQuantity": 2.0,
      "varianceReason": "LESS_THAN_PLANNED",
      "notes": "Rơi mất 1 cái khi sử dụng"
    }
  ]
}
```

#### Backend Logic
```java
BigDecimal difference = 2.0 - 3.0 = -1.0  // Negative!

if (difference < 0) {
    // Return to warehouse
    ItemBatch newestBatch = getNewestBatch(itemId);
    newestBatch.setQuantityOnHand(
        newestBatch.getQuantityOnHand() + Math.abs(difference)
    );
}
```

### 📊 Kết Quả

**Warehouse:**
```
K-files: 100 → 97 → 98
(Trừ 3, sau đó +1 trả lại)
```

**Usage Record:**
```sql
SELECT 
  planned_quantity,   -- 3.00
  actual_quantity,    -- 2.00
  variance_quantity   -- -1.00 (auto calculated)
FROM procedure_material_usage 
WHERE usage_id = 5003;
```

---

## 🧪 Test Matrix

| Scenario | Service | Expected Behavior | Verification |
|----------|---------|------------------|--------------|
| 1. Happy Path | FILLING_COMP | Materials deducted | `materials_deducted_at != NULL` |
| 2. No Stock | FILLING_COMP | Error logged, not deducted | `materials_deducted_at = NULL` |
| 3. Multi Procedure | Multiple | All procedures deducted | Count(deducted) = 3 |
| 4. FEFO Depletion | GEN_EXAM | Batch A depleted first | `Batch A = 0` |
| 5. Negative Variance | ROOT_CANAL | Return to warehouse | Stock increased |

---

## 📚 Next Steps

- ➡️ Test các scenario này trên môi trường dev
- ➡️ Ghi nhận bugs nếu có
- ➡️ Đọc `PROCEDURE_MATERIAL_CONSUMPTION_API_GUIDE.md` cho API đầy đủ
