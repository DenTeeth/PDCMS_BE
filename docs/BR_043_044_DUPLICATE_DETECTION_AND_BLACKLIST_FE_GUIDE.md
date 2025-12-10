# BR-043 & BR-044: Duplicate Detection & Patient Blacklist - Frontend Integration Guide

**Backend Version**: V34  
**Date**: January 15, 2025  
**Features**: Duplicate Patient Detection + Patient Blacklist Management

---

## Table of Contents
- [Overview](#overview)
- [BR-043: Duplicate Patient Detection](#br-043-duplicate-patient-detection)
- [BR-044: Patient Blacklist Management](#br-044-patient-blacklist-management)
- [UI Integration Examples](#ui-integration-examples)
- [Error Handling](#error-handling)

---

## Overview

### BR-043: Duplicate Patient Detection
**Problem**: Staff accidentally create duplicate patient records with slightly different spellings or phone numbers.  
**Solution**: System automatically checks for duplicates by Name+DOB or Phone when creating new patients.

### BR-044: Patient Blacklist with Mandatory Reasons
**Problem**: Need to blacklist problematic patients (abuse, debt) but require accountability.  
**Solution**: Manager/Admin can blacklist patients, but MUST select a predefined reason (no free-text).

---

## BR-043: Duplicate Patient Detection

### API Endpoint: Check for Duplicates

**Endpoint**: `GET /api/v1/patients/check-duplicate`

**Authorization**: JWT token with roles: `ADMIN`, `RECEPTIONIST`, or `MANAGER`

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `firstName` | string | Yes | Patient's first name |
| `lastName` | string | Yes | Patient's last name |
| `dateOfBirth` | date | Yes | Format: `YYYY-MM-DD` |
| `phone` | string | No | Phone number (optional) |

**Example Request**:
```http
GET /api/v1/patients/check-duplicate?firstName=Nguyen&lastName=Van%20A&dateOfBirth=1990-01-01&phone=0123456789
Authorization: Bearer <jwt_token>
```

**Success Response** (200 OK):

**Case 1: No Duplicates Found**
```json
{
  "hasDuplicates": false,
  "matches": [],
  "message": "Không tìm thấy bệnh nhân trùng."
}
```

**Case 2: Potential Duplicates Found**
```json
{
  "hasDuplicates": true,
  "matches": [
    {
      "patientId": 123,
      "patientCode": "P-000123",
      "fullName": "Nguyen Van A",
      "dateOfBirth": "1990-01-01",
      "phone": "0123456789",
      "email": "nguyenvana@example.com",
      "matchType": "EXACT_MATCH",
      "confidenceScore": 95
    },
    {
      "patientId": 456,
      "patientCode": "P-000456",
      "fullName": "Nguyen Van A",
      "dateOfBirth": "1990-01-01",
      "phone": "0987654321",
      "email": "nguyena@example.com",
      "matchType": "NAME_AND_DOB",
      "confidenceScore": 80
    }
  ],
  "message": "Tìm thấy 2 bệnh nhân có thông tin tương tự. Vui lòng kiểm tra trước khi tạo mới."
}
```

**Match Types Explained**:
| Match Type | Confidence Score | Meaning |
|------------|------------------|---------|
| `EXACT_MATCH` | 95% | Name + DOB + Phone all match (likely duplicate) |
| `NAME_AND_DOB` | 80% | Name + DOB match (high chance duplicate) |
| `NAME_AND_PHONE` | 85% | Name + Phone match |
| `PHONE` | 60% | Phone only (could be family member) |

**Error Responses**:
| Status | Error | Description |
|--------|-------|-------------|
| 400 | Bad Request | Missing required parameters |
| 403 | Forbidden | User role not authorized |

---

### Integration Workflow

**When to Call**: Before submitting `POST /api/v1/patients` (create patient)

**Recommended Flow**:
```
1. User fills out patient form
2. User clicks "Tạo bệnh nhân" button
3. Frontend calls GET /check-duplicate
4. If hasDuplicates === true:
   ├─ Show modal with duplicate matches
   ├─ Display confidence scores and match types
   ├─ Buttons: "Sử dụng bệnh nhân có sẵn" or "Vẫn tạo mới"
   └─ If user confirms "Tạo mới" → Proceed to POST /patients
5. If hasDuplicates === false:
   └─ Directly proceed to POST /patients
```

**Example React Code**:
```tsx
const handleCreatePatient = async (formData: PatientFormData) => {
  // Step 1: Check for duplicates
  const duplicateCheck = await api.get('/api/v1/patients/check-duplicate', {
    params: {
      firstName: formData.firstName,
      lastName: formData.lastName,
      dateOfBirth: formData.dateOfBirth,
      phone: formData.phone
    }
  });

  // Step 2: Handle duplicates
  if (duplicateCheck.data.hasDuplicates) {
    const userConfirmed = await showDuplicateModal(duplicateCheck.data.matches);
    
    if (!userConfirmed) {
      return; // User canceled
    }
  }

  // Step 3: Create patient
  const response = await api.post('/api/v1/patients', formData);
  message.success('Tạo bệnh nhân thành công!');
};
```

---

### Duplicate Modal UI Example

**Components to Display**:
1. **Title**: "⚠️ Phát hiện bệnh nhân trùng khớp"
2. **Message**: `duplicateCheck.data.message`
3. **Table of Matches**:

| Mã BN | Họ Tên | Ngày Sinh | SĐT | Độ Trùng Khớp | Loại Trùng |
|-------|--------|-----------|-----|---------------|------------|
| P-000123 | Nguyen Van A | 01/01/1990 | 0123456789 | 95% | Trùng khớp hoàn toàn |
| P-000456 | Nguyen Van A | 01/01/1990 | 0987654321 | 80% | Trùng Tên + Ngày sinh |

4. **Action Buttons**:
   - **Primary**: "Sử dụng bệnh nhân có sẵn" (redirects to patient detail)
   - **Secondary**: "Vẫn tạo mới" (proceeds with creation)
   - **Tertiary**: "Hủy"

**Ant Design Example**:
```tsx
import { Modal, Table, Tag } from 'antd';

const DuplicateModal = ({ visible, matches, onUseExisting, onCreate, onCancel }) => {
  const columns = [
    {
      title: 'Mã BN',
      dataIndex: 'patientCode',
      key: 'patientCode',
    },
    {
      title: 'Họ Tên',
      dataIndex: 'fullName',
      key: 'fullName',
    },
    {
      title: 'Ngày Sinh',
      dataIndex: 'dateOfBirth',
      key: 'dateOfBirth',
      render: (date) => dayjs(date).format('DD/MM/YYYY'),
    },
    {
      title: 'SĐT',
      dataIndex: 'phone',
      key: 'phone',
    },
    {
      title: 'Độ Trùng Khớp',
      dataIndex: 'confidenceScore',
      key: 'confidenceScore',
      render: (score) => (
        <Tag color={score >= 90 ? 'red' : score >= 75 ? 'orange' : 'blue'}>
          {score}%
        </Tag>
      ),
    },
    {
      title: 'Loại Trùng',
      dataIndex: 'matchType',
      key: 'matchType',
      render: (type) => {
        const labels = {
          EXACT_MATCH: 'Trùng khớp hoàn toàn',
          NAME_AND_DOB: 'Trùng Tên + Ngày sinh',
          NAME_AND_PHONE: 'Trùng Tên + SĐT',
          PHONE: 'Trùng SĐT'
        };
        return labels[type] || type;
      }
    },
    {
      title: 'Hành động',
      key: 'action',
      render: (_, record) => (
        <Button size="small" onClick={() => onUseExisting(record.patientId)}>
          Sử dụng
        </Button>
      ),
    }
  ];

  return (
    <Modal
      title="⚠️ Phát hiện bệnh nhân trùng khớp"
      open={visible}
      onCancel={onCancel}
      width={900}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          Hủy
        </Button>,
        <Button key="create" type="primary" onClick={onCreate}>
          Vẫn tạo mới
        </Button>,
      ]}
    >
      <p className="mb-4">
        Hệ thống phát hiện {matches.length} bệnh nhân có thông tin tương tự. 
        Vui lòng kiểm tra trước khi tạo mới để tránh trùng lặp.
      </p>
      <Table 
        dataSource={matches} 
        columns={columns} 
        rowKey="patientId"
        pagination={false}
      />
    </Modal>
  );
};
```

---

## BR-044: Patient Blacklist Management

### 1. Blacklist Reasons Enum

**Predefined Reasons** (8 options):

| Enum Value | Display Name (Vietnamese) | Use Case |
|------------|---------------------------|----------|
| `STAFF_ABUSE` | Xúc phạm nhân viên | Verbal/physical abuse towards staff |
| `DEBT_DEFAULT` | Bùng nợ | Unpaid bills, refuses to pay |
| `FRIVOLOUS_LAWSUIT` | Doạ kiện không có cơ sở | Threatens baseless legal action |
| `PROPERTY_DAMAGE` | Phá hoại tài sản phòng khám | Damaged clinic property |
| `INTOXICATION` | Vi phạm quy định (say rượu/ma túy) | Showed up intoxicated |
| `DISRUPTIVE_BEHAVIOR` | Gây rối trật tự liên tục | Repeatedly creates disturbances |
| `POLICY_VIOLATION` | Vi phạm nội quy nhiều lần | Multiple policy violations |
| `OTHER_SERIOUS` | Lý do nghiêm trọng khác | Other serious reason (Manager documents) |

---

### 2. API: Add Patient to Blacklist

**Endpoint**: `POST /api/v1/patients/{id}/blacklist`

**Authorization**: JWT token with roles: `MANAGER` or `ADMIN` **ONLY**

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | Patient ID to blacklist |

**Request Body**:
```json
{
  "reason": "STAFF_ABUSE",
  "notes": "Optional additional context or incident details"
}
```

**Field Validation**:
- `reason`: **Required**, must be one of the 8 enum values
- `notes`: Optional, max 1000 characters

**Success Response** (200 OK):
```json
{
  "message": "Đã thêm bệnh nhân vào blacklist thành công",
  "patientId": 123,
  "patientName": "Nguyen Van A",
  "blacklistReason": "STAFF_ABUSE",
  "blacklistReasonDisplay": "Xúc phạm nhân viên",
  "notes": "Patient verbally abused receptionist on 2025-01-15",
  "blacklistedBy": "manager01",
  "blacklistedAt": "2025-01-15T14:30:00"
}
```

**Error Responses**:

| Status | Title | Detail | When |
|--------|-------|--------|------|
| 400 | Blacklist Reason Required | Lý do blacklist bắt buộc phải chọn từ danh sách định sẵn. | `reason` is null |
| 400 | Patient Already Blacklisted | Bệnh nhân này đã bị blacklist rồi. | Patient already on blacklist |
| 403 | Forbidden | Access denied | User is not MANAGER/ADMIN |
| 404 | Patient Not Found | Không tìm thấy bệnh nhân với ID: {id} | Invalid patient ID |

**Example Request** (Axios):
```typescript
const blacklistPatient = async (patientId: number, reason: string, notes?: string) => {
  try {
    const response = await api.post(`/api/v1/patients/${patientId}/blacklist`, {
      reason: reason, // e.g., "STAFF_ABUSE"
      notes: notes    // Optional
    });
    
    message.success(response.data.message);
    return response.data;
  } catch (error: any) {
    if (error.response?.status === 403) {
      message.error('Chỉ Manager/Admin mới có quyền blacklist bệnh nhân');
    } else if (error.response?.data?.detail) {
      message.error(error.response.data.detail);
    } else {
      message.error('Có lỗi xảy ra khi thêm blacklist');
    }
    throw error;
  }
};
```

---

### 3. API: Remove Patient from Blacklist

**Endpoint**: `DELETE /api/v1/patients/{id}/blacklist`

**Authorization**: JWT token with roles: `MANAGER` or `ADMIN` **ONLY**

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | Patient ID to remove from blacklist |

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `reason` | string | No | Reason for removal (audit trail) |

**Example Request**:
```http
DELETE /api/v1/patients/123/blacklist?reason=Issue resolved, patient apologized
Authorization: Bearer <jwt_token>
```

**Success Response** (200 OK):
```json
{
  "message": "Đã xóa bệnh nhân khỏi blacklist",
  "patientId": 123,
  "patientName": "Nguyen Van A",
  "blacklistReason": "STAFF_ABUSE",
  "blacklistReasonDisplay": "Xúc phạm nhân viên",
  "notes": "Issue resolved, patient apologized",
  "blacklistedBy": "manager01",
  "blacklistedAt": "2025-01-15T15:45:00"
}
```

**Error Responses**:

| Status | Title | Detail | When |
|--------|-------|--------|------|
| 400 | Patient Not Blacklisted | Bệnh nhân này không có trong blacklist. | Patient not blacklisted |
| 403 | Forbidden | Access denied | User is not MANAGER/ADMIN |
| 404 | Patient Not Found | Không tìm thấy bệnh nhân với ID: {id} | Invalid patient ID |

**Example Request** (Axios):
```typescript
const removeFromBlacklist = async (patientId: number, reason?: string) => {
  try {
    const response = await api.delete(`/api/v1/patients/${patientId}/blacklist`, {
      params: { reason }
    });
    
    message.success(response.data.message);
    return response.data;
  } catch (error: any) {
    if (error.response?.data?.detail) {
      message.error(error.response.data.detail);
    } else {
      message.error('Có lỗi xảy ra khi xóa blacklist');
    }
    throw error;
  }
};
```

---

## UI Integration Examples

### 1. Patient Detail Page - Blacklist Badge

**Display Location**: Next to patient name or in patient card header

**Example**:
```tsx
const PatientStatusBadge = ({ patient }: { patient: Patient }) => {
  if (patient.isBlacklisted) {
    return (
      <Tooltip title={`Lý do: ${patient.blacklistReasonDisplay || patient.blacklistReason}`}>
        <Tag color="red" icon={<WarningOutlined />}>
          BLACKLIST
        </Tag>
      </Tooltip>
    );
  }
  
  if (patient.isBookingBlocked) {
    return (
      <Tag color="orange" icon={<StopOutlined />}>
        Bị chặn đặt lịch ({patient.consecutiveNoShows} lần no-show)
      </Tag>
    );
  }
  
  return (
    <Tag color="green" icon={<CheckCircleOutlined />}>
      Hoạt động
    </Tag>
  );
};
```

---

### 2. Blacklist Action Button (Manager Only)

**Display Location**: Patient detail page, action menu

**Permission Check**:
```tsx
const canBlacklist = (user: User) => {
  return ['MANAGER', 'ADMIN'].includes(user.role);
};

// In component:
{canBlacklist(currentUser) && !patient.isBlacklisted && (
  <Button 
    danger 
    icon={<StopOutlined />}
    onClick={() => setBlacklistModalVisible(true)}
  >
    Thêm vào Blacklist
  </Button>
)}

{canBlacklist(currentUser) && patient.isBlacklisted && (
  <Button 
    type="primary"
    onClick={() => handleRemoveFromBlacklist(patient.patientId)}
  >
    Xóa khỏi Blacklist
  </Button>
)}
```

---

### 3. Blacklist Modal with Reason Selection

**Components**:
```tsx
import { Modal, Form, Select, Input, message } from 'antd';

const BlacklistModal = ({ 
  visible, 
  patient, 
  onClose, 
  onSuccess 
}: BlacklistModalProps) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const blacklistReasons = [
    { value: 'STAFF_ABUSE', label: 'Xúc phạm nhân viên' },
    { value: 'DEBT_DEFAULT', label: 'Bùng nợ' },
    { value: 'FRIVOLOUS_LAWSUIT', label: 'Doạ kiện không có cơ sở' },
    { value: 'PROPERTY_DAMAGE', label: 'Phá hoại tài sản phòng khám' },
    { value: 'INTOXICATION', label: 'Vi phạm quy định (say rượu/ma túy)' },
    { value: 'DISRUPTIVE_BEHAVIOR', label: 'Gây rối trật tự liên tục' },
    { value: 'POLICY_VIOLATION', label: 'Vi phạm nội quy nhiều lần' },
    { value: 'OTHER_SERIOUS', label: 'Lý do nghiêm trọng khác' },
  ];

  const handleSubmit = async (values: { reason: string; notes?: string }) => {
    setLoading(true);
    try {
      const response = await api.post(
        `/api/v1/patients/${patient.patientId}/blacklist`,
        values
      );
      
      message.success(response.data.message);
      onSuccess(response.data);
      onClose();
      form.resetFields();
    } catch (error: any) {
      if (error.response?.data?.detail) {
        message.error(error.response.data.detail);
      } else {
        message.error('Có lỗi xảy ra khi thêm blacklist');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title="⚠️ Thêm bệnh nhân vào Blacklist"
      open={visible}
      onCancel={onClose}
      onOk={() => form.submit()}
      confirmLoading={loading}
      okText="Xác nhận"
      cancelText="Hủy"
      okButtonProps={{ danger: true }}
    >
      <div className="mb-4">
        <p><strong>Bệnh nhân:</strong> {patient.fullName}</p>
        <p><strong>Mã BN:</strong> {patient.patientCode}</p>
        <p className="text-red-600 mt-2">
          ⚠️ Hành động này sẽ chặn bệnh nhân khỏi việc đặt lịch hẹn.
        </p>
      </div>

      <Form form={form} onFinish={handleSubmit} layout="vertical">
        <Form.Item
          name="reason"
          label="Lý do *"
          rules={[{ required: true, message: 'Vui lòng chọn lý do' }]}
        >
          <Select
            placeholder="Chọn lý do blacklist"
            options={blacklistReasons}
            showSearch
            optionFilterProp="label"
          />
        </Form.Item>

        <Form.Item
          name="notes"
          label="Ghi chú (Tùy chọn)"
          rules={[{ max: 1000, message: 'Ghi chú không được vượt quá 1000 ký tự' }]}
        >
          <Input.TextArea
            rows={4}
            placeholder="Nhập thông tin chi tiết về sự việc (VD: Ngày xảy ra, người chứng kiến...)"
            showCount
            maxLength={1000}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};
```

---

### 4. Blacklist History Viewer (Optional)

**Display Location**: Patient detail page, "Lịch sử Blacklist" tab

**Data to Show**:
- When patient was blacklisted (`blacklistedAt`)
- Who blacklisted (`blacklistedBy`)
- Reason (`blacklistReasonDisplay`)
- Notes (`blacklistNotes`)
- When removed (if applicable)

**Example**:
```tsx
const BlacklistHistory = ({ patient }: { patient: Patient }) => {
  if (!patient.isBlacklisted && !patient.blacklistedAt) {
    return <Empty description="Bệnh nhân chưa từng bị blacklist" />;
  }

  return (
    <Descriptions bordered column={1}>
      <Descriptions.Item label="Trạng thái">
        {patient.isBlacklisted ? (
          <Tag color="red">Đang bị blacklist</Tag>
        ) : (
          <Tag color="green">Đã được xóa khỏi blacklist</Tag>
        )}
      </Descriptions.Item>
      
      {patient.isBlacklisted && (
        <>
          <Descriptions.Item label="Lý do">
            {patient.blacklistReasonDisplay || patient.blacklistReason}
          </Descriptions.Item>
          
          <Descriptions.Item label="Ghi chú">
            {patient.blacklistNotes || '-'}
          </Descriptions.Item>
          
          <Descriptions.Item label="Thực hiện bởi">
            {patient.blacklistedBy}
          </Descriptions.Item>
          
          <Descriptions.Item label="Thời gian">
            {dayjs(patient.blacklistedAt).format('DD/MM/YYYY HH:mm')}
          </Descriptions.Item>
        </>
      )}
    </Descriptions>
  );
};
```

---

### 5. Patient List - Blacklist Filter

**Filter Option**: Add "Blacklist Status" filter to patient list

**Example**:
```tsx
const PatientListFilters = () => {
  return (
    <Form layout="inline">
      {/* ... other filters ... */}
      
      <Form.Item name="blacklistStatus" label="Trạng thái Blacklist">
        <Select placeholder="Tất cả" style={{ width: 200 }}>
          <Select.Option value="">Tất cả</Select.Option>
          <Select.Option value="blacklisted">
            <Tag color="red">Đang blacklist</Tag>
          </Select.Option>
          <Select.Option value="not_blacklisted">
            <Tag color="green">Không blacklist</Tag>
          </Select.Option>
        </Select>
      </Form.Item>
    </Form>
  );
};
```

---

## Error Handling

### Standard Error Response Format

All errors follow Spring Framework's `ProblemDetail` (RFC 7807):

```json
{
  "type": "about:blank",
  "title": "Patient Not Found",
  "status": 404,
  "detail": "Không tìm thấy bệnh nhân với ID: 123",
  "instance": "/api/v1/patients/123/blacklist"
}
```

### Global Error Handler Example

```typescript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.data?.detail) {
      // Display backend error message
      message.error(error.response.data.detail);
    } else if (error.response?.status === 403) {
      message.error('Bạn không có quyền thực hiện hành động này');
    } else if (error.response?.status === 401) {
      message.error('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.');
      // Redirect to login
    } else {
      message.error('Có lỗi xảy ra. Vui lòng thử lại sau.');
    }
    return Promise.reject(error);
  }
);
```

---

## Testing Checklist

### BR-043 (Duplicate Detection)
- [ ] Check duplicate by Name+DOB → Shows modal with matches
- [ ] Check duplicate by Phone → Shows modal with matches
- [ ] No duplicates → Proceeds directly to create patient
- [ ] User clicks "Sử dụng bệnh nhân có sẵn" → Redirects to existing patient detail
- [ ] User clicks "Vẫn tạo mới" → Creates new patient despite warning
- [ ] Confidence score displays correctly (60-95%)
- [ ] Match types show correct Vietnamese labels

### BR-044 (Blacklist)
- [ ] Manager can see "Thêm vào Blacklist" button
- [ ] Receptionist CANNOT see blacklist button (403 error if called)
- [ ] Modal shows all 8 predefined reasons
- [ ] Cannot submit without selecting a reason
- [ ] Notes field is optional
- [ ] Success message shows after blacklisting
- [ ] Patient badge updates to show "BLACKLIST" tag
- [ ] Blacklisted patient cannot book appointments (blocked)
- [ ] Manager can remove patient from blacklist
- [ ] Removing from blacklist requires reason (audit trail)

---

## Permission Matrix

| Feature | ADMIN | MANAGER | RECEPTIONIST | DENTIST | PATIENT |
|---------|-------|---------|--------------|---------|---------|
| Check Duplicate | ✅ | ✅ | ✅ | ❌ | ❌ |
| Add to Blacklist | ✅ | ✅ | ❌ | ❌ | ❌ |
| Remove from Blacklist | ✅ | ✅ | ❌ | ❌ | ❌ |
| View Blacklist Status | ✅ | ✅ | ✅ | ✅ | ❌ |

---

## Summary

### New Endpoints Added

| Method | Endpoint | Role | Purpose |
|--------|----------|------|---------|
| GET | `/api/v1/patients/check-duplicate` | ADMIN, MANAGER, RECEPTIONIST | Check for duplicate patients |
| POST | `/api/v1/patients/{id}/blacklist` | ADMIN, MANAGER | Add patient to blacklist |
| DELETE | `/api/v1/patients/{id}/blacklist` | ADMIN, MANAGER | Remove from blacklist |

### Key UI Components to Implement

1. **Duplicate Detection Modal** - Shows when creating new patient with similar data
2. **Blacklist Action Button** - Manager-only button on patient detail page
3. **Blacklist Reason Selector** - Dropdown with 8 predefined reasons
4. **Blacklist Badge** - Red badge on patient card/list when blacklisted
5. **Blacklist History** - Show when/who/why patient was blacklisted

### Important Notes

- ⚠️ **Blacklist also blocks booking**: Blacklisting automatically sets `isBookingBlocked = true`
- ⚠️ **Manager/Admin only**: Receptionist CANNOT blacklist patients (prevents abuse)
- ⚠️ **Mandatory reason**: Cannot blacklist without selecting from 8 predefined reasons
- ⚠️ **Duplicate check is advisory**: System warns but doesn't force-block creation (staff decides)
- ⚠️ **Audit trail**: All blacklist actions logged with username and timestamp

---

**Questions or Issues?**  
Contact backend team or refer to:
- `PatientBlacklistService.java` - Business logic
- `DuplicatePatientDetectionService.java` - Duplicate detection logic
- `schema.sql` V34 - Database schema

---

**End of Document**
