# Entity Refactoring Progress - Phase 2

## ✅ Entity Updates Completed

### 1. Account Entity
- **ID Type**: String (UUID) → **Integer** (auto-increment) ✅
- **Code Field**: Added `accountCode` VARCHAR(20) ✅
- **Format**: ACC001, ACC002, ACC003...
- **Status**: Entity complete, service layer pending

### 2. Employee Entity  
- **ID Type**: String (UUID) → **Integer** (auto-increment) ✅
- **Code Field**: `employeeCode` updated to VARCHAR(20) ✅
- **Format**: EMP001, EMP002, EMP003...
- **Status**: Entity complete, service layer pending

### 3. Patient Entity
- **ID Type**: String (UUID) → **Integer** (auto-increment) ✅
- **Code Field**: `patientCode` updated to VARCHAR(20) ✅
- **Format**: PAT001, PAT002, PAT003...
- **Status**: Entity complete, service layer pending

### 4. ContactHistory Entity (FK Updates)
- **employee_id**: VARCHAR(36) → **Integer** ✅
- **contact_id**: VARCHAR(36) → VARCHAR(20) ✅ (already done in Phase 1)

### 5. CustomerContact Entity (FK Updates)
- **assigned_to**: VARCHAR(36) → **Integer** ✅
- **converted_patient_id**: VARCHAR(36) → **Integer** ✅
- **contact_id**: Still VARCHAR(20) (correct - date-based ID)

### 6. Specialization Entity
- **ID Type**: String (UUID) → **Integer** ✅
- **Format**: 1, 2, 3, 4, 5, 6, 7

## 📋 Files That Need Updates (Compilation Errors)

### Authentication & User Services (3 files)
- [ ] `AuthenticationService.java` - Update UserInfoResponse and UserProfileResponse ID types
- [ ] `CustomUserDetailsService.java` - Update accountId return type

### Employee Module (3 files)
- [ ] `EmployeeMapper.java` - Update ID mappings (Integer instead of String)
- [ ] `EmployeeService.java` - Remove UUID generation, use SequentialCodeGenerator
- [ ] `EmployeeInfoResponse.java` - Change employeeId, accountId, specializationId to Integer

### Patient Module (3 files)
- [ ] `PatientMapper.java` - Update ID mappings (Integer instead of String)
- [ ] `PatientService.java` - Remove UUID generation, use SequentialCodeGenerator
- [ ] `PatientInfoResponse.java` - Change patientId to Integer

### CustomerContact Module (3 files)
- [ ] `CustomerContactMapper.java` - Update assignedTo and convertedPatientId types
- [ ] `CustomerContactService.java` - Update FK types and methods
- [ ] `ContactInfoResponse.java` - Change assignedTo and convertedPatientId to Integer
- [ ] `CustomerContactRepository.java` - Update query methods parameter types

### ContactHistory Module (2 files)
- [ ] `ContactHistoryMapper.java` - Update employeeId type
- [ ] `ContactHistoryService.java` - Update employeeId handling
- [ ] `ContactHistoryResponse.java` - Change employeeId to Integer

### DTOs/Requests/Responses (~15+ files)
Multiple response DTOs need ID field type updates:
- [ ] `UserInfoResponse.java` - accountId String → Integer
- [ ] `UserProfileResponse.java` - accountId String → Integer
- [ ] `EmployeeInfoResponse.java` - Multiple ID fields
- [ ] `PatientInfoResponse.java` - patientId String → Integer
- [ ] `ContactInfoResponse.java` - Multiple FK fields
- [ ] `ContactHistoryResponse.java` - employeeId String → Integer
- [ ] Various request DTOs...

### Repositories (~3+ files)
- [ ] `CustomerContactRepository.java` - Update query methods for Integer FK types
- [ ] `AccountRepository.java` - Update findById to use Integer
- [ ] `EmployeeRepository.java` - Update findById to use Integer
- [ ] `PatientRepository.java` - Update findById to use Integer

## 🔧 Required Service Layer Changes

### Account Service
```java
@Transactional
public Account createAccount(CreateAccountRequest request) {
    Account account = new Account();
    // ... set fields ...
    
    // Save to get auto-generated ID
    Account saved = accountRepository.save(account);
    
    // Generate code based on ID
    saved.setAccountCode(codeGenerator.generateAccountCode(saved.getAccountId()));
    
    // Save again with code
    return accountRepository.save(saved);
}
```

### Employee Service
```java
@Transactional
public Employee createEmployee(CreateEmployeeRequest request) {
    Employee employee = new Employee();
    // ... set fields ...
    
    // Save to get auto-generated ID
    Employee saved = employeeRepository.save(employee);
    
    // Generate code based on ID
    saved.setEmployeeCode(codeGenerator.generateEmployeeCode(saved.getEmployeeId()));
    
    // Save again with code
    return employeeRepository.save(saved);
}
```

### Patient Service
```java
@Transactional
public Patient createPatient(CreatePatientRequest request) {
    Patient patient = new Patient();
    // ... set fields ...
    
    // Save to get auto-generated ID
    Patient saved = patientRepository.save(patient);
    
    // Generate code based on ID
    saved.setPatientCode(codeGenerator.generatePatientCode(saved.getPatientId()));
    
    // Save again with code
    return patientRepository.save(saved);
}
```

## 🗄️ Database Migration Script

```sql
-- ============================================
-- PHASE 2: ID Refactoring Migration
-- ============================================

-- STEP 1: Backup existing data
CREATE TABLE accounts_backup AS SELECT * FROM accounts;
CREATE TABLE employees_backup AS SELECT * FROM employees;
CREATE TABLE patients_backup AS SELECT * FROM patients;

-- STEP 2: Create ID mapping tables
CREATE TABLE account_id_mapping (
    old_id VARCHAR(36) PRIMARY KEY,
    new_id INT AUTO_INCREMENT,
    KEY (new_id)
);

CREATE TABLE employee_id_mapping (
    old_id VARCHAR(36) PRIMARY KEY,
    new_id INT AUTO_INCREMENT,
    KEY (new_id)
);

CREATE TABLE patient_id_mapping (
    old_id VARCHAR(36) PRIMARY KEY,
    new_id INT AUTO_INCREMENT,
    KEY (new_id)
);

-- STEP 3: Populate mapping tables (in creation order to get sequential IDs)
INSERT INTO account_id_mapping (old_id)
SELECT account_id FROM accounts ORDER BY created_at;

INSERT INTO employee_id_mapping (old_id)
SELECT employee_id FROM employees ORDER BY created_at;

INSERT INTO patient_id_mapping (old_id)
SELECT patient_id FROM patients ORDER BY created_at;

-- STEP 4: Drop foreign key constraints
ALTER TABLE employees DROP FOREIGN KEY fk_employee_account;
ALTER TABLE patients DROP FOREIGN KEY fk_patient_account;
ALTER TABLE employee_specializations DROP FOREIGN KEY fk_emp_spec_employee;
ALTER TABLE contact_history DROP FOREIGN KEY fk_contact_history_employee;
ALTER TABLE customer_contacts DROP FOREIGN KEY fk_customer_contact_employee;
ALTER TABLE customer_contacts DROP FOREIGN KEY fk_customer_contact_patient;
-- ... and more

-- STEP 5: Add new ID columns (temporary)
ALTER TABLE accounts ADD COLUMN account_id_new INT;
ALTER TABLE accounts ADD COLUMN account_code VARCHAR(20);

ALTER TABLE employees ADD COLUMN employee_id_new INT;
ALTER TABLE employees ADD COLUMN account_id_new INT;

ALTER TABLE patients ADD COLUMN patient_id_new INT;
ALTER TABLE patients ADD COLUMN account_id_new INT;

-- STEP 6: Update with new IDs using mapping tables
UPDATE accounts a
JOIN account_id_mapping m ON a.account_id = m.old_id
SET a.account_id_new = m.new_id;

UPDATE employees e
JOIN employee_id_mapping em ON e.employee_id = em.old_id
JOIN account_id_mapping am ON e.account_id = am.old_id
SET e.employee_id_new = em.new_id,
    e.account_id_new = am.new_id;

UPDATE patients p
JOIN patient_id_mapping pm ON p.patient_id = pm.old_id
LEFT JOIN account_id_mapping am ON p.account_id = am.old_id
SET p.patient_id_new = pm.new_id,
    p.account_id_new = am.new_id;

-- STEP 7: Generate codes
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(account_id_new, 3, '0'));
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(employee_id_new, 3, '0'));
UPDATE patients SET patient_code = CONCAT('PAT', LPAD(patient_id_new, 3, '0'));

-- STEP 8: Update foreign keys
UPDATE employee_specializations es
JOIN employee_id_mapping m ON es.employee_id = m.old_id
SET es.employee_id = m.new_id;

UPDATE contact_history ch
JOIN employee_id_mapping m ON ch.employee_id = m.old_id
SET ch.employee_id = m.new_id;

UPDATE customer_contacts cc
LEFT JOIN employee_id_mapping em ON cc.assigned_to = em.old_id
LEFT JOIN patient_id_mapping pm ON cc.converted_patient_id = pm.old_id
SET cc.assigned_to = em.new_id,
    cc.converted_patient_id = pm.new_id;

-- STEP 9: Drop old ID columns and rename new ones
ALTER TABLE accounts 
  DROP PRIMARY KEY,
  DROP COLUMN account_id,
  CHANGE COLUMN account_id_new account_id INT AUTO_INCREMENT PRIMARY KEY;

ALTER TABLE employees 
  DROP PRIMARY KEY,
  DROP COLUMN employee_id,
  DROP COLUMN account_id,
  CHANGE COLUMN employee_id_new employee_id INT AUTO_INCREMENT PRIMARY KEY,
  CHANGE COLUMN account_id_new account_id INT;

ALTER TABLE patients 
  DROP PRIMARY KEY,
  DROP COLUMN patient_id,
  DROP COLUMN account_id,
  CHANGE COLUMN patient_id_new patient_id INT AUTO_INCREMENT PRIMARY KEY,
  CHANGE COLUMN account_id_new account_id INT;

-- STEP 10: Update FK column types
ALTER TABLE employee_specializations MODIFY COLUMN employee_id INT;
ALTER TABLE contact_history MODIFY COLUMN employee_id INT;
ALTER TABLE customer_contacts MODIFY COLUMN assigned_to INT;
ALTER TABLE customer_contacts MODIFY COLUMN converted_patient_id INT;

-- STEP 11: Recreate foreign key constraints
ALTER TABLE employees 
  ADD CONSTRAINT fk_employee_account 
  FOREIGN KEY (account_id) REFERENCES accounts(account_id);

ALTER TABLE patients 
  ADD CONSTRAINT fk_patient_account 
  FOREIGN KEY (account_id) REFERENCES accounts(account_id);

ALTER TABLE employee_specializations 
  ADD CONSTRAINT fk_emp_spec_employee 
  FOREIGN KEY (employee_id) REFERENCES employees(employee_id);

ALTER TABLE contact_history 
  ADD CONSTRAINT fk_contact_history_employee 
  FOREIGN KEY (employee_id) REFERENCES employees(employee_id);

-- ... and more

-- STEP 12: Add unique constraints
ALTER TABLE accounts ADD UNIQUE KEY uk_account_code (account_code);
ALTER TABLE employees ADD UNIQUE KEY uk_employee_code (employee_code);
ALTER TABLE patients ADD UNIQUE KEY uk_patient_code (patient_code);

-- STEP 13: Drop mapping tables (after verification)
-- DROP TABLE account_id_mapping;
-- DROP TABLE employee_id_mapping;
-- DROP TABLE patient_id_mapping;
```

## 📊 Summary Statistics

### Entity Changes: 6 entities
- ✅ Account
- ✅ Employee
- ✅ Patient
- ✅ ContactHistory
- ✅ CustomerContact
- ✅ Specialization

### Files Requiring Updates: ~40-50 files
- Entities: 6 (done ✅)
- Services: ~8 files
- Mappers: ~6 files
- DTOs/Responses: ~15 files
- Repositories: ~4 files
- Controllers: ~6 files (may need updates)

### Current Status
- **Entities**: 100% complete ✅
- **Services**: 0% complete ⏳
- **DTOs**: 0% complete ⏳
- **Mappers**: 0% complete ⏳
- **Repositories**: 0% complete ⏳
- **Database Migration**: Script ready, not executed ⚠️

## 🎯 Next Steps

1. **Test Entity Layer**
   - Verify entity structure is correct
   - Check relationships

2. **Update DTOs** (highest priority - affects many files)
   - Change ID field types from String to Integer
   - Update request/response classes

3. **Update Repositories**
   - Change parameter types in query methods
   - Update method signatures

4. **Update Services**
   - Remove UUID generation
   - Implement SequentialCodeGenerator integration
   - Fix ID type mismatches

5. **Update Mappers**
   - Fix type conversions
   - Update ID mappings

6. **Database Migration**
   - Test migration script on dev/staging
   - Execute migration
   - Verify data integrity

7. **Integration Testing**
   - Test all CRUD operations
   - Verify foreign key relationships
   - Check code generation

## ⚠️ Important Notes

- This is a **breaking change** - existing data must be migrated
- **Backup database** before migration
- Code generation happens in service layer (2 saves per entity)
- Consider using database triggers as alternative
- Thorough testing required before production deployment
