# 📋 API ENDPOINTS WITH FUNCTION NAMES & REQUEST BODY SAMPLES

**Project:** PDCMS_BE - Dental Clinic Management System  
**Generated:** December 11, 2025  
**Purpose:** Unit Test Case Documentation

---

## **TABLE OF CONTENTS**

- [1. Working Schedule Management (20 functions)](#1-working-schedule-management)
- [2. Warehouse & Inventory (18 functions)](#2-warehouse--inventory)
- [3. Treatment Plan Management (11 functions)](#3-treatment-plan-management)
- [4. Patient Management (12 functions)](#4-patient-management)
- [5. Appointment & Booking (10 functions)](#5-appointment--booking)
- [6. Employee Management (3 functions)](#6-employee-management)
- [7. Service Management (4 functions)](#7-service-management)
- [8. Clinical Records (7 functions)](#8-clinical-records)
- [9. Authentication & Account (6 functions)](#9-authentication--account)
- [10. Permission & Role Management (7 functions)](#10-permission--role-management)

---

## **1. WORKING SCHEDULE MANAGEMENT**

### **1.1 HOLIDAY DEFINITION**
#### **Function: `createHolidayDefinition`**
- **Feature:** Holiday Management
- **Endpoint:** `POST /api/v1/holiday-definitions`
- **Request Body:**
```json
{
  "name": "Tết Nguyên Đán 2025",
  "holidayType": "NATIONAL",
  "description": "Vietnamese Lunar New Year",
  "isRecurring": true,
  "recurrenceRule": "YEARLY"
}
```

#### **Function: `updateHolidayDefinition`**
- **Feature:** Holiday Management
- **Endpoint:** `PATCH /api/v1/holiday-definitions/{definitionId}`
- **Request Body:**
```json
{
  "name": "Tết Nguyên Đán 2025 (Cập nhật)",
  "description": "Vietnamese Lunar New Year - Extended",
  "isActive": true
}
```

---

### **1.2 HOLIDAY DATE**
#### **Function: `createHolidayDate`**
- **Feature:** Holiday Management
- **Endpoint:** `POST /api/v1/holiday-dates`
- **Request Body:**
```json
{
  "holidayDefinitionId": "HOL_NAT_01",
  "holidayDate": "2025-01-29",
  "description": "Tết Nguyên Đán - Ngày 1"
}
```

#### **Function: `updateHolidayDate`**
- **Feature:** Holiday Management
- **Endpoint:** `PATCH /api/v1/holiday-dates/{holidayDate}/definition/{definitionId}`
- **Request Body:**
```json
{
  "description": "Tết Nguyên Đán - Ngày 1 (Cập nhật)"
}
```

---

### **1.3 WORK SHIFT**
#### **Function: `createWorkShift`**
- **Feature:** Work Shift Management
- **Endpoint:** `POST /api/v1/work-shifts`
- **Request Body:**
```json
{
  "workShiftId": "WKS_MORNING_01",
  "shiftName": "Ca Sáng",
  "startTime": "08:00:00",
  "endTime": "12:00:00",
  "category": "NORMAL",
  "allowedMinutesLate": 15,
  "isActive": true
}
```

#### **Function: `updateWorkShift`**
- **Feature:** Work Shift Management
- **Endpoint:** `PATCH /api/v1/work-shifts/{workShiftId}`
- **Request Body:**
```json
{
  "shiftName": "Ca Sáng (Cập nhật)",
  "startTime": "08:30:00",
  "endTime": "12:30:00",
  "allowedMinutesLate": 10
}
```

---

### **1.4 PART-TIME SLOT**
#### **Function: `createSlot`**
- **Feature:** Part-Time Slot Management
- **Endpoint:** `POST /api/v1/work-slots`
- **Request Body:**
```json
{
  "workShiftId": "WKS_MORNING_01",
  "dayOfWeek": "MONDAY",
  "quota": 10,
  "effectiveFrom": "2025-01-01",
  "effectiveTo": "2025-12-31",
  "isActive": true
}
```

#### **Function: `updateSlot`**
- **Feature:** Part-Time Slot Management
- **Endpoint:** `PUT /api/v1/work-slots/{slotId}`
- **Request Body:**
```json
{
  "quota": 15,
  "isActive": true
}
```

---

### **1.5 FIXED SHIFT REGISTRATION**
#### **Function: `createFixedRegistration`**
- **Feature:** Fixed Shift Registration
- **Endpoint:** `POST /api/v1/fixed-registrations`
- **Request Body:**
```json
{
  "employeeId": 5,
  "workShiftIds": ["WKS_MORNING_01", "WKS_AFTERNOON_01"],
  "registrationDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
  "effectiveFrom": "2025-01-01",
  "effectiveTo": "2025-12-31"
}
```

#### **Function: `updateFixedRegistration`**
- **Feature:** Fixed Shift Registration
- **Endpoint:** `PATCH /api/v1/fixed-registrations/{registrationId}`
- **Request Body:**
```json
{
  "workShiftIds": ["WKS_MORNING_01"],
  "registrationDays": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "effectiveTo": "2025-06-30"
}
```

---

### **1.6 PART-TIME FLEX REGISTRATION (EMPLOYEE)**
#### **Function: `createRegistration`**
- **Feature:** Part-Time Flex Registration
- **Endpoint:** `POST /api/v1/registrations/part-time-flex`
- **Request Body:**
```json
{
  "slotId": 1,
  "effectiveFrom": "2025-01-01",
  "effectiveTo": "2025-03-31",
  "notes": "Request for part-time shifts"
}
```

#### **Function: `updateEffectiveTo`**
- **Feature:** Part-Time Flex Registration
- **Endpoint:** `PATCH /api/v1/registrations/part-time-flex/{registrationId}/effective-to`
- **Request Body:**
```json
{
  "newEffectiveTo": "2025-06-30"
}
```

---

### **1.7 PART-TIME FLEX REGISTRATION (ADMIN)**
#### **Function: `updateStatus`**
- **Feature:** Part-Time Registration Admin
- **Endpoint:** `PATCH /api/v1/admin/registrations/part-time-flex/{registrationId}/status`
- **Request Body:**
```json
{
  "status": "APPROVED",
  "reason": null
}
```
**OR (for rejection):**
```json
{
  "status": "REJECTED",
  "reason": "Không đủ quota cho tháng này"
}
```

#### **Function: `bulkApprove`**
- **Feature:** Part-Time Registration Admin
- **Endpoint:** `POST /api/v1/admin/registrations/part-time-flex/bulk-approve`
- **Request Body:**
```json
{
  "registrationIds": [101, 102, 103, 104]
}
```

---

### **1.8 EMPLOYEE SHIFT**
#### **Function: `createManualShift`**
- **Feature:** Employee Shift Management
- **Endpoint:** `POST /api/v1/shifts`
- **Request Body:**
```json
{
  "employeeId": 5,
  "workShiftId": "WKS_MORNING_01",
  "workDate": "2025-12-15",
  "status": "SCHEDULED"
}
```

#### **Function: `updateShift`**
- **Feature:** Employee Shift Management
- **Endpoint:** `PATCH /api/v1/shifts/{id}`
- **Request Body:**
```json
{
  "status": "COMPLETED",
  "actualCheckIn": "08:05:00",
  "actualCheckOut": "12:00:00",
  "notes": "Completed shift on time"
}
```

---

### **1.9 SHIFT RENEWAL (EMPLOYEE)**
#### **Function: `respondToRenewal`**
- **Feature:** Shift Renewal
- **Endpoint:** `PATCH /api/v1/registrations/renewals/{renewal_id}/respond`
- **Request Body:**
```json
{
  "action": "CONFIRMED"
}
```
**OR (for decline):**
```json
{
  "action": "DECLINED",
  "declineReason": "Planning to change working hours"
}
```

---

### **1.10 SHIFT RENEWAL (ADMIN)**
#### **Function: `finalizeRenewal`**
- **Feature:** Admin Shift Renewal
- **Endpoint:** `POST /api/v1/admin/registrations/renewals/finalize`
- **Request Body:**
```json
{
  "renewalRequestId": "SRR_20251201_00001",
  "newEffectiveTo": "2026-12-31"
}
```

---

### **1.11 OVERTIME REQUEST**
#### **Function: `createOvertimeRequest`**
- **Feature:** Overtime Management
- **Endpoint:** `POST /api/v1/overtime-requests`
- **Request Body (Employee self-request):**
```json
{
  "workDate": "2025-12-15",
  "workShiftId": "WKS_NIGHT_01",
  "reason": "Complete month-end reports"
}
```
**OR (Admin creates for employee):**
```json
{
  "employeeId": 5,
  "workDate": "2025-12-15",
  "workShiftId": "WKS_NIGHT_01",
  "reason": "Urgent patient care required"
}
```

#### **Function: `updateOvertimeStatus`**
- **Feature:** Overtime Management
- **Endpoint:** `PATCH /api/v1/overtime-requests/{requestId}`
- **Request Body (Approve):**
```json
{
  "status": "APPROVED"
}
```
**OR (Reject):**
```json
{
  "status": "REJECTED",
  "reason": "Budget constraints"
}
```
**OR (Cancel):**
```json
{
  "status": "CANCELLED",
  "reason": "Change of plans"
}
```

---

### **1.12 TIME-OFF TYPE (ADMIN)**
#### **Function: `createTimeOffType`**
- **Feature:** Time-Off Type Management
- **Endpoint:** `POST /api/v1/admin/time-off-types`
- **Request Body:**
```json
{
  "typeCode": "UNPAID_LEAVE",
  "typeName": "Nghỉ không lương",
  "isPaid": false,
  "isActive": true
}
```

#### **Function: `updateTimeOffType`**
- **Feature:** Time-Off Type Management
- **Endpoint:** `PATCH /api/v1/admin/time-off-types/{type_id}`
- **Request Body:**
```json
{
  "typeName": "Nghỉ không lương (Việc riêng)",
  "isActive": true
}
```

---

### **1.13 TIME-OFF REQUEST**
#### **Function: `createRequest`**
- **Feature:** Time-Off Request Management
- **Endpoint:** `POST /api/v1/time-off-requests`
- **Request Body:**
```json
{
  "employeeId": 5,
  "timeOffTypeId": "ANNUAL_LEAVE",
  "startDate": "2025-12-20",
  "endDate": "2025-12-22",
  "reason": "Family vacation",
  "slotId": null
}
```
**OR (Half-day leave):**
```json
{
  "employeeId": 5,
  "timeOffTypeId": "ANNUAL_LEAVE",
  "startDate": "2025-12-20",
  "endDate": "2025-12-20",
  "reason": "Personal appointment",
  "slotId": 1
}
```

#### **Function: `updateRequestStatus`**
- **Feature:** Time-Off Request Management
- **Endpoint:** `PATCH /api/v1/time-off-requests/{requestId}`
- **Request Body (Approve):**
```json
{
  "status": "APPROVED"
}
```
**OR (Reject):**
```json
{
  "status": "REJECTED",
  "reason": "Already at max capacity for this period"
}
```

---

### **1.14 LEAVE BALANCE (ADMIN)**
#### **Function: `adjustLeaveBalance`**
- **Feature:** Leave Balance Management
- **Endpoint:** `POST /api/v1/admin/leave-balances/adjust`
- **Request Body:**
```json
{
  "employeeId": 5,
  "timeOffTypeId": "ANNUAL_LEAVE",
  "cycleYear": 2025,
  "changeAmount": 2.0,
  "notes": "Bonus leave for excellent performance"
}
```

#### **Function: `annualReset`**
- **Feature:** Leave Balance Management
- **Endpoint:** `POST /api/v1/admin/leave-balances/annual-reset`
- **Request Body:**
```json
{
  "targetYear": 2026,
  "defaultAnnualLeaveDays": 12.0
}
```

---

## **2. WAREHOUSE & INVENTORY**

### **2.1 ITEM MASTER**
#### **Function: `createItemMaster`**
- **Feature:** Item Master Management
- **Endpoint:** `POST /api/v1/warehouse/items`
- **Request Body:**
```json
{
  "itemCode": "MED-AMOXICILLIN-500",
  "itemName": "Amoxicillin 500mg",
  "description": "Antibiotic for bacterial infections",
  "categoryId": 1,
  "warehouseType": "NORMAL",
  "prescriptionRequired": true,
  "minStockLevel": 100,
  "maxStockLevel": 1000,
  "defaultShelfLifeDays": 730,
  "units": [
    {
      "unitName": "Box",
      "conversionRate": 100,
      "isBaseUnit": false,
      "displayOrder": 1
    },
    {
      "unitName": "Strip",
      "conversionRate": 10,
      "isBaseUnit": false,
      "displayOrder": 2
    },
    {
      "unitName": "Pill",
      "conversionRate": 1,
      "isBaseUnit": true,
      "displayOrder": 3
    }
  ]
}
```

#### **Function: `updateItemMaster`**
- **Feature:** Item Master Management
- **Endpoint:** `PUT /api/v1/warehouse/items/{id}`
- **Request Body:**
```json
{
  "itemName": "Amoxicillin 500mg (Updated)",
  "description": "Antibiotic for bacterial infections - Updated formula",
  "categoryId": 1,
  "minStockLevel": 150,
  "maxStockLevel": 1200,
  "units": [
    {
      "itemUnitId": 101,
      "unitName": "Carton",
      "conversionRate": 100,
      "isBaseUnit": false,
      "isActive": true,
      "displayOrder": 1
    },
    {
      "unitName": "Pallet",
      "conversionRate": 1000,
      "isBaseUnit": false,
      "isActive": true,
      "displayOrder": 0
    }
  ]
}
```

#### **Function: `convertUnits`**
- **Feature:** Item Unit Conversion
- **Endpoint:** `POST /api/v1/warehouse/items/units/convert`
- **Request Body:**
```json
{
  "conversions": [
    {
      "itemMasterId": 24,
      "fromUnitId": 101,
      "fromQuantity": 5,
      "toUnitId": 103
    },
    {
      "itemMasterId": 25,
      "fromUnitId": 105,
      "fromQuantity": 10,
      "toUnitId": 106
    }
  ]
}
```

---

### **2.2 SUPPLIER**
#### **Function: `createSupplier`**
- **Feature:** Supplier Management
- **Endpoint:** `POST /api/v1/warehouse/suppliers`
- **Request Body:**
```json
{
  "supplierName": "ABC Medical Supplies",
  "phone": "0901234567",
  "email": "contact@abcmedical.com",
  "address": "123 Nguyen Hue, District 1, HCMC",
  "notes": "Reliable supplier with good quality products",
  "isBlacklisted": false
}
```

#### **Function: `updateSupplier`**
- **Feature:** Supplier Management
- **Endpoint:** `PUT /api/v1/warehouse/suppliers/{id}`
- **Request Body:**
```json
{
  "supplierName": "ABC Medical Supplies Ltd.",
  "phone": "0901234567",
  "email": "sales@abcmedical.com",
  "address": "123 Nguyen Hue, District 1, HCMC (New warehouse)",
  "notes": "Updated contact information",
  "isActive": true,
  "isBlacklisted": false
}
```

---

### **2.3 WAREHOUSE TRANSACTIONS**
#### **Function: `createImportTransaction`**
- **Feature:** Warehouse Import
- **Endpoint:** `POST /api/v1/warehouse/import`
- **Request Body:**
```json
{
  "invoiceNumber": "INV-2025-001",
  "supplierId": 1,
  "expectedDeliveryDate": "2025-12-15",
  "notes": "Routine monthly order",
  "items": [
    {
      "itemMasterId": 24,
      "inputUnitId": 101,
      "quantity": 10,
      "purchasePrice": 250000,
      "lotNumber": "LOT-2025-001",
      "expiryDate": "2027-12-01"
    },
    {
      "itemMasterId": 25,
      "inputUnitId": 105,
      "quantity": 5,
      "purchasePrice": 150000,
      "lotNumber": "LOT-2025-002",
      "expiryDate": "2026-12-01"
    }
  ]
}
```

#### **Function: `approveTransaction`**
- **Feature:** Transaction Approval
- **Endpoint:** `POST /api/v1/warehouse/transactions/{id}/approve`
- **Request Body:**
```json
{
  "notes": "Verified and approved by warehouse manager"
}
```

#### **Function: `rejectTransaction`**
- **Feature:** Transaction Rejection
- **Endpoint:** `POST /api/v1/warehouse/transactions/{id}/reject`
- **Request Body:**
```json
{
  "reason": "Incorrect quantities received"
}
```

#### **Function: `cancelTransaction`**
- **Feature:** Transaction Cancellation
- **Endpoint:** `POST /api/v1/warehouse/transactions/{id}/cancel`
- **Request Body:**
```json
{
  "reason": "Supplier delayed delivery indefinitely"
}
```

---

### **2.4 SERVICE CONSUMABLES**
#### **Function: `setServiceConsumables`**
- **Feature:** Service Consumables (BOM)
- **Endpoint:** `POST /api/v1/warehouse/consumables`
- **Request Body:**
```json
{
  "serviceConsumables": [
    {
      "serviceId": 5,
      "itemMasterId": 24,
      "quantity": 2,
      "unitId": 103,
      "notes": "Per procedure"
    },
    {
      "serviceId": 5,
      "itemMasterId": 25,
      "quantity": 1,
      "unitId": 106,
      "notes": "Single use"
    }
  ]
}
```

#### **Function: `updateServiceConsumables`**
- **Feature:** Service Consumables (BOM)
- **Endpoint:** `PUT /api/v1/warehouse/consumables/services/{serviceId}`
- **Request Body:**
```json
{
  "consumables": [
    {
      "itemMasterId": 24,
      "quantity": 3,
      "unitId": 103,
      "notes": "Increased usage per procedure"
    }
  ]
}
```

---

### **2.5 INVENTORY (LEGACY)**
#### **Function: `createItemMaster` (Inventory)**
- **Feature:** Inventory Management (Legacy)
- **Endpoint:** `POST /api/v1/inventory/item-master`
- **Request Body:**
```json
{
  "itemCode": "SUPP-COTTON-SWAB",
  "itemName": "Cotton Swabs",
  "categoryId": 2,
  "unitPrice": 5000,
  "minStockLevel": 50,
  "maxStockLevel": 500
}
```

#### **Function: `updateItemMaster` (Inventory)**
- **Feature:** Inventory Management (Legacy)
- **Endpoint:** `PUT /api/v1/inventory/item-master/{id}`
- **Request Body:**
```json
{
  "itemName": "Cotton Swabs (Medical Grade)",
  "unitPrice": 5500,
  "minStockLevel": 100
}
```

#### **Function: `createCategory`**
- **Feature:** Category Management
- **Endpoint:** `POST /api/v1/inventory/categories`
- **Request Body:**
```json
{
  "categoryName": "Dental Tools",
  "description": "Various dental equipment and tools"
}
```

#### **Function: `updateCategory`**
- **Feature:** Category Management
- **Endpoint:** `PUT /api/v1/inventory/categories/{id}`
- **Request Body:**
```json
{
  "categoryName": "Dental Tools & Equipment",
  "description": "Professional dental equipment and tools"
}
```

#### **Function: `createImportTransaction` (Inventory)**
- **Feature:** Import Transaction (Legacy)
- **Endpoint:** `POST /api/v1/inventory/import`
- **Request Body:**
```json
{
  "supplierId": 1,
  "items": [
    {
      "itemMasterId": 24,
      "quantity": 100,
      "unitPrice": 25000
    }
  ],
  "notes": "Monthly stock replenishment"
}
```

#### **Function: `createExportTransaction`**
- **Feature:** Export Transaction (Legacy)
- **Endpoint:** `POST /api/v1/inventory/export`
- **Request Body:**
```json
{
  "items": [
    {
      "itemMasterId": 24,
      "quantity": 10,
      "reason": "Used for patient treatments"
    }
  ],
  "notes": "Daily usage export"
}
```

---

## **3. TREATMENT PLAN MANAGEMENT**

### **3.1 TREATMENT PLAN CREATION**
#### **Function: `createTreatmentPlan` (from template)**
- **Feature:** Treatment Plan Creation
- **Endpoint:** `POST /api/v1/patients/{patientCode}/treatment-plans`
- **Request Body:**
```json
{
  "sourceTemplateCode": "TPL_ORTHO_METAL",
  "doctorEmployeeCode": "NV-2001",
  "discount": 1000000,
  "paymentType": "INSTALLMENT"
}
```

#### **Function: `createCustomTreatmentPlan`**
- **Feature:** Treatment Plan Creation
- **Endpoint:** `POST /api/v1/patients/{patientCode}/treatment-plans/custom`
- **Request Body:**
```json
{
  "planName": "Custom Orthodontics Treatment",
  "doctorEmployeeCode": "NV-2001",
  "discount": 500000,
  "paymentType": "FULL_PAYMENT",
  "phases": [
    {
      "phaseNumber": 1,
      "phaseName": "Initial Assessment",
      "estimatedDurationDays": 30,
      "items": [
        {
          "serviceId": 5,
          "quantity": 1,
          "customPrice": 500000,
          "notes": "Initial consultation and diagnosis"
        }
      ]
    },
    {
      "phaseNumber": 2,
      "phaseName": "Treatment Phase",
      "estimatedDurationDays": 180,
      "items": [
        {
          "serviceId": 10,
          "quantity": 1,
          "customPrice": 15000000,
          "notes": "Main orthodontic treatment"
        }
      ]
    }
  ]
}
```

---

### **3.2 TREATMENT PLAN MANAGEMENT**
#### **Function: `updateItemStatus`**
- **Feature:** Treatment Plan Item Status
- **Endpoint:** `PATCH /api/v1/patient-plan-items/{itemId}/status`
- **Request Body:**
```json
{
  "status": "IN_PROGRESS"
}
```

#### **Function: `addItemsToPhase`**
- **Feature:** Treatment Plan Phase Items
- **Endpoint:** `POST /api/v1/patient-plan-phases/{phaseId}/items`
- **Request Body:**
```json
{
  "items": [
    {
      "serviceId": 12,
      "quantity": 1,
      "customPrice": 800000,
      "notes": "Additional cleaning service"
    },
    {
      "serviceId": 15,
      "quantity": 2,
      "customPrice": 300000,
      "notes": "Follow-up consultations"
    }
  ]
}
```

#### **Function: `submitForApproval`**
- **Feature:** Treatment Plan Approval
- **Endpoint:** `PATCH /api/v1/patient-treatment-plans/{planCode}/approval`
- **Request Body:**
```json
{
  "notes": "Ready for director approval"
}
```

#### **Function: `approveTreatmentPlan`**
- **Feature:** Treatment Plan Approval
- **Endpoint:** `POST /api/v1/treatment-plans/{planCode}/approve`
- **Request Body:**
```json
{
  "approvalNotes": "Approved by medical director",
  "effectiveDate": "2025-12-15"
}
```

#### **Function: `updatePlanItem`**
- **Feature:** Treatment Plan Item Update
- **Endpoint:** `PATCH /api/v1/patient-plan-items/{itemId}`
- **Request Body:**
```json
{
  "quantity": 2,
  "customPrice": 600000,
  "notes": "Updated quantity and price after consultation"
}
```

#### **Function: `submitForReview`**
- **Feature:** Treatment Plan Review
- **Endpoint:** `PATCH /api/v1/patient-treatment-plans/{planCode}/submit-for-review`
- **Request Body:**
```json
{
  "reviewNotes": "Please review updated pricing"
}
```

#### **Function: `updatePlanPrices`**
- **Feature:** Treatment Plan Price Update
- **Endpoint:** `PATCH /api/v1/patient-treatment-plans/{planCode}/prices`
- **Request Body:**
```json
{
  "itemPrices": [
    {
      "itemId": 1001,
      "newPrice": 750000
    },
    {
      "itemId": 1002,
      "newPrice": 1200000
    }
  ],
  "discount": 500000
}
```

#### **Function: `reorderPhaseItems`**
- **Feature:** Treatment Plan Item Ordering
- **Endpoint:** `PATCH /api/v1/patient-plan-phases/{phaseId}/items/reorder`
- **Request Body:**
```json
{
  "itemOrders": [
    {
      "itemId": 1001,
      "displayOrder": 1
    },
    {
      "itemId": 1002,
      "displayOrder": 2
    },
    {
      "itemId": 1003,
      "displayOrder": 3
    }
  ]
}
```

#### **Function: `assignDoctorToItem`**
- **Feature:** Treatment Plan Doctor Assignment
- **Endpoint:** `PUT /api/v1/patient-plan-items/{itemId}/assign-doctor`
- **Request Body:**
```json
{
  "doctorEmployeeCode": "NV-2005",
  "notes": "Assigned specialist for this procedure"
}
```

---

## **4. PATIENT MANAGEMENT**

### **4.1 PATIENT CORE**
#### **Function: `createPatient`**
- **Feature:** Patient Management
- **Endpoint:** `POST /api/v1/patients`
- **Request Body:**
```json
{
  "fullName": "Nguyễn Văn A",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "phoneNumber": "0901234567",
  "email": "nguyenvana@email.com",
  "address": "123 Nguyen Hue, District 1, HCMC",
  "idCard": "001234567890",
  "occupation": "Engineer",
  "emergencyContact": {
    "name": "Nguyễn Thị B",
    "relationship": "Wife",
    "phone": "0907654321"
  },
  "medicalHistory": {
    "allergies": ["Penicillin"],
    "chronicDiseases": ["Hypertension"],
    "currentMedications": ["Lisinopril 10mg daily"]
  }
}
```

#### **Function: `updatePatient` (PATCH)**
- **Feature:** Patient Management
- **Endpoint:** `PATCH /api/v1/patients/{patientCode}`
- **Request Body:**
```json
{
  "phoneNumber": "0912345678",
  "email": "nguyenvana.new@email.com",
  "address": "456 Le Loi, District 3, HCMC"
}
```

#### **Function: `replacePatient` (PUT)**
- **Feature:** Patient Management
- **Endpoint:** `PUT /api/v1/patients/{patientCode}`
- **Request Body:**
```json
{
  "fullName": "Nguyễn Văn A",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "phoneNumber": "0912345678",
  "email": "nguyenvana.new@email.com",
  "address": "456 Le Loi, District 3, HCMC",
  "idCard": "001234567890",
  "occupation": "Senior Engineer",
  "emergencyContact": {
    "name": "Nguyễn Thị B",
    "relationship": "Wife",
    "phone": "0907654321"
  }
}
```

#### **Function: `updateToothStatusWithPathParam`**
- **Feature:** Tooth Status Update
- **Endpoint:** `PUT /api/v1/patients/{patientCode}/tooth-status/{toothNumber}`
- **Request Body:**
```json
{
  "status": "MISSING",
  "notes": "Extracted due to severe decay"
}
```

#### **Function: `updateToothStatus`**
- **Feature:** Tooth Status Update
- **Endpoint:** `PUT /api/v1/patients/{patientCode}/tooth-status`
- **Request Body:**
```json
{
  "toothNumber": "16",
  "status": "FILLED",
  "notes": "Filled with composite resin"
}
```

#### **Function: `unbanPatient`**
- **Feature:** Patient Unban
- **Endpoint:** `POST /api/v1/patients/{patientCode}/unban`
- **Request Body:**
```json
{
  "reason": "Patient has settled all outstanding payments",
  "notes": "Approved by clinic manager"
}
```

#### **Function: `blacklistPatient`**
- **Feature:** Patient Blacklist
- **Endpoint:** `POST /api/v1/patients/{patientCode}/blacklist`
- **Request Body:**
```json
{
  "reason": "Repeated no-show without notification",
  "notes": "3 consecutive no-shows in November 2025"
}
```

---

### **4.2 PATIENT IMAGES**
#### **Function: `createPatientImage`**
- **Feature:** Patient Image Management
- **Endpoint:** `POST /api/v1/patient-images`
- **Request Body:**
```json
{
  "patientCode": "BN-20251201-001",
  "imageType": "X_RAY",
  "imageUrl": "https://storage.example.com/images/xray-001.jpg",
  "takenDate": "2025-12-10",
  "description": "Panoramic X-ray - Initial consultation",
  "toothNumber": null
}
```

#### **Function: `updatePatientImage`**
- **Feature:** Patient Image Management
- **Endpoint:** `PUT /api/v1/patient-images/{imageId}`
- **Request Body:**
```json
{
  "imageType": "X_RAY",
  "description": "Panoramic X-ray - Initial consultation (Updated)",
  "toothNumber": "16"
}
```

---

### **4.3 CONTACT HISTORY**
#### **Function: `addHistory`**
- **Feature:** Contact History
- **Endpoint:** `POST /api/v1/contact-history`
- **Request Body:**
```json
{
  "patientCode": "BN-20251201-001",
  "contactType": "PHONE_CALL",
  "contactDate": "2025-12-10T14:30:00",
  "contactedBy": "NV-1001",
  "notes": "Called to confirm appointment for tomorrow",
  "outcome": "CONFIRMED"
}
```

---

### **4.4 CUSTOMER CONTACTS**
#### **Function: `createContact`**
- **Feature:** Customer Contact Management
- **Endpoint:** `POST /api/v1/customer-contacts`
- **Request Body:**
```json
{
  "fullName": "Trần Thị C",
  "phoneNumber": "0923456789",
  "email": "tranthic@email.com",
  "source": "WEBSITE",
  "interestedServices": ["Orthodontics", "Teeth Whitening"],
  "notes": "Interested in invisible braces"
}
```

#### **Function: `updateContact`**
- **Feature:** Customer Contact Management
- **Endpoint:** `PUT /api/v1/customer-contacts/{contactId}`
- **Request Body:**
```json
{
  "fullName": "Trần Thị C (Updated)",
  "phoneNumber": "0923456789",
  "email": "tranthic.new@email.com",
  "status": "CONTACTED",
  "notes": "Had initial consultation call"
}
```

#### **Function: `assignContact`**
- **Feature:** Customer Contact Assignment
- **Endpoint:** `POST /api/v1/customer-contacts/{contactId}/assign`
- **Request Body:**
```json
{
  "receptionistEmployeeCode": "NV-1002"
}
```

#### **Function: `convertContact`**
- **Feature:** Customer Contact Conversion
- **Endpoint:** `POST /api/v1/customer-contacts/{contactId}/convert-to-patient`
- **Request Body:**
```json
{
  "additionalInfo": {
    "dateOfBirth": "1995-05-20",
    "gender": "FEMALE",
    "idCard": "002345678901",
    "address": "789 Tran Hung Dao, District 5, HCMC"
  }
}
```

---

## **5. APPOINTMENT & BOOKING**

### **5.1 APPOINTMENTS**
#### **Function: `createAppointment`**
- **Feature:** Appointment Management
- **Endpoint:** `POST /api/v1/appointments`
- **Request Body:**
```json
{
  "patientCode": "BN-20251201-001",
  "doctorEmployeeCode": "NV-2001",
  "serviceId": 5,
  "appointmentDate": "2025-12-15",
  "startTime": "09:00",
  "duration": 60,
  "roomId": 1,
  "notes": "Patient prefers morning appointments",
  "paymentMethod": "CASH"
}
```

#### **Function: `updateAppointmentStatus`**
- **Feature:** Appointment Status Update
- **Endpoint:** `PATCH /api/v1/appointments/{appointmentCode}/status`
- **Request Body:**
```json
{
  "status": "CONFIRMED",
  "notes": "Patient confirmed via phone call"
}
```

#### **Function: `delayAppointment`**
- **Feature:** Appointment Delay
- **Endpoint:** `PATCH /api/v1/appointments/{appointmentCode}/delay`
- **Request Body:**
```json
{
  "delayMinutes": 30,
  "reason": "Previous appointment running late"
}
```

#### **Function: `rescheduleAppointment`**
- **Feature:** Appointment Reschedule
- **Endpoint:** `POST /api/v1/appointments/{appointmentCode}/reschedule`
- **Request Body:**
```json
{
  "newDate": "2025-12-20",
  "newStartTime": "14:00",
  "reason": "Patient requested due to work conflict"
}
```

---

### **5.2 SERVICES**
#### **Function: `createService`**
- **Feature:** Service Management
- **Endpoint:** `POST /api/v1/booking/services`
- **Request Body:**
```json
{
  "serviceName": "Dental Cleaning",
  "serviceCategoryId": 1,
  "basePrice": 500000,
  "estimatedDuration": 30,
  "description": "Professional dental cleaning and polishing",
  "isActive": true
}
```

#### **Function: `updateService`**
- **Feature:** Service Management
- **Endpoint:** `PUT /api/v1/booking/services/{serviceId}`
- **Request Body:**
```json
{
  "serviceName": "Dental Cleaning (Deep Clean)",
  "basePrice": 650000,
  "estimatedDuration": 45,
  "description": "Deep cleaning with scaling",
  "isActive": true
}
```

#### **Function: `toggleServiceActive`**
- **Feature:** Service Status Toggle
- **Endpoint:** `PATCH /api/v1/booking/services/{serviceId}/toggle-active`
- **Request Body:**
```json
{
  "isActive": false
}
```

---

### **5.3 ROOMS**
#### **Function: `createRoom`**
- **Feature:** Room Management
- **Endpoint:** `POST /api/v1/rooms`
- **Request Body:**
```json
{
  "roomCode": "ROOM-01",
  "roomName": "Treatment Room 1",
  "roomType": "TREATMENT",
  "capacity": 1,
  "floor": 2,
  "isActive": true,
  "equipment": [
    "Dental chair",
    "X-ray viewer",
    "Sterilization unit",
    "LED light"
  ]
}
```

#### **Function: `updateRoom`**
- **Feature:** Room Management
- **Endpoint:** `PUT /api/v1/rooms/{roomId}`
- **Request Body:**
```json
{
  "roomName": "Treatment Room 1 (VIP)",
  "roomType": "TREATMENT",
  "capacity": 1,
  "floor": 2,
  "isActive": true,
  "equipment": [
    "Dental chair",
    "Digital X-ray",
    "Advanced sterilization unit",
    "LED light",
    "Intraoral camera"
  ]
}
```

#### **Function: `updateRoomServices`**
- **Feature:** Room Service Assignment
- **Endpoint:** `PUT /api/v1/rooms/{roomId}/services`
- **Request Body:**
```json
{
  "serviceIds": [5, 10, 12, 15]
}
```

---

## **6. EMPLOYEE MANAGEMENT**

#### **Function: `createEmployee`**
- **Feature:** Employee Management
- **Endpoint:** `POST /api/v1/employees`
- **Request Body:**
```json
{
  "fullName": "Dr. Nguyễn Văn C",
  "dateOfBirth": "1985-05-20",
  "gender": "MALE",
  "phoneNumber": "0912345678",
  "email": "doctor.nguyenvanc@dental.com",
  "address": "456 Le Loi, District 3, HCMC",
  "idCard": "001987654321",
  "employeeType": "DOCTOR",
  "specializationId": 1,
  "startDate": "2025-01-01",
  "salary": 25000000,
  "account": {
    "username": "dr.nguyenvanc",
    "password": "SecurePassword123!",
    "email": "doctor.nguyenvanc@dental.com"
  }
}
```

#### **Function: `updateEmployee` (PATCH)**
- **Feature:** Employee Management
- **Endpoint:** `PATCH /api/v1/employees/{employeeCode}`
- **Request Body:**
```json
{
  "phoneNumber": "0913456789",
  "email": "dr.nguyenvanc.new@dental.com",
  "salary": 27000000
}
```

#### **Function: `replaceEmployee` (PUT)**
- **Feature:** Employee Management
- **Endpoint:** `PUT /api/v1/employees/{employeeCode}`
- **Request Body:**
```json
{
  "fullName": "Dr. Nguyễn Văn C",
  "dateOfBirth": "1985-05-20",
  "gender": "MALE",
  "phoneNumber": "0913456789",
  "email": "dr.nguyenvanc.new@dental.com",
  "address": "789 Vo Van Tan, District 3, HCMC",
  "idCard": "001987654321",
  "employeeType": "DOCTOR",
  "specializationId": 1,
  "salary": 27000000,
  "isActive": true
}
```

---

## **7. SERVICE MANAGEMENT**

### **7.1 SERVICE CATEGORIES**
#### **Function: `createCategory`**
- **Feature:** Service Category Management
- **Endpoint:** `POST /api/v1/service-categories`
- **Request Body:**
```json
{
  "categoryName": "Orthodontics",
  "description": "Teeth alignment and braces services",
  "displayOrder": 1,
  "isActive": true
}
```

#### **Function: `updateCategory`**
- **Feature:** Service Category Management
- **Endpoint:** `PATCH /api/v1/service-categories/{categoryId}`
- **Request Body:**
```json
{
  "categoryName": "Orthodontics & Braces",
  "description": "Comprehensive teeth alignment services",
  "displayOrder": 1
}
```

#### **Function: `reorderCategories`**
- **Feature:** Service Category Ordering
- **Endpoint:** `POST /api/v1/service-categories/reorder`
- **Request Body:**
```json
{
  "orders": [
    {
      "categoryId": 1,
      "displayOrder": 1
    },
    {
      "categoryId": 2,
      "displayOrder": 2
    },
    {
      "categoryId": 3,
      "displayOrder": 3
    }
  ]
}
```

---

## **8. CLINICAL RECORDS**

### **8.1 CLINICAL RECORD CORE**
#### **Function: `createClinicalRecord`**
- **Feature:** Clinical Record Management
- **Endpoint:** `POST /api/v1/clinical-records`
- **Request Body:**
```json
{
  "appointmentCode": "APT-20251211-001",
  "chiefComplaint": "Severe toothache upper right molar",
  "diagnosis": "Acute pulpitis - Tooth #16",
  "treatmentProvided": "Root canal therapy initiated, access cavity prepared",
  "vitalSigns": {
    "bloodPressure": "120/80",
    "heartRate": 72,
    "temperature": 36.5,
    "respiratoryRate": 16
  },
  "procedures": [
    {
      "serviceId": 5,
      "toothNumber": "16",
      "surface": "OCCLUSAL",
      "notes": "Access cavity prepared, pulp chamber cleaned"
    }
  ],
  "prescriptions": [
    {
      "medicationName": "Amoxicillin 500mg",
      "dosage": "1 capsule",
      "frequency": "3 times daily",
      "duration": "7 days",
      "instructions": "Take after meals"
    },
    {
      "medicationName": "Ibuprofen 400mg",
      "dosage": "1 tablet",
      "frequency": "As needed",
      "duration": "5 days",
      "instructions": "For pain relief, max 3 times daily"
    }
  ]
}
```

#### **Function: `updateClinicalRecord`**
- **Feature:** Clinical Record Management
- **Endpoint:** `PUT /api/v1/clinical-records/{recordId}`
- **Request Body:**
```json
{
  "chiefComplaint": "Severe toothache upper right molar (Follow-up)",
  "diagnosis": "Acute pulpitis - Tooth #16 (Resolved)",
  "treatmentProvided": "Root canal therapy completed, temporary filling placed",
  "vitalSigns": {
    "bloodPressure": "118/78",
    "heartRate": 70,
    "temperature": 36.6
  }
}
```

---

### **8.2 PROCEDURES**
#### **Function: `addProcedure`**
- **Feature:** Clinical Procedure Management
- **Endpoint:** `POST /api/v1/clinical-records/{recordId}/procedures`
- **Request Body:**
```json
{
  "serviceId": 10,
  "toothNumber": "17",
  "surface": "MESIAL",
  "notes": "Composite filling placed"
}
```

#### **Function: `updateProcedure`**
- **Feature:** Clinical Procedure Management
- **Endpoint:** `PUT /api/v1/clinical-records/{recordId}/procedures/{procedureId}`
- **Request Body:**
```json
{
  "serviceId": 10,
  "toothNumber": "17",
  "surface": "MESIAL_OCCLUSAL",
  "notes": "Composite filling extended to occlusal surface"
}
```

---

### **8.3 PRESCRIPTIONS**
#### **Function: `savePrescription`**
- **Feature:** Prescription Management
- **Endpoint:** `POST /api/v1/clinical-records/{recordId}/prescription`
- **Request Body:**
```json
{
  "medications": [
    {
      "medicationName": "Metronidazole 500mg",
      "dosage": "1 tablet",
      "frequency": "2 times daily",
      "duration": "7 days",
      "instructions": "Take with food, avoid alcohol"
    }
  ]
}
```

---

### **8.4 ATTACHMENTS**
#### **Function: `uploadAttachment`**
- **Feature:** Clinical Record Attachments
- **Endpoint:** `POST /api/v1/clinical-records/{recordId}/attachments`
- **Request Body (multipart/form-data):**
```
file: [Binary file data]
attachmentType: "IMAGE"
description: "Post-treatment X-ray"
```

---

## **9. AUTHENTICATION & ACCOUNT**

#### **Function: `login`**
- **Feature:** Authentication
- **Endpoint:** `POST /api/v1/auth/login`
- **Request Body:**
```json
{
  "username": "admin@dental.com",
  "password": "SecurePassword123!"
}
```

#### **Function: `refreshToken`**
- **Feature:** Authentication
- **Endpoint:** `POST /api/v1/auth/refresh`
- **Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### **Function: `logout`**
- **Feature:** Authentication
- **Endpoint:** `POST /api/v1/auth/logout`
- **Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### **Function: `resendVerification`**
- **Feature:** Email Verification
- **Endpoint:** `POST /api/v1/auth/resend-verification`
- **Request Body:**
```json
{
  "email": "user@dental.com"
}
```

#### **Function: `forgotPassword`**
- **Feature:** Password Reset
- **Endpoint:** `POST /api/v1/auth/forgot-password`
- **Request Body:**
```json
{
  "email": "user@dental.com"
}
```

#### **Function: `resetPassword`**
- **Feature:** Password Reset
- **Endpoint:** `POST /api/v1/auth/reset-password`
- **Request Body:**
```json
{
  "token": "reset_token_from_email",
  "newPassword": "NewSecurePassword456!",
  "confirmPassword": "NewSecurePassword456!"
}
```

---

## **10. PERMISSION & ROLE MANAGEMENT**

### **10.1 PERMISSIONS**
#### **Function: `createPermission`**
- **Feature:** Permission Management
- **Endpoint:** `POST /api/v1/permissions`
- **Request Body:**
```json
{
  "permissionCode": "VIEW_REPORTS",
  "permissionName": "View Reports",
  "module": "REPORTING",
  "description": "Ability to view system reports",
  "isActive": true
}
```

#### **Function: `updatePermission`**
- **Feature:** Permission Management
- **Endpoint:** `PATCH /api/v1/permissions/{permissionId}`
- **Request Body:**
```json
{
  "permissionName": "View All Reports",
  "description": "Ability to view all system reports including sensitive data"
}
```

---

### **10.2 ROLES**
#### **Function: `createRole`**
- **Feature:** Role Management
- **Endpoint:** `POST /api/v1/roles`
- **Request Body:**
```json
{
  "roleName": "Senior Doctor",
  "roleCode": "SENIOR_DOCTOR",
  "description": "Senior medical practitioners with extended permissions",
  "isActive": true
}
```

#### **Function: `updateRole`**
- **Feature:** Role Management
- **Endpoint:** `PUT /api/v1/roles/{roleId}`
- **Request Body:**
```json
{
  "roleName": "Senior Dentist",
  "roleCode": "SENIOR_DENTIST",
  "description": "Senior dental practitioners with full clinical permissions",
  "isActive": true
}
```

#### **Function: `assignPermissionsToRole`**
- **Feature:** Role Permission Assignment
- **Endpoint:** `POST /api/v1/roles/{roleId}/permissions`
- **Request Body:**
```json
{
  "permissionIds": [1, 2, 3, 5, 8, 13, 21, 34, 55]
}
```

#### **Function: `assignRoleToAccount`**
- **Feature:** User Role Assignment
- **Endpoint:** `POST /api/v1/roles/users/{userId}/roles`
- **Request Body:**
```json
{
  "roleId": 3
}
```

---

## **📊 SUMMARY BY FEATURE**

| **Feature Module** | **Function Count** |
|--------------------|-------------------|
| Working Schedule Management | 20 |
| Warehouse & Inventory | 18 |
| Treatment Plan Management | 11 |
| Patient Management | 12 |
| Appointment & Booking | 10 |
| Clinical Records | 7 |
| Authentication & Account | 6 |
| Permission & Role Management | 7 |
| Employee Management | 3 |
| Service Management | 4 |
| **TOTAL** | **98** |

---

## **🧪 UNIT TEST CATEGORIES**

For each function above, create test cases for:

### **1. Valid Input Scenarios ✅**
- Happy path with all required fields
- Optional fields included
- Boundary values (min/max lengths, dates)
- Different valid data combinations

### **2. Invalid Input Validation ❌**
- Missing required fields
- Invalid data types
- Invalid formats (email, phone, dates)
- Out-of-range values
- Invalid enum values
- Null values where not allowed

### **3. Business Rule Validation ⚠️**
- Duplicate checks
- Constraint violations
- Status transition rules
- Relationship validations
- Permission checks
- Quota limitations

### **4. Edge Cases 🔶**
- Empty strings vs null
- Special characters
- Very long strings
- Past/future dates
- Concurrent modifications
- Soft-deleted entities

---

**Document Version:** 2.0  
**Last Updated:** December 11, 2025  
**For:** Unit Test Case Excel Documentation  
**Maintainer:** Backend Development Team
