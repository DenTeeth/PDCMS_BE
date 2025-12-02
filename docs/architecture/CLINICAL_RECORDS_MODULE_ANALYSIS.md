# Clinical Records Module (Module #9) - Architecture Analysis

## 📋 Executive Summary

Bạn đã phát hiện ra **VẤN ĐỀ NGHIÊM TRỌNG** trong thiết kế Clinical Records Module. Sau khi phân tích toàn bộ kiến trúc hệ thống (Appointments, Treatment Plans, Warehouse), tôi xác nhận:

### ❌ VẤN ĐỀ CHÍNH: Thiếu Logic Kết Nối Warehouse

**Bạn nói đúng**: _"không có nối gì tới kho - chúng ta không bán thuốc"_

Schema hiện tại có bảng `clinical_prescriptions` (đơn thuốc) nhưng **KHÔNG CÓ WORKFLOW** để:

1. **Tự động xuất kho** khi kê đơn thuốc
2. **Kiểm tra tồn kho** trước khi kê đơn
3. **Ghi nhận chi phí** thuốc vào hồ sơ tài chính
4. **Truy vết** thuốc đã cấp cho bệnh nhân

---

## 🔍 Phân Tích Chi Tiết Kiến Trúc Hiện Tại

### 1. **Appointment Module** - Trung tâm điều phối

```java
// Appointment.java
@Entity
@Table(name = "appointments")
public class Appointment {
    private Integer appointmentId;
    private String appointmentCode;
    private Integer patientId;       // Bệnh nhân
    private Integer employeeId;      // Bác sĩ chính
    private String roomId;           // Phòng khám
    private LocalDateTime appointmentStartTime;
    private LocalDateTime appointmentEndTime;
    private AppointmentStatus status; // SCHEDULED, IN_PROGRESS, COMPLETED
    private String notes;            // Ghi chú chung
}
```

**Vai trò**:

- Xác định **AI, ĐÂU, KHI NÀO** xảy ra dịch vụ
- Link tới `patient_id`, `employee_id` (bác sĩ), `room_id`
- Có `appointment_services` (dịch vụ dự kiến làm)
- Có `appointment_participants` (phụ tá)

**Thiếu**:

- Không có thông tin y khoa chi tiết (chẩn đoán, triệu chứng, chỉ số sinh tồn)
- Không có thông tin thuốc/vật tư đã sử dụng
- `notes` là text tự do, không có cấu trúc

---

### 2. **Treatment Plan Module** - Hợp đồng dài hạn

```java
@Entity
@Table(name = "patient_treatment_plans")
public class PatientTreatmentPlan {
    private Long planId;
    private String planCode;
    private String planName;
    private Patient patient;
    private Employee createdBy;           // Bác sĩ tạo kế hoạch
    private TreatmentPlanStatus status;   // PENDING, IN_PROGRESS, COMPLETED
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalCost;
    private PaymentType paymentType;      // FULL_PAYMENT, INSTALLMENT
}

@Entity
@Table(name = "patient_plan_items")
public class PatientPlanItem {
    private Long patientItemId;
    private PatientPlanPhase phase;
    private DentalService service;        // Dịch vụ cần làm
    private Integer quantity;
    private PlanItemStatus status;        // PENDING, SCHEDULED, IN_PROGRESS, COMPLETED
    // Link to appointments
    @ManyToMany
    private Set<Appointment> linkedAppointments;
}
```

**Vai trò**:

- Định nghĩa **KẾ HOẠCH ĐIỀU TRỊ DÀI HẠN** (niềng răng, implant)
- Chia thành các giai đoạn (phases)
- Mỗi giai đoạn có nhiều dịch vụ (plan items)
- Khi làm xong item → update status thành COMPLETED

**Quan trọng**:

- `PatientPlanItem` link tới `Appointment` qua bảng trung gian `appointment_plan_items`
- Khi appointment hoàn thành → service layer CÓ THỂ update plan item status

---

### 3. **Warehouse Module** - Quản lý vật tư/thuốc

#### 3.1. Item Masters (Định nghĩa vật tư)

```java
@Entity
@Table(name = "item_masters")
public class ItemMaster {
    private Integer itemMasterId;
    private String itemCode;              // MED-AMOXICILLIN-500, CON-GLOVE-01
    private String itemName;
    private ItemCategory category;
    private WarehouseType warehouseType;  // NORMAL, PRESCRIPTION_REQUIRED
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private BigDecimal currentMarketPrice;
    private Boolean isPrescriptionRequired; // TRUE nếu là thuốc kê đơn
    private Integer defaultShelfLifeDays;
    private Integer cachedTotalQuantity;   // Tổng tồn kho (denormalized)
}
```

#### 3.2. Service Consumables (Bill of Materials - API 6.17-6.19)

```java
@Entity
@Table(name = "service_consumables")
public class ServiceConsumable {
    private Long linkId;
    private Long serviceId;              // FK -> services
    private ItemMaster itemMaster;       // FK -> item_masters
    private BigDecimal quantityPerService; // Định mức: 2.5 viên, 1.0 ống
    private ItemUnit unit;               // Đơn vị
    private String notes;
}
```

**Logic hiện tại**:

- Mỗi dịch vụ có định mức tiêu hao vật tư (BOM)
- Ví dụ: Dịch vụ "Trám răng composite" cần:
  - 5g composite (MAT-COMP-01)
  - 2 găng tay (CON-GLOVE-01)
  - 1 khẩu trang (CON-MASK-01)

**NHƯNG**: Không có logic tự động **XUẤT KHO** dựa trên BOM!

#### 3.3. Storage Transactions (Phiếu xuất/nhập kho)

```java
@Entity
@Table(name = "storage_transactions")
public class StorageTransaction {
    private Long transactionId;
    private String transactionCode;       // IMP-2024-001, EXP-2024-001
    private TransactionType transactionType; // IMPORT, EXPORT
    private LocalDateTime transactionDate;
    private String exportType;            // APPOINTMENT, DISPOSAL, INTERNAL
    private Appointment relatedAppointment; // FK -> appointments (nullable)
    private TransactionStatus approvalStatus;
    private BigDecimal totalValue;
    private Employee createdBy;
}

@Entity
@Table(name = "storage_transaction_items")
public class StorageTransactionItem {
    private Long itemId;
    private StorageTransaction transaction;
    private ItemBatch batch;              // Lô hàng xuất (FEFO algorithm)
    private String itemCode;
    private Integer quantityChange;       // Âm = xuất, Dương = nhập
    private BigDecimal price;             // Giá vốn
    private BigDecimal totalLineValue;
}
```

**Logic hiện tại**:

- Có trường `related_appointment_id` trong `storage_transactions`
- Manual export: Kho tự tạo phiếu xuất, gõ tay các item, chọn appointment liên quan
- KHÔNG TỰ ĐỘNG dựa trên service BOM

**Ví dụ trong seed data**:

```sql
-- Transaction EXP-2024-001 (Manual export for appointment)
INSERT INTO storage_transactions
(transaction_code, type, export_type, related_appointment_id, ...)
VALUES ('EXP-2024-001', 'EXPORT', 'APPOINTMENT', 123, ...);

-- Chi tiết xuất (Gõ tay!)
INSERT INTO storage_transaction_items
(transaction_id, batch_id, item_code, quantity_change, notes)
VALUES
(tx_id, batch_id, 'CON-GLOVE-01', -10, 'Xuất cho lịch hẹn APT-20251106-001'),
(tx_id, batch_id, 'CON-MASK-01', -5, 'Xuất cho lịch hẹn APT-20251106-001');
```

---

## ❌ VẤN ĐỀ VỚI SCHEMA MODULE 9 HIỆN TẠI

### 1. **Clinical Prescriptions Table - Thiếu Workflow Xuất Kho**

```sql
-- Schema bạn đề xuất
Table clinical_prescriptions {
  prescription_id SERIAL [pk]
  clinical_record_id INT [not null]
  notes TEXT
  created_at timestamp
}

Table clinical_prescription_items {
  pres_item_id SERIAL [pk]
  prescription_id INT [not null]
  item_master_id INT [not null]  // Link sang kho thuốc
  quantity INT [not null]         // Số lượng cấp
  dosage VARCHAR(100)             // Cách dùng: Sáng 1 viên, Tối 1 viên
  duration_days INT
}
```

**Vấn đề nghiêm trọng**:

1. **Không kiểm tra tồn kho**: Bác sĩ kê 100 viên Amoxicillin nhưng kho chỉ còn 20 viên
2. **Không tự động xuất kho**: Bệnh nhân nhận thuốc nhưng kho không trừ
3. **Không tracking chi phí**: Thuốc đã cấp không được tính vào chi phí điều trị
4. **Không có audit trail**: Không biết ai lấy thuốc, lúc nào, từ lô nào (batch tracking)
5. **Không có workflow duyệt**: Kho không biết đơn thuốc nào cần chuẩn bị

### 2. **Clinical Record Procedures - Thiếu Auto-Export**

```sql
Table clinical_record_procedures {
  proc_id SERIAL [pk]
  clinical_record_id INT [not null]
  service_id INT [not null]          // Link sang dịch vụ
  patient_plan_item_id INT           // Link sang kế hoạch điều trị
  tooth_number VARCHAR(10)
  quantity INT [default: 1]
  notes TEXT
}
```

**Vấn đề**:

- Có link sang `service_id` (dịch vụ đã làm)
- Service có BOM trong `service_consumables` (định mức vật tư)
- **NHƯNG KHÔNG CÓ LOGIC** tự động tạo phiếu xuất kho theo BOM

**Ví dụ thực tế**:

```
Bác sĩ làm "Trám răng composite" (service_id=10) x 2 răng
→ BOM: 5g composite/răng → Cần 10g
→ Lẽ ra phải: Tự động tạo phiếu xuất 10g composite
→ Thực tế: KHÔNG TỰ ĐỘNG, kho phải gõ tay sau
```

### 3. **Appointment Notes vs Clinical Records - Trùng lặp**

Schema hiện tại:

- `appointments` có trường `notes` (TEXT)
- Bạn thêm `clinical_records` cũng có `notes`, `chief_complaint`, `diagnosis`

**Confusion**:

- Bác sĩ ghi chú ở đâu? Appointment hay Clinical Record?
- Làm sao phân biệt "ghi chú lễ tân" vs "ghi chú y khoa"?

---

## ✅ GIẢI PHÁP ĐỀ XUẤT

### **Phương Án 1: Tích Hợp Workflow Xuất Kho Tự Động**

#### 1.1. Sửa Schema Clinical Prescriptions

```sql
-- Thêm trạng thái workflow
Table clinical_prescriptions {
  prescription_id SERIAL [pk]
  clinical_record_id INT [not null]
  status VARCHAR(20) [note: 'PENDING, DISPENSING, DISPENSED, CANCELLED']

  -- Link to warehouse export transaction
  storage_transaction_id INT [note: 'FK -> storage_transactions. Phiếu xuất kho tự động']

  dispensed_by INT [note: 'FK -> employees. Dược sĩ/kho phát thuốc']
  dispensed_at TIMESTAMP

  notes TEXT
  created_at TIMESTAMP
  updated_at TIMESTAMP
}
```

**Workflow đề xuất**:

1. Bác sĩ kê đơn → `status = PENDING`
2. Kho kiểm tra tồn kho → Nếu đủ: `status = DISPENSING`
3. **Tự động tạo phiếu xuất kho** (`storage_transactions`)
   - `type = EXPORT`
   - `export_type = PRESCRIPTION`
   - `related_appointment_id = clinical_record.appointment_id`
4. Kho phát thuốc → `status = DISPENSED`, lưu `storage_transaction_id`

#### 1.2. Logic Service Layer (Pseudo-code)

```java
@Transactional
public void dispensePrescription(Long prescriptionId) {
    // 1. Load prescription
    ClinicalPrescription prescription = repository.findById(prescriptionId);

    // 2. Validate stock availability
    for (PrescriptionItem item : prescription.getItems()) {
        Integer availableStock = itemBatchRepository.getTotalQuantity(item.getItemMasterId());
        if (availableStock < item.getQuantity()) {
            throw new InsufficientStockException(item.getItemMaster().getItemName());
        }
    }

    // 3. Create export transaction (AUTO)
    StorageTransaction exportTx = StorageTransaction.builder()
        .transactionCode(generateCode("EXP-PRESCRIPTION"))
        .transactionType(TransactionType.EXPORT)
        .exportType("PRESCRIPTION")
        .relatedAppointment(prescription.getClinicalRecord().getAppointment())
        .referenceCode("PRESC-" + prescriptionId)
        .requestedBy(prescription.getClinicalRecord().getDoctor().getFullName())
        .departmentName("Khám bệnh")
        .build();

    // 4. FEFO allocation (First Expired, First Out)
    for (PrescriptionItem item : prescription.getItems()) {
        List<ItemBatch> batches = itemBatchRepository.findAvailableBatches(
            item.getItemMasterId(),
            OrderBy.EXPIRY_DATE_ASC
        );

        Integer remainingQty = item.getQuantity();
        for (ItemBatch batch : batches) {
            Integer allocatedQty = Math.min(remainingQty, batch.getCurrentQuantity());

            // Deduct stock
            batch.setCurrentQuantity(batch.getCurrentQuantity() - allocatedQty);

            // Create transaction item
            StorageTransactionItem txItem = StorageTransactionItem.builder()
                .transaction(exportTx)
                .batch(batch)
                .itemCode(item.getItemMaster().getItemCode())
                .quantityChange(-allocatedQty)  // Negative = export
                .price(batch.getUnitCost())
                .build();
            exportTx.getItems().add(txItem);

            remainingQty -= allocatedQty;
            if (remainingQty == 0) break;
        }
    }

    // 5. Save transaction
    storageTransactionRepository.save(exportTx);

    // 6. Update prescription status
    prescription.setStatus("DISPENSED");
    prescription.setStorageTransaction(exportTx);
    prescription.setDispensedBy(getCurrentEmployee());
    prescription.setDispensedAt(LocalDateTime.now());
}
```

#### 1.3. Thêm Validation Logic

```java
@Transactional(readOnly = true)
public PrescriptionValidationResult validatePrescription(Long prescriptionId) {
    ClinicalPrescription prescription = repository.findById(prescriptionId);
    List<String> warnings = new ArrayList<>();

    for (PrescriptionItem item : prescription.getItems()) {
        ItemMaster itemMaster = item.getItemMaster();

        // Check 1: Stock availability
        Integer availableStock = itemBatchRepository.getTotalQuantity(itemMaster.getItemMasterId());
        if (availableStock < item.getQuantity()) {
            warnings.add(String.format(
                "%s: Thiếu hàng! Cần %d, tồn kho %d",
                itemMaster.getItemName(), item.getQuantity(), availableStock
            ));
        }

        // Check 2: Prescription requirement
        if (itemMaster.getIsPrescriptionRequired() && !hasValidLicense(prescription.getDoctor())) {
            warnings.add(String.format(
                "%s: Thuốc kê đơn, bác sĩ không có chứng chỉ",
                itemMaster.getItemName()
            ));
        }

        // Check 3: Expiry warning
        LocalDate nearestExpiry = itemBatchRepository.getNearestExpiryDate(itemMaster.getItemMasterId());
        if (nearestExpiry != null && nearestExpiry.isBefore(LocalDate.now().plusDays(30))) {
            warnings.add(String.format(
                "%s: Thuốc gần hết hạn (còn %d ngày)",
                itemMaster.getItemName(), ChronoUnit.DAYS.between(LocalDate.now(), nearestExpiry)
            ));
        }
    }

    return PrescriptionValidationResult.builder()
        .isValid(warnings.isEmpty())
        .warnings(warnings)
        .build();
}
```

---

### **Phương Án 2: Auto-Export for Service Consumables**

#### 2.1. Trigger Logic When Marking Service Complete

```java
@Transactional
public void completeServiceProcedure(Long procedureId) {
    ClinicalRecordProcedure procedure = repository.findById(procedureId);

    // 1. Get service BOM
    List<ServiceConsumable> bom = serviceConsumableRepository.findByServiceId(
        procedure.getService().getServiceId()
    );

    if (!bom.isEmpty()) {
        // 2. Calculate total quantity (considering quantity multiplier)
        Integer quantity = procedure.getQuantity(); // Số lần làm dịch vụ

        // 3. Create export transaction
        StorageTransaction exportTx = StorageTransaction.builder()
            .transactionCode(generateCode("EXP-SERVICE"))
            .transactionType(TransactionType.EXPORT)
            .exportType("APPOINTMENT")
            .relatedAppointment(procedure.getClinicalRecord().getAppointment())
            .referenceCode("PROC-" + procedureId)
            .requestedBy(procedure.getClinicalRecord().getDoctor().getFullName())
            .departmentName("Khám bệnh")
            .build();

        // 4. Export each BOM item
        for (ServiceConsumable bomItem : bom) {
            BigDecimal totalQty = bomItem.getQuantityPerService()
                .multiply(BigDecimal.valueOf(quantity));

            // FEFO allocation
            allocateAndExport(
                exportTx,
                bomItem.getItemMaster(),
                totalQty.intValue(),
                bomItem.getUnit()
            );
        }

        // 5. Save transaction
        storageTransactionRepository.save(exportTx);

        // 6. Link transaction to procedure (optional)
        procedure.setStorageTransactionId(exportTx.getTransactionId());
    }

    // 7. Update procedure status
    procedure.setStatus("COMPLETED");
}
```

#### 2.2. Schema Update for Procedures

```sql
-- Thêm link sang phiếu xuất kho
ALTER TABLE clinical_record_procedures
ADD COLUMN storage_transaction_id INT,
ADD CONSTRAINT fk_procedure_export
    FOREIGN KEY (storage_transaction_id)
    REFERENCES storage_transactions(storage_transaction_id);
```

---

### **Phương Án 3: Unified Clinical Records Architecture**

#### 3.1. Schema Đề Xuất Mới

```sql
-- ============================================
-- MODULE #9: CLINICAL RECORDS (REVISED V2)
-- ============================================

-- 1. PHIẾU KHÁM LÂM SÀNG (Core medical record)
Table clinical_records {
  clinical_record_id SERIAL [pk]

  -- Link to appointment (1-to-1)
  appointment_id INT [unique, not null, note: 'FK -> appointments']

  -- Medical data
  chief_complaint TEXT [note: 'Lý do khám']
  clinical_findings TEXT [note: 'Triệu chứng lâm sàng']
  diagnosis TEXT [note: 'Chẩn đoán']
  vital_signs JSONB [note: '{"bp": "120/80", "pulse": 80, "temp": 37}']
  treatment_note TEXT [note: 'Lời dặn']

  -- Status tracking
  status VARCHAR(20) [note: 'DRAFT, COMPLETED, BILLED']

  created_at TIMESTAMP [default: 'NOW()']
  updated_at TIMESTAMP
}

-- 2. THỦ THUẬT ĐÃ LÀM (With warehouse integration)
Table clinical_record_procedures {
  proc_id SERIAL [pk]
  clinical_record_id INT [not null]
  service_id INT [not null]
  patient_plan_item_id INT [note: 'Link to treatment plan']

  -- Clinical details
  tooth_number VARCHAR(10)
  tooth_surface VARCHAR(10)
  quantity INT [default: 1]
  notes TEXT

  -- Warehouse integration
  storage_transaction_id INT [note: 'FK -> storage_transactions. Auto-created export']
  auto_export_status VARCHAR(20) [note: 'PENDING, EXPORTED, FAILED']

  created_at TIMESTAMP
}

-- 3. ĐƠN THUỐC (With warehouse workflow)
Table clinical_prescriptions {
  prescription_id SERIAL [pk]
  clinical_record_id INT [not null]

  -- Workflow status
  status VARCHAR(20) [note: 'PENDING, VALIDATED, DISPENSED, CANCELLED']

  -- Warehouse integration
  storage_transaction_id INT [note: 'FK -> storage_transactions. Auto export']

  -- Dispensing tracking
  validated_by INT [note: 'FK -> employees. Dược sĩ kiểm tra']
  validated_at TIMESTAMP
  dispensed_by INT [note: 'FK -> employees. Người phát thuốc']
  dispensed_at TIMESTAMP

  notes TEXT
  created_at TIMESTAMP
}

Table clinical_prescription_items {
  pres_item_id SERIAL [pk]
  prescription_id INT [not null]
  item_master_id INT [not null]

  quantity INT [not null]
  dosage VARCHAR(100) [note: 'Sáng 1 viên, Tối 1 viên']
  duration_days INT

  -- Stock validation
  available_stock INT [note: 'Snapshot at creation time']
  allocated_stock INT [note: 'Stock reserved after validation']
}

-- 4. HIỆN TRẠNG RĂNG (Unchanged)
Table patient_tooth_status {
  status_id SERIAL [pk]
  patient_id INT [not null]
  tooth_number VARCHAR(5) [not null]
  condition_code VARCHAR(50) [note: 'NORMAL, MISSING, IMPLANT, CROWN, CARIES']
  last_updated_at TIMESTAMP [default: 'NOW()']
}

-- ============================================
-- RELATIONSHIPS (REVISED)
-- ============================================

Ref: clinical_records.appointment_id - appointments.appointment_id [delete: restrict]
Ref: clinical_record_procedures.clinical_record_id > clinical_records.clinical_record_id [delete: cascade]
Ref: clinical_record_procedures.service_id > services.service_id
Ref: clinical_record_procedures.patient_plan_item_id > patient_plan_items.patient_item_id [delete: set null]
Ref: clinical_record_procedures.storage_transaction_id > storage_transactions.storage_transaction_id [delete: set null]

Ref: clinical_prescriptions.clinical_record_id > clinical_records.clinical_record_id [delete: cascade]
Ref: clinical_prescriptions.storage_transaction_id > storage_transactions.storage_transaction_id [delete: set null]
Ref: clinical_prescriptions.validated_by > employees.employee_id [delete: set null]
Ref: clinical_prescriptions.dispensed_by > employees.employee_id [delete: set null]

Ref: clinical_prescription_items.prescription_id > clinical_prescriptions.prescription_id [delete: cascade]
Ref: clinical_prescription_items.item_master_id > item_masters.item_master_id [delete: restrict]

Ref: patient_tooth_status.patient_id > patients.patient_id [delete: cascade]
```

---

## 🎯 WORKFLOW ĐỀ XUẤT CHO MODULE 9

### **Flow 1: Khám bệnh và Kê đơn thuốc**

```
1. Lễ tân tạo Appointment (status=SCHEDULED)
   ↓
2. Bác sĩ bắt đầu khám (status=IN_PROGRESS)
   ↓
3. Bác sĩ tạo Clinical Record:
   - Nhập chief_complaint, clinical_findings, diagnosis
   - Nhập vital signs (huyết áp, mạch, nhiệt độ)
   ↓
4. Bác sĩ kê đơn thuốc:
   - Chọn thuốc từ item_masters (filter: isPrescriptionRequired)
   - Nhập quantity, dosage, duration
   - Hệ thống CHECK tồn kho real-time
   - Nếu thiếu hàng → Warning: "Amoxicillin thiếu 50 viên"
   ↓
5. Bác sĩ lưu đơn (status=PENDING)
   ↓
6. Dược sĩ/Kho validate đơn:
   - Kiểm tra tồn kho
   - Kiểm tra tương tác thuốc (nếu cần)
   - Duyệt → status=VALIDATED
   ↓
7. Hệ thống TỰ ĐỘNG tạo phiếu xuất kho:
   - type=EXPORT, export_type=PRESCRIPTION
   - related_appointment_id = appointment.id
   - Áp dụng FEFO (First Expired, First Out)
   - Tạo storage_transaction_items (negative quantities)
   ↓
8. Kho phát thuốc:
   - In nhãn thuốc (tên, cách dùng, expiry date)
   - Scan QR code đơn thuốc
   - Xác nhận → status=DISPENSED
   ↓
9. Hoàn thành appointment (status=COMPLETED)
```

### **Flow 2: Làm thủ thuật với vật tư tiêu hao**

```
1. Bác sĩ làm thủ thuật (clinical_record_procedures)
   - Chọn service: "Trám răng composite"
   - Nhập tooth_number: "36"
   - Quantity: 1
   ↓
2. Hệ thống load BOM của service:
   - 5g composite (MAT-COMP-01)
   - 2 găng tay (CON-GLOVE-01)
   - 1 khẩu trang (CON-MASK-01)
   ↓
3. Bác sĩ bấm "Complete Procedure"
   ↓
4. Hệ thống TỰ ĐỘNG:
   a) Kiểm tra tồn kho
   b) Tạo phiếu xuất kho (storage_transactions)
      - type=EXPORT, export_type=APPOINTMENT
      - related_appointment_id = appointment.id
   c) Áp dụng FEFO cho từng item
   d) Trừ tồn kho (item_batches.current_quantity)
   e) Cập nhật procedure.storage_transaction_id
   ↓
5. Nếu link tới Treatment Plan:
   - Update patient_plan_item.status = COMPLETED
   - Tính % hoàn thành của phase
   ↓
6. Kho report cuối ngày:
   - "Hôm nay xuất 50g composite cho 10 appointments"
```

---

## 📊 SO SÁNH KIẾN TRÚC

| Khía cạnh                 | Schema Gốc (Bạn đề xuất)      | Schema Revised (Đề xuất của tôi)      |
| ------------------------- | ----------------------------- | ------------------------------------- |
| **Prescription Workflow** | Không có                      | PENDING → VALIDATED → DISPENSED       |
| **Kiểm tra tồn kho**      | Không                         | Real-time validation                  |
| **Tự động xuất kho**      | Không                         | Auto-create storage_transactions      |
| **FEFO Algorithm**        | Không                         | Áp dụng (ưu tiên lô gần hết hạn)      |
| **Chi phí thuốc**         | Không tracking                | Tính từ batch.unitCost                |
| **Audit trail**           | Không                         | storage_transaction_items có batch_id |
| **Link appointment**      | Gián tiếp qua clinical_record | Trực tiếp qua related_appointment_id  |
| **Procedure BOM**         | Không tự động                 | Auto-export theo service_consumables  |
| **Pharmacy validation**   | Không                         | validated_by, dispensed_by            |
| **Stock reservation**     | Không                         | allocated_stock field                 |

---

## ⚠️ RỦI RO NẾU KHÔNG SỬA

1. **Mất mát vật tư**: Bệnh nhân nhận thuốc nhưng kho không trừ → Kiểm kê sai lệch
2. **Thiếu hàng đột ngột**: Kê 100 viên nhưng kho chỉ còn 20 → Bệnh nhân chờ đợi
3. **Không tính được chi phí**: Báo cáo tài chính thiếu chi phí thuốc/vật tư
4. **Truy vết khó khăn**: Thuốc hết hạn được phát cho ai? Không biết!
5. **Compliance risk**: Thuốc kê đơn không có audit trail → Vi phạm quy định
6. **Double work**: Bác sĩ ghi → Kho phải gõ lại phiếu xuất (Manual, dễ sai)

---

## ✅ KHUYẾN NGHỊ TRIỂN KHAI

### **Phase 1: Core Clinical Records (2 weeks)**

- [ ] Tạo bảng `clinical_records` (link 1-1 với appointments)
- [ ] Tạo bảng `clinical_record_procedures`
- [ ] Tạo bảng `patient_tooth_status`
- [ ] API tạo/sửa/xem clinical record
- [ ] Link procedure tới treatment plan items

### **Phase 2: Prescription Workflow (3 weeks)**

- [ ] Tạo bảng `clinical_prescriptions` + items (có status field)
- [ ] API kê đơn thuốc (với stock validation real-time)
- [ ] API validate đơn thuốc (dược sĩ review)
- [ ] **AUTO-CREATE** phiếu xuất kho khi validate
- [ ] API dispensing (phát thuốc)
- [ ] Print prescription label

### **Phase 3: Service Consumables Auto-Export (2 weeks)**

- [ ] Service layer: Detect procedure completion
- [ ] Load BOM từ `service_consumables`
- [ ] **AUTO-CREATE** phiếu xuất kho theo BOM
- [ ] Link `storage_transaction_id` vào procedure
- [ ] Kho review: Dashboard xuất kho tự động

### **Phase 4: Financial Integration (1 week)**

- [ ] Tính COGS (Cost of Goods Sold) từ thuốc/vật tư
- [ ] Báo cáo chi phí theo appointment
- [ ] Báo cáo chi phí theo treatment plan

---

## 🔗 THAM KHẢO KIẾN TRÚC HIỆN TẠI

### **Files cần xem thêm**:

1. `src/main/java/com/dental/clinic/management/warehouse/service/ExportTransactionService.java`

   - Logic FEFO allocation
   - Auto-unpacking (xé lẻ đơn vị lớn)

2. `src/main/java/com/dental/clinic/management/warehouse/domain/StorageTransaction.java`

   - Trường `relatedAppointment` (đã có sẵn!)
   - Trường `exportType` (APPOINTMENT, DISPOSAL, INTERNAL)

3. `src/main/java/com/dental/clinic/management/service/ServiceConsumable.java`

   - BOM structure (service → items → quantity)

4. `docs/api-guides/warehouse/API_6.17_SERVICE_CONSUMABLES_COMPLETE.md`
   - Logic định mức tiêu hao

---

## 💡 TÓM TẮT

**Bạn phát hiện đúng vấn đề!** Schema gốc của bạn thiếu:

1. ❌ Workflow xuất kho tự động
2. ❌ Validation tồn kho trước khi kê đơn
3. ❌ Tracking chi phí thuốc/vật tư
4. ❌ Link giữa đơn thuốc ↔ phiếu xuất kho

**Giải pháp**:

- ✅ Thêm `status` field vào prescriptions
- ✅ Thêm `storage_transaction_id` link
- ✅ Auto-create export transaction khi validate/dispense
- ✅ Reuse logic FEFO của warehouse module
- ✅ Auto-export theo BOM khi complete procedure

**Ưu điểm**:

- Không trùng lặp code (reuse ExportTransactionService)
- Tận dụng infrastructure sẵn có (storage_transactions, FEFO, batch tracking)
- Audit trail đầy đủ
- Chi phí tính chính xác

---

Bạn muốn tôi implement prototype code cho workflow prescription auto-export không? Hoặc vẽ sequence diagram chi tiết hơn?
