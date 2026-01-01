# 📁 SQL Files Policy & Cleanup Summary

**Date**: January 1, 2026  
**Action**: Enforce SQL files policy  
**Status**: ✅ **COMPLETED**

---

## 📋 Policy

### ✅ Allowed SQL Files (2 files only)

**Location**: `src/main/resources/db/`

1. **`enums.sql`**
   - Purpose: Define PostgreSQL ENUM types
   - Execution: Runs **BEFORE** Hibernate (via `spring.sql.init.schema-locations`)
   - Example:
     ```sql
     CREATE TYPE employment_type AS ENUM ('FULL_TIME', 'PART_TIME_FIXED', 'PART_TIME_FLEX');
     ```

2. **`dental-clinic-seed-data.sql`**
   - Purpose: Insert seed/test data into tables
   - Execution: Runs **AFTER** Hibernate creates tables (via `DataInitializer.java` bean)
   - Example:
     ```sql
     INSERT INTO accounts (username, password, ...) VALUES (...);
     INSERT INTO employees (...) VALUES (...);
     ```

### ✅ Exception: Documentation SQL Files

**Location**: `docs/` folder

- **`docs/warehouse-integration/TEST_QUERIES.sql`** ✅ **ALLOWED**
- Purpose: Example queries for documentation/testing
- These are **NOT executed** by application
- Purely for developer reference

---

## ❌ Prohibited SQL Files

### Files Deleted in This Cleanup

1. ❌ **`sql-scripts/fix-invoice-data-integrity-20260101.sql`**
   - Reason: Migration scripts NOT allowed
   - Policy: Use seed data or manual DB operations
   - Status: **DELETED** ✅

### Why No Migration Scripts?

Our application uses **`ddl-auto: create-drop`**:
- Database is **recreated** on every restart
- All data is **wiped** on restart
- Seed data is loaded fresh each time

**Therefore**:
- Migration scripts are **meaningless** (DB resets anyway)
- All schema changes go in **entity classes** (Hibernate generates DDL)
- All data changes go in **`dental-clinic-seed-data.sql`**

---

## 🗂️ Directory Structure After Cleanup

```
PDCMS_BE/
├── src/main/resources/db/
│   ├── enums.sql                          ✅ ENUM definitions
│   └── dental-clinic-seed-data.sql        ✅ Seed data (5501 lines)
│
├── docs/
│   └── warehouse-integration/
│       └── TEST_QUERIES.sql               ✅ Documentation (example queries)
│
├── target/classes/db/                     ⚙️ Compiled (auto-generated)
│   ├── enums.sql
│   └── dental-clinic-seed-data.sql
│
└── sql-scripts/                           ❌ DELETED!
    └── fix-invoice-data-integrity...sql   (removed)
```

---

## 📖 How to Add New Entity/Field

### Step 1: Update Entity Class
```java
// src/main/java/.../domain/Invoice.java
@Entity
@Table(name = "invoices")
public class Invoice {
    
    @Column(name = "new_field", length = 100)  // ✅ Add here
    private String newField;
    
    // Hibernate will auto-create column
}
```

### Step 2: Update Seed Data (if needed)
```sql
-- src/main/resources/db/dental-clinic-seed-data.sql

-- Add new column to INSERT statement
INSERT INTO invoices (
    invoice_code, 
    patient_id, 
    new_field  -- ✅ Add here
) VALUES (
    'INV-001', 
    1, 
    'value'
);
```

### Step 3: Restart Application
```bash
docker-compose down -v  # Delete volumes (wipes DB)
docker-compose up -d    # Restart (recreates DB with new schema + seed data)
```

**That's it!** ✅ No migration scripts needed!

---

## 📖 How to Add New ENUM

### Step 1: Update `enums.sql`
```sql
-- src/main/resources/db/enums.sql

CREATE TYPE invoice_status AS ENUM (
    'PENDING',
    'PAID',
    'NEW_STATUS'  -- ✅ Add here
);
```

### Step 2: Update Entity
```java
// src/main/java/.../enums/InvoiceStatus.java
@Enumerated(EnumType.STRING)
public enum InvoiceStatus {
    PENDING,
    PAID,
    NEW_STATUS  // ✅ Add here
}
```

### Step 3: Restart Application
```bash
docker-compose down -v
docker-compose up -d
```

---

## ✅ Verification Checklist

Run this after any changes:

```bash
# 1. Check only 2 SQL files exist in src/main/resources/db/
find src/main/resources/db -name "*.sql" -type f
# Expected output:
#   src/main/resources/db/enums.sql
#   src/main/resources/db/dental-clinic-seed-data.sql

# 2. Check no sql-scripts folder exists
ls -la | grep sql-scripts
# Expected: (no output)

# 3. Check docs SQL files are documentation only
find docs -name "*.sql" -type f
# Expected: docs/warehouse-integration/TEST_QUERIES.sql (OK for docs)

# 4. Compile and verify
./mvnw clean compile -DskipTests
# Expected: BUILD SUCCESS
```

---

## 🚨 Enforcement Rules

### For Developers

1. **DO NOT** create migration scripts (Flyway, Liquibase, custom SQL)
2. **DO NOT** create `sql-scripts/` or similar folders
3. **DO** update entity classes for schema changes
4. **DO** update `dental-clinic-seed-data.sql` for data changes
5. **DO** update `enums.sql` for new ENUMs

### For Code Review

Before merging:
- ✅ Check: No new `.sql` files outside `src/main/resources/db/`
- ✅ Check: No migration scripts
- ✅ Check: Entity classes updated for schema changes
- ✅ Check: Seed data updated if needed

---

## 📚 Related Documents

1. ✅ `docs/INVOICE_DATA_INTEGRITY_ISSUE_AND_FIX.md`
   - Explains why we deleted `sql-scripts/fix-invoice-data-integrity-20260101.sql`
   - Manual SQL fix for production (run once, not a migration)

2. ✅ `src/main/resources/db/dental-clinic-seed-data.sql`
   - Master seed data file (5501 lines)
   - Contains all test data

3. ✅ `src/main/resources/db/enums.sql`
   - All ENUM definitions
   - Runs before Hibernate

---

## 🎯 Summary

| Item | Status | Details |
|------|--------|---------|
| **Policy** | ✅ Enforced | Only 2 SQL files allowed in `src/main/resources/db/` |
| **Cleanup** | ✅ Completed | Deleted `sql-scripts/` folder |
| **Documentation** | ✅ Updated | This document + data integrity fix doc |
| **Build** | ✅ Verified | `./mvnw clean compile` SUCCESS |

---

**Created**: January 1, 2026  
**Last Updated**: January 1, 2026  
**Enforced By**: Backend Team
