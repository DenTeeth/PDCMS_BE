# Phase 5: Treatment Plan Appointment Booking - Frontend Implementation Guide

## 📋 Overview

**Issue**: Frontend cần integrate appointment booking trực tiếp từ treatment plan items.

**Solution**: Backend đã thêm `serviceCode` vào API response và support booking với `patientPlanItemIds`.

**Status**: ✅ Backend Implementation Complete - Ready for FE Integration

---

## 🎯 What's New - Backend Changes

### 1. ✅ Added `serviceCode` Field to Item Response

**API Endpoint**: `GET /api/v1/patients/{patientCode}/treatment-plans/{planCode}`

**Before (Old Response)**:
```json
{
  "phases": [
    {
      "items": [
        {
          "itemId": 123,
          "serviceId": 45,
          // ❌ Missing serviceCode - FE phải call thêm API để lấy service details
          "itemName": "Điều trị tủy răng cửa",
          "price": 5000000.00,
          "status": "READY_FOR_BOOKING"
        }
      ]
    }
  ]
}
```

**After (New Response)**:
```json
{
  "phases": [
    {
      "items": [
        {
          "itemId": 123,
          "serviceId": 45,
          "serviceCode": "ENDO_TREAT_ANT", // ✅ NEW - No extra API call needed!
          "itemName": "Điều trị tủy răng cửa",
          "price": 5000000.00,
          "status": "READY_FOR_BOOKING"
        }
      ]
    }
  ]
}
```

**Benefits**:
- ✅ FE không cần gọi thêm service API
- ✅ Giảm số lượng network requests
- ✅ Pre-fill appointment form nhanh hơn
- ✅ Better performance & UX

### 2. ✅ Support Booking with Treatment Plan Items

**API Endpoint**: `POST /api/v1/appointments`

**Request Body** (Already Supported):
```json
{
  "patientCode": "BN-1002",
  "roomId": 1,
  "doctorId": 5,
  "appointmentDate": "2025-11-25",
  "appointmentStartTime": "09:00",
  "appointmentEndTime": "10:30",
  "notes": "Khám theo kế hoạch điều trị",
  
  // ✅ Option 1: Book từ treatment plan (Phase 5)
  "patientPlanItemIds": [123, 124, 125],
  
  // ❌ Option 2: Book standalone (existing)
  // "serviceCodes": ["ENDO_TREAT_ANT", "CROWN_ZIR_KATANA"]
}
```

**Validation Rules**:
- ⚠️ **XOR**: EITHER `patientPlanItemIds` OR `serviceCodes` (không được cả 2)
- ⚠️ Items phải có status `READY_FOR_BOOKING`
- ⚠️ Items phải thuộc về patient trong request
- ✅ Automatic status update: `READY_FOR_BOOKING` → `SCHEDULED`
- ✅ Plan auto-activate: `PENDING` → `IN_PROGRESS` (nếu appointment đầu tiên)

---

## 🛠️ Frontend Implementation Steps

### Step 1: Update TypeScript Interfaces

**File**: `types/treatment-plan.ts` (hoặc tương tự)

```typescript
// ✅ ADD serviceCode field
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
  patientPlanItemIds: number[]; // ✅ Use this for Phase 5 booking
}
```

---

### Step 2: Update API Service

**File**: `services/appointment.service.ts`

```typescript
import axios from 'axios';

export class AppointmentService {
  /**
   * ✅ NEW: Book appointment from treatment plan items
   * Phase 5: Automatic item status update + plan activation
   */
  static async createAppointmentFromPlan(
    request: CreateAppointmentFromPlanRequest
  ): Promise<AppointmentResponse> {
    const response = await axios.post('/api/v1/appointments', request, {
      headers: {
        Authorization: `Bearer ${getToken()}`,
      },
    });
    return response.data;
  }

  /**
   * Existing: Book standalone appointment
   */
  static async createStandaloneAppointment(
    request: CreateStandaloneAppointmentRequest
  ): Promise<AppointmentResponse> {
    const response = await axios.post('/api/v1/appointments', request, {
      headers: {
        Authorization: `Bearer ${getToken()}`,
      },
    });
    return response.data;
  }
}
```

---

### Step 3: Create Booking Dialog Component

**File**: `components/BookAppointmentFromPlanDialog.tsx`

```typescript
import React, { useState, useEffect } from 'react';
import { TreatmentPlanItem } from '@/types/treatment-plan';
import { AppointmentService } from '@/services/appointment.service';

interface Props {
  open: boolean;
  onClose: () => void;
  selectedItems: TreatmentPlanItem[]; // Items from treatment plan
  patientCode: string;
  onSuccess: () => void;
}

export const BookAppointmentFromPlanDialog: React.FC<Props> = ({
  open,
  onClose,
  selectedItems,
  patientCode,
  onSuccess,
}) => {
  // ✅ Pre-fill form from selected items
  const [formData, setFormData] = useState({
    patientCode,
    roomId: null,
    doctorId: null,
    appointmentDate: '',
    appointmentStartTime: '',
    appointmentEndTime: '',
    notes: `Khám theo kế hoạch điều trị: ${selectedItems.map(i => i.itemName).join(', ')}`,
    patientPlanItemIds: selectedItems.map(item => item.itemId), // ✅ NEW
  });

  // ✅ Calculate total duration from items
  const totalDuration = selectedItems.reduce(
    (sum, item) => sum + (item.estimatedTimeMinutes || 0),
    0
  );

  // ✅ Auto-calculate end time based on duration
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
      // ✅ Call backend with patientPlanItemIds
      await AppointmentService.createAppointmentFromPlan(formData);
      
      toast.success('Đặt lịch thành công! Items đã được cập nhật sang SCHEDULED');
      onSuccess(); // Refresh treatment plan to show updated status
      onClose();
    } catch (error) {
      if (error.response?.status === 400) {
        toast.error(error.response.data.message);
      } else {
        toast.error('Có lỗi xảy ra khi đặt lịch');
      }
    }
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Đặt Lịch Hẹn Từ Kế Hoạch Điều Trị</DialogTitle>
      
      <DialogContent>
        {/* ✅ Show selected items */}
        <div className="mb-4">
          <h3 className="font-semibold mb-2">Dịch vụ đã chọn:</h3>
          {selectedItems.map(item => (
            <div key={item.itemId} className="flex items-center gap-2 p-2 bg-gray-50 rounded">
              <span className="text-sm">
                {item.itemName} - {item.serviceCode}
              </span>
              <span className="text-xs text-gray-500">
                ({item.estimatedTimeMinutes || 0} phút)
              </span>
            </div>
          ))}
          <div className="mt-2 text-sm text-blue-600">
            Tổng thời gian ước tính: {totalDuration} phút
          </div>
        </div>

        {/* Form fields: date, time, room, doctor, notes */}
        {/* ... (standard form fields) ... */}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Hủy</Button>
        <Button onClick={handleSubmit} variant="contained">
          Đặt Lịch
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

---

### Step 4: Update Treatment Plan View

**File**: `pages/TreatmentPlanDetail.tsx`

```typescript
import React, { useState } from 'react';
import { TreatmentPlanItem } from '@/types/treatment-plan';
import { BookAppointmentFromPlanDialog } from '@/components/BookAppointmentFromPlanDialog';

export const TreatmentPlanDetail: React.FC = () => {
  const [selectedItems, setSelectedItems] = useState<TreatmentPlanItem[]>([]);
  const [showBookingDialog, setShowBookingDialog] = useState(false);

  // ✅ Filter items that can be booked
  const bookableItems = planData.phases
    .flatMap(phase => phase.items)
    .filter(item => item.status === 'READY_FOR_BOOKING');

  const handleItemSelect = (item: TreatmentPlanItem) => {
    setSelectedItems(prev => {
      const exists = prev.find(i => i.itemId === item.itemId);
      if (exists) {
        return prev.filter(i => i.itemId !== item.itemId);
      } else {
        return [...prev, item];
      }
    });
  };

  const handleOpenBookingDialog = () => {
    if (selectedItems.length === 0) {
      toast.warning('Vui lòng chọn ít nhất 1 dịch vụ để đặt lịch');
      return;
    }
    setShowBookingDialog(true);
  };

  return (
    <div>
      {/* ✅ Show "Book Appointment" button */}
      <div className="mb-4">
        <Button
          variant="contained"
          onClick={handleOpenBookingDialog}
          disabled={selectedItems.length === 0}
        >
          Đặt Lịch ({selectedItems.length} dịch vụ)
        </Button>
      </div>

      {/* ✅ Show items with checkboxes */}
      {planData.phases.map(phase => (
        <div key={phase.phaseId}>
          <h3>{phase.phaseName}</h3>
          
          {phase.items.map(item => (
            <div key={item.itemId} className="flex items-center gap-2 p-2 border rounded">
              {/* ✅ Checkbox for READY_FOR_BOOKING items */}
              {item.status === 'READY_FOR_BOOKING' && (
                <Checkbox
                  checked={selectedItems.some(i => i.itemId === item.itemId)}
                  onChange={() => handleItemSelect(item)}
                />
              )}
              
              <div className="flex-1">
                <div className="font-medium">{item.itemName}</div>
                <div className="text-sm text-gray-500">
                  {item.serviceCode} - {formatCurrency(item.price)}
                </div>
                <StatusBadge status={item.status} />
                
                {/* ✅ Show linked appointments */}
                {item.appointments && item.appointments.length > 0 && (
                  <div className="mt-1 text-sm text-blue-600">
                    📅 Đã có lịch: {item.appointments[0].appointmentCode}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      ))}

      {/* ✅ Booking dialog */}
      <BookAppointmentFromPlanDialog
        open={showBookingDialog}
        onClose={() => setShowBookingDialog(false)}
        selectedItems={selectedItems}
        patientCode={patientCode}
        onSuccess={() => {
          setSelectedItems([]);
          refetchPlanData(); // Refresh to show updated statuses
        }}
      />
    </div>
  );
};
```

---

## 🔍 Testing Guide

### Test Case 1: Get Treatment Plan with serviceCode

**Request**:
```bash
GET /api/v1/patients/BN-1002/treatment-plans/PLAN-20240515-001
Authorization: Bearer <token>
```

**Expected Response**:
```json
{
  "statusCode": 200,
  "data": {
    "planCode": "PLAN-20240515-001",
    "phases": [
      {
        "items": [
          {
            "itemId": 123,
            "serviceId": 45,
            "serviceCode": "ENDO_TREAT_ANT", // ✅ Check this field exists
            "status": "READY_FOR_BOOKING"
          }
        ]
      }
    ]
  }
}
```

**Verify**: ✅ `serviceCode` field có trong response

---

### Test Case 2: Book Appointment from Plan Items

**Request**:
```bash
POST /api/v1/appointments
Authorization: Bearer <token>
Content-Type: application/json

{
  "patientCode": "BN-1002",
  "roomId": 1,
  "doctorId": 5,
  "appointmentDate": "2025-11-25",
  "appointmentStartTime": "09:00",
  "appointmentEndTime": "10:30",
  "patientPlanItemIds": [123, 124]
}
```

**Expected Response**:
```json
{
  "statusCode": 200,
  "data": {
    "appointmentCode": "APPT-20251125-001",
    "status": "SCHEDULED",
    "linkedPlanItems": [
      {
        "itemId": 123,
        "oldStatus": "READY_FOR_BOOKING",
        "newStatus": "SCHEDULED" // ✅ Auto-updated
      }
    ]
  }
}
```

**Verify**:
1. ✅ Appointment created successfully
2. ✅ Items status changed: `READY_FOR_BOOKING` → `SCHEDULED`
3. ✅ Plan status changed: `PENDING` → `IN_PROGRESS` (if first appointment)

---

### Test Case 3: Error Handling

**Scenario 1**: Items not READY_FOR_BOOKING
```json
{
  "statusCode": 400,
  "error": "error.bad.request",
  "message": "Item 123 has status COMPLETED, cannot book (must be READY_FOR_BOOKING)"
}
```

**Scenario 2**: Items belong to different patient
```json
{
  "statusCode": 400,
  "error": "error.bad.request",
  "message": "Item 123 belongs to patient BN-1003, not BN-1002"
}
```

**Scenario 3**: Using both patientPlanItemIds AND serviceCodes
```json
{
  "statusCode": 400,
  "error": "error.bad.request",
  "message": "Cannot specify both serviceCodes and patientPlanItemIds. Choose one booking mode."
}
```

---

## 📊 Status Flow Diagram

```
Treatment Plan Lifecycle:
┌─────────────────────────────────────────────────────────────┐
│ PENDING → IN_PROGRESS → PAUSED → IN_PROGRESS → COMPLETED   │
└─────────────────────────────────────────────────────────────┘
                   ↑
                   │ First appointment booked
                   │

Item Status Flow:
┌──────────────────────────────────────────────────────────────┐
│ NOT_STARTED → READY_FOR_BOOKING → SCHEDULED → IN_PROGRESS → │
│                                                 COMPLETED     │
└──────────────────────────────────────────────────────────────┘
                              ↑
                              │ Book appointment (Phase 5)
                              │ POST /appointments with patientPlanItemIds
```

---

## ⚠️ Important Notes for Frontend

### 1. Validation Before Booking

```typescript
// ✅ Check items before showing booking dialog
const canBookItems = (items: TreatmentPlanItem[]): boolean => {
  return items.every(item => item.status === 'READY_FOR_BOOKING');
};

if (!canBookItems(selectedItems)) {
  toast.error('Chỉ có thể đặt lịch cho items có trạng thái READY_FOR_BOOKING');
  return;
}
```

### 2. Refresh After Booking

```typescript
// ✅ Always refresh plan data after booking
const handleBookingSuccess = () => {
  refetchTreatmentPlan(); // Items status will be updated to SCHEDULED
  setSelectedItems([]);
  toast.success('Đặt lịch thành công!');
};
```

### 3. Display Changes

- **serviceCode** giờ có sẵn trong response → Không cần fetch thêm
- **appointments** array trong item → Show linked appointments
- **status** tự động update → Hiển thị realtime status

### 4. UX Recommendations

- ✅ Show checkbox chỉ cho items `READY_FOR_BOOKING`
- ✅ Auto-calculate appointment duration từ items
- ✅ Show total price của selected items
- ✅ Confirm dialog trước khi book
- ✅ Toast notification khi success/error
- ✅ Disable re-booking items đã `SCHEDULED`

---

## 📚 API Reference

### Get Treatment Plan Detail
- **Endpoint**: `GET /api/v1/patients/{patientCode}/treatment-plans/{planCode}`
- **Response**: Includes `serviceCode` in each item
- **New Field**: `item.serviceCode` (String)

### Create Appointment
- **Endpoint**: `POST /api/v1/appointments`
- **New Field**: `patientPlanItemIds` (Array<number>)
- **Auto Actions**:
  - Update item status → `SCHEDULED`
  - Create bridge records (appointment_plan_items)
  - Activate plan (PENDING → IN_PROGRESS) if first appointment

---

## 🎉 Benefits Summary

### For Frontend
- ✅ **No extra API calls** - serviceCode included in response
- ✅ **Faster UX** - Pre-fill appointment form instantly
- ✅ **Automatic updates** - Items & plan status managed by backend
- ✅ **Type-safe** - Clear TypeScript interfaces

### For Backend
- ✅ **Single query** - No N+1 problem with service JOIN
- ✅ **Data integrity** - Automatic status validation
- ✅ **Audit trail** - All changes tracked via bridge table

---

## 🆘 Support & Questions

**Backend Contact**: [Your Team]
**API Documentation**: `/docs/api-guides/`
**Tested On**: 2025-11-19
**Verified Examples**:
- Patient: `BN-1002`
- Plan: `PLAN-20240515-001`
- Service Codes: `ENDO_POST_CORE`, `IMPL_IMPRESSION`, `CROWN_ZIR_KATANA`

---

## ✅ Checklist for FE Implementation

- [ ] Update TypeScript interfaces với `serviceCode` field
- [ ] Update API service với `patientPlanItemIds` parameter
- [ ] Create booking dialog component
- [ ] Add item selection UI (checkboxes)
- [ ] Implement validation (READY_FOR_BOOKING check)
- [ ] Handle success/error responses
- [ ] Refresh plan data after booking
- [ ] Test with real data (BN-1002, PLAN-20240515-001)
- [ ] Handle edge cases (already scheduled items, wrong patient, etc.)
- [ ] Update UI to show appointment links on items

---

**Status**: ✅ Ready for Frontend Implementation
**Last Updated**: 2025-11-19
**Backend Version**: V21.5 - Phase 5
