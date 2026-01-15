# Yêu cầu cập nhật Dashboard Excel Export

## ✅ IMPLEMENTATION COMPLETED - January 14, 2026

**Đã hoàn thành:**
- ✅ Thêm Feedbacks tab vào Dashboard Export (Excel & CSV)
- ✅ Endpoint mới: `GET /api/v1/dashboard/feedbacks`
- ✅ Export endpoints hỗ trợ: `feedbacks` và `all` (6 tabs)
- ✅ Đầy đủ statistics: Overall stats, Top 10 doctors, rating breakdowns

**Files đã sửa:**
1. `DashboardController.java` - Added `/feedbacks` endpoint
2. `DashboardService.java` - Added feedback service integration
3. `DashboardExportService.java` - Added `exportFeedbacks()` và `exportFeedbacksCSV()`

**Ready for FE Testing!** 🎉

---

## 📊 Tổng quan

Hiện tại hệ thống BE Dashboard Export đang thiếu tab **Feedbacks** và cần bổ sung thêm một số trường dữ liệu để khớp với những gì FE đang hiển thị.

### Vấn đề chính

1. **Tab Feedbacks bị thiếu hoàn toàn trong export**
   - FE có 6 tabs: Overview, Revenue/Expenses, Employees, Warehouse, Transactions, **Feedbacks**
   - BE chỉ export 5 tabs (không có Feedbacks)
   - Controller không có endpoint `/api/v1/dashboard/feedbacks` để lấy dữ liệu feedback

2. **Export không có option cho feedback**
   - Endpoint: `GET /api/v1/dashboard/export/{tab}` chỉ support: `overview`, `revenue-expenses`, `employees`, `warehouse`, `transactions`, `all`
   - Cần thêm: `feedbacks` vào danh sách tabs được export

---

## 🎯 Yêu cầu cập nhật

### 1. Thêm Feedbacks Tab vào Export

#### A. Tạo endpoint API mới cho Feedbacks data

**File cần sửa:** `DashboardController.java`

```java
@GetMapping("/feedbacks")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@Operation(summary = "Get feedback statistics",
           description = "Get doctor feedback and rating statistics")
public ResponseEntity<FeedbackStatisticsResponse> getFeedbackStatistics(
        @RequestParam(required = false) String month,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(defaultValue = "10") int topDoctors,
        @RequestParam(defaultValue = "rating") String sortBy) {
    FeedbackStatisticsResponse response = feedbackService.getFeedbackStatistics(
        month, startDate, endDate, topDoctors, sortBy);
    return ResponseEntity.ok(response);
}
```

#### B. Thêm method export cho Feedbacks

**File cần sửa:** `DashboardExportService.java`

Thêm vào method `exportToExcel()`:

```java
public byte[] exportToExcel(String tab, String month, LocalDate startDate, LocalDate endDate) {
    log.info("Exporting tab: {} - month: {}, startDate: {}, endDate: {}", tab, month, startDate, endDate);
    
    try (Workbook workbook = new XSSFWorkbook();
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        
        switch (tab.toLowerCase()) {
            case "overview" -> exportOverview(workbook, month, startDate, endDate);
            case "revenue-expenses" -> exportRevenueExpenses(workbook, month, startDate, endDate);
            case "employees" -> exportEmployees(workbook, month, startDate, endDate);
            case "warehouse" -> exportWarehouse(workbook, month, startDate, endDate);
            case "transactions" -> exportTransactions(workbook, month, startDate, endDate);
            case "feedbacks" -> exportFeedbacks(workbook, month, startDate, endDate); // ✅ THÊM MỚI
            default -> throw new IllegalArgumentException("Invalid tab: " + tab);
        }
        
        workbook.write(out);
        return out.toByteArray();
        
    } catch (IOException e) {
        log.error("Error exporting dashboard to Excel", e);
        throw new RuntimeException("Failed to export dashboard to Excel", e);
    }
}
```

**Thêm method mới `exportFeedbacks()`:**

```java
private void exportFeedbacks(Workbook workbook, String month, LocalDate startDate, LocalDate endDate) {
    FeedbackStatisticsResponse data = feedbackService.getFeedbackStatistics(month, startDate, endDate, 10, "rating");
    Sheet sheet = workbook.createSheet("Feedbacks");
    
    CellStyle headerStyle = createHeaderStyle(workbook);
    CellStyle percentStyle = createPercentStyle(workbook);
    
    int rowNum = 0;
    
    // Title
    Row titleRow = sheet.createRow(rowNum++);
    Cell titleCell = titleRow.createCell(0);
    titleCell.setCellValue("Feedback & Ratings Statistics - " + month);
    titleCell.setCellStyle(createTitleStyle(workbook));
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
    rowNum++;
    
    // Overall Statistics
    createSectionHeader(sheet, rowNum++, "Overall Statistics", headerStyle);
    rowNum = addDataRow(sheet, rowNum, "Total Doctors", data.getTotalDoctors());
    rowNum = addDataRow(sheet, rowNum, "Average Rating", data.getAverageRating(), percentStyle);
    rowNum = addDataRow(sheet, rowNum, "Total Feedbacks", data.getTotalFeedbacks());
    rowNum = addDataRow(sheet, rowNum, "Top Rated Doctors (≥4.5)", data.getTopRatedCount());
    rowNum++;
    
    // Top Doctors by Rating
    if (!data.getDoctors().isEmpty()) {
        createSectionHeader(sheet, rowNum++, "Top Doctors by Rating", headerStyle);
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Rank");
        headerRow.createCell(1).setCellValue("Employee Code");
        headerRow.createCell(2).setCellValue("Doctor Name");
        headerRow.createCell(3).setCellValue("Specialization");
        headerRow.createCell(4).setCellValue("Average Rating");
        headerRow.createCell(5).setCellValue("Total Feedbacks");
        headerRow.createCell(6).setCellValue("5-Star");
        headerRow.createCell(7).setCellValue("4-Star");
        headerRow.createCell(8).setCellValue("3-Star");
        headerRow.createCell(9).setCellValue("2-Star");
        headerRow.createCell(10).setCellValue("1-Star");
        
        for (Cell cell : headerRow) {
            cell.setCellStyle(headerStyle);
        }
        
        int rank = 1;
        for (DoctorStatistics doctor : data.getDoctors()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rank++);
            row.createCell(1).setCellValue(doctor.getEmployeeCode());
            row.createCell(2).setCellValue(doctor.getEmployeeName());
            row.createCell(3).setCellValue(doctor.getSpecialization());
            
            Cell ratingCell = row.createCell(4);
            ratingCell.setCellValue(doctor.getStatistics().getAverageRating());
            
            row.createCell(5).setCellValue(doctor.getStatistics().getTotalFeedbacks());
            row.createCell(6).setCellValue(doctor.getStatistics().getRating5Count());
            row.createCell(7).setCellValue(doctor.getStatistics().getRating4Count());
            row.createCell(8).setCellValue(doctor.getStatistics().getRating3Count());
            row.createCell(9).setCellValue(doctor.getStatistics().getRating2Count());
            row.createCell(10).setCellValue(doctor.getStatistics().getRating1Count());
        }
    }
    
    autoSizeColumns(sheet, 11);
}
```

**Thêm CSV export cho Feedbacks:**

```java
private void exportFeedbacksCSV(CSVPrinter csv, String month, LocalDate startDate, LocalDate endDate) throws IOException {
    FeedbackStatisticsResponse data = feedbackService.getFeedbackStatistics(month, startDate, endDate, 10, "rating");
    
    csv.printRecord("Feedback & Ratings Statistics", month);
    csv.println();
    
    csv.printRecord("Overall Statistics");
    csv.printRecord("Total Doctors", data.getTotalDoctors());
    csv.printRecord("Average Rating", data.getAverageRating());
    csv.printRecord("Total Feedbacks", data.getTotalFeedbacks());
    csv.printRecord("Top Rated Doctors (≥4.5)", data.getTopRatedCount());
    csv.println();
    
    csv.printRecord("Top Doctors by Rating");
    csv.printRecord("Rank", "Employee Code", "Doctor Name", "Specialization", "Avg Rating", 
                   "Total Feedbacks", "5-Star", "4-Star", "3-Star", "2-Star", "1-Star");
    
    int rank = 1;
    for (DoctorStatistics doctor : data.getDoctors()) {
        csv.printRecord(
            rank++,
            doctor.getEmployeeCode(),
            doctor.getEmployeeName(),
            doctor.getSpecialization(),
            String.format("%.2f", doctor.getStatistics().getAverageRating()),
            doctor.getStatistics().getTotalFeedbacks(),
            doctor.getStatistics().getRating5Count(),
            doctor.getStatistics().getRating4Count(),
            doctor.getStatistics().getRating3Count(),
            doctor.getStatistics().getRating2Count(),
            doctor.getStatistics().getRating1Count()
        );
    }
}
```

#### C. Cập nhật method `exportAllTabs()`

**File cần sửa:** `DashboardExportService.java`

```java
public byte[] exportAllTabs(String month, LocalDate startDate, LocalDate endDate) {
    log.info("Exporting all dashboard tabs - month: {}, startDate: {}, endDate: {}", month, startDate, endDate);
    
    try (Workbook workbook = new XSSFWorkbook();
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        
        // Export all 6 tabs to one workbook (✅ THAY ĐỔI: 5 → 6)
        exportOverview(workbook, month, startDate, endDate);
        exportRevenueExpenses(workbook, month, startDate, endDate);
        exportEmployees(workbook, month, startDate, endDate);
        exportWarehouse(workbook, month, startDate, endDate);
        exportTransactions(workbook, month, startDate, endDate);
        exportFeedbacks(workbook, month, startDate, endDate); // ✅ THÊM MỚI
        
        workbook.write(out);
        return out.toByteArray();
        
    } catch (IOException e) {
        log.error("Error exporting all dashboard tabs to Excel", e);
        throw new RuntimeException("Failed to export all dashboard tabs to Excel", e);
    }
}
```

#### D. Cập nhật CSV export

**File cần sửa:** `DashboardExportService.java`

Thêm vào method `exportToCSV()`:

```java
public String exportToCSV(String tab, String month, LocalDate startDate, LocalDate endDate) {
    log.info("Exporting tab: {} to CSV - month: {}, startDate: {}, endDate: {}", tab, month, startDate, endDate);
    
    try (StringWriter sw = new StringWriter();
         CSVPrinter csvPrinter = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
        
        switch (tab.toLowerCase()) {
            case "overview" -> exportOverviewCSV(csvPrinter, month, startDate, endDate);
            case "revenue-expenses" -> exportRevenueExpensesCSV(csvPrinter, month, startDate, endDate);
            case "employees" -> exportEmployeesCSV(csvPrinter, month, startDate, endDate);
            case "warehouse" -> exportWarehouseCSV(csvPrinter, month, startDate, endDate);
            case "transactions" -> exportTransactionsCSV(csvPrinter, month, startDate, endDate);
            case "feedbacks" -> exportFeedbacksCSV(csvPrinter, month, startDate, endDate); // ✅ THÊM MỚI
            default -> throw new IllegalArgumentException("Invalid tab: " + tab);
        }
        
        csvPrinter.flush();
        return sw.toString();
        
    } catch (IOException e) {
        log.error("Error exporting dashboard to CSV", e);
        throw new RuntimeException("Failed to export dashboard to CSV", e);
    }
}
```

---

## 📋 Dữ liệu FE đang hiển thị (để tham khảo)

### Feedbacks Tab (FE)

**Nguồn:** `src/components/dashboard/FeedbacksTab.tsx`

FE hiện đang gọi API: `GET /api/v1/appointment-feedbacks/statistics-by-doctor`

**Parameters:**
- `startDate`: string
- `endDate`: string
- `top`: number (default: 10)
- `sortBy`: "rating" | "feedbacks" (default: "rating")

**Response hiện tại từ appointmentFeedbackService:**

```typescript
interface DoctorStatistics {
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  specialization: string;
  avatar?: string;
  statistics: {
    averageRating: number;        // ⭐ 1-5
    totalFeedbacks: number;       // Tổng số feedback
    rating5Count: number;         // Số lượng 5 sao
    rating4Count: number;         // Số lượng 4 sao
    rating3Count: number;         // Số lượng 3 sao
    rating2Count: number;         // Số lượng 2 sao
    rating1Count: number;         // Số lượng 1 sao
  };
}

interface FeedbackStatisticsResponse {
  doctors: DoctorStatistics[];
}
```

**FE tính toán thêm:**
```typescript
// Overall Statistics (tính từ danh sách doctors)
const stats = {
  totalDoctors: doctors.length,
  avgRating: average(doctors.map(d => d.statistics.averageRating)),
  totalFeedbacks: sum(doctors.map(d => d.statistics.totalFeedbacks)),
  topRatedCount: doctors.filter(d => d.statistics.averageRating >= 4.5).length
};
```

**Hiển thị:**
1. **Overall Summary Cards** (4 thẻ tổng quan):
   - Total Doctors with Feedback
   - Average Rating (all doctors)
   - Total Feedbacks
   - Top Rated Doctors (≥4.5 stars)

2. **Doctor Cards Grid** (Top 10):
   - Rank badge (1st, 2nd, 3rd có màu đặc biệt)
   - Avatar
   - Doctor Name
   - Specialization
   - Average Rating (stars visualization)
   - Total Feedbacks count
   - Rating breakdown: 5★, 4★, 3★, 2★, 1★ (with counts)

---

## 🔄 So sánh FE Display vs BE Export (các tabs hiện có)

### 1. Overview Tab

#### FE Display (`OverviewTab.tsx`)
| Trường | Hiển thị | Có trong Export |
|--------|----------|-----------------|
| **Summary Stats** | | |
| Total Revenue | ✅ Card | ✅ |
| Total Expenses | ✅ Card | ✅ |
| Net Profit | ✅ Card | ✅ |
| Total Invoices | ✅ Card | ✅ |
| Total Appointments | ✅ (in data) | ✅ |
| Total Patients | ✅ (in data) | ✅ |
| **Alerts** | | |
| Alert Type | ✅ Badge | ❌ THIẾU |
| Alert Severity | ✅ Color coded | ❌ THIẾU |
| Alert Message | ✅ | ❌ THIẾU |
| **Invoice Stats** | | |
| Paid/Pending/Overdue | ✅ Pie Chart | ✅ |
| Total Amount | ✅ | ✅ |
| Paid Amount | ✅ | ✅ |
| **Appointment Stats** | | |
| Scheduled | ✅ | ✅ |
| Completed | ✅ | ✅ |
| Cancelled | ✅ | ✅ |
| **Charts** | | |
| Revenue vs Expenses (Bar) | ✅ | ❌ (chỉ có số) |
| Invoice Status (Pie) | ✅ | ❌ (chỉ có số) |
| Appointment Status (Pie) | ✅ | ❌ (chỉ có số) |

**⚠️ Cần bổ sung vào Overview Export:**
- **Alerts Section**: Cần thêm section cho alerts nếu có
  ```
  Alerts Section
  - Severity | Type | Message
  ```

---

### 2. Revenue & Expenses Tab

#### FE Display (`RevenueExpensesTab.tsx`)
| Trường | Hiển thị | Có trong Export |
|--------|----------|-----------------|
| **Revenue** | | |
| Total Revenue | ✅ | ✅ |
| By Type breakdown | ✅ | ✅ |
| Top Services | ✅ Table + Chart | ✅ |
| **Expenses** | | |
| Total Expenses | ✅ | ✅ |
| By Type breakdown | ✅ | ✅ |
| Top Items | ✅ Table + Chart | ✅ |
| **Profit Margin** | | |
| Net Profit | ✅ Card | ❌ THIẾU |
| Profit Margin % | ✅ Card | ❌ THIẾU |

**⚠️ Cần bổ sung vào Revenue-Expenses Export:**
```java
// Thêm vào exportRevenueExpenses()
rowNum = addDataRow(sheet, rowNum, "Net Profit", 
    data.getRevenue().getTotal().subtract(data.getExpenses().getTotal()), currencyStyle);

BigDecimal profitMargin = data.getRevenue().getTotal().compareTo(BigDecimal.ZERO) > 0
    ? data.getRevenue().getTotal().subtract(data.getExpenses().getTotal())
        .divide(data.getRevenue().getTotal(), 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
    : BigDecimal.ZERO;
rowNum = addDataRow(sheet, rowNum, "Profit Margin %", profitMargin.doubleValue(), percentStyle);
```

---

### 3. Employees Tab

#### FE Display (`EmployeesTab.tsx`)
| Trường | Hiển thị | Có trong Export |
|--------|----------|-----------------|
| **Top Doctors** | | |
| Rank | ✅ (STT) | ❌ THIẾU |
| Employee Code | ✅ | ✅ |
| Full Name | ✅ | ✅ |
| Appointment Count | ✅ | ✅ |
| Total Revenue | ✅ | ✅ |
| Avg Revenue/Apt | ✅ | ✅ |
| Service Count | ✅ | ✅ (labeled as "Patients" - cần sửa label) |
| **Time-Off Stats** | | |
| By Type | ✅ | ✅ |
| By Status | ✅ | ✅ |

**⚠️ Cần bổ sung vào Employees Export:**
```java
// Thêm cột Rank vào Top Doctors table
Row headerRow = sheet.createRow(rowNum++);
headerRow.createCell(0).setCellValue("Rank");        // ✅ THÊM MỚI
headerRow.createCell(1).setCellValue("Employee Code");
headerRow.createCell(2).setCellValue("Doctor Name");
headerRow.createCell(3).setCellValue("Revenue");
headerRow.createCell(4).setCellValue("Appointments");
headerRow.createCell(5).setCellValue("Services");    // ✅ SỬA LABEL (was "Patients")
headerRow.createCell(6).setCellValue("Avg Revenue/Apt"); // ✅ THÊM MỚI

int rank = 1;
for (EmployeeStatisticsResponse.DoctorPerformance doctor : data.getTopDoctors()) {
    Row row = sheet.createRow(rowNum++);
    row.createCell(0).setCellValue(rank++);           // ✅ THÊM MỚI
    row.createCell(1).setCellValue(doctor.getEmployeeCode());
    row.createCell(2).setCellValue(doctor.getFullName());
    // ... rest of fields
    Cell avgCell = row.createCell(6);
    avgCell.setCellValue(doctor.getAverageRevenuePerAppointment().doubleValue());
    avgCell.setCellStyle(currencyStyle);
}
```

---

### 4. Warehouse Tab

#### FE Display (`WarehouseTab.tsx`)
| Trường | Hiển thị | Có trong Export |
|--------|----------|-----------------|
| **Summary Cards** | | |
| Total Transactions | ✅ | ✅ |
| Import Value | ✅ | ✅ |
| Export Value | ✅ | ✅ |
| Current Inventory Value | ✅ | ✅ |
| **Transaction Stats** | | |
| Import Count/Value | ✅ | ✅ |
| Export Count/Value | ✅ | ✅ |
| By Status | ✅ Pie Chart | ✅ |
| **Inventory Alerts** | | |
| Low Stock Items | ✅ Badge | ✅ |
| Expiring Items (30 days) | ✅ Badge | ✅ |
| Usage Rate % | ✅ (in data) | ✅ |
| **Top Items** | | |
| Top Imports | ✅ Table | ✅ |
| Top Exports | ✅ Table | ✅ |

**✅ Warehouse Export đã đầy đủ!**

---

### 5. Transactions Tab

#### FE Display (`TransactionsTab.tsx`)
| Trường | Hiển thị | Có trong Export |
|--------|----------|-----------------|
| **Invoice Summary** | | |
| Total Invoices | ✅ | ✅ |
| Total Value | ✅ | ✅ |
| Payment Rate % | ✅ | ✅ |
| Total Debt | ✅ | ✅ |
| **Invoice By Status** | | |
| Pending/Partial/Paid/Cancelled | ✅ Count + Value | ✅ |
| **Invoice By Type** | | |
| Appointment/Treatment/Supplemental | ✅ Count + Value | ✅ |
| **Payment Stats** | | |
| Total Payments | ✅ | ✅ |
| Total Value | ✅ | ✅ |
| By Method | ✅ Count + Value | ✅ |
| **Charts** | | |
| Invoice Status Pie | ✅ | ❌ (chỉ có số) |
| Payment Method Pie | ✅ | ❌ (chỉ có số) |
| Daily Trend Line | ✅ | ❌ THIẾU |

**⚠️ Transactions Export đã khá đầy đủ, nhưng thiếu:**
- Daily trend data (nếu có trong response từ BE)

---

## 📝 Tổng kết các thay đổi cần thực hiện

### ✅ ĐÃ HOÀN THÀNH (Completed - January 14, 2026)

1. **✅ Thêm Feedbacks tab vào export** (hoàn toàn mới)
   - ✅ Tạo endpoint `/api/v1/dashboard/feedbacks`
   - ✅ Thêm `exportFeedbacks()` method
   - ✅ Thêm `exportFeedbacksCSV()` method
   - ✅ Cập nhật `exportAllTabs()` để include feedbacks
   - ✅ Cập nhật `exportToCSV()` switch case
   - ✅ Thêm `AppointmentFeedbackService` dependency vào DashboardExportService và DashboardService

**Files đã sửa:**
- `DashboardController.java` - Thêm endpoint `/feedbacks` và update export description
- `DashboardService.java` - Thêm `AppointmentFeedbackService` dependency
- `DashboardExportService.java` - Thêm `exportFeedbacks()`, `exportFeedbacksCSV()`, update switches và exportAllTabs

### 🔧 Nên có (Recommended) - CHƯA THỰC HIỆN

2. **Overview Tab - Thêm Alerts section**
   - Xuất danh sách alerts nếu có trong response

3. **Revenue & Expenses Tab - Thêm Profit metrics**
   - Net Profit
   - Profit Margin %

4. **Employees Tab - Thêm Rank column**
   - Thêm cột STT (Rank) vào Top Doctors table
   - Sửa label "Patients" → "Services"
   - Thêm cột "Avg Revenue/Apt"

### 📊 Tùy chọn (Optional)

5. **Tất cả tabs - Chart data**
   - Hiện tại chỉ export số liệu
   - Nếu cần, có thể thêm sheet riêng cho chart data (để import vào Excel charts)

---

## 🧪 Testing Checklist

### Export Functionality Tests

- [ ] Export Overview tab → Check có đầy đủ summary stats
- [ ] Export Revenue-Expenses tab → Check có profit metrics
- [ ] Export Employees tab → Check có rank column
- [ ] Export Warehouse tab → Check data integrity
- [ ] Export Transactions tab → Check đầy đủ invoice/payment stats
- [x] **Export Feedbacks tab** → ✅ IMPLEMENTED - Check có đầy đủ doctor ratings
- [x] Export "all" tabs → ✅ IMPLEMENTED - Check có cả 6 sheets (bao gồm Feedbacks)
- [x] Export CSV format → ✅ IMPLEMENTED - Check format cho tất cả 6 tabs
- [ ] Download file Excel → Check có mở được và format đẹp
- [ ] Check date range filtering → Đảm bảo startDate/endDate hoạt động
- [ ] Check month filtering → Đảm bảo month parameter hoạt động
- [ ] Check permissions → Chỉ ADMIN/MANAGER được export

### Data Accuracy Tests

- [ ] So sánh số liệu Overview FE vs Excel export
- [ ] So sánh Revenue-Expenses FE vs Excel export
- [ ] So sánh Employees Top 10 FE vs Excel export
- [ ] So sánh Warehouse stats FE vs Excel export
- [ ] So sánh Transactions FE vs Excel export
- [x] **So sánh Feedbacks FE vs Excel export** → ✅ READY FOR TESTING
- [ ] Verify currency formatting (VND)
- [ ] Verify percent formatting (%)
- [ ] Verify số liệu rounded chính xác

---

## 📞 Liên hệ

Nếu có thắc mắc về requirements này, vui lòng liên hệ FE team.

**Tài liệu tham khảo:**
- FE Source: `src/components/dashboard/*Tab.tsx`
- BE Source: `docs/files/dashboard/`
- API docs: Swagger UI

---

**Ngày tạo:** 2024
**Người tạo:** FE Team
**Ngày hoàn thành:** January 14, 2026
**Trạng thái:** ✅ ĐÃ HOÀN THÀNH - Feedbacks tab đã được thêm vào export
