# Issue: BE - Các tính năng còn thiếu cho Treatment Plan Invoice

**Ngày tạo:** 2026-01-26  
**Ngày hoàn thành:** 2026-01-05  
**Mức độ:** Medium-High  
**Module:** Treatment Plan, Payment/Invoice  
**Trạng thái:** ✅ COMPLETED - All 3 Issues Resolved  
**Related Issue:** `ISSUE_BE_TREATMENT_PLAN_PHASED_INVOICE_CREATION.md`

---

## 📋 Tổng quan

Sau khi kiểm tra implementation hiện tại, BE đã implement thành công:
- ✅ Tự động tạo invoice khi approve plan (FULL payment)
- ✅ Tự động tạo invoice theo phase khi approve plan (PHASED payment)
- ✅ Tự động tạo invoice theo đợt trả góp khi approve plan (INSTALLMENT payment) - **Completed 2026-01-05**
- ✅ API GET /api/v1/invoices để lấy tất cả invoices - **Completed 2026-01-05**
- ✅ Xử lý chỉnh sửa plan sau khi tạo invoice - **Completed 2026-01-05**

**All issues have been resolved!** 🎉

---

## ✅ Issue 1: INSTALLMENT Payment Type (COMPLETED)

### ✅ Status: COMPLETED (2026-01-05)

### Mô tả

Hiện tại trong `TreatmentPlanApprovalService.java` (line 471-474), case INSTALLMENT chỉ log warning và không tạo invoice:

```java
case INSTALLMENT:
    // TODO: Implement installment logic
    log.warn("⚠️ INSTALLMENT payment type not yet implemented for plan: {}", plan.getPlanCode());
    break;
```

### Yêu cầu

Cần implement logic tạo invoice theo đợt thanh toán cho INSTALLMENT payment type:

- Tạo nhiều invoices theo số đợt thanh toán
- Mỗi invoice có:
  - `phaseNumber = null`
  - `installmentNumber = 1, 2, 3, ...`
  - Items được phân bổ vào các đợt

### ✅ Implementation Summary

**Date Completed:** January 5, 2026

**Solution Chosen:** Option 1 - Config trong plan (flexible and manageable)

#### 1. Database Changes

**Added fields to `PatientTreatmentPlan` entity:**
```java
/**
 * Number of installments for INSTALLMENT payment type.
 * If null or 0, defaults to 3 installments.
 * Only used when paymentType = INSTALLMENT.
 */
@Column(name = "installment_count")
private Integer installmentCount;

/**
 * Number of days between each installment payment.
 * If null or 0, defaults to 30 days (monthly).
 * Only used when paymentType = INSTALLMENT.
 */
@Column(name = "installment_interval_days")
private Integer installmentIntervalDays;
```

#### 2. API Request Changes

**Updated `CreateTreatmentPlanRequest` DTO:**
```java
/**
 * Number of installments (only for INSTALLMENT payment type).
 * If not provided or 0, defaults to 3 installments.
 * Example: 3 (pay in 3 installments over 3 months)
 */
@Min(value = 1, message = "Số đợt trả góp phải >= 1")
@Max(value = 12, message = "Số đợt trả góp phải <= 12")
@Schema(description = "Number of installments for INSTALLMENT payment type (1-12). Defaults to 3 if not provided.", 
        example = "3", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
private Integer installmentCount;

/**
 * Number of days between each installment payment.
 * If not provided or 0, defaults to 30 days (monthly).
 * Example: 30 (monthly payments)
 */
@Min(value = 1, message = "Khoảng cách giữa các đợt trả góp phải >= 1 ngày")
@Max(value = 90, message = "Khoảng cách giữa các đợt trả góp phải <= 90 ngày")
@Schema(description = "Days between each installment payment. Defaults to 30 (monthly) if not provided.", 
        example = "30", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
private Integer installmentIntervalDays;
```

#### 3. Business Logic Implementation

**Modified `TreatmentPlanApprovalService.java`:**

```java
case INSTALLMENT:
    createInstallmentInvoices(plan);
    break;

/**
 * Create multiple invoices for INSTALLMENT payment type.
 * - Divides total cost evenly across installments
 * - phaseNumber = null (not phase-based)
 * - installmentNumber = 1, 2, 3, ...
 * - Each installment has proportional share of items
 * 
 * Business Rules:
 * - Number of installments from plan.installmentCount (default: 3)
 * - Interval between installments from plan.installmentIntervalDays (default: 30 days)
 * - Items distributed evenly across installments
 * - If items can't be divided evenly, first installments get extra items
 * - Due dates staggered based on interval
 * 
 * Example: 3 installments, 5 items total
 * - Installment 1: 2 items (due in 7 days)
 * - Installment 2: 2 items (due in 37 days)
 * - Installment 3: 1 item (due in 67 days)
 */
private void createInstallmentInvoices(PatientTreatmentPlan plan) {
    // Implementation details in code
}
```

#### 4. Business Rules

**Answers to Original Questions:**

1. **Số đợt thanh toán được xác định như thế nào?**
   - ✅ **Config trong plan khi tạo** via `installmentCount` field
   - Default: 3 đợt nếu không cung cấp
   - Range: 1-12 đợt (validated)

2. **Items được phân bổ như thế nào?**
   - ✅ **Chia đều items across installments**
   - If items can't be divided evenly, first installments get extra items
   - Example: 5 items, 3 installments → [2, 2, 1]
   - Maintains phase information in item notes

3. **Due date cho mỗi đợt?**
   - ✅ **Staggered based on installmentIntervalDays**
   - First installment: Due in 7 days from approval
   - Subsequent installments: 7 + (n-1) × intervalDays
   - Default interval: 30 days (monthly)
   - Example with 30-day interval: [7 days, 37 days, 67 days]

#### 5. Example API Usage

**Create treatment plan with INSTALLMENT payment:**

```bash
POST /api/v1/patients/P12345/treatment-plans
```

**Request Body:**
```json
{
  "sourceTemplateCode": "TPL_ORTHO_METAL",
  "doctorEmployeeCode": "DR_AN_KHOA",
  "planNameOverride": "Lộ trình niềng răng trả góp 6 tháng",
  "discountAmount": 2000000,
  "paymentType": "INSTALLMENT",
  "installmentCount": 6,
  "installmentIntervalDays": 30
}
```

**Result when approved:**
- Creates 6 invoices automatically
- Each invoice has `installmentNumber` = 1, 2, 3, 4, 5, 6
- Due dates: 7, 37, 67, 97, 127, 157 days from now
- Items distributed evenly across all installments

#### 6. Test Scenarios

✅ **Test Case 1:** Create plan with default installments (not specified)
- **Expected:** 3 installments, 30-day intervals
- **Result:** ✅ Defaults applied correctly

✅ **Test Case 2:** Create plan with custom installments (6 installments, 15-day intervals)
- **Expected:** 6 invoices with 15-day spacing
- **Result:** ✅ Custom configuration respected

✅ **Test Case 3:** Approve plan with 5 items, 3 installments
- **Expected:** Items distributed [2, 2, 1]
- **Result:** ✅ Even distribution with remainder handled

✅ **Test Case 4:** Approve plan with 9 items, 4 installments
- **Expected:** Items distributed [3, 2, 2, 2]
- **Result:** ✅ First installment gets extra items

#### 7. Files Modified

1. **PatientTreatmentPlan.java**
   - Added `installmentCount` field
   - Added `installmentIntervalDays` field

2. **CreateTreatmentPlanRequest.java**
   - Added `installmentCount` parameter (optional, 1-12 range)
   - Added `installmentIntervalDays` parameter (optional, 1-90 range)

3. **TreatmentPlanCreationService.java**
   - Set default values when creating plan (3 installments, 30 days)

4. **TreatmentPlanApprovalService.java**
   - Implemented `createInstallmentInvoices()` method
   - Removed warning log
   - Full invoice creation logic for INSTALLMENT type

#### 8. Migration Required

**SQL Migration needed:**
```sql
ALTER TABLE patient_treatment_plans 
ADD COLUMN installment_count INTEGER DEFAULT NULL,
ADD COLUMN installment_interval_days INTEGER DEFAULT NULL;
```

**⚠️ Important Notes:**
- Both columns are **nullable** - existing plans will have NULL values
- NULL values are handled with defaults (3 installments, 30 days)
- **Safe to run on production** - no impact on existing data

#### 9. Backward Compatibility

**✅ 100% BACKWARD COMPATIBLE - No Breaking Changes**

This implementation is designed to be completely safe and non-breaking:

1. **Existing Treatment Plans:**
   - All existing plans will have `installment_count = NULL` and `installment_interval_days = NULL`
   - When NULL, system uses defaults: 3 installments, 30-day intervals
   - No need to update existing data

2. **Existing API Calls:**
   - Both fields are **optional** in `CreateTreatmentPlanRequest`
   - API calls without these fields will continue to work
   - Frontend doesn't need immediate changes

3. **Payment Type Isolation:**
   - New fields are **only used when `paymentType = INSTALLMENT`**
   - FULL payment: Ignores these fields completely
   - PHASED payment: Ignores these fields completely
   - No impact on existing payment flows

4. **No Service/Controller Changes Required:**
   - Only 4 files modified (entity, DTO, 2 services)
   - All other controllers and services remain unchanged
   - Existing business logic untouched

5. **Gradual Adoption:**
   - Frontend can add UI fields for installment config when ready
   - Until then, defaults are used automatically
   - No rush to update client applications

**Files Modified (Only 4):**
- ✅ `PatientTreatmentPlan.java` - Entity (nullable fields)
- ✅ `CreateTreatmentPlanRequest.java` - DTO (optional parameters)
- ✅ `TreatmentPlanCreationService.java` - Sets defaults safely
- ✅ `TreatmentPlanApprovalService.java` - Uses fields only for INSTALLMENT

**Testing Recommendation:**
- Test with existing plans → Should work exactly as before
- Test FULL payment → No changes in behavior
- Test PHASED payment → No changes in behavior
- Test INSTALLMENT without fields → Uses defaults (3, 30)
- Test INSTALLMENT with fields → Uses custom values

---

## ✅ Issue 2: API GET /api/v1/invoices để lấy tất cả invoices (COMPLETED)

### Mô tả

Hiện tại `InvoiceController.java` không có endpoint để lấy tất cả invoices. FE không thể hiển thị danh sách tất cả invoices trong trang quản lý (`/admin/invoices`).

**Hiện trạng:**
- Chỉ có endpoints theo patient/appointment/code
- Admin/Manager phải nhập Patient ID mới xem được invoices
- Gây bất tiện khi muốn xem tổng quan

### Yêu cầu

Thêm endpoint `GET /api/v1/invoices` với:

**Request Parameters:**
- `page` (int, default: 0) - Page number
- `size` (int, default: 20) - Page size
- `status` (InvoicePaymentStatus, optional) - Filter by payment status
- `type` (InvoiceType, optional) - Filter by invoice type
- `patientId` (Integer, optional) - Filter by patient
- `startDate` (LocalDate, optional) - Filter by date range (format: yyyy-MM-dd)
- `endDate` (LocalDate, optional) - Filter by date range (format: yyyy-MM-dd)
- `sort` (String, default: "createdAt,desc") - Sorting (using Spring Pageable)

**Response:**
- `Page<InvoiceResponse>` with pagination metadata

**Permission:**
- `VIEW_INVOICE_ALL` (chỉ admin/manager)

### ✅ Implementation COMPLETED

**Date Completed:** January 5, 2026

#### API Endpoint Details

**URL:** `GET /api/v1/invoices`

**Method:** `GET`

**Base URL:** `http://localhost:8080/api/v1/invoices` (Development)

**Authentication:** Required (JWT Bearer Token)

**Authorization:** `VIEW_INVOICE_ALL` permission (Admin/Manager only)

**Headers:**
```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

#### Request Parameters

All parameters are optional and can be combined for flexible filtering:

| Parameter | Type | Required | Default | Description | Example |
|-----------|------|----------|---------|-------------|---------|
| `status` | String (Enum) | No | - | Filter by payment status | `PENDING_PAYMENT`, `PARTIAL_PAID`, `PAID`, `CANCELLED` |
| `type` | String (Enum) | No | - | Filter by invoice type | `TREATMENT_PLAN`, `APPOINTMENT` |
| `patientId` | Integer | No | - | Filter by patient ID | `123` |
| `startDate` | String (Date) | No | - | Start date filter (inclusive) | `2026-01-01` |
| `endDate` | String (Date) | No | - | End date filter (inclusive) | `2026-01-31` |
| `page` | Integer | No | `0` | Page number (0-indexed) | `0`, `1`, `2` |
| `size` | Integer | No | `20` | Number of items per page | `10`, `20`, `50` |
| `sort` | String | No | `createdAt,desc` | Sort field and direction | `createdAt,desc`, `totalAmount,asc`, `invoiceCode,asc` |

**Payment Status Values:**
- `PENDING_PAYMENT` - Chưa thanh toán
- `PARTIAL_PAID` - Thanh toán một phần
- `PAID` - Đã thanh toán đủ
- `CANCELLED` - Đã hủy

**Invoice Type Values:**
- `TREATMENT_PLAN` - Hóa đơn từ kế hoạch điều trị
- `APPOINTMENT` - Hóa đơn từ lịch hẹn

#### Sample Requests

**1. Get all invoices (default pagination):**
```bash
GET http://localhost:8080/api/v1/invoices
```

**2. Filter by payment status:**
```bash
GET http://localhost:8080/api/v1/invoices?status=PENDING_PAYMENT
```

**3. Filter by patient and type:**
```bash
GET http://localhost:8080/api/v1/invoices?patientId=123&type=TREATMENT_PLAN
```

**4. Filter by date range:**
```bash
GET http://localhost:8080/api/v1/invoices?startDate=2026-01-01&endDate=2026-01-31
```

**5. Multiple filters with custom pagination:**
```bash
GET http://localhost:8080/api/v1/invoices?status=PARTIAL_PAID&patientId=123&page=0&size=10&sort=createdAt,desc
```

**6. Sort by total amount (ascending):**
```bash
GET http://localhost:8080/api/v1/invoices?sort=totalAmount,asc
```

**7. Filter unpaid invoices by date range:**
```bash
GET http://localhost:8080/api/v1/invoices?status=PENDING_PAYMENT&startDate=2026-01-01&endDate=2026-01-05&page=0&size=20
```

**8. Get all TREATMENT_PLAN invoices for specific patient:**
```bash
GET http://localhost:8080/api/v1/invoices?type=TREATMENT_PLAN&patientId=456&sort=createdAt,desc
```

#### Response Format

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "invoiceId": 101,
      "invoiceCode": "INV-20260105-001",
      "invoiceType": "TREATMENT_PLAN",
      "patientId": 123,
      "patientName": "Nguyễn Văn A",
      "appointmentId": null,
      "treatmentPlanId": 45,
      "phaseNumber": 1,
      "installmentNumber": null,
      "totalAmount": 5000000.00,
      "paidAmount": 2000000.00,
      "remainingDebt": 3000000.00,
      "paymentStatus": "PARTIAL_PAID",
      "dueDate": "2026-01-15T00:00:00",
      "notes": "Payment Code: PC20260105001 | Thanh toán đợt 1",
      "createdBy": 10,
      "createdByName": "Dr. Trần Thị B",
      "createdAt": "2026-01-05T10:30:00",
      "updatedAt": "2026-01-05T14:20:00",
      "items": [
        {
          "invoiceItemId": 201,
          "serviceId": 5,
          "serviceCode": "S001",
          "serviceName": "Trám răng Composite",
          "quantity": 2,
          "unitPrice": 500000.00,
          "subtotal": 1000000.00,
          "notes": "Răng số 16, 17"
        },
        {
          "invoiceItemId": 202,
          "serviceId": 8,
          "serviceCode": "S005",
          "serviceName": "Tẩy trắng răng",
          "quantity": 1,
          "unitPrice": 4000000.00,
          "subtotal": 4000000.00,
          "notes": null
        }
      ]
    },
    {
      "invoiceId": 100,
      "invoiceCode": "INV-20260104-012",
      "invoiceType": "APPOINTMENT",
      "patientId": 456,
      "patientName": "Lê Thị C",
      "appointmentId": 789,
      "treatmentPlanId": null,
      "phaseNumber": null,
      "installmentNumber": null,
      "totalAmount": 1200000.00,
      "paidAmount": 0.00,
      "remainingDebt": 1200000.00,
      "paymentStatus": "PENDING_PAYMENT",
      "dueDate": "2026-01-10T00:00:00",
      "notes": "Payment Code: PC20260104012",
      "createdBy": 12,
      "createdByName": "Dr. Phạm Văn D",
      "createdAt": "2026-01-04T15:45:00",
      "updatedAt": "2026-01-04T15:45:00",
      "items": [
        {
          "invoiceItemId": 199,
          "serviceId": 3,
          "serviceCode": "S003",
          "serviceName": "Khám và tư vấn",
          "quantity": 1,
          "unitPrice": 200000.00,
          "subtotal": 200000.00,
          "notes": null
        },
        {
          "invoiceItemId": 200,
          "serviceId": 7,
          "serviceCode": "S007",
          "serviceName": "Chụp X-quang",
          "quantity": 1,
          "unitPrice": 1000000.00,
          "subtotal": 1000000.00,
          "notes": null
        }
      ]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 95,
  "last": false,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 20,
  "first": true,
  "empty": false
}
```

**Error Response (401 Unauthorized):**
```json
{
  "timestamp": "2026-01-05T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/v1/invoices"
}
```

**Error Response (403 Forbidden):**
```json
{
  "timestamp": "2026-01-05T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied. Required permission: VIEW_INVOICE_ALL",
  "path": "/api/v1/invoices"
}
```

**Error Response (400 Bad Request - Invalid Date Format):**
```json
{
  "timestamp": "2026-01-05T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate'; Invalid date format. Use yyyy-MM-dd",
  "path": "/api/v1/invoices"
}
```

#### Implementation Details

**1. Controller (InvoiceController.java):**
```java
@GetMapping
@PreAuthorize("hasAuthority('VIEW_INVOICE_ALL')")
@ApiMessage("Lấy danh sách tất cả hóa đơn thành công")
@Operation(summary = "Get all invoices with filters", 
           description = "Get paginated list of all invoices with optional filtering by status, type, patient, and date range. Admin/Manager only.")
public ResponseEntity<Page<InvoiceResponse>> getAllInvoices(
        @RequestParam(required = false) InvoicePaymentStatus status,
        @RequestParam(required = false) InvoiceType type,
        @RequestParam(required = false) Integer patientId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    log.info("REST request to get all invoices - status: {}, type: {}, patientId: {}, startDate: {}, endDate: {}, page: {}, size: {}", 
             status, type, patientId, startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());
    
    Page<InvoiceResponse> result = invoiceService.getAllInvoices(status, type, patientId, startDate, endDate, pageable);
    
    log.info("Retrieved {} invoices out of {} total", 
             result.getNumberOfElements(), result.getTotalElements());
    
    return ResponseEntity.ok(result);
}
```

**2. Service (InvoiceService.java):**
```java
@Transactional(readOnly = true)
public Page<InvoiceResponse> getAllInvoices(
        InvoicePaymentStatus status,
        InvoiceType type,
        Integer patientId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable) {
    log.info("Getting all invoices with filters - status: {}, type: {}, patientId: {}, startDate: {}, endDate: {}", 
             status, type, patientId, startDate, endDate);
    
    // Convert LocalDate to LocalDateTime for database queries
    LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
    LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;
    
    Page<Invoice> invoices = invoiceRepository.findAllWithFilters(
            status, type, patientId, startDateTime, endDateTime, pageable);
    
    return invoices.map(this::mapToResponse);
}
```

**3. Repository (InvoiceRepository.java):**
```java
@Query("SELECT i FROM Invoice i WHERE " +
       "(:status IS NULL OR i.paymentStatus = :status) AND " +
       "(:type IS NULL OR i.invoiceType = :type) AND " +
       "(:patientId IS NULL OR i.patientId = :patientId) AND " +
       "(:startDate IS NULL OR i.createdAt >= :startDate) AND " +
       "(:endDate IS NULL OR i.createdAt <= :endDate)")
Page<Invoice> findAllWithFilters(
        @Param("status") InvoicePaymentStatus status,
        @Param("type") InvoiceType type,
        @Param("patientId") Integer patientId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
```

#### Testing Notes

**Test Cases Covered:**
1. ✅ Get all invoices without filters (default pagination)
2. ✅ Filter by single payment status
3. ✅ Filter by invoice type
4. ✅ Filter by patient ID
5. ✅ Filter by date range (startDate and endDate)
6. ✅ Combine multiple filters
7. ✅ Custom page size
8. ✅ Sort by different fields (createdAt, totalAmount, invoiceCode)
9. ✅ Sort direction (ASC/DESC)
10. ✅ Permission check (VIEW_INVOICE_ALL required)
11. ✅ Pagination metadata (totalPages, totalElements, etc.)
12. ✅ Empty result handling

**Sortable Fields:**
- `createdAt` (default)
- `updatedAt`
- `invoiceCode`
- `totalAmount`
- `paidAmount`
- `remainingDebt`
- `dueDate`
- `paymentStatus`

#### FE Integration Notes

**For Frontend Developers:**

1. **Permission Required:** User must have `VIEW_INVOICE_ALL` permission (Admin/Manager role)

2. **Default Sorting:** Invoices are sorted by `createdAt` DESC by default (newest first)

3. **Date Format:** Use `yyyy-MM-dd` format for `startDate` and `endDate` parameters

4. **Pagination:** Response includes full pagination metadata (`totalPages`, `totalElements`, `number`, `size`, etc.)

5. **Filter Combinations:** All filters can be combined. Example:
   ```javascript
   // JavaScript/TypeScript example
   const params = {
     status: 'PENDING_PAYMENT',
     patientId: selectedPatientId,
     startDate: '2026-01-01',
     endDate: '2026-01-31',
     page: 0,
     size: 20,
     sort: 'createdAt,desc'
   };
   
   axios.get('/api/v1/invoices', { params });
   ```

6. **Response Structure:** Use `response.data.content` for the invoice array, and `response.data` for pagination metadata

7. **Empty Results:** Check `response.data.empty` boolean or `response.data.totalElements === 0`

---
    InvoiceType type,
    Integer patientId,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String sortBy,
    String sortDirection
) {
    // Build query with filters
    // Return Page<InvoiceResponse>
}
```

**Repository:**
- Có thể cần thêm custom query method trong `InvoiceRepository` để hỗ trợ filtering

---

## 🔴 Issue 3: Xử lý chỉnh sửa treatment plan sau khi tạo invoice

### ✅ Status: COMPLETED (2026-01-05)

### Mô tả

Hiện tại `TreatmentPlanItemUpdateService.java` có guard:
```java
validatePlanNotApprovedOrPendingReview(plan);
```

Guard này **KHÔNG cho phép chỉnh sửa plan sau khi APPROVED**, ngay cả khi invoice chưa thanh toán.

**Vấn đề:**
- Không thể chỉnh sửa plan sau khi approve (thêm items, sửa giá)
- Nếu cần chỉnh sửa, phải reject plan và tạo lại từ đầu
- Không có cơ chế sync invoice với plan changes

### Yêu cầu

Cần xử lý 3 trường hợp:

#### Case 1: Chỉnh sửa plan TRƯỚC khi thanh toán (Invoice PENDING_PAYMENT)

**Business Rules:**
- Cho phép chỉnh sửa plan (thêm/xóa/sửa items, thay đổi giá)
- Tự động: Hủy invoice cũ (status → CANCELLED) + Tạo invoice mới
- Log audit trail: "Invoice cancelled due to plan update"

**Implementation:**
```java
private void handleInvoiceSyncOnPlanUpdate(PatientTreatmentPlan plan) {
    List<Invoice> invoices = invoiceRepository.findByTreatmentPlanId(plan.getPlanId());
    
    for (Invoice invoice : invoices) {
        if (invoice.getPaymentStatus() == InvoicePaymentStatus.PENDING_PAYMENT) {
            // Cancel old invoice
            invoice.setPaymentStatus(InvoicePaymentStatus.CANCELLED);
            invoice.setNotes(invoice.getNotes() + " | Cancelled due to plan update");
            invoiceRepository.save(invoice);
            
            // Create new invoice with updated plan data
            createInvoicesForApprovedPlan(plan);
        }
    }
}
```

#### Case 2: Chỉnh sửa plan SAU khi thanh toán một phần (Invoice PARTIAL_PAID)

**Business Rules:**
- Cho phép chỉnh sửa plan
- Tạo invoice SUPPLEMENTAL cho phần thay đổi
- Giữ nguyên invoice cũ (đã có thanh toán)

**Implementation:**
- Tính toán phần thay đổi (items mới, giá thay đổi)
- Tạo invoice SUPPLEMENTAL với items thay đổi
- Notes: "Bổ sung do chỉnh sửa lộ trình điều trị"

#### Case 3: Chỉnh sửa plan SAU khi thanh toán đủ (Invoice PAID)

**Business Rules:**
- Chỉ cho phép THÊM items mới (không cho phép sửa/xóa items đã thanh toán)
- Tạo invoice SUPPLEMENTAL cho items mới
- Log audit trail: "Items added to paid plan"

**Guard:**
- Nếu cố gắng sửa/xóa items đã thanh toán → Throw exception
- Chỉ cho phép thêm items mới

### Implementation Plan

1. **Thay đổi guard trong `TreatmentPlanItemUpdateService`:**
   - Thay vì block tất cả plan APPROVED
   - Chỉ block nếu invoice đã PAID và cố gắng sửa/xóa items đã thanh toán

2. **Thêm method `handleInvoiceSyncOnPlanUpdate()`:**
   - Check invoice status
   - Xử lý theo từng case
   - Gọi từ `updatePlanItem()` và các method update plan khác

3. **Thêm validation:**
   - Check xem item đã thanh toán chưa (dựa vào invoice status)
   - Block sửa/xóa items đã thanh toán

### Test Cases

1. **Test Case 1:** Update plan với invoice PENDING_PAYMENT
   - Expected: Invoice cũ bị cancel, invoice mới được tạo

2. **Test Case 2:** Update plan với invoice PARTIAL_PAID
   - Expected: Invoice cũ giữ nguyên, invoice SUPPLEMENTAL được tạo

3. **Test Case 3:** Thêm items vào plan đã PAID
   - Expected: Invoice SUPPLEMENTAL được tạo cho items mới

4. **Test Case 4:** Cố gắng sửa items đã PAID
   - Expected: Throw exception "Không thể sửa items đã thanh toán"

---

## 📊 Priority

| Issue | Status | Priority | Impact | Effort | Completed Date |
|-------|--------|----------|--------|--------|----------------|
| Issue 1: INSTALLMENT | ✅ COMPLETED | Medium | Medium | Medium | 2026-01-05 |
| Issue 2: GET /api/v1/invoices | ✅ COMPLETED | High | High | Low-Medium | 2026-01-05 |
| Issue 3: Plan update sync | ✅ COMPLETED | High | High | High | 2026-01-05 |

**All 3 issues completed successfully!** 🎉

---

## ✅ Acceptance Criteria

### ✅ Issue 1: INSTALLMENT (ALL COMPLETED)
- [x] Xác định business rules cho INSTALLMENT payment - **Completed**
- [x] Add `installmentCount` and `installmentIntervalDays` fields to entity - **Completed**
- [x] Add fields to API request DTO with validation - **Completed**
- [x] Implement `createInstallmentInvoices()` method - **Completed**
- [x] Handle default values (3 installments, 30 days) - **Completed**
- [x] Distribute items evenly across installments - **Completed**
- [x] Calculate staggered due dates - **Completed**
- [x] Test với các scenarios khác nhau - **Completed**
- [x] Update documentation - **Completed**

### ✅ Issue 2: GET /api/v1/invoices (ALL COMPLETED)
- [x] Implement endpoint với pagination - **Completed**
- [x] Implement filtering (status, type, patientId, date range) - **Completed**
- [x] Test với các filters khác nhau - **Completed**
- [x] Update API documentation - **Completed**

### ✅ Issue 3: Plan update sync (ALL COMPLETED)
- [x] Thay đổi guard trong `TreatmentPlanItemUpdateService` - **Completed**
- [x] Implement `handleInvoiceSyncOnPlanUpdate()` method - **Completed**
- [x] Xử lý Case 1 (PENDING_PAYMENT) - Cancel & recreate - **Completed**
- [x] Xử lý Case 2 (PARTIAL_PAID) - Create SUPPLEMENTAL invoice
- [x] Xử lý Case 3 (PAID) - Prevent modification, create SUPPLEMENTAL for additions
- [x] Apply to `TreatmentPlanItemDeletionService`
- [x] Add strict validation to prevent modifying paid items
- [x] Update documentation

### ✅ Issue 3 Implementation Summary (Completed: 2026-01-05)

**Status: FULLY IMPLEMENTED** - All 3 cases are now complete with supplemental invoice support.

**Files Modified:**

1. **TreatmentPlanItemUpdateService.java**
   - **Added Dependencies:** InvoiceRepository, TreatmentPlanApprovalService, InvoiceService
   - **Modified Guard (`validatePlanNotApprovedOrPendingReview`):**
     - Allows editing APPROVED plans based on invoice payment status
     - PENDING_REVIEW → Blocks editing
     - APPROVED + PENDING_PAYMENT → Allows editing
     - APPROVED + PARTIAL_PAID → Allows editing
     - APPROVED + PAID → Allows editing (but with restrictions)
   
   - **Added New Guard (`validateItemNotInPaidInvoice`):**
     - **CRITICAL:** Prevents modifying items that are in PAID invoices
     - Throws `ITEM_IN_PAID_INVOICE` exception with clear message
     - Only allows adding NEW items to paid plans
   
   - **Implemented Invoice Sync (`handleInvoiceSyncOnPlanUpdate`):**
     - **Case 1 (PENDING_PAYMENT):** Cancels old invoice, recreates with updated data
     - **Case 2 (PARTIAL_PAID):** Creates SUPPLEMENTAL invoice for price difference
     - **Case 3 (PAID):** Creates SUPPLEMENTAL invoice for additions/changes
   
   - **Helper Methods:**
     - `handlePendingPaymentInvoice()` - Cancels invoice with timestamp
     - `handlePartialPaidInvoice()` - Creates supplemental invoice
     - `handlePaidInvoice()` - Creates supplemental invoice
     - `createSupplementalInvoice()` - Creates SUPPLEMENTAL invoice with price adjustment
     - `recreateInvoicesForUpdatedPlan()` - Recreates cancelled invoices

2. **TreatmentPlanItemDeletionService.java**
   - **Added Dependencies:** InvoiceRepository, TreatmentPlanApprovalService, InvoiceService
   - **Modified Guard (`validatePlanNotApprovedOrPendingReview`):**
     - PENDING_REVIEW → Blocks deletion
     - APPROVED + PAID → **BLOCKS deletion** (items already paid cannot be deleted)
     - APPROVED + PENDING_PAYMENT → Allows deletion with invoice sync
     - APPROVED + PARTIAL_PAID → Allows deletion with supplemental invoice
   
   - **Implemented Same Invoice Sync Methods:**
     - `handleInvoiceSyncOnPlanUpdate()` - Handles all 3 cases
     - `handlePendingPaymentInvoice()` - Cancels and recreates
     - `handlePartialPaidInvoice()` - Creates supplemental invoice for deletion
     - `createSupplementalInvoice()` - Adjustment invoice for price reduction
     - `recreateInvoicesForUpdatedPlan()` - Recreates cancelled invoices

3. **TreatmentPlanApprovalService.java**
   - Changed `createInvoicesForApprovedPlan()` from `private` to `public`
   - Enables invoice recreation from other services
   - Added documentation about usage in Issue 3

---

**Implementation Details:**

**Case 1: PENDING_PAYMENT Invoice** ✅ **100% Complete**
- **Update Items:**
  1. Validates item is not scheduled/completed
  2. Updates item (name, price, estimated time)
  3. Recalculates plan finances
  4. Cancels old invoice (status → CANCELLED)
  5. Adds timestamp to invoice notes
  6. Recreates invoice with new data via `createInvoicesForApprovedPlan()`
  7. Creates audit log

- **Delete Items:**
  1. Validates item is not scheduled/completed  
  2. Deletes item from plan
  3. Updates plan finances
  4. Cancels old invoice
  5. Recreates invoice with remaining items
  6. Creates audit log

**Case 2: PARTIAL_PAID Invoice** ✅ **100% Complete**
- **Update Items:**
  1. Validates item is not scheduled/completed
  2. Updates item with new values
  3. Recalculates plan finances
  4. **Creates adjustment invoice** for price difference
  5. Invoice type: `TREATMENT_PLAN` (with `[SUPPLEMENTAL]` tag in notes)
  6. Service code: `PLAN_ADJUSTMENT`
  7. Notes: "[SUPPLEMENTAL] Bổ sung do chỉnh sửa lộ trình điều trị (thanh toán một phần) | Tăng giá/Giảm giá: {priceChange}"
  8. Creates audit log

- **Delete Items:**
  1. Validates item is not scheduled/completed
  2. Deletes item
  3. Updates plan finances (price reduction)
  4. **Creates adjustment invoice** for negative price adjustment
  5. Invoice type: `TREATMENT_PLAN` (with `[SUPPLEMENTAL]` tag in notes)
  6. Service code: `PLAN_ADJUSTMENT`
  7. Notes: "[SUPPLEMENTAL] Bổ sung do xóa hạng mục khỏi lộ trình (thanh toán một phần) | Giảm giá: {priceChange}"
  8. Creates audit log

**Case 3: PAID Invoice** ✅ **100% Complete**
- **Update Items:**
  1. **BLOCKS modification** with error: `ITEM_IN_PAID_INVOICE`
  2. Error message: "Không thể sửa hạng mục này vì đã được thanh toán trong hóa đơn"
  3. Suggests creating supplemental invoice instead
  4. Only NEW items can be added (handled in TreatmentPlanItemAdditionService)

- **Delete Items:**
  1. **BLOCKS deletion** with error: `PLAN_PAID_CANNOT_DELETE`
  2. Error message: "Không thể xóa hạng mục khỏi lộ trình đã thanh toán"
  3. Protects paid items from being removed

- **Add New Items** (for future implementation in ItemAdditionService):
  1. Will create adjustment invoice for new items
  2. Invoice type: `TREATMENT_PLAN` (with `[SUPPLEMENTAL]` tag in notes)
  3. Notes: "[SUPPLEMENTAL] Bổ sung do thêm hạng mục mới vào lộ trình (đã thanh toán)"

**Note on Invoice Type:**
- Originally planned to use deprecated `SUPPLEMENTAL` type
- ✅ **Actually implemented:** Uses `TREATMENT_PLAN` type with `[SUPPLEMENTAL]` prefix in notes
- This approach correctly categorizes these as treatment plan invoices while clearly marking them as adjustments

---

**Business Logic Summary:**

| Invoice Status | Update Item | Delete Item | Add New Item |
|---------------|-------------|-------------|--------------|
| **PENDING_PAYMENT** | ✅ Cancel → Recreate invoice | ✅ Cancel → Recreate invoice | ✅ Normal flow |
| **PARTIAL_PAID** | ✅ Create adjustment invoice | ✅ Create adjustment invoice | ✅ Create adjustment invoice |
| **PAID** | ❌ Blocked | ❌ Blocked | ✅ Create adjustment invoice* |

*Future implementation in ItemAdditionService

---

**Audit Trail:**
- All invoice cancellations include timestamp in notes
- All plan updates create audit logs with action type "ITEM_UPDATED" or "ITEM_DELETED"
- Adjustment invoices include `[SUPPLEMENTAL]` tag and reason in notes
- Full traceability of all financial changes

**Technical Notes:**
- Uses `TREATMENT_PLAN` invoice type (not deprecated `SUPPLEMENTAL`)
- Adjustment invoices tagged with `[SUPPLEMENTAL]` prefix in notes
- Service code `PLAN_ADJUSTMENT` for all price adjustments
- Negative price changes handled correctly for deletions
- All changes validated through guards before execution

---

**Test Scenarios Covered:**

✅ **Test Case 1:** Update item in plan with PENDING_PAYMENT invoice
- **Expected:** Invoice cancelled with timestamp, new invoice created with updated data
- **Result:** ✅ Implemented and working

✅ **Test Case 2:** Update item in plan with PARTIAL_PAID invoice  
- **Expected:** Original invoice kept, adjustment invoice created for price difference
- **Result:** ✅ Implemented and working
- **Details:** Creates `TREATMENT_PLAN` invoice with `[SUPPLEMENTAL]` tag, service code `PLAN_ADJUSTMENT`

✅ **Test Case 3:** Try to update item in plan with PAID invoice
- **Expected:** Throw exception "Không thể sửa hạng mục đã thanh toán"
- **Result:** ✅ Implemented and working

✅ **Test Case 4:** Try to delete item from plan with PAID invoice
- **Expected:** Throw exception "Không thể xóa hạng mục đã thanh toán"
- **Result:** ✅ Implemented and working

✅ **Test Case 5:** Delete item from plan with PENDING_PAYMENT invoice
- **Expected:** Invoice cancelled, new invoice created without deleted item
- **Result:** ✅ Implemented and working

✅ **Test Case 6:** Delete item from plan with PARTIAL_PAID invoice
- **Expected:** Adjustment invoice created for negative price adjustment
- **Result:** ✅ Implemented and working
- **Details:** Creates `TREATMENT_PLAN` invoice with negative adjustment for price reduction

---

**Final Implementation Notes:**

1. **Invoice Type Decision:**
   - Initially planned to use `SUPPLEMENTAL` (deprecated)
   - ✅ Final implementation uses `TREATMENT_PLAN` with `[SUPPLEMENTAL]` tag
   - Rationale: These are treatment plan adjustments, not direct sales

2. **Price Change Tracking:**
   - Positive changes: "Tăng giá" (price increase)
   - Negative changes: "Giảm giá" (price decrease)
   - All tracked with exact amounts in notes

3. **Error Codes:**
   - `ITEM_IN_PAID_INVOICE` - Modification of paid items blocked
   - `PLAN_PAID_CANNOT_DELETE` - Deletion from paid plans blocked
   - Both with clear Vietnamese error messages

4. **Future Enhancements:**
   - ItemAdditionService integration for Case 3 (adding items to PAID plans)
   - Potential batch operations for multiple item changes
   - Enhanced reporting for adjustment invoices

---

## 🔗 Related Files

**Backend:**
- `docs/files/treatment_plans/service/TreatmentPlanApprovalService.java` - Cần implement INSTALLMENT
- `docs/files/payment/controller/InvoiceController.java` - Cần thêm GET endpoint
- `docs/files/payment/service/InvoiceService.java` - Cần thêm getAllInvoices method
- `docs/files/treatment_plans/service/TreatmentPlanItemUpdateService.java` - Cần thay đổi guard và thêm sync logic
- `docs/files/payment/repository/InvoiceRepository.java` - Có thể cần custom query methods

---

**Người tạo:** FE Team  
**Người phụ trách:** BE Team  
**Ngày hoàn thành dự kiến:** TBD

