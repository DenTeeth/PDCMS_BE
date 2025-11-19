# ✅ BACKEND RESPONSE: Treatment Plan Status Workflow Clarification

**Date**: 2025-11-18
**Purpose**: Trả lời câu hỏi của Frontend về status workflow sau khi approve/reject

---

## 📋 TÓM TẮT: FE HỎI ĐÚng 95% ✅

Frontend team đã hiểu đúng hầu hết workflow. Chỉ có **MỘT điểm cần làm rõ** về rejection behavior.

---

## ✅ XÁC NHẬN: Những Gì FE Hiểu ĐÚNG

### 1. ✅ Sau khi Approve: approvalStatus = APPROVED (mãi mãi)

**ĐÚNG!** Code backend:

```java
// TreatmentPlanApprovalService.java line 211-221
private ApprovalStatus determineNewApprovalStatus(ApproveTreatmentPlanRequest request) {
    if (request.isApproval()) {
        return ApprovalStatus.APPROVED; // ✅ Stays APPROVED forever
    } else if (request.isRejection()) {
        return ApprovalStatus.DRAFT; // Returns to DRAFT
    }
}
```

**Xác nhận**: Sau khi approve, `approvalStatus` sẽ **LÀ và MÃI MÃI LÀ** `APPROVED`.

---

### 2. ✅ Sau khi Approve: status vẫn là PENDING (chờ activate)

**ĐÚNG!** Backend **KHÔNG tự động chuyển** `status` khi approve.

**Code backend**:

```java
// TreatmentPlanApprovalService.java line 95-107
ApprovalStatus newStatus = determineNewApprovalStatus(request);
plan.setApprovalStatus(newStatus);  // ✅ Only change approvalStatus
plan.setApprovedBy(manager);
plan.setApprovedAt(LocalDateTime.now());

// ❌ KHÔNG có code chuyển plan.status
// status vẫn giữ nguyên PENDING
```

**Xác nhận**: Sau khi approve:

- `approvalStatus`: `PENDING_REVIEW` → `APPROVED` ✅
- `status`: `PENDING` → `PENDING` (không đổi) ✅

---

### 3. ✅ Plan chuyển PENDING → IN_PROGRESS khi đặt lịch đầu tiên

**ĐÚNG theo thiết kế!** Nhưng **hiện tại chưa implement**.

**Code comment trong ApprovalStatus.java**:

```java
// line 42-43
/**
 * APPROVED: Đã duyệt.
 * - Quản lý phê duyệt (giá override hợp lý).
 * - Lộ trình này có thể kích hoạt (API 5.5) và đặt lịch.
 */
APPROVED,
```

**Hiện trạng**:

- ✅ Thiết kế: Plan tự động → IN_PROGRESS khi đặt lịch đầu tiên
- ✅ Code: **ĐÃ IMPLEMENT AUTO-ACTIVATION** (V21.3)
- ⚠️ API 5.5 (Activate Plan): **KHÔNG CẦN** (auto-activation thay thế)

**Implementation**: Xem chi tiết ở Section "Q1: Auto-activation" bên dưới.

---

### 4. ✅ Plan tự động → COMPLETED khi tất cả phases done

**ĐÚNG và ĐÃ IMPLEMENT!** (V21.3)

**Implementation Details**:

```java
// TreatmentPlanItemService.java (V21.3)
// Auto-completes plan when ALL phases are done
private void checkAndCompletePlan(PatientTreatmentPlan plan) {
    if (plan.getStatus() != IN_PROGRESS) {
        return; // Only check IN_PROGRESS plans
    }

    boolean allPhasesCompleted = plan.getPhases().stream()
        .allMatch(phase -> phase.getStatus() == PhaseStatus.COMPLETED);

    if (allPhasesCompleted) {
        plan.setStatus(TreatmentPlanStatus.COMPLETED);
        log.info("✅ Auto-completed plan {} (IN_PROGRESS → COMPLETED)", planCode);
    }
}
```

**Behavior**:

- ✅ Triggers when doctor marks **LAST item** as COMPLETED
- ✅ After phase auto-completion check
- ✅ Checks if **ALL phases** are COMPLETED
- ✅ Automatically sets `plan.status = COMPLETED`
- ✅ Logged for audit trail
- ✅ Transactional (rolls back if fails)

---

## ⚠️ LÀM RÕ: Rejection Behavior (Điểm FE Hơi Nhầm)

### ❓ FE Hỏi:

> **"Khi Manager reject plan (API 5.9 with status=REJECTED):**
>
> - Response 5.9 trả về `approvalStatus: REJECTED` hay `DRAFT`?
> - Response 5.2 (sau khi reject) trả về `approvalStatus: REJECTED` hay `DRAFT`?
>
> **Frontend hiện tại** đang hiển thị "ĐÃ TỪ CHỐI" ngay khi API 5.9 trả về REJECTED."

### ✅ BACKEND ANSWER:

**API 5.9 Response** (ngay sau khi reject):

```json
{
  "planCode": "PLAN-001",
  "approvalStatus": "DRAFT", // ✅ TRẢ VỀ DRAFT NGAY
  "status": "PENDING",
  "approvalMetadata": {
    "approvedBy": "Manager Name",
    "approvedAt": "2025-11-18T10:00:00",
    "notes": "Lý do từ chối..." // Rejection reason
  }
}
```

**Code backend**:

```java
// TreatmentPlanApprovalService.java line 211-221
private ApprovalStatus determineNewApprovalStatus(ApproveTreatmentPlanRequest request) {
    if (request.isApproval()) {
        return ApprovalStatus.APPROVED;
    } else if (request.isRejection()) {
        return ApprovalStatus.DRAFT; // ✅ IMMEDIATELY returns to DRAFT
    }
}

// Line 95-107
ApprovalStatus newStatus = determineNewApprovalStatus(request);
plan.setApprovalStatus(newStatus); // Sets to DRAFT immediately
plan = planRepository.save(plan);   // Saves DRAFT to DB

// Line 119
TreatmentPlanDetailResponse response = mapToDetailResponse(plan);
// Response contains approvalStatus: DRAFT ✅
```

**Xác nhận**:

- ✅ API 5.9 response **TRẢ VỀ `DRAFT` NGAY** (không phải `REJECTED`)
- ✅ Database lưu `approvalStatus = DRAFT` ngay lập tức
- ✅ API 5.2 (sau khi reject) cũng trả về `approvalStatus = DRAFT`
- ❌ **KHÔNG BAO GIỜ** có state `REJECTED` trong response hay database

### 🎯 REJECTED Là Gì?

`REJECTED` **CHỈ LÀ INPUT** trong request, **KHÔNG PHẢI STATUS** trong database/response.

```
Flow:
Manager gửi request { approvalStatus: "REJECTED" }
  ↓
Backend xử lý: IF (request = REJECTED) → SET plan.approvalStatus = DRAFT
  ↓
Backend save: plan.approvalStatus = DRAFT
  ↓
Backend response: { approvalStatus: "DRAFT" }
```

**ApprovalStatus enum chỉ có 4 giá trị**:

```java
public enum ApprovalStatus {
    DRAFT,           // ✅ Có trong DB/Response
    PENDING_REVIEW,  // ✅ Có trong DB/Response
    APPROVED,        // ✅ Có trong DB/Response
    REJECTED         // ❌ CHỈ dùng trong Request, KHÔNG lưu vào DB
}
```

**⚠️ LƯU Ý**:

- Enum có `REJECTED` value
- Nhưng backend **KHÔNG BAO GIỜ LƯU** `REJECTED` vào database
- Backend **LUÔN CONVERT** `REJECTED` → `DRAFT` ngay

---

## 🔧 FRONTEND CẦN SỬA

### Vấn đề hiện tại:

Frontend đang:

```typescript
// ❌ SAI: FE hiển thị "ĐÃ TỪ CHỐI" dựa trên response
if (response.approvalStatus === "REJECTED") {
  showRejectedBadge(); // ❌ Sẽ KHÔNG BAO GIỜ XẢY RA
}
```

### Sửa thành:

**Option 1: Dựa vào approvalMetadata.notes** (Khuyến nghị)

```typescript
// ✅ ĐÚNG: Check notes để biết có phải rejection không
if (response.approvalStatus === "DRAFT" && response.approvalMetadata?.notes) {
  // Có notes + DRAFT = vừa bị reject
  showRejectedMessage(response.approvalMetadata.notes);
} else if (response.approvalStatus === "DRAFT") {
  // DRAFT thuần túy (chưa submit)
  showDraftBadge();
}
```

**Option 2: Backend thêm field `wasRejected`** (Nếu FE cần rõ ràng hơn)

```typescript
// Backend có thể thêm vào response:
{
  "approvalStatus": "DRAFT",
  "wasRejected": true,  // NEW: Indicate this is a rejected plan
  "approvalMetadata": {
    "rejectedBy": "Manager Name",
    "rejectedAt": "2025-11-18T10:00:00",
    "rejectionReason": "..."
  }
}
```

### Khuyến nghị:

**Dùng Option 1** (check `approvalMetadata.notes`):

- ✅ Không cần thay đổi backend
- ✅ Rejection reason luôn có trong notes
- ✅ FE có thể hiển thị lý do reject
- ✅ Phân biệt được DRAFT thuần vs DRAFT-after-rejection

---

## 📊 FLOW CHART: Rejection Behavior

```
┌─────────────────────────────────────────────────────────────┐
│  REJECTION FLOW (What Actually Happens)                      │
└─────────────────────────────────────────────────────────────┘

Manager clicks "Từ chối" button
  ↓
FE gửi: { approvalStatus: "REJECTED", notes: "Lý do..." }
  ↓
BE validate: plan.approvalStatus == PENDING_REVIEW? ✅
  ↓
BE validate: notes có text? ✅
  ↓
BE logic: determineNewApprovalStatus(request)
  ↓
  IF (request.approvalStatus == "REJECTED")
    THEN newStatus = ApprovalStatus.DRAFT  // ✅ Convert to DRAFT
  ↓
BE save: plan.approvalStatus = DRAFT
BE save: plan.rejectionReason = "Lý do..."
BE save: plan.approvedBy = Manager
BE save: plan.approvedAt = NOW()
  ↓
BE response: {
  "approvalStatus": "DRAFT",  // ✅ NOT "REJECTED"
  "approvalMetadata": {
    "approvedBy": "Manager",
    "approvedAt": "...",
    "notes": "Lý do từ chối"
  }
}
  ↓
FE nhận: approvalStatus = "DRAFT"
FE check: có approvalMetadata.notes?
  → YES: Show "Đã từ chối: [Lý do...]"
  → NO: Show "Bản nháp"
```

---

## 🎯 FINAL ANSWERS: Questions for Backend

### Q1: Auto-activation - Plan tự động PENDING → IN_PROGRESS?

**A**: ✅ **ĐÃ IMPLEMENT** (V21.3).

**Implementation Details**:

```java
// AppointmentCreationService.java (V21.3)
// Auto-activates plan when creating FIRST appointment
private void activatePlanIfFirstAppointment(Appointment appointment, List<Long> itemIds) {
    // Get plan from items
    PatientTreatmentPlan plan = firstItem.getPhase().getTreatmentPlan();

    // Check eligibility
    if (plan.getStatus() == PENDING && plan.getApprovalStatus() == APPROVED) {
        // Check if this is first appointment
        long appointmentCount = appointmentPlanItemRepository.countAppointmentsForPlan(planId);

        if (appointmentCount == 1) {
            // AUTO-ACTIVATE
            plan.setStatus(IN_PROGRESS);
            log.info("✅ Auto-activated plan {} (PENDING → IN_PROGRESS)", planCode);
        }
    }
}
```

**Behavior**:

- ✅ Triggers when receptionist books **FIRST** appointment for plan
- ✅ Only if `plan.status == PENDING` and `plan.approvalStatus == APPROVED`
- ✅ Automatically sets `plan.status = IN_PROGRESS`
- ✅ Logged for audit trail
- ✅ Transactional (rolls back if fails)

---

### Q2: Rejection behavior - Response trả về REJECTED hay DRAFT?

**A**: ✅ **TRẢ VỀ `DRAFT` NGAY**.

**Backend luôn convert**: `REJECTED` (input) → `DRAFT` (stored & returned).

**Frontend cần**: Check `approvalMetadata.notes` để phân biệt DRAFT-after-rejection vs pure-DRAFT.

---

### Q3: Plan cancellation API - Có không?

**A**: ❌ **CHƯA CÓ API CANCEL PLAN**.

**Use case**: Bệnh nhân không tiếp tục điều trị.

**Recommendation**:

- Option 1: Thêm API `PATCH /patient-treatment-plans/{planCode}/cancel`
- Option 2: Dùng status update API (nếu có) để chuyển → `CANCELLED`

---

### Q4: API 5.5 Activate Plan - Có không?

**A**: ❌ **CHƯA CÓ API 5.5**.

**Recommendation**:

- Option 1: Implement auto-activation (khuyến nghị)
- Option 2: Tạo API 5.5 nếu cần manual activation

---

## 📝 ACTION ITEMS

### For Backend Team:

- [x] **P0**: ~~Implement auto-activation logic in AppointmentService~~ ✅ **DONE (V21.3)**

  - ✅ When creating first appointment → set plan.status = IN_PROGRESS
  - ✅ Implemented in `AppointmentCreationService.activatePlanIfFirstAppointment()`

- [x] **P0**: ~~Implement auto-complete logic in TreatmentPlanItemService~~ ✅ **DONE (V21.3)**

  - ✅ When all phases done → set plan.status = COMPLETED
  - ✅ Implemented in `TreatmentPlanItemService.checkAndCompletePlan()`

- [ ] **P1**: Consider adding `wasRejected` flag to response (optional)

  - Giúp FE dễ phân biệt DRAFT vs DRAFT-after-rejection
  - Current workaround: FE check `approvalMetadata.notes`

- [ ] **P2**: ~~Implement API 5.5 Activate Plan (if needed)~~ ❌ **NOT NEEDED**

  - Auto-activation implemented → manual activation not required

- [ ] **P3**: Implement Plan Cancellation API
  - Use case: Patient discontinues treatment
  - Endpoint: `PATCH /patient-treatment-plans/{planCode}/cancel`

### For Frontend Team:

- [ ] **P0**: Fix rejection display logic

  - Check `approvalMetadata.notes` instead of expecting `REJECTED` status

- [ ] **P1**: Update UI flow

  - DRAFT + has notes = "Đã từ chối: [reason]"
  - DRAFT + no notes = "Bản nháp"

- [ ] **P2**: Handle auto-activation (✅ Backend ready V21.3)

  - After booking first appointment → refresh plan detail (API 5.2)
  - Expect status change: `PENDING` → `IN_PROGRESS`
  - Update badge: "Chờ thực hiện" → "Đang thực hiện"

- [ ] **P3**: Handle auto-completion (✅ Backend ready V21.3)
  - After marking last item COMPLETED → refresh plan detail (API 5.2)
  - Expect status change: `IN_PROGRESS` → `COMPLETED`
  - Update badge: "Đang thực hiện" → "Hoàn thành"
  - Show completion timeline/summary

---

## 📞 SUMMARY FOR FRONTEND

### ✅ What FE Got RIGHT:

1. ✅ After approve: `approvalStatus` = `APPROVED` forever
2. ✅ After approve: `status` still `PENDING` (until activated)
3. ✅ Plan should auto-activate on first appointment (design intent)
4. ✅ Plan auto-completes when all phases done

### ⚠️ What FE Needs to FIX:

1. ❌ **REJECTION RESPONSE**:

   - Backend returns `"approvalStatus": "DRAFT"` (NOT "REJECTED")
   - FE should check `approvalMetadata.notes` to detect rejection

2. ⏳ **AUTO-ACTIVATION**:
   - Design says: auto-activate on first appointment
   - Reality: NOT YET IMPLEMENTED in backend
   - FE should expect it in future, but don't rely on it now

### 📋 What to Tell Frontend:

> **"FE team hiểu đúng 95%! Chỉ có 1 điểm cần sửa:**
>
> **Rejection behavior**: Backend **KHÔNG BAO GIỜ** trả về `approvalStatus: "REJECTED"`. Khi Manager reject, backend tự động convert về `"DRAFT"` ngay.
>
> **Cách phân biệt**:
>
> - `DRAFT` + có `approvalMetadata.notes` = Vừa bị reject (hiển thị "Đã từ chối: [lý do]")
> - `DRAFT` + không có `approvalMetadata.notes` = Bản nháp thuần túy
>
> **Auto-activation**: Thiết kế đúng (tự động khi đặt lịch đầu tiên), nhưng backend chưa implement. Sẽ làm sau."

---

**Prepared By**: Backend Team
**Date**: 2025-11-18
**Status**: ✅ Ready to Share with Frontend
