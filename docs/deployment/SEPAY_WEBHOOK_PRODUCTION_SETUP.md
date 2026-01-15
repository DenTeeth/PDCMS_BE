# 🚀 Setup SePay Webhook - Production Ready

**Domain của bạn**: `https://pdcms.duckdns.org`
**Webhook URL**: `https://pdcms.duckdns.org/api/v1/webhooks/sepay`
**Status**: ✅ Backend sẵn sàng - Cần configure SePay Dashboard

---

## 📋 Bước 1: Test Endpoint (Xác nhận backend hoạt động)

### 1.1. Test health check

```bash
curl https://pdcms.duckdns.org/actuator/health
```

**Expected**:

```json
{ "status": "UP" }
```

✅ Nếu thấy `"UP"` → Backend đang running

---

### 1.2. Test webhook endpoint

```bash
curl -X POST https://pdcms.duckdns.org/api/v1/webhooks/sepay \
  -H "Content-Type: application/json" \
  -d '{
    "id": 99999,
    "gateway": "ACB",
    "transactionDate": "2025-12-30 15:00:00",
    "accountNumber": "24131687",
    "code": "TEST",
    "content": "Test PDCMS25123001",
    "transferType": "in",
    "transferAmount": 10000,
    "accumulated": 1000000,
    "referenceNumber": "TEST001"
  }'
```

**Expected**:

```json
{
  "success": true,
  "message": "Webhook processed successfully"
}
```

✅ Nếu thấy `"success": true` → Webhook endpoint hoạt động OK

---

## 🎯 Bước 2: Configure SePay Dashboard ⚠️ QUAN TRỌNG NHẤT

### 2.1. Đăng nhập SePay

Truy cập: **https://my.sepay.vn/login**

Đăng nhập bằng:

- Email SePay của bạn
- Password

---

### 2.2. Vào trang Webhooks

**Cách 1**: Menu

```
Dashboard → Cài đặt (Settings) → Webhooks
```

**Cách 2**: Link trực tiếp

```
https://my.sepay.vn/settings/webhooks
```

---

### 2.3. Thêm Webhook mới

Click nút **"Thêm Webhook"** hoặc **"+ Add Webhook"**

---

### 2.4. Điền thông tin ⚠️ LÀM CHÍNH XÁC

#### **Bước 2.4.1: Webhook URL**

Copy-paste chính xác URL này:

```
https://pdcms.duckdns.org/api/v1/webhooks/sepay
```

**⚠️ Kiểm tra kỹ**:

- ✅ Có `https://` ở đầu (không phải `http://`)
- ✅ Domain: `pdcms.duckdns.org` (chính xác)
- ✅ Path: `/api/v1/webhooks/sepay` (đầy đủ)
- ✅ Không có khoảng trắng thừa
- ✅ Không có ký tự đặc biệt thừa

---

#### **Bước 2.4.2: Events (Sự kiện)**

Chọn loại giao dịch cần nhận webhook:

- ✅ **Giao dịch vào** (Money In / Transfer In) ← **BẮT BUỘC PHẢI CHỌN**
- ⬜ Giao dịch ra (Money Out) ← Không cần

**Tại sao chỉ chọn "Giao dịch vào"?**

- Khách hàng chuyển tiền vào tài khoản phòng khám
- Bạn cần biết khi nào có tiền vào để cập nhật invoice
- "Giao dịch ra" là khi bạn chuyển tiền đi (không liên quan đến thanh toán)

---

#### **Bước 2.4.3: Tài khoản ngân hàng**

Chọn tài khoản ngân hàng đã liên kết với SePay:

```
ACB - 24131687 (TRINH CONG THAI)
```

Hoặc tài khoản ngân hàng nào bạn muốn monitor giao dịch.

**Lưu ý**: Chỉ giao dịch vào tài khoản này mới trigger webhook.

---

#### **Bước 2.4.4: Trạng thái (Status)**

- ✅ **Active / Kích hoạt** ← **BẮT BUỘC BẬT**

**Nếu tắt**: SePay sẽ KHÔNG gửi webhook dù có giao dịch.

---

### 2.5. Lưu cấu hình

Click nút **"Lưu"** hoặc **"Save"**

SePay sẽ validate URL (gửi test request) và lưu cấu hình.

---

### 2.6. Xác nhận thành công

Sau khi lưu, bạn sẽ thấy webhook trong danh sách:

```
✅ https://pdcms.duckdns.org/api/v1/webhooks/sepay
   Events: Transfer In (Giao dịch vào)
   Status: Active 🟢
   Bank: ACB - 24131687
   Last Updated: 2025-12-30 23:00:00
```

**Screenshot để tham khảo**:

```
┌─────────────────────────────────────────────────────────┐
│ Webhook URL                                             │
│ https://pdcms.duckdns.org/api/v1/webhooks/sepay        │
├─────────────────────────────────────────────────────────┤
│ Events                                                  │
│ ☑ Giao dịch vào (Transfer In)                          │
│ ☐ Giao dịch ra (Transfer Out)                          │
├─────────────────────────────────────────────────────────┤
│ Bank Account                                            │
│ ACB - 24131687 (TRINH CONG THAI)                       │
├─────────────────────────────────────────────────────────┤
│ Status                                                  │
│ ● Active                                                │
└─────────────────────────────────────────────────────────┘
```

✅ **Xong! SePay đã sẵn sàng gửi webhook!**

---

## 🧪 Bước 3: Test End-to-End (Chuyển khoản thật)

### 3.1. Tạo invoice test

```bash
curl -X POST https://pdcms.duckdns.org/api/v1/invoices \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "invoiceType": "SERVICE",
    "items": [{
      "serviceName": "Test SePay webhook",
      "quantity": 1,
      "unitPrice": 10000
    }],
    "notes": "Test webhook production"
  }'
```

**Lấy từ response**:

```json
{
  "data": {
    "invoiceId": 456,
    "paymentCode": "PDCMS25123001",  ← LƯU LẠI CÁI NÀY
    "qrCodeUrl": "https://img.vietqr.io/image/...",
    "totalAmount": 10000,
    "paymentStatus": "PENDING_PAYMENT"
  }
}
```

---

### 3.2. Chuyển khoản qua app ngân hàng

**Mở app ngân hàng** (ACB, Vietcombank, Techcombank, etc.)

**Thông tin chuyển khoản**:

```
Ngân hàng:      ACB (Á Châu)
Số tài khoản:   24131687
Tên người nhận: TRINH CONG THAI
Số tiền:        10,000 VND
Nội dung:       PDCMS25123001  ← COPY payment code từ step 3.1
```

**⚠️ CỰC KỲ QUAN TRỌNG**:

- Nội dung **PHẢI CHỨA** payment code chính xác: `PDCMS25123001`
- Có thể thêm text khác: `Thanh toan PDCMS25123001` hoặc `PDCMS25123001 benh nhan Nguyen Van A`
- Nhưng **PHẢI CÓ** chuỗi `PDCMS` + 8 chữ số

---

### 3.3. Đợi SePay phát hiện (10-30 giây)

**Quá trình tự động**:

1. **Chuyển khoản thành công** (app ngân hàng)
2. **Ngân hàng gửi SMS** tới số điện thoại đăng ký
3. **SePay đọc SMS** và parse thông tin giao dịch
4. **SePay gửi webhook** tới backend:
   ```
   POST https://pdcms.duckdns.org/api/v1/webhooks/sepay
   ```
5. **Backend nhận webhook** và xử lý:
   - Extract payment code: `PDCMS25123001`
   - Find invoice với payment code này
   - Create payment record
   - Update invoice status: `PENDING_PAYMENT` → `PAID`
6. **Backend return success** cho SePay

**Timeline**:

```
T+0s:  Bạn bấm "Xác nhận" chuyển khoản
T+2s:  Ngân hàng gửi SMS
T+5s:  SePay đọc SMS và parse
T+7s:  SePay gửi webhook tới backend
T+8s:  Backend xử lý và update invoice
T+10s: Invoice status = PAID ✅
```

---

### 3.4. Check backend logs

**SSH vào server**:

```bash
ssh root@YOUR_SERVER_IP
```

**Xem logs real-time**:

```bash
docker logs -f dentalclinic-app | grep -E "webhook|payment|PDCMS"
```

**Expected logs** (sau khi chuyển khoản 10-30s):

```
[INFO] 🔔 Received SePay webhook - ID: 12345, Gateway: ACB, Amount: 10000, Content: PDCMS25123001
[INFO] Processing webhook: 12345
[INFO] ✅ Payment code extracted: PDCMS25123001
[INFO] ✅ Invoice found: INV-20251230-001 (ID: 456)
[INFO] ✅ Payment created: 10000 VND
[INFO] ✅ Invoice status updated: PAID
[INFO] ✅ Invoice INV-20251230-001 payment processed successfully. Paid: 10000, Remaining: 0
```

✅ **Nếu thấy logs này** → Webhook hoạt động hoàn hảo!

---

### 3.5. Verify invoice đã PAID

```bash
curl https://pdcms.duckdns.org/api/v1/invoices/456 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected response**:

```json
{
  "success": true,
  "data": {
    "invoiceId": 456,
    "invoiceCode": "INV-20251230-001",
    "totalAmount": 10000,
    "paidAmount": 10000,
    "remainingDebt": 0,
    "paymentStatus": "PAID",  ← ĐÃ THANH TOÁN
    "paymentCode": "PDCMS25123001"
  }
}
```

✅ **Nếu `paymentStatus` = `"PAID"`** → TEST THÀNH CÔNG! 🎉

---

## 📊 Bước 4: Monitor Webhook

### 4.1. Xem Webhook Logs trong SePay

**Truy cập**:

```
https://my.sepay.vn/settings/webhooks/logs
```

**Hoặc**: Dashboard → Settings → Webhooks → Click webhook URL → Tab "Logs"

---

### 4.2. Kiểm tra Status Codes

| Status                  | Meaning             | Action Needed                             |
| ----------------------- | ------------------- | ----------------------------------------- |
| ✅ **200 OK**           | Success             | Normal - không cần làm gì                 |
| ✅ **201 Created**      | Created payment     | Normal - webhook xử lý thành công         |
| ❌ **404 Not Found**    | URL không tồn tại   | Check lại webhook URL trong SePay config  |
| ❌ **500 Server Error** | Lỗi backend         | Check backend logs, có thể bug trong code |
| ⏳ **Timeout**          | Không nhận response | Check server uptime, có thể server down   |

---

### 4.3. Xem chi tiết từng webhook

Click vào webhook log để xem:

```
Request:
  URL: https://pdcms.duckdns.org/api/v1/webhooks/sepay
  Method: POST
  Body: {
    "id": 12345,
    "gateway": "ACB",
    "transferAmount": 10000,
    "content": "PDCMS25123001",
    ...
  }

Response:
  Status: 201 Created
  Body: {"success": true, "message": "Webhook processed successfully"}

Timestamp: 2025-12-30 15:30:45
```

---

## ❓ Troubleshooting

### ❌ Issue 1: SePay logs hiển thị 404 Not Found

**Triệu chứng**: Webhook logs trong SePay: `404 Not Found`

**Nguyên nhân**:

1. Sai webhook URL (thiếu `/api/v1/webhooks/sepay`)
2. Backend không running
3. Nginx Proxy Manager chưa config đúng

**Solution**:

```bash
# Test 1: Check URL thủ công
curl -X POST https://pdcms.duckdns.org/api/v1/webhooks/sepay \
  -H "Content-Type: application/json" \
  -d '{"id":1}'

# Nếu 404 → Check nginx config

# Test 2: Check backend running
ssh root@YOUR_SERVER_IP
docker ps | grep dentalclinic-app

# Nếu không thấy container → Start lại
cd /root/pdcms-be
docker-compose up -d

# Test 3: Check nginx proxy manager
# Access: http://YOUR_IP:81
# Login: admin@example.com / changeme
# Verify: pdcms.duckdns.org → app:8080
```

---

### ❌ Issue 2: Webhook timeout

**Triệu chứng**: SePay logs: `Timeout` - không nhận response

**Nguyên nhân**:

1. Backend xử lý chậm (>30 giây)
2. Server overload (CPU/Memory 100%)
3. Database lock

**Solution**:

```bash
# Check server resources
ssh root@YOUR_SERVER_IP
top

# Nếu CPU/Memory > 90% → Cần optimize hoặc upgrade server

# Check backend logs
docker logs --tail 100 dentalclinic-app | grep -i "error\|exception"

# Restart app
docker-compose restart app
```

---

### ❌ Issue 3: Invoice không update (webhook 200 OK nhưng status vẫn PENDING)

**Triệu chứng**:

- SePay logs: `200 OK` ✅
- Backend logs: `Webhook processed successfully` ✅
- Nhưng invoice vẫn `PENDING_PAYMENT` ❌

**Nguyên nhân**:

1. Payment code không khớp (sai format)
2. Invoice không tồn tại
3. Transaction rollback (database error)

**Debug**:

```bash
# 1. Check backend logs chi tiết
docker logs dentalclinic-app --tail 200 | grep -i "PDCMS25123001"

# Expected:
# ✅ Payment code extracted: PDCMS25123001
# ✅ Invoice found: INV-xxx

# Nếu KHÔNG thấy → Payment code không match

# 2. Check database
docker exec -it dentalclinic-postgres psql -U root -d dental_clinic_db

# Check invoice
SELECT invoice_code, notes, payment_status
FROM invoices
WHERE notes LIKE '%PDCMS25123001%';

# Check payment
SELECT * FROM payments
WHERE invoice_id = 456
ORDER BY created_at DESC;
```

---

## ✅ Checklist Hoàn Tất

### Backend (Đã xong ✅)

- [x] Backend deployed: `https://pdcms.duckdns.org`
- [x] Webhook endpoint: `/api/v1/webhooks/sepay`
- [x] HTTPS enabled (SSL active)
- [x] Health check OK

### SePay Config (BẠN CẦN LÀM NGAY ⏰)

- [ ] Đăng nhập SePay: https://my.sepay.vn/login
- [ ] Vào Settings → Webhooks
- [ ] Click "Thêm Webhook"
- [ ] Điền URL: `https://pdcms.duckdns.org/api/v1/webhooks/sepay`
- [ ] Chọn Event: ✅ Giao dịch vào
- [ ] Chọn Bank: ACB - 24131687
- [ ] Status: Active ✅
- [ ] Click "Lưu"

### Testing

- [ ] Test với curl (fake webhook)
- [ ] Tạo invoice test qua API
- [ ] Chuyển khoản test (10,000 VND)
- [ ] Check SePay logs (200/201 OK)
- [ ] Check backend logs (webhook received)
- [ ] Verify invoice status = PAID
- [ ] Test với frontend (nếu đã có UI)

---

## 📚 Quick Reference

### URLs của bạn

```
Backend:      https://pdcms.duckdns.org
Health:       https://pdcms.duckdns.org/actuator/health
Webhook:      https://pdcms.duckdns.org/api/v1/webhooks/sepay
```

### SePay URLs

```
Login:        https://my.sepay.vn/login
Webhooks:     https://my.sepay.vn/settings/webhooks
Transactions: https://my.sepay.vn/transactions
Logs:         https://my.sepay.vn/settings/webhooks/logs
```

### Bank Info

```
Bank:         ACB (Á Châu)
Account:      24131687
Name:         TRINH CONG THAI
```

### Payment Code Format

```
Format:       PDCMSyymmddxy
Example:      PDCMS25123001
- PDCMS:      Prefix (cố định)
- 25:         Year 2025
- 12:         Month 12 (December)
- 30:         Day 30
- 01:         Sequence (invoice thứ 1 trong ngày)
```

---

## 🎯 Next Step

**HÀNH ĐỘNG NGAY BÂY GIỜ**:

1. ✅ Mở trình duyệt
2. ✅ Truy cập: https://my.sepay.vn/login
3. ✅ Đăng nhập
4. ✅ Vào Settings → Webhooks
5. ✅ Add webhook URL: `https://pdcms.duckdns.org/api/v1/webhooks/sepay`
6. ✅ Enable "Giao dịch vào"
7. ✅ Chọn bank ACB - 24131687
8. ✅ Active + Save

**Ước tính thời gian**: 2 phút

**Sau đó test ngay**: Chuyển khoản 10k VND với payment code từ invoice test!

---

**Date**: 2025-12-30
**Status**: ✅ BACKEND READY - WAITING FOR SEPAY CONFIG
**Webhook URL**: `https://pdcms.duckdns.org/api/v1/webhooks/sepay`
**Bạn cần**: Configure webhook trong SePay Dashboard (2 phút)
