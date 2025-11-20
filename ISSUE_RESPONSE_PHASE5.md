# Issue Response: Treatment Plan Appointment Booking Integration

## 👋 Hi FE Team!

Backend đã hoàn thành Phase 5 implementation cho appointment booking từ treatment plan items. Tất cả changes bạn cần đã ready để integrate!

---

## ✅ What's Done (Backend)

### 1. Added `serviceCode` to API Response
Giờ FE không cần call thêm service API để lấy code. Tất cả có sẵn trong treatment plan response:

```json
{
  "itemId": 123,
  "serviceCode": "ENDO_TREAT_ANT", // ✅ NEW FIELD
  "itemName": "Điều trị tủy răng cửa",
  "status": "READY_FOR_BOOKING"
}
```

### 2. Support Booking with Treatment Plan Items
FE có thể book appointment trực tiếp với `patientPlanItemIds`:

```json
POST /api/v1/appointments
{
  "patientCode": "BN-1002",
  "patientPlanItemIds": [123, 124], // ✅ NEW - book từ plan
  "appointmentDate": "2025-11-25",
  "appointmentStartTime": "09:00",
  ...
}
```

### 3. Automatic Status Management
Backend tự động handle:
- ✅ Items update: `READY_FOR_BOOKING` → `SCHEDULED`
- ✅ Plan activation: `PENDING` → `IN_PROGRESS` (if first appointment)
- ✅ Validation: Chỉ book items thuộc về patient
- ✅ XOR check: Không được mix `patientPlanItemIds` và `serviceCodes`

---

## 📋 What FE Needs to Do

### Quick Checklist:
1. **Update TypeScript types** - Add `serviceCode: string` field
2. **Add item selection UI** - Checkboxes cho items `READY_FOR_BOOKING`
3. **Create booking dialog** - Pre-fill từ selected items
4. **Call API with `patientPlanItemIds`** - Instead of serviceCodes
5. **Refresh after booking** - Show updated statuses

### Example TypeScript:
```typescript
interface TreatmentPlanItem {
  itemId: number;
  serviceCode: string; // ✅ ADD THIS
  status: 'READY_FOR_BOOKING' | 'SCHEDULED' | ...;
}

interface CreateAppointmentRequest {
  patientPlanItemIds: number[]; // ✅ ADD THIS
}
```

---

## 🧪 Test It Now!

**Live Test Data**:
```bash
# Get treatment plan (has serviceCode)
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

**Expected**: Items auto-update to `SCHEDULED` status ✅

---

## 📚 Documentation

**Quick Reference** (5 mins read):
👉 [`FE_PHASE5_QUICK_SUMMARY.md`](./FE_PHASE5_QUICK_SUMMARY.md)

**Full Implementation Guide** (React code, examples, testing):
👉 [`FE_PHASE5_APPOINTMENT_BOOKING_GUIDE.md`](./FE_PHASE5_APPOINTMENT_BOOKING_GUIDE.md)

---

## 💡 Key Benefits

**Before**:
- FE calls 2 APIs (plan + service details)
- Manual status tracking
- More network overhead

**After (Phase 5)**:
- FE calls 1 API (plan with serviceCode)
- Automatic status updates by backend
- Faster booking flow 🚀

---

## 🎯 Ready to Start?

1. Read quick summary: `FE_PHASE5_QUICK_SUMMARY.md`
2. Check full guide for code examples: `FE_PHASE5_APPOINTMENT_BOOKING_GUIDE.md`
3. Test with real data: Patient `BN-1002`, Plan `PLAN-20240515-001`
4. Integrate into your app
5. Let us know if you need help! 🙌

---

**Backend Status**: ✅ Complete and Tested
**Frontend Status**: ⏳ Ready to Implement
**Tested On**: 2025-11-19
**Version**: V21.5 - Phase 5

Happy coding! 🚀
