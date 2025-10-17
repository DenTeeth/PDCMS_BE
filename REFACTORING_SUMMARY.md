# ID/Code Refactoring Summary - Phase 1 Complete

## Overview
This document summarizes all the ID and code format changes implemented in Phase 1.

## ✅ Completed Changes

### 1. CustomerContact
- **Old ID**: `String` (UUID) - VARCHAR(36)
- **New ID**: `String` (Date-based) - VARCHAR(20)
- **Format**: `CTC-YYMMDD-SEQ` (e.g., `CTC-251016-001`)
- **Counter**: Daily reset
- **Generator**: `IdGenerator.java`

### 2. ContactHistory
- **Old ID**: `String` (UUID) - VARCHAR(36)
- **New ID**: `String` (Date-based) - VARCHAR(20)
- **Format**: `CTH-YYMMDD-SEQ` (e.g., `CTH-251016-001`)
- **Counter**: Daily reset
- **Generator**: `IdGenerator.java`
- **Foreign Keys Updated**:
  - `contact_id`: VARCHAR(36) → VARCHAR(20)

### 3. Permission
- **Old ID**: `String` (UUID) - VARCHAR(30)
- **New ID**: `String` (Permission name) - VARCHAR(50)
- **Format**: Permission name directly (e.g., `CREATE_CONTACT`, `UPDATE_EMPLOYEE`)
- **Generator**: Uses permission name as ID
- **Service Updated**: PermissionService.java - removed UUID generation

### 4. Role
- **ID Type**: `String` - VARCHAR(50) *(no change)*
- **Format**: Role name (e.g., `ROLE_ADMIN`, `ROLE_DOCTOR`)
- **Note**: Already using correct format, no changes needed

### 5. Specialization
- **Old ID**: `String` (UUID) - VARCHAR(36)
- **New ID**: `Integer` - INT
- **Format**: Simple integers (1, 2, 3, 4, 5, 6, 7)
- **No Code Field**: ID is the identifier
- **Seed Data Updated**: Uses 1-7 for specializations

### 6. Account *(Entity Updated, Services Pending)*
- **Old ID**: `String` (UUID) - VARCHAR(36)
- **New ID**: `Integer` (Auto-increment) - INT
- **Code Field Added**: `account_code` - VARCHAR(20)
- **Code Format**: `ACC###` (e.g., `ACC001`, `ACC151`)
- **Generator**: `SequentialCodeGenerator.java`
- **Status**: ⚠️ Entity updated, service integration pending

## 🔧 Utility Classes Created

### IdGenerator.java
- **Purpose**: Generate date-based IDs
- **Format**: `PREFIX-YYMMDD-SEQ`
- **Used By**: CustomerContact, ContactHistory
- **Features**:
  - Thread-safe counter per prefix-date
  - Daily counter reset
  - Auto-cleanup of old dates

### SequentialCodeGenerator.java
- **Purpose**: Generate sequential codes from entity IDs
- **Format**: `PREFIX###` (3-digit padded)
- **Used By**: Account, Employee (future), Patient (future)
- **Features**:
  - Code based on auto-increment ID
  - No separate counter needed
  - Consistent with database IDs

### EntityIdGeneratorConfig.java
- **Purpose**: Inject IdGenerator into entities at startup
- **Configured Entities**: CustomerContact, ContactHistory
- **Note**: Only for date-based IDs (not sequential codes)

## 📊 ID Format Summary Table

| Entity | Old ID Type | New ID Type | Format | Example | Generator |
|--------|------------|-------------|--------|---------|-----------|
| CustomerContact | String (UUID) | String (Date) | CTC-YYMMDD-SEQ | CTC-251016-001 | IdGenerator |
| ContactHistory | String (UUID) | String (Date) | CTH-YYMMDD-SEQ | CTH-251016-002 | IdGenerator |
| Permission | String (UUID) | String (Name) | PERMISSION_NAME | CREATE_CONTACT | Direct assignment |
| Role | String (Name) | String (Name) | ROLE_NAME | ROLE_ADMIN | Direct assignment |
| Specialization | String (UUID) | Integer | # | 1, 2, 3... | Manual/Seed data |
| Account | String (UUID) | Integer + Code | ACC### | ID: 1, Code: ACC001 | SequentialCodeGenerator |
| Employee | String (UUID) | **Pending** | EMP### | ID: ?, Code: EMP002 | SequentialCodeGenerator |
| Patient | String (UUID) | **Pending** | PAT### | ID: ?, Code: PAT012 | SequentialCodeGenerator |

## ⏳ Pending Work (Phase 2)

### Employee Entity
- [ ] Change `employeeId` from String to Integer
- [ ] Update `employeeCode` generation to use SequentialCodeGenerator
- [ ] Update EmployeeService to generate codes after save
- [ ] Update all Employee DTOs, requests, responses
- [ ] Update foreign key references:
  - `ContactHistory.employee_id`: VARCHAR(36) → INT
  - `CustomerContact.assigned_to`: VARCHAR(36) → INT
  - `employee_specializations.employee_id`: VARCHAR(36) → INT

### Patient Entity
- [ ] Change `patientId` from String to Integer
- [ ] Update `patientCode` generation to use SequentialCodeGenerator
- [ ] Update PatientService to generate codes after save
- [ ] Update all Patient DTOs, requests, responses
- [ ] Update foreign key references:
  - `CustomerContact.converted_patient_id`: VARCHAR(36) → INT

### Account Services
- [ ] Update AccountService to generate codes after save
- [ ] Update all Account DTOs, requests, responses
- [ ] Update AccountRepository methods
- [ ] Update foreign key references:
  - `Employee.account_id`: VARCHAR(36) → INT
  - `Patient.account_id`: VARCHAR(36) → INT
  - `account_roles.account_id`: VARCHAR(36) → INT

## 🗄️ Database Schema Changes Required

### Completed Tables
```sql
-- customer_contacts
ALTER TABLE customer_contacts 
  MODIFY COLUMN contact_id VARCHAR(20);

-- contact_history
ALTER TABLE contact_history 
  MODIFY COLUMN history_id VARCHAR(20),
  MODIFY COLUMN contact_id VARCHAR(20);

-- permissions
ALTER TABLE permissions 
  MODIFY COLUMN permission_id VARCHAR(50);

-- specializations
ALTER TABLE specializations 
  MODIFY COLUMN specialization_id INT;

-- employee_specializations
ALTER TABLE employee_specializations 
  MODIFY COLUMN specialization_id INT;
```

### Pending Tables
```sql
-- accounts (structure changed, pending service implementation)
ALTER TABLE accounts 
  MODIFY COLUMN account_id INT AUTO_INCREMENT,
  ADD COLUMN account_code VARCHAR(20) UNIQUE AFTER account_id;

-- employees (not yet started)
ALTER TABLE employees 
  MODIFY COLUMN employee_id INT AUTO_INCREMENT,
  MODIFY COLUMN account_id INT,
  MODIFY COLUMN employee_code VARCHAR(20);

-- patients (not yet started)
ALTER TABLE patients 
  MODIFY COLUMN patient_id INT AUTO_INCREMENT,
  MODIFY COLUMN account_id INT,
  MODIFY COLUMN patient_code VARCHAR(20);

-- Update foreign keys...
ALTER TABLE employee_specializations 
  MODIFY COLUMN employee_id INT;

ALTER TABLE contact_history 
  MODIFY COLUMN employee_id INT;

ALTER TABLE customer_contacts 
  MODIFY COLUMN assigned_to INT,
  MODIFY COLUMN converted_patient_id INT;

-- And many more...
```

## 🎯 Code Generation Rules

### Date-Based IDs (CTC, CTH)
1. Format: `PREFIX-YYMMDD-SEQ`
2. Generated in `@PrePersist` method
3. Counter resets daily
4. Example: CTC-251016-001, CTC-251016-002

### Sequential Codes (ACC, EMP, PAT)
1. Format: `PREFIX###` (3-digit padded)
2. Generated in service layer AFTER entity save
3. Based on auto-increment ID
4. Example: ACC001 (ID=1), EMP150 (ID=150)

### Name-Based IDs (Permission, Role)
1. Format: Actual name
2. Assigned directly from request
3. No generation needed
4. Example: CREATE_CONTACT, ROLE_ADMIN

### Simple Integers (Specialization)
1. Format: 1, 2, 3, 4...
2. Manually assigned in seed data
3. No auto-generation
4. Example: 1, 2, 3

## 📝 Next Steps

### Before Proceeding with Phase 2:
1. **Test Phase 1 changes**
   - Verify CustomerContact creation
   - Verify ContactHistory creation
   - Check ID format correctness

2. **Decide on Phase 2 approach**
   - Option A: Continue with Integer ID refactoring
   - Option B: Keep UUID IDs, only add code fields

3. **Database Migration Strategy**
   - Create backup
   - Test migration scripts
   - Plan for zero-downtime deployment

4. **Impact Assessment**
   - Estimate files affected (~80-100+)
   - Testing requirements
   - Timeline estimation

## 🔍 Consistency Check

### ✅ Now Consistent:
- Specialization: Integer ID ✅
- Account: Integer ID ✅
- Permission: String (name-based, expanded to VARCHAR 50) ✅
- Role: String (name-based) ✅

### ⚠️ Still Inconsistent:
- Employee: Still String (UUID)
- Patient: Still String (UUID)

**Recommendation**: Complete Employee and Patient refactoring to maintain consistency across all core entities (Account, Employee, Patient).
