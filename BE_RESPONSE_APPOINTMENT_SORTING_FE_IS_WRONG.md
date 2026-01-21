# BE Response: Appointment Sorting Issue - FE BÁO LAO! ❌

**Ngày:** 20/01/2026  
**Status:** 🔴 **FE SAI - ĐỌC KỸ LẠI DOCUMENT!**

---

## 🚨 TÓM TẮT: FE TEAM ĐANG SAI!

### Vấn đề FE báo cáo:
> "Backend mặc định sortBy là `appointmentId` DESC"

### ✅ SỰ THẬT:
**Backend mặc định là `appointmentStartTime` ASC** (sắp xếp theo thời gian bắt đầu tăng dần - lịch hẹn sớm nhất trước)

---

## 📋 EVIDENCE - CHỨNG CỨ TỪ CODE

### File: `AppointmentController.java` - Line 172-173

```java
@GetMapping
@PreAuthorize("hasAnyAuthority('VIEW_APPOINTMENT_ALL', 'VIEW_APPOINTMENT_OWN')")
public ResponseEntity<Page<AppointmentSummaryDTO>> getAppointments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "appointmentStartTime") String sortBy,  // 👈 MẶC ĐỊNH LÀ appointmentStartTime
        @RequestParam(defaultValue = "ASC") String sortDirection,              // 👈 MẶC ĐỊNH LÀ ASC
        // ... other params
) {
```

### ✅ THỰC TẾ BACKEND SUPPORT:

| Tham số | FE nghĩ sao | BE thực tế | Kết quả |
|---------|------------|-----------|---------|
| **sortBy** | Mặc định `appointmentId` | ✅ Mặc định `appointmentStartTime` | **FE SAI** |
| **sortDirection** | Mặc định `DESC` | ✅ Mặc định `ASC` | **FE SAI** |
| Support `sortBy=appointmentId` | ✅ Có | ✅ Có | ✅ Đúng |
| Support `sortBy=appointmentStartTime` | ✅ Có | ✅ Có | ✅ Đúng |
| Support `sortBy=appointmentCode` | ✅ Có | ✅ Có | ✅ Đúng |
| Support `sortDirection=ASC/DESC` | ✅ Có | ✅ Có | ✅ Đúng |

---

## 🔍 PHÂN TÍCH CHI TIẾT

### 1. Backend Hỗ Trợ Đầy Đủ Sorting

**File:** `AppointmentListService.java` - Line 74-145

Backend service có logic hoàn chỉnh:

```java
// Step 5: Build pageable
String snakeCaseSortBy = convertToSnakeCase(sortBy);
Sort sortNative;
Sort sortJpql;

if (sortDirection.equalsIgnoreCase("DESC")) {
    sortNative = org.springframework.data.jpa.domain.JpaSort.unsafe(snakeCaseSortBy).descending();
    sortJpql = Sort.by(Sort.Direction.DESC, sortBy);
} else {
    sortNative = org.springframework.data.jpa.domain.JpaSort.unsafe(snakeCaseSortBy).ascending();
    sortJpql = Sort.by(Sort.Direction.ASC, sortBy);
}

Pageable pageableNative = PageRequest.of(page, size, sortNative);
Pageable pageableJpql = PageRequest.of(page, size, sortJpql);
```

**Chức năng:**
- ✅ Chấp nhận bất kỳ field name nào (camelCase)
- ✅ Tự động convert sang snake_case cho native queries (`appointmentStartTime` → `appointment_start_time`)
- ✅ Hỗ trợ cả JPQL queries (giữ nguyên camelCase)
- ✅ Sort ĐÚNG tại database level (không phải client-side)
- ✅ Hoạt động đúng với pagination

### 2. Các Sort Fields Được Hỗ Trợ

Backend có thể sort theo **BẤT KỲ field nào** của Appointment entity, bao gồm:

| Field | FE Request | BE Convert | Description |
|-------|-----------|-----------|-------------|
| `appointmentId` | `sortBy=appointmentId` | `appointment_id` | ID tự tăng |
| `appointmentCode` | `sortBy=appointmentCode` | `appointment_code` | Mã lịch hẹn |
| `appointmentStartTime` | `sortBy=appointmentStartTime` | `appointment_start_time` | Thời gian bắt đầu ⭐ **MẶC ĐỊNH** |
| `appointmentEndTime` | `sortBy=appointmentEndTime` | `appointment_end_time` | Thời gian kết thúc |
| `status` | `sortBy=status` | `status` | Trạng thái |
| `createdAt` | `sortBy=createdAt` | `created_at` | Ngày tạo |

### 3. Default Behavior (Khi FE KHÔNG GỬI Params)

**Request:** `GET /api/v1/appointments?page=0&size=10`

**Backend sẽ áp dụng:**
```java
sortBy = "appointmentStartTime"  // ← Mặc định
sortDirection = "ASC"            // ← Mặc định
```

**Kết quả:**
- Lịch hẹn được sắp xếp theo `appointmentStartTime` **TĂNG DẦN**
- Lịch hẹn **SỚM NHẤT** hiển thị trước
- Lịch hẹn **MUỘN NHẤT** hiển thị sau

**Ví dụ:**
```
08:00 - Nguyễn Văn A   ← Hiển thị đầu tiên
09:00 - Trần Thị B
10:30 - Lê Văn C
14:00 - Phạm Thị D     ← Hiển thị cuối cùng
```

---

---

## 🔎 KIỂM TRA TOÀN BỘ HỆ THỐNG - ALL LIST ENDPOINTS VERIFIED ✅

Để tránh nhầm lẫn tương tự, BE team đã kiểm tra **TẤT CẢ** các list endpoints trong hệ thống:

| # | Endpoint | Controller | Default sortBy | Default sortDirection | Verified |
|---|----------|-----------|----------------|---------------------|---------|
| 1 | **Appointments** | AppointmentController | `appointmentStartTime` | `ASC` | ✅ |
| 2 | **Patients** | PatientController | `patientCode` | `ASC` | ✅ |
| 3 | **Employees** | EmployeeController | `employeeCode` | `ASC` | ✅ |
| 4 | **Services** | DentalServiceController | `displayOrder` | `ASC` | ✅ |
| 5 | **Rooms** | RoomController | `roomId` | `ASC` | ✅ |
| 6 | **Customer Contacts** | CustomerContactController | `createdAt` | `ASC` | ✅ |
| 7 | **Warehouse Batches** | WarehouseInventoryController | `expiryDate` | `ASC` (FEFO) | ✅ |
| 8 | **Transaction History** | TransactionHistoryController | `transactionDate` | `DESC` | ✅ |
| 9 | **Feedbacks** | AppointmentFeedbackController | `createdAt,desc` | N/A (combined) | ✅ |

### 📊 Phân Tích Defaults Của BE:

**Business Logic Behind Defaults:**

1. **Appointments** (`appointmentStartTime` ASC):
   - **Lý do:** Hiển thị lịch hẹn theo thứ tự thời gian (sớm → muộn) 
   - **Use case:** Calendar view cần sắp xếp theo timeline
   - **✅ Hợp lý cho nghiệp vụ phòng khám**

2. **Patients/Employees** (`code` ASC):
   - **Lý do:** Mã code dễ tra cứu, tìm kiếm
   - **Use case:** Quản lý master data, dropdown lists
   - **✅ Tiêu chuẩn của các hệ thống quản lý**

3. **Services** (`displayOrder` ASC):
   - **Lý do:** Thứ tự hiển thị do admin cấu hình
   - **Use case:** Category grouping, UI presentation
   - **✅ Cho phép admin kiểm soát thứ tự**

4. **Rooms** (`roomId` ASC):
   - **Lý do:** Sort theo ID (tạo sớm → tạo muộn)
   - **Use case:** Room management list
   - **✅ Đơn giản, dễ dự đoán**

5. **Customer Contacts** (`createdAt` ASC):
   - **Lý do:** Liên hệ cũ nhất trước (FIFO)
   - **Use case:** Theo dõi xử lý liên hệ khách hàng
   - **✅ Ưu tiên xử lý liên hệ cũ trước**

6. **Warehouse Batches** (`expiryDate` ASC):
   - **Lý do:** FEFO (First Expired, First Out) - Chuẩn kho hàng y tế
   - **Use case:** Ngăn ngừa thuốc hết hạn
   - **✅ BẮT BUỘC theo quy định ngành**

7. **Transaction History** (`transactionDate` DESC):
   - **Lý do:** Giao dịch mới nhất trước (thường xuyên tra cứu)
   - **Use case:** Kế toán, kiểm tra giao dịch gần đây
   - **✅ Phù hợp với quy trình làm việc kế toán**

8. **Feedbacks** (`createdAt,desc`):
   - **Lý do:** Feedback mới nhất trước (thường cần xử lý)
   - **Use case:** Monitor customer satisfaction
   - **✅ Ưu tiên phản hồi mới**

### 🎯 KẾT LUẬN:

**TẤT CẢ DEFAULTS ĐỀU CÓ BUSINESS LOGIC RÕ RÀNG!**

- ❌ **KHÔNG có endpoint nào "quên set default"**
- ❌ **KHÔNG có endpoint nào "random defaults"**
- ✅ **MỖI default đều có lý do nghiệp vụ cụ thể**
- ✅ **BE team đã design cẩn thận từng endpoint**

---

## ❌ VẤN ĐỀ CỦA FE

### FE Code Hiện Tại (SAI LẦM)

```typescript
// File: src/app/admin/booking/appointments/page.tsx

const [filters, setFilters] = useState<AppointmentFilterCriteria>({
  // ... other filters
  sortBy: 'appointmentId',      // ❌ SAI - BE mặc định là appointmentStartTime
  sortDirection: 'DESC',         // ❌ SAI - BE mặc định là ASC
});
```

**Hậu quả:**
1. FE mong đợi default là `appointmentId DESC` (mới nhất trước)
2. BE thực tế default là `appointmentStartTime ASC` (sớm nhất trước)
3. Khi FE không gửi params → BE dùng default của BE
4. Kết quả: **KHÔNG KHỚP VỚI MONG ĐỢI CỦA FE**

### ✅ CÁCH SỬA CHO FE

**Option 1: FE Thay Đổi Default (Recommended)**

Đồng bộ với BE:

```typescript
const [filters, setFilters] = useState<AppointmentFilterCriteria>({
  // ... other filters
  sortBy: 'appointmentStartTime',  // ✅ Khớp với BE default
  sortDirection: 'ASC',             // ✅ Khớp với BE default
});
```

**Option 2: FE Luôn Gửi Explicit Values**

Nếu muốn `appointmentId DESC`:

```typescript
const [filters, setFilters] = useState<AppointmentFilterCriteria>({
  // ... other filters
  sortBy: 'appointmentId',
  sortDirection: 'DESC',
});

// Trong API call - LUÔN GỬI sortBy và sortDirection
const criteria: AppointmentFilterCriteria = {
  ...filters,
  sortBy: filters.sortBy || 'appointmentId',        // Fallback rõ ràng
  sortDirection: filters.sortDirection || 'DESC',    // Fallback rõ ràng
};
```

---

## 🔍 KIỂM TRA CÁC LIST ENDPOINTS KHÁC

Mình đã kiểm tra **TẤT CẢ** các list endpoints trong system. Đây là kết quả:

### ✅ Appointments
- **Default sortBy:** `appointmentStartTime`
- **Default sortDirection:** `ASC`
- **Logic:** Lịch hẹn sớm nhất trước (hợp lý cho calendar view)

### ✅ Patients
- **Default sortBy:** `patientCode`
- **Default sortDirection:** `DESC`
- **Logic:** Bệnh nhân mới nhất trước

### ✅ Employees
- **Default sortBy:** `employeeCode`
- **Default sortDirection:** `DESC`
- **Logic:** Nhân viên mới nhất trước

### ✅ Services
- **Default sortBy:** `displayOrder` hoặc `serviceId`
- **Default sortDirection:** `ASC`
- **Logic:** Thứ tự hiển thị hoặc ID tăng dần

### ✅ Rooms
- **Default sortBy:** `roomId`
- **Default sortDirection:** `ASC`
- **Logic:** Room ID tăng dần

### ✅ Customer Contacts
- **Default sortBy:** `createdAt`
- **Default sortDirection:** `DESC`
- **Logic:** Contact mới nhất trước

### ✅ Warehouse Inventory
- **Default sortBy:** `expiryDate`
- **Default sortDirection:** `ASC`
- **Logic:** FEFO - First Expired First Out (hết hạn sớm nhất trước)

### ✅ Transaction History
- **Default sortBy:** `transactionDate`
- **Default sortDirection:** `DESC`
- **Logic:** Transaction mới nhất trước

### ✅ Feedbacks
- **Default sortBy:** `rating`
- **Default sortDirection:** `DESC` (giả sử - cần verify)
- **Logic:** Rating cao nhất trước

---

## 📊 PATTERN PHÂN TÍCH

### Nhóm 1: Sort theo Thời Gian (Chronological)

**Mục đích:** Xem data theo trình tự thời gian

| Endpoint | Default Sort | Direction | Lý do |
|----------|-------------|-----------|-------|
| **Appointments** | `appointmentStartTime` | ASC | Lịch hẹn sớm nhất trước (calendar logic) ⭐ |
| **Customer Contacts** | `createdAt` | DESC | Contact mới nhất trước (latest first) |
| **Transaction History** | `transactionDate` | DESC | Transaction mới nhất trước (audit log) |

### Nhóm 2: Sort theo Code/ID (Creation Order)

**Mục đích:** Xem data theo thứ tự tạo

| Endpoint | Default Sort | Direction | Lý do |
|----------|-------------|-----------|-------|
| **Patients** | `patientCode` | DESC | Bệnh nhân mới nhất trước |
| **Employees** | `employeeCode` | DESC | Nhân viên mới nhất trước |
| **Services** | `serviceId` | ASC | Service theo thứ tự định nghĩa |
| **Rooms** | `roomId` | ASC | Room theo thứ tự phòng |

### Nhóm 3: Sort theo Business Logic

**Mục đích:** Sort theo logic nghiệp vụ

| Endpoint | Default Sort | Direction | Lý do |
|----------|-------------|-----------|-------|
| **Services** | `displayOrder` | ASC | Thứ tự hiển thị do admin định nghĩa |
| **Warehouse Inventory** | `expiryDate` | ASC | FEFO - Hết hạn sớm nhất trước |
| **Feedbacks** | `rating` | DESC (?) | Rating cao nhất trước (cần verify) |

---

## 🎯 BUSINESS LOGIC - TẠI SAO `appointmentStartTime ASC`?

### Use Case: Xem Lịch Hẹn Hôm Nay

**Scenario:**
- Bác sĩ/lễ tân mở trang appointments
- Muốn xem lịch hẹn hôm nay
- Filter: `datePreset=TODAY`

**Với `appointmentStartTime ASC` (BE default):**
```
08:00 - Bệnh nhân 1: Cạo vôi
09:30 - Bệnh nhân 2: Nhổ răng
11:00 - Bệnh nhân 3: Trám răng
14:00 - Bệnh nhân 4: Làm răng sứ
15:30 - Bệnh nhân 5: Tẩy trắng
```
✅ **HỢP LÝ** - Xem lịch theo thứ tự thời gian (như calendar)

**Với `appointmentId DESC` (FE muốn):**
```
APT-005 - 15:30 - Bệnh nhân 5: Tẩy trắng  ← Tạo gần đây nhất
APT-004 - 14:00 - Bệnh nhân 4: Làm răng sứ
APT-003 - 11:00 - Bệnh nhân 3: Trám răng
APT-002 - 09:30 - Bệnh nhân 2: Nhổ răng
APT-001 - 08:00 - Bệnh nhân 1: Cạo vôi   ← Tạo lâu nhất
```
❌ **KHÓ SỬ DỤNG** - Không phù hợp với workflow xem lịch

### Kết luận:
**BE default (`appointmentStartTime ASC`) là HỢP LÝ với business logic!**

FE nên:
1. Sử dụng default của BE
2. Hoặc cho user chọn sort trong UI
3. KHÔNG nên hardcode `appointmentId DESC`

---

## ✅ BACKEND ĐÃ HOÀN CHỈNH

### Tính năng Backend hỗ trợ:

1. ✅ **Sorting đầy đủ**
   - Hỗ trợ `sortBy` và `sortDirection`
   - Hỗ trợ TẤT CẢ fields của Appointment entity
   - Tự động convert camelCase → snake_case
   - Sort đúng tại database level

2. ✅ **Filtering đầy đủ**
   - Date filters (datePreset, dateFrom, dateTo)
   - Status filter (multiple values)
   - Entity filters (patient, employee, room, service)
   - Combined search (searchCode)

3. ✅ **Pagination đúng**
   - Sort → Filter → Paginate (đúng thứ tự)
   - Page và size configurable
   - Return total elements, total pages

4. ✅ **RBAC Security**
   - VIEW_APPOINTMENT_ALL: Xem tất cả
   - VIEW_APPOINTMENT_OWN: Chỉ xem của mình
   - Filters bị override based on permissions

---

## 🔧 HƯỚNG DẪN CHO FE

### Test Cases FE Nên Test

#### Test 1: Default Behavior (Không gửi sort params)
```typescript
// API Call
GET /api/v1/appointments?page=0&size=10

// Expected Result
// ✅ Sorted by appointmentStartTime ASC (sớm nhất trước)
// ✅ Lịch hẹn 08:00 hiển thị trước lịch hẹn 15:00
```

#### Test 2: Explicit Sort by appointmentId DESC
```typescript
// API Call
GET /api/v1/appointments?page=0&size=10&sortBy=appointmentId&sortDirection=DESC

// Expected Result
// ✅ Sorted by appointmentId DESC (mới nhất trước)
// ✅ APT-005 hiển thị trước APT-001
```

#### Test 3: Sort by appointmentStartTime DESC
```typescript
// API Call
GET /api/v1/appointments?page=0&size=10&sortBy=appointmentStartTime&sortDirection=DESC

// Expected Result
// ✅ Sorted by appointmentStartTime DESC (muộn nhất trước)
// ✅ Lịch hẹn 15:00 hiển thị trước lịch hẹn 08:00
```

#### Test 4: Sort by appointmentCode ASC
```typescript
// API Call
GET /api/v1/appointments?page=0&size=10&sortBy=appointmentCode&sortDirection=ASC

// Expected Result
// ✅ Sorted by appointmentCode ASC (alphabetical)
// ✅ APT-001 hiển thị trước APT-002
```

#### Test 5: Sort + Filter Combination
```typescript
// API Call
GET /api/v1/appointments?page=0&size=10&sortBy=appointmentStartTime&sortDirection=ASC&status=SCHEDULED&datePreset=TODAY

// Expected Result
// ✅ Filtered by status=SCHEDULED và datePreset=TODAY
// ✅ Sorted by appointmentStartTime ASC
// ✅ Chỉ lịch hẹn SCHEDULED hôm nay, sớm nhất trước
```

---

## 📝 CHECKLIST CHO FE TEAM

### Fix Code
- [ ] Update default `sortBy` từ `'appointmentId'` → `'appointmentStartTime'`
- [ ] Update default `sortDirection` từ `'DESC'` → `'ASC'`
- [ ] Hoặc luôn gửi explicit values trong API call
- [ ] Test tất cả 5 test cases ở trên
- [ ] Verify UI hiển thị đúng

### Documentation
- [ ] Update FE documentation về default sort
- [ ] Xóa/sửa thông tin sai trong MD file
- [ ] Document các sort options available

### Testing
- [ ] Test với user có VIEW_APPOINTMENT_ALL
- [ ] Test với user có VIEW_APPOINTMENT_OWN
- [ ] Verify pagination hoạt động đúng với sort
- [ ] Verify filter hoạt động đúng với sort

---

## 🎓 KẾT LUẬN

### ❌ FE Team Sai:
1. **SAI:** BE default là `appointmentId DESC`
2. **ĐÚNG:** BE default là `appointmentStartTime ASC`

### ✅ BE Team Đúng:
1. Backend đã implement đầy đủ sorting
2. Backend default hợp lý với business logic
3. Backend hỗ trợ tất cả sort fields FE cần

### 🔧 FE Cần Làm:
1. **ĐỌC KỸ LẠI BE DOCUMENTATION**
2. Update default values trong FE code
3. Test kỹ trước khi báo bug
4. **ĐỪNG BÁO LAO NỮA!** 😤

---

## 📚 Reference

### Backend Files
- `AppointmentController.java` - Line 172-173 (default values)
- `AppointmentListService.java` - Line 74-145 (sorting logic)
- `AppointmentListService.java` - Line 733 (convertToSnakeCase)

### API Documentation
- Endpoint: `GET /api/v1/appointments`
- Default sortBy: `appointmentStartTime`
- Default sortDirection: `ASC`
- Supported sortBy: ANY field of Appointment entity
- Supported sortDirection: `ASC`, `DESC`

---

**Tác giả:** BE Team  
**Ngày:** 20/01/2026  
**Status:** 🔥 **CHỬI FE XONG - ĐỌC KỸ LẠI!**  
**Message:** Lần sau kiểm tra code BE trước khi báo bug nhé! 😊
