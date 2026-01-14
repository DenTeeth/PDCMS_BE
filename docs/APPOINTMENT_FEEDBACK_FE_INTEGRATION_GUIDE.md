# Appointment Feedback API - Hướng Dẫn Frontend Integration

## 📋 Tổng Quan

Module Appointment Feedback cho phép bệnh nhân đánh giá lịch hẹn sau khi hoàn thành. Hệ thống tuân thủ các quy tắc nghiệp vụ chặt chẽ:

- ✅ BR-20: Đánh giá **KHÔNG THỂ** chỉnh sửa hoặc xóa sau khi gửi
- ✅ BR-21: Chỉ bệnh nhân, Admin, hoặc Manager mới có quyền đánh giá
- ✅ BR-22: Chỉ đánh giá được lịch hẹn có status = `COMPLETED`
- ✅ BR-23: Mỗi lịch hẹn chỉ được đánh giá **1 LẦN DUY NHẤT**
- ✅ BR-24: Rating (1-5 sao) là **BẮT BUỘC**, comment và tags là tùy chọn

---

## 🔌 API Endpoints

### 1. Create Feedback (Tạo Đánh Giá)

**Endpoint:** `POST /api/v1/feedbacks`

**Authorization:**
- Bệnh nhân: Chỉ có thể đánh giá lịch hẹn của mình
- Admin/Manager: Có thể đánh giá bất kỳ lịch hẹn nào

**Request Body:**
```json
{
  "appointmentCode": "APT-20260107-001",
  "rating": 5,
  "comment": "Bác sĩ làm nhẹ nhàng, tư vấn kỹ",
  "tags": ["Thân thiện", "Chuyên nghiệp", "Tư vấn kỹ"]
}
```

**Field Validation:**
| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `appointmentCode` | string | ✅ Yes | Must exist, status must be COMPLETED |
| `rating` | integer | ✅ Yes | Must be 1-5 |
| `comment` | string | ❌ No | Max 1000 characters |
| `tags` | string[] | ❌ No | Max 10 tags |

**Success Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "feedbackId": 123,
    "appointmentCode": "APT-20260107-001",
    "rating": 5,
    "comment": "Bác sĩ làm nhẹ nhàng, tư vấn kỹ",
    "tags": ["Thân thiện", "Chuyên nghiệp", "Tư vấn kỹ"],
    "createdAt": "2026-01-07T10:30:00Z"
  },
  "message": "Đánh giá đã được gửi thành công"
}
```

**Error Responses:**
| Code | Error Code | Message |
|------|------------|---------|
| 400 | `INVALID_RATING` | Rating phải từ 1 đến 5 |
| 400 | `FEEDBACK_ALREADY_EXISTS` | Lịch hẹn này đã được đánh giá |
| 403 | `APPOINTMENT_NOT_COMPLETED` | Chỉ có thể đánh giá lịch hẹn đã hoàn thành |
| 403 | `NOT_AUTHORIZED` | Bạn không có quyền đánh giá lịch hẹn này |
| 404 | `APPOINTMENT_NOT_FOUND` | Không tìm thấy lịch hẹn |

---

### 2. Get Feedback by Appointment Code

**Endpoint:** `GET /api/v1/feedbacks/appointment/{appointmentCode}`

**Authorization:**
- Patient: Có thể xem feedback của lịch hẹn của mình
- Employee/Admin: Có thể xem bất kỳ feedback nào

**Example:**
```
GET /api/v1/feedbacks/appointment/APT-20260107-001
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "feedbackId": 123,
    "appointmentCode": "APT-20260107-001",
    "patientName": "Đoàn Thanh Phong",
    "employeeName": "Trịnh Công Thái",
    "rating": 5,
    "comment": "Bác sĩ làm nhẹ nhàng, tư vấn kỹ",
    "tags": ["Thân thiện", "Chuyên nghiệp"],
    "createdAt": "2026-01-07T10:30:00Z"
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "success": false,
  "error": {
    "code": "FEEDBACK_NOT_FOUND",
    "message": "Lịch hẹn này chưa có đánh giá"
  }
}
```

---

### 3. Get Feedbacks List (Admin/Employee Only)

**Endpoint:** `GET /api/v1/feedbacks`

**Authorization:** Admin, Manager, hoặc có permission `VIEW_FEEDBACK`

**Query Parameters:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `page` | int | 0 | Trang (0-indexed) |
| `size` | int | 20 | Số item/trang |
| `rating` | int | - | Filter theo số sao (1-5) |
| `employeeCode` | string | - | Filter theo bác sĩ |
| `patientCode` | string | - | Filter theo bệnh nhân |
| `fromDate` | date | - | Filter từ ngày (YYYY-MM-DD) |
| `toDate` | date | - | Filter đến ngày (YYYY-MM-DD) |
| `sort` | string | createdAt,desc | Sắp xếp (field,direction) |

**Example:**
```
GET /api/v1/feedbacks?rating=5&fromDate=2026-01-01&toDate=2026-01-31&page=0&size=20&sort=createdAt,desc
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "feedbackId": 123,
        "appointmentCode": "APT-20260107-001",
        "patientName": "Đoàn Thanh Phong",
        "employeeName": "Trịnh Công Thái",
        "rating": 5,
        "comment": "Bác sĩ làm nhẹ nhàng",
        "tags": ["Thân thiện"],
        "createdAt": "2026-01-07T10:30:00Z"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "number": 0,
    "size": 20
  }
}
```

---

### 4. Get Feedback Statistics (Admin/Employee Only)

**Endpoint:** `GET /api/v1/feedbacks/statistics`

**Authorization:** Admin, Manager, hoặc có permission `VIEW_FEEDBACK`

**Query Parameters:**
| Param | Type | Description |
|-------|------|-------------|
| `employeeCode` | string | Filter theo bác sĩ (optional) |
| `fromDate` | date | Từ ngày (YYYY-MM-DD, optional) |
| `toDate` | date | Đến ngày (YYYY-MM-DD, optional) |

**Example:**
```
GET /api/v1/feedbacks/statistics?fromDate=2026-01-01&toDate=2026-01-31
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "totalFeedbacks": 150,
    "averageRating": 4.5,
    "ratingDistribution": {
      "1": 5,
      "2": 10,
      "3": 20,
      "4": 45,
      "5": 70
    },
    "topTags": [
      { "tag": "Thân thiện", "count": 80 },
      { "tag": "Chuyên nghiệp", "count": 65 },
      { "tag": "Sạch sẽ", "count": 50 }
    ]
  }
}
```

---

## 🏷️ Predefined Tags

Gợi ý các tags có sẵn cho người dùng chọn:

```javascript
const PREDEFINED_TAGS = [
  "Sạch sẽ",
  "Thân thiện",
  "Chuyên nghiệp",
  "Đúng giờ",
  "Tư vấn kỹ",
  "Nhẹ nhàng",
  "Giá hợp lý",
  "Cơ sở vật chất tốt"
];
```

---

## 🔄 Integration với Appointment API

### Trường `hasFeedback` trong Appointment Response

Cả `AppointmentDetailDTO` và `AppointmentSummaryDTO` đều có thêm field mới:

```json
{
  "appointmentCode": "APT-20260107-001",
  "status": "COMPLETED",
  "hasFeedback": true,  // ← Field mới
  // ... other fields
}
```

**Cách sử dụng:**
- `hasFeedback = false` → Hiện nút "Đánh giá"
- `hasFeedback = true` → Ẩn nút "Đánh giá", hiện badge "Đã đánh giá"

---

## 💻 Frontend Implementation Example

### 1. Kiểm tra và hiển thị nút đánh giá

```typescript
// Trong Appointment Detail Component
const appointment = {
  appointmentCode: "APT-20260107-001",
  status: "COMPLETED",
  hasFeedback: false
};

// Chỉ hiện nút đánh giá khi:
// 1. Status = COMPLETED
// 2. hasFeedback = false
const shouldShowFeedbackButton = 
  appointment.status === 'COMPLETED' && !appointment.hasFeedback;

return (
  <div>
    {shouldShowFeedbackButton && (
      <Button onClick={openFeedbackModal}>
        Đánh giá lịch hẹn
      </Button>
    )}
    
    {appointment.hasFeedback && (
      <Badge color="green">Đã đánh giá</Badge>
    )}
  </div>
);
```

### 2. Tạo feedback (POST)

```typescript
const submitFeedback = async (data: {
  rating: number;
  comment?: string;
  tags?: string[];
}) => {
  try {
    const response = await api.post('/api/v1/feedbacks', {
      appointmentCode: appointment.appointmentCode,
      rating: data.rating,
      comment: data.comment,
      tags: data.tags
    });

    if (response.data.success) {
      toast.success('Đánh giá đã được gửi thành công!');
      // Refresh appointment data để cập nhật hasFeedback
      refreshAppointmentData();
    }
  } catch (error) {
    if (error.response?.status === 400) {
      const errorCode = error.response.data.error?.code;
      
      if (errorCode === 'FEEDBACK_ALREADY_EXISTS') {
        toast.error('Lịch hẹn này đã được đánh giá rồi');
      } else if (errorCode === 'INVALID_RATING') {
        toast.error('Rating phải từ 1 đến 5');
      }
    } else if (error.response?.status === 403) {
      toast.error('Chỉ có thể đánh giá lịch hẹn đã hoàn thành');
    }
  }
};
```

### 3. Xem feedback đã gửi (GET)

```typescript
const loadFeedback = async (appointmentCode: string) => {
  try {
    const response = await api.get(
      `/api/v1/feedbacks/appointment/${appointmentCode}`
    );

    if (response.data.success) {
      setFeedback(response.data.data);
    }
  } catch (error) {
    if (error.response?.status === 404) {
      console.log('Lịch hẹn chưa có đánh giá');
    }
  }
};
```

### 4. Rating Component

```tsx
import { Star } from 'lucide-react';

const RatingStars = ({ value, onChange, readOnly = false }) => {
  return (
    <div className="flex gap-1">
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          type="button"
          disabled={readOnly}
          onClick={() => onChange?.(star)}
          className={`text-2xl ${
            star <= value ? 'text-yellow-400' : 'text-gray-300'
          }`}
        >
          <Star fill={star <= value ? 'currentColor' : 'none'} />
        </button>
      ))}
    </div>
  );
};
```

### 5. Feedback Form

```tsx
const FeedbackForm = ({ appointmentCode, onSuccess }) => {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [selectedTags, setSelectedTags] = useState<string[]>([]);

  const PREDEFINED_TAGS = [
    "Sạch sẽ", "Thân thiện", "Chuyên nghiệp", 
    "Đúng giờ", "Tư vấn kỹ", "Nhẹ nhàng", 
    "Giá hợp lý", "Cơ sở vật chất tốt"
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (rating === 0) {
      toast.error('Vui lòng chọn số sao đánh giá');
      return;
    }

    await submitFeedback({
      appointmentCode,
      rating,
      comment: comment.trim() || undefined,
      tags: selectedTags.length > 0 ? selectedTags : undefined
    });

    onSuccess();
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-medium mb-2">
          Đánh giá của bạn <span className="text-red-500">*</span>
        </label>
        <RatingStars value={rating} onChange={setRating} />
      </div>

      <div>
        <label className="block text-sm font-medium mb-2">
          Tags (Tùy chọn)
        </label>
        <div className="flex flex-wrap gap-2">
          {PREDEFINED_TAGS.map((tag) => (
            <button
              key={tag}
              type="button"
              onClick={() => {
                setSelectedTags((prev) =>
                  prev.includes(tag)
                    ? prev.filter((t) => t !== tag)
                    : [...prev, tag]
                );
              }}
              className={`px-3 py-1 rounded-full text-sm ${
                selectedTags.includes(tag)
                  ? 'bg-blue-500 text-white'
                  : 'bg-gray-200 text-gray-700'
              }`}
            >
              {tag}
            </button>
          ))}
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium mb-2">
          Nhận xét (Tùy chọn)
        </label>
        <textarea
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          maxLength={1000}
          rows={4}
          className="w-full border rounded p-2"
          placeholder="Chia sẻ trải nghiệm của bạn..."
        />
        <div className="text-xs text-gray-500 text-right">
          {comment.length}/1000 ký tự
        </div>
      </div>

      <Button type="submit" className="w-full">
        Gửi đánh giá
      </Button>
    </form>
  );
};
```

---

## ⚠️ Lưu Ý Quan Trọng

1. **KHÔNG cho phép chỉnh sửa/xóa feedback:** Sau khi gửi, feedback không thể thay đổi (BR-20)

2. **Kiểm tra status trước khi hiện form:** Chỉ hiện form khi `status === 'COMPLETED'`

3. **Validation rating:** Đảm bảo rating từ 1-5, không để người dùng submit rating = 0

4. **Handle error codes:** Xử lý đúng các error codes để hiện thông báo phù hợp

5. **Refresh data sau khi submit:** Sau khi submit thành công, cần refresh appointment data để `hasFeedback` cập nhật thành `true`

6. **Giới hạn tags:** Tối đa 10 tags, nên validate trên FE

7. **Character limit:** Comment giới hạn 1000 ký tự

---

## 📊 Dashboard/Statistics Page

Admin/Employee có thể xem thống kê tổng quan:

```tsx
const FeedbackStatistics = () => {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    loadStatistics();
  }, []);

  const loadStatistics = async () => {
    const response = await api.get('/api/v1/feedbacks/statistics', {
      params: {
        fromDate: '2026-01-01',
        toDate: '2026-01-31'
      }
    });

    setStats(response.data.data);
  };

  if (!stats) return <Loading />;

  return (
    <div className="grid grid-cols-3 gap-4">
      <Card>
        <h3>Tổng đánh giá</h3>
        <p className="text-3xl font-bold">{stats.totalFeedbacks}</p>
      </Card>

      <Card>
        <h3>Rating trung bình</h3>
        <p className="text-3xl font-bold">{stats.averageRating}/5</p>
      </Card>

      <Card>
        <h3>Phân bố rating</h3>
        <BarChart data={stats.ratingDistribution} />
      </Card>

      <Card className="col-span-3">
        <h3>Top Tags</h3>
        <ul>
          {stats.topTags.map((item) => (
            <li key={item.tag}>
              {item.tag}: {item.count} lượt
            </li>
          ))}
        </ul>
      </Card>
    </div>
  );
};
```

---

## 🧪 Testing Checklist

- [ ] Submit feedback với đầy đủ thông tin (rating + comment + tags)
- [ ] Submit feedback chỉ có rating (minimum viable)
- [ ] Submit feedback cho appointment chưa COMPLETED → Error 403
- [ ] Submit feedback 2 lần cho cùng 1 appointment → Error 400 FEEDBACK_ALREADY_EXISTS
- [ ] Patient submit feedback cho appointment của người khác → Error 403
- [ ] Kiểm tra hasFeedback cập nhật đúng sau khi submit
- [ ] Load feedback list với các filters
- [ ] Load statistics và verify numbers

---

Chúc bạn triển khai thành công! 🎉
