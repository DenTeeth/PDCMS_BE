# API 9.5: Delete Patient Image

## Overview

Xóa hình ảnh khỏi database. **Lưu ý:** API này chỉ xóa record trong database, không xóa file trên Cloudinary. Frontend cần xóa file trên Cloudinary riêng hoặc để Cloudinary tự cleanup.

## Endpoint

```
DELETE /api/v1/patient-images/{imageId}
```

## Permission Required

`PATIENT_IMAGE_DELETE` - Assigned to ROLE_DENTIST, ROLE_ADMIN

## Request

### Path Parameters

| Parameter | Type | Required | Description             |
| --------- | ---- | -------- | ----------------------- |
| imageId   | Long | Yes      | ID của hình ảnh cần xóa |

### Headers

```
Authorization: Bearer {accessToken}
```

## Response

### Success Response

**Status Code:** `204 No Content`

No response body (successful deletion)

### Error Responses

| Status Code | Error Message | Description                         |
| ----------- | ------------- | ----------------------------------- |
| 403         | Forbidden     | Không có quyền PATIENT_IMAGE_DELETE |
| 404         | Not Found     | Image không tồn tại với ID đã cho   |

## Curl Command

```bash
curl -X DELETE http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Frontend Integration

### Delete Image Function

```typescript
async function deletePatientImage(
  imageId: number,
  token: string
): Promise<void> {
  const response = await fetch(
    `http://localhost:8080/api/v1/patient-images/${imageId}`,
    {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error("Image not found");
    }
    if (response.status === 403) {
      throw new Error("Permission denied");
    }
    throw new Error("Failed to delete image");
  }

  // Success - no response body
}
```

### Complete Delete Flow (with Cloudinary)

```typescript
async function deleteImageComplete(
  imageId: number,
  cloudinaryPublicId: string,
  token: string
) {
  try {
    // Step 1: Delete from Backend database
    await deletePatientImage(imageId, token);

    // Step 2: Delete from Cloudinary (FE handles this)
    await deleteFromCloudinary(cloudinaryPublicId);

    console.log("Image deleted successfully from both database and Cloudinary");
  } catch (error) {
    console.error("Failed to delete image:", error);
    throw error;
  }
}

// Cloudinary delete (example - depends on your Cloudinary setup)
async function deleteFromCloudinary(publicId: string) {
  // Option 1: Call your backend proxy endpoint
  await fetch("/api/cloudinary/delete", {
    method: "POST",
    body: JSON.stringify({ publicId }),
  });

  // Option 2: Use Cloudinary client-side SDK (if allowed)
  // cloudinary.uploader.destroy(publicId);
}
```

### Delete with Confirmation

```typescript
// React/Vue component example
const handleDelete = async (image: PatientImage) => {
  // Show confirmation dialog
  const confirmed = await showConfirmDialog(
    "Xóa ảnh này?",
    "Bạn có chắc muốn xóa ảnh này? Hành động này không thể hoàn tác."
  );

  if (!confirmed) return;

  try {
    // Delete from database
    await deletePatientImage(image.imageId, accessToken);

    // Optionally delete from Cloudinary
    if (image.cloudinaryPublicId) {
      await deleteFromCloudinary(image.cloudinaryPublicId);
    }

    // Update UI - remove from list
    setImages(images.filter((img) => img.imageId !== image.imageId));

    showSuccessMessage("Ảnh đã được xóa thành công");
  } catch (error) {
    showErrorMessage("Không thể xóa ảnh: " + error.message);
  }
};
```

## Delete Flow Options

### Option 1: Delete Backend Only (Recommended for Testing)

```
User click Delete
  → FE gọi DELETE /api/v1/patient-images/{id}
  → BE xóa record trong database
  → FE remove image from UI
  → Cloudinary file vẫn tồn tại (có thể cleanup sau)
```

### Option 2: Delete Both (Production)

```
User click Delete
  → FE gọi DELETE /api/v1/patient-images/{id}
  → BE xóa record trong database (success 204)
  → FE gọi Cloudinary API để xóa file
  → FE remove image from UI
```

### Option 3: Delete Cloudinary First

```
User click Delete
  → FE gọi Cloudinary API để xóa file
  → FE gọi DELETE /api/v1/patient-images/{id}
  → BE xóa record trong database
  → FE remove image from UI
```

**Khuyến nghị:** Dùng Option 2 (delete database first) vì:

- Database delete nhanh hơn
- Nếu Cloudinary fail, vẫn có thể cleanup manual sau
- User thấy response nhanh hơn

## Use Cases

1. **Remove Wrong Image** - User upload nhầm ảnh, cần xóa
2. **Duplicate Image** - Xóa ảnh duplicate
3. **Privacy Request** - Bệnh nhân yêu cầu xóa ảnh
4. **Storage Cleanup** - Xóa ảnh cũ không cần thiết

## Business Rules

1. **Hard Delete:** Xóa vĩnh viễn khỏi database (không soft delete)
2. **Cascade Delete:** Clinical record link bị xóa theo
3. **No Undo:** Không thể khôi phục sau khi xóa
4. **Permissions:** Chỉ Dentist và Admin có quyền xóa
5. **Cloudinary Separate:** Backend không tự động xóa file trên Cloudinary

## Cloudinary Cleanup Options

### Manual Cleanup

- Periodically clean up unused files trên Cloudinary dashboard
- Có thể implement batch cleanup script

### Auto Cleanup via Cloudinary

- Set Auto Upload Mapping với Auto Tagging
- Use Cloudinary Auto Upload feature
- Enable Auto Moderation and cleanup policies

### Backend Proxy Endpoint (Optional)

Có thể tạo thêm endpoint `/api/cloudinary/delete` để FE gọi:

```java
@DeleteMapping("/api/cloudinary/delete")
public ResponseEntity<Void> deleteFromCloudinary(@RequestBody String publicId) {
    // Use Cloudinary SDK to delete
    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    return ResponseEntity.noContent().build();
}
```

## Test Scenarios

### Test 1: Successful Delete

```bash
# Step 1: Create image
curl -X POST http://localhost:8080/api/v1/patient-images \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"patientId":1,"imageUrl":"https://test.jpg","cloudinaryPublicId":"test","imageType":"XRAY"}'

# Step 2: Note imageId from response (e.g., 123)

# Step 3: Delete image
curl -X DELETE http://localhost:8080/api/v1/patient-images/123 \
  -H "Authorization: Bearer $TOKEN"

# Step 4: Verify deleted (should return 404)
curl -X GET http://localhost:8080/api/v1/patient-images/123 \
  -H "Authorization: Bearer $TOKEN"
```

### Test 2: Delete Non-Existent Image

```bash
curl -X DELETE http://localhost:8080/api/v1/patient-images/99999 \
  -H "Authorization: Bearer $TOKEN"

# Expected: 404 Not Found
```

### Test 3: Delete Without Permission

```bash
# Login as receptionist (only has READ permission)
curl -X DELETE http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer $RECEPTIONIST_TOKEN"

# Expected: 403 Forbidden
```

## Test Data

**Test Credentials:**

- Username: `bacsi1`, Password: `123456` (ROLE_DENTIST - can delete)
- Username: `admin`, Password: `123456` (ROLE_ADMIN - can delete)
- Username: `reception1`, Password: `123456` (ROLE_RECEPTIONIST - CANNOT delete)

## Safety Notes

1. **No Confirmation in API:** Backend xóa ngay, FE phải handle confirmation
2. **No Soft Delete:** Xóa vĩnh viễn, không thể recover
3. **No Cloudinary Delete:** FE phải handle Cloudinary cleanup
4. **Cascade Impact:** Nếu image linked với clinical record, link bị xóa

---

**Module:** Patient Images (API 9.5)
**Last Updated:** December 9, 2025
**Version:** 1.0
