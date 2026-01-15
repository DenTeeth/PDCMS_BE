# PDCMS Documentation

This folder contains all documentation for the Private Dental Clinic Management System.

## 📁 Folder Structure

| Folder                   | Description                                        |
| ------------------------ | -------------------------------------------------- |
| `api-guides/`            | Detailed API documentation for each module         |
| `architecture/`          | System architecture and design decisions           |
| `business-rules/`        | Business rule implementations (BR-37, BR-41, etc.) |
| `deployment/`            | Production deployment guides                       |
| `fe-integration/`        | Frontend integration guides                        |
| `features/`              | Feature-specific documentation                     |
| `setup/`                 | Configuration and setup guides                     |
| `warehouse-integration/` | Warehouse module integration guides                |

## 📄 Main Documents

| File                                               | Description                                 |
| -------------------------------------------------- | ------------------------------------------- |
| `PROJECT_DOCUMENTATION.md`                         | **Main project documentation** - Start here |
| `API_DOCUMENTATION.md`                             | Complete API reference                      |
| `API_ENDPOINTS_WITH_FUNCTION_NAMES_AND_SAMPLES.md` | API endpoints with code samples             |

## 🚀 Quick Links

### For Frontend Developers

- [FE Integration Guides](fe-integration/)
- [JWT Claims Reference](fe-integration/JWT_CLAIMS_REFERENCE_FOR_FE.md)
- [Payment Integration](fe-integration/FE_SEPAY_PAYMENT_INTEGRATION_GUIDE.md)

### For Backend Developers

- [Architecture Docs](architecture/)
- [Business Rules](business-rules/)
- [API Guides](api-guides/)

### For DevOps

- [Deployment Guide](deployment/DEPLOY_TO_DIGITALOCEAN_STEP_BY_STEP.md)
- [Email Setup](setup/EMAIL_CONFIGURATION_GUIDE.md)
- [SePay Webhook Setup](deployment/SEPAY_WEBHOOK_PRODUCTION_SETUP.md)

## 📋 Document Categories

### 1. API Guides (`api-guides/`)

Detailed API documentation organized by module:

- `booking/` - Appointment and room APIs
- `clinical-records/` - Medical records APIs
- `notification/` - Notification APIs
- `patient/` - Patient management APIs
- `permission/` - Permission APIs
- `role/` - Role management APIs
- `service/` - Service APIs
- `shift-management/` - Employee shift APIs
- `treatment-plan/` - Treatment plan APIs
- `warehouse/` - Inventory APIs

### 2. Architecture (`architecture/`)

System design and architecture decisions:

- Clinical records attachment flow
- Cron job architecture (P8)
- Module analysis and final decisions

### 3. Business Rules (`business-rules/`)

Implementation details for business rules:

- BR-37: Weekly working hours limit (48h/week)
- BR-41: Self-approval prevention
- BR-043/044: Duplicate detection and blacklist

### 4. Deployment (`deployment/`)

Production deployment and operations:

- DigitalOcean deployment guide
- SePay webhook production setup

### 5. FE Integration (`fe-integration/`)

Guides for frontend team integration:

- Dashboard features
- Payment flow
- Notification system
- Patient data handling

### 6. Features (`features/`)

Feature-specific documentation:

- AI Chatbot usage
- Scheduled jobs
- Warehouse module
- Material consumption

### 7. Setup (`setup/`)

Initial setup and configuration:

- Email configuration (SendGrid)
- Seed data updates
- Troubleshooting guides

---

_Last Updated: January 15, 2026_
