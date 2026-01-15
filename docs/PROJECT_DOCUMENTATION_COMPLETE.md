# PDCMS - Private Dental Clinic Management System

**Project Code:** FA25SE202

**Student Group:** GFA25SE96

## Team Members:

- Vo Nguyen Minh Quan - SE160914
- Doan Nguyen Khoi Nguyen - SE182907
- Le Anh Khoa - SE184100
- Trinh Cong Thai - SE183743

**Supervisor:** Lam Huu Khanh Phuong

**External Supervisor:** Nguyen Van Chien, Nguyen Xuan Binh

**Semester:** FA25

**January 15, 2026**

---

## Contents

1. [Software Modules Created in the Project](#1-software-modules-created-in-the-project)
   - 1.1 [Backend Application (Monolithic Architecture)](#11-backend-application-monolithic-architecture)
   - 1.2 [Alternative: Starting Backend with Docker Compose](#12-alternative-starting-backend-with-docker-compose)
   - 1.3 [Frontend Application](#13-frontend-application)
2. [Third-Party Libraries, Frameworks, and Tools](#2-third-party-libraries-frameworks-and-tools)
   - 2.1 [Backend Technologies](#21-backend-technologies)
   - 2.2 [Frontend Technologies](#22-frontend-technologies)
   - 2.3 [Database & Infrastructure](#23-database--infrastructure)
   - 2.4 [Email & AI Integration](#24-email--ai-integration)
3. [Configuration Documentation](#3-configuration-documentation)
   - 3.1 [Internal Software Component Configuration](#31-internal-software-component-configuration)
   - 3.2 [Third-Party Service Configuration](#32-third-party-service-configuration)
4. [List of All Roles, Username/Password for Demo System](#4-list-of-all-roles-usernamepassword-for-demo-system)
   - 4.1 [System Roles](#41-system-roles)
   - 4.2 [Demo Account List](#42-demo-account-list)
   - 4.3 [Account Testing Guide](#43-account-testing-guide)
5. [Complete System Installation Guide](#5-complete-system-installation-guide)
   - 5.1 [Prerequisites](#51-prerequisites)
   - 5.2 [Quick Installation with Docker Compose (Recommended)](#52-quick-installation-with-docker-compose-recommended)
   - 5.3 [Manual Installation](#53-manual-installation)
   - 5.4 [Initial Data Seeding](#54-initial-data-seeding)
6. [Other Related Documents](#6-other-related-documents)
   - 6.1 [Project Document List](#61-project-document-list)
   - 6.2 [Source Code Repositories](#62-source-code-repositories)

---

## 1. Software Modules Created in the Project

The PDCMS system is built on a monolithic architecture using Spring Boot, with multiple functional modules organized following domain-driven design. All modules can be installed following the instructions in this document.

### 1.1 Backend Application (Monolithic Architecture)

**Function:** REST API backend for dental clinic management system, including patient management, appointments, clinical records, treatment plans, warehouse inventory, employee scheduling, payments, and AI chatbot.

**Technologies:**

- Spring Boot 3.2.10
- Spring Data JPA + Hibernate
- Spring Security 6 + JWT OAuth2
- Spring WebSocket (Real-time notifications)
- PostgreSQL 13+
- Redis 7 (Caching)
- Java 17

**Port:** 8080

**Database:** dental_clinic_db

**Source code directory:** `src/main/java/com/dental/clinic/management/`

**Core Modules:**

| Module              | Directory              | Description                                           |
| ------------------- | ---------------------- | ----------------------------------------------------- |
| Authentication      | `authentication/`      | Login, JWT tokens, email verification, password reset |
| Account             | `account/`             | User account management and profiles                  |
| Employee            | `employee/`            | Clinic staff management (Dentists, Nurses, etc.)      |
| Patient             | `patient/`             | Patient records and medical history                   |
| Booking Appointment | `booking_appointment/` | Appointment scheduling and room management            |
| Treatment Plans     | `treatment_plans/`     | Multi-phase treatment planning                        |
| Clinical Records    | `clinical_records/`    | Clinical notes, odontogram, attachments               |
| Service             | `service/`             | Dental services and procedures catalog                |
| Warehouse           | `warehouse/`           | Inventory, materials, and stock management            |
| Working Schedule    | `working_schedule/`    | Employee shifts, time-off, overtime requests          |
| Payment             | `payment/`             | Invoices, payments, SePay QR integration              |
| Notification        | `notification/`        | Real-time WebSocket notifications                     |
| Chatbot             | `chatbot/`             | FAQ chatbot with Gemini AI                            |
| Dashboard           | `dashboard/`           | Reports and analytics                                 |
| Feedback            | `feedback/`            | Patient feedback and reviews                          |
| Permission          | `permission/`          | RBAC permission management                            |
| Role                | `role/`                | Role and base role management                         |

**Build command:**

```bash
cd PDCMS_BE
./mvnw clean package -DskipTests
```

**Run command:**

```bash
java -jar target/dental-clinic-management-0.0.1-SNAPSHOT.jar
```

---

### 1.2 Alternative: Starting Backend with Docker Compose

Instead of building and running manually as above, you can use Docker Compose to start all services at once.

**Requirements:**

- Docker Engine 20.10+
- Docker Compose 2.0+

**Advantages of this method:**

- Automatically build and start backend with PostgreSQL and Redis
- Manage dependencies and correct startup order
- Automatically create Docker network for services to connect
- Easy to scale and deploy to production

**Implementation steps:**

**Step 1:** Navigate to project directory

```bash
cd PDCMS_BE
```

**Step 2:** Ensure file `.env` is configured (see section 3.1.4)

**Step 3:** Run Docker Compose

```bash
docker compose up --build
```

This command will:

- Build Docker image for backend application
- Start services in order: PostgreSQL → Redis → Backend App → Nginx Proxy Manager
- Display logs of all services

**Run in background (detached mode):**

```bash
docker compose up --build -d
```

**View logs:**

```bash
docker compose logs -f
```

**Stop all services:**

```bash
docker compose down
```

---

### 1.3 Frontend Application

**Function:** Web application for Admin, Manager, Dentist, Receptionist, Accountant, Nurse, and Patient roles.

**Technologies:**

- Next.js 16 with Turbopack
- React 19
- TypeScript 5
- Tailwind CSS 4
- TanStack Query (React Query) 5
- React Hook Form + Zod validation
- Radix UI (Headless components)
- Lucide React + FontAwesome (icons)
- FullCalendar (Appointment scheduling)
- Recharts (Data visualization)
- WebSocket with STOMP.js + SockJS
- next-intl (Internationalization)
- Framer Motion (Animations)

**Development Port:** 3000

**Production URL:** https://pdcms.vercel.app

**Source code repository:** Separate frontend repository (private)

**Install dependencies:**

```bash
cd PDCMS_FE
npm install
```

**Run development server:**

```bash
npm run dev
```

**Build for production:**

```bash
npm run build
```

---

## 2. Third-Party Libraries, Frameworks, and Tools

### 2.1 Backend Technologies

| Name                         | Version | Purpose                              |
| ---------------------------- | ------- | ------------------------------------ |
| Java JDK                     | 17      | Backend programming language         |
| Spring Boot                  | 3.2.10  | Application framework                |
| Spring Data JPA              | 3.2.x   | ORM and database access              |
| Spring Security              | 6.x     | Authentication and authorization     |
| Spring WebSocket             | 3.2.x   | Real-time notifications              |
| PostgreSQL JDBC Driver       | 42.x    | Database connectivity                |
| Maven                        | 3.9+    | Build tool and dependency management |
| Lombok                       | 1.18.x  | Reduce boilerplate code              |
| JWT (OAuth2 Resource Server) | 6.x     | JSON Web Token authentication        |
| SpringDoc OpenAPI            | 2.5.0   | Swagger API documentation            |
| Apache POI                   | 5.4.0   | Excel export functionality           |
| Apache Commons CSV           | 1.10.0  | CSV export functionality             |
| MapStruct                    | 1.5.x   | DTO mapping                          |

### 2.2 Frontend Technologies

#### Core Framework & Runtime

| Name       | Version | Purpose                      |
| ---------- | ------- | ---------------------------- |
| Node.js    | 20+     | JavaScript runtime           |
| Next.js    | 16.0+   | React framework with SSR/SSG |
| React      | 19.2+   | UI framework                 |
| TypeScript | 5+      | Type-safe JavaScript         |

#### UI Component Libraries

| Name          | Version | Purpose                    |
| ------------- | ------- | -------------------------- |
| Radix UI      | Latest  | Headless component library |
| Lucide React  | 0.544+  | Icon library               |
| FontAwesome   | 7.0+    | Icon library               |
| Framer Motion | 12.23+  | Animation library          |

#### Styling

| Name                     | Version | Purpose                       |
| ------------------------ | ------- | ----------------------------- |
| Tailwind CSS             | 4+      | Utility-first CSS framework   |
| Class Variance Authority | 0.7+    | Component variant management  |
| clsx                     | 2.1+    | Classname utility             |
| tailwind-merge           | 3.3+    | Merge Tailwind classes        |
| next-themes              | 0.4+    | Theme management (dark/light) |

#### Form & Validation

| Name                | Version | Purpose                  |
| ------------------- | ------- | ------------------------ |
| React Hook Form     | 7.65+   | Form management          |
| Zod                 | 4.1+    | Schema validation        |
| @hookform/resolvers | 5.2+    | Form validation resolver |

#### Data Visualization & Calendar

| Name         | Version | Purpose            |
| ------------ | ------- | ------------------ |
| Recharts     | 3.3+    | Chart library      |
| FullCalendar | 6.1+    | Calendar component |

#### Date & Time

| Name             | Version | Purpose               |
| ---------------- | ------- | --------------------- |
| date-fns         | 4.1+    | Date utility library  |
| React Day Picker | 9.11+   | Date picker component |

#### State Management & HTTP

| Name            | Version | Purpose                 |
| --------------- | ------- | ----------------------- |
| TanStack Query  | 5.90+   | Server state management |
| Axios           | 1.13+   | HTTP client             |
| GraphQL         | 16.12+  | Query language          |
| graphql-request | 7.4+    | GraphQL client          |

#### Real-time Communication

| Name           | Version | Purpose                |
| -------------- | ------- | ---------------------- |
| @stomp/stompjs | 7.2+    | WebSocket STOMP client |
| sockjs-client  | 1.6+    | WebSocket fallback     |

#### File & Media Handling

| Name                  | Version | Purpose                        |
| --------------------- | ------- | ------------------------------ |
| ExcelJS               | 4.4+    | Excel file generation          |
| Cloudinary            | 2.8+    | Image/video management         |
| next-cloudinary       | 6.17+   | Cloudinary Next.js integration |
| React Lazy Load Image | 1.6+    | Image lazy loading             |

#### Medical Imaging

| Name           | Version | Purpose                    |
| -------------- | ------- | -------------------------- |
| @niivue/niivue | 0.65+   | NIfTI medical image viewer |

#### Authentication & Cookies

| Name       | Version | Purpose           |
| ---------- | ------- | ----------------- |
| jwt-decode | 4.0+    | JWT token decoder |
| js-cookie  | 3.0+    | Cookie management |

#### Internationalization & Notifications

| Name      | Version | Purpose             |
| --------- | ------- | ------------------- |
| next-intl | 4.4+    | i18n for Next.js    |
| Sonner    | 2.0+    | Toast notifications |

### 2.3 Database & Infrastructure

| Name                 | Version      | Purpose                          |
| -------------------- | ------------ | -------------------------------- |
| PostgreSQL           | 13+          | Primary relational database      |
| Redis                | 7-alpine     | Cache and session management     |
| Docker               | 20.10+       | Containerization                 |
| Docker Compose       | 2.0+         | Multi-container orchestration    |
| Nginx Proxy Manager  | Latest       | Reverse proxy and SSL management |
| DigitalOcean Droplet | Ubuntu 22.04 | Production server hosting        |

### 2.4 Email & AI Integration

| Name               | Version   | Purpose                           |
| ------------------ | --------- | --------------------------------- |
| Resend Java SDK    | 3.0.0     | Production email service          |
| Spring Mail        | 3.2.x     | Email sending abstraction         |
| LangChain4J Gemini | 0.35.0    | Gemini AI integration for chatbot |
| Google AI Gemini   | 2.0-flash | AI model for FAQ responses        |

---

## 3. Configuration Documentation

### 3.1 Internal Software Component Configuration

#### 3.1.1 Database Configuration

**PostgreSQL Connection:**

Create database for the system:

```sql
CREATE DATABASE dental_clinic_db;
```

**Connection String Format:**

```
jdbc:postgresql://localhost:5432/dental_clinic_db
Username: root (or postgres)
Password: your_password
```

#### 3.1.2 API Ports Configuration

**Port list for services:**

| Service Name                | Port | URL                   |
| --------------------------- | ---- | --------------------- |
| Backend Application         | 8080 | http://localhost:8080 |
| PostgreSQL Database         | 5432 | localhost:5432        |
| Redis Cache                 | 6379 | localhost:6379        |
| Nginx Proxy Manager (HTTP)  | 80   | http://localhost:80   |
| Nginx Proxy Manager (HTTPS) | 443  | https://localhost:443 |
| Nginx Admin Panel           | 81   | http://localhost:81   |
| Frontend Application (Dev)  | 3000 | http://localhost:3000 |

**Note:** All API requests should go through the backend application port (8080). In production, Nginx Proxy Manager handles SSL termination.

#### 3.1.3 JWT Token Configuration

**File:** `src/main/resources/application.yaml`

```yaml
dentalclinic:
  jwt:
    base64-secret: your-256-bit-secret-key-change-in-production
    # 15 minutes = 900 seconds (access token)
    access-token-validity-in-seconds: 9000
    # 30 days = 2592000 seconds (refresh token)
    refresh-token-validity-in-seconds: 2592000
```

**Token Generation:** Performed by Authentication module after successful login.

**Token Validation:** Performed by Spring Security filter for all protected routes.

#### 3.1.4 Environment Variables - Backend Application

**File:** `.env` (root directory)

```env
# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Database Configuration
DB_USERNAME=root
DB_PASSWORD=123456
DB_DATABASE=dental_clinic_db
DB_PORT=5432

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=redis123

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-change-in-production
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=2592000000

# Email Configuration (Resend)
RESEND_API_KEY=re_xxxxxxxxxxxxxxxxxxxxxxxxxxxx
MAIL_FROM=noreply@yourdomain.com
MAIL_REPLY_TO=support@yourdomain.com

# Frontend URL (for email links)
FRONTEND_URL=https://pdcms.vercel.app

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://pdcms.vercel.app

# Chatbot Configuration (Gemini AI)
GEMINI_API_KEY=AIzaSyxxxxxxxxxxxxxxxxxxxxxxxxx

# Application Port
APP_PORT=8080
```

#### 3.1.5 Environment Variables - Frontend Application

**File:** `.env.local` (frontend directory)

```env
# API Configuration
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws

# Cloudinary Configuration (Image uploads)
NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME=your-cloud-name
NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET=your-upload-preset

# For production deployment:
# NEXT_PUBLIC_API_BASE_URL=https://pdcms.duckdns.org
# NEXT_PUBLIC_WS_URL=wss://pdcms.duckdns.org/ws
```

---

### 3.2 Third-Party Service Configuration

#### 3.2.1 Resend Email Configuration

**Purpose:** Send verification emails, password reset, appointment reminders to patients.

**Services using:** Authentication, Patient Management, Notification

**Required configuration:**

- Resend API Key
- Verified domain email address
- Reply-to email address

**How to obtain API key:**

1. Register account at https://resend.com
2. Go to Dashboard → API Keys
3. Create new API Key
4. Verify your domain (add DNS records)

**Environment variables:**

```env
RESEND_API_KEY=re_xxxxxxxxxxxxxxxxxxxxxxxxxxxx
MAIL_FROM=noreply@yourdomain.com
MAIL_REPLY_TO=support@yourdomain.com
```

#### 3.2.2 Gemini AI Configuration

**Purpose:** AI chatbot for patient FAQ about dental services and pricing.

**Service using:** Chatbot module

**Required configuration:**

- Google AI API Key
- Model name (gemini-2.0-flash)

**How to obtain API key:**

1. Access https://aistudio.google.com
2. Create new API Key
3. Copy and save the key

**Environment variables:**

```env
GEMINI_API_KEY=AIzaSyxxxxxxxxxxxxxxxxxxxxxxxxx
```

#### 3.2.3 SePay Payment Configuration

**Purpose:** Dynamic QR payment integration for patient invoices.

**Service using:** Payment module

**Required configuration:**

- SePay API Key
- Webhook Secret
- Bank Account Information

**Environment variables:**

```env
SEPAY_API_KEY=your-sepay-api-key
SEPAY_WEBHOOK_SECRET=your-webhook-secret
SEPAY_BANK_CODE=your-bank-code
SEPAY_ACCOUNT_NUMBER=your-account-number
```

#### 3.2.4 Redis Cache Configuration

**Purpose:** Caching layer for improved performance and session management.

**Service using:** All modules (authentication tokens, API responses)

**How to install Redis:**

**Option 1: Docker (Recommended)**

```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine redis-server --requirepass redis123
```

**Option 2: Using docker-compose.yml (included in project)**

Redis is automatically started when running `docker compose up`.

**Environment variables:**

```env
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis123
```

---

## 4. List of All Roles, Username/Password for Demo System

### 4.1 System Roles

The PDCMS system has 9 main roles organized into 3 base categories:

| No. | Role Name              | Base Role | Description                                         |
| --- | ---------------------- | --------- | --------------------------------------------------- |
| 1   | ROLE_ADMIN             | admin     | System Administrator - Full system access           |
| 2   | ROLE_MANAGER           | employee  | Manager - Operations and HR management              |
| 3   | ROLE_DENTIST           | employee  | Dentist - Patient examination and treatment         |
| 4   | ROLE_NURSE             | employee  | Nurse - Patient care and treatment support          |
| 5   | ROLE_RECEPTIONIST      | employee  | Receptionist - Reception and appointment management |
| 6   | ROLE_ACCOUNTANT        | employee  | Accountant - Finance and payment management         |
| 7   | ROLE_INVENTORY_MANAGER | employee  | Inventory Manager - Warehouse and supplies          |
| 8   | ROLE_DENTIST_INTERN    | employee  | Dental Intern - Training and supervised practice    |
| 9   | ROLE_PATIENT           | patient   | Patient - View records and book appointments        |

### 4.2 Demo Account List

**IMPORTANT NOTES:**

- All default passwords: `123456`
- These accounts are for demo/testing environment only
- In production, all passwords must be changed
- JWT secret key must be randomly generated and secured

#### 4.2.1 Admin Account

| Role  | Username | Email                  | Password | Description                        |
| ----- | -------- | ---------------------- | -------- | ---------------------------------- |
| ADMIN | admin    | admin@dentalclinic.com | 123456   | System Administrator (full access) |

**Permissions:**

- Manage all users and roles
- Create/edit/delete employees and patients
- View all reports and analytics
- Configure entire system

**Screen after login:** `/admin/dashboard`

#### 4.2.2 Manager Account

| Username | Email                     | Password | Description    |
| -------- | ------------------------- | -------- | -------------- |
| quanli1  | quan.vnm@dentalclinic.com | 123456   | Clinic Manager |

**Permissions:**

- Manage employee work schedules
- Approve time-off and overtime requests
- View revenue reports
- Manage shift registrations

**Screen after login:** `/manager/dashboard`

#### 4.2.3 Dentist Accounts

| Username | Email                    | Password | Employment Type |
| -------- | ------------------------ | -------- | --------------- |
| bacsi1   | khoa.la@dentalclinic.com | 123456   | FULL_TIME       |
| bacsi2   | thai.tc@dentalclinic.com | 123456   | FULL_TIME       |
| bacsi3   | jimmy.d@dentalclinic.com | 123456   | PART_TIME_FLEX  |
| bacsi4   | junya.o@dentalclinic.com | 123456   | PART_TIME_FIXED |

**Permissions:**

- View and update patient records
- Create and manage treatment plans
- Write clinical records
- View own appointment schedule
- Create prescriptions

**Screen after login:** `/dentist/dashboard`

#### 4.2.4 Nurse Accounts

| Username | Email                        | Password | Employment Type |
| -------- | ---------------------------- | -------- | --------------- |
| yta1     | nguyen.dnkn@dentalclinic.com | 123456   | FULL_TIME       |
| yta2     | khang.nttk@dentalclinic.com  | 123456   | FULL_TIME       |
| yta3     | nhat.htqn@dentalclinic.com   | 123456   | PART_TIME_FIXED |
| yta4     | chinh.nd@dentalclinic.com    | 123456   | PART_TIME_FLEX  |

**Permissions:**

- View patient records (read-only)
- Support patient check-in
- View own work schedule

**Screen after login:** `/nurse/dashboard`

#### 4.2.5 Receptionist Account

| Username | Email                      | Password | Description         |
| -------- | -------------------------- | -------- | ------------------- |
| letan1   | thuan.dkb@dentalclinic.com | 123456   | Clinic Receptionist |

**Permissions:**

- Manage appointments (create/edit/cancel)
- Register new patients
- Check-in patients
- View all appointments

**Screen after login:** `/receptionist/dashboard`

#### 4.2.6 Accountant Account

| Username | Email                     | Password | Description       |
| -------- | ------------------------- | -------- | ----------------- |
| ketoan1  | thanh.cq@dentalclinic.com | 123456   | Clinic Accountant |

**Permissions:**

- Create and manage invoices
- Process payments
- View financial reports
- Export financial data

**Screen after login:** `/accountant/dashboard`

#### 4.2.7 Patient Accounts

| Username  | Email              | Password | Patient Code |
| --------- | ------------------ | -------- | ------------ |
| benhnhan1 | phong.dt@email.com | 123456   | BN-1001      |
| benhnhan2 | phong.pv@email.com | 123456   | BN-1002      |
| benhnhan3 | anh.nt@email.com   | 123456   | BN-1003      |
| benhnhan4 | mit.bit@email.com  | 123456   | BN-1004      |

**Permissions:**

- View personal profile
- Book appointments
- View treatment history
- View own invoices
- Chat with AI chatbot

**Screen after login:** `/patient/dashboard`

### 4.3 Account Testing Guide

#### 4.3.1 Test Admin Account

1. Access: `http://localhost:3000/login`
2. Login with: `admin` / `123456`
3. Verify redirect to: `/admin/dashboard`
4. Test features:
   - Create/edit/delete users
   - Manage roles and permissions
   - View all branches
   - View system reports

#### 4.3.2 Test Dentist Account

1. Access: `http://localhost:3000/login`
2. Login with: `bacsi1` / `123456`
3. Verify redirect to: `/dentist/dashboard`
4. Test features:
   - View today's appointments
   - Open patient records
   - Create treatment plan
   - Write clinical record

#### 4.3.3 Test Patient Account

1. Access: `http://localhost:3000/login`
2. Login with: `benhnhan1` / `123456`
3. Verify redirect to: `/patient/dashboard`
4. Test features:
   - View personal profile
   - Book new appointment
   - View treatment history
   - Chat with AI chatbot

---

## 5. Complete System Installation Guide

### 5.1 Prerequisites

Before installation, ensure your computer has:

- **Java Development Kit (JDK) 17** or higher
- **Docker & Docker Compose** (recommended)
- **PostgreSQL 13+** (if not using Docker)
- **Redis 7+** (if not using Docker)
- **Maven 3.9+** (or use included Maven Wrapper)
- **Git** for version control
- **Node.js 18+** (for frontend development)

### 5.2 Quick Installation with Docker Compose (Recommended)

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

### 5.3 Manual Installation

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

### 5.4 Initial Data Seeding

After installation, seed data is automatically loaded:

**Execution Order:**

1. `db/enums.sql` runs BEFORE Hibernate - creates PostgreSQL ENUMs
2. Hibernate creates tables from Entity classes (ddl-auto: update)
3. `DataInitializer.java` loads INSERT statements from `dental-clinic-seed-data.sql`

**Seed data includes:**

- Demo users (all accounts in section 4)
- Sample employees (Dentists, Nurses, Receptionist, etc.)
- Sample patients (10 demo patients)
- Sample services (Dental services with pricing)
- Work shifts (Morning, Afternoon, Evening shifts)
- Time-off types (Annual leave, Sick leave, etc.)
- Permissions and Role-Permission mappings
- Chatbot knowledge base

---

## 6. Other Related Documents

### 6.1 Project Document List

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

### 6.2 Source Code Repositories

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

_Version: 1.0.0_
