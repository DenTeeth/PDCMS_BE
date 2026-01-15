# PDCMS – Private Dental Clinic Management System# PDCMS – Private Dental Clinic Management System# PDCMS – Private Dental Clinic Management System

**Project Code:** FA25SE202**Project Code:** FA25SE202**Project Code:** FA25SE202

**Student Group:** GFA25SE96**Student Group:** GFA25SE96**Student Group:** GFA25SE96

## Team Members## Team Members:## Team Members:

| Name | Student ID |- Võ Nguyễn Minh Quân – SE160914- Võ Nguyễn Minh Quân – SE160914

|------|------------|

| Võ Nguyễn Minh Quân | SE160914 |- Đoàn Nguyễn Khôi Nguyên – SE182907- Đoàn Nguyễn Khôi Nguyên – SE182907

| Đoàn Nguyễn Khôi Nguyên | SE182907 |

| Lê Anh Khoa | SE184100 |- Lê Anh Khoa – SE184100- Lê Anh Khoa – SE184100

| Trịnh Công Thái | SE183743 |

- Trịnh Công Thái – SE183743- Trịnh Công Thái – SE183743

**Supervisor:** Lâm Hữu Khánh Phương

**Supervisor:** Lâm Hữu Khánh Phương**Supervisor:** Lâm Hữu Khánh Phương

**External Supervisor:** Nguyễn Văn Chiến, Nguyễn Xuân Bỉnh

**External Supervisor:** Nguyễn Văn Chiến, Nguyễn Xuân Bỉnh**External Supervisor:** Nguyễn Văn Chiến, Nguyễn Xuân Bỉnh

**Semester:** FA25

**Semester:** FA25**Semester:** FA25

**Last Updated:** January 15, 2026

**January 15, 2026\*\***January 14, 2026\*\*

---

---

## Table of Contents

## Contents## Contents

1. [Software Modules Created in the Project](#1-software-modules-created-in-the-project)

2. [Third-Party Libraries, Frameworks, and Tools](#2-third-party-libraries-frameworks-and-tools)1. [Software Modules Created in the Project](#1-software-modules-created-in-the-project)1. [Software Modules Created in the Project](#1-software-modules-created-in-the-project)

3. [Security Architecture - RBAC](#3-security-architecture---rbac)

4. [Configuration Documentation](#4-configuration-documentation) - 1.1 [Backend Application (Monolithic Architecture)](#11-backend-application-monolithic-architecture) - 1.1 [Backend Application (Monolithic Architecture)](#11-backend-application-monolithic-architecture)

5. [Demo Accounts](#5-demo-accounts)

6. [Installation Guide](#6-installation-guide) - 1.2 [Alternative: Starting Backend with Docker Compose](#12-alternative-starting-backend-with-docker-compose) - 1.2 [Alternative: Starting Backend with Docker Compose](#12-alternative-starting-backend-with-docker-compose)

7. [Documentation Structure](#7-documentation-structure)

8. [Source Code Repositories](#8-source-code-repositories) - 1.3 [Frontend Application](#13-frontend-application) - 1.3 [Frontend Application](#13-frontend-application)

---2. [Third-Party Libraries, Frameworks, and Tools](#2-third-party-libraries-frameworks-and-tools)2. [Third-Party Libraries, Frameworks, and Tools](#2-third-party-libraries-frameworks-and-tools)

## 1. Software Modules Created in the Project - 2.1 [Backend Technologies](#21-backend-technologies) - 2.1 [Backend Technologies](#21-backend-technologies)

The PDCMS system is built on a **monolithic architecture** using Spring Boot, with 22 functional modules organized following domain-driven design. - 2.2 [Frontend Technologies](#22-frontend-technologies) - 2.2 [Frontend Technologies](#22-frontend-technologies)

### 1.1 Module List - 2.3 [Database & Infrastructure](#23-database--infrastructure) - 2.3 [Database & Infrastructure](#23-database--infrastructure)

| # | Module | Description | Key Features | - 2.4 [Email & AI Integration](#24-email--ai-integration) - 2.4 [Email & AI Integration](#24-email--ai-integration)

|---|--------|-------------|--------------|

| 1 | **Authentication** | JWT-based authentication | Login, Register, Password Reset, Email Verification |3. [Security Architecture - RBAC (Role-Based Access Control)](#3-security-architecture---rbac-role-based-access-control)3. [Configuration Documentation](#3-configuration-documentation)

| 2 | **Patient** | Patient management | CRUD, Medical History, Blocking/Unblocking |

| 3 | **Employee** | Staff management | Profiles, Contracts, Specializations | - 3.1 [RBAC Overview](#31-rbac-overview) - 3.1 [Internal Software Component Configuration](#31-internal-software-component-configuration)

| 4 | **Appointment** | Booking system | Scheduling, Status Tracking, Participants |

| 5 | **Clinical Records** | Medical documentation | Procedures, Prescriptions, Attachments, Tooth Status | - 3.2 [Permission Structure](#32-permission-structure) - 3.2 [Third-Party Service Configuration](#32-third-party-service-configuration)

| 6 | **Treatment Plan** | Treatment planning | Multi-phase Plans, Auto-scheduling, Payment Types |

| 7 | **Invoice** | Billing system | Payment Tracking, History, Export | - 3.3 [Role-Permission Mapping](#33-role-permission-mapping)4. [List of All Roles, Username/Password for Demo System](#4-list-of-all-roles-usernamepassword-for-demo-system)

| 8 | **Payment** | SePay integration | QR Code, Webhook, Auto-confirmation |

| 9 | **Warehouse** | Inventory management | Items, Categories, Stock In/Out, Expiry Alerts | - 3.4 [Method-Level Security Implementation](#34-method-level-security-implementation) - 4.1 [System Roles](#41-system-roles)

| 10 | **Service** | Dental services | Categories, Pricing, Constraints |

| 11 | **Room** | Facility management | Room scheduling, Availability | - 3.5 [Permission Modules](#35-permission-modules) - 4.2 [Demo Account List](#42-demo-account-list)

| 12 | **Shift** | Work scheduling | Fixed/Flex shifts, Time slots |

| 13 | **Time-off** | Leave management | Requests, Approvals |4. [Configuration Documentation](#4-configuration-documentation) - 4.3 [Account Testing Guide](#43-account-testing-guide)

| 14 | **Overtime** | Overtime tracking | Requests, Limits (BR-41, BR-42) |

| 15 | **Holiday** | Holiday management | Annual holidays, Auto-skip scheduling | - 4.1 [Internal Software Component Configuration](#41-internal-software-component-configuration)5. [Complete System Installation Guide](#5-complete-system-installation-guide)

| 16 | **Notification** | Alert system | In-app, Email notifications |

| 17 | **Feedback** | Patient reviews | Ratings, Comments, Statistics | - 4.2 [Third-Party Service Configuration](#42-third-party-service-configuration) - 5.1 [Prerequisites](#51-prerequisites)

| 18 | **Dashboard** | Analytics | Revenue, Appointments, Employee stats |

| 19 | **Role** | Role management | CRUD Roles |5. [List of All Roles, Username/Password for Demo System](#5-list-of-all-roles-usernamepassword-for-demo-system) - 5.2 [Quick Installation with Docker Compose (Recommended)](#52-quick-installation-with-docker-compose-recommended)

| 20 | **Permission** | Permission management | 75 permissions across modules |

| 21 | **Chatbot** | AI Assistant | Gemini 2.0, Dental consultation | - 5.1 [System Roles](#51-system-roles) - 5.3 [Manual Installation](#53-manual-installation)

| 22 | **File Upload** | Attachment handling | Images, Documents, X-rays |

- 5.2 [Demo Account List](#52-demo-account-list) - 5.4 [Initial Data Seeding](#54-initial-data-seeding)

### 1.2 Backend Application

- 5.3 [Account Testing Guide](#53-account-testing-guide)6. [Other Related Documents](#6-other-related-documents)

**Technology Stack:**

- Spring Boot 3.2.106. [Complete System Installation Guide](#6-complete-system-installation-guide) - 6.1 [Project Document List](#61-project-document-list)

- Java 17

- Spring Data JPA + Hibernate - 6.1 [Prerequisites](#61-prerequisites) - 6.2 [Source Code Repositories](#62-source-code-repositories)

- Spring Security 6 + JWT OAuth2

- PostgreSQL 15 - 6.2 [Quick Installation with Docker Compose (Recommended)](#62-quick-installation-with-docker-compose-recommended)

- Redis Cache

- Gemini AI 2.0 Flash - 6.3 [Manual Installation](#63-manual-installation)---

**Starting the Backend:** - 6.4 [Initial Data Seeding](#64-initial-data-seeding)

````bash7. [Other Related Documents](#7-other-related-documents)## 1. Software Modules Created in the Project

# Option 1: Maven

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   - 7.1 [Project Document List](#71-project-document-list)



# Option 2: Docker Compose (Recommended)   - 7.2 [Source Code Repositories](#72-source-code-repositories)The PDCMS system is built on a monolithic architecture using Spring Boot, with multiple functional modules organized following domain-driven design. All modules can be installed following the instructions in this document.

docker-compose up -d

```---### 1.1 Backend Application (Monolithic Architecture)



### 1.3 Frontend Application## 1. Software Modules Created in the Project**Function:** REST API backend for dental clinic management system, including patient management, appointments, clinical records, treatment plans, warehouse inventory, employee scheduling, payments, and AI chatbot.



**Technology Stack:**The PDCMS system is built on a monolithic architecture using Spring Boot, with multiple functional modules organized following domain-driven design. All modules can be installed following the instructions in this document.**Technologies:**

- Next.js 14

- TypeScript### 1.1 Backend Application (Monolithic Architecture)- Spring Boot 3.2.10

- Tailwind CSS

- Shadcn/ui- Spring Data JPA + Hibernate



**Starting the Frontend:****Function:** REST API backend for dental clinic management system, including patient management, appointments, clinical records, treatment plans, warehouse inventory, employee scheduling, payments, and AI chatbot.- Spring Security 6 + JWT OAuth2



```bash- Spring WebSocket (Real-time notifications)

cd PDCMS_FE

npm install**Technologies:**- PostgreSQL 13+

npm run dev

```- Redis 7 (Caching)



---- Spring Boot 3.2.10- Java 17



## 2. Third-Party Libraries, Frameworks, and Tools- Spring Data JPA + Hibernate



### 2.1 Backend Dependencies- Spring Security 6 + JWT OAuth2**Port:** 8080



| Library | Version | Purpose |- Spring WebSocket (Real-time notifications)

|---------|---------|---------|

| Spring Boot | 3.2.10 | Application framework |- PostgreSQL 13+**Database:** dental_clinic_db

| Spring Security | 6.x | Authentication & Authorization |

| Spring Data JPA | 3.x | Database ORM |- Redis 7 (Caching)

| PostgreSQL Driver | 42.x | Database connectivity |

| Redis | 7.x | Caching |- Java 17**Source code directory:** `src/main/java/com/dental/clinic/management/`

| JWT (jjwt) | 0.12.6 | Token generation |

| Lombok | 1.18.x | Boilerplate reduction |**Port:** 8080**Core Modules:**

| MapStruct | 1.5.x | Object mapping |

| Apache POI | 5.x | Excel export |**Database:** dental_clinic_db| Module | Directory | Description |

| OpenCSV | 5.x | CSV export |

| Springdoc OpenAPI | 2.x | API documentation || ------------------- | ---------------------- | ----------------------------------------------------- |



### 2.2 External Services**Source code directory:** `src/main/java/com/dental/clinic/management/`| Authentication | `authentication/` | Login, JWT tokens, email verification, password reset |



| Service | Purpose | Configuration || Account | `account/` | User account management and profiles |

|---------|---------|---------------|

| **SendGrid** | Email delivery | SMTP + API Key |**Core Modules:**| Employee | `employee/` | Clinic staff management (Dentists, Nurses, etc.) |

| **Google Gemini AI** | Chatbot | gemini-2.0-flash model |

| **SePay** | Payment gateway | Webhook integration || Patient | `patient/` | Patient records and medical history |

| **DigitalOcean** | Cloud hosting | Droplet + Docker |

| **DuckDNS** | Dynamic DNS | pdcms.duckdns.org || Module | Directory | Description || Booking Appointment | `booking_appointment/` | Appointment scheduling and room management |



### 2.3 DevOps Tools| ------------------- | ---------------------- | ----------------------------------------------------- || Treatment Plans | `treatment_plans/` | Multi-phase treatment planning |



| Tool | Purpose || Authentication | `authentication/` | Login, JWT tokens, email verification, password reset || Clinical Records | `clinical_records/` | Clinical notes, odontogram, attachments |

|------|---------|

| Docker | Containerization || Account | `account/` | User account management and profiles || Service | `service/` | Dental services and procedures catalog |

| Docker Compose | Multi-container orchestration |

| GitHub Actions | CI/CD pipeline || Employee | `employee/` | Clinic staff management (Dentists, Nurses, etc.) || Warehouse | `warehouse/` | Inventory, materials, and stock management |

| Nginx Proxy Manager | SSL + Reverse proxy |

| Patient | `patient/` | Patient records and medical history || Working Schedule | `working_schedule/` | Employee shifts, time-off, overtime requests |

---

| Booking Appointment | `booking_appointment/` | Appointment scheduling and room management || Payment | `payment/` | Invoices, payments, SePay QR integration |

## 3. Security Architecture - RBAC

| Treatment Plans | `treatment_plans/` | Multi-phase treatment planning || Notification | `notification/` | Real-time WebSocket notifications |

### 3.1 Overview

| Clinical Records | `clinical_records/` | Clinical notes, odontogram, attachments || Chatbot | `chatbot/` | FAQ chatbot with Gemini AI |

PDCMS implements **Role-Based Access Control (RBAC)** with:

- 6 predefined roles| Service | `service/` | Dental services and procedures catalog || Dashboard | `dashboard/` | Reports and analytics |

- 75 granular permissions

- Method-level security using `@PreAuthorize`| Warehouse | `warehouse/` | Inventory, materials, and stock management || Feedback | `feedback/` | Patient feedback and reviews |

- JWT token with embedded roles/permissions

| Working Schedule | `working_schedule/` | Employee shifts, time-off, overtime requests |

### 3.2 Role Hierarchy

| Payment | `payment/` | Invoices, payments, SePay QR integration |**Build command:**

````

ROLE_ADMIN (Full access)| Notification | `notification/` | Real-time WebSocket notifications |

    └── ROLE_MANAGER (Management access)

            ├── ROLE_DENTIST (Clinical access)| Chatbot | `chatbot/` | FAQ chatbot with Gemini AI |```bash

            ├── ROLE_NURSE (Support access)

            └── ROLE_RECEPTIONIST (Front desk access)| Dashboard | `dashboard/` | Reports and analytics |cd PDCMS_BE

                    └── ROLE_PATIENT (Self-service access)

```| Feedback | `feedback/` | Patient feedback and reviews |./mvnw clean package -DskipTests

### 3.3 Permission Structure| **Permission** | `permission/` | **RBAC permission management** |```

Permissions follow the naming convention: `{ACTION}_{MODULE}`| **Role** | `role/` | **Role and base role management** |

**Actions:** VIEW, CREATE, UPDATE, DELETE, MANAGE, APPROVE**Run command:**

**Example Permissions:\*\***Build command:\*\*

| Permission | Description |````bash

|------------|-------------|

| `VIEW_PATIENT` | View patient list and details |```bashjava -jar target/dental-clinic-management-0.0.1-SNAPSHOT.jar

| `CREATE_APPOINTMENT` | Create new appointments |

| `APPROVE_TIME_OFF` | Approve time-off requests |cd PDCMS_BE```

| `MANAGE_WAREHOUSE` | Full warehouse access |

| `VIEW_DASHBOARD` | Access dashboard analytics |./mvnw clean package -DskipTests

### 3.4 Permission Modules (75 total)```---

| Module | Permissions |

|--------|-------------|

| Patient | VIEW, CREATE, UPDATE, DELETE, BLOCK, UNBLOCK |**Run command:**### 1.2 Alternative: Starting Backend with Docker Compose

| Appointment | VIEW, CREATE, UPDATE, DELETE, MANAGE |

| Clinical Records | VIEW, CREATE, UPDATE, DELETE, UPLOAD_ATTACHMENT |

| Treatment Plan | VIEW, CREATE, UPDATE, DELETE |

| Invoice | VIEW, CREATE, UPDATE, DELETE, EXPORT |```bashInstead of building and running manually as above, you can use Docker Compose to start all services at once.

| Warehouse | VIEW, CREATE, UPDATE, DELETE, MANAGE, EXPORT |

| Employee | VIEW, CREATE, UPDATE, DELETE |java -jar target/dental-clinic-management-0.0.1-SNAPSHOT.jar

| Shift | VIEW, CREATE, UPDATE, DELETE, MANAGE_FIXED, MANAGE_FLEX |

| Time-off | VIEW, CREATE, UPDATE, DELETE, APPROVE |```**Requirements:**

| Overtime | VIEW, CREATE, UPDATE, DELETE, APPROVE |

| Role | VIEW, CREATE, UPDATE, DELETE |

| Permission | VIEW, ASSIGN |

| Dashboard | VIEW, EXPORT |---- Docker Engine 20.10+

| Notification | VIEW, MANAGE |

| Feedback | VIEW, CREATE, DELETE |- Docker Compose 2.0+

| Service | VIEW, CREATE, UPDATE, DELETE |

| Room | VIEW, CREATE, UPDATE, DELETE |### 1.2 Alternative: Starting Backend with Docker Compose

| Holiday | VIEW, CREATE, UPDATE, DELETE |

**Advantages of this method:**

### 3.5 Implementation Example

Instead of building and running manually as above, you can use Docker Compose to start all services at once.

```java

// Controller method with permission check- Automatically build and start backend with PostgreSQL and Redis

@GetMapping("/patients")

@PreAuthorize("hasAuthority('VIEW_PATIENT')")**Requirements:**- Manage dependencies and correct startup order

public ResponseEntity<Page<PatientResponse>> getPatients(...) {

    // Only users with VIEW_PATIENT permission can access- Automatically create Docker network for services to connect

}

- Docker Engine 20.10+- Easy to scale and deploy to production

// Multiple permissions

@PostMapping("/appointments")- Docker Compose 2.0+

@PreAuthorize("hasAnyAuthority('CREATE_APPOINTMENT', 'MANAGE_APPOINTMENT')")

public ResponseEntity<AppointmentResponse> createAppointment(...) {**Implementation steps:**

    // Users with either permission can create appointments

}**Advantages of this method:**

```

**Step 1:** Navigate to project directory

### 3.6 Role-Permission Matrix

- Automatically build and start backend with PostgreSQL and Redis

| Permission | ADMIN | MANAGER | DENTIST | NURSE | RECEPTIONIST | PATIENT |

|------------|:-----:|:-------:|:-------:|:-----:|:------------:|:-------:|- Manage dependencies and correct startup order```bash

| VIEW_PATIENT | ✓ | ✓ | ✓ | ✓ | ✓ | - |

| CREATE_PATIENT | ✓ | ✓ | - | - | ✓ | - |- Automatically create Docker network for services to connectcd PDCMS_BE

| VIEW_APPOINTMENT | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

| CREATE_APPOINTMENT | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |- Easy to scale and deploy to production```

| VIEW_CLINICAL_RECORD | ✓ | ✓ | ✓ | ✓ | - | ✓ |

| CREATE_CLINICAL_RECORD | ✓ | ✓ | ✓ | ✓ | - | - |

| VIEW_DASHBOARD | ✓ | ✓ | - | - | - | - |

| MANAGE_WAREHOUSE | ✓ | ✓ | - | - | - | - |**Implementation steps:\*\***Step 2:\*\* Ensure file `.env` is configured (see section 3.1.4)

| APPROVE_TIME_OFF | ✓ | ✓ | - | - | - | - |

| MANAGE_ROLE | ✓ | - | - | - | - | - |

---**Step 1:** Navigate to project directory**Step 3:** Run Docker Compose

## 4. Configuration Documentation

### 4.1 Environment Variables`bash`bash

`````bashcd PDCMS_BEdocker compose up --build

# Database

POSTGRES_HOST=localhost````

POSTGRES_PORT=5432

POSTGRES_DB=dental_clinic**Step 2:** Ensure file `.env` is configured (see section 4.1.4)This command will:

POSTGRES_USER=postgres

POSTGRES_PASSWORD=your_password**Step 3:** Run Docker Compose- Build Docker image for backend application



# JWT- Start services in order: PostgreSQL → Redis → Backend App → Nginx Proxy Manager

JWT_SECRET_KEY=your_256_bit_secret_key

JWT_EXPIRATION=86400000````bash- Display logs of all services



# Email (SendGrid)docker compose up --build

SENDGRID_API_KEY=SG.xxxxx

MAIL_FROM=noreply@pdcms.com```**Run in background (detached mode):**



# AI Chatbot

GEMINI_API_KEY=AIzaxxxxx

This command will:```bash

# Redis

REDIS_HOST=localhostdocker compose up --build -d

REDIS_PORT=6379

- Build Docker image for backend application```

# SePay Payment

SEPAY_ACCOUNT_NUMBER=0382498470- Start services in order: PostgreSQL → Redis → Backend App → Nginx Proxy Manager

SEPAY_BANK_CODE=MBBank

```- Display logs of all services**View logs:**



### 4.2 Application Profiles



| Profile | Purpose | Database |**Run in background (detached mode):**```bash

|---------|---------|----------|

| `dev` | Local development | localhost PostgreSQL |docker compose logs -f

| `test` | Testing | H2 in-memory |

| `prod` | Production | DigitalOcean PostgreSQL |```bash```



### 4.3 Docker Compose Servicesdocker compose up --build -d



```yaml```**Stop all services:**

services:

  app:        # Spring Boot application (port 8080)

  postgres:   # PostgreSQL database (port 5432)

  redis:      # Redis cache (port 6379)**View logs:**```bash

  npm:        # Nginx Proxy Manager (ports 80, 443, 81)

```docker compose down



---```bash```



## 5. Demo Accountsdocker compose logs -f



### 5.1 Production URL```---



- **Backend API:** https://pdcms.duckdns.org/api/v1

- **Swagger UI:** https://pdcms.duckdns.org/swagger-ui.html

**Stop all services:**### 1.3 Frontend Application

### 5.2 Test Accounts



| Role | Email | Password |

|------|-------|----------|```bash**Function:** Web application for Admin, Manager, Dentist, Receptionist, Accountant, Nurse, and Patient roles.

| **Admin** | admin@dentalclinic.com | Admin@123 |

| **Manager** | manager@dentalclinic.com | Manager@123 |docker compose down

| **Dentist** | dr.minh@dentalclinic.com | Dentist@123 |

| **Nurse** | nurse.lan@dentalclinic.com | Nurse@123 |```**Technologies:**

| **Receptionist** | receptionist@dentalclinic.com | Reception@123 |

| **Patient** | patient@example.com | Patient@123 |



### 5.3 Login API---- React/Next.js



```bash- TypeScript

POST /api/v1/auth/login

Content-Type: application/json### 1.3 Frontend Application- Tailwind CSS



{- TanStack Query (React Query)

  "email": "admin@dentalclinic.com",

  "password": "Admin@123"**Function:** Web application for Admin, Manager, Dentist, Receptionist, Accountant, Nurse, and Patient roles.- React Hook Form + Zod validation

}

```- Lucide React (icons)



---**Technologies:**- WebSocket (real-time updates)



## 6. Installation Guide



### 6.1 Prerequisites- Next.js 16 with Turbopack**Development Port:** 3000



- Java 17+- React 19

- Docker & Docker Compose

- Node.js 18+ (for frontend)- TypeScript 5**Production URL:** https://pdcms.vercel.app

- Git

- Tailwind CSS 4

### 6.2 Quick Start (Docker)

- TanStack Query (React Query) 5**Source code repository:** Separate frontend repository (private)

```bash

# 1. Clone repository- React Hook Form + Zod validation

git clone https://github.com/DenTeeth/PDCMS_BE.git

cd PDCMS_BE- Radix UI (Headless components)**Install dependencies:**



# 2. Create .env file- Lucide React + FontAwesome (icons)

cp .env.example .env

# Edit .env with your configurations- FullCalendar (Appointment scheduling)```bash



# 3. Start all services- Recharts (Data visualization)cd pdcms-fe

docker-compose up -d

- WebSocket with STOMP.js + SockJSnpm install

# 4. Check status

docker-compose ps- next-intl (Internationalization)```

docker-compose logs -f app

```- Framer Motion (Animations)



### 6.3 Manual Installation**Run development server:**



```bash**Development Port:** 3000

# 1. Start PostgreSQL & Redis

docker-compose up -d postgres redis```bash



# 2. Build application**Production URL:** https://pdcms.vercel.appnpm run dev

./mvnw clean package -DskipTests

`````

# 3. Run application

java -jar target/dental-clinic-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev**Source code repository:** Separate frontend repository (private)

`````

**Build for production:**

### 6.4 Verify Installation

**Install dependencies:**

```bash

# Health check````bash

curl http://localhost:8080/actuator/health

```bashnpm run build

# Expected response

{"status":"UP"}cd PDCMS_FE```

`````

npm install

---

```---

## 7. Documentation Structure



```

docs/**Run development server:**## 2. Third-Party Libraries, Frameworks, and Tools

├── PROJECT_DOCUMENTATION.md # This file - Main documentation

├── API_DOCUMENTATION.md # Complete API reference

├── API_ENDPOINTS_WITH_FUNCTION_NAMES_AND_SAMPLES.md

│```bash### 2.1 Backend Technologies

├── api-guides/ # Detailed API guides by module

│ ├── booking/npm run dev

│ ├── clinical-records/

│ ├── notification/```| Name | Version | Purpose |

│ ├── patient/

│ ├── permission/| ---------------------------- | ------- | ------------------------------------ |

│ ├── role/

│ ├── service/**Build for production:**| Java JDK | 17 | Backend programming language |

│ ├── shift-management/

│ ├── treatment-plan/| Spring Boot | 3.2.10 | Application framework |

│ └── warehouse/

│```bash| Spring Data JPA | 3.2.x | ORM and database access |

├── architecture/ # System architecture docs

│ ├── CLINICAL_RECORDS_ATTACHMENTS_FLOW.mdnpm run build| Spring Security | 6.x | Authentication and authorization |

│ ├── CLINICAL_RECORDS_FINAL_DECISION.md

│ └── CRON_JOB_P8_ARCHITECTURE.md```| Spring WebSocket | 3.2.x | Real-time notifications |

│

├── business-rules/ # Business rule implementations| PostgreSQL JDBC Driver | 42.x | Database connectivity |

│ ├── BR-37-WEEKLY-WORKING-HOURS-LIMIT-IMPLEMENTATION.md

│ ├── BR-41-SELF-APPROVAL-PREVENTION.md---| Maven | 3.9+ | Build tool and dependency management |

│ ├── BR_043_044_DUPLICATE_DETECTION_AND_BLACKLIST_FE_GUIDE.md

│ ├── COMPREHENSIVE_BUSINESS_RULES_AND_CONSTRAINTS_V2_COMPLETE.md| Lombok | 1.18.x | Reduce boilerplate code |

│ └── FE_APPOINTMENT_BUSINESS_RULES_SUMMARY.md

│## 2. Third-Party Libraries, Frameworks, and Tools| JWT (OAuth2 Resource Server) | 6.x | JSON Web Token authentication |

├── deployment/ # Production deployment guides

│ ├── DEPLOY_TO_DIGITALOCEAN_STEP_BY_STEP.md| SpringDoc OpenAPI | 2.5.0 | Swagger API documentation |

│ └── SEPAY_WEBHOOK_PRODUCTION_SETUP.md

│### 2.1 Backend Technologies| Apache POI | 5.4.0 | Excel export functionality |

├── fe-integration/ # Frontend integration guides

│ ├── APPOINTMENT_FEEDBACK_FE_INTEGRATION_GUIDE.md| Apache Commons CSV | 1.10.0 | CSV export functionality |

│ ├── BE_4_FE_INTEGRATION_GUIDE.md

│ ├── BE_4_TREATMENT_PLAN_AUTO_SCHEDULING_FE_GUIDE.md| Name | Version | Purpose || MapStruct | 1.5.x | DTO mapping |

│ ├── DASHBOARD_ADVANCED_FEATURES_FE_GUIDE.md

│ ├── FE_MATERIAL_CONSUMPTION_DETAILED_GUIDE.md| ---------------------------- | ------- | ------------------------------------ |

│ ├── FE_PATIENT_DATA_ENHANCEMENT_GUIDE.md

│ ├── FE_SEPAY_PAYMENT_INTEGRATION_GUIDE.md| Java JDK | 17 | Backend programming language |### 2.2 Frontend Technologies

│ ├── JWT_CLAIMS_REFERENCE_FOR_FE.md

│ ├── NOTIFICATION_SYSTEM_FE_BE_INTEGRATION_GUIDE.md| Spring Boot | 3.2.10 | Application framework |

│ ├── PATIENT_BLOCKING_FIELDS_FE_GUIDE.md

│ ├── PATIENT_UNBAN_FE_INTEGRATION_GUIDE.md| Spring Data JPA | 3.2.x | ORM and database access || Name | Version | Purpose |

│ ├── PAYMENT_FLOW_DYNAMIC_QR_WEBHOOK.md

│ ├── PHASE_SCHEDULING_AND_ROOM_FILTERING_API_GUIDE.md| Spring Security | 6.x | Authentication and authorization || --------------- | ------- | --------------------------- |

│ ├── REQUEST_NOTIFICATION_SYSTEM_FE_INTEGRATION_GUIDE.md

│ └── TREATMENT_PLAN_FE_TROUBLESHOOTING_GUIDE.md| Spring WebSocket | 3.2.x | Real-time notifications || Node.js | 18+ | JavaScript runtime |

│

├── features/ # Feature-specific documentation| PostgreSQL JDBC Driver | 42.x | Database connectivity || React/Next.js | Latest | UI framework |

│ ├── CHATBOT_USAGE_GUIDE.md

│ ├── PROCEDURE_MATERIAL_CONSUMPTION_API_GUIDE.md| Maven | 3.9+ | Build tool and dependency management || TypeScript | 5.x | Type-safe JavaScript |

│ ├── SCHEDULED_JOBS_COMPLETE_GUIDE.md

│ ├── WAREHOUSE_EXPIRY_EMAIL_NOTIFICATION_GUIDE.md| Lombok | 1.18.x | Reduce boilerplate code || Tailwind CSS | 3.4+ | Utility-first CSS framework |

│ └── WAREHOUSE_MODULE_API_REFERENCE.md

│| JWT (OAuth2 Resource Server) | 6.x | JSON Web Token authentication || TanStack Query | 5.x | Server state management |

├── setup/ # Configuration & setup guides

│ ├── EMAIL_CONFIGURATION_GUIDE.md| SpringDoc OpenAPI | 2.5.0 | Swagger API documentation || React Hook Form | 7.x | Form management |

│ ├── EMAIL_SYSTEM_TROUBLESHOOTING_GUIDE.md

│ ├── SENDGRID_SETUP_GUIDE.md| Apache POI | 5.4.0 | Excel export functionality || Zod | 3.x | Schema validation |

│ └── UPDATE_SEED_DATA_GUIDE.md

│| Apache Commons CSV | 1.10.0 | CSV export functionality || Lucide React | Latest | Icon library |

└── warehouse-integration/ # Warehouse module integration

    ├── 00_QUICK_START_WAREHOUSE_SERVICE_INTEGRATION.md| MapStruct                    | 1.5.x   | DTO mapping                          |

    ├── 01_TEST_DATA_SETUP.md

    ├── 02_DATA_FLOW_EXPLAINED.md### 2.3 Database & Infrastructure

    ├── 03_API_TESTING_GUIDE.md

    ├── 04_PERMISSIONS_GUIDE.md### 2.2 Frontend Technologies

    ├── 05_SAMPLE_SCENARIOS.md

    └── README.md| Name                 | Version      | Purpose                          |

````

#### Core Framework & Runtime| -------------------- | ------------ | -------------------------------- |

---

| PostgreSQL           | 13+          | Primary relational database      |

## 8. Source Code Repositories

| Name       | Version | Purpose                              || Redis                | 7-alpine     | Cache and session management     |

| Repository | URL | Description |

|------------|-----|-------------|| ---------- | ------- | ------------------------------------ || Docker               | 20.10+       | Containerization                 |

| **Backend** | https://github.com/DenTeeth/PDCMS_BE | Spring Boot API |

| **Frontend** | https://github.com/DenTeeth/PDCMS_FE | Next.js Application || Node.js    | 20+     | JavaScript runtime                   || Docker Compose       | 2.0+         | Multi-container orchestration    |



---| Next.js    | 16.0+   | React framework with SSR/SSG         || Nginx Proxy Manager  | Latest       | Reverse proxy and SSL management |



## Appendix: Quick Reference| React      | 19.2+   | UI framework                         || DigitalOcean Droplet | Ubuntu 22.04 | Production server hosting        |



### API Base URLs| TypeScript | 5+      | Type-safe JavaScript                 |



| Environment | URL |### 2.4 Email & AI Integration

|-------------|-----|

| Development | http://localhost:8080/api/v1 |#### UI Component Libraries

| Production | https://pdcms.duckdns.org/api/v1 |

| Name               | Version   | Purpose                           |

### Common HTTP Status Codes

| Name                     | Version | Purpose                      || ------------------ | --------- | --------------------------------- |

| Code | Meaning |

|------|---------|| ------------------------ | ------- | ---------------------------- || Resend Java SDK    | 3.0.0     | Production email service          |

| 200 | Success |

| 201 | Created || Radix UI                 | Latest  | Headless component library   || Spring Mail        | 3.2.x     | Email sending abstraction         |

| 400 | Bad Request |

| 401 | Unauthorized || Lucide React             | 0.544+  | Icon library                 || LangChain4J Gemini | 0.35.0    | Gemini AI integration for chatbot |

| 403 | Forbidden |

| 404 | Not Found || FontAwesome              | 7.0+    | Icon library                 || Google AI Gemini   | 2.5-flash | AI model for FAQ responses        |

| 500 | Internal Server Error |

| Framer Motion            | 12.23+  | Animation library            |

### Support Contacts

---

- **Technical Issues:** Create GitHub Issue

- **Email:** support@dentalclinic.com#### Styling



---## 3. Configuration Documentation



*Document Version: 2.0*| Name                     | Version | Purpose                      |

*Last Updated: January 15, 2026*

| ------------------------ | ------- | ---------------------------- |### 3.1 Internal Software Component Configuration

| Tailwind CSS             | 4+      | Utility-first CSS framework  |

| Class Variance Authority | 0.7+    | Component variant management |#### 3.1.1 Database Configuration

| clsx                     | 2.1+    | Classname utility            |

| tailwind-merge           | 3.3+    | Merge Tailwind classes       |**PostgreSQL Connection:**

| next-themes              | 0.4+    | Theme management (dark/light)|

Create database for the system:

#### Form & Validation

```sql

| Name               | Version | Purpose                 |CREATE DATABASE dental_clinic_db;

| ------------------ | ------- | ----------------------- |```

| React Hook Form    | 7.65+   | Form management         |

| Zod                | 4.1+    | Schema validation       |**Connection String Format:**

| @hookform/resolvers| 5.2+    | Form validation resolver|

````

#### Data Visualization & Calendarjdbc:postgresql://localhost:5432/dental_clinic_db

Username: root (or postgres)

| Name | Version | Purpose |Password: your_password

| ------------ | ------- | -------------------- |```

| Recharts | 3.3+ | Chart library |

| FullCalendar | 6.1+ | Calendar component |#### 3.1.2 API Ports Configuration

#### Date & Time**Port list for services:**

| Name | Version | Purpose || Service Name | Port | URL |

| ---------------- | ------- | --------------------- || --------------------------- | ---- | --------------------- |

| date-fns | 4.1+ | Date utility library || Backend Application | 8080 | http://localhost:8080 |

| React Day Picker | 9.11+ | Date picker component || PostgreSQL Database | 5432 | localhost:5432 |

| Redis Cache | 6379 | localhost:6379 |

#### State Management & HTTP| Nginx Proxy Manager (HTTP) | 80 | http://localhost:80 |

| Nginx Proxy Manager (HTTPS) | 443 | https://localhost:443 |

| Name | Version | Purpose || Nginx Admin Panel | 81 | http://localhost:81 |

| -------------- | ------- | ------------------------ || Frontend Application (Dev) | 3000 | http://localhost:3000 |

| TanStack Query | 5.90+ | Server state management |

| Axios | 1.13+ | HTTP client |**Note:** All API requests should go through the backend application port (8080). In production, Nginx Proxy Manager handles SSL termination.

| GraphQL | 16.12+ | Query language |

| graphql-request| 7.4+ | GraphQL client |#### 3.1.3 JWT Token Configuration

#### Real-time Communication**File:** `src/main/resources/application.yaml`

| Name | Version | Purpose |```yaml

| ------------- | ------- | ------------------------ |dentalclinic:

| @stomp/stompjs| 7.2+ | WebSocket STOMP client | jwt:

| sockjs-client | 1.6+ | WebSocket fallback | base64-secret: your-256-bit-secret-key-change-in-production

    # 15 minutes = 900 seconds (access token)

#### File & Media Handling access-token-validity-in-seconds: 9000

    # 30 days = 2592000 seconds (refresh token)

| Name | Version | Purpose | refresh-token-validity-in-seconds: 2592000

| ----------------------- | ------- | ------------------------------ |```

| ExcelJS | 4.4+ | Excel file generation |

| Cloudinary | 2.8+ | Image/video management |**Token Generation:** Performed by Authentication module after successful login.

| next-cloudinary | 6.17+ | Cloudinary Next.js integration |

| React Lazy Load Image | 1.6+ | Image lazy loading |**Token Validation:** Performed by Spring Security filter for all protected routes.

#### Medical Imaging#### 3.1.4 Environment Variables - Backend Application

| Name | Version | Purpose |**File:** `.env` (root directory)

| --------------- | ------- | ------------------------- |

| @niivue/niivue | 0.65+ | NIfTI medical image viewer|```env

# Spring Profile

#### Authentication & CookiesSPRING_PROFILES_ACTIVE=prod

| Name | Version | Purpose |# Database Configuration

| ---------- | ------- | ------------------ |DB_USERNAME=root

| jwt-decode | 4.0+ | JWT token decoder |DB_PASSWORD=123456

| js-cookie | 3.0+ | Cookie management |DB_DATABASE=dental_clinic_db

DB_PORT=5432

#### Internationalization & Notifications

# Redis Configuration

| Name | Version | Purpose |REDIS_HOST=redis

| --------- | ------- | -------------------- |REDIS_PORT=6379

| next-intl | 4.4+ | i18n for Next.js |REDIS_PASSWORD=redis123

| Sonner | 2.0+ | Toast notifications |

# JWT Configuration

### 2.3 Database & InfrastructureJWT_SECRET=your-256-bit-secret-key-change-in-production

JWT_EXPIRATION=86400000

| Name | Version | Purpose |JWT_REFRESH_EXPIRATION=2592000000

| -------------------- | ------------ | -------------------------------- |

| PostgreSQL | 13+ | Primary relational database |# Email Configuration (Resend)

| Redis | 7-alpine | Cache and session management |RESEND_API_KEY=re_xxxxxxxxxxxxxxxxxxxxxxxxxxxx

| Docker | 20.10+ | Containerization |MAIL_FROM=noreply@yourdomain.com

| Docker Compose | 2.0+ | Multi-container orchestration |MAIL_REPLY_TO=support@yourdomain.com

| Nginx Proxy Manager | Latest | Reverse proxy and SSL management |

| DigitalOcean Droplet | Ubuntu 22.04 | Production server hosting |# Frontend URL (for email links)

FRONTEND_URL=https://pdcms.vercel.app

### 2.4 Email & AI Integration

# CORS Configuration

| Name | Version | Purpose |CORS_ALLOWED_ORIGINS=http://localhost:3000,https://pdcms.vercel.app

| ------------------ | --------- | --------------------------------- |

| Resend Java SDK | 3.0.0 | Production email service |# Chatbot Configuration (Gemini AI)

| Spring Mail | 3.2.x | Email sending abstraction |GEMINI_API_KEY=AIzaSyxxxxxxxxxxxxxxxxxxxxxxxxx

| LangChain4J Gemini | 0.35.0 | Gemini AI integration for chatbot |

| Google AI Gemini | 2.0-flash | AI model for FAQ responses |# Application Port

APP_PORT=8080

---```

## 3. Security Architecture - RBAC (Role-Based Access Control)#### 3.1.5 Environment Variables - Frontend Application

### 3.1 RBAC Overview**File:** `.env.local` (frontend directory)

The PDCMS system implements a comprehensive **Role-Based Access Control (RBAC)** system with the following components:```env

# API Configuration

````NEXT_PUBLIC_API_BASE_URL=http://localhost:8080

┌─────────────────────────────────────────────────────────────┐NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws

│                    RBAC ARCHITECTURE                        │

├─────────────────────────────────────────────────────────────┤# For production deployment:

│                                                             │# NEXT_PUBLIC_API_BASE_URL=https://pdcms.duckdns.org

│   ┌─────────┐     ┌─────────┐     ┌─────────────────┐      │# NEXT_PUBLIC_WS_URL=wss://pdcms.duckdns.org/ws

│   │  User   │────▶│  Role   │────▶│   Permissions   │      │```

│   └─────────┘     └─────────┘     └─────────────────┘      │

│        │               │                   │                │---

│        │               │                   │                │

│        ▼               ▼                   ▼                │### 3.2 Third-Party Service Configuration

│   ┌─────────┐     ┌─────────┐     ┌─────────────────┐      │

│   │ Account │     │BaseRole │     │    @PreAuthorize │      │#### 3.2.1 Resend Email Configuration

│   │(Patient/│     │ (admin/ │     │  Method-Level    │      │

│   │Employee)│     │employee/│     │    Security      │      │**Purpose:** Send verification emails, password reset, appointment reminders to patients.

│   └─────────┘     │ patient)│     └─────────────────┘      │

│                   └─────────┘                               │**Services using:** Authentication, Patient Management, Notification

│                                                             │

└─────────────────────────────────────────────────────────────┘**Required configuration:**

````

- Resend API Key

**Key Features:**- Verified domain email address

- Reply-to email address

1. **Hierarchical Role Structure**: BaseRole → Role → Permission

2. **Fine-grained Permissions**: 75+ permissions across 20 modules**How to obtain API key:**

3. **Method-Level Security**: Using Spring Security `@PreAuthorize`

4. **Dynamic Permission Assignment**: Admin can assign/revoke permissions via API1. Register account at https://resend.com

5. **Permission Inheritance**: Parent-child permission hierarchy (e.g., VIEW_ALL includes VIEW_OWN)2. Go to Dashboard → API Keys

6. Create new API Key

### 3.2 Permission Structure4. Verify your domain (add DNS records)

**Database Schema:\*\***Environment variables:\*\*

`sql`env

-- Permissions TableRESEND_API_KEY=re_xxxxxxxxxxxxxxxxxxxxxxxxxxxx

CREATE TABLE permissions (MAIL_FROM=noreply@yourdomain.com

    permission_id VARCHAR(50) PRIMARY KEY,MAIL_REPLY_TO=support@yourdomain.com

    permission_name VARCHAR(100) NOT NULL,```

    module VARCHAR(20) NOT NULL,

    description TEXT,#### 3.2.2 Gemini AI Configuration

    display_order INTEGER,

    parent_permission_id VARCHAR(50),  -- For hierarchical permissions**Purpose:** AI chatbot for patient FAQ about dental services and pricing.

    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP**Service using:** Chatbot module

);

**Required configuration:**

-- Roles Table

CREATE TABLE roles (- Google AI API Key

    role_id VARCHAR(50) PRIMARY KEY,- Model name (gemini-2.5-flash)

    role_name VARCHAR(50) NOT NULL,

    description TEXT,**How to obtain API key:**

    base_role_id VARCHAR(20) NOT NULL,  -- FK to base_roles

    requires_specialization BOOLEAN DEFAULT FALSE,1. Access https://aistudio.google.com

    is_active BOOLEAN DEFAULT TRUE,2. Create new API Key

    created_at TIMESTAMP3. Copy and save the key

);

**Environment variables:**

-- Role-Permission Mapping (Many-to-Many)

CREATE TABLE role_permissions (```env

    role_id VARCHAR(50) NOT NULL,GEMINI_API_KEY=AIzaSyxxxxxxxxxxxxxxxxxxxxxxxxx

    permission_id VARCHAR(50) NOT NULL,```

    PRIMARY KEY (role_id, permission_id),

    FOREIGN KEY (role_id) REFERENCES roles(role_id),#### 3.2.3 SePay Payment Configuration

    FOREIGN KEY (permission_id) REFERENCES permissions(permission_id)

);**Purpose:** Dynamic QR payment integration for patient invoices.

````

**Service using:** Payment module

**Permission Entity (Java):**

**Required configuration:**

```java

@Entity- SePay API Key

@Table(name = "permissions")- Webhook Secret

public class Permission {- Bank Account Information

    @Id

    private String permissionId;**Environment variables:**



    private String permissionName;```env

    private String module;SEPAY_API_KEY=your-sepay-api-key

    private String description;SEPAY_WEBHOOK_SECRET=your-webhook-secret

    private Integer displayOrder;SEPAY_BANK_CODE=your-bank-code

    SEPAY_ACCOUNT_NUMBER=your-account-number

    @ManyToOne```

    @JoinColumn(name = "parent_permission_id")

    private Permission parentPermission;  // Hierarchical permissions#### 3.2.4 Redis Cache Configuration



    private Boolean isActive = true;**Purpose:** Caching layer for improved performance and session management.



    @ManyToMany(mappedBy = "permissions")**Service using:** All modules (authentication tokens, API responses)

    private Set<Role> roles = new HashSet<>();

}**How to install Redis:**

````

**Option 1: Docker (Recommended)**

### 3.3 Role-Permission Mapping

```bash

**Base Roles:**docker run -d --name redis -p 6379:6379 redis:7-alpine redis-server --requirepass redis123

```

| Base Role | Description |

| --------- | ------------------------------ |**Option 2: Using docker-compose.yml (included in project)**

| admin | System administrators |

| employee | Clinic staff (dentist, nurse) |Redis is automatically started when running `docker compose up`.

| patient | Patients with limited access |

**Environment variables:**

**System Roles and Their Permissions:**

````env

| Role                   | Key Permissions                                           |REDIS_HOST=localhost

| ---------------------- | --------------------------------------------------------- |REDIS_PORT=6379

| ROLE_ADMIN             | ALL permissions (automatically granted)                   |REDIS_PASSWORD=redis123

| ROLE_MANAGER           | APPROVE_*, VIEW_*, MANAGE_SCHEDULE, VIEW_DASHBOARD        |```

| ROLE_DENTIST           | VIEW_PATIENT, MANAGE_TREATMENT_PLAN, WRITE_CLINICAL_*     |

| ROLE_NURSE             | VIEW_PATIENT, VIEW_APPOINTMENT_OWN, VIEW_CLINICAL_RECORD  |---

| ROLE_RECEPTIONIST      | VIEW_APPOINTMENT_ALL, CREATE_APPOINTMENT, MANAGE_PATIENT  |

| ROLE_ACCOUNTANT        | VIEW_INVOICE, MANAGE_PAYMENT, VIEW_DASHBOARD              |## 4. List of All Roles, Username/Password for Demo System

| ROLE_INVENTORY_MANAGER | MANAGE_WAREHOUSE, VIEW_WAREHOUSE, VIEW_TRANSACTION        |

| ROLE_DENTIST_INTERN    | VIEW_PATIENT (restricted), VIEW_CLINICAL_RECORD           |### 4.1 System Roles

| ROLE_PATIENT           | VIEW_OWN_*, CREATE_APPOINTMENT, VIEW_INVOICE              |

The PDCMS system has 9 main roles organized into 3 base categories:

**Sample Role-Permission Assignment (SQL):**

| No. | Role Name              | Base Role | Description                                         |

```sql| --- | ---------------------- | --------- | --------------------------------------------------- |

-- Admin gets ALL permissions| 1   | ROLE_ADMIN             | admin     | System Administrator - Full system access           |

INSERT INTO role_permissions (role_id, permission_id)| 2   | ROLE_MANAGER           | employee  | Manager - Operations and HR management              |

SELECT 'ROLE_ADMIN', permission_id FROM permissions WHERE is_active = TRUE;| 3   | ROLE_DENTIST           | employee  | Dentist - Patient examination and treatment         |

| 4   | ROLE_NURSE             | employee  | Nurse - Patient care and treatment support          |

-- Dentist permissions| 5   | ROLE_RECEPTIONIST      | employee  | Receptionist - Reception and appointment management |

INSERT INTO role_permissions (role_id, permission_id) VALUES| 6   | ROLE_ACCOUNTANT        | employee  | Accountant - Finance and payment management         |

('ROLE_DENTIST', 'VIEW_PATIENT'),| 7   | ROLE_INVENTORY_MANAGER | employee  | Inventory Manager - Warehouse and supplies          |

('ROLE_DENTIST', 'MANAGE_PATIENT'),| 8   | ROLE_DENTIST_INTERN    | employee  | Dental Intern - Training and supervised practice    |

('ROLE_DENTIST', 'VIEW_APPOINTMENT_OWN'),| 9   | ROLE_PATIENT           | patient   | Patient - View records and book appointments        |

('ROLE_DENTIST', 'WRITE_CLINICAL_RECORD'),

('ROLE_DENTIST', 'MANAGE_TREATMENT_PLAN'),### 4.2 Demo Account List

('ROLE_DENTIST', 'VIEW_TREATMENT_PLAN_OWN');

```**IMPORTANT NOTES:**



### 3.4 Method-Level Security Implementation- All default passwords: `123456`

- These accounts are for demo/testing environment only

**Using @PreAuthorize Annotation:**- In production, all passwords must be changed

- JWT secret key must be randomly generated and secured

```java

@Service#### 4.2.1 Admin Account

public class PatientService {

| Role  | Username | Email                  | Password | Description                        |

    // Only Admin or users with VIEW_PATIENT permission| ----- | -------- | ---------------------- | -------- | ---------------------------------- |

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_PATIENT')")| ADMIN | admin    | admin@dentalclinic.com | 123456   | System Administrator (full access) |

    public Page<PatientResponse> getAllPatients(Pageable pageable) {

        return patientRepository.findAll(pageable).map(this::toResponse);**Permissions:**

    }

- Manage all users and roles

    // Only Admin or users with MANAGE_PATIENT permission- Create/edit/delete employees and patients

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_PATIENT')")- View all reports and analytics

    public PatientResponse createPatient(CreatePatientRequest request) {- Configure entire system

        // ... create logic

    }**Screen after login:** `/admin/dashboard`



    // Only Admin or users with DELETE_PATIENT permission#### 4.2.2 Manager Account

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DELETE_PATIENT')")

    public void deletePatient(Long patientId) {| Username | Email                     | Password | Description    |

        // ... delete logic| -------- | ------------------------- | -------- | -------------- |

    }| quanli1  | quan.vnm@dentalclinic.com | 123456   | Clinic Manager |

}

```**Permissions:**



**Common Permission Patterns:**- Manage employee work schedules

- Approve time-off and overtime requests

```java- View revenue reports

// View all (Manager/Receptionist level)- Manage shift registrations

@PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_APPOINTMENT_ALL')")

**Screen after login:** `/manager/dashboard`

// View own only (Dentist/Patient level)

@PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_APPOINTMENT_OWN')")#### 4.2.3 Dentist Accounts



// Combined role + permission check| Username | Email                    | Password | Employment Type |

@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or hasAuthority('APPROVE_TIME_OFF')")| -------- | ------------------------ | -------- | --------------- |

| bacsi1   | khoa.la@dentalclinic.com | 123456   | FULL_TIME       |

// Multiple permissions (OR)| bacsi2   | thai.tc@dentalclinic.com | 123456   | FULL_TIME       |

@PreAuthorize("hasAuthority('VIEW_CLINICAL_RECORD') or hasAuthority('WRITE_CLINICAL_RECORD')")| bacsi3   | jimmy.d@dentalclinic.com | 123456   | PART_TIME_FLEX  |

```| bacsi4   | junya.o@dentalclinic.com | 123456   | PART_TIME_FIXED |



### 3.5 Permission Modules**Permissions:**



The system has **75 permissions** organized into **20 modules**:- View and update patient records

- Create and manage treatment plans

| No. | Module           | Permissions                                                | Description                        |- Write clinical records

| --- | ---------------- | ---------------------------------------------------------- | ---------------------------------- |- View own appointment schedule

| 1   | DASHBOARD        | VIEW_DASHBOARD, VIEW_REPORTS                               | Dashboard & analytics              |- Create prescriptions

| 2   | EMPLOYEE         | VIEW_EMPLOYEE, MANAGE_EMPLOYEE, DELETE_EMPLOYEE            | Staff management                   |

| 3   | PATIENT          | VIEW_PATIENT, MANAGE_PATIENT, DELETE_PATIENT               | Patient records                    |**Screen after login:** `/dentist/dashboard`

| 4   | APPOINTMENT      | VIEW_APPOINTMENT_ALL, VIEW_APPOINTMENT_OWN, CREATE_*, etc. | Appointment scheduling             |

| 5   | CLINICAL_RECORDS | WRITE_CLINICAL_RECORD, VIEW_ATTACHMENT, MANAGE_ATTACHMENTS | Medical records                    |#### 4.2.4 Nurse Accounts

| 6   | PATIENT_IMAGES   | PATIENT_IMAGE_READ, MANAGE_PATIENT_IMAGES                  | Medical images                     |

| 7   | NOTIFICATION     | VIEW_NOTIFICATION, MANAGE_NOTIFICATION                     | System notifications               || Username | Email                        | Password | Employment Type |

| 8   | HOLIDAY          | VIEW_HOLIDAY, MANAGE_HOLIDAY                               | Holiday calendar                   || -------- | ---------------------------- | -------- | --------------- |

| 9   | SERVICE          | VIEW_SERVICE, MANAGE_SERVICE                               | Dental services                    || yta1     | nguyen.dnkn@dentalclinic.com | 123456   | FULL_TIME       |

| 10  | TREATMENT_PLAN   | VIEW_TREATMENT_PLAN_ALL, MANAGE_TREATMENT_PLAN             | Treatment planning                 || yta2     | khang.nttk@dentalclinic.com  | 123456   | FULL_TIME       |

| 11  | WAREHOUSE        | VIEW_WAREHOUSE, MANAGE_WAREHOUSE, VIEW_TRANSACTION         | Inventory management               || yta3     | nhat.htqn@dentalclinic.com   | 123456   | PART_TIME_FIXED |

| 12  | INVOICE          | VIEW_INVOICE, CREATE_INVOICE, MANAGE_PAYMENT               | Financial records                  || yta4     | chinh.nd@dentalclinic.com    | 123456   | PART_TIME_FLEX  |

| 13  | SCHEDULE         | VIEW_SCHEDULE_ALL, VIEW_SCHEDULE_OWN, MANAGE_SCHEDULE      | Employee scheduling                |

| 14  | REGISTRATION     | VIEW_REGISTRATION_ALL, CREATE_REGISTRATION, APPROVE_*      | Shift registration                 |**Permissions:**

| 15  | TIME_OFF         | VIEW_TIME_OFF_ALL, REQUEST_TIME_OFF, APPROVE_TIME_OFF      | Leave management                   |

| 16  | OVERTIME         | VIEW_OVERTIME_ALL, REQUEST_OVERTIME, APPROVE_OVERTIME      | Overtime tracking                  |- View patient records (read-only)

| 17  | ACCOUNT          | VIEW_OWN_PROFILE, MANAGE_ACCOUNT                           | User accounts                      |- Support patient check-in

| 18  | ROLE             | VIEW_ROLE, MANAGE_ROLE                                     | Role administration                |- View own work schedule

| 19  | PERMISSION       | VIEW_PERMISSION, ASSIGN_PERMISSION                         | Permission management              |

| 20  | FEEDBACK         | VIEW_FEEDBACK, CREATE_FEEDBACK                             | Patient feedback                   |**Screen after login:** `/nurse/dashboard`



**Permission API Endpoints:**#### 4.2.5 Receptionist Account



| Method | Endpoint                              | Description                    || Username | Email                      | Password | Description         |

| ------ | ------------------------------------- | ------------------------------ || -------- | -------------------------- | -------- | ------------------- |

| GET    | `/api/v1/permissions`                 | List all permissions           || letan1   | thuan.dkb@dentalclinic.com | 123456   | Clinic Receptionist |

| GET    | `/api/v1/permissions/hierarchy`       | Get permission hierarchy       |

| GET    | `/api/v1/permissions/{id}`            | Get permission by ID           |**Permissions:**

| POST   | `/api/v1/permissions`                 | Create new permission          |

| PUT    | `/api/v1/permissions/{id}`            | Update permission              |- Manage appointments (create/edit/cancel)

| GET    | `/api/v1/roles/{roleId}/permissions`  | Get permissions for a role     |- Register new patients

| POST   | `/api/v1/roles/{roleId}/permissions`  | Assign permissions to role     |- Check-in patients

| DELETE | `/api/v1/roles/{roleId}/permissions`  | Revoke permissions from role   |- View all appointments



---**Screen after login:** `/receptionist/dashboard`



## 4. Configuration Documentation#### 4.2.6 Accountant Account



### 4.1 Internal Software Component Configuration| Username | Email                     | Password | Description       |

| -------- | ------------------------- | -------- | ----------------- |

#### 4.1.1 Database Configuration| ketoan1  | thanh.cq@dentalclinic.com | 123456   | Clinic Accountant |



**PostgreSQL Connection:****Permissions:**



Create database for the system:- Create and manage invoices

- Process payments

```sql- View financial reports

CREATE DATABASE dental_clinic_db;- Export financial data

````

**Screen after login:** `/accountant/dashboard`

**Connection String Format:**

#### 4.2.7 Patient Accounts

````

jdbc:postgresql://localhost:5432/dental_clinic_db| Username  | Email              | Password | Patient Code |

Username: root (or postgres)| --------- | ------------------ | -------- | ------------ |

Password: your_password| benhnhan1 | phong.dt@email.com | 123456   | BN-1001      |

```| benhnhan2 | phong.pv@email.com | 123456   | BN-1002      |

| benhnhan3 | anh.nt@email.com   | 123456   | BN-1003      |

#### 4.1.2 API Ports Configuration| benhnhan4 | mit.bit@email.com  | 123456   | BN-1004      |



**Port list for services:****Permissions:**



| Service Name                | Port | URL                   |- View personal profile

| --------------------------- | ---- | --------------------- |- Book appointments

| Backend Application         | 8080 | http://localhost:8080 |- View treatment history

| PostgreSQL Database         | 5432 | localhost:5432        |- View own invoices

| Redis Cache                 | 6379 | localhost:6379        |- Chat with AI chatbot

| Nginx Proxy Manager (HTTP)  | 80   | http://localhost:80   |

| Nginx Proxy Manager (HTTPS) | 443  | https://localhost:443 |**Screen after login:** `/patient/dashboard`

| Nginx Admin Panel           | 81   | http://localhost:81   |

| Frontend Application (Dev)  | 3000 | http://localhost:3000 |### 4.3 Account Testing Guide



**Note:** All API requests should go through the backend application port (8080). In production, Nginx Proxy Manager handles SSL termination.#### 4.3.1 Test Admin Account



#### 4.1.3 JWT Token Configuration1. Access: `http://localhost:3000/login`

2. Login with: `admin` / `123456`

**File:** `src/main/resources/application.yaml`3. Verify redirect to: `/admin/dashboard`

4. Test features:

```yaml   - Create/edit/delete users

dentalclinic:   - Manage roles and permissions

  jwt:   - View all branches

    base64-secret: your-256-bit-secret-key-change-in-production   - View system reports

    # 15 minutes = 900 seconds (access token)

    access-token-validity-in-seconds: 9000#### 4.3.2 Test Dentist Account

    # 30 days = 2592000 seconds (refresh token)

    refresh-token-validity-in-seconds: 25920001. Access: `http://localhost:3000/login`

```2. Login with: `bacsi1` / `123456`

3. Verify redirect to: `/dentist/dashboard`

**Token Generation:** Performed by Authentication module after successful login.4. Test features:

   - View today's appointments

**Token Validation:** Performed by Spring Security filter for all protected routes.   - Open patient records

   - Create treatment plan

**JWT Claims Structure (for RBAC):**   - Write clinical record



```json#### 4.3.3 Test Patient Account

{

  "sub": "admin",1. Access: `http://localhost:3000/login`

  "accountId": 1,2. Login with: `benhnhan1` / `123456`

  "baseRole": "admin",3. Verify redirect to: `/patient/dashboard`

  "role": "ROLE_ADMIN",4. Test features:

  "permissions": [   - View personal profile

    "VIEW_PATIENT",   - Book new appointment

    "MANAGE_PATIENT",   - View treatment history

    "VIEW_APPOINTMENT_ALL",   - Chat with AI chatbot

    "..."

  ],---

  "iat": 1737028800,

  "exp": 1737115200## 5. Complete System Installation Guide

}

```### 5.1 Prerequisites



#### 4.1.4 Environment Variables - Backend ApplicationBefore installation, ensure your computer has:



**File:** `.env` (root directory)- **Java Development Kit (JDK) 17** or higher

- **Docker & Docker Compose** (recommended)

```env- **PostgreSQL 13+** (if not using Docker)

# Spring Profile- **Redis 7+** (if not using Docker)

SPRING_PROFILES_ACTIVE=prod- **Maven 3.9+** (or use included Maven Wrapper)

- **Git** for version control

# Database Configuration- **Node.js 18+** (for frontend development)

DB_USERNAME=root

DB_PASSWORD=123456### 5.2 Quick Installation with Docker Compose (Recommended)

DB_DATABASE=dental_clinic_db

DB_PORT=5432**Step 1:** Clone repository



# Redis Configuration```bash

REDIS_HOST=redisgit clone https://github.com/DenTeeth/PDCMS_BE.git

REDIS_PORT=6379cd PDCMS_BE

REDIS_PASSWORD=redis123```



# JWT Configuration**Step 2:** Configure environment variables

JWT_SECRET=your-256-bit-secret-key-change-in-production

JWT_EXPIRATION=86400000Create file `.env` in root directory:

JWT_REFRESH_EXPIRATION=2592000000

```env

# Email Configuration (Resend)# Database

RESEND_API_KEY=re_xxxxxxxxxxxxxxxxxxxxxxxxxxxxDB_USERNAME=root

MAIL_FROM=noreply@yourdomain.comDB_PASSWORD=123456

MAIL_REPLY_TO=support@yourdomain.comDB_DATABASE=dental_clinic_db

DB_PORT=5432

# Frontend URL (for email links)

FRONTEND_URL=https://pdcms.vercel.app# Redis

REDIS_PASSWORD=redis123

# CORS Configuration

CORS_ALLOWED_ORIGINS=http://localhost:3000,https://pdcms.vercel.app# Email (Resend)

RESEND_API_KEY=re_xxxxxxxxxxxx

# Chatbot Configuration (Gemini AI)MAIL_FROM=noreply@yourdomain.com

GEMINI_API_KEY=AIzaSyxxxxxxxxxxxxxxxxxxxxxxxxxMAIL_REPLY_TO=support@yourdomain.com



# Application Port# Frontend URL

APP_PORT=8080FRONTEND_URL=http://localhost:3000

````

# Chatbot (Gemini AI)

#### 4.1.5 Environment Variables - Frontend ApplicationGEMINI_API_KEY=AIzaSyxxxxxxxxxx

**File:** `.env.local` (frontend directory)# JWT

JWT_SECRET=change-this-to-random-256-bit-secret

`env`

# API Configuration

NEXT_PUBLIC_API_BASE_URL=http://localhost:8080**Step 3:** Start all services

NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws

````bash

# Cloudinary Configuration (Image uploads)docker compose up --build

NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME=your-cloud-name```

NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET=your-upload-preset

**Step 4:** Verify services

# For production deployment:

# NEXT_PUBLIC_API_BASE_URL=https://pdcms.duckdns.orgCheck API health:

# NEXT_PUBLIC_WS_URL=wss://pdcms.duckdns.org/ws

````

http://localhost:8080/actuator/health

---```

### 4.2 Third-Party Service ConfigurationExpected response:

#### 4.2.1 Resend Email Configuration```json

{

**Purpose:** Send verification emails, password reset, appointment reminders to patients. "status": "UP"

}

**Services using:** Authentication, Patient Management, Notification```

**Required configuration:\*\***Step 5:\*\* Access applications

- Resend API Key- **Swagger UI:** http://localhost:8080/swagger-ui.html

- Verified domain email address- **API Base URL:** http://localhost:8080/api/v1

- Reply-to email address- **Nginx Admin Panel:** http://localhost:81 (default: admin@example.com / changeme)

- **Frontend (production):** https://pdcms.vercel.app

**How to obtain API key:**

### 5.3 Manual Installation

1. Register account at https://resend.com

2. Go to Dashboard → API KeysFor manual installation without Docker:

3. Create new API Key

4. Verify your domain (add DNS records)**Step 1:** Setup PostgreSQL database

**Environment variables:**```bash

# Connect to PostgreSQL

````envpsql -U postgres

RESEND_API_KEY=re_xxxxxxxxxxxxxxxxxxxxxxxxxxxx

MAIL_FROM=noreply@yourdomain.com# Create database

MAIL_REPLY_TO=support@yourdomain.comCREATE DATABASE dental_clinic_db;

```\q

````

#### 4.2.2 Gemini AI Configuration

**Step 2:** Setup Redis

**Purpose:** AI chatbot for patient FAQ about dental services and pricing.

```````bash

**Service using:** Chatbot module# Run Redis with Docker

docker run -d --name redis -p 6379:6379 redis:7-alpine redis-server --requirepass redis123

**Required configuration:**```



- Google AI API Key**Step 3:** Configure application

- Model name (gemini-2.0-flash)

Edit `src/main/resources/application-dev.yaml` with your database credentials.

**How to obtain API key:**

**Step 4:** Build application

1. Access https://aistudio.google.com

2. Create new API Key```bash

3. Copy and save the keycd PDCMS_BE

./mvnw clean package -DskipTests

**Environment variables:**```



```env**Step 5:** Run application

GEMINI_API_KEY=AIzaSyxxxxxxxxxxxxxxxxxxxxxxxxx

``````bash

java -jar target/dental-clinic-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

#### 4.2.3 SePay Payment Configuration```



**Purpose:** Dynamic QR payment integration for patient invoices.### 5.4 Initial Data Seeding



**Service using:** Payment moduleAfter installation, seed data is automatically loaded:



**Required configuration:****Execution Order:**



- SePay API Key1. `db/enums.sql` runs BEFORE Hibernate - creates PostgreSQL ENUMs

- Webhook Secret2. Hibernate creates tables from Entity classes (ddl-auto: update)

- Bank Account Information3. `DataInitializer.java` loads INSERT statements from `dental-clinic-seed-data.sql`



**Environment variables:****Seed data includes:**



```env- Demo users (all accounts in section 4)

SEPAY_API_KEY=your-sepay-api-key- Sample employees (Dentists, Nurses, Receptionist, etc.)

SEPAY_WEBHOOK_SECRET=your-webhook-secret- Sample patients (10 demo patients)

SEPAY_BANK_CODE=your-bank-code- Sample services (Dental services with pricing)

SEPAY_ACCOUNT_NUMBER=your-account-number- Work shifts (Morning, Afternoon, Evening shifts)

```- Time-off types (Annual leave, Sick leave, etc.)

- Permissions and Role-Permission mappings

#### 4.2.4 Redis Cache Configuration- Chatbot knowledge base



**Purpose:** Caching layer for improved performance and session management.---



**Service using:** All modules (authentication tokens, API responses)## 6. Other Related Documents



**How to install Redis:**### 6.1 Project Document List



**Option 1: Docker (Recommended)**| No. | Document Name           | Description                                  |

| --- | ----------------------- | -------------------------------------------- |

```bash| 1   | API Documentation       | Detailed API endpoints documentation         |

docker run -d --name redis -p 6379:6379 redis:7-alpine redis-server --requirepass redis123| 2   | API Endpoints Reference | Function names and sample requests           |

```| 3   | Business Rules          | Comprehensive business rules and constraints |

| 4   | Deployment Guide        | Step-by-step deployment to DigitalOcean      |

**Option 2: Using docker-compose.yml (included in project)**| 5   | Email Configuration     | Email service configuration guide            |

| 6   | JWT Claims Reference    | JWT token structure for frontend             |

Redis is automatically started when running `docker compose up`.| 7   | Notification System     | Real-time notifications integration guide    |

| 8   | Payment Integration     | SePay payment flow documentation             |

**Environment variables:**| 9   | Warehouse Module        | Warehouse API reference                      |

| 10  | Scheduled Jobs          | Background jobs documentation                |

```env| 11  | Chatbot Integration     | Gemini AI chatbot guide                      |

REDIS_HOST=localhost| 12  | Dashboard Features      | Advanced dashboard features guide            |

REDIS_PORT=6379

REDIS_PASSWORD=redis123### 6.2 Source Code Repositories

```````

**Backend Application:**

---

- Repository: https://github.com/DenTeeth/PDCMS_BE

## 5. List of All Roles, Username/Password for Demo System- Main branch: `main`

- Development branch: `feat/BE-905-payment-implement`

### 5.1 System Roles- Technology: Spring Boot 3.2.10 + Java 17

The PDCMS system has 9 main roles organized into 3 base categories:**Frontend Application:**

| No. | Role Name | Base Role | Description |- Production URL: https://pdcms.vercel.app

| --- | ---------------------- | --------- | --------------------------------------------------- |- Technology: React/Next.js + TypeScript

| 1 | ROLE_ADMIN | admin | System Administrator - Full system access |

| 2 | ROLE_MANAGER | employee | Manager - Operations and HR management |**Production Server:**

| 3 | ROLE_DENTIST | employee | Dentist - Patient examination and treatment |

| 4 | ROLE_NURSE | employee | Nurse - Patient care and treatment support |- Server: DigitalOcean Droplet (Ubuntu 22.04)

| 5 | ROLE_RECEPTIONIST | employee | Receptionist - Reception and appointment management |- IP: 157.230.37.20

| 6 | ROLE_ACCOUNTANT | employee | Accountant - Finance and payment management |- Domain: pdcms.duckdns.org

| 7 | ROLE_INVENTORY_MANAGER | employee | Inventory Manager - Warehouse and supplies |- SSL: Let's Encrypt via Nginx Proxy Manager

| 8 | ROLE_DENTIST_INTERN | employee | Dental Intern - Training and supervised practice |

| 9 | ROLE_PATIENT | patient | Patient - View records and book appointments |---

### 5.2 Demo Account List*Document generated: January 14, 2026*

_Version: 1.0.0_

**IMPORTANT NOTES:**

- All default passwords: `123456`
- These accounts are for demo/testing environment only
- In production, all passwords must be changed
- JWT secret key must be randomly generated and secured

#### 5.2.1 Admin Account

| Role  | Username | Email                  | Password | Description                        |
| ----- | -------- | ---------------------- | -------- | ---------------------------------- |
| ADMIN | admin    | admin@dentalclinic.com | 123456   | System Administrator (full access) |

**Permissions:** ALL (automatically assigned all 75+ permissions)

**Screen after login:** `/admin/dashboard`

#### 5.2.2 Manager Account

| Username | Email                     | Password | Description    |
| -------- | ------------------------- | -------- | -------------- |
| quanli1  | quan.vnm@dentalclinic.com | 123456   | Clinic Manager |

**Key Permissions:**

- APPROVE_TIME_OFF, APPROVE_OVERTIME, APPROVE_REGISTRATION
- VIEW_SCHEDULE_ALL, MANAGE_SCHEDULE
- VIEW_DASHBOARD, VIEW_REPORTS

**Screen after login:** `/manager/dashboard`

#### 5.2.3 Dentist Accounts

| Username | Email                    | Password | Employment Type |
| -------- | ------------------------ | -------- | --------------- |
| bacsi1   | khoa.la@dentalclinic.com | 123456   | FULL_TIME       |
| bacsi2   | thai.tc@dentalclinic.com | 123456   | FULL_TIME       |
| bacsi3   | jimmy.d@dentalclinic.com | 123456   | PART_TIME_FLEX  |
| bacsi4   | junya.o@dentalclinic.com | 123456   | PART_TIME_FIXED |

**Key Permissions:**

- VIEW_PATIENT, MANAGE_PATIENT
- VIEW_APPOINTMENT_OWN, UPDATE_APPOINTMENT_STATUS
- WRITE_CLINICAL_RECORD, VIEW_ATTACHMENT
- MANAGE_TREATMENT_PLAN, VIEW_TREATMENT_PLAN_OWN

**Screen after login:** `/dentist/dashboard`

#### 5.2.4 Nurse Accounts

| Username | Email                        | Password | Employment Type |
| -------- | ---------------------------- | -------- | --------------- |
| yta1     | nguyen.dnkn@dentalclinic.com | 123456   | FULL_TIME       |
| yta2     | khang.nttk@dentalclinic.com  | 123456   | FULL_TIME       |
| yta3     | nhat.htqn@dentalclinic.com   | 123456   | PART_TIME_FIXED |
| yta4     | chinh.nd@dentalclinic.com    | 123456   | PART_TIME_FLEX  |

**Key Permissions:**

- VIEW_PATIENT (read-only)
- VIEW_APPOINTMENT_OWN
- VIEW_CLINICAL_RECORD

**Screen after login:** `/nurse/dashboard`

#### 5.2.5 Receptionist Account

| Username | Email                      | Password | Description         |
| -------- | -------------------------- | -------- | ------------------- |
| letan1   | thuan.dkb@dentalclinic.com | 123456   | Clinic Receptionist |

**Key Permissions:**

- VIEW_APPOINTMENT_ALL, CREATE_APPOINTMENT, MANAGE_APPOINTMENT
- VIEW_PATIENT, MANAGE_PATIENT
- UPDATE_APPOINTMENT_STATUS

**Screen after login:** `/receptionist/dashboard`

#### 5.2.6 Accountant Account

| Username | Email                     | Password | Description       |
| -------- | ------------------------- | -------- | ----------------- |
| ketoan1  | thanh.cq@dentalclinic.com | 123456   | Clinic Accountant |

**Key Permissions:**

- VIEW_INVOICE, CREATE_INVOICE, MANAGE_PAYMENT
- VIEW_DASHBOARD, VIEW_REPORTS
- EXPORT_INVOICE

**Screen after login:** `/accountant/dashboard`

#### 5.2.7 Patient Accounts

| Username  | Email              | Password | Patient Code |
| --------- | ------------------ | -------- | ------------ |
| benhnhan1 | phong.dt@email.com | 123456   | BN-1001      |
| benhnhan2 | phong.pv@email.com | 123456   | BN-1002      |
| benhnhan3 | anh.nt@email.com   | 123456   | BN-1003      |
| benhnhan4 | mit.bit@email.com  | 123456   | BN-1004      |

**Key Permissions:**

- VIEW_OWN_PROFILE, VIEW_OWN_APPOINTMENTS
- CREATE_APPOINTMENT
- VIEW_OWN_INVOICE, VIEW_OWN_TREATMENT_PLAN

**Screen after login:** `/patient/dashboard`

### 5.3 Account Testing Guide

#### 5.3.1 Test Admin Account (Full RBAC Access)

1. Access: `http://localhost:3000/login`
2. Login with: `admin` / `123456`
3. Verify redirect to: `/admin/dashboard`
4. Test RBAC features:
   - Navigate to **Settings → Roles** to manage roles
   - Navigate to **Settings → Permissions** to view all permissions
   - Assign/revoke permissions from roles
   - Create new custom roles

#### 5.3.2 Test Dentist Account (Limited Permissions)

1. Access: `http://localhost:3000/login`
2. Login with: `bacsi1` / `123456`
3. Verify redirect to: `/dentist/dashboard`
4. Test permission restrictions:
   - ✅ Can view patient records
   - ✅ Can write clinical records
   - ❌ Cannot access employee management
   - ❌ Cannot access financial reports

#### 5.3.3 Test Patient Account (Minimal Permissions)

1. Access: `http://localhost:3000/login`
2. Login with: `benhnhan1` / `123456`
3. Verify redirect to: `/patient/dashboard`
4. Test permission restrictions:
   - ✅ Can view own profile only
   - ✅ Can book appointments
   - ❌ Cannot view other patients
   - ❌ Cannot access admin features

---

## 6. Complete System Installation Guide

### 6.1 Prerequisites

Before installation, ensure your computer has:

- **Java Development Kit (JDK) 17** or higher
- **Docker & Docker Compose** (recommended)
- **PostgreSQL 13+** (if not using Docker)
- **Redis 7+** (if not using Docker)
- **Maven 3.9+** (or use included Maven Wrapper)
- **Git** for version control
- **Node.js 18+** (for frontend development)

### 6.2 Quick Installation with Docker Compose (Recommended)

**Step 1:** Clone repository

```bash
git clone https://github.com/DenTeeth/PDCMS_BE.git
cd PDCMS_BE
```

**Step 2:** Configure environment variables

Create file `.env` in root directory:

```env
# Database
DB_USERNAME=root
DB_PASSWORD=123456
DB_DATABASE=dental_clinic_db
DB_PORT=5432

# Redis
REDIS_PASSWORD=redis123

# Email (Resend)
RESEND_API_KEY=re_xxxxxxxxxxxx
MAIL_FROM=noreply@yourdomain.com
MAIL_REPLY_TO=support@yourdomain.com

# Frontend URL
FRONTEND_URL=http://localhost:3000

# Chatbot (Gemini AI)
GEMINI_API_KEY=AIzaSyxxxxxxxxxx

# JWT
JWT_SECRET=change-this-to-random-256-bit-secret
```

**Step 3:** Start all services

```bash
docker compose up --build
```

**Step 4:** Verify services

Check API health:

```
http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

**Step 5:** Access applications

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Base URL:** http://localhost:8080/api/v1
- **Nginx Admin Panel:** http://localhost:81 (default: admin@example.com / changeme)
- **Frontend (production):** https://pdcms.vercel.app

### 6.3 Manual Installation

For manual installation without Docker:

**Step 1:** Setup PostgreSQL database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE dental_clinic_db;
\q
```

**Step 2:** Setup Redis

```bash
# Run Redis with Docker
docker run -d --name redis -p 6379:6379 redis:7-alpine redis-server --requirepass redis123
```

**Step 3:** Configure application

Edit `src/main/resources/application-dev.yaml` with your database credentials.

**Step 4:** Build application

```bash
cd PDCMS_BE
./mvnw clean package -DskipTests
```

**Step 5:** Run application

```bash
java -jar target/dental-clinic-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 6.4 Initial Data Seeding

After installation, seed data is automatically loaded:

**Execution Order:**

1. `db/enums.sql` runs BEFORE Hibernate - creates PostgreSQL ENUMs
2. Hibernate creates tables from Entity classes (ddl-auto: update)
3. `DataInitializer.java` loads INSERT statements from `dental-clinic-seed-data.sql`

**Seed data includes:**

- Demo users (all accounts in section 5)
- Sample employees (Dentists, Nurses, Receptionist, etc.)
- Sample patients (10 demo patients)
- Sample services (Dental services with pricing)
- Work shifts (Morning, Afternoon, Evening shifts)
- Time-off types (Annual leave, Sick leave, etc.)
- **Permissions (75+ permissions across 20 modules)**
- **Role-Permission mappings (RBAC configuration)**
- Chatbot knowledge base

---

## 7. Other Related Documents

### 7.1 Project Document List

| No. | Document Name           | Description                                  |
| --- | ----------------------- | -------------------------------------------- |
| 1   | API Documentation       | Detailed API endpoints documentation         |
| 2   | API Endpoints Reference | Function names and sample requests           |
| 3   | Business Rules          | Comprehensive business rules and constraints |
| 4   | Deployment Guide        | Step-by-step deployment to DigitalOcean      |
| 5   | Email Configuration     | Email service configuration guide            |
| 6   | JWT Claims Reference    | JWT token structure for frontend             |
| 7   | Notification System     | Real-time notifications integration guide    |
| 8   | Payment Integration     | SePay payment flow documentation             |
| 9   | Warehouse Module        | Warehouse API reference                      |
| 10  | Scheduled Jobs          | Background jobs documentation                |
| 11  | Chatbot Integration     | Gemini AI chatbot guide                      |
| 12  | Dashboard Features      | Advanced dashboard features guide            |
| 13  | **RBAC Permissions**    | **Permission hierarchy and assignment**      |

### 7.2 Source Code Repositories

**Backend Application:**

- Repository: https://github.com/DenTeeth/PDCMS_BE
- Main branch: `main`
- Development branch: `feat/BE-905-payment-implement`
- Technology: Spring Boot 3.2.10 + Java 17

**Frontend Application:**

- Production URL: https://pdcms.vercel.app
- Technology: React/Next.js + TypeScript

**Production Server:**

- Server: DigitalOcean Droplet (Ubuntu 22.04)
- IP: 157.230.37.20
- Domain: pdcms.duckdns.org
- SSL: Let's Encrypt via Nginx Proxy Manager

---

_Document generated: January 15, 2026_
_Version: 1.1.0_
_Updated: Added comprehensive RBAC documentation (Section 3)_
