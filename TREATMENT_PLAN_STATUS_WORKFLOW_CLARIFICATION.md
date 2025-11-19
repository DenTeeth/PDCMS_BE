# 🔍 Làm Rõ: Treatment Plan Status Workflow - Sau Khi Duyệt

**Date**: 2025-11-18  
**Version**: V21  
**Purpose**: Giải thích chi tiết workflow status của Treatment Plan từ lúc tạo đến hoàn thành

---

## 🎯 Câu Hỏi Của Frontend

> **"Duyệt rồi Status nó như thế nào?"**

Câu hỏi này thực ra gồm 2 phần:
1. **ApprovalStatus** sau khi duyệt → Thay đổi thế nào?
2. **TreatmentPlanStatus** (status thực thi) → Thay đổi thế nào?

---

## 📊 Workflow Đầy Đủ: Từ Tạo Plan → Hoàn Thành

### Phase 1: TẠO VÀ DUYỆT PLAN (Approval Workflow)

```
┌─────────────────────────────────────────────────────────────┐
│  APPROVAL WORKFLOW (ApprovalStatus)                          │
└─────────────────────────────────────────────────────────────┘

1. Bác sĩ tạo plan mới
   → approvalStatus: DRAFT
   → status: PENDING (chờ duyệt)
   🔴 CHƯA THỂ ĐIỀU TRỊ

2. Bác sĩ submit for review (API 5.12)
   → approvalStatus: DRAFT → PENDING_REVIEW
   → status: PENDING (không đổi)
   🔴 CHƯA THỂ ĐIỀU TRỊ (đang chờ quản lý duyệt)

3A. Quản lý approve (API 5.9 - APPROVED)
   → approvalStatus: PENDING_REVIEW → APPROVED
   → status: PENDING → PENDING (không đổi tự động)
   🟡 PLAN ĐÃ ĐƯỢC DUYỆT - SẴN SÀNG ĐIỀU TRỊ
   
   ⚠️ LƯU Ý: Plan vẫn ở PENDING cho đến khi:
   - Đặt lịch hẹn đầu tiên (tự động → IN_PROGRESS)
   - Hoặc gọi API 5.5 Activate Plan (nếu có)

3B. Quản lý reject (API 5.9 - REJECTED)
   → approvalStatus: PENDING_REVIEW → REJECTED
   → Backend tự động: REJECTED → DRAFT
   → status: PENDING (không đổi)
   🔴 PLAN BỊ TỪ CHỐI - Bác sĩ cần sửa lại
```

---

### Phase 2: THỰC THI ĐIỀU TRỊ (Treatment Execution)

```
┌─────────────────────────────────────────────────────────────┐
│  TREATMENT EXECUTION (TreatmentPlanStatus)                   │
└─────────────────────────────────────────────────────────────┘

4. Kích hoạt plan (Sau khi APPROVED)
   
   CÁCH 1 (TỰ ĐỘNG - KHUYẾN NGHỊ):
   - Lễ tân/Bác sĩ đặt lịch hẹn đầu tiên
   - Backend tự động: status: PENDING → IN_PROGRESS
   - approvalStatus: APPROVED (không đổi)
   🟢 PLAN ĐANG THỰC HIỆN
   
   CÁCH 2 (THỦ CÔNG - NẾU CÓ API 5.5):
   - Gọi API 5.5 Activate Plan
   - status: PENDING → IN_PROGRESS
   - approvalStatus: APPROVED (không đổi)
   🟢 PLAN ĐANG THỰC HIỆN

5. Tiến hành điều trị
   - Bác sĩ update item status (API 5.6)
   - Item: PENDING → READY_FOR_BOOKING → SCHEDULED → IN_PROGRESS → COMPLETED
   - approvalStatus: APPROVED (không đổi)
   - status: IN_PROGRESS (không đổi)
   🟢 PLAN ĐANG THỰC HIỆN

6. Hoàn thành plan (TỰ ĐỘNG)
   - Khi tất cả items: COMPLETED/SKIPPED
   - Khi tất cả phases: COMPLETED
   - Backend tự động: status: IN_PROGRESS → COMPLETED
   - approvalStatus: APPROVED (không đổi)
   ✅ PLAN HOÀN TẤT
```

---

## 🎨 Timeline Visual

```
TIME ─────────────────────────────────────────────────────→

┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│  DRAFT   │→ │ PENDING_ │→ │ APPROVED │→ │IN_PROGRESS│→│COMPLETED │
│          │  │ REVIEW   │  │          │  │           │  │          │
└──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘
approvalStatus approvalStatus approvalStatus approvalStatus approvalStatus
    DRAFT       PENDING_REVIEW   APPROVED      APPROVED      APPROVED
    
status          status           status        status         status
   PENDING      PENDING          PENDING      IN_PROGRESS   COMPLETED

🔴 Chưa thể    🔴 Chờ duyệt   🟡 Đã duyệt   🟢 Đang điều trị  ✅ Hoàn thành
   điều trị                      chưa bắt đầu
```

---

## 🔑 Key Points: Sau Khi Duyệt (APPROVED)

### 1. approvalStatus = APPROVED

**Ý nghĩa**: Plan đã được quản lý phê duyệt, có thể bắt đầu điều trị.

**Điều KHÔNG thay đổi**:
- ✅ `approvalStatus` sẽ luôn là `APPROVED` (không đổi nữa)
- ✅ Không thể edit/delete items nữa (đã lock)
- ✅ Không thể submit for review lại (đã approved)

**Điều CÓ THỂ làm**:
- ✅ Đặt lịch hẹn cho các items
- ✅ Update item status (API 5.6) - từ PENDING → COMPLETED
- ✅ Xem chi tiết plan (API 5.2)

---

### 2. status = PENDING → IN_PROGRESS (Tự động hoặc thủ công)

**Sau khi approved, plan status vẫn là `PENDING`**

**Plan chuyển sang `IN_PROGRESS` khi**:
- **Tự động**: Khi đặt lịch hẹn đầu tiên (khuyến nghị)
- **Thủ công**: Gọi API 5.5 Activate Plan (nếu có)

**Ví dụ timeline**:
```
09:00 - Manager approve plan
        → approvalStatus: APPROVED
        → status: PENDING (chưa bắt đầu điều trị)

10:30 - Lễ tân đặt lịch hẹn đầu tiên cho item 1
        → Backend tự động: status: PENDING → IN_PROGRESS
        → approvalStatus: APPROVED (không đổi)

11:00 - Bệnh nhân đến phòng khám
        → Bác sĩ update item 1 status: SCHEDULED → IN_PROGRESS
        → Plan status vẫn IN_PROGRESS

12:00 - Bác sĩ hoàn thành item 1
        → Update item 1 status: IN_PROGRESS → COMPLETED
        → Plan status vẫn IN_PROGRESS (còn items khác)
```

---

## 💡 So Sánh: APPROVED vs IN_PROGRESS

### approvalStatus: APPROVED (Approval State)
- **Mục đích**: Phản ánh QUYỀN HẠN và WORKFLOW
- **Ý nghĩa**: "Plan đã được duyệt, có thể bắt đầu"
- **Thay đổi**: Chỉ thay đổi qua approval workflow (API 5.9, 5.12)
- **UI hiển thị**: Text "Đã duyệt" dưới mã plan
- **Business rule**: Không thể edit plan sau khi approved

### status: IN_PROGRESS (Execution State)
- **Mục đích**: Phản ánh TIẾN TRÌNH THỰC TẾ
- **Ý nghĩa**: "Plan đang được thực hiện điều trị"
- **Thay đổi**: Tự động khi đặt lịch/hoàn thành items
- **UI hiển thị**: Badge màu xanh ở header
- **Business rule**: Bệnh nhân đang trong quá trình điều trị

---

## 🔄 Các Trường Hợp Cụ Thể

### Case 1: Plan vừa được approve

**Request**:
```http
GET /api/v1/patients/BN-1001/treatment-plans/PLAN-001
```

**Response**:
```json
{
  "planCode": "PLAN-001",
  "planName": "Implant + Niềng răng",
  "approvalStatus": "APPROVED",     // ✅ Đã duyệt
  "status": "PENDING",               // 🟡 Chưa bắt đầu điều trị
  "phases": [
    {
      "phaseName": "Phase 1: Implant",
      "items": [
        {
          "itemName": "Nhổ răng khôn",
          "status": "PENDING"         // Chưa đặt lịch
        },
        {
          "itemName": "Cấy implant",
          "status": "PENDING"         // Chưa đặt lịch
        }
      ]
    }
  ]
}
```

**Frontend hiển thị**:
- ✅ Text: "Đã duyệt" (dưới mã plan)
- 🟡 Badge: "Chờ thực hiện" (màu xám - PENDING)
- ✅ Button: "Đặt lịch hẹn" (enabled)
- ❌ Button: "Chỉnh sửa" (disabled - đã approved)

---

### Case 2: Plan đã bắt đầu điều trị (có lịch hẹn đầu tiên)

**Request**:
```http
GET /api/v1/patients/BN-1001/treatment-plans/PLAN-001
```

**Response**:
```json
{
  "planCode": "PLAN-001",
  "planName": "Implant + Niềng răng",
  "approvalStatus": "APPROVED",     // ✅ Vẫn là APPROVED
  "status": "IN_PROGRESS",          // 🟢 Đang điều trị
  "phases": [
    {
      "phaseName": "Phase 1: Implant",
      "items": [
        {
          "itemName": "Nhổ răng khôn",
          "status": "SCHEDULED"       // Đã đặt lịch
        },
        {
          "itemName": "Cấy implant",
          "status": "PENDING"         // Chưa đặt lịch
        }
      ]
    }
  ]
}
```

**Frontend hiển thị**:
- ✅ Text: "Đã duyệt" (dưới mã plan)
- 🟢 Badge: "Đang thực hiện" (màu xanh - IN_PROGRESS)
- ✅ Button: "Cập nhật tiến độ" (enabled)
- ❌ Button: "Chỉnh sửa" (disabled - đã approved)

---

### Case 3: Plan hoàn thành

**Request**:
```http
GET /api/v1/patients/BN-1001/treatment-plans/PLAN-001
```

**Response**:
```json
{
  "planCode": "PLAN-001",
  "planName": "Implant + Niềng răng",
  "approvalStatus": "APPROVED",     // ✅ Vẫn là APPROVED
  "status": "COMPLETED",            // ✅ Hoàn thành
  "phases": [
    {
      "phaseName": "Phase 1: Implant",
      "items": [
        {
          "itemName": "Nhổ răng khôn",
          "status": "COMPLETED"       // Hoàn thành
        },
        {
          "itemName": "Cấy implant",
          "status": "COMPLETED"       // Hoàn thành
        }
      ]
    }
  ]
}
```

**Frontend hiển thị**:
- ✅ Text: "Đã duyệt" (dưới mã plan)
- ✅ Badge: "Hoàn thành" (màu lục - COMPLETED)
- ✅ Hiển thị timeline hoàn thành
- ❌ Tất cả buttons disabled (plan đã xong)

---

## ❓ Câu Hỏi Thường Gặp

### Q1: Sau khi approve, approvalStatus có đổi không?

**A**: **KHÔNG**. Sau khi approve, `approvalStatus` sẽ mãi mãi là `APPROVED`.

```
approvalStatus Timeline:
DRAFT → PENDING_REVIEW → APPROVED (dừng lại ở đây)
                                  ↓
                          (không đổi nữa)
```

---

### Q2: Sau khi approve, status có đổi không?

**A**: **CÓ**. `status` sẽ đổi theo tiến trình điều trị:

```
status Timeline (sau khi approved):
PENDING → IN_PROGRESS → COMPLETED
   ↓           ↓            ↓
Đã duyệt   Đang điều trị  Hoàn thành
chưa bắt đầu
```

---

### Q3: Khi nào plan chuyển từ PENDING → IN_PROGRESS?

**A**: Có 2 cách:

**Cách 1 (Tự động - Khuyến nghị)**:
- Khi đặt lịch hẹn đầu tiên cho bất kỳ item nào
- Backend tự động detect và chuyển status

**Cách 2 (Thủ công - Nếu có API 5.5)**:
- Gọi API 5.5 Activate Plan
- Frontend chủ động kích hoạt plan

**Khuyến nghị**: Dùng cách 1 (tự động) để tránh nhầm lẫn.

---

### Q4: Có thể edit plan sau khi approved không?

**A**: **KHÔNG**. Sau khi approved:
- ❌ Không thể thêm/xóa/sửa items
- ❌ Không thể thay đổi giá
- ❌ Không thể submit for review lại
- ✅ Chỉ có thể update item status (PENDING → COMPLETED)
- ✅ Chỉ có thể đặt/hủy lịch hẹn

---

### Q5: Nếu muốn sửa plan đã approved thì phải làm sao?

**A**: Có 2 options:

**Option 1: Reject plan** (Khuyến nghị nếu chưa bắt đầu điều trị)
```
1. Manager reject plan (API 5.9)
   → approvalStatus: APPROVED → REJECTED → DRAFT
   → status: vẫn PENDING (nếu chưa bắt đầu)

2. Doctor sửa plan

3. Doctor submit for review lại (API 5.12)

4. Manager approve lại (API 5.9)
```

**Option 2: Tạo plan mới** (Nếu đã bắt đầu điều trị)
```
1. Giữ nguyên plan cũ (history)

2. Tạo plan mới với adjustments

3. Submit → Approve plan mới

4. Bắt đầu điều trị theo plan mới
```

---

## 🎯 Summary: Sau Khi Duyệt

### Điều KHÔNG ĐỔI:
- ✅ `approvalStatus` = `APPROVED` (mãi mãi)
- ✅ Không thể edit plan (đã lock)

### Điều SẼ ĐỔI:
- 🔄 `status`: `PENDING` → `IN_PROGRESS` → `COMPLETED`
- 🔄 `item.status`: `PENDING` → ... → `COMPLETED`
- 🔄 `phase.status`: Auto-update based on items

### Logic Tự Động:
```
Khi đặt lịch đầu tiên
  → status: PENDING → IN_PROGRESS

Khi item hoàn thành
  → item.status: ... → COMPLETED
  → item tiếp theo: PENDING → READY_FOR_BOOKING

Khi tất cả items trong phase done
  → phase.status: ... → COMPLETED

Khi tất cả phases done
  → status: IN_PROGRESS → COMPLETED
```

---

## 📋 Checklist Cho Frontend Team

### Sau khi plan được approve, Frontend cần:

- [ ] Hiển thị text "Đã duyệt" (approvalStatus: APPROVED)
- [ ] Hiển thị badge "Chờ thực hiện" (status: PENDING) - màu xám/cam
- [ ] Disable các button edit/delete items
- [ ] Enable button "Đặt lịch hẹn"
- [ ] Khi đặt lịch đầu tiên:
  - [ ] Gọi API tạo appointment
  - [ ] Refresh plan detail (API 5.2)
  - [ ] Expect: status: PENDING → IN_PROGRESS
  - [ ] Update badge thành "Đang thực hiện" - màu xanh
- [ ] Hiển thị progress bar dựa trên item status
- [ ] Update UI khi items hoàn thành (COMPLETED)
- [ ] Khi tất cả items done:
  - [ ] Expect: status: IN_PROGRESS → COMPLETED
  - [ ] Hiển thị badge "Hoàn thành" - màu lục
  - [ ] Show completion summary/timeline

---

## 🔗 Related APIs

### APIs Liên Quan Đến Status Changes:

| API | Endpoint | Status Changes |
|-----|----------|----------------|
| **API 5.2** | `GET /patients/{code}/treatment-plans/{planCode}` | Read current status |
| **API 5.9** | `PATCH /patient-treatment-plans/{planCode}/approval` | `approvalStatus` changes |
| **API 5.12** | `PATCH /patient-treatment-plans/{planCode}/submit-for-review` | `DRAFT → PENDING_REVIEW` |
| **API 5.5** | `PATCH /patient-treatment-plans/{planCode}/activate` | `status: PENDING → IN_PROGRESS` (if exists) |
| **API 5.6** | `PATCH /patient-plan-items/{itemId}/status` | `item.status` changes (triggers auto-updates) |
| **Appointment API** | `POST /appointments` | Triggers `status: PENDING → IN_PROGRESS` (first appointment) |

---

## 🚨 Important Notes

### 1. Về API 5.5 Activate Plan

⚠️ **CẦN XÁC NHẬN VỚI BACKEND**:
- Có API này không?
- Hay plan tự động activate khi đặt lịch đầu tiên?

**Khuyến nghị**: Dùng auto-activation (khi đặt lịch đầu tiên) để đơn giản hóa workflow.

### 2. Về Rejection Behavior

⚠️ **CẦN XÁC NHẬN VỚI BACKEND**:

Khi Manager reject plan (API 5.9 with status=REJECTED):
- Response 5.9 trả về `approvalStatus: REJECTED` hay `DRAFT`?
- Response 5.2 (sau khi reject) trả về `approvalStatus: REJECTED` hay `DRAFT`?

**Frontend hiện tại** đang hiển thị "ĐÃ TỪ CHỐI" ngay khi API 5.9 trả về REJECTED.

**Suggestion**: Backend nên tự động convert `REJECTED → DRAFT` trong response để doctor có thể sửa ngay.

### 3. Về Plan Cancellation

⚠️ **CẦN XÁC NHẬN VỚI BACKEND**:
- Có API để cancel plan (chuyển status → CANCELLED) không?
- Use case: Bệnh nhân không tiếp tục điều trị
- Có thể cancel plan đang IN_PROGRESS không?

---

## 📞 Questions for Backend Team

### Critical Questions:

1. **Auto-activation**:
   - Plan có tự động chuyển `PENDING → IN_PROGRESS` khi đặt lịch đầu tiên không?
   - Hay cần gọi API 5.5 Activate Plan riêng?

2. **Rejection behavior**:
   - API 5.9 response với status=REJECTED trả về `approvalStatus: REJECTED` hay `DRAFT`?
   - API 5.2 (sau khi reject) trả về `approvalStatus: REJECTED` hay `DRAFT`?

3. **Plan cancellation**:
   - Có API để cancel plan không?
   - Syntax: `PATCH /patient-treatment-plans/{planCode}/cancel`?

4. **Activation API (API 5.5)**:
   - API này có tồn tại không?
   - Endpoint: `PATCH /patient-treatment-plans/{planCode}/activate`?
   - Khi nào cần dùng?

---

**Last Updated**: 2025-11-18  
**Status**: ✅ Complete - Waiting for Backend Confirmation  
**Next Steps**: Backend team xác nhận các questions above
