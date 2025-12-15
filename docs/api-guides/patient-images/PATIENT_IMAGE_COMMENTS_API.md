# Patient Image Comments API

## Overview
API endpoints for managing comments on patient images. Allows employees (doctors, assistants) to add annotations, observations, and notes on clinical images.

---

## Endpoints

### 1. Create Comment
Add a new comment to an image.

**Endpoint:** `POST /api/patient-images/comments`

**Request Body:**
```json
{
  "imageId": 123,
  "commentText": "Răng số 6 có dấu hiệu sâu ở mặt phía trong"
}
```

**Response:** `201 Created`
```json
{
  "commentId": 456,
  "imageId": 123,
  "commentText": "Răng số 6 có dấu hiệu sâu ở mặt phía trong",
  "createdById": 5,
  "createdByName": "Dr. Nguyen Van A",
  "createdByCode": "EMP001",
  "createdAt": "2025-12-15T10:30:00",
  "updatedAt": "2025-12-15T10:30:00",
  "isDeleted": false
}
```

---

### 2. Get Comments for Image
Retrieve all comments for a specific image.

**Endpoint:** `GET /api/patient-images/{imageId}/comments`

**Path Parameters:**
- `imageId` (Long) - Image ID

**Response:** `200 OK`
```json
[
  {
    "commentId": 456,
    "imageId": 123,
    "commentText": "Răng số 6 có dấu hiệu sâu ở mặt phía trong",
    "createdById": 5,
    "createdByName": "Dr. Nguyen Van A",
    "createdByCode": "EMP001",
    "createdAt": "2025-12-15T10:30:00",
    "updatedAt": "2025-12-15T10:30:00",
    "isDeleted": false
  },
  {
    "commentId": 457,
    "imageId": 123,
    "commentText": "Cần chụp X-quang bổ sung để xác định mức độ",
    "createdById": 3,
    "createdByName": "Dr. Tran Thi B",
    "createdByCode": "EMP002",
    "createdAt": "2025-12-15T11:00:00",
    "updatedAt": "2025-12-15T11:00:00",
    "isDeleted": false
  }
]
```

---

### 3. Update Comment
Update an existing comment (only by creator).

**Endpoint:** `PUT /api/patient-images/comments/{commentId}`

**Path Parameters:**
- `commentId` (Long) - Comment ID

**Request Body:**
```json
{
  "commentText": "Răng số 6 có sâu sâu mức độ trung bình ở mặt phía trong"
}
```

**Response:** `200 OK`
```json
{
  "commentId": 456,
  "imageId": 123,
  "commentText": "Răng số 6 có sâu răng mức độ trung bình ở mặt phía trong",
  "createdById": 5,
  "createdByName": "Dr. Nguyen Van A",
  "createdByCode": "EMP001",
  "createdAt": "2025-12-15T10:30:00",
  "updatedAt": "2025-12-15T11:15:00",
  "isDeleted": false
}
```

---

### 4. Delete Comment
Soft delete a comment (only by creator).

**Endpoint:** `DELETE /api/patient-images/comments/{commentId}`

**Path Parameters:**
- `commentId` (Long) - Comment ID

**Response:** `204 No Content`

---

### 5. Get Comment Count
Get the number of non-deleted comments for an image.

**Endpoint:** `GET /api/patient-images/{imageId}/comments/count`

**Path Parameters:**
- `imageId` (Long) - Image ID

**Response:** `200 OK`
```json
5
```

---

## Error Responses

### 400 Bad Request
```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "Image not found with ID: 999",
  "entityName": "PatientImageComment",
  "errorKey": "IMAGE_NOT_FOUND"
}
```

### 401 Unauthorized
```json
{
  "title": "Unauthorized",
  "status": 401,
  "detail": "You can only update your own comments",
  "entityName": "PatientImageComment",
  "errorKey": "UNAUTHORIZED_UPDATE"
}
```

---

## Business Rules

1. **Authentication Required**: All endpoints require valid JWT token
2. **Creator Permissions**: Only comment creator can update/delete their own comments
3. **Soft Delete**: Comments are soft-deleted (is_deleted = true) not permanently removed
4. **Sort Order**: Comments returned newest first (created_at DESC)
5. **Image Validation**: Image must exist before creating comment
6. **Non-Empty Text**: Comment text cannot be blank

---

## Use Cases

### Doctor Annotating X-Ray
```javascript
// Doctor views X-ray and adds observation
POST /api/patient-images/comments
{
  "imageId": 789,
  "commentText": "X-quang cho thấy mất xương vùng răng 36, cần cấy ghép xương trước khi implant"
}
```

### Team Collaboration
```javascript
// Get all comments to review team observations
GET /api/patient-images/789/comments

// Returns comments from multiple doctors discussing the case
```

### Comment Correction
```javascript
// Doctor updates their earlier comment
PUT /api/patient-images/comments/456
{
  "commentText": "X-quang cho thấy mất xương vùng răng 36 mức độ nghiêm trọng"
}
```

---

## Frontend Integration

### Display Comments Below Image
```javascript
// Fetch comments when image is displayed
const response = await fetch(`/api/patient-images/${imageId}/comments`);
const comments = await response.json();

comments.forEach(comment => {
  displayComment({
    author: comment.createdByName,
    text: comment.commentText,
    timestamp: comment.createdAt,
    canEdit: comment.createdById === currentUserId
  });
});
```

### Add Comment Form
```html
<div class="comment-form">
  <textarea id="commentText" placeholder="Thêm nhận xét về hình ảnh..."></textarea>
  <button onclick="addComment()">Gửi</button>
</div>

<script>
async function addComment() {
  const commentText = document.getElementById('commentText').value;
  
  await fetch('/api/patient-images/comments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      imageId: currentImageId,
      commentText: commentText
    })
  });
  
  // Refresh comments list
  loadComments();
}
</script>
```

### Show Comment Count Badge
```javascript
// Display badge on image thumbnail
const count = await fetch(`/api/patient-images/${imageId}/comments/count`)
  .then(r => r.json());

if (count > 0) {
  showBadge(count); // "5 nhận xét"
}
```

---

## Database Schema

```sql
CREATE TABLE patient_image_comments (
    comment_id BIGSERIAL PRIMARY KEY,
    image_id BIGINT NOT NULL REFERENCES patient_images(image_id),
    comment_text TEXT NOT NULL,
    created_by INTEGER NOT NULL REFERENCES employees(employee_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
```

---

## Security Considerations

1. **Access Control**: Only authenticated employees can access
2. **Audit Trail**: created_by and timestamps track all changes
3. **Soft Delete**: Maintains data integrity for audit
4. **Permission Check**: Users can only modify their own comments
5. **Patient Privacy**: Comments inherit image access permissions
