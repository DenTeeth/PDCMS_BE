# API 9.1: Create Patient Image

## Overview

Tạo record hình ảnh sau khi Frontend đã upload file lên Cloudinary. Backend chỉ lưu metadata (URL, publicId, type, description) vào database.

## Endpoint

```
POST /api/v1/patient-images
```

## Permission Required

`PATIENT_IMAGE_CREATE` - Assigned to ROLE_DENTIST, ROLE_ADMIN

## Request

### Headers

```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

### Body

```json
{
  "patientId": 1,
  "clinicalRecordId": null,
  "imageUrl": "https://res.cloudinary.com/demo/image/upload/v1/patients/patient_1/xray/image123.jpg",
  "cloudinaryPublicId": "patients/patient_1/xray/image123",
  "imageType": "XRAY",
  "description": "X-quang răng số 6",
  "capturedDate": "2025-12-08"
}
```

### Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| patientId | Long | Yes | ID của bệnh nhân |
| clinicalRecordId | Long | No | ID của clinical record (optional) |
| imageUrl | String | Yes | Full URL từ Cloudinary |
| cloudinaryPublicId | String | Yes | Public ID trên Cloudinary (dùng để xóa) |
| imageType | Enum | Yes | Loại ảnh: XRAY, PHOTO, BEFORE_TREATMENT, AFTER_TREATMENT, SCAN, OTHER |
| description | String | No | Mô tả ảnh |
| capturedDate | Date | No | Ngày chụp ảnh (YYYY-MM-DD) |

### Image Types

- `XRAY` - X-quang
- `PHOTO` - Ảnh chụp thông thường
- `BEFORE_TREATMENT` - Ảnh trước điều trị
- `AFTER_TREATMENT` - Ảnh sau điều trị
- `SCAN` - Scan tài liệu
- `OTHER` - Loại khác

## Response

### Success Response

**Status Code:** `201 Created`

```json
{
  "imageId": 1,
  "patientId": 1,
  "patientName": "Nguyen Van A",
  "clinicalRecordId": null,
  "imageUrl": "https://res.cloudinary.com/demo/image/upload/v1/patients/patient_1/xray/image123.jpg",
  "cloudinaryPublicId": "patients/patient_1/xray/image123",
  "imageType": "XRAY",
  "description": "X-quang răng số 6",
  "capturedDate": "2025-12-08",
  "uploadedBy": 1,
  "uploaderName": "Le Anh Khoa",
  "createdAt": "2025-12-08T10:30:00",
  "updatedAt": "2025-12-08T10:30:00"
}
```

### Error Responses

| Status Code | Error Message | Description |
|-------------|---------------|-------------|
| 400 | Bad Request | Thiếu required fields hoặc dữ liệu không hợp lệ |
| 403 | Forbidden | Không có quyền PATIENT_IMAGE_CREATE |
| 404 | Not Found | Patient không tồn tại |
| 404 | Not Found | Clinical record không tồn tại (nếu có clinicalRecordId) |
| 400 | Bad Request | Clinical record không thuộc về bệnh nhân này |

## Curl Command

```bash
curl -X POST http://localhost:8080/api/v1/patient-images \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "imageUrl": "https://res.cloudinary.com/demo/image/upload/v1/patients/patient_1/xray/test1.jpg",
    "cloudinaryPublicId": "patients/patient_1/xray/test1",
    "imageType": "XRAY",
    "description": "X-quang test",
    "capturedDate": "2025-12-08"
  }'
```

## Frontend Integration Flow

1. **User chọn file** từ device
2. **FE upload file lên Cloudinary** (sử dụng Cloudinary upload widget hoặc API)
3. **Cloudinary trả về response** chứa `secure_url` và `public_id`
4. **FE gọi API này** với data:
   - `imageUrl` = `secure_url` từ Cloudinary
   - `cloudinaryPublicId` = `public_id` từ Cloudinary
   - `patientId`, `imageType`, `description`, `capturedDate`
5. **Backend lưu metadata** vào database
6. **FE hiển thị ảnh** trong gallery

## Cloudinary Folder Structure (Recommend)

```
patients/
  patient_1/
    xray/
      patient_1_1733667890_abc123.jpg
    photo/
      patient_1_1733667900_def456.jpg
    before_treatment/
    after_treatment/
    scan/
    other/
```

**Naming Convention:**
```
patients/patient_{patientId}/{imageType}/patient_{patientId}_{timestamp}_{random}.{ext}
```

## Test Data

**Available Patients (from seed):**
- Patient ID: 1 - Nguyen Van A (BN001)
- Patient ID: 2 - Tran Thi B (BN002)

**Test Credentials:**
- Username: `bacsi1`, Password: `123456` (ROLE_DENTIST)
- Username: `admin`, Password: `123456` (ROLE_ADMIN)

## Business Rules

1. **Patient Validation:** Patient phải tồn tại trong database
2. **Clinical Record Validation:** Nếu có clinicalRecordId, clinical record phải tồn tại và thuộc về patient đó
3. **Uploader Tracking:** System tự động lưu employee_id của người đang đăng nhập (từ JWT token)
4. **Timestamps:** createdAt và updatedAt tự động generate

## Notes

- Backend **KHÔNG** upload file lên Cloudinary
- Backend **CHỈ** lưu metadata
- FE handle toàn bộ upload process với Cloudinary
- clinicalRecordId là optional, có thể link sau bằng UPDATE API

---

**Module:** Patient Images (API 9.1)  
**Last Updated:** December 9, 2025  
**Version:** 1.0
