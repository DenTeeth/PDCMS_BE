# PostgreSQL Setup Guide for PDCMS_BE (Docker + DBeaver)

## Prerequisites
- **Docker Desktop** installed and running
- **DBeaver 7.3.5+** (for database management)
- **Git** (to checkout the branch)
- **Maven** (to run the application)

---

## Quick Start (5 Steps)

### Step 1: Checkout the PostgreSQL Branch
```bash
git checkout postgre
git pull origin postgre
```

### Step 2: Start PostgreSQL with Docker Compose
```bash
docker-compose up -d
```

This command will:
- Download PostgreSQL 14 image (if not already downloaded)
- Create a container named `dental_clinic_postgres`
- Automatically create database `dental_clinic_db`
- Set up user `root` with password `123456`
- Expose PostgreSQL on port `5432`

**Verify the container is running:**
```bash
docker ps
```
You should see `dental_clinic_postgres` in the list with status "Up".

### Step 3: Run the Spring Boot Application
```bash
mvn clean install
mvn spring-boot:run
```

The application will:
- Connect to the PostgreSQL database in Docker
- Create all tables automatically (via Hibernate)
- Load all seed data from `dental-clinic-seed-data_postgres.sql`

**Wait for this log message:**
```
Started DentalClinicManagementApplication in X.XXX seconds
```

### Step 4: Verify Database Connection in DBeaver
1. Open **DBeaver**
2. Click **Database** → **New Database Connection** (or plug icon 🔌)
3. Select **PostgreSQL** → Click **Next**
4. Enter connection details:
   - **Host:** `localhost`
   - **Port:** `5432`
   - **Database:** `dental_clinic_db`
   - **Username:** `root`
   - **Password:** `123456`
5. Click **Test Connection** ✅ (should show "Connected")
6. Click **Finish**

### Step 5: Browse the Data in DBeaver
In DBeaver, expand the connection and verify:

**Tables created:**
- `roles`, `permissions`, `role_permissions`
- `accounts`, `account_roles`
- `employees`, `employee_specializations`
- `patients`, `specializations`
- And more...

**Sample data loaded:**
- **Roles:** 7 main roles + 4 legacy roles
- **Permissions:** 32 permissions (including Work Shifts)
- **Accounts:** 14 accounts total (admin, doctors, staff, patients, legacy)
- **Employees:** 11 employees (6 main + 5 legacy)
- **Patients:** 3 patients
- **Specializations:** 7 specializations

✅ **Done!** Your database is ready with all test data!

---

## Docker Commands Reference

### Start PostgreSQL Container
```bash
docker-compose up -d
```
Use `-d` flag to run in detached mode (background).

### Stop PostgreSQL Container
```bash
docker-compose down
```
This stops the container but keeps the data.

### Stop and Remove All Data (Fresh Start)
```bash
docker-compose down -v
```
⚠️ This deletes the database volume! Use only when you want to completely reset.

### View Container Logs
```bash
docker logs dental_clinic_postgres
```

### Check Container Status
```bash
docker ps
```

### Access PostgreSQL Shell (Optional)
```bash
docker exec -it dental_clinic_postgres psql -U root -d dental_clinic_db
```
Type `\q` to exit.

### Restart Container
```bash
docker-compose restart
```

---

## What's in docker-compose.yml?

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14                      # PostgreSQL version 14
    container_name: dental_clinic_postgres  # Container name
    restart: "no"
    environment:
      POSTGRES_USER: root                   # Username
      POSTGRES_PASSWORD: 123456             # Password
      POSTGRES_DB: dental_clinic_db         # Database name
      TZ: "Asia/Ho_Chi_Minh"               # Timezone
    ports:
      - "5432:5432"                         # Map port 5432
    volumes:
      - postgres-data:/var/lib/postgresql/data  # Persist data

volumes:
  postgres-data:
    name: dental_postgres_data              # Named volume
```

**Key Points:**
- Database and user are created automatically by Docker
- Data persists even after stopping the container (stored in `dental_postgres_data` volume)
- Port 5432 is exposed to localhost for DBeaver/application connection

---

## Application Configuration

The application connects using these settings in `application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dental_clinic_db
    username: root
    password: 123456
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update    # Creates/updates tables automatically

  sql:
    init:
      mode: always        # Always run seed data script
      data-locations: classpath:db/pg/dental-clinic-seed-data_postgres.sql
```

---

## Test Credentials After Setup

Once the application is running and data is loaded, you can login with:

### Admin:
- Username: `admin`
- Password: `123456`

### Doctors:
- Username: `nhasi1` | Password: `123456`
- Username: `nhasi2` | Password: `123456`

### Staff:
- Username: `yta` (Nurse) | Password: `123456`
- Username: `letan` (Receptionist) | Password: `123456`
- Username: `ketoan` (Accountant) | Password: `123456`

### Patients:
- Username: `benhnhan1` | Password: `123456`
- Username: `benhnhan2` | Password: `123456`
- Username: `benhnhan3` | Password: `123456`

---

## Troubleshooting

### Problem 1: "docker-compose: command not found"
**Solution:**
- Install Docker Desktop from: https://www.docker.com/products/docker-desktop
- Make sure Docker Desktop is running

### Problem 2: "Cannot connect to the Docker daemon"
**Solution:**
- Start Docker Desktop application
- Wait until Docker icon shows "Docker Desktop is running"

### Problem 3: "Port 5432 is already in use"
**Solution:**
- Another PostgreSQL instance is using port 5432
- Option A: Stop the other PostgreSQL service
- Option B: Change the port in `docker-compose.yml`:
  ```yaml
  ports:
    - "5433:5432"  # Use port 5433 instead
  ```
  Then update `application.yaml` to use `jdbc:postgresql://localhost:5433/...`

### Problem 4: "Connection refused" in DBeaver
**Solution:**
- Check if container is running: `docker ps`
- If not running, start it: `docker-compose up -d`
- Check container logs: `docker logs dental_clinic_postgres`

### Problem 5: "Database dental_clinic_db does not exist"
**Solution:**
- The container might not have fully initialized
- Wait 10-15 seconds after `docker-compose up -d`
- Check logs: `docker logs dental_clinic_postgres`
- Look for: "database system is ready to accept connections"

### Problem 6: Application starts but no seed data
**Solution:**
- Check that seed script ran: Look for logs mentioning "Executing SQL scripts"
- If not, try:
  ```bash
  docker-compose down -v
  docker-compose up -d
  mvn spring-boot:run
  ```

---

## Fresh Start Instructions

If you want to completely reset everything:

```bash
# Stop and remove container + volume
docker-compose down -v

# Start fresh
docker-compose up -d

# Wait 10 seconds for initialization
timeout 10

# Run application
mvn spring-boot:run
```

---

## Still Having Issues?

Contact the team and provide:
1. Docker version: `docker --version`
2. Container status: `docker ps -a`
3. Container logs: `docker logs dental_clinic_postgres`
4. Application error message

---

**Happy Coding! 🚀**

**Note:** This setup uses Docker, so you don't need to install PostgreSQL locally. Everything runs in an isolated container!
