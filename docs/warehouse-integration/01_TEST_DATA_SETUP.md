# 📝 Test Data Setup - Dữ Liệu Mẫu Để Test

## 🎯 Mục Đích
File này cung cấp **dữ liệu mẫu có SẴN** trong database seed để test warehouse integration.

---

## 📦 Dữ Liệu Đã Có Sẵn

### 1. 🏥 Dịch Vụ (Services) Đã Có BOM

#### Service #1: Khám Tổng Quát (GEN_EXAM)
```sql
service_code: 'GEN_EXAM'
service_id: 1
```

**Vật tư cần thiết:**
- 1 Đôi găng tay (`CON-GLOVE-01`)
- 1 Cái khẩu trang (`CON-MASK-01`)

---

#### Service #3: Lấy Cao Răng Mức 1 (SCALING_L1)
```sql
service_code: 'SCALING_L1'
service_id: 3
```

**Vật tư cần thiết:**
- 2 Đôi găng tay (`CON-GLOVE-01`)
- 1 Cái khẩu trang (`CON-MASK-01`)
- 3 Gói gạc (`CON-GAUZE-01`)
- 15g Sò đánh bóng (`MAT-POL-01`)

---

#### Service #5: Trám Răng Composite (FILLING_COMP)
```sql
service_code: 'FILLING_COMP'
service_id: 5
```

**Vật tư cần thiết (Đây là service HAY DÙNG NHẤT cho test!):**
- 1 Đôi găng tay (`CON-GLOVE-01`)
- 1 Cái khẩu trang (`CON-MASK-01`)
- 2 Gói gạc (`CON-GAUZE-01`)
- 8g Composite (`MAT-COMP-01`)
- 3ml Etching Gel (`MAT-ETCH-01`)
- 5 giọt Bonding Agent (`MAT-BOND-01`)

---

#### Service #8: Nhổ Răng Sữa (EXTRACT_MILK)
```sql
service_code: 'EXTRACT_MILK'
service_id: 8
```

**Vật tư cần thiết:**
- 1 Đôi găng tay (`CON-GLOVE-01`)
- 5 Gói gạc (`CON-GAUZE-01`)
- 1g Gel tê bôi (`MED-GEL-01`)

---

### 2. 📦 Vật Tư Trong Kho (Item Batches)

#### Găng Tay Y Tế (CON-GLOVE-01)
```
Batch 1: BATCH-GLOVE-2024-001
├─ Số lượng: 150 đôi
├─ Hết hạn: 90 ngày nữa (2026-03-26)
└─ Vị trí: Kệ A-01

Batch 2: BATCH-GLOVE-2023-012
├─ Số lượng: 30 đôi
├─ Hết hạn: 20 ngày nữa (2026-01-15)
└─ Vị trí: Kệ A-02
```

**→ FEFO sẽ dùng Batch 2 trước (hết hạn sớm hơn)**

---

#### Khẩu Trang Y Tế (CON-MASK-01)
```
Batch: BATCH-MASK-2024-001
├─ Số lượng: 800 cái
├─ Hết hạn: 120 ngày nữa (2026-04-25)
└─ Vị trí: Kệ A-03
```

---

#### Composite (MAT-COMP-01)
```
Batch 1: BATCH-COMP-2024-001
├─ Số lượng: 35g
├─ Hết hạn: 200 ngày nữa (2026-07-14)
└─ Vị trí: Kệ C-01

Batch 2: BATCH-COMP-2022-005 (ĐÃ HẾT HẠN - EXPIRED)
├─ Số lượng: 0g
├─ Hết hạn: -10 ngày (2025-12-16)
└─ Vị trí: Kệ C-05 (HẾT HẠN)
```

---

#### Bông Gạc (CON-GAUZE-01)
```
Batch: BATCH-GAUZE-2024-001
├─ Số lượng: 280 gói
├─ Hết hạn: 180 ngày nữa (2026-06-24)
└─ Vị trí: Kệ B-02
```

---

#### Bonding Agent (MAT-BOND-01)
```
Batch: BATCH-BOND-2024-001
├─ Số lượng: 45ml (≈ 900 giọt)
├─ Hết hạn: 220 ngày nữa (2026-08-03)
└─ Vị trí: Kệ C-02
```

---

#### Etching Gel (MAT-ETCH-01)
```
Batch: (Seed data chưa có - cần thêm!)
```

⚠️ **LƯU Ý**: Cần thêm batch cho MAT-ETCH-01 để test service FILLING_COMP hoàn chỉnh!

---

### 3. 👥 Users & Permissions

#### Admin (Xem tất cả, bao gồm giá)
```
username: admin
permissions: 
  - VIEW_CLINICAL_RECORD ✅
  - WRITE_CLINICAL_RECORD ✅
  - VIEW_WAREHOUSE_COST ✅
  - MANAGE_WAREHOUSE ✅
```

#### Doctor (Xem vật tư, KHÔNG xem giá)
```
username: dr.nguyen / dr.thai
permissions:
  - VIEW_CLINICAL_RECORD ✅
  - WRITE_CLINICAL_RECORD ✅
  - VIEW_WAREHOUSE_COST ❌
```

#### Nurse (Cập nhật số lượng, KHÔNG xem giá)
```
username: nurse.lan
permissions:
  - VIEW_CLINICAL_RECORD ✅
  - WRITE_CLINICAL_RECORD ✅
  - VIEW_WAREHOUSE_COST ❌
```

#### Accountant (Xem giá, KHÔNG cập nhật)
```
username: accountant.minh
permissions:
  - VIEW_CLINICAL_RECORD ✅
  - WRITE_CLINICAL_RECORD ❌
  - VIEW_WAREHOUSE_COST ✅
```

---

## 🧪 Scenario Test Mẫu

### Scenario 1: Trám Răng (HAPPY PATH)

**Dữ liệu input:**
```json
{
  "patientId": 1,
  "serviceId": 5,
  "employeeId": 1,
  "roomId": 1,
  "appointmentStartTime": "2025-12-27T10:00:00"
}
```

**Kết quả mong đợi:**
- Sau khi COMPLETE, kho trừ:
  - 1 đôi găng tay
  - 1 cái khẩu trang
  - 2 gói gạc
  - 8g composite
  - 5 giọt (0.25ml) bonding agent

---

### Scenario 2: Test FEFO

**Setup:**
1. Dùng dịch vụ cần găng tay (GEN_EXAM)
2. Complete appointment

**Kết quả mong đợi:**
- Trừ từ `BATCH-GLOVE-2023-012` trước (hết hạn 20 ngày)
- KHÔNG trừ từ `BATCH-GLOVE-2024-001` (hết hạn 90 ngày)

**Verify:**
```sql
SELECT lot_number, quantity_on_hand, expiry_date
FROM item_batches
WHERE item_master_id = (
  SELECT item_master_id FROM item_masters WHERE item_code = 'CON-GLOVE-01'
)
ORDER BY expiry_date;
```

---

### Scenario 3: Thiếu Vật Tư

**Setup:**
1. Update kho composite về 0:
```sql
UPDATE item_batches 
SET quantity_on_hand = 0 
WHERE lot_number = 'BATCH-COMP-2024-001';
```

2. Thử complete appointment với service FILLING_COMP

**Kết quả mong đợi:**
- Báo lỗi: "Insufficient stock for MAT-COMP-01"
- Appointment vẫn COMPLETE được
- Procedure có `materials_deducted_at = NULL`

---

### Scenario 4: Cập Nhật Số Lượng Thực Tế

**Setup:**
1. Complete appointment với FILLING_COMP
2. Planned quantity: 8g composite
3. Actual usage: 10g composite (dùng thêm 2g)

**API Call:**
```http
PUT /api/v1/clinical-records/procedures/{procedureId}/materials
```

```json
{
  "materials": [
    {
      "usageId": 1001,
      "actualQuantity": 10.0,
      "varianceReason": "ADDITIONAL_USAGE",
      "notes": "Sâu răng sâu hơn dự kiến"
    }
  ]
}
```

**Kết quả mong đợi:**
- Kho trừ thêm 2g composite
- `variance_quantity = +2.0`
- Stock adjustment logged

---

## 🔧 Script Thêm Dữ Liệu Thiếu

### Thêm Batch cho Etching Gel
```sql
INSERT INTO item_batches (
  item_master_id, 
  lot_number, 
  quantity_on_hand, 
  initial_quantity, 
  expiry_date, 
  supplier_id, 
  imported_at, 
  bin_location, 
  created_at
)
SELECT 
  im.item_master_id,
  'BATCH-ETCH-2024-001',
  500,  -- 500ml
  500,
  CURRENT_DATE + INTERVAL '150 days',
  3,
  NOW() - INTERVAL '10 days',
  'Kệ C-03',
  NOW()
FROM item_masters im 
WHERE im.item_code = 'MAT-ETCH-01';
```

### Thêm BOM cho Dịch Vụ Mới
```sql
-- Ví dụ: Thêm BOM cho service "Root Canal Treatment"
INSERT INTO service_consumables (
  service_id, 
  item_master_id, 
  quantity_per_service, 
  unit_id, 
  notes
)
SELECT 
  s.service_id,
  im.item_master_id,
  2,  -- 2 ống
  u.unit_id,
  'Gây tê trong điều trị tủy'
FROM services s
CROSS JOIN item_masters im
CROSS JOIN item_units u
WHERE s.service_code = 'ROOT_CANAL'
  AND im.item_code = 'MED-SEPT-01'
  AND u.item_master_id = im.item_master_id
  AND u.unit_name = 'Ống';
```

---

## 📊 Queries Kiểm Tra Dữ Liệu

### 1. Xem BOM của tất cả dịch vụ
```sql
SELECT 
  s.service_code,
  s.service_name,
  im.item_code,
  im.item_name,
  sc.quantity_per_service,
  u.unit_name
FROM service_consumables sc
JOIN services s ON sc.service_id = s.service_id
JOIN item_masters im ON sc.item_master_id = im.item_master_id
JOIN item_units u ON sc.unit_id = u.unit_id
ORDER BY s.service_code, im.item_code;
```

### 2. Xem tồn kho tất cả vật tư
```sql
SELECT 
  im.item_code,
  im.item_name,
  ib.lot_number,
  ib.quantity_on_hand,
  ib.expiry_date,
  ib.bin_location,
  CASE 
    WHEN ib.expiry_date < CURRENT_DATE THEN '❌ HẾT HẠN'
    WHEN ib.expiry_date < CURRENT_DATE + INTERVAL '30 days' THEN '⚠️ GẦN HẾT HẠN'
    ELSE '✅ CÒN HẠN'
  END as status
FROM item_batches ib
JOIN item_masters im ON ib.item_master_id = im.item_master_id
ORDER BY im.item_code, ib.expiry_date;
```

### 3. Xem procedure đã deduct materials
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
GROUP BY p.procedure_id, p.tooth_number, s.service_name, p.materials_deducted_at, p.materials_deducted_by
ORDER BY p.materials_deducted_at DESC;
```

---

## ✅ Checklist Trước Khi Test

- [ ] Database đã chạy seed script (`dental-clinic-seed-data.sql`)
- [ ] Có ít nhất 1 service có BOM (recommend: FILLING_COMP)
- [ ] Có ít nhất 2 batch cho cùng 1 item (để test FEFO)
- [ ] User test có đúng permissions
- [ ] Kho có đủ vật tư (quantity_on_hand > 0)

---

## 🆘 Troubleshooting

**Q: Không thấy dữ liệu?**
```sql
-- Check seed script đã chạy chưa
SELECT COUNT(*) FROM service_consumables;
-- Kết quả phải > 0

SELECT COUNT(*) FROM item_batches;
-- Kết quả phải > 0
```

**Q: Service không có BOM?**
```sql
-- List services có BOM
SELECT DISTINCT s.service_code, s.service_name
FROM services s
JOIN service_consumables sc ON s.service_id = sc.service_id;
```

**Q: Kho bị âm (negative stock)?**
```sql
-- Reset batch về trạng thái ban đầu
UPDATE item_batches 
SET quantity_on_hand = initial_quantity
WHERE lot_number = 'BATCH-GLOVE-2024-001';
```

---

## 📚 Next Steps

Sau khi hiểu dữ liệu test:
- ➡️ Đọc `02_DATA_FLOW_EXPLAINED.md` - Hiểu luồng xử lý
- ➡️ Đọc `03_API_TESTING_GUIDE.md` - Test API từng bước
