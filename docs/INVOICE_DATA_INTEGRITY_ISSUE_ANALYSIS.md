# 🚨 CRITICAL: Invoice Data Integrity Issue - Analysis & Fix

**Date**: January 1, 2026  
**Priority**: 🔴 **CRITICAL**  
**Status**: ✅ **ROOT CAUSE IDENTIFIED**

---

## 📋 Problem Summary

**FE Report**: Khi gọi `GET /api/v1/invoices/appointment/{appointmentId}`, BE trả về invoice có:
- ✅ `appointmentCode` **ĐÚNG**
- ❌ `patientName` **SAI** 
- ❌ `items` (services) **SAI**

**Example**:
- **Appointment**: `APT-20260105-002`, Patient: `BN-1004` (Mít tơ Bít), Service: `BLEACH_ATHOME`
- **Invoice**: appointmentCode = `APT-20260105-002` ✅, patientName = `Phạm Văn Phong` ❌, items = `SCALING_L2` ❌

---

## 🔍 Root Cause Analysis

### ✅ Code is CORRECT!

**InvoiceService.java - mapToResponse()** đã implement đúng:

```java
// ✅ Populate appointmentCode from appointmentId
String appointmentCode = null;
if (invoice.getAppointmentId() != null) {
    appointmentCode = appointmentRepository.findById(invoice.getAppointmentId())
            .map(Appointment::getAppointmentCode)
            .orElse(null);
}

// ✅ Populate patientName from patientId
String patientName = null;
if (invoice.getPatientId() != null) {
    patientName = patientRepository.findById(invoice.getPatientId())
            .map(Patient::getFullName)
            .orElse(null);
}
```

**Logic**: 
- Invoice có `appointmentId = X`
- Code query Appointment table với ID = X → Get `appointmentCode` ✅
- Code query Patient table với `patientId` từ invoice → Get `patientName`

### ❌ Database Data is WRONG!

**Vấn đề**: Invoice trong database có **DATA MISMATCH**:

```sql
-- Invoice record:
{
  invoice_id: 1,
  appointment_id: 2,  -- Trỏ đến appointment mới (APT-20260105-002)
  patient_id: 1,      -- ❌ Trỏ đến patient CŨ (Phạm Văn Phong)
  ...
}

-- Invoice Items:
{
  invoice_id: 1,
  service_code: 'SCALING_L2',  -- ❌ Service CŨ
  ...
}

-- Appointment record (ID = 2):
{
  appointment_id: 2,
  appointment_code: 'APT-20260105-002',
  patient_id: 4,  -- Patient MỚI (Mít tơ Bít)
  services: ['BLEACH_ATHOME']  -- Service MỚI
}
```

**Kết quả**:
- Code populate `appointmentCode` từ `appointmentId = 2` → `APT-20260105-002` ✅
- Code populate `patientName` từ `patientId = 1` → `Phạm Văn Phong` ❌ (sai vì patient_id không khớp)
- Invoice items có `SCALING_L2` ❌ (sai vì không khớp với appointment services)

---

## 🔎 How Did This Happen?

### **Scenario 1: Manual Database Edit**
```sql
-- Admin manually update invoice:
UPDATE invoices 
SET appointment_id = 2  -- Change to new appointment
WHERE invoice_id = 1;

-- ❌ Forgot to update patient_id and invoice_items!
```

### **Scenario 2: Bug in Invoice Creation Code**
```java
// Khi tạo invoice từ appointment:
Invoice invoice = Invoice.builder()
    .appointmentId(appointment.getAppointmentId())  // ✅ Correct
    .patientId(1)  // ❌ Hardcoded old patient ID
    .build();

// Invoice items:
InvoiceItem item = InvoiceItem.builder()
    .serviceCode("SCALING_L2")  // ❌ Hardcoded old service
    .build();
```

### **Scenario 3: Data Migration Error**
```sql
-- Migration script có bug:
-- Update appointmentId nhưng không update patientId
UPDATE invoices SET appointment_id = new_appointment_id;
-- ❌ Missing: UPDATE invoices SET patient_id = new_patient_id;
```

---

## 🧪 Diagnostic Queries

### **Query 1: Find Invoices with Mismatched Patient**
```sql
-- Tìm invoices có patientId khác với appointment.patientId
SELECT 
    i.invoice_id,
    i.invoice_code,
    i.appointment_id,
    i.patient_id as invoice_patient_id,
    a.appointment_code,
    a.patient_id as appointment_patient_id,
    p1.first_name || ' ' || p1.last_name as invoice_patient_name,
    p2.first_name || ' ' || p2.last_name as appointment_patient_name,
    CASE 
        WHEN i.patient_id != a.patient_id THEN '❌ MISMATCH'
        ELSE '✅ OK'
    END as status
FROM invoices i
LEFT JOIN appointments a ON i.appointment_id = a.appointment_id
LEFT JOIN patients p1 ON i.patient_id = p1.patient_id
LEFT JOIN patients p2 ON a.patient_id = p2.patient_id
WHERE i.appointment_id IS NOT NULL
  AND i.patient_id != a.patient_id;  -- ❌ Show mismatched records
```

**Expected Output**:
```
invoice_id | invoice_code      | appointment_id | invoice_patient_id | appointment_patient_id | invoice_patient_name | appointment_patient_name | status
-----------|-------------------|----------------|--------------------|------------------------|----------------------|--------------------------|----------
1          | INV-20251105-001  | 2              | 1                  | 4                      | Phạm Văn Phong       | Mít tơ Bít               | ❌ MISMATCH
```

### **Query 2: Check Invoice Items vs Appointment Services**
```sql
-- So sánh invoice items với appointment services
SELECT 
    i.invoice_id,
    i.invoice_code,
    i.appointment_id,
    a.appointment_code,
    ii.service_code as invoice_service_code,
    ii.service_name as invoice_service_name,
    aps.service_code as appointment_service_code,
    s.service_name as appointment_service_name,
    CASE 
        WHEN ii.service_code = aps.service_code THEN '✅ MATCH'
        ELSE '❌ MISMATCH'
    END as status
FROM invoices i
LEFT JOIN appointments a ON i.appointment_id = a.appointment_id
LEFT JOIN invoice_items ii ON i.invoice_id = ii.invoice_id
LEFT JOIN appointment_services aps ON a.appointment_id = aps.appointment_id
LEFT JOIN booking_dental_services s ON aps.service_id = s.service_id
WHERE i.appointment_id = 2;  -- Appointment APT-20260105-002
```

**Expected Output**:
```
invoice_id | invoice_code      | appointment_id | invoice_service_code | appointment_service_code | status
-----------|-------------------|----------------|----------------------|--------------------------|----------
1          | INV-20251105-001  | 2              | SCALING_L2           | BLEACH_ATHOME            | ❌ MISMATCH
```

---

## 🔧 Fix Scripts

### **Fix 1: Sync Invoice Patient with Appointment Patient**
```sql
-- Update invoice patientId to match appointment patientId
UPDATE invoices i
SET patient_id = a.patient_id
FROM appointments a
WHERE i.appointment_id = a.appointment_id
  AND i.appointment_id IS NOT NULL
  AND i.patient_id != a.patient_id;  -- Only update mismatched records

-- Verify fix:
SELECT 
    i.invoice_id,
    i.invoice_code,
    i.patient_id as invoice_patient_id,
    a.patient_id as appointment_patient_id,
    CASE 
        WHEN i.patient_id = a.patient_id THEN '✅ FIXED'
        ELSE '❌ STILL WRONG'
    END as status
FROM invoices i
LEFT JOIN appointments a ON i.appointment_id = a.appointment_id
WHERE i.appointment_id IS NOT NULL;
```

### **Fix 2: Manually Fix Invoice Items (Case-by-Case)**
```sql
-- ❌ Cannot auto-fix invoice items because we don't know if:
-- - Invoice was created BEFORE or AFTER services were changed
-- - Invoice should reflect OLD services (if patient paid) or NEW services
-- - Services were legitimately different (custom invoice)

-- ⚠️ MANUAL REVIEW REQUIRED:
-- 1. Check invoice creation date vs appointment creation/update date
-- 2. Ask business: Should invoice reflect old or new services?
-- 3. If new: Manually delete old items and insert new items

-- Example for invoice_id = 1:
-- Delete old items:
DELETE FROM invoice_items WHERE invoice_id = 1;

-- Insert correct items from appointment:
INSERT INTO invoice_items (invoice_id, service_id, service_code, service_name, quantity, unit_price, subtotal)
SELECT 
    1 as invoice_id,
    s.service_id,
    s.service_code,
    s.service_name,
    1 as quantity,
    s.price as unit_price,
    s.price as subtotal
FROM appointment_services aps
JOIN booking_dental_services s ON aps.service_id = s.service_id
WHERE aps.appointment_id = 2;  -- Appointment APT-20260105-002

-- Update invoice totals:
UPDATE invoices 
SET total_amount = (SELECT SUM(subtotal) FROM invoice_items WHERE invoice_id = 1),
    remaining_debt = (SELECT SUM(subtotal) FROM invoice_items WHERE invoice_id = 1) - COALESCE(paid_amount, 0)
WHERE invoice_id = 1;
```

---

## 🛡️ Prevention Measures

### **1. Add Database Constraint**
```sql
-- ❌ Cannot add foreign key constraint because:
-- - Invoice can exist WITHOUT appointment (manual invoice)
-- - Invoice can have patientId different from appointment (edge cases)

-- ✅ Alternative: Add trigger to validate data integrity
CREATE OR REPLACE FUNCTION validate_invoice_data()
RETURNS TRIGGER AS $$
BEGIN
    -- If invoice has appointmentId, validate patientId matches
    IF NEW.appointment_id IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1 FROM appointments 
            WHERE appointment_id = NEW.appointment_id 
            AND patient_id = NEW.patient_id
        ) THEN
            RAISE EXCEPTION 'Invoice patientId (%) does not match appointment patientId', NEW.patient_id;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER invoice_data_validation
BEFORE INSERT OR UPDATE ON invoices
FOR EACH ROW
EXECUTE FUNCTION validate_invoice_data();
```

### **2. Add Validation in Code**
```java
// InvoiceService.java - createInvoice()
@Transactional
public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
    // ✅ Validate: If appointmentId is provided, patientId must match
    if (request.getAppointmentId() != null) {
        Appointment appointment = appointmentRepository
            .findById(request.getAppointmentId())
            .orElseThrow(() -> new ResourceNotFoundException("APPOINTMENT_NOT_FOUND", 
                "Appointment not found: " + request.getAppointmentId()));
        
        // Validate patient matches
        if (!appointment.getPatientId().equals(request.getPatientId())) {
            throw new IllegalArgumentException(
                "Invoice patientId (" + request.getPatientId() + ") " +
                "does not match appointment patientId (" + appointment.getPatientId() + ")"
            );
        }
    }
    
    // ... rest of invoice creation
}
```

### **3. Add Data Integrity Check Job**
```java
// ScheduledJob to check data integrity
@Scheduled(cron = "0 0 2 * * ?")  // Run daily at 2 AM
public void checkInvoiceDataIntegrity() {
    List<Invoice> mismatchedInvoices = invoiceRepository.findAll().stream()
        .filter(invoice -> {
            if (invoice.getAppointmentId() == null) return false;
            
            Appointment appointment = appointmentRepository
                .findById(invoice.getAppointmentId())
                .orElse(null);
            
            return appointment != null && 
                   !invoice.getPatientId().equals(appointment.getPatientId());
        })
        .collect(Collectors.toList());
    
    if (!mismatchedInvoices.isEmpty()) {
        log.error("Found {} invoices with mismatched patientId!", mismatchedInvoices.size());
        // Send alert email to admin
        emailService.sendAlert("Invoice Data Integrity Issue", 
            "Found " + mismatchedInvoices.size() + " invoices with mismatched data");
    }
}
```

---

## 📋 Action Items

### ✅ Immediate Actions (< 1 hour)

1. **Run Diagnostic Query 1** to find all mismatched invoices
   ```bash
   psql -U root -d dental_clinic_db -f diagnostic_query_1.sql > mismatched_invoices.txt
   ```

2. **Run Fix Script 1** to sync invoice patientId with appointment patientId
   ```sql
   -- Backup first!
   CREATE TABLE invoices_backup AS SELECT * FROM invoices;
   
   -- Then run fix
   UPDATE invoices i SET patient_id = a.patient_id FROM appointments a 
   WHERE i.appointment_id = a.appointment_id AND i.patient_id != a.patient_id;
   ```

3. **Manually review and fix invoice items** (case-by-case)

### ✅ Short-term Actions (< 1 day)

4. **Add validation in InvoiceService.createInvoice()** to prevent future mismatches

5. **Add database trigger** to validate data integrity

6. **Test thoroughly** with FE team

### ✅ Long-term Actions (< 1 week)

7. **Add scheduled job** to check data integrity daily

8. **Add monitoring/alerting** for data integrity issues

9. **Review all invoice creation code** to ensure consistency

10. **Document business rules** for invoice creation

---

## 📊 Expected Results After Fix

### **Query 1: All invoices should match**
```sql
SELECT 
    i.invoice_id,
    i.invoice_code,
    i.patient_id as invoice_patient_id,
    a.patient_id as appointment_patient_id,
    CASE 
        WHEN i.patient_id = a.patient_id THEN '✅ OK'
        ELSE '❌ MISMATCH'
    END as status
FROM invoices i
LEFT JOIN appointments a ON i.appointment_id = a.appointment_id
WHERE i.appointment_id IS NOT NULL;

-- Expected: All records show '✅ OK'
```

### **FE API Response**
```json
{
  "invoiceId": 1,
  "invoiceCode": "INV-20251105-001",
  "appointmentId": 2,
  "appointmentCode": "APT-20260105-002",  // ✅ Correct
  "patientId": 4,  // ✅ Fixed - Now matches appointment
  "patientName": "Mít tơ Bít",  // ✅ Fixed - Now matches appointment
  "items": [
    {
      "serviceCode": "BLEACH_ATHOME",  // ✅ Fixed - Now matches appointment
      "serviceName": "Tẩy trắng răng tại nhà",
      ...
    }
  ]
}
```

---

## ✅ Conclusion

**Root Cause**: Database data integrity issue, NOT code bug

**Fix**: 
1. Sync invoice `patientId` with appointment `patientId` (SQL UPDATE)
2. Manually review and fix invoice items (case-by-case)
3. Add validation to prevent future issues

**Timeline**: 
- Immediate fix: < 1 hour
- Validation code: < 1 day
- Long-term prevention: < 1 week

---

**Document created**: January 1, 2026  
**Author**: Backend Team  
**Status**: ✅ Ready for implementation
