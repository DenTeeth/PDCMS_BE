# API 9.2: Get Patient Images (with Filters and Pagination)

## Overview

Lấy danh sách tất cả hình ảnh của một bệnh nhân với khả năng filter theo nhiều tiêu chí và pagination.

## Endpoint

```
GET /api/v1/patient-images/patient/{patientId}
```

## Permission Required

`PATIENT_IMAGE_READ` - Assigned to ROLE_DENTIST, ROLE_ADMIN, ROLE_RECEPTIONIST

## Request

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| patientId | Long | Yes | ID của bệnh nhân |

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| imageType | Enum | No | - | Filter theo loại ảnh: XRAY, PHOTO, BEFORE_TREATMENT, AFTER_TREATMENT, SCAN, OTHER |
| clinicalRecordId | Long | No | - | Filter theo clinical record |
| fromDate | Date | No | - | Lọc từ ngày (format: YYYY-MM-DD) |
| toDate | Date | No | - | Lọc đến ngày (format: YYYY-MM-DD) |
| page | Integer | No | 0 | Số trang (bắt đầu từ 0) |
| size | Integer | No | 20 | Số items mỗi trang |

### Headers

```
Authorization: Bearer {accessToken}
```

## Response

### Success Response

**Status Code:** `200 OK`

```json
{
  "images": [
    {
      "imageId": 1,
      "patientId": 1,
      "patientName": "Nguyen Van A",
      "clinicalRecordId": null,
      "imageUrl": "https://res.cloudinary.com/.../test1.jpg",
      "cloudinaryPublicId": "patients/patient_1/xray/test1",
      "imageType": "XRAY",
      "description": "X-quang test",
      "capturedDate": "2025-12-08",
      "uploadedBy": 1,
      "uploaderName": "Le Anh Khoa",
      "createdAt": "2025-12-08T10:30:00",
      "updatedAt": "2025-12-08T10:30:00"
    },
    {
      "imageId": 2,
      "patientId": 1,
      "patientName": "Nguyen Van A",
      "clinicalRecordId": 5,
      "imageUrl": "https://res.cloudinary.com/.../photo1.jpg",
      "cloudinaryPublicId": "patients/patient_1/photo/photo1",
      "imageType": "PHOTO",
      "description": "Ảnh chụp răng",
      "capturedDate": "2025-12-08",
      "uploadedBy": 1,
      "uploaderName": "Le Anh Khoa",
      "createdAt": "2025-12-08T14:00:00",
      "updatedAt": "2025-12-08T14:00:00"
    }
  ],
  "currentPage": 0,
  "totalPages": 1,
  "totalElements": 2,
  "pageSize": 20
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| images | Array | Danh sách hình ảnh |
| currentPage | Integer | Trang hiện tại |
| totalPages | Integer | Tổng số trang |
| totalElements | Long | Tổng số phần tử |
| pageSize | Integer | Kích thước trang |

### Error Responses

| Status Code | Error Message | Description |
|-------------|---------------|-------------|
| 400 | Bad Request | Patient ID không được cung cấp |
| 403 | Forbidden | Không có quyền PATIENT_IMAGE_READ |
| 404 | Not Found | Patient không tồn tại |

## Curl Commands

### Test Case 1: Get All Images of Patient

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Test Case 2: Filter by Image Type

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1?imageType=XRAY" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Test Case 3: Filter by Date Range

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1?fromDate=2025-12-01&toDate=2025-12-31" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Test Case 4: Filter by Clinical Record

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1?clinicalRecordId=5" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Test Case 5: Pagination

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Test Case 6: Combined Filters

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1?imageType=XRAY&fromDate=2025-12-01&page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Filter Logic

**Filters apply với AND logic:**

1. `patientId` - Required, luôn filter theo patient
2. `imageType` - Optional, filter theo loại ảnh
3. `clinicalRecordId` - Optional, filter theo clinical record
4. `fromDate` - Optional, filter captured_date >= fromDate
5. `toDate` - Optional, filter captured_date <= toDate

**Sort Order:** Mặc định sắp xếp theo `created_at DESC` (ảnh mới nhất trước)

## Frontend Integration

### Display Gallery

```typescript
// Fetch all images for patient
const response = await fetch(
  `http://localhost:8080/api/v1/patient-images/patient/${patientId}`,
  {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  }
);

const data = await response.json();
// data.images = array of images
// data.totalElements = total count
```

### Filter by Type

```typescript
// Filter X-ray images only
const response = await fetch(
  `http://localhost:8080/api/v1/patient-images/patient/${patientId}?imageType=XRAY`,
  {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  }
);
```

### Pagination

```typescript
// Load page 2 with 10 items
const response = await fetch(
  `http://localhost:8080/api/v1/patient-images/patient/${patientId}?page=1&size=10`,
  {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  }
);
```

## Test Data

**Available Patients (from seed):**
- Patient ID: 1 - Nguyen Van A (BN001)
- Patient ID: 2 - Tran Thi B (BN002)

**Test Credentials:**
- Username: `bacsi1`, Password: `123456` (ROLE_DENTIST)
- Username: `admin`, Password: `123456` (ROLE_ADMIN)
- Username: `reception1`, Password: `123456` (ROLE_RECEPTIONIST - Read only)

## Business Rules

1. **Default Sorting:** Images sorted by created_at DESC (newest first)
2. **Pagination:** Default page size = 20, có thể adjust từ 1-100
3. **Empty Result:** Trả về empty array nếu không có ảnh
4. **Date Filter:** Sử dụng captured_date field (not created_at)

## Performance Notes

- Index trên `patient_id` để query nhanh
- Index trên `image_type` để filter hiệu quả
- Index trên `created_at` để sort nhanh
- Pagination giúp giảm tải khi có nhiều ảnh

---

**Module:** Patient Images (API 9.2)  
**Last Updated:** December 9, 2025  
**Version:** 1.0
