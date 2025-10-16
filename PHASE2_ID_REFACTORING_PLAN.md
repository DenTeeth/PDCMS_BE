# Phase 2: Account, Employee, Patient ID Refactoring Plan

## Overview
Change primary keys from UUID String to auto-increment Integer, and add code fields (ACC001, EMP001, PAT001).

## ⚠️ CRITICAL WARNING
This is a **BREAKING CHANGE** that affects:
- Database schema (primary keys, foreign keys)
- All repositories using these IDs
- All services handling these entities
- All DTOs, requests, responses
- All controllers
- Foreign key relationships in other entities

## Proposed Changes

### 1. Account Entity
```java
// OLD
@Id
@Column(name = "account_id", length = 36)
private String accountId;

// NEW
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "account_id")
private Integer accountId;

@NotBlank
@Column(name = "account_code", unique = true, length = 20)
private String accountCode;  // ACC001, ACC002, etc.
```

**Impacts:**
- `Employee.account_id` (FK) - Change from VARCHAR(36) to INT
- `Patient.account_id` (FK) - Change from VARCHAR(36) to INT
- `account_roles.account_id` (FK) - Change from VARCHAR(36) to INT
- All Account repository methods
- All Account service methods
- All Account DTOs/responses
- Authentication/Authorization logic

### 2. Employee Entity
```java
// OLD
@Id
@Column(name = "employee_id", length = 36)
private String employeeId;

@Column(name = "employee_code"...)  // Already exists
private String employeeCode;

// NEW
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "employee_id")
private Integer employeeId;

@Column(name = "employee_code"...)  // Keep as is, but generate EMP001, EMP002
private String employeeCode;
```

**Impacts:**
- `ContactHistory.employee_id` (FK) - Change from VARCHAR(36) to INT
- `employee_specializations.employee_id` (FK) - Change from VARCHAR(36) to INT
- CustomerContact.assigned_to - Change from VARCHAR(36) to INT
- All Employee repository methods
- All Employee service methods
- All Employee DTOs/responses

### 3. Patient Entity
```java
// OLD
@Id
@Column(name = "patient_id", length = 36)
private String patientId;

@Column(name = "patient_code"...)  // Already exists
private String patientCode;

// NEW
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "patient_id")
private Integer patientId;

@Column(name = "patient_code"...)  // Keep as is, but generate PAT001, PAT002
private String patientCode;
```

**Impacts:**
- `CustomerContact.converted_patient_id` (FK) - Change from VARCHAR(36) to INT
- All Patient repository methods
- All Patient service methods
- All Patient DTOs/responses

## Code Generation Strategy

### Option A: Database-Level Auto Generation (Recommended)
Use auto-increment for IDs and trigger/function for codes:

```sql
-- For Account
CREATE TRIGGER before_account_insert 
BEFORE INSERT ON accounts
FOR EACH ROW
SET NEW.account_code = CONCAT('ACC', LPAD(NEW.account_id, 3, '0'));

-- For Employee
CREATE TRIGGER before_employee_insert 
BEFORE INSERT ON employees
FOR EACH ROW
SET NEW.employee_code = CONCAT('EMP', LPAD(NEW.employee_id, 3, '0'));

-- For Patient
CREATE TRIGGER before_patient_insert 
BEFORE INSERT ON patients
FOR EACH ROW
SET NEW.patient_code = CONCAT('PAT', LPAD(NEW.patient_id, 3, '0'));
```

### Option B: Service-Level Generation
Generate codes in service after entity is saved:

```java
@Transactional
public Account createAccount(CreateAccountRequest request) {
    Account account = new Account();
    // ... set fields ...
    Account saved = accountRepository.save(account);  // Get auto-generated ID
    saved.setAccountCode(String.format("ACC%03d", saved.getAccountId()));
    return accountRepository.save(saved);  // Save again with code
}
```

## Migration Steps

### Step 1: Backup Database ⚠️
```bash
mysqldump -u username -p database_name > backup_before_id_refactor.sql
```

### Step 2: Create Migration Scripts

#### 2.1 Create New Tables with Integer IDs
```sql
-- Create temporary tables with new structure
CREATE TABLE accounts_new LIKE accounts;
ALTER TABLE accounts_new 
  MODIFY COLUMN account_id INT AUTO_INCREMENT,
  ADD COLUMN account_code VARCHAR(20) UNIQUE AFTER account_id;

CREATE TABLE employees_new LIKE employees;
ALTER TABLE employees_new 
  MODIFY COLUMN employee_id INT AUTO_INCREMENT,
  MODIFY COLUMN account_id INT,
  MODIFY COLUMN employee_code VARCHAR(20) NOT NULL;

CREATE TABLE patients_new LIKE patients;
ALTER TABLE patients_new 
  MODIFY COLUMN patient_id INT AUTO_INCREMENT,
  MODIFY COLUMN account_id INT,
  MODIFY COLUMN patient_code VARCHAR(20) NOT NULL;
```

#### 2.2 Migrate Data
This is COMPLEX and requires mapping old UUIDs to new Integer IDs.

```sql
-- Create mapping tables
CREATE TABLE account_id_mapping (
  old_id VARCHAR(36),
  new_id INT AUTO_INCREMENT PRIMARY KEY
);

-- Insert and map
INSERT INTO accounts_new (username, email, password, status, created_at)
SELECT username, email, password, status, created_at FROM accounts;

INSERT INTO account_id_mapping (old_id)
SELECT account_id FROM accounts ORDER BY created_at;

UPDATE account_id_mapping m
JOIN accounts_new n ON n.created_at = (
  SELECT created_at FROM accounts WHERE account_id = m.old_id
)
SET m.new_id = n.account_id;

-- Similar for Employee and Patient...
```

### Step 3: Update All Foreign Keys
```sql
-- Update employee.account_id
UPDATE employees_new e
JOIN account_id_mapping m ON e.account_id = m.old_id
SET e.account_id = m.new_id;

-- Update patient.account_id
UPDATE patients_new p
JOIN account_id_mapping m ON p.account_id = m.old_id
SET p.account_id = m.new_id;

-- ... many more FK updates ...
```

### Step 4: Generate Codes
```sql
UPDATE accounts_new SET account_code = CONCAT('ACC', LPAD(account_id, 3, '0'));
UPDATE employees_new SET employee_code = CONCAT('EMP', LPAD(employee_id, 3, '0'));
UPDATE patients_new SET patient_code = CONCAT('PAT', LPAD(patient_id, 3, '0'));
```

### Step 5: Swap Tables
```sql
DROP TABLE accounts;
RENAME TABLE accounts_new TO accounts;
-- Similar for employees, patients...
```

## Code Files That Need Updates

### Entities (3 files)
- ✅ Account.java - ID type changed to Integer, added accountCode
- ⚠️ Employee.java - Need to change ID type to Integer
- ⚠️ Patient.java - Need to change ID type to Integer
- ⚠️ ContactHistory.java - FK employee_id to Integer
- ⚠️ CustomerContact.java - FK assigned_to, converted_patient_id to Integer

### Repositories (~10+ files)
All repository methods using String IDs must change to Integer:
- AccountRepository.java
- EmployeeRepository.java
- PatientRepository.java
- And any custom query methods...

### Services (~20+ files)
All service methods handling these entities:
- AccountService.java
- EmployeeService.java
- PatientService.java
- AuthenticationService.java
- And related services...

### DTOs/Requests/Responses (~30+ files)
All DTOs containing accountId, employeeId, patientId:
- AccountInfoResponse.java
- CreateAccountRequest.java
- UpdateAccountRequest.java
- EmployeeResponse.java
- ... many more ...

### Controllers (~10+ files)
All controller endpoints with path variables:
- AccountController.java
- EmployeeController.java
- PatientController.java

### Mappers (~10+ files)
All mapper classes converting between entities and DTOs

## Estimated Effort
- **Files to modify**: 80-100+ files
- **Database migration**: Complex, requires careful planning
- **Testing required**: Extensive
- **Risk level**: **VERY HIGH** 🔴

## Recommendation

Given the scope and complexity of this change, I recommend:

1. **Create a feature branch** for this refactoring
2. **Start with a proof-of-concept** on one entity (e.g., Account)
3. **Write comprehensive tests** before making changes
4. **Create detailed migration scripts** and test on staging
5. **Consider if UUID -> Integer is truly necessary** (breaking change costs)

## Alternative Approach: Keep UUID, Add Code Field Only

A **safer** alternative:
- Keep `account_id`, `employee_id`, `patient_id` as UUID String
- Only add `account_code`, update `employee_code`, `patient_code` generation
- Much less invasive, fewer files to change
- Codes are for display purposes, IDs remain internal

Would you like me to:
1. **Proceed with full Integer ID refactoring** (high effort, many files)
2. **Do the safer alternative** (add/update code fields only, keep UUID IDs)
3. **Start with one entity as proof-of-concept**

Please advise before I continue!
