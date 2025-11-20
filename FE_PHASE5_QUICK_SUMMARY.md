# Phase 5: Treatment Plan Appointment Booking - Quick Summary for FE

## ✅ Backend Ready - What FE Needs to Do

### 🎯 Tóm tắt
Backend đã implement xong Phase 5. FE giờ có thể book appointments trực tiếp từ treatment plan items mà không cần call thêm API để lấy service details.

---

## 🚀 Changes Summary

### 1. API Response đã có `serviceCode`

**Endpoint**: `GET /api/v1/patients/{patientCode}/treatment-plans/{planCode}`

```json
{
  "phases": [
    {
      "items": [
        {
          "itemId": 123,
          "serviceCode": "ENDO_TREAT_ANT", // ✅ NEW - Không cần fetch thêm
          "itemName": "Điều trị tủy răng cửa",
          "price": 5000000,
          "status": "READY_FOR_BOOKING"
        }
      ]
    }
  ]
}
```

### 2. Booking API hỗ trợ `patientPlanItemIds`

**Endpoint**: `POST /api/v1/appointments`

```json
{
  "patientCode": "BN-1002",
  "roomId": 1,
  "doctorId": 5,
  "appointmentDate": "2025-11-25",
  "appointmentStartTime": "09:00",
  "appointmentEndTime": "10:30",
  "patientPlanItemIds": [123, 124] // ✅ NEW - Book từ treatment plan
}
```

**Auto Actions**:
- ✅ Items tự động update: `READY_FOR_BOOKING` → `SCHEDULED`
- ✅ Plan tự động activate: `PENDING` → `IN_PROGRESS` (nếu là appointment đầu tiên)

---

## 📝 FE Implementation Checklist

### **Must Do**:
1. ✅ Thêm field `serviceCode: string` vào interface `TreatmentPlanItem`
2. ✅ Thêm field `patientPlanItemIds: number[]` vào `CreateAppointmentRequest`
3. ✅ Tạo UI để select items từ treatment plan (checkboxes)
4. ✅ Hiển thị booking dialog với pre-filled data từ items
5. ✅ Call API `/appointments` với `patientPlanItemIds`
6. ✅ Refresh treatment plan sau khi book (để show updated status)

### **Should Do** (UX Improvements):
- Show checkbox chỉ cho items có status `READY_FOR_BOOKING`
- Auto-calculate appointment duration từ items
- Show total price của selected items
- Show linked appointments trên items đã `SCHEDULED`
- Disable re-booking items đã scheduled

---

## 🧪 Test Data

**Available for Testing**:
```
Patient: BN-1002
Plan: PLAN-20240515-001
Items with service codes:
- ENDO_POST_CORE (Post core)
- ENDO_TREAT_ANT (Điều trị tủy răng cửa)
- IMPL_IMPRESSION (Lấy dấu implant)
- CROWN_ZIR_KATANA (Crown zirconia Katana)
```

**Test với curl**:
```bash
# 1. Get plan (check serviceCode field)
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans/PLAN-20240515-001" \
  -H "Authorization: Bearer <token>"

# 2. Book appointment
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

---

## 📚 Full Documentation

**Chi tiết implementation** (React code, TypeScript, testing, error handling):
👉 [`FE_PHASE5_APPOINTMENT_BOOKING_GUIDE.md`](./FE_PHASE5_APPOINTMENT_BOOKING_GUIDE.md)

---

## ⚠️ Validation Rules (Backend sẽ check)

- ❌ Không được dùng cả `patientPlanItemIds` VÀ `serviceCodes` cùng lúc
- ❌ Items phải có status `READY_FOR_BOOKING`
- ❌ Items phải thuộc về patient trong request
- ✅ Backend tự động update status → FE chỉ cần refresh

---

## 💡 Key Benefits

### Trước (Old Flow):
```
1. FE: Get treatment plan → có serviceId
2. FE: Call service API để lấy serviceCode
3. FE: Pre-fill appointment form
4. FE: Book appointment với serviceCode
5. FE: Manual update UI (nếu có)
```

### Bây giờ (Phase 5 Flow):
```
1. FE: Get treatment plan → có serviceCode luôn ✅
2. FE: Book appointment với patientPlanItemIds ✅
3. BE: Auto update status (READY_FOR_BOOKING → SCHEDULED) ✅
4. FE: Refresh plan → show updated UI ✅
```

**Result**: Ít API calls hơn, faster UX, automatic status management 🚀

---

## 🆘 Questions?

**Full Guide**: `FE_PHASE5_APPOINTMENT_BOOKING_GUIDE.md`
**Status**: ✅ Backend Complete - Ready for FE
**Tested**: 2025-11-19
**Backend Version**: V21.5 - Phase 5
