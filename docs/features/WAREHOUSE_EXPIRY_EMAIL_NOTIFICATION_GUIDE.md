# Warehouse Expiry Email Notification System

## Overview

Automatic daily email notification system that alerts warehouse staff about items approaching their expiry dates. Uses a **Daily Digest** strategy to consolidate all alerts into ONE professional email per day, preventing spam while maintaining quality communication.

## Strategy: Daily Digest (Quality over Quantity)

### Problem with Real-Time Notifications

If we send one email per item:

- 50 items expiring → 50 separate emails
- Result: User inbox spam → emails filtered to Spam/Trash → **system becomes useless**

### Solution: Scheduled Report

- **ONE email per day** at 8:00 AM
- Groups ALL expiring items by urgency
- Professional HTML format with color-coded sections
- Only sent to users with `VIEW_WAREHOUSE` permission

## Alert Thresholds

The system monitors items expiring at **exactly** these intervals:

| Threshold | Urgency Level | Color Code       | Description               |
| --------- | ------------- | ---------------- | ------------------------- |
| 5 days    | CRITICAL      | Red (#f44336)    | Immediate action required |
| 15 days   | WARNING       | Orange (#ff9800) | Handle this week          |
| 30 days   | INFO          | Yellow (#ffc107) | Plan ahead                |

**Note:** Items are alerted only ONCE when they reach each threshold (not every day within the range).

## Email Structure

### Subject Line

```
Bao cao vat tu het han - KHAN: 2 | CANH BAO: 5 | THONG BAO: 3
```

Shows quick counts for each urgency level.

### Email Body Sections

#### 1. CRITICAL Section (Red)

- **Background:** Light red (#ffebee)
- **Border:** Dark red (#f44336)
- **Items:** Expiring in 5 days
- **Action:** Can xu ly NGAY LAP TUC

#### 2. WARNING Section (Orange)

- **Background:** Light orange (#fff3e0)
- **Border:** Orange (#ff9800)
- **Items:** Expiring in 15 days
- **Action:** Uu tien xu ly trong tuan nay

#### 3. INFO Section (Yellow)

- **Background:** Light yellow (#fffde7)
- **Border:** Yellow (#ffc107)
- **Items:** Expiring in 30 days
- **Action:** Can chu y va lap ke hoach xu ly

### Table Columns

Each section contains a table with:

| Column     | Description              |
| ---------- | ------------------------ |
| Ma VT      | Item code                |
| Ten vat tu | Item name                |
| Lo so      | Batch/Lot number         |
| So luong   | Quantity on hand + unit  |
| Han dung   | Expiry date (dd/MM/yyyy) |
| Con lai    | Days remaining (in bold) |
| NCC        | Supplier name            |

## Recipients

Email is sent ONLY to users with the `VIEW_WAREHOUSE` permission:

Based on seed data:

- **ROLE_ADMIN** - `admin@dentalclinic.com`
- **ROLE_MANAGER** - `quan.vnm@dentalclinic.com`
- **ROLE_RECEPTIONIST** - `thuan.dkb@dentalclinic.com`

**Warehouse-specific roles only** - does not spam other users.

## Technical Implementation

### Scheduled Job

**File:** `WarehouseExpiryEmailJob.java`

```java
@Scheduled(cron = "0 0 8 * * ?")
public void sendDailyExpiryReport()
```

- **Runs:** 8:00 AM every day
- **Cron Format:** `second minute hour day-of-month month day-of-week`

### Process Flow

1. **Query Database**

   - Find batches expiring in exactly 5 days
   - Find batches expiring in exactly 15 days
   - Find batches expiring in exactly 30 days

2. **Check Alert Condition**

   - If NO items found → Skip email (don't send empty emails)
   - If items found → Proceed

3. **Fetch Recipients**

   - Query all accounts with roles: ADMIN, MANAGER, RECEPTIONIST
   - Filter by valid email addresses

4. **Generate HTML Content**

   - Group items by urgency (CRITICAL, WARNING, INFO)
   - Create color-coded sections
   - Generate responsive HTML tables

5. **Send Emails**
   - Send to each warehouse user
   - Log success/failure for each recipient

### Email Service Integration

**File:** `EmailService.java`

```java
public void sendExpiryAlertEmail(
    String toEmail,
    String username,
    String htmlContent,
    int criticalCount,
    int warningCount,
    int infoCount
)
```

- Uses `JavaMailSender` for SMTP
- Async execution (@Async)
- HTML content support (MimeMessage)

## Configuration

### SMTP Settings

**File:** `application.yaml`

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:dentalclinicPDCMS@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

**Environment Variables:**

- `MAIL_USERNAME` - Gmail address
- `MAIL_PASSWORD` - Gmail App Password (not regular password)

### Gmail Setup

1. Enable 2-Factor Authentication on Gmail
2. Generate App Password:
   - Go to: https://myaccount.google.com/apppasswords
   - Select "Mail" and your device
   - Copy generated password
   - Use as `MAIL_PASSWORD`

## Testing

### Manual Trigger (Development)

Create test controller endpoint:

```java
@PostMapping("/trigger-expiry-email")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> triggerExpiryEmail() {
    warehouseExpiryEmailJob.sendDailyExpiryReport();
    return ResponseEntity.ok("Email sent");
}
```

Then test with:

```bash
curl -X POST "http://localhost:8080/api/v1/test/trigger-expiry-email" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Wait for Scheduled Execution

1. Start application before 8:00 AM
2. Wait until 8:00 AM
3. Check logs:

   ```
   START: Warehouse Expiry Email Job
   Found expiring items - CRITICAL: 2, WARNING: 5, INFO: 3
   Found 3 warehouse users to notify
   Expiry alert email sent to: admin@dentalclinic.com
   Expiry alert email sent to: quan.vnm@dentalclinic.com
   Expiry alert email sent to: thuan.dkb@dentalclinic.com
   END: Warehouse Expiry Email Job
   ```

4. Check recipient inboxes

### Test Data Setup

To test with seed data, manually update `expiryDate` in database:

```sql
-- Set some batches to expire in 5 days
UPDATE item_batches
SET expiry_date = CURRENT_DATE + INTERVAL '5 days'
WHERE batch_id IN (1, 2);

-- Set some batches to expire in 15 days
UPDATE item_batches
SET expiry_date = CURRENT_DATE + INTERVAL '15 days'
WHERE batch_id IN (3, 4, 5);

-- Set some batches to expire in 30 days
UPDATE item_batches
SET expiry_date = CURRENT_DATE + INTERVAL '30 days'
WHERE batch_id IN (6, 7, 8);
```

## Logging

All email operations are logged:

```
INFO - START: Warehouse Expiry Email Job
INFO - Found expiring items - CRITICAL: X, WARNING: Y, INFO: Z
INFO - Found N warehouse users to notify
INFO - Expiry alert email sent to: user@example.com
ERROR - Failed to send expiry alert to user@example.com: Connection timeout
INFO - END: Warehouse Expiry Email Job
```

## Business Rules

### BR-01: Daily Digest Only

- Maximum **ONE email per day** per user
- No real-time notifications (prevents spam)

### BR-02: Exact Threshold Matching

- Items alerted only when they reach EXACTLY 5, 15, or 30 days remaining
- Prevents duplicate notifications on consecutive days

### BR-03: Quality Recipients

- Only users with `VIEW_WAREHOUSE` permission
- Only accounts with valid email addresses
- No spam to unrelated roles

### BR-04: No Empty Emails

- If no items are expiring at any threshold → Skip email
- Don't waste user attention with "all clear" messages

### BR-05: Professional Format

- Color-coded urgency (Red/Orange/Yellow)
- Responsive HTML layout
- Clear actionable information
- Proper Vietnamese character encoding (UTF-8)

## Monitoring

### Check Email Delivery

1. **Application Logs**

   ```
   grep "Expiry alert email" logs/application.log
   ```

2. **SMTP Server Logs**

   - Check Gmail "Sent" folder
   - Verify email not in "Spam"

3. **Recipient Feedback**
   - Ask warehouse staff if receiving emails
   - Verify email not filtered by corporate email rules

### Common Issues

#### Issue 1: Emails not sending

**Symptoms:** No "email sent" logs

**Solutions:**

- Check SMTP credentials (MAIL_USERNAME, MAIL_PASSWORD)
- Verify Gmail App Password (not regular password)
- Check firewall/network allows port 587
- Verify `spring.mail.host` is correct

#### Issue 2: Emails in Spam

**Symptoms:** Users don't see emails

**Solutions:**

- Ask users to mark as "Not Spam"
- Check email subject doesn't contain spam triggers
- Verify sender email domain reputation
- Consider using company email server instead of Gmail

#### Issue 3: Missing recipients

**Symptoms:** Some users not receiving

**Solutions:**

- Verify user has `VIEW_WAREHOUSE` permission
- Check account `email` field is not NULL
- Verify email address format is valid
- Check account `status` is ACTIVE

## Maintenance

### Updating Alert Thresholds

To change alert intervals (e.g., 7 days instead of 5):

1. Update `WarehouseExpiryEmailJob.java`:

   ```java
   List<ExpiryAlert> criticalAlerts = findExpiringBatches(today, 7); // Changed from 5
   ```

2. Update documentation to reflect new thresholds

### Changing Email Schedule

To run at different time (e.g., 9:00 AM):

```java
@Scheduled(cron = "0 0 9 * * ?") // Changed from 8
public void sendDailyExpiryReport()
```

### Adding More Thresholds

To add 10-day threshold:

1. Add query: `List<ExpiryAlert> urgentAlerts = findExpiringBatches(today, 10);`
2. Update email template with new section (e.g., Purple color)
3. Update subject line: `KHAN: X | URGENT: Y | CANH BAO: Z | THONG BAO: W`

## Best Practices

### DO ✅

- Send consolidated daily digests
- Use color-coding for urgency
- Include actionable information
- Log all email operations
- Test with real email accounts
- Monitor delivery success rate

### DON'T ❌

- Send real-time emails per item (spam)
- Send empty/all-clear emails
- Include sensitive cost data in emails
- Use complex email templates (compatibility issues)
- Spam users without VIEW_WAREHOUSE permission
- Forget to handle email sending failures

## Related Documentation

- **API 6.1:** Inventory Summary
- **API 6.2:** Item Batches Detail
- **API 6.3:** Expiring Alerts API
- **Email Configuration Guide:** SMTP setup and troubleshooting

## Changelog

### 2025-12-12 - Initial Implementation

- Implemented daily digest strategy
- Created WarehouseExpiryEmailJob with 8:00 AM schedule
- Added color-coded email template (Red/Orange/Yellow)
- Configured recipient filtering (VIEW_WAREHOUSE only)
- Set thresholds: 5 days (CRITICAL), 15 days (WARNING), 30 days (INFO)
- Added sendExpiryAlertEmail() method to EmailService
- Documented all business rules and testing procedures

---

**Status:** Implemented
**Version:** 1.0
**Last Updated:** 2025-12-12
**Next Review:** After first production deployment
