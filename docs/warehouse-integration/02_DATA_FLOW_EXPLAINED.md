# 🔄 Data Flow Explained - Luồng Dữ Liệu Chi Tiết

## 🎯 Mục Đích
Giải thích **TỪNG BƯỚC** dữ liệu chạy qua hệ thống từ khi tạo appointment đến khi vật tư được trừ khỏi kho.

---

## 📊 Sơ Đồ Tổng Quan

```
┌─────────────────────────────────────────────────────────────────┐
│                   APPOINTMENT LIFECYCLE                          │
└─────────────────────────────────────────────────────────────────┘

1. SCHEDULED          2. CHECKED_IN         3. IN_PROGRESS
   └─ Tạo appointment    └─ Patient đến        └─ Doctor điều trị
   └─ Warehouse: 💤      └─ Warehouse: 💤      └─ Warehouse: 💤
   
                    ┌──────────────────────────────────────┐
                    │  4. COMPLETED ⚡ (TRIGGER POINT)     │
                    └──────────────────────────────────────┘
                                    │
            ┌───────────────────────┴───────────────────────┐
            │   AppointmentStatusService.updateStatus()     │
            │   ├─ Detect: oldStatus != COMPLETED           │
            │   └─ Detect: newStatus == COMPLETED           │
            └───────────────────────┬───────────────────────┘
                                    │
                                    ▼
            ┌──────────────────────────────────────────────┐
            │ ClinicalRecordService                        │
            │   .deductMaterialsForAppointment(appointId)  │
            └───────────────────────┬──────────────────────┘
                                    │
                                    ▼
            ┌──────────────────────────────────────────────┐
            │ Get all procedures in clinical record        │
            │ WHERE clinical_record.appointment_id = ?     │
            └───────────────────────┬──────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │   For EACH procedure:         │
                    └───────────────┬───────────────┘
                                    │
                                    ▼
            ┌──────────────────────────────────────────────┐
            │ ProcedureMaterialService                     │
            │   .deductMaterialsForProcedure(procedureId)  │
            └───────────────────────┬──────────────────────┘
                                    │
        ┌───────────────────────────┴────────────────────────────┐
        │                                                         │
        ▼                                                         ▼
┌──────────────────┐                                   ┌──────────────────┐
│ Check if already │                                   │ Get Service BOM  │
│ deducted?        │                                   │ from service_    │
│ materials_       │───── YES ───► SKIP                │ consumables      │
│ deducted_at      │                                   │                  │
│ != NULL?         │                                   │ WHERE service_id │
└──────────────────┘                                   └────────┬─────────┘
        │                                                       │
        NO                                                      ▼
        │                                           ┌────────────────────┐
        │                                           │ BOM empty?         │
        │                                           │ (no materials)     │
        │                                           └─────┬──────────────┘
        │                                                 │
        │                                           ┌─────┴─────┐
        │                                           │           │
        │                                          YES         NO
        │                                           │           │
        │                                        SKIP           │
        │                                                       │
        │                                                       ▼
        │                                           ┌────────────────────┐
        │                                           │ For EACH BOM item: │
        │                                           └─────┬──────────────┘
        │                                                 │
        │                                                 ▼
        │                                     ┌──────────────────────────┐
        │                                     │ Calculate quantity:      │
        │                                     │ qty = BOM qty ×          │
        │                                     │   quantity_multiplier    │
        │                                     └────────┬─────────────────┘
        │                                              │
        │                                              ▼
        │                                  ┌───────────────────────────┐
        │                                  │ deductFromWarehouse()     │
        │                                  │ (FEFO Algorithm)          │
        │                                  └─────┬─────────────────────┘
        │                                        │
        │        ┌───────────────────────────────┴───────────────────┐
        │        │                                                   │
        │        ▼                                                   ▼
        │  ┌──────────────────┐                          ┌──────────────────┐
        │  │ Get batches FEFO │                          │ Deduct from each │
        │  │ ORDER BY:        │                          │ batch (FEFO)     │
        │  │ 1. expiry_date   │                          │ until qty = 0    │
        │  │ 2. NULLS LAST    │                          └────────┬─────────┘
        │  └──────────────────┘                                   │
        │                                                          │
        │                                              ┌───────────┴──────────┐
        │                                              │                      │
        │                                              ▼                      ▼
        │                                    ┌──────────────┐      ┌──────────────┐
        │                                    │ Update       │      │ Insufficient │
        │                                    │ quantity_on_ │      │ stock?       │
        │                                    │ hand         │      │ → LOG ERROR  │
        │                                    └──────┬───────┘      └──────────────┘
        │                                           │
        │                                           ▼
        │                                 ┌──────────────────────┐
        │                                 │ Create usage record  │
        │                                 │ in procedure_        │
        │                                 │ material_usage:      │
        │                                 │ - planned_quantity   │
        │                                 │ - actual_quantity    │
        │                                 │ - item_master_id     │
        │                                 └──────┬───────────────┘
        │                                        │
        └────────────────────────────────────────┘
                                                 │
                                                 ▼
                                   ┌──────────────────────────┐
                                   │ Update procedure:        │
                                   │ - materials_deducted_at  │
                                   │ - materials_deducted_by  │
                                   │ - storage_transaction_id │
                                   └──────────────────────────┘
```

---

## 🔍 Chi Tiết Từng Bước

### BƯỚC 1: Tạo Appointment (SCHEDULED)

**Input:**
```json
POST /api/v1/appointments
{
  "patientId": 1,
  "serviceId": 5,
  "employeeId": 1,
  "roomId": 1,
  "appointmentStartTime": "2025-12-27T10:00:00"
}
```

**Database Changes:**
```sql
INSERT INTO appointments (
  appointment_id, patient_id, service_id, 
  employee_id, room_id, status, ...
) VALUES (
  100, 1, 5, 1, 1, 'SCHEDULED', ...
);
```

**Warehouse Impact:** ❌ NONE

---

### BƯỚC 2: Check-in (CHECKED_IN)

**API Call:**
```http
PUT /api/v1/appointments/100/status
{ "newStatus": "CHECKED_IN" }
```

**Database Changes:**
```sql
UPDATE appointments 
SET status = 'CHECKED_IN' 
WHERE appointment_id = 100;
```

**Warehouse Impact:** ❌ NONE

---

### BƯỚC 3: Bắt Đầu Điều Trị (IN_PROGRESS)

**API Call:**
```http
PUT /api/v1/appointments/100/status
{ "newStatus": "IN_PROGRESS" }
```

**Doctor tạo Clinical Record:**
```http
POST /api/v1/appointments/clinical-records
{
  "appointmentId": 100,
  "chiefComplaint": "Đau răng",
  "diagnosis": "Sâu răng"
}
```

**Database Changes:**
```sql
INSERT INTO clinical_records (
  clinical_record_id, appointment_id, ...
) VALUES (50, 100, ...);
```

**Doctor thêm Procedure:**
```http
POST /api/v1/clinical-records/50/procedures
{
  "serviceId": 5,
  "toothNumber": "46"
}
```

**Database Changes:**
```sql
INSERT INTO clinical_record_procedures (
  procedure_id, clinical_record_id, service_id, 
  tooth_number, materials_deducted_at
) VALUES (
  123, 50, 5, '46', NULL  -- ⚠️ NULL = chưa trừ kho
);
```

**Warehouse Impact:** ❌ NONE (vật tư chỉ được PLAN, chưa trừ!)

---

### BƯỚC 4: Hoàn Thành ⚡ (COMPLETED - TRIGGER POINT!)

**API Call:**
```http
PUT /api/v1/appointments/100/status
{ "newStatus": "COMPLETED" }
```

**Backend Code Flow:**

#### 4.1. AppointmentStatusService Detect Change
```java
// File: AppointmentStatusService.java
public void updateAppointmentStatus(Integer appointmentId, String newStatus) {
    Appointment appointment = appointmentRepository.findById(appointmentId)...;
    
    String oldStatus = appointment.getStatus();
    appointment.setStatus(newStatus);
    appointmentRepository.save(appointment);
    
    // ⚡ TRIGGER: Detect COMPLETED
    if ("COMPLETED".equals(newStatus) && !"COMPLETED".equals(oldStatus)) {
        // Call material deduction
        clinicalRecordService.deductMaterialsForAppointment(appointmentId);
    }
}
```

#### 4.2. ClinicalRecordService Get Procedures
```java
// File: ClinicalRecordService.java
public void deductMaterialsForAppointment(Integer appointmentId) {
    ClinicalRecord record = clinicalRecordRepository
        .findByAppointment_AppointmentId(appointmentId);
    
    if (record == null) {
        log.warn("No clinical record for appointment {}", appointmentId);
        return;
    }
    
    // Get all procedures
    List<ClinicalRecordProcedure> procedures = 
        procedureRepository.findByClinicalRecord_ClinicalRecordId(
            record.getClinicalRecordId()
        );
    
    // Deduct materials for EACH procedure
    for (ClinicalRecordProcedure procedure : procedures) {
        procedureMaterialService.deductMaterialsForProcedure(
            procedure.getProcedureId()
        );
    }
}
```

#### 4.3. ProcedureMaterialService - Main Logic
```java
// File: ProcedureMaterialService.java
public List<ProcedureMaterialUsage> deductMaterialsForProcedure(Integer procedureId) {
    // Step 1: Get procedure
    ClinicalRecordProcedure procedure = procedureRepository
        .findById(procedureId).orElseThrow();
    
    // Step 2: Check if already deducted
    if (procedure.getMaterialsDeductedAt() != null) {
        log.warn("Materials already deducted at {}", 
            procedure.getMaterialsDeductedAt());
        return materialUsageRepository
            .findByProcedure_ProcedureId(procedureId);
    }
    
    // Step 3: Get Service BOM
    Long serviceId = procedure.getService().getServiceId();
    List<ServiceConsumable> bom = serviceConsumableRepository
        .findByServiceIdWithDetails(serviceId);
    
    if (bom.isEmpty()) {
        log.info("No BOM for service {}, skip deduction", serviceId);
        return new ArrayList<>();
    }
    
    // Step 4: Get current user
    String username = getCurrentUsername();
    
    // Step 5: Deduct each material
    List<ProcedureMaterialUsage> usageRecords = new ArrayList<>();
    Integer multiplier = procedure.getQuantityMultiplier() != null 
        ? procedure.getQuantityMultiplier() : 1;
    
    for (ServiceConsumable bomItem : bom) {
        // Calculate total quantity
        BigDecimal plannedQty = bomItem.getQuantityPerService()
            .multiply(BigDecimal.valueOf(multiplier));
        
        // ⚡ DEDUCT FROM WAREHOUSE (FEFO)
        deductFromWarehouse(
            bomItem.getItemMaster().getItemMasterId(), 
            plannedQty
        );
        
        // Create usage record
        ProcedureMaterialUsage usage = ProcedureMaterialUsage.builder()
            .procedure(procedure)
            .itemMaster(bomItem.getItemMaster())
            .plannedQuantity(plannedQty)
            .actualQuantity(plannedQty)  // Initial = planned
            .unit(bomItem.getUnit())
            .recordedAt(LocalDateTime.now())
            .recordedBy(username)
            .build();
        
        usageRecords.add(materialUsageRepository.save(usage));
    }
    
    // Step 6: Update procedure
    procedure.setMaterialsDeductedAt(LocalDateTime.now());
    procedure.setMaterialsDeductedBy(username);
    procedureRepository.save(procedure);
    
    return usageRecords;
}
```

#### 4.4. FEFO Deduction Algorithm
```java
private void deductFromWarehouse(Long itemMasterId, BigDecimal quantity) {
    // Get batches ordered by expiry date (FEFO)
    List<ItemBatch> batches = itemBatchRepository
        .findByItemMasterIdAndQuantityGreaterThanOrderByExpiryAsc(
            itemMasterId, 0
        );
    
    int remainingToDeduct = quantity.intValue();
    
    for (ItemBatch batch : batches) {
        if (remainingToDeduct <= 0) break;
        
        int available = batch.getQuantityOnHand();
        int toDeduct = Math.min(available, remainingToDeduct);
        
        // Update batch quantity
        batch.setQuantityOnHand(available - toDeduct);
        itemBatchRepository.save(batch);
        
        remainingToDeduct -= toDeduct;
        
        log.info("Deducted {} from batch {} (remaining: {})", 
            toDeduct, batch.getLotNumber(), remainingToDeduct);
    }
    
    if (remainingToDeduct > 0) {
        throw new IllegalStateException(
            String.format("Insufficient stock for item %d. " +
                "Needed: %d, Available: %d", 
                itemMasterId, quantity.intValue(), 
                quantity.intValue() - remainingToDeduct)
        );
    }
}
```

**Database Changes:**
```sql
-- 1. Update batches (FEFO order)
UPDATE item_batches 
SET quantity_on_hand = quantity_on_hand - 1 
WHERE batch_id = 10 AND lot_number = 'BATCH-GLOVE-2023-012';
-- (Batch hết hạn sớm nhất)

UPDATE item_batches 
SET quantity_on_hand = quantity_on_hand - 8 
WHERE batch_id = 25 AND lot_number = 'BATCH-COMP-2024-001';

-- 2. Create usage records
INSERT INTO procedure_material_usage (
  procedure_id, item_master_id, 
  planned_quantity, actual_quantity,
  unit_id, recorded_at, recorded_by
) VALUES 
  (123, 501, 1.00, 1.00, 103, NOW(), 'dr.nguyen'),
  (123, 502, 1.00, 1.00, 104, NOW(), 'dr.nguyen'),
  (123, 503, 2.00, 2.00, 105, NOW(), 'dr.nguyen'),
  (123, 504, 8.00, 8.00, 106, NOW(), 'dr.nguyen');

-- 3. Update procedure
UPDATE clinical_record_procedures 
SET 
  materials_deducted_at = NOW(),
  materials_deducted_by = 'dr.nguyen'
WHERE procedure_id = 123;
```

**Warehouse Impact:** ✅ **MATERIALS DEDUCTED!**

---

### BƯỚC 5: Xem Vật Tư Đã Dùng

**API Call:**
```http
GET /api/v1/clinical-records/procedures/123/materials
```

**SQL Query:**
```sql
SELECT 
  pmu.usage_id,
  pmu.planned_quantity,
  pmu.actual_quantity,
  pmu.variance_quantity,  -- GENERATED COLUMN
  im.item_code,
  im.item_name,
  u.unit_name,
  SUM(ib.quantity_on_hand) as current_stock
FROM procedure_material_usage pmu
JOIN item_masters im ON pmu.item_master_id = im.item_master_id
JOIN item_units u ON pmu.unit_id = u.unit_id
LEFT JOIN item_batches ib ON ib.item_master_id = im.item_master_id
WHERE pmu.procedure_id = 123
GROUP BY pmu.usage_id, im.item_master_id, u.unit_id;
```

**Response:**
```json
{
  "procedureId": 123,
  "materialsDeducted": true,
  "deductedAt": "2025-12-27T10:30:00",
  "materials": [
    {
      "itemName": "Găng tay",
      "plannedQuantity": 1.00,
      "actualQuantity": 1.00,
      "currentStock": 179  // Batch 1: 29 + Batch 2: 150
    }
  ]
}
```

---

### BƯỚC 6: Cập Nhật Số Lượng Thực Tế

**Scenario:** Y tá nhận ra dùng 10g composite, không phải 8g.

**API Call:**
```http
PUT /api/v1/clinical-records/procedures/123/materials
```

```json
{
  "materials": [
    {
      "usageId": 1004,
      "actualQuantity": 10.0,
      "varianceReason": "ADDITIONAL_USAGE",
      "notes": "Sâu răng sâu hơn dự kiến"
    }
  ]
}
```

**Backend Logic:**
```java
public ProcedureMaterialUsage updateActualQuantity(
    Long usageId, 
    BigDecimal actualQuantity,
    String varianceReason
) {
    ProcedureMaterialUsage usage = materialUsageRepository
        .findById(usageId).orElseThrow();
    
    BigDecimal oldActual = usage.getActualQuantity();
    BigDecimal difference = actualQuantity.subtract(oldActual);
    
    // If actual INCREASED, deduct MORE from warehouse
    if (difference.compareTo(BigDecimal.ZERO) > 0) {
        deductFromWarehouse(
            usage.getItemMaster().getItemMasterId(), 
            difference
        );
    }
    // If DECREASED, return to warehouse
    else if (difference.compareTo(BigDecimal.ZERO) < 0) {
        // Add back to newest batch
        // (Implementation detail...)
    }
    
    usage.setActualQuantity(actualQuantity);
    usage.setVarianceReason(varianceReason);
    
    return materialUsageRepository.save(usage);
}
```

**Database Changes:**
```sql
-- 1. Deduct additional 2g from warehouse
UPDATE item_batches 
SET quantity_on_hand = quantity_on_hand - 2 
WHERE batch_id = 25;  -- FEFO picks same batch

-- 2. Update usage record
UPDATE procedure_material_usage 
SET 
  actual_quantity = 10.0,
  variance_quantity = 2.0,  -- AUTO CALCULATED: 10 - 8
  variance_reason = 'ADDITIONAL_USAGE',
  notes = 'Sâu răng sâu hơn dự kiến'
WHERE usage_id = 1004;
```

---

## 🔄 Transaction Safety

### Rollback Scenarios

**Scenario 1: Insufficient Stock**
```java
@Transactional
public void deductMaterialsForProcedure(Integer procedureId) {
    try {
        // Deduct material 1: SUCCESS ✅
        deductFromWarehouse(item1, qty1);
        
        // Deduct material 2: FAIL ❌ (insufficient stock)
        deductFromWarehouse(item2, qty2);
        
    } catch (Exception e) {
        // ⚡ ROLLBACK: Material 1 quantity RESTORED
        log.error("Failed to deduct materials", e);
        throw e;
    }
}
```

**Result:**
- ❌ NO partial deduction
- ✅ Database reverts to original state
- ✅ `materials_deducted_at` remains NULL

---

## 📊 Performance Considerations

### Query Optimization

**❌ N+1 Query Problem:**
```java
// BAD: 1 query for BOM + N queries for item details
List<ServiceConsumable> bom = repository.findByServiceId(serviceId);
for (ServiceConsumable sc : bom) {
    ItemMaster item = sc.getItemMaster();  // ❌ SELECT per item
    String name = item.getItemName();
}
```

**✅ JOIN FETCH Solution:**
```java
// GOOD: Single query with JOINs
@Query("""
    SELECT sc FROM ServiceConsumable sc
    JOIN FETCH sc.itemMaster im
    JOIN FETCH sc.unit u
    WHERE sc.serviceId = :serviceId
""")
List<ServiceConsumable> findByServiceIdWithDetails(Long serviceId);
```

---

## 📚 Next Steps

- ➡️ Đọc `03_API_TESTING_GUIDE.md` - Test API từng bước
- ➡️ Đọc `04_PERMISSIONS_GUIDE.md` - Hiểu phân quyền
- ➡️ Đọc `05_SAMPLE_SCENARIOS.md` - Các tình huống thực tế
