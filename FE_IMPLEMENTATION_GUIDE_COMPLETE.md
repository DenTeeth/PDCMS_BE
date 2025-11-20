# Frontend Implementation Guide - Complete Reference

**Version**: V21.4 & V21.5 + Phase 5
**Last Updated**: 2025-11-19
**Status**: ✅ Backend Complete - Ready for Frontend Implementation

---

## 📚 Table of Contents

1. [Quick Start - Phase 5 Appointment Booking](#phase-5-quick-start)
2. [Phase 5: Treatment Plan Appointment Integration](#phase-5-details)
3. [V21.4 & V21.5 API Changes](#v21-api-changes)
4. [Testing Guide](#testing-guide)
5. [Complete API Reference](#api-reference)

---

# Phase 5 Quick Start

## 🎯 What's New

Backend đã implement Phase 5 cho appointment booking từ treatment plan items:

### ✅ 1. Added `serviceCode` to API Response
```json
{
  "itemId": 123,
  "serviceCode": "ENDO_TREAT_ANT", // ✅ NEW - Không cần fetch thêm
  "itemName": "Điều trị tủy răng cửa",
  "status": "READY_FOR_BOOKING"
}
```

### ✅ 2. Support Booking with `patientPlanItemIds`
```json
POST /api/v1/appointments
{
  "patientCode": "BN-1002",
  "patientPlanItemIds": [123, 124], // ✅ NEW
  "appointmentDate": "2025-11-25",
  "appointmentStartTime": "09:00",
  ...
}
```

### ✅ 3. Automatic Status Management
- Items auto-update: `READY_FOR_BOOKING` → `SCHEDULED`
- Plan auto-activate: `PENDING` → `IN_PROGRESS` (first appointment)

---

## 📋 FE Quick Checklist

1. **Update TypeScript types**:
```typescript
interface TreatmentPlanItem {
  serviceCode: string; // ✅ ADD THIS
  status: 'READY_FOR_BOOKING' | 'SCHEDULED' | ...;
}

interface CreateAppointmentRequest {
  patientPlanItemIds?: number[]; // ✅ ADD THIS
}
```

2. **Add item selection UI** - Checkboxes for `READY_FOR_BOOKING` items
3. **Create booking dialog** - Pre-fill from selected items
4. **Call API** with `patientPlanItemIds`
5. **Refresh** after booking to show updated statuses

---

## 🧪 Test Now

```bash
# Get plan (has serviceCode)
GET /api/v1/patients/BN-1002/treatment-plans/PLAN-20240515-001

# Book appointment
POST /api/v1/appointments
{
  "patientCode": "BN-1002",
  "roomId": 1,
  "doctorId": 5,
  "appointmentDate": "2025-11-25",
  "appointmentStartTime": "09:00",
  "appointmentEndTime": "10:30",
  "patientPlanItemIds": [123]
}
```

---

# Phase 5 Details

## 📖 Complete Implementation Guide

### Step 1: Update TypeScript Interfaces

**File**: `types/treatment-plan.ts`

```typescript
export interface TreatmentPlanItem {
  itemId: number;
  serviceId: number;
  serviceCode: string; // ✅ NEW FIELD
  itemName: string;
  price: number;
  status: 'NOT_STARTED' | 'READY_FOR_BOOKING' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED';
  estimatedTimeMinutes?: number;
  completedAt?: string;
  sequenceNumber: number;
  
  // Appointment info (if linked)
  appointments?: Array<{
    appointmentCode: string;
    appointmentStartTime: string;
    status: string;
  }>;
}

export interface CreateAppointmentFromPlanRequest {
  patientCode: string;
  roomId: number;
  doctorId: number;
  appointmentDate: string;
  appointmentStartTime: string;
  appointmentEndTime: string;
  notes?: string;
  patientPlanItemIds: number[]; // ✅ NEW - for Phase 5
}
```

---

### Step 2: Update API Service

**File**: `services/appointment.service.ts`

```typescript
export class AppointmentService {
  /**
   * ✅ NEW: Book appointment from treatment plan items
   */
  static async createAppointmentFromPlan(
    request: CreateAppointmentFromPlanRequest
  ): Promise<AppointmentResponse> {
    const response = await axios.post('/api/v1/appointments', request, {
      headers: { Authorization: `Bearer ${getToken()}` },
    });
    return response.data;
  }
}
```

---

### Step 3: Create Booking Component

**File**: `components/BookAppointmentFromPlanDialog.tsx`

```typescript
import React, { useState, useEffect } from 'react';

export const BookAppointmentFromPlanDialog: React.FC<Props> = ({
  selectedItems,
  patientCode,
  onSuccess,
}) => {
  const [formData, setFormData] = useState({
    patientCode,
    roomId: null,
    doctorId: null,
    appointmentDate: '',
    appointmentStartTime: '',
    appointmentEndTime: '',
    notes: `Khám theo kế hoạch: ${selectedItems.map(i => i.itemName).join(', ')}`,
    patientPlanItemIds: selectedItems.map(item => item.itemId), // ✅
  });

  // Auto-calculate duration
  const totalDuration = selectedItems.reduce(
    (sum, item) => sum + (item.estimatedTimeMinutes || 0),
    0
  );

  // Auto-calculate end time
  useEffect(() => {
    if (formData.appointmentStartTime) {
      const [hours, minutes] = formData.appointmentStartTime.split(':').map(Number);
      const endTime = new Date();
      endTime.setHours(hours);
      endTime.setMinutes(minutes + totalDuration);
      
      const endTimeStr = `${String(endTime.getHours()).padStart(2, '0')}:${String(endTime.getMinutes()).padStart(2, '0')}`;
      setFormData(prev => ({ ...prev, appointmentEndTime: endTimeStr }));
    }
  }, [formData.appointmentStartTime, totalDuration]);

  const handleSubmit = async () => {
    try {
      await AppointmentService.createAppointmentFromPlan(formData);
      toast.success('Đặt lịch thành công!');
      onSuccess(); // Refresh plan
    } catch (error) {
      toast.error(error.response?.data?.message || 'Có lỗi xảy ra');
    }
  };

  return (
    <Dialog>
      <DialogTitle>Đặt Lịch Từ Kế Hoạch Điều Trị</DialogTitle>
      
      <DialogContent>
        {/* Selected Items */}
        <div className="mb-4">
          <h3>Dịch vụ đã chọn:</h3>
          {selectedItems.map(item => (
            <div key={item.itemId}>
              {item.itemName} - {item.serviceCode}
              ({item.estimatedTimeMinutes || 0} phút)
            </div>
          ))}
          <div className="text-blue-600">
            Tổng: {totalDuration} phút
          </div>
        </div>

        {/* Form fields: date, time, room, doctor */}
        {/* ... */}
      </DialogContent>

      <DialogActions>
        <Button onClick={handleSubmit}>Đặt Lịch</Button>
      </DialogActions>
    </Dialog>
  );
};
```

---

### Step 4: Update Treatment Plan View

**File**: `pages/TreatmentPlanDetail.tsx`

```typescript
export const TreatmentPlanDetail: React.FC = () => {
  const [selectedItems, setSelectedItems] = useState<TreatmentPlanItem[]>([]);

  const handleItemSelect = (item: TreatmentPlanItem) => {
    setSelectedItems(prev => {
      const exists = prev.find(i => i.itemId === item.itemId);
      return exists 
        ? prev.filter(i => i.itemId !== item.itemId)
        : [...prev, item];
    });
  };

  return (
    <div>
      {/* Book Button */}
      <Button
        onClick={() => setShowBookingDialog(true)}
        disabled={selectedItems.length === 0}
      >
        Đặt Lịch ({selectedItems.length} dịch vụ)
      </Button>

      {/* Items with checkboxes */}
      {planData.phases.map(phase => (
        <div key={phase.phaseId}>
          {phase.items.map(item => (
            <div key={item.itemId}>
              {item.status === 'READY_FOR_BOOKING' && (
                <Checkbox
                  checked={selectedItems.some(i => i.itemId === item.itemId)}
                  onChange={() => handleItemSelect(item)}
                />
              )}
              
              <div>
                {item.itemName}
                <div>{item.serviceCode} - {formatCurrency(item.price)}</div>
                <StatusBadge status={item.status} />
              </div>
            </div>
          ))}
        </div>
      ))}

      <BookAppointmentFromPlanDialog
        selectedItems={selectedItems}
        patientCode={patientCode}
        onSuccess={() => {
          setSelectedItems([]);
          refetchPlanData(); // Refresh to show SCHEDULED status
        }}
      />
    </div>
  );
};
```

---

## ⚠️ Validation Rules

Backend validates automatically:
- ❌ Cannot use both `patientPlanItemIds` AND `serviceCodes`
- ❌ Items must have status `READY_FOR_BOOKING`
- ❌ Items must belong to patient in request
- ✅ Auto-updates status to `SCHEDULED`

---

## 🎯 Status Flow

```
Treatment Plan:
PENDING → IN_PROGRESS (first appointment) → COMPLETED

Item Status:
NOT_STARTED → READY_FOR_BOOKING → SCHEDULED → IN_PROGRESS → COMPLETED
                                      ↑
                                   Book here!
```

---

# V21 API Changes

## API 5.12: Submit Plan for Review

**Endpoint**: `PATCH /api/v1/patient-treatment-plans/{planCode}/submit-for-review`

**Request**:
```json
{
  "notes": "Kế hoạch điều trị đã hoàn tất, gửi phê duyệt"
}
```

**Response**:
```json
{
  "planCode": "PLAN-20251001-001",
  "oldStatus": "DRAFT",
  "newStatus": "PENDING_APPROVAL",
  "submittedAt": "2025-11-19T10:30:00",
  "submittedBy": "Dr. Nguyen Van A"
}
```

**Status Flow**: `DRAFT` → `PENDING_APPROVAL`

---

## API 5.13: Update Prices (Finance)

**Endpoint**: `PATCH /api/v1/patient-treatment-plans/{planCode}/prices`

**Permission**: `MANAGE_PLAN_PRICING` (Accountant, Manager)

**Request**:
```json
{
  "items": [
    {
      "itemId": 1,
      "newPrice": 5000000.00,
      "note": "Điều chỉnh giá theo chính sách mới"
    }
  ],
  "discountAmount": 500000.00,
  "discountNote": "Giảm giá khách hàng thân thiết"
}
```

**Response**:
```json
{
  "planCode": "PLAN-20251001-001",
  "totalCostBefore": 35000000.00,
  "totalCostAfter": 28300000.00,
  "finalCost": 27800000.00,
  "itemsUpdated": 1,
  "discountUpdated": true,
  "updatedBy": "Accountant Name"
}
```

**Features**:
- Update individual item prices
- Update plan discount
- Recalculate totalPrice & finalCost
- Audit trail (who, when, why)

---

## API 5.14: Reorder Items (Drag & Drop)

**Endpoint**: `PATCH /api/v1/patient-plan-phases/{phaseId}/items/reorder`

**Request**:
```json
{
  "itemIds": [3, 1, 2] // New order
}
```

**Response**:
```json
{
  "phaseId": 1,
  "phaseName": "Giai đoạn 1",
  "itemsReordered": 3,
  "items": [
    { "itemId": 3, "sequenceNumber": 1 },
    { "itemId": 1, "sequenceNumber": 2 },
    { "itemId": 2, "sequenceNumber": 3 }
  ]
}
```

**Features**:
- Drag & drop support
- Atomic update (all or nothing)
- Validates all items exist in phase
- SERIALIZABLE isolation (concurrent-safe)

---

## V21.4: Optional Price Field

**Affected APIs**:
- `POST /api/v1/patients/{patientCode}/custom-plans` (API 5.1)
- `POST /api/v1/patient-plan-phases/{phaseId}/items` (API 5.7)

**Before**:
```json
{
  "serviceId": 45,
  "price": 5000000.00 // ❌ Required
}
```

**After**:
```json
{
  "serviceId": 45,
  "price": 5000000.00 // ✅ Optional - auto-fill from service default
}
```

**Benefits**:
- FE không cần pre-fill price
- Backend auto-fetch từ service table
- Reduce API calls

---

## V21.4: Auto Submit Parameter

**Endpoint**: `POST /api/v1/patient-plan-phases/{phaseId}/items` (API 5.7)

**New Parameter**: `?autoSubmit=true`

**Behavior**:
```
autoSubmit=false (default) → Add item → status DRAFT
autoSubmit=true → Add item → auto submit for review → status PENDING_APPROVAL
```

**Example**:
```bash
POST /api/v1/patient-plan-phases/1/items?autoSubmit=true
{
  "serviceId": 45,
  "notes": "Cần điều trị gấp"
}
```

**Response**:
```json
{
  "itemAdded": { "itemId": 123, "status": "NOT_STARTED" },
  "planStatus": {
    "oldStatus": "DRAFT",
    "newStatus": "PENDING_APPROVAL", // ✅ Auto-submitted
    "autoSubmitted": true
  }
}
```

---

# Testing Guide

## Test Data

**Available Patients**:
```
BN-1002 - Phạm Văn Phong
BN-1003 - Nguyễn Tuấn Anh
```

**Available Plans**:
```
PLAN-20240515-001 (BN-1002)
PLAN-20251105-001 (BN-1003)
PLAN-20250110-001 (BN-1003)
```

**Service Codes in Plans**:
```
ENDO_POST_CORE - Post core
ENDO_TREAT_ANT - Điều trị tủy răng cửa
IMPL_IMPRESSION - Lấy dấu implant
CROWN_ZIR_KATANA - Crown zirconia Katana
```

---

## Test Scenarios

### Scenario 1: Get Plan with serviceCode

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans/PLAN-20240515-001" \
  -H "Authorization: Bearer <token>"
```

**Verify**: Each item has `serviceCode` field ✅

---

### Scenario 2: Book Appointment from Plan

```bash
curl -X POST "http://localhost:8080/api/v1/appointments" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "patientCode": "BN-1002",
    "roomId": 1,
    "doctorId": 5,
    "appointmentDate": "2025-11-25",
    "appointmentStartTime": "09:00",
    "appointmentEndTime": "10:30",
    "patientPlanItemIds": [123]
  }'
```

**Verify**: 
- Appointment created ✅
- Item status → `SCHEDULED` ✅
- Plan status → `IN_PROGRESS` (if first) ✅

---

### Scenario 3: Update Prices (Finance)

```bash
curl -X PATCH "http://localhost:8080/api/v1/patient-treatment-plans/PLAN-20251001-001/prices" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "itemId": 1,
        "newPrice": 5000000.00,
        "note": "Điều chỉnh giá"
      }
    ]
  }'
```

**Verify**: 
- Price updated ✅
- totalCost recalculated ✅

---

### Scenario 4: Reorder Items

```bash
curl -X PATCH "http://localhost:8080/api/v1/patient-plan-phases/1/items/reorder" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "itemIds": [3, 1, 2]
  }'
```

**Verify**: 
- Items reordered ✅
- sequenceNumber updated ✅

---

### Scenario 5: Add Item with Auto Submit

```bash
curl -X POST "http://localhost:8080/api/v1/patient-plan-phases/1/items?autoSubmit=true" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": 45
  }'
```

**Verify**: 
- Item added ✅
- Plan auto-submitted → `PENDING_APPROVAL` ✅

---

## Error Handling

### Error 1: Invalid Item Status
```json
{
  "statusCode": 400,
  "message": "Item 123 has status COMPLETED, cannot book"
}
```

### Error 2: Wrong Patient
```json
{
  "statusCode": 400,
  "message": "Item 123 belongs to patient BN-1003, not BN-1002"
}
```

### Error 3: Mixed Booking Modes
```json
{
  "statusCode": 400,
  "message": "Cannot specify both serviceCodes and patientPlanItemIds"
}
```

---

# API Reference

## Phase 5 APIs

### GET Treatment Plan Detail
```
GET /api/v1/patients/{patientCode}/treatment-plans/{planCode}

Response includes:
- phases[].items[].serviceCode ✅ NEW
- phases[].items[].appointments[] (if linked)
```

### POST Create Appointment
```
POST /api/v1/appointments

Request:
{
  "patientCode": "BN-1002",
  "patientPlanItemIds": [123, 124], ✅ NEW
  "roomId": 1,
  "doctorId": 5,
  "appointmentDate": "2025-11-25",
  "appointmentStartTime": "09:00",
  "appointmentEndTime": "10:30"
}

Auto Actions:
- Update item status → SCHEDULED
- Activate plan → IN_PROGRESS
- Create bridge records
```

---

## V21.4 & V21.5 APIs

### PATCH Submit for Review (5.12)
```
PATCH /api/v1/patient-treatment-plans/{planCode}/submit-for-review
Body: { "notes": "..." }
```

### PATCH Update Prices (5.13)
```
PATCH /api/v1/patient-treatment-plans/{planCode}/prices
Body: {
  "items": [{ "itemId": 1, "newPrice": 5000000, "note": "..." }],
  "discountAmount": 500000,
  "discountNote": "..."
}
```

### PATCH Reorder Items (5.14)
```
PATCH /api/v1/patient-plan-phases/{phaseId}/items/reorder
Body: { "itemIds": [3, 1, 2] }
```

### POST Add Item with Auto Submit (5.7)
```
POST /api/v1/patient-plan-phases/{phaseId}/items?autoSubmit=true
Body: {
  "serviceId": 45,
  "price": 5000000 // Optional
}
```

---

## 💡 Key Benefits Summary

### Phase 5
- ✅ **No extra API calls** - serviceCode in response
- ✅ **Faster booking** - Pre-fill instantly
- ✅ **Auto status management** - Backend handles all updates
- ✅ **Type-safe** - Clear interfaces

### V21.4 & V21.5
- ✅ **Finance control** - Dedicated price management
- ✅ **Drag & drop** - Intuitive reordering
- ✅ **Optional price** - Auto-fill from service
- ✅ **Auto submit** - Streamlined workflow

---

## 🆘 Support

**Questions?** Contact backend team
**API Docs**: `/docs/api-guides/`
**Tested On**: 2025-11-19
**Version**: V21.4, V21.5, Phase 5

---

## ✅ Complete Implementation Checklist

### Phase 5 (Appointment Booking)
- [ ] Add `serviceCode` to TreatmentPlanItem interface
- [ ] Add `patientPlanItemIds` to CreateAppointmentRequest
- [ ] Create item selection UI (checkboxes)
- [ ] Create booking dialog component
- [ ] Implement auto-duration calculation
- [ ] Call API with patientPlanItemIds
- [ ] Handle success/error responses
- [ ] Refresh plan after booking
- [ ] Display appointment links on items
- [ ] Test with BN-1002, PLAN-20240515-001

### V21.4 & V21.5 APIs
- [ ] Implement Submit for Review UI (5.12)
- [ ] Implement Update Prices UI (5.13) - Finance role
- [ ] Implement Drag & Drop reorder (5.14)
- [ ] Update Add Item form - make price optional
- [ ] Add autoSubmit checkbox to Add Item form
- [ ] Test all APIs with real data
- [ ] Handle all error scenarios
- [ ] Update UI for new statuses

---

**🎉 Ready to implement! All backend features are tested and working. Happy coding! 🚀**
