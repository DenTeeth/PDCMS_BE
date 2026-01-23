# Hướng Dẫn FE: Validation Status Khi Lưu Bệnh Án

## Tổng Quan

Khi lưu bệnh án (Clinical Record), buổi hẹn **BẮT BUỘC** phải có trạng thái `IN_PROGRESS` mới được phép lưu. Nếu trạng thái khác sẽ trả về lỗi.

---

## Quy Trình Cập Nhật Status

```
SCHEDULED → CONFIRMED → CHECKED_IN → IN_PROGRESS → COMPLETED
```

**Lưu ý:** Chỉ khi buổi hẹn ở trạng thái `IN_PROGRESS` mới có thể lưu bệnh án.

---

## Error Response Khi Lưu Bệnh Án

### 1. Buổi hẹn chưa ở trạng thái IN_PROGRESS

**HTTP Status:** `400 Bad Request`

**Response:**

```json
{
  "statusCode": 400,
  "error": "INVALID_APPOINTMENT_STATUS",
  "message": "Buổi hẹn phải ở trạng thái IN_PROGRESS để lưu bệnh án. Trạng thái hiện tại: CHECKED_IN",
  "data": {
    "currentStatus": "CHECKED_IN",
    "requiredStatus": "IN_PROGRESS",
    "appointmentId": 123
  }
}
```

### 2. Không tìm thấy buổi hẹn

**HTTP Status:** `404 Not Found`

**Response:**

```json
{
  "statusCode": 404,
  "error": "APPOINTMENT_NOT_FOUND",
  "message": "Không tìm thấy buổi hẹn với ID: 123",
  "data": null
}
```

### 3. Buổi hẹn đã hoàn thành

**HTTP Status:** `400 Bad Request`

**Response:**

```json
{
  "statusCode": 400,
  "error": "APPOINTMENT_ALREADY_COMPLETED",
  "message": "Buổi hẹn đã hoàn thành, không thể chỉnh sửa bệnh án",
  "data": {
    "appointmentId": 123,
    "status": "COMPLETED"
  }
}
```

---

## Mã Lỗi (Error Codes)

| Error Code                      | HTTP Status | Mô tả                                  | Hướng xử lý FE                                     |
| ------------------------------- | ----------- | -------------------------------------- | -------------------------------------------------- |
| `INVALID_APPOINTMENT_STATUS`    | 400         | Buổi hẹn chưa ở trạng thái IN_PROGRESS | Hiển thị thông báo yêu cầu chuyển trạng thái trước |
| `APPOINTMENT_NOT_FOUND`         | 404         | Không tìm thấy buổi hẹn                | Hiển thị lỗi và quay lại danh sách                 |
| `APPOINTMENT_ALREADY_COMPLETED` | 400         | Buổi hẹn đã hoàn thành                 | Disable form chỉnh sửa                             |
| `CLINICAL_RECORD_EXISTS`        | 409         | Đã có bệnh án cho buổi hẹn này         | Chuyển sang chế độ xem/cập nhật                    |
| `PERMISSION_DENIED`             | 403         | Không có quyền lưu bệnh án             | Hiển thị thông báo không có quyền                  |

---

## Hướng Dẫn Xử Lý FE

### Trước khi mở form lưu bệnh án

```typescript
// 1. Kiểm tra trạng thái buổi hẹn
const canSaveClinicalRecord = appointment.status === "IN_PROGRESS";

// 2. Nếu chưa phải IN_PROGRESS, hiển thị thông báo
if (!canSaveClinicalRecord) {
  showWarning(`Vui lòng chuyển buổi hẹn sang trạng thái "Đang khám" trước khi lưu bệnh án.
               Trạng thái hiện tại: ${getStatusLabel(appointment.status)}`);

  // Có thể hiện button để chuyển status
  showChangeStatusButton(appointment.id);
}
```

### Xử lý lỗi khi submit form

```typescript
try {
  await saveClinicalRecord(data);
  showSuccess("Lưu bệnh án thành công!");
} catch (error) {
  const response = error.response?.data;

  switch (response?.error) {
    case "INVALID_APPOINTMENT_STATUS":
      showError(`Buổi hẹn phải ở trạng thái "Đang khám" để lưu bệnh án.
                 Trạng thái hiện tại: ${getStatusLabel(response.data.currentStatus)}`);

      // Hỏi user có muốn chuyển status không
      if (await confirmChangeStatus()) {
        await updateAppointmentStatus(
          response.data.appointmentId,
          "IN_PROGRESS",
        );
        // Thử lưu lại
        await saveClinicalRecord(data);
      }
      break;

    case "APPOINTMENT_ALREADY_COMPLETED":
      showError("Buổi hẹn đã hoàn thành, không thể chỉnh sửa bệnh án");
      disableForm();
      break;

    case "PERMISSION_DENIED":
      showError("Bạn không có quyền lưu bệnh án");
      break;

    default:
      showError(response?.message || "Có lỗi xảy ra");
  }
}
```

---

## API Chuyển Trạng Thái Buổi Hẹn

### Endpoint

```
PUT /api/v1/appointments/{appointmentId}/status
```

### Request Body

```json
{
  "status": "IN_PROGRESS"
}
```

### Các trạng thái hợp lệ

| Trạng thái  | Giá trị       | Mô tả                    |
| ----------- | ------------- | ------------------------ |
| Đã lên lịch | `SCHEDULED`   | Buổi hẹn mới tạo         |
| Đã xác nhận | `CONFIRMED`   | Bệnh nhân xác nhận đến   |
| Đã check-in | `CHECKED_IN`  | Bệnh nhân đến phòng khám |
| Đang khám   | `IN_PROGRESS` | ⭐ Bác sĩ bắt đầu khám   |
| Hoàn thành  | `COMPLETED`   | Khám xong                |
| Đã hủy      | `CANCELLED`   | Buổi hẹn bị hủy          |
| Không đến   | `NO_SHOW`     | Bệnh nhân vắng mặt       |

---

## Flow Hoàn Chỉnh

```
┌─────────────────────────────────────────────────────────────────┐
│                      FLOW LƯU BỆNH ÁN                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. FE mở form lưu bệnh án                                      │
│     │                                                           │
│     ▼                                                           │
│  2. Kiểm tra appointment.status === 'IN_PROGRESS'?              │
│     │                                                           │
│     ├── NO ──▶ Hiển thị warning + button "Bắt đầu khám"         │
│     │           │                                               │
│     │           ▼                                               │
│     │         User click "Bắt đầu khám"                         │
│     │           │                                               │
│     │           ▼                                               │
│     │         PUT /appointments/{id}/status {status:"IN_PROGRESS"}│
│     │           │                                               │
│     │           ▼                                               │
│     │         Refresh appointment data                          │
│     │           │                                               │
│     ├───────────┘                                               │
│     │                                                           │
│     ▼                                                           │
│  3. YES ──▶ Enable form lưu bệnh án                             │
│     │                                                           │
│     ▼                                                           │
│  4. User điền thông tin và submit                               │
│     │                                                           │
│     ▼                                                           │
│  5. POST /clinical-records                                       │
│     │                                                           │
│     ├── 200 ──▶ Hiển thị success                                │
│     │                                                           │
│     └── 400 ──▶ Xử lý error theo error code                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Ví Dụ Code FE Hoàn Chỉnh

```typescript
// Component: ClinicalRecordForm.tsx

interface ClinicalRecordFormProps {
  appointment: Appointment;
}

const ClinicalRecordForm: React.FC<ClinicalRecordFormProps> = ({ appointment }) => {
  const [isLoading, setIsLoading] = useState(false);

  // Kiểm tra có thể lưu bệnh án không
  const canSave = appointment.status === 'IN_PROGRESS';
  const isCompleted = appointment.status === 'COMPLETED';

  // Hàm chuyển status sang IN_PROGRESS
  const handleStartExamination = async () => {
    try {
      setIsLoading(true);
      await updateAppointmentStatus(appointment.id, 'IN_PROGRESS');
      toast.success('Đã bắt đầu khám');
      // Refresh data
      await refetchAppointment();
    } catch (error) {
      toast.error('Không thể chuyển trạng thái');
    } finally {
      setIsLoading(false);
    }
  };

  // Render warning nếu chưa IN_PROGRESS
  if (!canSave && !isCompleted) {
    return (
      <Alert type="warning">
        <p>Buổi hẹn phải ở trạng thái "Đang khám" để lưu bệnh án.</p>
        <p>Trạng thái hiện tại: <strong>{getStatusLabel(appointment.status)}</strong></p>

        {appointment.status === 'CHECKED_IN' && (
          <Button
            onClick={handleStartExamination}
            loading={isLoading}
          >
            Bắt đầu khám
          </Button>
        )}
      </Alert>
    );
  }

  // Form lưu bệnh án
  return (
    <Form onSubmit={handleSubmit} disabled={isCompleted}>
      {/* Form fields */}
    </Form>
  );
};
```

---

## Liên Hệ

Nếu có thắc mắc, liên hệ Backend team qua:

- Slack: #backend-support
- Email: backend@dentalclinic.com

---

**Cập nhật lần cuối:** 23/01/2026
