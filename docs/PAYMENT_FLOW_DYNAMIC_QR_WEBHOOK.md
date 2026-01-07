# Luồng Thanh Toán Tự Động - SePay Dynamic QR

## 📋 Overview

Hệ thống thanh toán cho **Hóa đơn dịch vụ nha khoa** (Invoice) và **Kế hoạch điều trị** (Treatment Plan) sử dụng **Dynamic QR Code** + **SePay Webhook**.

**Đặc điểm**:

- ✅ Mỗi hóa đơn có mã thanh toán riêng (Payment Code)
- ✅ QR code tự động điền số tiền + mã thanh toán
- ✅ Webhook tự động xác nhận khi khách chuyển khoản
- ✅ Frontend tự động cập nhật trạng thái (polling)

---

## 🔄 Luồng Đầy Đủ (4 Bước)

### **Bước 1: Tạo Hóa Đơn & Hiển Thị QR** (Backend)

#### 1.1. Frontend gọi API tạo Invoice

```http
POST /api/v1/invoices
Content-Type: application/json
Authorization: Bearer {token}

{
  "patientId": 1,
  "appointmentId": 123,
  "invoiceType": "SERVICE",
  "items": [
    {
      "serviceId": 5,
      "serviceName": "Trám răng",
      "quantity": 2,
      "unitPrice": 250000
    }
  ],
  "notes": "Điều trị răng số 16, 17"
}
```

#### 1.2. Backend xử lý (InvoiceService.java)

```java
// 1. Generate payment code unique
String paymentCode = generatePaymentCode();
// Result: PDCMS25123001 (2025-12-30, sequence 01)

// 2. Create Invoice entity
Invoice invoice = Invoice.builder()
    .invoiceCode("INV-20251230-001")
    .totalAmount(500000)         // 2 x 250,000
    .paidAmount(0)
    .remainingDebt(500000)
    .paymentStatus(PENDING_PAYMENT)  // ⚠️ QUAN TRỌNG: Trạng thái chờ
    .notes("Payment Code: PDCMS25123001 | Điều trị răng số 16, 17")
    .build();

// 3. Save to database
invoiceRepository.save(invoice);
```

#### 1.3. Backend generate QR URL (VietQRService.java)

```java
// Generate VietQR URL with payment code
String qrCodeUrl = vietQRService.generateQRUrl(
    500000,              // Số tiền
    "PDCMS25123001"      // Mã thanh toán (addInfo)
);

// Result:
// https://img.vietqr.io/image/ACB-24131687-compact2.png
//   ?amount=500000
//   &addInfo=PDCMS25123001
//   &accountName=TRINH%20CONG%20THAI
```

#### 1.4. Backend trả response về Frontend

```json
{
  "success": true,
  "data": {
    "invoiceId": 456,
    "invoiceCode": "INV-20251230-001",
    "totalAmount": 500000,
    "paidAmount": 0,
    "remainingDebt": 500000,
    "paymentStatus": "PENDING_PAYMENT",
    "paymentCode": "PDCMS25123001",
    "qrCodeUrl": "https://img.vietqr.io/image/ACB-24131687-compact2.png?amount=500000&addInfo=PDCMS25123001&accountName=TRINH%20CONG%20THAI",
    "items": [...]
  }
}
```

---

### **Bước 2: Khách Hàng Quét QR & Thanh Toán** (Customer)

#### 2.1. Khách hàng thấy QR trên màn hình

Frontend hiển thị:

- ✅ Mã QR code (ảnh từ `qrCodeUrl`)
- ✅ Số tiền: **500,000 VND**
- ✅ Mã thanh toán: **PDCMS25123001**
- ✅ Ngân hàng: **ACB - 24131687**
- ✅ Tên tài khoản: **TRINH CONG THAI**

#### 2.2. Khách mở app ngân hàng, quét mã

App ngân hàng **TỰ ĐỘNG** điền:

```
Số tiền: 500,000 VND
Nội dung: PDCMS25123001
Người nhận: TRINH CONG THAI (24131687)
```

#### 2.3. Khách bấm "Xác nhận chuyển khoản"

- Tiền được chuyển từ tài khoản khách → Tài khoản phòng khám
- Nội dung chuyển khoản: **"PDCMS25123001"** (hoặc "Nguyen Van A chuyen tien PDCMS25123001")

---

### **Bước 3: Xử Lý Webhook** (Backend - TỰ ĐỘNG)

#### 3.1. SePay phát hiện giao dịch

- SePay monitor tài khoản ngân hàng **ACB - 24131687**
- Phát hiện có tiền vào: **500,000 VND**
- Nội dung: **"PDCMS25123001"**

#### 3.2. SePay gửi Webhook về Backend

**📡 Webhook Request từ SePay**:

SePay tự động gửi POST request khi phát hiện giao dịch chuyển khoản:

```http
POST https://your-production-domain.com/api/v1/webhooks/sepay
Content-Type: application/json

{
  "id": "12345678",
  "gateway": "ACB",
  "transaction_date": "2025-12-30 14:30:00",
  "account_number": "24131687",
  "sub_account": null,
  "amount_in": 500000,
  "amount_out": 0,
  "accumulated": 1500000,
  "code": "PDCMS25123001",
  "transaction_content": "Nguyen Van A chuyen tien PDCMS25123001",
  "reference_number": "REF123456",
  "body": "..."
}
```

**Production URL Examples**:

| Deployment            | Webhook URL                                                     |
| --------------------- | --------------------------------------------------------------- |
| DigitalOcean + Domain | `https://denteeth-api.com/api/v1/webhooks/sepay`                |
| DigitalOcean IP       | `http://167.71.45.123:8080/api/v1/webhooks/sepay`               |
| Railway               | `https://pdcms-production.up.railway.app/api/v1/webhooks/sepay` |
| Render                | `https://pdcms-api.onrender.com/api/v1/webhooks/sepay`          |

**⚠️ BẢO MẬT**:

- **KHÔNG CẦN API KEY** - SePay đã bảo mật bằng IP whitelist
- Backend chỉ cần nhận request và return `{"success": true}`

**✅ Backend Response** (SePayWebhookController.java):

```json
{
  "success": true,
  "message": "Webhook processed successfully"
}
```

#### 3.3. Backend nhận Webhook (SePayWebhookController.java)

```java
@PostMapping("/api/v1/webhooks/sepay")
public ResponseEntity<?> handleWebhook(
    @RequestHeader("Authorization") String authorization,
    @RequestBody SePayWebhookData webhookData
) {
    // 1. Validate API Key
    if (!authorization.equals("Apikey " + sePayApiKey)) {
        return ResponseEntity.status(401).body(Map.of("success", false));
    }

    // 2. Process webhook
    sePayWebhookService.processWebhook(webhookData);

    // 3. Return success (QUAN TRỌNG: Để SePay biết đã nhận)
    return ResponseEntity.status(201).body(Map.of("success", true));
}
```

#### 3.4. Backend xử lý logic (SePayWebhookService.java)

```java
@Transactional
public void processWebhook(SePayWebhookData webhookData) {
    // ===== BƯỚC 1: Extract Payment Code =====
    String paymentCode = extractPaymentCode(webhookData);
    // Result: "PDCMS25123001"

    if (paymentCode == null) {
        log.warn("No payment code found in webhook");
        return;
    }

    // ===== BƯỚC 2: Tìm Invoice theo Payment Code =====
    Invoice invoice = findInvoiceByPaymentCode(paymentCode);
    // Query: SELECT * FROM invoices WHERE notes LIKE '%PDCMS25123001%'

    if (invoice == null) {
        log.warn("Invoice not found for payment code: {}", paymentCode);
        return;
    }

    // ===== BƯỚC 3: Kiểm tra duplicate =====
    if (isWebhookProcessed(webhookData.getId())) {
        log.info("Webhook already processed: {}", webhookData.getId());
        return;
    }

    // ===== BƯỚC 4: Kiểm tra số tiền =====
    BigDecimal transferAmount = BigDecimal.valueOf(webhookData.getAmountIn());

    if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
        log.warn("Invalid amount: {}", transferAmount);
        return;
    }

    // ===== BƯỚC 5: Tạo Payment Record =====
    Payment payment = paymentService.createPaymentFromWebhook(
        invoice,
        transferAmount,
        PaymentMethod.SEPAY,
        "SePay webhook - " + webhookData.getGateway(),
        webhookData.getId(),
        webhookData  // Full webhook data
    );

    // ===== BƯỚC 6: Update Invoice Status =====
    invoice.setPaidAmount(
        invoice.getPaidAmount().add(transferAmount)
    );
    invoice.setRemainingDebt(
        invoice.getTotalAmount().subtract(invoice.getPaidAmount())
    );

    // Kiểm tra đã thanh toán đủ chưa
    if (invoice.getRemainingDebt().compareTo(BigDecimal.ZERO) <= 0) {
        invoice.setPaymentStatus(InvoicePaymentStatus.PAID);  // ✅ ĐÃ THANH TOÁN ĐỦ
        invoice.setRemainingDebt(BigDecimal.ZERO);
    } else {
        invoice.setPaymentStatus(InvoicePaymentStatus.PARTIALLY_PAID);  // ⏳ THANH TOÁN MỘT PHẦN
    }

    invoiceRepository.save(invoice);

    log.info("Invoice {} payment processed successfully. Paid: {}, Remaining: {}",
        invoice.getInvoiceCode(),
        invoice.getPaidAmount(),
        invoice.getRemainingDebt());
}
```

---

### **Bước 4: Frontend Tự Động Cập Nhật** (Polling)

#### 4.1. Frontend polling (kiểm tra liên tục)

```typescript
// useInvoicePolling.ts
useEffect(() => {
  const interval = setInterval(async () => {
    // Gọi API kiểm tra invoice status mỗi 5 giây
    const response = await fetch(`/api/v1/invoices/${invoiceId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const result = await response.json();

    if (result.success) {
      const invoice = result.data;

      // Cập nhật UI
      setInvoiceStatus(invoice.paymentStatus);
      setPaidAmount(invoice.paidAmount);
      setRemainingDebt(invoice.remainingDebt);

      // Nếu đã thanh toán đủ → Dừng polling
      if (invoice.paymentStatus === "PAID") {
        clearInterval(interval);

        // Chuyển sang màn hình thành công
        router.push("/payment-success");

        // Hoặc hiện modal thành công
        showSuccessModal();
      }
    }
  }, 5000); // Poll mỗi 5 giây

  return () => clearInterval(interval);
}, [invoiceId]);
```

#### 4.2. Màn hình thanh toán cập nhật real-time

```jsx
// PaymentQRCode.tsx
{
  paymentStatus === "PENDING_PAYMENT" && (
    <div className="status-pending">
      <Spinner />
      <p>⏳ Đang chờ thanh toán...</p>
      <p>Vui lòng quét mã QR và chuyển khoản</p>
    </div>
  );
}

{
  paymentStatus === "PARTIALLY_PAID" && (
    <div className="status-partial">
      <p>✅ Đã nhận: {formatCurrency(paidAmount)}</p>
      <p>⏳ Còn lại: {formatCurrency(remainingDebt)}</p>
      <p>Vui lòng chuyển khoản phần còn lại</p>
    </div>
  );
}

{
  paymentStatus === "PAID" && (
    <div className="status-success">
      <CheckCircle className="icon-success" />
      <h2>✅ Thanh toán thành công!</h2>
      <p>Đã nhận đủ: {formatCurrency(totalAmount)}</p>
      <button onClick={handleDone}>Hoàn tất</button>
    </div>
  );
}
```

---

## 📊 Database Schema

### Invoice Table (Quan trọng nhất)

```sql
CREATE TABLE invoices (
    invoice_id SERIAL PRIMARY KEY,
    invoice_code VARCHAR(50) UNIQUE NOT NULL,
    patient_id INTEGER NOT NULL,
    appointment_id INTEGER,
    treatment_plan_id INTEGER,

    -- Thông tin thanh toán
    total_amount DECIMAL(15,2) NOT NULL,
    paid_amount DECIMAL(15,2) DEFAULT 0,
    remaining_debt DECIMAL(15,2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,  -- PENDING_PAYMENT, PARTIALLY_PAID, PAID

    -- Mã thanh toán trong notes
    notes TEXT,  -- "Payment Code: PDCMS25123001 | Ghi chú khác"

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Index để tìm invoice theo payment code nhanh
CREATE INDEX idx_invoice_notes ON invoices USING gin(to_tsvector('simple', notes));
```

### Payments Table

```sql
CREATE TABLE payments (
    payment_id SERIAL PRIMARY KEY,
    invoice_id INTEGER NOT NULL REFERENCES invoices(invoice_id),
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,  -- SEPAY, CASH, CARD, etc.
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### Payment Transactions Table (Lưu webhook data)

```sql
CREATE TABLE payment_transactions (
    transaction_id SERIAL PRIMARY KEY,
    payment_id INTEGER REFERENCES payments(payment_id),
    payment_link_id VARCHAR(255),  -- SePay webhook.id (để detect duplicate)
    callback_data TEXT,  -- Full JSON webhook data
    payment_method VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Index để check duplicate webhook
CREATE UNIQUE INDEX idx_payment_link_id ON payment_transactions(payment_link_id);
```

---

## 🎯 Payment Code Format

### Format: `PDCMSyymmddxy`

| Component | Description      | Example                       |
| --------- | ---------------- | ----------------------------- |
| `PDCMS`   | Prefix cố định   | PDCMS                         |
| `yy`      | Năm (2 digits)   | 25 = 2025                     |
| `mm`      | Tháng (2 digits) | 12 = December                 |
| `dd`      | Ngày (2 digits)  | 30 = Day 30                   |
| `xy`      | Sequence (01-99) | 01 = First invoice of the day |

### Examples:

- `PDCMS25123001` → 2025-12-30, invoice thứ 1
- `PDCMS25123002` → 2025-12-30, invoice thứ 2
- `PDCMS26010199` → 2026-01-01, invoice thứ 99

### Generation Logic:

```java
private String generatePaymentCode() {
    LocalDateTime now = LocalDateTime.now();
    String prefix = "PDCMS";
    String dateStr = now.format(DateTimeFormatter.ofPattern("yyMMdd"));

    // Count invoices created today
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    long todayCount = invoiceRepository.countByCreatedAtBetween(startOfDay, endOfDay);

    // Sequence from 01-99
    int sequence = (int) (todayCount % 99) + 1;
    String sequenceStr = String.format("%02d", sequence);

    return prefix + dateStr + sequenceStr;  // PDCMS25123001
}
```

---

## 🔍 Debugging & Testing

### Test Webhook Locally

```bash
# 1. Expose local server với ngrok
ngrok http 8080

# 2. Configure SePay webhook URL
# https://xxxx-xx-xx-xx-xx.ngrok.io/api/v1/webhooks/sepay

# 3. Test gửi fake webhook
curl -X POST http://localhost:8080/api/v1/webhooks/sepay \
  -H "Authorization: Apikey YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test123",
    "gateway": "ACB",
    "amount_in": 500000,
    "transaction_content": "Test PDCMS25123001",
    "code": "PDCMS25123001"
  }'

# 4. Check logs
docker-compose logs -f app | grep -i "webhook\|payment"
```

### Check Invoice Status

```bash
# Query invoice by payment code
curl http://localhost:8080/api/v1/invoices/{invoiceId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected response:
{
  "paymentStatus": "PAID",
  "paidAmount": 500000,
  "remainingDebt": 0,
  "paymentCode": "PDCMS25123001"
}
```

---

## ⚠️ Edge Cases & Solutions

### Case 1: Khách chuyển thiếu tiền

**Scenario**: Invoice 500k, khách chuyển 300k

**Backend xử lý**:

```java
// Webhook nhận 300k
invoice.setPaidAmount(300000);
invoice.setRemainingDebt(200000);
invoice.setPaymentStatus(PARTIALLY_PAID);  // ⏳ Chưa đủ
```

**Frontend hiển thị**:

- Còn lại: **200,000 VND**
- QR code mới với số tiền **200,000 VND** và cùng mã **PDCMS25123001**

### Case 2: Khách chuyển dư tiền

**Scenario**: Invoice 500k, khách chuyển 600k

**Backend xử lý**:

```java
invoice.setPaidAmount(600000);
invoice.setRemainingDebt(-100000);  // Dư 100k
invoice.setPaymentStatus(PAID);  // ✅ Đã đủ

// TODO: Handle overpayment
// - Tạo credit note
// - Hoặc refund
```

### Case 3: Khách chuyển nhiều lần

**Scenario**: Invoice 500k, khách chuyển 200k + 300k (2 lần)

**Backend xử lý**:

```java
// Webhook 1: 200k
invoice.setPaidAmount(200000);
invoice.setPaymentStatus(PARTIALLY_PAID);

// Webhook 2: 300k
invoice.setPaidAmount(200000 + 300000);  // = 500k
invoice.setRemainingDebt(0);
invoice.setPaymentStatus(PAID);  // ✅ Đủ rồi
```

### Case 4: Duplicate Webhook

**Scenario**: SePay gửi webhook 2 lần (network retry)

**Backend xử lý**:

```java
// Check webhook.id đã xử lý chưa
if (isWebhookProcessed(webhookData.getId())) {
    log.info("Webhook already processed");
    return;  // Bỏ qua, không tạo payment mới
}

// Nếu chưa → Xử lý bình thường
// Lưu webhook.id vào payment_transactions
```

### Case 5: Sai mã thanh toán

**Scenario**: Khách chuyển khoản với nội dung "PDCMS99999999" (mã không tồn tại)

**Backend xử lý**:

```java
Invoice invoice = findInvoiceByPaymentCode("PDCMS99999999");

if (invoice == null) {
    log.warn("Invoice not found for payment code: PDCMS99999999");
    // Lưu vào bảng unmatched_payments để admin xử lý thủ công
    saveUnmatchedPayment(webhookData);
    return;
}
```

---

## 📈 Performance & Scalability

### Query Optimization

```sql
-- Index cho việc tìm invoice theo payment code
CREATE INDEX idx_invoice_notes_payment_code ON invoices
USING gin(to_tsvector('simple', notes));

-- Query nhanh với GIN index
SELECT * FROM invoices
WHERE to_tsvector('simple', notes) @@ to_tsquery('simple', 'PDCMS25123001');
```

### Webhook Processing

- **Async**: Webhook xử lý trong background thread (đã có `@Async`)
- **Queue**: Nếu nhiều webhook cùng lúc, xem xét dùng Redis Queue
- **Retry**: SePay tự động retry nếu backend không trả success

### Frontend Polling

```typescript
// Exponential backoff: giảm tần suất polling khi chờ lâu
const getPollingInterval = (attemptCount: number) => {
  if (attemptCount < 6) return 5000; // 0-30s: 5s
  if (attemptCount < 12) return 10000; // 30s-2m: 10s
  return 15000; // >2m: 15s
};
```

---

## ✅ Checklist Implementation

### Backend

- [x] Generate unique payment code (PDCMSyymmddxy)
- [x] Store payment code in invoice.notes
- [x] Generate VietQR URL with payment code
- [x] Create SePay webhook endpoint
- [x] Validate webhook API key
- [x] Extract payment code from webhook
- [x] Find invoice by payment code
- [x] Prevent duplicate webhook processing
- [x] Create payment record
- [x] Update invoice status (PENDING → PARTIALLY_PAID → PAID)
- [x] Handle partial payments
- [x] Return success response to SePay

### Frontend

- [ ] Display QR code from API response
- [ ] Show payment information (amount, code, bank)
- [ ] Implement polling to check invoice status
- [ ] Update UI when payment detected
- [ ] Stop polling when PAID
- [ ] Show success message/redirect
- [ ] Handle partial payment display
- [ ] Copy payment code button

### Testing

- [ ] Test webhook với fake data
- [ ] Test full payment flow
- [ ] Test partial payment
- [ ] Test duplicate webhook
- [ ] Test invalid payment code
- [ ] Test polling UI updates

---

**Date**: 2025-12-30
**Status**: ✅ READY FOR INTEGRATION
**Payment Flow**: DYNAMIC QR + WEBHOOK
**Target**: Invoice & Treatment Plan Payment
