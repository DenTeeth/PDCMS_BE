# API 9.3: Get Patient Image by ID

## Overview

Lấy chi tiết một hình ảnh cụ thể theo imageId.

## Endpoint

```
GET /api/v1/patient-images/{imageId}
```

## Permission Required

`PATIENT_IMAGE_READ` - Assigned to ROLE_DENTIST, ROLE_ADMIN, ROLE_RECEPTIONIST

## Request

### Path Parameters

| Parameter | Type | Required | Description             |
| --------- | ---- | -------- | ----------------------- |
| imageId   | Long | Yes      | ID của hình ảnh cần lấy |

### Headers

```
Authorization: Bearer {accessToken}
```

## Response

### Success Response

**Status Code:** `200 OK`

```json
{
  "imageId": 1,
  "patientId": 1,
  "patientName": "Nguyen Van A",
  "clinicalRecordId": 5,
  "imageUrl": "https://res.cloudinary.com/demo/image/upload/v1/patients/patient_1/xray/test1.jpg",
  "cloudinaryPublicId": "patients/patient_1/xray/test1",
  "imageType": "XRAY",
  "description": "X-quang răng số 6",
  "capturedDate": "2025-12-08",
  "uploadedBy": 1,
  "uploaderName": "Le Anh Khoa",
  "createdAt": "2025-12-08T10:30:00",
  "updatedAt": "2025-12-08T10:30:00"
}
```

### Response Fields

| Field              | Type     | Description                       |
| ------------------ | -------- | --------------------------------- |
| imageId            | Long     | ID của hình ảnh                   |
| patientId          | Long     | ID của bệnh nhân                  |
| patientName        | String   | Tên bệnh nhân                     |
| clinicalRecordId   | Long     | ID của clinical record (nullable) |
| imageUrl           | String   | URL đầy đủ trên Cloudinary        |
| cloudinaryPublicId | String   | Public ID trên Cloudinary         |
| imageType          | Enum     | Loại ảnh: XRAY, PHOTO, etc.       |
| description        | String   | Mô tả ảnh                         |
| capturedDate       | Date     | Ngày chụp ảnh                     |
| uploadedBy         | Long     | ID của employee đã upload         |
| uploaderName       | String   | Tên của employee đã upload        |
| createdAt          | DateTime | Thời gian tạo record              |
| updatedAt          | DateTime | Thời gian cập nhật gần nhất       |

### Error Responses

| Status Code | Error Message | Description                       |
| ----------- | ------------- | --------------------------------- |
| 403         | Forbidden     | Không có quyền PATIENT_IMAGE_READ |
| 404         | Not Found     | Image không tồn tại với ID đã cho |

## Curl Command

```bash
curl -X GET http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Frontend Integration

### Get Image Details

```typescript
async function getImageDetails(imageId: number, token: string) {
  const response = await fetch(
    `http://localhost:8080/api/v1/patient-images/${imageId}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error("Image not found");
    }
    throw new Error("Failed to fetch image details");
  }

  return await response.json();
}
```

### Display Image Details

```typescript
// Usage in React/Vue component
const image = await getImageDetails(1, accessToken);

// Display image
<div>
  <img src={image.imageUrl} alt={image.description} />
  <p>Type: {image.imageType}</p>
  <p>Description: {image.description}</p>
  <p>Captured Date: {image.capturedDate}</p>
  <p>Uploaded by: {image.uploaderName}</p>
  <p>Patient: {image.patientName}</p>
</div>;
```

## Use Cases

1. **View Image Details** - Hiển thị thông tin chi tiết của ảnh khi user click vào gallery
2. **Edit Image** - Lấy current data trước khi edit
3. **Verify Upload** - Check ảnh đã được upload đúng chưa
4. **Link to Clinical Record** - Hiển thị trong clinical record detail page

## Test Data

**Available Images (after seeding or creation):**

- Create test images using API 9.1 first
- Then use returned imageId to test this API

**Test Credentials:**

- Username: `bacsi1`, Password: `123456` (ROLE_DENTIST)
- Username: `admin`, Password: `123456` (ROLE_ADMIN)
- Username: `reception1`, Password: `123456` (ROLE_RECEPTIONIST)

## Business Rules

1. **Single Record:** Always returns single image object
2. **Not Found:** Returns 404 if imageId doesn't exist
3. **No Filter:** No filtering, direct lookup by primary key
4. **Fast Query:** Uses primary key index for instant lookup

## Performance Notes

- Very fast query using primary key
- No joins needed (uses lazy loading)
- patientName và uploaderName loaded eagerly in service layer

---

**Module:** Patient Images (API 9.3)
**Last Updated:** December 9, 2025
**Version:** 1.0
