# Patient Unban Feature - Frontend Integration Summary

**Feature**: Patient Unban (BR-085/BR-086)  
**Backend Version**: V33  
**API Base**: `/api/v1/patients`

---

## Quick Start

### 1. Unban Patient API

**Endpoint**: `POST /api/v1/patients/{patientId}/unban`

**Authorization**: JWT token with roles: `RECEPTIONIST`, `MANAGER`, or `ADMIN`

**Request**:
```json
{
  "reason": "Khách trình bày lý do ốm, cam kết không tái phạm"
}
```

**Validation Rules**:
- `reason` is required (non-empty)
- Minimum length: **10 characters**
- Maximum length: **500 characters**

**Success Response** (200 OK):
```json
{
  "message": "Mở khóa bệnh nhân thành công",
  "patientId": 123,
  "patientName": "Nguyễn Văn A",
  "previousNoShowCount": 3,
  "newNoShowCount": 0,
  "unbanBy": "receptionist01",
  "unbanByRole": "RECEPTIONIST",
  "unbanAt": "2025-01-15T14:30:00"
}
```

**Error Responses**:

| Status | Title | Detail |
|--------|-------|--------|
| 400 | Reason Required | Lễ tân bắt buộc phải nhập lý do mở khóa |
| 400 | Reason Too Short | Lý do mở khóa phải có ít nhất 10 ký tự |
| 400 | Reason Too Long | Lý do mở khóa không được vượt quá 500 ký tự |
| 400 | Patient Not Blocked | Bệnh nhân này chưa bị chặn đặt lịch. Không cần mở khóa. |
| 403 | Forbidden | Access denied (user role not authorized) |
| 404 | Patient Not Found | Không tìm thấy bệnh nhân với ID: {id} |

---

### 2. Get Unban History API

**Endpoint**: `GET /api/v1/patients/{patientId}/unban-history`

**Authorization**: JWT token with roles: `RECEPTIONIST`, `MANAGER`, or `ADMIN`

**Success Response** (200 OK):
```json
[
  {
    "auditId": 2,
    "patientId": 123,
    "patientName": "Nguyễn Văn A",
    "previousNoShowCount": 5,
    "performedBy": "manager01",
    "performedByRole": "MANAGER",
    "reason": "Khách xin lỗi vì tình huống khẩn cấp",
    "timestamp": "2025-01-15T10:00:00"
  },
  {
    "auditId": 1,
    "patientId": 123,
    "patientName": "Nguyễn Văn A",
    "previousNoShowCount": 3,
    "performedBy": "receptionist01",
    "performedByRole": "RECEPTIONIST",
    "reason": "Khách trình bày lý do ốm, cam kết không tái phạm",
    "timestamp": "2025-01-10T14:30:00"
  }
]
```

**Note**: Results are ordered by `timestamp` descending (newest first).

---

## UI Implementation Guide

### 1. Patient Status Badge

**Show When**: Patient is blocked (`isBookingBlocked === true`)

**Example** (React + Tailwind):
```tsx
const PatientStatusBadge = ({ patient }: { patient: Patient }) => {
  if (patient.isBookingBlocked) {
    return (
      <span className="px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
        🚫 Bị chặn ({patient.consecutiveNoShows} lần no-show)
      </span>
    );
  }
  return (
    <span className="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
      ✓ Hoạt động
    </span>
  );
};
```

---

### 2. Unban Modal/Dialog

**Trigger**: Click "Mở khóa bệnh nhân" button on patient detail page

**Form Fields**:
- **Label**: "Lý do mở khóa *"
- **Type**: `<textarea>`
- **Validation**: 
  - Required
  - Min length: 10 chars (show live counter: "10/500")
  - Max length: 500 chars
- **Placeholder**: "VD: Khách trình bày lý do ốm, cam kết không tái phạm..."

**Example** (React + Ant Design):
```tsx
import { Modal, Form, Input, message } from 'antd';
import { useState } from 'react';

const UnbanPatientModal = ({ 
  patient, 
  visible, 
  onClose, 
  onSuccess 
}: UnbanModalProps) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values: { reason: string }) => {
    setLoading(true);
    try {
      const response = await api.post(
        `/api/v1/patients/${patient.patientId}/unban`,
        { reason: values.reason.trim() }
      );
      
      message.success(response.data.message);
      onSuccess(response.data);
      onClose();
    } catch (error: any) {
      if (error.response?.data?.detail) {
        message.error(error.response.data.detail);
      } else {
        message.error('Có lỗi xảy ra khi mở khóa bệnh nhân');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title="Mở khóa bệnh nhân"
      open={visible}
      onCancel={onClose}
      onOk={() => form.submit()}
      confirmLoading={loading}
      okText="Xác nhận mở khóa"
      cancelText="Hủy"
    >
      <div className="mb-4">
        <p><strong>Bệnh nhân:</strong> {patient.fullName}</p>
        <p><strong>Số lần no-show:</strong> {patient.consecutiveNoShows}</p>
      </div>

      <Form form={form} onFinish={handleSubmit} layout="vertical">
        <Form.Item
          name="reason"
          label="Lý do mở khóa"
          rules={[
            { required: true, message: 'Vui lòng nhập lý do' },
            { min: 10, message: 'Lý do phải có ít nhất 10 ký tự' },
            { max: 500, message: 'Lý do không được vượt quá 500 ký tự' }
          ]}
        >
          <Input.TextArea
            rows={4}
            placeholder="VD: Khách trình bày lý do ốm, cam kết không tái phạm..."
            showCount
            maxLength={500}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};
```

---

### 3. Unban History Table

**Location**: Patient detail page, "Lịch sử mở khóa" tab

**Columns**:
1. **Thời gian**: `timestamp` (format: `DD/MM/YYYY HH:mm`)
2. **Người thực hiện**: `performedBy`
3. **Vai trò**: `performedByRole`
4. **Số lần no-show trước đó**: `previousNoShowCount`
5. **Lý do**: `reason`

**Example** (React + Ant Design Table):
```tsx
import { Table } from 'antd';
import { useQuery } from '@tanstack/react-query';
import dayjs from 'dayjs';

const UnbanHistoryTable = ({ patientId }: { patientId: number }) => {
  const { data, isLoading } = useQuery({
    queryKey: ['unban-history', patientId],
    queryFn: () => api.get(`/api/v1/patients/${patientId}/unban-history`)
      .then(res => res.data)
  });

  const columns = [
    {
      title: 'Thời gian',
      dataIndex: 'timestamp',
      key: 'timestamp',
      render: (timestamp: string) => dayjs(timestamp).format('DD/MM/YYYY HH:mm'),
      sorter: (a: any, b: any) => dayjs(a.timestamp).unix() - dayjs(b.timestamp).unix(),
    },
    {
      title: 'Người thực hiện',
      dataIndex: 'performedBy',
      key: 'performedBy',
    },
    {
      title: 'Vai trò',
      dataIndex: 'performedByRole',
      key: 'performedByRole',
      render: (role: string) => {
        const colors: Record<string, string> = {
          RECEPTIONIST: 'blue',
          MANAGER: 'green',
          ADMIN: 'red'
        };
        return <Tag color={colors[role] || 'default'}>{role}</Tag>;
      }
    },
    {
      title: 'No-show trước đó',
      dataIndex: 'previousNoShowCount',
      key: 'previousNoShowCount',
      align: 'center' as const,
    },
    {
      title: 'Lý do',
      dataIndex: 'reason',
      key: 'reason',
      ellipsis: true,
    }
  ];

  return (
    <Table
      columns={columns}
      dataSource={data || []}
      loading={isLoading}
      rowKey="auditId"
      pagination={{ pageSize: 10 }}
    />
  );
};
```

---

## User Flow

### Receptionist Workflow

1. **Patient Detail Page**:
   - See red badge: "🚫 Bị chặn (3 lần no-show)"
   - Click "Mở khóa bệnh nhân" button

2. **Unban Modal**:
   - Enter reason (min 10 chars): "Khách trình bày lý do ốm, cam kết không tái phạm"
   - Click "Xác nhận mở khóa"

3. **Success**:
   - Toast message: "Mở khóa bệnh nhân thành công"
   - Patient badge updates to: "✓ Hoạt động"
   - Patient can now book appointments

4. **Error Handling**:
   - Reason too short: Show inline error "Lý do phải có ít nhất 10 ký tự"
   - Network error: Show toast "Có lỗi xảy ra khi mở khóa bệnh nhân"

---

### Manager Review Workflow

1. **Patient Detail Page**:
   - Click "Lịch sử mở khóa" tab

2. **Unban History Table**:
   - View all unban actions for this patient
   - See who unbanned, when, why, and previous no-show count
   - Sort by timestamp to see recent actions

3. **Audit Analysis**:
   - Look for patterns: Same patient unbanned multiple times?
   - Check if reasons are legitimate (not generic)
   - Discuss with Receptionist if abuse suspected

---

## Permission Handling

**Client-Side Check**:
```tsx
const canUnbanPatient = (user: User) => {
  return ['RECEPTIONIST', 'MANAGER', 'ADMIN'].includes(user.role);
};

// In component:
{canUnbanPatient(currentUser) && patient.isBookingBlocked && (
  <Button onClick={openUnbanModal}>
    Mở khóa bệnh nhân
  </Button>
)}
```

**Note**: Client-side check is for UI only. Backend enforces authorization with `@PreAuthorize`.

---

## Error Handling Patterns

### 1. Display Backend Error Message
```tsx
catch (error: any) {
  const errorMessage = error.response?.data?.detail || 'Có lỗi xảy ra';
  message.error(errorMessage);
}
```

### 2. Client-Side Validation (Pre-Submit)
```tsx
const validateReason = (reason: string) => {
  const trimmed = reason.trim();
  if (trimmed.length < 10) {
    return 'Lý do phải có ít nhất 10 ký tự';
  }
  if (trimmed.length > 500) {
    return 'Lý do không được vượt quá 500 ký tự';
  }
  return null; // Valid
};
```

### 3. Handle 403 Forbidden
```tsx
catch (error: any) {
  if (error.response?.status === 403) {
    message.error('Bạn không có quyền thực hiện hành động này');
    // Optionally redirect to login or home
  }
}
```

---

## API Client Example (Axios)

```typescript
// services/patientApi.ts
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add JWT token to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwtToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const patientApi = {
  unban: (patientId: number, reason: string) =>
    api.post(`/api/v1/patients/${patientId}/unban`, { reason }),

  getUnbanHistory: (patientId: number) =>
    api.get(`/api/v1/patients/${patientId}/unban-history`)
};
```

---

## Testing Checklist

### Unit Tests (Frontend)
- [ ] Validate reason length (10-500 chars)
- [ ] Trim whitespace from reason before submit
- [ ] Show correct error message for each validation rule
- [ ] Display loading state during API call
- [ ] Handle success response (update UI, show toast)
- [ ] Handle error responses (400, 403, 404, 500)

### Integration Tests
- [ ] Receptionist can unban patient
- [ ] Dentist cannot see unban button (permission check)
- [ ] Patient status badge updates after unban
- [ ] Unban history table loads correctly
- [ ] Audit log shows correct timestamp/performer/reason

### Manual Testing Scenarios
1. **Unban blocked patient** → Success
2. **Unban already-active patient** → Error: "Bệnh nhân này chưa bị chặn"
3. **Reason too short (< 10 chars)** → Error: "Lý do phải có ít nhất 10 ký tự"
4. **Reason too long (> 500 chars)** → Error: "Lý do không được vượt quá 500 ký tự"
5. **No JWT token** → Error: 401 Unauthorized
6. **Wrong role (DENTIST)** → Error: 403 Forbidden

---

## Troubleshooting

### Problem: "CORS error when calling API"
**Solution**: Ensure backend CORS config allows your frontend origin:
```yaml
# application.yaml
spring:
  web:
    cors:
      allowed-origins: http://localhost:3000
      allowed-methods: GET,POST,PUT,DELETE
```

### Problem: "401 Unauthorized"
**Solution**: Check JWT token is included in request header:
```
Authorization: Bearer <token>
```

### Problem: "Patient status not updating after unban"
**Solution**: Refetch patient data after successful unban:
```tsx
onSuccess: () => {
  queryClient.invalidateQueries(['patient', patientId]);
}
```

---

## Summary

| Feature | Endpoint | Method | Role |
|---------|----------|--------|------|
| Unban Patient | `/api/v1/patients/{id}/unban` | POST | RECEPTIONIST, MANAGER, ADMIN |
| View History | `/api/v1/patients/{id}/unban-history` | GET | RECEPTIONIST, MANAGER, ADMIN |

**Key Points**:
1. Reason is **mandatory** (10-500 chars)
2. **BR-085**: Receptionist can unban without approval
3. **BR-086**: All actions logged for accountability
4. Use `ProblemDetail` for error handling
5. Show patient status badge with no-show count
6. Provide Manager with audit history table

---

**Contact**: See full documentation in `PATIENT_UNBAN_FEATURE_IMPLEMENTATION.md`
