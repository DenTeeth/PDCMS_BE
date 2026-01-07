# BE Response to Dashboard UI Issues

**Date:** 2026-01-07  
**Related Document:** `DASHBOARD_UI_ISSUES_ANALYSIS.md` (from FE Team)  
**Status:** ✅ BE Changes Implemented

---

## 📝 Summary

This document addresses the 4 dashboard UI/UX issues reported by the FE team. Out of 4 issues:
- **1 issue requires BE changes** → ✅ **COMPLETED**
- **3 issues are FE-only** → No BE changes needed

---

## 🔧 Issue #1: Overview Tab - Appointment Status (✅ FIXED)

### Problem
FE currently displays only 3 appointment statuses:
- `COMPLETED` (Hoàn thành)
- `CANCELLED` (Đã hủy)
- `NO_SHOW` (Không đến)

User requested all 7 statuses to be displayed.

### Solution Implemented

#### 1. Updated `DashboardOverviewResponse.AppointmentStats` DTO

**File:** `src/main/java/com/dental/clinic/management/dashboard/dto/DashboardOverviewResponse.java`

**Before:**
```java
public static class AppointmentStats {
    private Long total;
    private Long completed;
    private Long cancelled;
    private Long noShow;
    private Double completionRate;
}
```

**After:**
```java
public static class AppointmentStats {
    private Long total;
    private Long scheduled;        // SCHEDULED - Đã đặt lịch
    private Long checkedIn;        // CHECKED_IN - Đã check-in
    private Long inProgress;       // IN_PROGRESS - Đang điều trị
    private Long completed;        // COMPLETED - Hoàn thành
    private Long cancelled;        // CANCELLED - Đã hủy (>24h)
    private Long cancelledLate;    // CANCELLED_LATE - Hủy muộn (≤24h)
    private Long noShow;           // NO_SHOW - Không đến
    private Double completionRate;
}
```

#### 2. Updated `buildAppointmentStats()` Method

**File:** `src/main/java/com/dental/clinic/management/dashboard/service/DashboardService.java`

Now counts all 7 appointment statuses:
```java
private DashboardOverviewResponse.AppointmentStats buildAppointmentStats(LocalDateTime startDate, LocalDateTime endDate) {
    Long total = appointmentRepository.countAppointmentsInRange(startDate, endDate);
    
    // Count all 7 appointment statuses
    Long scheduled = appointmentRepository.countByStatusInRange(startDate, endDate, AppointmentStatus.SCHEDULED);
    Long checkedIn = appointmentRepository.countByStatusInRange(startDate, endDate, AppointmentStatus.CHECKED_IN);
    Long inProgress = appointmentRepository.countByStatusInRange(startDate, endDate, AppointmentStatus.IN_PROGRESS);
    Long completed = appointmentRepository.countByStatusInRange(startDate, endDate, AppointmentStatus.COMPLETED);
    Long cancelled = appointmentRepository.countByStatusInRange(startDate, endDate, AppointmentStatus.CANCELLED);
    Long cancelledLate = appointmentRepository.countByStatusInRange(startDate, endDate, AppointmentStatus.CANCELLED_LATE);
    Long noShow = appointmentRepository.countByStatusInRange(startDate, endDate, AppointmentStatus.NO_SHOW);
    
    Double completionRate = total == 0 ? 0.0 : (completed.doubleValue() / total.doubleValue()) * 100;
    
    return DashboardOverviewResponse.AppointmentStats.builder()
            .total(total)
            .scheduled(scheduled)
            .checkedIn(checkedIn)
            .inProgress(inProgress)
            .completed(completed)
            .cancelled(cancelled)
            .cancelledLate(cancelledLate)
            .noShow(noShow)
            .completionRate(completionRate)
            .build();
}
```

### New API Response Structure

```json
{
  "month": "2026-01",
  "appointments": {
    "total": 150,
    "scheduled": 45,
    "checkedIn": 12,
    "inProgress": 5,
    "completed": 75,
    "cancelled": 8,
    "cancelledLate": 3,
    "noShow": 2,
    "completionRate": 50.0
  }
}
```

### Status Definitions

| Status | BE Enum | Vietnamese | Description |
|--------|---------|-----------|-------------|
| `scheduled` | `SCHEDULED` | Đã đặt lịch | Initial status when appointment is created |
| `checkedIn` | `CHECKED_IN` | Đã check-in | Patient checked in at reception |
| `inProgress` | `IN_PROGRESS` | Đang điều trị | Doctor started treatment |
| `completed` | `COMPLETED` | Hoàn thành | Treatment completed successfully |
| `cancelled` | `CANCELLED` | Đã hủy | Cancelled >24h before appointment |
| `cancelledLate` | `CANCELLED_LATE` | Hủy muộn | Cancelled ≤24h before appointment |
| `noShow` | `NO_SHOW` | Không đến | Patient didn't show up |

### FE Action Required

**File to update:** `src/components/dashboard/OverviewTab.tsx`

Update the appointment status chart to display all 7 statuses:

```typescript
const appointmentStatusData = [
  { name: 'Đã đặt lịch', value: data.appointments.scheduled || 0, color: '#3b82f6' },
  { name: 'Đã check-in', value: data.appointments.checkedIn || 0, color: '#8b5cf6' },
  { name: 'Đang điều trị', value: data.appointments.inProgress || 0, color: '#f59e0b' },
  { name: 'Hoàn thành', value: data.appointments.completed || 0, color: '#10b981' },
  { name: 'Đã hủy', value: data.appointments.cancelled || 0, color: '#ef4444' },
  { name: 'Hủy muộn', value: data.appointments.cancelledLate || 0, color: '#dc2626' },
  { name: 'Không đến', value: data.appointments.noShow || 0, color: '#6b7280' },
].filter((item) => item.value > 0);
```

---

## 📊 Issue #2: Revenue Tab - Expense Types (FE ONLY)

### Problem
Users don't understand what "Chi Phí Theo Loại" (Expense by Type) means.

### BE Context

The expense data comes from `StorageTransaction` (warehouse export transactions).

**BE DTO Structure:**
```java
public static class ExpenseByType {
    private BigDecimal serviceConsumption;  // Tiêu hao dịch vụ
    private BigDecimal damaged;             // Hỏng
    private BigDecimal expired;              // Hết hạn
    private BigDecimal other;               // Khác
}
```

### Expense Type Definitions

| Field | Vietnamese | Description | Source |
|-------|-----------|-------------|--------|
| `serviceConsumption` | Tiêu hao dịch vụ | Materials used in dental services | `exportType = 'SERVICE'` |
| `damaged` | Hỏng | Damaged materials that can't be used | `exportType = 'WASTAGE'` (damaged) |
| `expired` | Hết hạn | Expired materials that must be discarded | `exportType = 'WASTAGE'` (expired) |
| `other` | Khác | Other export types | `exportType = 'SALE'`, `'TRANSFER'`, etc. |

### FE Solution Recommendations

**Option 1: Add Tooltips**
```tsx
<div className="flex items-center gap-2">
  <h3>Chi Phí Theo Loại</h3>
  <Tooltip content="Phân loại chi phí xuất kho theo mục đích sử dụng">
    <InfoCircleIcon className="w-5 h-5 text-gray-400" />
  </Tooltip>
</div>
```

**Option 2: Improve Labels**
```typescript
const expenseByTypeData = [
  { name: 'Tiêu hao dịch vụ (Vật tư điều trị)', value: data.expenses.byType.serviceConsumption },
  { name: 'Hỏng (Vật tư bị hỏng)', value: data.expenses.byType.damaged },
  { name: 'Hết hạn (Vật tư hết hạn)', value: data.expenses.byType.expired },
  { name: 'Khác', value: data.expenses.byType.other }
];
```

**No BE changes needed** - This is purely a UI/UX improvement.

---

## 👥 Issue #3: Employee Tab - Time Off Types (FE ONLY)

### Problem
Displaying all 5 leave types makes the chart too complex and hard to read.

### BE Context

**BE DTO Structure:**
```java
public static class TimeOffByType {
    private TypeStats paidLeave;      // Có phép
    private TypeStats unpaidLeave;    // Không phép
    private TypeStats emergencyLeave; // Khẩn cấp
    private TypeStats sickLeave;      // Nghỉ ốm
    private TypeStats other;          // Khác
}
```

### FE Solution Recommendations

**Option 1 (Recommended): Show 3 Main Types + "Other"**

```typescript
const timeOffByTypeData = [
  { name: 'Có phép', value: data.timeOff.byType.paidLeave?.days || 0 },
  { name: 'Nghỉ ốm', value: data.timeOff.byType.sickLeave?.days || 0 },
  { name: 'Không phép', value: data.timeOff.byType.unpaidLeave?.days || 0 },
  {
    name: 'Khác',
    value: (data.timeOff.byType.emergencyLeave?.days || 0) + 
           (data.timeOff.byType.other?.days || 0)
  }
].filter((item) => item.value > 0);
```

**Option 2: Show 2 Main Types + "Other"**

```typescript
const timeOffByTypeData = [
  { name: 'Có phép', value: data.timeOff.byType.paidLeave?.days || 0 },
  { name: 'Nghỉ ốm', value: data.timeOff.byType.sickLeave?.days || 0 },
  {
    name: 'Khác',
    value: (data.timeOff.byType.unpaidLeave?.days || 0) + 
           (data.timeOff.byType.emergencyLeave?.days || 0) + 
           (data.timeOff.byType.other?.days || 0)
  }
].filter((item) => item.value > 0);
```

**No BE changes needed** - FE can aggregate the data client-side.

---

## 📦 Issue #4: Warehouse Tab - Transaction Status (FE ONLY)

### Problem
Users unclear about what "Giao Dịch Theo Trạng Thái" (Transaction by Status) means.

### BE Context

This tracks **warehouse transaction statuses** (not invoice or payment statuses).

**BE DTO Structure:**
```java
public static class TransactionByStatus {
    private Long pending;    // PENDING_APPROVAL - Chờ duyệt
    private Long approved;   // APPROVED - Đã duyệt
    private Long rejected;   // REJECTED - Từ chối
    private Long cancelled;  // CANCELLED - Đã hủy
}
```

### Transaction Status Definitions

| Status | BE Enum | Vietnamese | Description |
|--------|---------|-----------|-------------|
| `pending` | `PENDING_APPROVAL` | Chờ duyệt | Transaction created but not approved |
| `approved` | `APPROVED` | Đã duyệt | Transaction approved and executed |
| `rejected` | `REJECTED` | Từ chối | Transaction rejected by manager |
| `cancelled` | `CANCELLED` | Đã hủy | Transaction cancelled |

### FE Solution Recommendations

**Option 1: Improve Title**
```tsx
// Change from:
<h3>Giao Dịch Theo Trạng Thái</h3>

// To:
<h3>Giao Dịch Kho Theo Trạng Thái</h3>
```

**Option 2: Add Tooltip**
```tsx
<div className="flex items-center gap-2">
  <h3>Giao Dịch Kho Theo Trạng Thái</h3>
  <Tooltip content="Thống kê trạng thái của các giao dịch xuất/nhập kho trong tháng">
    <InfoCircleIcon className="w-5 h-5 text-gray-400" />
  </Tooltip>
</div>
```

**No BE changes needed** - This is purely a labeling improvement.

---

## 🧪 Testing

### Test the Appointment Stats API

```bash
GET /api/dashboard/overview?month=2026-01
```

**Expected Response:**
```json
{
  "month": "2026-01",
  "summary": {
    "totalRevenue": 50000000.00,
    "totalExpenses": 10000000.00,
    "netProfit": 40000000.00,
    "totalInvoices": 120,
    "totalAppointments": 150,
    "totalPatients": 80,
    "totalEmployees": 15
  },
  "appointments": {
    "total": 150,
    "scheduled": 45,
    "checkedIn": 12,
    "inProgress": 5,
    "completed": 75,
    "cancelled": 8,
    "cancelledLate": 3,
    "noShow": 2,
    "completionRate": 50.0
  },
  "invoices": { ... },
  "revenue": { ... },
  "expenses": { ... }
}
```

### Verify All 7 Statuses

```sql
-- Check appointment status distribution for January 2026
SELECT 
    status,
    COUNT(*) as count
FROM appointment
WHERE appointment_date_time >= '2026-01-01' 
  AND appointment_date_time < '2026-02-01'
GROUP BY status
ORDER BY status;
```

---

## 📋 Summary for FE Team

### BE Changes Completed ✅
- ✅ **Issue #1:** AppointmentStats DTO updated to include all 7 statuses
- ✅ Service layer updated to count all statuses
- ✅ API now returns complete appointment status breakdown

### FE Action Items
1. **Issue #1 - Appointment Status** (REQUIRED)
   - Update `OverviewTab.tsx` to display all 7 appointment statuses
   - Update TypeScript types to match new BE DTO
   
2. **Issue #2 - Expense Types** (OPTIONAL - UX improvement)
   - Add tooltips to explain expense types
   - Improve labels for better clarity
   
3. **Issue #3 - Time Off Types** (OPTIONAL - UX improvement)
   - Simplify chart by grouping less common types into "Other"
   
4. **Issue #4 - Transaction Status** (OPTIONAL - UX improvement)
   - Update title to "Giao Dịch Kho Theo Trạng Thái"
   - Add tooltip to explain data source

### Files FE Needs to Update

1. `src/types/dashboard.ts` - Update `AppointmentStats` interface
2. `src/components/dashboard/OverviewTab.tsx` - Display all 7 statuses
3. `src/components/dashboard/RevenueExpensesTab.tsx` - (Optional) Add tooltips
4. `src/components/dashboard/EmployeesTab.tsx` - (Optional) Simplify chart
5. `src/components/dashboard/WarehouseTab.tsx` - (Optional) Update title

---

## 🔄 Deployment Notes

- BE changes are **backward compatible** (only adding new fields, not removing)
- Existing FE will continue to work (will just ignore new fields)
- FE should update to take advantage of new data
- No database migration needed (data already exists)

---

## 📞 Contact

If FE team has any questions or needs clarification:
- Check this document first
- Refer to `AppointmentStatus` enum for status definitions
- Test API endpoint: `GET /api/dashboard/overview?month=2026-01`

**Status:** ✅ Ready for FE integration
