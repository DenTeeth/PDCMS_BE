# Dental Clinic Management System - Backend API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.10-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13+-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)

REST API backend for dental clinic management system. Built with Spring Boot 3.2, PostgreSQL, and Redis.

## 🚀 Quick Start

### Prerequisites

- JDK 17+
- Docker & Docker Compose
- Git

### Run Application

```bash
# Clone repository
git clone <your-repo-url>
cd PDCMS_BE

# Copy environment file
cp .env.example .env
# Edit .env with your configuration

# Start database services
docker-compose up postgres redis -d

# Run application (auto-detects environment)
./mvnw spring-boot:run
```

The application will:

- ✅ Auto-detect if running locally or in Docker
- ✅ Select correct profile (`dev` for local, `prod` for Docker)
- ✅ Create database ENUMs automatically
- ✅ Initialize seed data on first run

Access API: http://localhost:8080

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [System Requirements](#system-requirements)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Database](#database)
- [Security](#security)
- [Testing](#testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Features

### Core Modules

- **Authentication & Authorization**

  - JWT-based authentication with refresh tokens
  - Role-based access control (RBAC) with 9 roles
  - Email verification and password reset
  - Token blacklist management

- **Appointment Management**

  - Schedule, reschedule, and cancel appointments
  - Multi-doctor and multi-assistant support
  - Real-time availability checking
  - Appointment status tracking (Scheduled → In Progress → Completed)
  - Integration with treatment plans

- **Treatment Plan Management**

  - Create multi-phase treatment plans from templates
  - Assign doctors to specific treatment items
  - Automatic status synchronization (Pending → In Progress → Completed)
  - Price management with discount support
  - Phase reordering and item management

- **Clinical Records**

  - Comprehensive patient dental records (Odontogram)
  - Procedure tracking with tooth-specific details
  - Prescription management with medicine filtering
  - Attachment uploads (X-rays, photos, lab results)
  - Tooth status history tracking

- **Warehouse & Inventory**

  - Multi-unit item management (base unit + conversion units)
  - Batch tracking with expiry date management
  - Transaction history (Import, Export, Adjustment)
  - Supplier management with tier classification
  - Stock level monitoring and low-stock alerts

- **Employee Scheduling**

  - Fixed and flexible shift management
  - Part-time slot registration with quota system
  - Overtime request approval workflow
  - Shift renewal automation
  - Leave balance and time-off management

- **Patient Management**
  - Patient profile with medical history
  - Account creation with email verification
  - Patient-doctor assignment
  - Treatment history tracking

### Business Features

- Automated workflows (cron jobs) for shift generation and cleanup
- Service-based consumables BOM (Bill of Materials)
- Holiday management (national and company holidays)
- Multi-language support (English, Vietnamese)
- Audit logging for critical operations
- Soft delete with data retention policies

## Tech Stack

### Backend Framework

- **Spring Boot 3.2.10** - Application framework
- **Spring Data JPA** - Data access layer with Hibernate ORM
- **Spring Security 6** - Authentication and authorization
- **Spring Web MVC** - RESTful API endpoints

### Database

- **PostgreSQL 15+** - Primary relational database

### Security & Authentication

- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing
- **OAuth2 Resource Server** - Token validation

### API & Documentation

- **Swagger/OpenAPI 3** - API documentation
- **Spring REST Docs** (planned) - Production-ready API docs

### Build & Deployment

- **Maven 3.8+** - Dependency management and build tool
- **Docker** - Containerization
- **Docker Compose** - Local development orchestration

### Development Tools

- **Lombok** - Reduce boilerplate code
- **MapStruct** - Object mapping
- **SLF4J + Logback** - Logging framework

## System Requirements

### Development Environment

- **Java Development Kit (JDK) 17** or higher
- **Maven 3.8+** (or use included Maven Wrapper)
- **PostgreSQL 15+** database server
- **Git** for version control
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions

### Supported Operating Systems

- Windows 10/11
- macOS 12+
- Linux (Ubuntu 20.04+, CentOS 8+, Debian 11+)

## Getting Started

### Prerequisites

1. **Install Java 17**

   ```bash
   # Verify installation
   java -version
   # Expected output: java version "17.x.x"
   ```

2. **Install PostgreSQL 15+**

   ```bash
   # Verify installation
   psql --version
   # Expected output: psql (PostgreSQL) 15.x
   ```

3. **Create Database**
   ```sql
   CREATE DATABASE dental_clinic;
   CREATE USER dental_user WITH ENCRYPTED PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE dental_clinic TO dental_user;
   ```

### Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/DenTeeth/PDCMS_BE.git
   cd PDCMS_BE
   ```

2. **Configure database connection**

   Edit `src/main/resources/application.yaml`:

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/dental_clinic
       username: dental_user
       password: your_password
   ```

3. **Build the project**

   ```bash
   # Using Maven Wrapper (recommended)
   ./mvnw clean install -DskipTests

   # Or using system Maven
   mvn clean install -DskipTests
   ```

### Quick Start with Docker (Recommended)

The easiest way to run the application locally is using Docker Compose:

```bash
# 1. Copy environment template
cp .env.example .env

# 2. Edit .env with your values (see .env.example for details)

# 3. Start all services (PostgreSQL + Redis + Spring Boot)
docker-compose up -d

# 4. Check application health
curl http://localhost:8080/actuator/health

# 5. Access Swagger UI
# Open browser: http://localhost:8080/swagger-ui.html

# 6. View logs
docker-compose logs -f app

# 7. Stop services
docker-compose down
```

**Quick Start Scripts:**

- **Windows:** `.\start.ps1`
- **Linux/Mac:** `./start.sh` (make executable with `chmod +x start.sh`)

For detailed Docker setup and deployment guides, see:

- **[Docker Quick Reference](DOCKER_QUICK_REFERENCE.md)** - Commands and troubleshooting
- **[Deployment Guide](DEPLOYMENT_GUIDE.md)** - DigitalOcean deployment instructions

### Configuration

#### Application Properties

**Development Mode** (`application.yaml`):

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update # Auto-create tables from entities
  sql:
    init:
      mode: always # Always run seed data on startup
```

**Production Mode** (`application-prod.yaml`):

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate # Only validate schema, don't modify
  sql:
    init:
      mode: never # Don't run seed data in production
```

#### Environment Variables

For production deployment, use environment variables:

```bash
export DB_HOST=your_database_host
export DB_PORT=5432
export DB_NAME=dental_clinic
export DB_USERNAME=dental_user
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_jwt_secret_key_min_256_bits
export JWT_EXPIRATION=86400000  # 24 hours in milliseconds
```

## Project Structure

```
PDCMS_BE/
├── src/
│   ├── main/
│   │   ├── java/com/dental/clinic/management/
│   │   │   ├── DentalClinicManagementApplication.java  # Main application class
│   │   │   ├── account/                  # Account management
│   │   │   ├── authentication/           # JWT authentication & authorization
│   │   │   ├── booking_appointment/      # Appointment scheduling
│   │   │   ├── clinical_records/         # Patient clinical records
│   │   │   ├── config/                   # Spring configuration
│   │   │   │   ├── SecurityConfig.java   # Security & CORS configuration
│   │   │   │   ├── JwtConfig.java        # JWT settings
│   │   │   │   └── DataInitializer.java  # Seed data loader
│   │   │   ├── customer_contact/         # Customer contact management
│   │   │   ├── employee/                 # Employee profiles
│   │   │   ├── exception/                # Global exception handling
│   │   │   ├── patient/                  # Patient management
│   │   │   ├── permission/               # RBAC permissions
│   │   │   ├── role/                     # User roles
│   │   │   ├── scheduled/                # Cron jobs & scheduled tasks
│   │   │   ├── service/                  # Dental services catalog
│   │   │   ├── specialization/           # Doctor specializations
│   │   │   ├── treatment_plans/          # Treatment plan management
│   │   │   ├── warehouse/                # Inventory & warehouse
│   │   │   └── working_schedule/         # Employee scheduling
│   │   └── resources/
│   │       ├── application.yaml          # Application configuration
│   │       ├── application-prod.yaml     # Production configuration
│   │       └── db/
│   │           ├── schema.sql            # Database schema documentation
│   │           └── dental-clinic-seed-data.sql  # Initial seed data
│   └── test/                             # Unit and integration tests
├── docs/                                 # API documentation
│   ├── API_DOCUMENTATION.md              # API overview
│   ├── api-guides/                       # Detailed API guides by module
│   ├── architecture/                     # System architecture documents
│   └── troubleshooting/                  # Issue fixes and solutions
├── docker-compose.yml                    # Docker orchestration
├── Dockerfile                            # Docker image definition
├── pom.xml                               # Maven dependencies
├── mvnw, mvnw.cmd                        # Maven Wrapper scripts
└── README.md                             # This file
```

### Key Directories

- **`domain/`** - JPA entities (database models)
- **`dto/`** - Data Transfer Objects (request/response models)
- **`repository/`** - Spring Data JPA repositories
- **`service/`** - Business logic layer
- **`controller/`** - REST API endpoints
- **`mapper/`** - Entity ↔ DTO mappers
- **`specification/`** - JPA Specification for dynamic queries

## API Documentation

### Base URL

- **Local Development**: `http://localhost:8080/api/v1`
- **Production**: `https://your-domain.com/api/v1`

### Authentication

All API endpoints (except `/auth/login` and `/auth/refresh`) require JWT authentication:

```bash
# Login to get JWT token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_username",
    "password": "your_password"
  }'

# Use token in subsequent requests
curl -X GET http://localhost:8080/api/v1/appointments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### API Modules

| Module               | Endpoints                    | Description                               |
| -------------------- | ---------------------------- | ----------------------------------------- |
| **Authentication**   | `/auth/*`                    | Login, refresh token, logout              |
| **Appointments**     | `/appointments/*`            | Schedule, reschedule, cancel appointments |
| **Treatment Plans**  | `/patient-treatment-plans/*` | Create and manage treatment plans         |
| **Clinical Records** | `/clinical-records/*`        | Patient dental records and procedures     |
| **Warehouse**        | `/warehouse/*`               | Inventory and supplier management         |
| **Employees**        | `/employees/*`               | Employee profiles and schedules           |
| **Patients**         | `/patients/*`                | Patient management                        |
| **Services**         | `/services/*`                | Dental services catalog                   |

### Swagger/OpenAPI

Interactive API documentation available at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Detailed API Guides

Comprehensive API guides with examples in `docs/api-guides/`:

- [Appointment Management APIs](docs/api-guides/booking/appointment/)
- [Treatment Plan APIs](docs/api-guides/treatment-plan/)
- [Clinical Records APIs](docs/api-guides/clinical-records/)
- [Warehouse APIs](docs/api-guides/warehouse/)
- [Employee Schedule APIs](docs/api-guides/shift-management/)

## Database

### Schema Management

The project uses **Hibernate DDL Auto** for schema management:

- **Development**: `ddl-auto: update` (auto-creates tables from entities)
- **Production**: `ddl-auto: validate` (only validates, doesn't modify)

### Important Files

1. **`schema.sql`** - Database schema documentation (reference only, NOT executed)
2. **`dental-clinic-seed-data.sql`** - Initial seed data (executed on startup)

### Seed Data

Default test accounts (password: `123456` for all):

| Username    | Role         | Description                             |
| ----------- | ------------ | --------------------------------------- |
| `admin`     | ADMIN        | System administrator                    |
| `bacsi1`    | DENTIST      | Dr. Le Anh Khoa (Full-time dentist)     |
| `bacsi2`    | DENTIST      | Dr. Trinh Cong Thai (Full-time dentist) |
| `letan1`    | RECEPTIONIST | Receptionist                            |
| `ketoan1`   | ACCOUNTANT   | Accountant                              |
| `yta1`      | NURSE        | Nurse                                   |
| `quanli1`   | MANAGER      | Clinic manager                          |
| `benhnhan1` | PATIENT      | Test patient                            |

### Database Migrations

For production deployments, consider using Flyway or Liquibase for controlled schema migrations.

### Backup & Restore

```bash
# Backup database
pg_dump -U dental_user -d dental_clinic > backup_$(date +%Y%m%d).sql

# Restore database
psql -U dental_user -d dental_clinic < backup_20251207.sql
```

## Security

### Authentication Flow

1. **Login** → Receive JWT access token + refresh token
2. **API Requests** → Include `Authorization: Bearer <access_token>` header
3. **Token Expiration** → Use refresh token to get new access token
4. **Logout** → Invalidate refresh token in database

### Password Security

- **Hashing**: BCrypt with strength 10
- **Minimum Length**: 6 characters (configurable)
- **Password Reset**: Email-based with expiring tokens (24 hours)
- **Email Verification**: Required for new patient accounts

### Authorization (RBAC)

9 predefined roles with granular permissions:

- **ROLE_ADMIN** - Full system access
- **ROLE_DENTIST** - Dental services, treatment plans, clinical records
- **ROLE_NURSE** - Assist doctors, limited clinical access
- **ROLE_RECEPTIONIST** - Appointments, patient registration
- **ROLE_ACCOUNTANT** - Financial data, invoices
- **ROLE_INVENTORY_MANAGER** - Warehouse and inventory
- **ROLE_MANAGER** - Employee scheduling, approvals
- **ROLE_DENTIST_INTERN** - Limited training access
- **ROLE_PATIENT** - Personal records, appointments

### Security Best Practices

- JWT tokens stored in memory only (never localStorage)
- Refresh tokens hashed in database (SHA-512)
- CORS configured for specific origins
- SQL injection prevention via JPA parameterized queries
- XSS protection headers enabled
- Rate limiting (planned)

## Testing

### Test Accounts

Use seed data accounts (password: `123456`) for testing:

```bash
# Login as dentist
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bacsi1","password":"123456"}'
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AppointmentServiceTest

# Run with coverage
./mvnw test jacoco:report
```

### Test Coverage

- Unit tests for service layer
- Integration tests for API endpoints (in progress)
- Repository tests for complex queries

### Manual API Testing

Refer to detailed test guides in `docs/api-guides/` for step-by-step testing instructions with curl commands and expected responses.

## Deployment

### Docker Deployment

**Build and run with Docker Compose:**

```bash
# Build and start services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### Production Deployment Checklist

- [ ] Set `spring.profiles.active=prod`
- [ ] Configure production database credentials
- [ ] Use strong JWT secret (minimum 256 bits)
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS for production domain
- [ ] Set up database backups
- [ ] Configure log aggregation (ELK, Splunk, etc.)
- [ ] Set up monitoring (Prometheus, Grafana)
- [ ] Configure reverse proxy (Nginx, Apache)
- [ ] Set resource limits (memory, CPU)
- [ ] Enable rate limiting
- [ ] Review and adjust security headers

### Environment-Specific Configuration

**Staging:**

```bash
java -jar app.jar --spring.profiles.active=staging
```

**Production:**

```bash
java -jar app.jar --spring.profiles.active=prod \
  -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -Dserver.port=8080
```

### Cloud Deployment

The application is cloud-ready and can be deployed on:

- **AWS**: Elastic Beanstalk, ECS, EKS
- **Azure**: App Service, Container Instances, AKS
- **GCP**: App Engine, Cloud Run, GKE
- **Heroku**: With Heroku Postgres addon
- **Railway**: Native PostgreSQL support

## Troubleshooting

### Common Issues

**1. Application fails to start - "Connection refused" error**

```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Verify connection
psql -U dental_user -d dental_clinic -h localhost
```

**2. "Table does not exist" errors**

Ensure Hibernate DDL auto is set correctly:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update # For development
```

**3. JWT token expired**

Use refresh token to get new access token:

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"YOUR_REFRESH_TOKEN"}'
```

**4. Permission denied errors**

Check user role has required permission:

```sql
-- Query user permissions
SELECT p.permission_id
FROM accounts a
JOIN roles r ON a.role_id = r.role_id
JOIN role_permissions rp ON r.role_id = rp.role_id
JOIN permissions p ON rp.permission_id = p.permission_id
WHERE a.username = 'bacsi1';
```

### Detailed Troubleshooting Guides

See `docs/troubleshooting/` for issue-specific guides:

- [Issue #37: Clinical Record Tab Fix](docs/troubleshooting/ISSUE_37_CLINICAL_RECORD_TAB_FIX.md)
- [Issue #40: Treatment Plan Status Sync](docs/troubleshooting/ISSUE_40_FIX_VERIFICATION.md)
- [Issue #47: Plan Status Data Fix](docs/troubleshooting/ISSUE_47_PLAN_STATUS_FIX.md)
- [Seed Data Initialization Issues](docs/troubleshooting/SEED_DATA_INITIALIZATION_FIX_20251128.md)

### Logs

Application logs location:

- **Console**: Standard output (development)
- **File**: `logs/spring-boot-application.log` (production)

Enable debug logging:

```yaml
logging:
  level:
    com.dental.clinic.management: DEBUG
```

## Contributing

### Development Workflow

1. **Fork the repository**
2. **Create feature branch**: `git checkout -b feat/BE-XXX-feature-name`
3. **Commit changes**: `git commit -m "feat: Add new feature"`
4. **Push to branch**: `git push origin feat/BE-XXX-feature-name`
5. **Create Pull Request**

### Commit Message Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: Add appointment reschedule API
fix: Fix treatment plan status sync issue
docs: Update API documentation
refactor: Simplify appointment validation logic
test: Add unit tests for warehouse service
chore: Update dependencies
```

### Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok to reduce boilerplate
- Write Javadoc for public APIs
- Use meaningful variable names
- Maximum line length: 120 characters

### Testing Requirements

- Write unit tests for service layer
- Integration tests for complex workflows
- Minimum 70% code coverage for new features

## License

**Private/Proprietary License**

Copyright (c) 2024-2025 DenTeeth Dental Clinic Management

All rights reserved. This software and associated documentation files (the "Software") are proprietary and confidential. Unauthorized copying, distribution, or use of this Software is strictly prohibited.

For licensing inquiries, contact: [your-email@example.com]

---

## Support

For technical support or questions:

- **Email**: support@example.com
- **Documentation**: [docs/](docs/)
- **Issue Tracker**: GitHub Issues (for authorized users)

## Roadmap

### Q1 2025

- [ ] Implement Flyway database migrations
- [ ] Add Spring REST Docs for API documentation
- [ ] Integration with payment gateways
- [ ] SMS notification system

### Q2 2025

- [ ] Multi-clinic support (SaaS mode)
- [ ] Advanced reporting and analytics
- [ ] Mobile app backend APIs
- [ ] Real-time notifications with WebSocket

### Q3 2025

- [ ] AI-powered appointment scheduling
- [ ] Telemedicine integration
- [ ] Electronic health records (EHR) export
- [ ] Integration with dental equipment

---

**Built by DenTeeth Development Team**

_Last Updated: December 7, 2025_
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-compiler-plugin</artifactId>
<configuration>
<release>17</release>
</configuration>
</plugin>
</plugins>
</build>

```

If the environment still shows the error, it means the machine running the build does not have a JDK 17 configured. Set the JDK as shown above and re-run.

## Run the application

- Ensure MySQL is running and the connection info in `src/main/resources/application.yaml` (or `application.properties`) matches your local setup.
- Start the app:
  - Windows: `mvnw.cmd spring-boot:run`
  - macOS/Linux: `./mvnw spring-boot:run`

The app will start on `http://localhost:8080`.

## Notes

- Both `application.yaml` and `application.properties` exist for convenience; you can keep one. YAML is the default in tests.
- Swagger UI is available at `/swagger-ui.html` when the app is running.
```
