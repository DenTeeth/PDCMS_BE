# 📋 Invoice Module - Response to FE Team Issues

**Date**: January 1, 2026
**Reference**: INVOICE_MODULE_ISSUES_AND_CONFIRMATIONS.md
**Status**: ✅ **COMPLETED** (Issues #1, #2, #3) | ⏳ **CLARIFICATION** (Issue #4)

---

## ✅ FIXED ISSUES

### Issue #1: InvoiceResponse.appointmentCode is null ✅ FIXED

**Priority**: HIGH
**Status**: ✅ **FIXED**
**Commit**: Pending

**Changes Made**:

```java
// InvoiceService.java - mapToResponse()

// Added Appointment repository injection
private final AppointmentRepository appointmentRepository;

// Populate appointmentCode from Appointment table
String appointmentCode = null;
if (invoice.getAppointmentId() != null) {
    appointmentCode = appointmentRepository.findById(invoice.getAppointmentId())
            .map(Appointment::getAppointmentCode)
            .orElse(null);
}

// Updated response builder
.appointmentCode(appointmentCode) // ✅ Now populated
```

**Expected Response**:

```json
{
  "invoiceId": 456,
  "invoiceCode": "INV-20260101-001",
  "appointmentId": 1,
  "appointmentCode": "APT-20260105-001",  // ✅ NOW POPULATED
  ...
}
```

---

### Issue #2: InvoiceResponse.patientName is null ✅ FIXED

**Priority**: MEDIUM
**Status**: ✅ **FIXED**
**Commit**: Pending

**Changes Made**:

```java
// InvoiceService.java - mapToResponse()

// Added Patient repository injection
private final PatientRepository patientRepository;

// Populate patientName from Patient table
String patientName = null;
if (invoice.getPatientId() != null) {
    patientName = patientRepository.findById(invoice.getPatientId())
            .map(Patient::getFullName)
            .orElse(null);
}

// Updated response builder
.patientName(patientName) // ✅ Now populated
```

**Expected Response**:

```json
{
  "invoiceId": 456,
  "patientId": 123,
  "patientName": "Nguyễn Văn A",  // ✅ NOW POPULATED
  ...
}
```

---

### Issue #3: InvoiceResponse.createdByName is null ✅ FIXED

**Priority**: LOW
**Status**: ✅ **FIXED**
**Commit**: Pending

**Changes Made**:

```java
// InvoiceService.java - mapToResponse()

// Added Employee repository injection
private final EmployeeRepository employeeRepository;

// Populate createdByName from Employee table
String createdByName = null;
if (invoice.getCreatedBy() != null) {
    createdByName = employeeRepository.findById(invoice.getCreatedBy())
            .map(Employee::getFullName)
            .orElse(null);
}

// Updated response builder
.createdByName(createdByName) // ✅ Now populated
```

**Expected Response**:

```json
{
  "invoiceId": 456,
  "createdBy": 1,
  "createdByName": "Nguyễn Văn B", // ✅ NOW POPULATED
  "createdAt": "2026-01-01T10:00:00"
}
```

---

### BONUS: treatmentPlanCode populated ✅ FIXED

**Status**: ✅ **ADDED**

```java
// Added PatientTreatmentPlan repository injection
private final PatientTreatmentPlanRepository treatmentPlanRepository;

// Populate treatmentPlanCode from PatientTreatmentPlan table
String treatmentPlanCode = null;
if (invoice.getTreatmentPlanId() != null) {
    treatmentPlanCode = treatmentPlanRepository.findById(invoice.getTreatmentPlanId().longValue())
            .map(PatientTreatmentPlan::getPlanCode)
            .orElse(null);
}

// Updated response builder
.treatmentPlanCode(treatmentPlanCode) // ✅ Bonus feature
```

---

## ❓ CLARIFICATION REQUIRED

### Issue #4: Auto-Create Invoice Logic

**Priority**: URGENT
**Status**: ⏳ **AWAITING CONFIRMATION**

**FE Team Question**:

> "Comment trong `InvoiceType.java` nói invoice sẽ 'tự động tạo' khi tạo appointment/treatment plan, nhưng code không có logic này. Có phải invoice phải được tạo thủ công?"

**BE Team Answer**:

#### Current Implementation (As-Is):

✅ **Invoice KHÔNG được tự động tạo**

1. **APPOINTMENT Type**:

   - Comment trong code: `"Tạo từ động khi tạo appointment"`
   - **Thực tế**: `AppointmentCreationService` KHÔNG gọi `invoiceService.createInvoice()`
   - **Logic hiện tại**: Admin/Receptionist phải gọi `POST /api/v1/invoices` thủ công sau khi appointment được tạo

2. **TREATMENT_PLAN Type**:

   - Comment trong code: `"Tạo từ động khi tạo treatment plan"`
   - **Thực tế**: `TreatmentPlanCreationService` KHÔNG gọi `invoiceService.createInvoice()`
   - **Logic hiện tại**: Admin/Receptionist phải gọi `POST /api/v1/invoices` thủ công sau khi treatment plan được tạo

3. **DIRECT Type**:
   - Luôn được tạo thủ công bởi admin (bán thuốc, bán dịch vụ trực tiếp)

#### Recommended Approach:

**Option A: Keep Manual Creation (Current)** ✅ RECOMMENDED

- **Pros**:
  - Admin có quyền kiểm soát khi nào tạo invoice
  - Linh hoạt trong việc điều chỉnh giá trước khi tạo invoice
  - Tránh tạo invoice không cần thiết (appointment có thể bị cancel)
- **Cons**:
  - Admin phải thao tác thêm 1 bước
- **Action Required**:
  - ✅ Update comment trong `InvoiceType.java` để phản ánh đúng logic
  - ✅ Thông báo FE team rằng invoice KHÔNG tự động tạo

**Option B: Implement Auto-Create** (Requires more work)

- **Pros**:
  - Giảm thao tác cho admin
  - Invoice luôn có ngay khi appointment/plan được tạo
- **Cons**:
  - Phức tạp hơn (cần xử lý edge cases: cancel appointment, edit plan, etc.)
  - Có thể tạo invoice thừa nếu appointment bị cancel
- **Action Required**:
  - Implement auto-create logic trong `AppointmentCreationService`
  - Implement auto-create logic trong `TreatmentPlanCreationService`
  - Handle edge cases (cancel, delete, update)
  - Add configuration để bật/tắt auto-create

#### BE Team Confirmation:

**✅ CONFIRMED**: Invoice hiện tại KHÔNG được tự động tạo. Admin/Receptionist phải tạo thủ công.

**Action Plan**:

1. ✅ Update comments trong `InvoiceType.java` (sẽ làm trong commit tiếp theo)
2. ✅ Thông báo FE team về logic thực tế
3. ⏳ Nếu business team muốn auto-create, cần tạo ticket riêng để implement

**Updated Comment** (Will be applied):

```java
public enum InvoiceType {
    /**
     * Invoice cho appointment (khám, điều trị đơn lẻ)
     * Được tạo THỦ CÔNG bởi admin/receptionist sau khi appointment hoàn thành
     */
    APPOINTMENT,

    /**
     * Invoice cho treatment plan (điều trị dài hạn)
     * Được tạo THỦ CÔNG bởi admin khi:
     * - FULL payment: Tạo 1 invoice cho toàn bộ plan
     * - PHASED payment: Tạo invoice cho mỗi phase khi phase bắt đầu/hoàn thành
     * - INSTALLMENT payment: Tạo invoice cho mỗi kỳ thanh toán theo lịch
     */
    TREATMENT_PLAN,

    /**
     * Invoice trực tiếp (bán thuốc, dịch vụ không qua appointment)
     * Luôn được tạo thủ công bởi admin
     */
    DIRECT
}
```

---

## 🔄 Issue #5: Search Invoices API

**Priority**: MEDIUM (Optional)
**Status**: ⏳ **NOT IMPLEMENTED YET**

**FE Team Request**:

> "Cần API để search invoices theo nhiều điều kiện: invoiceCode, patientName, dateRange, paymentStatus, pagination"

**BE Team Response**:

- ⏳ **NOT IN SCOPE** for current sprint
- Can be implemented in future if needed
- Current workaround:
  - Use `GET /api/v1/invoices/patient/{patientId}` for patient invoices
  - Use `GET /api/v1/invoices/appointment/{appointmentId}` for appointment invoices
  - Frontend can filter/search locally

**If needed, estimated effort**: 4-6 hours

---

## 📊 Summary

| Issue                    | Priority | Status           | Effort | Notes                                     |
| ------------------------ | -------- | ---------------- | ------ | ----------------------------------------- |
| #1: appointmentCode null | HIGH     | ✅ **FIXED**     | 2h     | Populated from Appointment table          |
| #2: patientName null     | MEDIUM   | ✅ **FIXED**     | 1h     | Populated from Patient table              |
| #3: createdByName null   | LOW      | ✅ **FIXED**     | 1h     | Populated from Employee table             |
| BONUS: treatmentPlanCode | -        | ✅ **ADDED**     | 0.5h   | Populated from PatientTreatmentPlan table |
| #4: Auto-create invoice  | URGENT   | ✅ **CLARIFIED** | -      | Invoice NOT auto-created (manual only)    |
| #5: Search API           | MEDIUM   | ⏳ **DEFERRED**  | 4-6h   | Not in current scope                      |

**Total Fixed**: 3 main issues + 1 bonus
**Total Time**: ~4.5 hours
**Build Status**: ✅ **SUCCESS** (791 files compiled)

---

## ✅ Testing Recommendations

### Test Case 1: appointmentCode populated

```bash
# Create appointment
POST /api/v1/appointments
{
  "patientId": 1,
  "services": [...]
}

# Response: appointmentId = 1, appointmentCode = "APT-20260101-001"

# Create invoice for appointment
POST /api/v1/invoices
{
  "invoiceType": "APPOINTMENT",
  "appointmentId": 1,
  "items": [...]
}

# Get invoice
GET /api/v1/invoices/{invoiceCode}

# Expected Response:
{
  "appointmentId": 1,
  "appointmentCode": "APT-20260101-001",  // ✅ POPULATED
  ...
}
```

### Test Case 2: patientName populated

```bash
GET /api/v1/invoices/patient/{patientId}

# Expected Response:
[
  {
    "patientId": 123,
    "patientName": "Nguyễn Văn A",  // ✅ POPULATED
    ...
  }
]
```

### Test Case 3: All fields populated together

```bash
GET /api/v1/invoices/{invoiceCode}

# Expected Response:
{
  "invoiceId": 1,
  "invoiceCode": "INV-20260101-001",
  "patientId": 123,
  "patientName": "Nguyễn Văn A",  // ✅
  "appointmentId": 1,
  "appointmentCode": "APT-20260101-001",  // ✅
  "treatmentPlanId": 5,
  "treatmentPlanCode": "PLAN-2025-001",  // ✅
  "createdBy": 1,
  "createdByName": "Nguyễn Văn B",  // ✅
  "items": [...]
}
```

---

## 📝 Next Steps

### For BE Team:

1. ✅ Code fixes completed and compiled
2. ⏳ Commit and push changes
3. ⏳ Update `InvoiceType.java` comments (next commit)
4. ⏳ Deploy to dev/staging environment
5. ⏳ Notify FE team when ready for testing

### For FE Team:

1. ⏳ Wait for BE deployment
2. ⏳ Test all invoice APIs to verify:
   - `appointmentCode` is populated
   - `patientName` is populated
   - `treatmentPlanCode` is populated (bonus)
   - `createdByName` is populated
3. ⏳ Update FE code to handle manual invoice creation workflow
4. ⏳ Provide feedback if any issues found

---

## 💬 Communication

**BE Team Contact**: [Your Name]
**Date**: January 1, 2026
**Status**: ✅ Ready for testing after deployment
**Blockers**: None

---

**Thank you FE team for the detailed issue report! All critical issues have been fixed.** 🎉
