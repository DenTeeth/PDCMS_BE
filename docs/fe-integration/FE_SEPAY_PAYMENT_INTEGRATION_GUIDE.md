# FE Integration Guide: SePay Payment System

## 📋 Overview

This guide shows how to integrate the SePay payment system into the frontend application. The system uses **bank transfer QR codes** and **webhook notifications** to automatically confirm payments.

### Key Features

- ✅ Generate payment QR codes automatically
- ✅ Real-time payment confirmation via webhook
- ✅ Payment code format: `PDCMSyymmddxy` (date-based, unique daily)
- ✅ Support for partial and full payments
- ✅ VietQR integration for easy bank transfers

---

## 🔄 Payment Flow

```
┌─────────────┐
│ 1. Create   │
│   Invoice   │ → Generate payment code: PDCMS25123001
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 2. Get      │
│   Invoice   │ → Receive paymentCode + qrCodeUrl
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 3. Display  │
│   QR Code   │ → Customer scans & transfers money
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 4. SePay    │
│   Webhook   │ → Backend receives notification → Update status
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 5. Poll     │
│   Invoice   │ → Check payment status → Update UI
└─────────────┘
```

---

## 🚀 Step-by-Step Implementation

### 1. Create Invoice (Existing Flow)

**Endpoint**: `POST /api/v1/invoices`

**Request**:

```json
{
  "patient": {
    "patientId": 1
  },
  "appointment": {
    "appointmentId": 123
  },
  "discount": 0,
  "notes": "Payment Code: PDCMS25123001" // Auto-generated
}
```

**Response**:

```json
{
  "success": true,
  "statusCode": 201,
  "message": "Invoice created successfully",
  "data": {
    "invoiceId": 456,
    "patientName": "Nguyen Van A",
    "totalAmount": 500000,
    "paidAmount": 0,
    "remainingDebt": 500000,
    "status": "UNPAID",
    "paymentCode": "PDCMS25123001",
    "qrCodeUrl": "https://img.vietqr.io/image/ACB-24131687-compact2.png?amount=500000&addInfo=PDCMS25123001&accountName=TRINH%20CONG%20THAI",
    "createdAt": "2025-12-30T10:30:00"
  }
}
```

### 2. Display QR Code Payment Screen

**React/Next.js Example**:

```typescript
// components/PaymentQRCode.tsx
import React, { useState, useEffect } from "react";
import Image from "next/image";

interface PaymentQRCodeProps {
  invoiceId: number;
  paymentCode: string;
  qrCodeUrl: string;
  totalAmount: number;
  remainingDebt: number;
}

export const PaymentQRCode: React.FC<PaymentQRCodeProps> = ({
  invoiceId,
  paymentCode,
  qrCodeUrl,
  totalAmount,
  remainingDebt,
}) => {
  const [paymentStatus, setPaymentStatus] = useState<
    "UNPAID" | "PARTIALLY_PAID" | "PAID"
  >("UNPAID");
  const [paidAmount, setPaidAmount] = useState(0);

  // Poll invoice status every 5 seconds
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const response = await fetch(`/api/v1/invoices/${invoiceId}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
          },
        });
        const result = await response.json();

        if (result.success) {
          setPaymentStatus(result.data.status);
          setPaidAmount(result.data.paidAmount);

          // Stop polling when fully paid
          if (result.data.status === "PAID") {
            clearInterval(interval);
            onPaymentComplete?.(result.data);
          }
        }
      } catch (error) {
        console.error("Error polling payment status:", error);
      }
    }, 5000); // Poll every 5 seconds

    return () => clearInterval(interval);
  }, [invoiceId]);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount);
  };

  return (
    <div className="payment-qr-container">
      <div className="qr-header">
        <h2>Thanh toán hóa đơn</h2>
        <p className="payment-code">
          Mã thanh toán: <strong>{paymentCode}</strong>
        </p>
      </div>

      <div className="qr-body">
        <div className="qr-image">
          <Image
            src={qrCodeUrl}
            alt="Payment QR Code"
            width={300}
            height={300}
            priority
          />
        </div>

        <div className="payment-info">
          <div className="info-row">
            <span>Tổng tiền:</span>
            <strong>{formatCurrency(totalAmount)}</strong>
          </div>
          <div className="info-row">
            <span>Đã thanh toán:</span>
            <strong className="text-success">
              {formatCurrency(paidAmount)}
            </strong>
          </div>
          <div className="info-row">
            <span>Còn lại:</span>
            <strong className="text-danger">
              {formatCurrency(remainingDebt)}
            </strong>
          </div>

          <div className="status-badge">
            {paymentStatus === "PAID" && (
              <span className="badge-success">✅ Đã thanh toán</span>
            )}
            {paymentStatus === "PARTIALLY_PAID" && (
              <span className="badge-warning">⏳ Thanh toán một phần</span>
            )}
            {paymentStatus === "UNPAID" && (
              <span className="badge-danger">❌ Chưa thanh toán</span>
            )}
          </div>
        </div>
      </div>

      <div className="qr-instructions">
        <h3>Hướng dẫn thanh toán</h3>
        <ol>
          <li>Mở ứng dụng ngân hàng (ACB, VCB, MBBank, v.v.)</li>
          <li>Quét mã QR bên trên</li>
          <li>Kiểm tra thông tin chuyển khoản</li>
          <li>Xác nhận thanh toán</li>
          <li>Chờ hệ thống cập nhật trạng thái (tự động trong 10-30 giây)</li>
        </ol>

        <div className="warning-box">
          <p>
            ⚠️ <strong>Lưu ý quan trọng:</strong>
          </p>
          <ul>
            <li>
              Nội dung chuyển khoản PHẢI có mã: <code>{paymentCode}</code>
            </li>
            <li>Không cần gõ thủ công, QR đã bao gồm mã thanh toán</li>
            <li>Nếu chuyển khoản sai mã, hệ thống sẽ KHÔNG tự động cập nhật</li>
          </ul>
        </div>
      </div>
    </div>
  );
};
```

**CSS Styling**:

```css
/* styles/PaymentQRCode.css */
.payment-qr-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.qr-header {
  text-align: center;
  margin-bottom: 24px;
}

.qr-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.payment-code {
  font-size: 16px;
  color: #666;
}

.payment-code strong {
  color: #0066cc;
  font-family: "Courier New", monospace;
}

.qr-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.qr-image {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  background: #f9f9f9;
}

.payment-info {
  width: 100%;
  background: #f5f5f5;
  padding: 16px;
  border-radius: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 16px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.status-badge {
  margin-top: 16px;
  text-align: center;
}

.badge-success,
.badge-warning,
.badge-danger {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
}

.badge-success {
  background: #d4edda;
  color: #155724;
}

.badge-warning {
  background: #fff3cd;
  color: #856404;
}

.badge-danger {
  background: #f8d7da;
  color: #721c24;
}

.qr-instructions {
  margin-top: 24px;
  padding: 16px;
  background: #f0f8ff;
  border-radius: 8px;
}

.qr-instructions h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}

.qr-instructions ol {
  margin: 0 0 16px 20px;
  line-height: 1.8;
}

.warning-box {
  background: #fff3cd;
  border-left: 4px solid #ffc107;
  padding: 12px 16px;
  border-radius: 4px;
}

.warning-box p {
  margin: 0 0 8px 0;
  font-weight: 600;
}

.warning-box ul {
  margin: 0 0 0 20px;
  line-height: 1.8;
}

.warning-box code {
  background: #ffe69c;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: "Courier New", monospace;
  font-weight: 600;
}

@media (max-width: 768px) {
  .payment-qr-container {
    padding: 16px;
  }

  .qr-image img {
    width: 250px;
    height: 250px;
  }
}
```

### 3. Payment Status Polling

**Option 1: Simple Interval Polling** (Recommended)

```typescript
// hooks/useInvoicePolling.ts
import { useState, useEffect } from "react";

export interface Invoice {
  invoiceId: number;
  status: "UNPAID" | "PARTIALLY_PAID" | "PAID";
  totalAmount: number;
  paidAmount: number;
  remainingDebt: number;
}

export const useInvoicePolling = (
  invoiceId: number,
  intervalMs: number = 5000
) => {
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    let intervalId: NodeJS.Timeout;

    const fetchInvoice = async () => {
      try {
        const response = await fetch(`/api/v1/invoices/${invoiceId}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
          },
        });

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }

        const result = await response.json();

        if (mounted && result.success) {
          setInvoice(result.data);
          setLoading(false);

          // Stop polling when paid
          if (result.data.status === "PAID") {
            clearInterval(intervalId);
          }
        }
      } catch (err) {
        if (mounted) {
          setError(
            err instanceof Error ? err.message : "Failed to fetch invoice"
          );
          setLoading(false);
        }
      }
    };

    // Initial fetch
    fetchInvoice();

    // Start polling
    intervalId = setInterval(fetchInvoice, intervalMs);

    return () => {
      mounted = false;
      clearInterval(intervalId);
    };
  }, [invoiceId, intervalMs]);

  return { invoice, loading, error };
};
```

**Usage**:

```typescript
// pages/invoice/[id].tsx
import { useInvoicePolling } from "@/hooks/useInvoicePolling";

export default function InvoicePage({ invoiceId }: { invoiceId: number }) {
  const { invoice, loading, error } = useInvoicePolling(invoiceId);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!invoice) return <div>Invoice not found</div>;

  return (
    <PaymentQRCode
      invoiceId={invoice.invoiceId}
      paymentCode={invoice.paymentCode}
      qrCodeUrl={invoice.qrCodeUrl}
      totalAmount={invoice.totalAmount}
      remainingDebt={invoice.remainingDebt}
    />
  );
}
```

**Option 2: WebSocket for Real-time Updates** (Advanced)

If you want instant updates without polling, consider implementing WebSocket connection on the backend and listening on the frontend.

---

## 📡 API Reference

### Get Invoice Details

**Endpoint**: `GET /api/v1/invoices/{invoiceId}`

**Headers**:

```
Authorization: Bearer {accessToken}
```

**Response**:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Invoice retrieved successfully",
  "data": {
    "invoiceId": 456,
    "patientName": "Nguyen Van A",
    "totalAmount": 500000,
    "paidAmount": 250000,
    "remainingDebt": 250000,
    "status": "PARTIALLY_PAID",
    "paymentCode": "PDCMS25123001",
    "qrCodeUrl": "https://img.vietqr.io/image/ACB-24131687-compact2.png?amount=250000&addInfo=PDCMS25123001&accountName=TRINH%20CONG%20THAI",
    "createdAt": "2025-12-30T10:30:00",
    "items": [
      {
        "serviceName": "Trám răng",
        "quantity": 2,
        "unitPrice": 250000,
        "totalPrice": 500000
      }
    ],
    "payments": [
      {
        "paymentId": 789,
        "amount": 250000,
        "paymentMethod": "SEPAY",
        "notes": "SePay webhook - ACB",
        "createdAt": "2025-12-30T10:35:00"
      }
    ]
  }
}
```

### Get All Invoices (with Payment Status)

**Endpoint**: `GET /api/v1/invoices?status={status}&page={page}&size={size}`

**Query Parameters**:

- `status` (optional): `UNPAID`, `PARTIALLY_PAID`, `PAID`
- `page` (optional): Page number (default: 0)
- `size` (optional): Items per page (default: 10)

**Response**:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Invoices retrieved successfully",
  "data": {
    "content": [
      {
        "invoiceId": 456,
        "patientName": "Nguyen Van A",
        "totalAmount": 500000,
        "paidAmount": 250000,
        "remainingDebt": 250000,
        "status": "PARTIALLY_PAID",
        "paymentCode": "PDCMS25123001",
        "qrCodeUrl": "https://img.vietqr.io/image/...",
        "createdAt": "2025-12-30T10:30:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 1,
      "totalPages": 1
    }
  }
}
```

---

## 🎨 Payment Status States

### Invoice Status Enum

| Status           | Description                         | Color              |
| ---------------- | ----------------------------------- | ------------------ |
| `UNPAID`         | No payment received yet             | Red (`#f44336`)    |
| `PARTIALLY_PAID` | Some payment received, debt remains | Orange (`#ff9800`) |
| `PAID`           | Fully paid, no debt                 | Green (`#4caf50`)  |

### Visual Status Indicators

```typescript
const getStatusBadge = (status: string) => {
  switch (status) {
    case "PAID":
      return {
        label: "Đã thanh toán",
        icon: "✅",
        color: "green",
      };
    case "PARTIALLY_PAID":
      return {
        label: "Thanh toán một phần",
        icon: "⏳",
        color: "orange",
      };
    case "UNPAID":
      return {
        label: "Chưa thanh toán",
        icon: "❌",
        color: "red",
      };
    default:
      return {
        label: "Unknown",
        icon: "❓",
        color: "gray",
      };
  }
};
```

---

## 🔐 Security Best Practices

### 1. Protect API Endpoints

Always include JWT token in requests:

```typescript
const fetchInvoice = async (invoiceId: number) => {
  const token = localStorage.getItem("accessToken");

  if (!token) {
    throw new Error("No authentication token found");
  }

  const response = await fetch(`/api/v1/invoices/${invoiceId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });

  if (response.status === 401) {
    // Token expired - redirect to login
    window.location.href = "/login";
    throw new Error("Authentication required");
  }

  return response.json();
};
```

### 2. Validate Payment Codes

Never trust payment codes from user input. Always fetch from API:

```typescript
// ❌ BAD: Don't construct payment code manually
const paymentCode = `PDCMS${Date.now()}`;

// ✅ GOOD: Get from invoice API response
const invoice = await fetchInvoice(invoiceId);
const paymentCode = invoice.data.paymentCode;
```

### 3. Handle Token Refresh

```typescript
import axios from "axios";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem("refreshToken");
        const response = await axios.post("/api/v1/auth/refresh", {
          refreshToken,
        });

        const { accessToken } = response.data;
        localStorage.setItem("accessToken", accessToken);

        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        // Refresh failed - redirect to login
        localStorage.clear();
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
```

---

## 🐛 Error Handling

### Common Errors

#### 1. QR Code Not Loading

```typescript
const [qrError, setQrError] = useState(false);

<Image
  src={qrCodeUrl}
  alt="Payment QR Code"
  onError={() => {
    setQrError(true);
    console.error("Failed to load QR code");
  }}
/>;

{
  qrError && (
    <div className="error-message">
      <p>⚠️ Không thể tải mã QR</p>
      <p>
        Vui lòng chuyển khoản thủ công với nội dung:{" "}
        <strong>{paymentCode}</strong>
      </p>
      <div className="bank-info">
        <p>
          Ngân hàng: <strong>ACB</strong>
        </p>
        <p>
          Số tài khoản: <strong>24131687</strong>
        </p>
        <p>
          Tên tài khoản: <strong>TRINH CONG THAI</strong>
        </p>
        <p>
          Số tiền: <strong>{formatCurrency(remainingDebt)}</strong>
        </p>
      </div>
    </div>
  );
}
```

#### 2. Payment Status Not Updating

```typescript
const [pollAttempts, setPollAttempts] = useState(0);
const MAX_POLL_ATTEMPTS = 60; // 5 minutes (60 * 5s)

useEffect(() => {
  if (pollAttempts >= MAX_POLL_ATTEMPTS && paymentStatus !== "PAID") {
    showNotification({
      type: "warning",
      message:
        "Chưa nhận được xác nhận thanh toán. Vui lòng liên hệ quản trị viên nếu đã chuyển khoản.",
    });
  }
}, [pollAttempts, paymentStatus]);
```

#### 3. Network Errors

```typescript
const fetchInvoiceWithRetry = async (invoiceId: number, retries = 3) => {
  for (let i = 0; i < retries; i++) {
    try {
      const response = await fetch(`/api/v1/invoices/${invoiceId}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
        },
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      if (i === retries - 1) {
        throw error;
      }

      // Exponential backoff
      await new Promise((resolve) =>
        setTimeout(resolve, Math.pow(2, i) * 1000)
      );
    }
  }
};
```

---

## 📱 Mobile Responsive Design

### Payment QR Screen

```css
/* Mobile-first approach */
@media (max-width: 640px) {
  .payment-qr-container {
    padding: 12px;
    margin: 0 8px;
  }

  .qr-header h2 {
    font-size: 20px;
  }

  .payment-code {
    font-size: 14px;
  }

  .qr-image {
    padding: 12px;
  }

  .qr-image img {
    width: 200px !important;
    height: 200px !important;
  }

  .info-row {
    font-size: 14px;
  }

  .qr-instructions ol,
  .warning-box ul {
    font-size: 14px;
    line-height: 1.6;
  }

  .warning-box {
    padding: 8px 12px;
  }
}
```

### Touch-friendly Copy Button

```typescript
const CopyPaymentCode: React.FC<{ code: string }> = ({ code }) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(code);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error("Failed to copy:", err);
    }
  };

  return (
    <button
      onClick={handleCopy}
      className="copy-button"
      aria-label="Copy payment code"
    >
      {copied ? "✅ Đã sao chép" : "📋 Sao chép mã"}
    </button>
  );
};
```

---

## 🧪 Testing Checklist

### Frontend Testing

- [ ] QR code displays correctly on desktop
- [ ] QR code displays correctly on mobile
- [ ] Payment code is visible and copyable
- [ ] Status updates automatically when payment received
- [ ] Polling stops when invoice is fully paid
- [ ] Error states handled (network error, QR load error)
- [ ] Loading states shown during API calls
- [ ] Token refresh works when access token expires
- [ ] Responsive design works on all screen sizes

### Integration Testing

- [ ] Create invoice → QR code generated
- [ ] Scan QR → Payment code included in transfer
- [ ] SePay webhook received → Invoice status updated
- [ ] Frontend polls → Latest status retrieved
- [ ] Multiple partial payments handled correctly
- [ ] Payment history displayed accurately

### Bank Transfer Testing

1. **Development Environment**:

   - Use SePay sandbox/test API
   - Test with small amounts (10,000 VND)

2. **Production Environment**:
   - Test with real bank transfer
   - Verify payment code format is correct
   - Confirm webhook received within 30 seconds
   - Verify invoice status updated in UI

---

## 🔄 Payment Code Format

### Format: `PDCMSyymmddxy`

| Component | Description      | Example                       |
| --------- | ---------------- | ----------------------------- |
| `PDCMS`   | Prefix (fixed)   | PDCMS                         |
| `yy`      | Year (2 digits)  | 25 = 2025                     |
| `mm`      | Month (2 digits) | 12 = December                 |
| `dd`      | Day (2 digits)   | 30 = Day 30                   |
| `xy`      | Sequence (01-99) | 01 = First invoice of the day |

**Examples**:

- `PDCMS25123001` → 2025-12-30, first invoice
- `PDCMS25123002` → 2025-12-30, second invoice
- `PDCMS26010199` → 2026-01-01, 99th invoice

**Sequence Rules**:

- Resets to 01 at midnight (00:00:00)
- Maximum 99 invoices per day
- If 100+ invoices created in one day, sequence wraps to 01 (rare case)

---

## 📞 Support & Troubleshooting

### Common Issues

#### Issue 1: Payment not confirming after 2 minutes

**Cause**: SePay webhook delayed or failed

**Solution**:

1. Check if customer transferred with correct payment code
2. Verify bank transfer history shows correct amount
3. Contact backend team to manually verify webhook logs
4. If needed, manually create payment record in admin panel

#### Issue 2: Wrong amount transferred

**Cause**: Customer transferred less/more than invoice amount

**Solution**:

- **Partial payment**: System automatically marks as `PARTIALLY_PAID`, remaining debt updated
- **Overpayment**: System records extra amount, admin can process refund
- **Underpayment**: Customer needs to transfer remaining amount with same payment code

#### Issue 3: Multiple transfers for one invoice

**Cause**: Customer split payment into multiple transactions

**Solution**:

- System handles this automatically
- Each webhook creates a new payment record
- `paidAmount` accumulates, `remainingDebt` recalculates
- When `remainingDebt = 0`, status changes to `PAID`

---

## 📚 Additional Resources

### Related Documentation

- [API Endpoints Reference](./API_ENDPOINTS_WITH_FUNCTION_NAMES_AND_SAMPLES.md)
- [Backend Integration Guide](./BE_4_FE_INTEGRATION_GUIDE.md)
- [SePay Webhook Documentation](https://docs.sepay.vn/tich-hop-webhooks.html)
- [VietQR API Documentation](https://vietqr.io/danh-sach-api)

### Development Tools

- **Postman Collection**: Test API endpoints
- **React DevTools**: Debug component state
- **Network Tab**: Monitor API requests and responses
- **SePay Dashboard**: View webhook history and test webhook delivery

---

## ✅ Implementation Checklist

### Phase 1: Basic Payment Display

- [ ] Fetch invoice with payment code and QR URL
- [ ] Display QR code image
- [ ] Show payment information (amount, code, bank details)
- [ ] Add payment instructions

### Phase 2: Status Polling

- [ ] Implement invoice polling hook
- [ ] Update UI when status changes
- [ ] Stop polling when fully paid
- [ ] Show payment history

### Phase 3: Error Handling

- [ ] Handle QR load errors
- [ ] Show fallback bank details
- [ ] Implement retry logic for API calls
- [ ] Display user-friendly error messages

### Phase 4: UX Improvements

- [ ] Add copy payment code button
- [ ] Show loading states
- [ ] Add payment success animation
- [ ] Implement responsive design

### Phase 5: Testing & Optimization

- [ ] Test on real devices
- [ ] Optimize polling frequency
- [ ] Add performance monitoring
- [ ] Document edge cases

---

## 🎉 Summary

This guide provides everything you need to integrate the SePay payment system into your frontend application:

1. **Simple Integration**: Just display QR code and poll for status updates
2. **Automatic Updates**: Webhook handles payment confirmation, frontend just needs to refresh
3. **Mobile-Friendly**: Responsive design works on all devices
4. **Error Resilient**: Comprehensive error handling and fallback options
5. **Production-Ready**: Security best practices and performance optimizations

For questions or issues, contact the backend team or refer to the troubleshooting section.

Happy coding! 🚀
