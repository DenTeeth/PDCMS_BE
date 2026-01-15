# PDCMS - Private Dental Clinic Management System

**Project Code:** FA25SE202  
**Student Group:** GFA25SE96  
**Semester:** FA25  
**Last Updated:** January 15, 2026

---

## Team Members

| Name | Student ID |
|------|------------|
| Vo Nguyen Minh Quan | SE160914 |
| Doan Nguyen Khoi Nguyen | SE182907 |
| Le Anh Khoa | SE184100 |
| Trinh Cong Thai | SE183743 |

**Supervisor:** Lam Huu Khanh Phuong  
**External Supervisor:** Nguyen Van Chien, Nguyen Xuan Binh

---

## Table of Contents

1. [Software Modules](#1-software-modules)
2. [Technology Stack](#2-technology-stack)
3. [Security - RBAC](#3-security---rbac)
4. [Configuration](#4-configuration)
5. [Demo Accounts](#5-demo-accounts)
6. [Installation Guide](#6-installation-guide)
7. [Documentation Structure](#7-documentation-structure)
8. [Source Code](#8-source-code)

---

## 1. Software Modules

The PDCMS system uses monolithic architecture with Spring Boot, containing 22 modules:

| No | Module | Description |
|----|--------|-------------|
| 1 | Authentication | JWT login, register, password reset, email verification |
| 2 | Patient | Patient management, medical history, blocking |
| 3 | Employee | Staff profiles, contracts, specializations |
| 4 | Appointment | Booking, scheduling, status tracking |
| 5 | Clinical Records | Procedures, prescriptions, attachments, tooth status |
| 6 | Treatment Plan | Multi-phase plans, auto-scheduling, payments |
| 7 | Invoice | Billing, payment tracking, export |
| 8 | Payment | SePay QR integration, webhook, auto-confirmation |
| 9 | Warehouse | Inventory, stock in/out, expiry alerts |
| 10 | Service | Dental services, categories, pricing |
| 11 | Room | Facility management, scheduling |
| 12 | Shift | Fixed/Flex shifts, time slots |
| 13 | Time-off | Leave requests, approvals |
| 14 | Overtime | Overtime tracking, limits (BR-41, BR-42) |
| 15 | Holiday | Annual holidays, auto-skip scheduling |
| 16 | Notification | In-app, email notifications, WebSocket |
| 17 | Feedback | Patient reviews, ratings, statistics |
| 18 | Dashboard | Revenue, appointments, employee analytics |
| 19 | Role | Role management CRUD |
| 20 | Permission | 75 permissions across modules |
| 21 | Chatbot | Gemini AI 2.0, dental consultation |
| 22 | File Upload | Images, documents, X-rays |

---

## 2. Technology Stack

### 2.1 Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming language |
| Spring Boot | 3.2.10 | Application framework |
| Spring Security | 6.x | Authentication and Authorization |
| Spring Data JPA | 3.x | Database ORM |
| PostgreSQL | 15 | Primary database |
| Redis | 7 | Caching |
| JWT (jjwt) | 0.12.6 | Token authentication |
| Lombok | 1.18.x | Boilerplate reduction |
| MapStruct | 1.5.x | Object mapping |
| Apache POI | 5.x | Excel export |
| Springdoc OpenAPI | 2.x | API documentation |

### 2.2 Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| Next.js | 14 | React framework |
| TypeScript | 5.x | Type-safe JavaScript |
| Tailwind CSS | 4.x | Styling |
| TanStack Query | 5.x | Server state |
| React Hook Form | 7.x | Form management |
| Zod | 3.x | Validation |

### 2.3 External Services

| Service | Purpose |
|---------|---------|
| SendGrid/Resend | Email delivery |
| Google Gemini AI | Chatbot (gemini-2.0-flash) |
| SePay | Payment gateway |
| DigitalOcean | Cloud hosting |
| DuckDNS | Dynamic DNS |

### 2.4 DevOps

| Tool | Purpose |
|------|---------|
| Docker | Containerization |
| Docker Compose | Multi-container orchestration |
| GitHub Actions | CI/CD pipeline |
| Nginx Proxy Manager | SSL and Reverse proxy |

---

## 3. Security - RBAC

### 3.1 Overview

PDCMS implements Role-Based Access Control (RBAC):

- 6 predefined roles
- 75 granular permissions
- Method-level security using PreAuthorize
- JWT token with embedded roles/permissions

### 3.2 Roles

| Role | Base | Description |
|------|------|-------------|
| ROLE_ADMIN | admin | Full system access |
| ROLE_MANAGER | employee | Operations and HR management |
| ROLE_DENTIST | employee | Patient treatment |
| ROLE_NURSE | employee | Patient care support |
| ROLE_RECEPTIONIST | employee | Reception and appointments |
| ROLE_PATIENT | patient | View records and book appointments |

### 3.3 Permission Structure

Permissions follow naming convention: ACTION_MODULE

Actions: VIEW, CREATE, UPDATE, DELETE, MANAGE, APPROVE

Example Permissions:

| Permission | Description |
|------------|-------------|
| VIEW_PATIENT | View patient list |
| CREATE_APPOINTMENT | Create appointments |
| APPROVE_TIME_OFF | Approve leave requests |
| MANAGE_WAREHOUSE | Full warehouse access |
| VIEW_DASHBOARD | Access analytics |

### 3.4 Permission Modules (75 total)

| Module | Key Permissions |
|--------|-----------------|
| Patient | VIEW, CREATE, UPDATE, DELETE, BLOCK, UNBLOCK |
| Appointment | VIEW_ALL, VIEW_OWN, CREATE, UPDATE, DELETE |
| Clinical Records | VIEW, CREATE, UPDATE, UPLOAD_ATTACHMENT |
| Treatment Plan | VIEW_ALL, VIEW_OWN, CREATE, UPDATE |
| Invoice | VIEW, CREATE, UPDATE, EXPORT |
| Warehouse | VIEW, CREATE, MANAGE, EXPORT |
| Shift | VIEW, CREATE, MANAGE_FIXED, MANAGE_FLEX |
| Time-off | VIEW_ALL, VIEW_OWN, REQUEST, APPROVE |
| Overtime | VIEW_ALL, REQUEST, APPROVE |
| Dashboard | VIEW, EXPORT |

### 3.5 Implementation Example

```java
// View all patients - requires VIEW_PATIENT permission
@GetMapping("/patients")
@PreAuthorize("hasAuthority('VIEW_PATIENT')")
public ResponseEntity<Page<PatientResponse>> getPatients() { }

// Approve time-off - requires APPROVE_TIME_OFF permission
@PostMapping("/time-off/{id}/approve")
@PreAuthorize("hasAuthority('APPROVE_TIME_OFF')")
public ResponseEntity<TimeOffResponse> approve() { }
```

### 3.6 Role-Permission Matrix

| Permission | ADMIN | MANAGER | DENTIST | NURSE | RECEPTIONIST | PATIENT |
|------------|-------|---------|---------|-------|--------------|---------|
| VIEW_PATIENT | Yes | Yes | Yes | Yes | Yes | No |
| CREATE_APPOINTMENT | Yes | Yes | Yes | Yes | Yes | Yes |
| VIEW_CLINICAL_RECORD | Yes | Yes | Yes | Yes | No | Yes |
| VIEW_DASHBOARD | Yes | Yes | No | No | No | No |
| MANAGE_WAREHOUSE | Yes | Yes | No | No | No | No |
| APPROVE_TIME_OFF | Yes | Yes | No | No | No | No |
| MANAGE_ROLE | Yes | No | No | No | No | No |

---

## 4. Configuration

### 4.1 Environment Variables

```
# Database
DB_USERNAME=root
DB_PASSWORD=123456
DB_DATABASE=dental_clinic_db
DB_PORT=5432

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=redis123

# JWT
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400000

# Email (Resend)
RESEND_API_KEY=re_xxxxxxxxxxxx
MAIL_FROM=noreply@yourdomain.com

# Chatbot (Gemini AI)
GEMINI_API_KEY=AIzaSyxxxxxxxxxx

# Frontend URL
FRONTEND_URL=http://localhost:3000

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://pdcms.vercel.app
```

### 4.2 Application Profiles

| Profile | Purpose | Database |
|---------|---------|----------|
| dev | Local development | localhost PostgreSQL |
| test | Testing | H2 in-memory |
| prod | Production | DigitalOcean PostgreSQL |

### 4.3 Port Configuration

| Service | Port |
|---------|------|
| Backend API | 8080 |
| PostgreSQL | 5432 |
| Redis | 6379 |
| Frontend (Dev) | 3000 |
| Nginx HTTP | 80 |
| Nginx HTTPS | 443 |
| Nginx Admin | 81 |

---

## 5. Demo Accounts

### 5.1 Production URLs

| Service | URL |
|---------|-----|
| Backend API | https://pdcms.duckdns.org/api/v1 |
| Swagger UI | https://pdcms.duckdns.org/swagger-ui.html |
| Frontend | https://pdcms.vercel.app |

### 5.2 Test Accounts

Default Password: 123456

| Role | Username | Email |
|------|----------|-------|
| Admin | admin | admin@dentalclinic.com |
| Manager | quanli1 | quan.vnm@dentalclinic.com |
| Dentist | bacsi1 | khoa.la@dentalclinic.com |
| Dentist | bacsi2 | thai.tc@dentalclinic.com |
| Nurse | yta1 | nguyen.dnkn@dentalclinic.com |
| Nurse | yta2 | khang.nttk@dentalclinic.com |
| Receptionist | letan1 | thuan.dkb@dentalclinic.com |
| Accountant | ketoan1 | thanh.cq@dentalclinic.com |
| Patient | benhnhan1 | phong.dt@email.com |
| Patient | benhnhan2 | phong.pv@email.com |

### 5.3 Login API

```
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

---

## 6. Installation Guide

### 6.1 Prerequisites

- Java 17+
- Docker and Docker Compose
- Node.js 18+ (for frontend)
- Git

### 6.2 Quick Start with Docker

```bash
# 1. Clone repository
git clone https://github.com/DenTeeth/PDCMS_BE.git
cd PDCMS_BE

# 2. Create .env file
cp .env.example .env
# Edit .env with your configurations

# 3. Start all services
docker-compose up -d

# 4. Check status
docker-compose ps
docker-compose logs -f app
```

### 6.3 Manual Installation

```bash
# 1. Start PostgreSQL and Redis
docker-compose up -d postgres redis

# 2. Build application
./mvnw clean package -DskipTests

# 3. Run application
java -jar target/dental-clinic-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 6.4 Verify Installation

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response
{"status":"UP"}
```

### 6.5 Seed Data

Seed data is automatically loaded on first startup:

- Demo users and accounts
- Sample employees and patients
- Dental services with pricing
- Work shifts
- Permissions and role mappings
- Chatbot knowledge base

---

## 7. Documentation Structure

```
docs/
|-- PROJECT_DOCUMENTATION_COMPLETE.md   # This file
|-- README.md                           # Documentation index
|-- API_DOCUMENTATION.md                # API reference
|-- API_ENDPOINTS_WITH_FUNCTION_NAMES_AND_SAMPLES.md
|
|-- api-guides/                         # Detailed API guides by module
|   |-- booking/
|   |-- clinical-records/
|   |-- notification/
|   |-- patient/
|   |-- permission/
|   |-- role/
|   |-- service/
|   |-- shift-management/
|   |-- treatment-plan/
|   +-- warehouse/
|
|-- architecture/                       # System architecture
|   |-- CLINICAL_RECORDS_ATTACHMENTS_FLOW.md
|   |-- CLINICAL_RECORDS_FINAL_DECISION.md
|   +-- CRON_JOB_P8_ARCHITECTURE.md
|
|-- business-rules/                     # Business rule implementations
|   |-- BR-37-WEEKLY-WORKING-HOURS-LIMIT-IMPLEMENTATION.md
|   |-- BR-41-SELF-APPROVAL-PREVENTION.md
|   |-- BR_043_044_DUPLICATE_DETECTION_AND_BLACKLIST_FE_GUIDE.md
|   |-- COMPREHENSIVE_BUSINESS_RULES_AND_CONSTRAINTS_V2_COMPLETE.md
|   +-- FE_APPOINTMENT_BUSINESS_RULES_SUMMARY.md
|
|-- deployment/                         # Production deployment
|   |-- DEPLOY_TO_DIGITALOCEAN_STEP_BY_STEP.md
|   +-- SEPAY_WEBHOOK_PRODUCTION_SETUP.md
|
|-- fe-integration/                     # Frontend integration guides
|   |-- APPOINTMENT_FEEDBACK_FE_INTEGRATION_GUIDE.md
|   |-- BE_4_FE_INTEGRATION_GUIDE.md
|   |-- BE_4_TREATMENT_PLAN_AUTO_SCHEDULING_FE_GUIDE.md
|   |-- DASHBOARD_ADVANCED_FEATURES_FE_GUIDE.md
|   |-- FE_MATERIAL_CONSUMPTION_DETAILED_GUIDE.md
|   |-- FE_PATIENT_DATA_ENHANCEMENT_GUIDE.md
|   |-- FE_SEPAY_PAYMENT_INTEGRATION_GUIDE.md
|   |-- JWT_CLAIMS_REFERENCE_FOR_FE.md
|   |-- NOTIFICATION_SYSTEM_FE_BE_INTEGRATION_GUIDE.md
|   |-- PATIENT_BLOCKING_FIELDS_FE_GUIDE.md
|   |-- PATIENT_UNBAN_FE_INTEGRATION_GUIDE.md
|   |-- PAYMENT_FLOW_DYNAMIC_QR_WEBHOOK.md
|   |-- PHASE_SCHEDULING_AND_ROOM_FILTERING_API_GUIDE.md
|   |-- REQUEST_NOTIFICATION_SYSTEM_FE_INTEGRATION_GUIDE.md
|   +-- TREATMENT_PLAN_FE_TROUBLESHOOTING_GUIDE.md
|
|-- features/                           # Feature documentation
|   |-- CHATBOT_USAGE_GUIDE.md
|   |-- PROCEDURE_MATERIAL_CONSUMPTION_API_GUIDE.md
|   |-- SCHEDULED_JOBS_COMPLETE_GUIDE.md
|   |-- WAREHOUSE_EXPIRY_EMAIL_NOTIFICATION_GUIDE.md
|   +-- WAREHOUSE_MODULE_API_REFERENCE.md
|
|-- setup/                              # Configuration guides
|   |-- EMAIL_CONFIGURATION_GUIDE.md
|   |-- EMAIL_SYSTEM_TROUBLESHOOTING_GUIDE.md
|   |-- SENDGRID_SETUP_GUIDE.md
|   +-- UPDATE_SEED_DATA_GUIDE.md
|
+-- warehouse-integration/              # Warehouse integration
    |-- README.md
    |-- 00_QUICK_START_WAREHOUSE_SERVICE_INTEGRATION.md
    |-- 01_TEST_DATA_SETUP.md
    |-- 02_DATA_FLOW_EXPLAINED.md
    |-- 03_API_TESTING_GUIDE.md
    |-- 04_PERMISSIONS_GUIDE.md
    |-- 05_SAMPLE_SCENARIOS.md
    |-- VI_QUICK_GUIDE.md
    +-- TEST_QUERIES.sql
```

---

## 8. Source Code

### 8.1 Repositories

| Repository | URL | Technology |
|------------|-----|------------|
| Backend | https://github.com/DenTeeth/PDCMS_BE | Spring Boot 3.2.10 |
| Frontend | https://github.com/DenTeeth/PDCMS_FE | Next.js 14 |

### 8.2 Production Server

| Item | Value |
|------|-------|
| Provider | DigitalOcean Droplet |
| OS | Ubuntu 22.04 |
| IP | 157.230.37.20 |
| Domain | pdcms.duckdns.org |
| SSL | Let's Encrypt (via Nginx Proxy Manager) |

### 8.3 Backend Structure

```
src/main/java/com/dental/clinic/management/
|-- authentication/     # Login, JWT, password reset
|-- account/            # User accounts
|-- employee/           # Staff management
|-- patient/            # Patient records
|-- booking_appointment/ # Appointments
|-- treatment_plans/    # Treatment planning
|-- clinical_records/   # Clinical notes
|-- service/            # Dental services
|-- warehouse/          # Inventory
|-- working_schedule/   # Shifts, time-off, overtime
|-- payment/            # Invoices, SePay
|-- notification/       # WebSocket notifications
|-- chatbot/            # Gemini AI
|-- dashboard/          # Analytics
|-- feedback/           # Patient reviews
|-- role/               # Role management
|-- permission/         # Permission management
+-- config/             # App configuration
```

---

## Quick Reference

### API Base URLs

| Environment | URL |
|-------------|-----|
| Development | http://localhost:8080/api/v1 |
| Production | https://pdcms.duckdns.org/api/v1 |

### Common HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 500 | Internal Server Error |

### Support

- Technical Issues: Create GitHub Issue
- Email: support@dentalclinic.com

---

Document Version: 2.0  
Last Updated: January 15, 2026
