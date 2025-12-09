# API 9.4: Update Patient Image Metadata

## Overview

Cập nhật metadata của hình ảnh (không upload lại file). Chỉ có thể update description, imageType, capturedDate, và clinicalRecordId. Không thể update imageUrl hoặc cloudinaryPublicId.

## Endpoint

```
PUT /api/v1/patient-images/{imageId}
```

## Permission Required

`PATIENT_IMAGE_UPDATE` - Assigned to ROLE_DENTIST, ROLE_ADMIN

## Request

### Path Parameters

| Parameter | Type | Required | Description                  |
| --------- | ---- | -------- | ---------------------------- |
| imageId   | Long | Yes      | ID của hình ảnh cần cập nhật |

### Headers

```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

### Body

All fields are **OPTIONAL**. Chỉ gửi fields cần update.

```json
{
  "imageType": "BEFORE_TREATMENT",
  "description": "Ảnh trước điều trị - Updated",
  "capturedDate": "2025-12-07",
  "clinicalRecordId": 5
}
```

### Field Descriptions

| Field            | Type   | Required | Description                                                           |
| ---------------- | ------ | -------- | --------------------------------------------------------------------- |
| imageType        | Enum   | No       | Loại ảnh: XRAY, PHOTO, BEFORE_TREATMENT, AFTER_TREATMENT, SCAN, OTHER |
| description      | String | No       | Mô tả ảnh mới                                                         |
| capturedDate     | Date   | No       | Ngày chụp ảnh (YYYY-MM-DD)                                            |
| clinicalRecordId | Long   | No       | Link đến clinical record                                              |

**Note:** Không thể update `imageUrl` hoặc `cloudinaryPublicId`. Nếu cần thay đổi file, phải DELETE và CREATE mới.

## Response

### Success Response

**Status Code:** `200 OK`

```json
{
  "imageId": 1,
  "patientId": 1,
  "patientName": "Nguyen Van A",
  "clinicalRecordId": 5,
  "imageUrl": "https://res.cloudinary.com/.../test1.jpg",
  "cloudinaryPublicId": "patients/patient_1/xray/test1",
  "imageType": "BEFORE_TREATMENT",
  "description": "Ảnh trước điều trị - Updated",
  "capturedDate": "2025-12-07",
  "uploadedBy": 1,
  "uploaderName": "Le Anh Khoa",
  "createdAt": "2025-12-08T10:30:00",
  "updatedAt": "2025-12-09T11:45:00"
}
```

### Error Responses

| Status Code | Error Message | Description                                             |
| ----------- | ------------- | ------------------------------------------------------- |
| 400         | Bad Request   | Clinical record không thuộc về bệnh nhân này            |
| 403         | Forbidden     | Không có quyền PATIENT_IMAGE_UPDATE                     |
| 404         | Not Found     | Image không tồn tại                                     |
| 404         | Not Found     | Clinical record không tồn tại (nếu có clinicalRecordId) |

## Curl Commands

### Test Case 1: Update Description Only

```bash
curl -X PUT http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated description"
  }'
```

### Test Case 2: Update Image Type

```bash
curl -X PUT http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageType": "BEFORE_TREATMENT"
  }'
```

### Test Case 3: Link to Clinical Record

```bash
curl -X PUT http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clinicalRecordId": 5
  }'
```

### Test Case 4: Update Multiple Fields

```bash
curl -X PUT http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageType": "AFTER_TREATMENT",
    "description": "Ảnh sau điều trị - kết quả tốt",
    "capturedDate": "2025-12-09",
    "clinicalRecordId": 5
  }'
```

### Test Case 5: Remove Clinical Record Link

```bash
curl -X PUT http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clinicalRecordId": null
  }'
```

## Frontend Integration

### Update Image Metadata

```typescript
async function updateImageMetadata(
  imageId: number,
  updates: Partial<ImageMetadata>,
  token: string
) {
  const response = await fetch(
    `http://localhost:8080/api/v1/patient-images/${imageId}`,
    {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(updates),
    }
  );

  if (!response.ok) {
    throw new Error("Failed to update image");
  }

  return await response.json();
}

// Usage
await updateImageMetadata(
  1,
  {
    description: "New description",
    imageType: "BEFORE_TREATMENT",
  },
  accessToken
);
```

### Edit Form Example

```typescript
// React/Vue component
const [formData, setFormData] = useState({
  imageType: image.imageType,
  description: image.description,
  capturedDate: image.capturedDate,
  clinicalRecordId: image.clinicalRecordId,
});

const handleSubmit = async () => {
  try {
    const updated = await updateImageMetadata(
      image.imageId,
      formData,
      accessToken
    );
    // Update UI with new data
    console.log("Updated successfully:", updated);
  } catch (error) {
    console.error("Update failed:", error);
  }
};
```

## Use Cases

1. **Correct Image Type** - User chọn sai type khi upload, cần đổi
2. **Add Description** - Thêm mô tả chi tiết sau khi upload
3. **Link to Clinical Record** - Link ảnh với clinical record sau khi tạo record
4. **Update Captured Date** - Sửa ngày chụp nếu nhập sai

## Business Rules

1. **Partial Update:** Chỉ update fields được gửi trong request
2. **Image URL Immutable:** Không thể đổi imageUrl hoặc cloudinaryPublicId
3. **Clinical Record Validation:** Phải thuộc về cùng patient
4. **Updated Timestamp:** Tự động update updatedAt khi save
5. **Permissions:** Chỉ Dentist và Admin có quyền update

## Validation Rules

- `imageType` - Phải là một trong 6 values hợp lệ
- `capturedDate` - Không được là future date
- `clinicalRecordId` - Phải tồn tại và thuộc về patient của image

## Test Data

**Test Credentials:**

- Username: `bacsi1`, Password: `123456` (ROLE_DENTIST - can update)
- Username: `admin`, Password: `123456` (ROLE_ADMIN - can update)
- Username: `reception1`, Password: `123456` (ROLE_RECEPTIONIST - CANNOT update)

## Notes

- **Cannot Update File:** Nếu cần thay file khác, phải DELETE image cũ và CREATE mới
- **Metadata Only:** API này chỉ update metadata trong database, không touch Cloudinary
- **Cloudinary Sync:** FE phải ensure Cloudinary file vẫn match với database record

---

**Module:** Patient Images (API 9.4)
**Last Updated:** December 9, 2025
**Version:** 1.0
