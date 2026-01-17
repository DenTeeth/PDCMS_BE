# 🚀 HƯỚNG DẪN DEPLOY PDCMS LÊN DIGITALOCEAN

## 📋 Mục lục

- [Yêu cầu](#yêu-cầu)
- [Bước 1: Chuẩn bị Droplet](#bước-1-chuẩn-bị-droplet)
- [Bước 2: Cấu hình GitHub Repository](#bước-2-cấu-hình-github-repository)
- [Bước 3: Cấu hình Discord Webhook](#bước-3-cấu-hình-discord-webhook-tùy-chọn)
- [Bước 4: Deploy lần đầu](#bước-4-deploy-lần-đầu)
- [Bước 5: Kích hoạt GitHub Actions](#bước-5-kích-hoạt-github-actions)
- [Xử lý sự cố](#xử-lý-sự-cố)

---

## ✅ Yêu cầu

### Trên máy local:

- Git đã cài đặt
- SSH key để truy cập Droplet

### Trên DigitalOcean Droplet:

- Ubuntu 20.04/22.04 LTS (recommended)
- Docker & Docker Compose đã cài đặt
- Ít nhất 2GB RAM
- Port 80, 443, 8080 đã mở

---

## 🖥️ Bước 1: Chuẩn bị Droplet

### 1.1. SSH vào Droplet

```bash
ssh root@YOUR_DROPLET_IP
```

### 1.2. Cài đặt Docker & Docker Compose (nếu chưa có)

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Start Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installation
docker --version
docker-compose --version
```

### 1.3. Tạo thư mục dự án

```bash
# Cho Production (main branch)
mkdir -p /root/pdcms-be
cd /root/pdcms-be

# Cho Staging (develop branch) - TÙY CHỌN
mkdir -p /root/pdcms-be-staging
```

### 1.4. Clone repository

```bash
# Clone vào thư mục production
cd /root/pdcms-be
git clone https://github.com/YOUR_USERNAME/PDCMS_BE.git .

# Hoặc nếu bạn đã clone rồi:
cd /root/pdcms-be
git remote set-url origin https://github.com/YOUR_USERNAME/PDCMS_BE.git
git fetch origin
git checkout main
git pull origin main
```

### 1.5. Tạo file .env

```bash
cd /root/pdcms-be
nano .env
```

Copy nội dung sau và điều chỉnh:

```env
# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Database Configuration
DB_USERNAME=pdcms_user
DB_PASSWORD=YOUR_STRONG_DB_PASSWORD_HERE
DB_DATABASE=pdcms_db
DB_PORT=5432

# Redis Configuration
REDIS_PASSWORD=YOUR_STRONG_REDIS_PASSWORD_HERE
REDIS_PORT=6379

# Application Port
APP_PORT=8080

# JWT Configuration
JWT_SECRET=YOUR_SUPER_SECRET_JWT_KEY_AT_LEAST_256_BITS_LONG_HERE
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=2592000000

# Email Configuration (Gmail example)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Frontend URL
FRONTEND_URL=https://your-frontend-domain.com

# Database Timezone
TZ=Asia/Ho_Chi_Minh
```

**Lưu ý:** Nhấn `Ctrl+X`, sau đó `Y`, sau đó `Enter` để lưu file.

### 1.6. Tạo file .env cho Staging (TÙY CHỌN)

```bash
cd /root/pdcms-be-staging
git clone https://github.com/YOUR_USERNAME/PDCMS_BE.git .
git checkout develop
nano .env
```

(Sử dụng cấu hình tương tự nhưng có thể thay đổi DB_DATABASE, ports...)

---

## 🔐 Bước 2: Cấu hình GitHub Repository

### 2.1. Lấy SSH Private Key từ Droplet

```bash
# Nếu chưa có SSH key, tạo mới:
ssh-keygen -t rsa -b 4096 -C "github-actions-deploy"

# Hiển thị private key
cat ~/.ssh/id_rsa
```

**Copy toàn bộ nội dung** (bao gồm `-----BEGIN RSA PRIVATE KEY-----` và `-----END RSA PRIVATE KEY-----`)

### 2.2. Thêm Public Key vào authorized_keys

```bash
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
```

### 2.3. Thêm Secrets vào GitHub Repository

1. Vào repository trên GitHub
2. Vào **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** và thêm các secret sau:

| Secret Name   | Value                                          | Mô tả                               |
| ------------- | ---------------------------------------------- | ----------------------------------- |
| `DO_SSH_KEY`  | (Nội dung private key từ bước 2.1)             | SSH private key để truy cập Droplet |
| `DO_HOST`     | IP address của Droplet (vd: `134.209.xxx.xxx`) | IP hoặc domain của Droplet          |
| `DO_USERNAME` | `root`                                         | Username SSH (thường là root)       |

**Ví dụ:**

```
DO_SSH_KEY:
-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEAx7jK...
...toàn bộ nội dung key...
-----END RSA PRIVATE KEY-----

DO_HOST:
134.209.100.50

DO_USERNAME:
root
```

---

## 🔔 Bước 3: Cấu hình Discord Webhook (TÙY CHỌN)

### 3.1. Tạo Discord Webhook

1. Vào Discord Server của bạn
2. Chọn channel muốn nhận thông báo
3. Click vào **Settings** (icon bánh răng) → **Integrations**
4. Click **Create Webhook** hoặc **View Webhooks**
5. Click **New Webhook**
6. Đặt tên (vd: "PDCMS Deployment")
7. Chọn channel
8. Click **Copy Webhook URL**

### 3.2. Thêm Webhook vào GitHub Secrets

Trong GitHub Repository:

- **Settings** → **Secrets and variables** → **Actions**
- **New repository secret**

| Secret Name       | Value                                             |
| ----------------- | ------------------------------------------------- |
| `DISCORD_WEBHOOK` | https://discord.com/api/webhooks/YOUR_WEBHOOK_URL |

**Lưu ý:** Nếu không muốn dùng Discord, có thể xóa hoặc comment các step Discord notification trong file `.github/workflows/deploy.yml`

---

## 🚀 Bước 4: Deploy lần đầu (Manual)

Trước khi dùng GitHub Actions, nên test deploy thủ công:

```bash
# SSH vào Droplet
ssh root@YOUR_DROPLET_IP

# Vào thư mục dự án
cd /root/pdcms-be

# Pull latest code
git fetch origin
git reset --hard origin/main
git pull origin main

# Build và start services
docker-compose down
docker-compose build --no-cache
docker-compose up -d

# Check logs
docker-compose logs -f app

# Check health
curl http://localhost:8080/actuator/health
```

### Kiểm tra services:

```bash
# Xem trạng thái containers
docker-compose ps

# Xem logs từng service
docker-compose logs postgres
docker-compose logs redis
docker-compose logs app

# Vào container để debug
docker-compose exec app sh
```

---

## ⚙️ Bước 5: Kích hoạt GitHub Actions

Sau khi deploy thủ công thành công, giờ có thể dùng GitHub Actions:

### 5.1. Push code để trigger deployment

```bash
# Trên máy local
git add .
git commit -m "feat: trigger deployment"
git push origin main        # Deploy to Production
# hoặc
git push origin develop     # Deploy to Staging
```

### 5.2. Theo dõi deployment

1. Vào GitHub repository
2. Click tab **Actions**
3. Xem workflow đang chạy
4. Click vào run để xem chi tiết từng step

### 5.3. Manual deployment (nếu cần)

1. Vào **Actions** tab
2. Chọn workflow **"🚀 Deploy PDCMS Backend to Digital Ocean"**
3. Click **Run workflow**
4. Chọn branch (`main` hoặc `develop`)
5. Click **Run workflow**

---

## 🎯 Luồng hoạt động

### Production Deployment (Branch: main)

```
Push to main
    ↓
GitHub Actions triggered
    ↓
1. Checkout code
2. Setup SSH
3. Backup current version
4. Pull latest code
5. Build Docker image
6. Stop old containers
7. Start new containers
8. Health check
    ↓
Success → Discord notification ✅
Failed → Rollback + Discord notification ❌
```

### Staging Deployment (Branch: develop)

```
Push to develop
    ↓
Deploy to /root/pdcms-be-staging
```

---

## 🛠️ Xử lý sự cố

### Lỗi 1: SSH Connection Failed

**Triệu chứng:**

```
Error: ssh: connect to host xxx.xxx.xxx.xxx port 22: Connection refused
```

**Giải pháp:**

```bash
# Kiểm tra SSH service trên Droplet
sudo systemctl status ssh

# Restart SSH service
sudo systemctl restart ssh

# Kiểm tra firewall
sudo ufw status
sudo ufw allow 22/tcp
```

### Lỗi 2: Docker Build Failed

**Triệu chứng:**

```
Error: failed to solve: process "/bin/sh -c mvn clean package" did not complete successfully
```

**Giải pháo:**

```bash
# SSH vào Droplet
cd /root/pdcms-be

# Build thủ công để xem lỗi
docker-compose build --no-cache

# Xem logs chi tiết
docker-compose logs app

# Kiểm tra file pom.xml
```

### Lỗi 3: Health Check Failed

**Triệu chứng:**

```
❌ Health check failed - initiating rollback...
```

**Giải pháp:**

```bash
# Xem logs ứng dụng
docker-compose logs --tail=200 app

# Kiểm tra database connection
docker-compose exec postgres psql -U pdcms_user -d pdcms_db -c "SELECT 1;"

# Kiểm tra Redis
docker-compose exec redis redis-cli -a YOUR_REDIS_PASSWORD ping

# Kiểm tra port
curl http://localhost:8080/actuator/health
```

### Lỗi 4: Database Connection Failed

**Triệu chứng:**

```
org.postgresql.util.PSQLException: Connection refused
```

**Giải pháp:**

```bash
# Kiểm tra PostgreSQL container
docker-compose ps postgres

# Restart PostgreSQL
docker-compose restart postgres

# Kiểm tra logs
docker-compose logs postgres

# Test connection
docker-compose exec postgres psql -U pdcms_user -d pdcms_db
```

### Lỗi 5: Port Already in Use

**Triệu chứng:**

```
Error: port is already allocated
```

**Giải pháp:**

```bash
# Tìm process đang dùng port 8080
sudo lsof -i :8080

# Kill process
sudo kill -9 <PID>

# Hoặc stop tất cả containers
docker-compose down
docker-compose up -d
```

### Lỗi 6: Out of Memory

**Triệu chứng:**

```
java.lang.OutOfMemoryError: Java heap space
```

**Giải pháo:**

```bash
# Tăng memory cho Docker trong Dockerfile
# Sửa file Dockerfile, thay đổi:
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]

# Rebuild
docker-compose down
docker-compose build --no-cache app
docker-compose up -d
```

---

## 📊 Kiểm tra trạng thái hệ thống

### Kiểm tra containers

```bash
# Xem tất cả containers
docker-compose ps

# Xem resource usage
docker stats

# Xem logs real-time
docker-compose logs -f
```

### Kiểm tra database

```bash
# Vào PostgreSQL
docker-compose exec postgres psql -U pdcms_user -d pdcms_db

# List tables
\dt

# Check connections
SELECT * FROM pg_stat_activity;

# Exit
\q
```

### Kiểm tra Redis

```bash
# Vào Redis CLI
docker-compose exec redis redis-cli -a YOUR_REDIS_PASSWORD

# Check info
INFO

# List keys
KEYS *

# Exit
exit
```

### Kiểm tra API

```bash
# Health check
curl http://localhost:8080/actuator/health

# Với Nginx
curl https://your-domain.com/actuator/health
```

---

## 🔄 Rollback thủ công

Nếu GitHub Actions rollback tự động thất bại:

```bash
# SSH vào Droplet
ssh root@YOUR_DROPLET_IP
cd /root/pdcms-be

# Xem commit trước đó
git log --oneline -10

# Rollback về commit cụ thể
git reset --hard <COMMIT_HASH>

# Rebuild và restart
docker-compose down
docker-compose build --no-cache app
docker-compose up -d

# Check logs
docker-compose logs -f app
```

---

## 📝 Maintenance Commands

### Xem logs

```bash
# All logs
docker-compose logs

# Specific service
docker-compose logs app
docker-compose logs postgres
docker-compose logs redis

# Follow logs
docker-compose logs -f app

# Last 100 lines
docker-compose logs --tail=100 app
```

### Restart services

```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart app
docker-compose restart postgres
```

### Clean up

```bash
# Stop and remove containers
docker-compose down

# Remove volumes (CẢNH BÁO: Xóa data)
docker-compose down -v

# Remove unused images
docker image prune -a

# Remove unused volumes
docker volume prune
```

### Backup database

```bash
# Backup
docker-compose exec postgres pg_dump -U pdcms_user pdcms_db > backup_$(date +%Y%m%d).sql

# Restore
cat backup_20231213.sql | docker-compose exec -T postgres psql -U pdcms_user pdcms_db
```

---

## 🎓 Best Practices

### 1. Sử dụng branches đúng cách

- `main` → Production (stable, tested)
- `develop` → Staging (testing, preview)
- `feat/*` → Feature branches (development)

### 2. Testing trước khi merge vào main

```bash
# Test trên local hoặc staging trước
git checkout develop
# Test thoroughly
# Nếu OK, merge vào main
git checkout main
git merge develop
git push origin main
```

### 3. Monitor logs thường xuyên

```bash
# Check logs hàng ngày
docker-compose logs --tail=100 app | grep ERROR
```

### 4. Backup database định kỳ

```bash
# Tạo cron job để backup tự động
crontab -e

# Thêm dòng này (backup mỗi ngày lúc 2 giờ sáng)
0 2 * * * cd /root/pdcms-be && docker-compose exec -T postgres pg_dump -U pdcms_user pdcms_db > /root/backups/pdcms_$(date +\%Y\%m\%d).sql
```

### 5. Update Docker images định kỳ

```bash
# Pull latest images
docker-compose pull

# Rebuild
docker-compose up -d --build
```

---

## 📞 Liên hệ & Hỗ trợ

Nếu gặp vấn đề:

1. ✅ Check logs: `docker-compose logs -f app`
2. ✅ Check GitHub Actions logs
3. ✅ Check Discord notifications (if configured)
4. ✅ Try manual deployment
5. ✅ Rollback if needed

---

## 🎉 Hoàn thành!

Giờ bạn đã có:

- ✅ GitHub Actions tự động deploy
- ✅ Blue-Green deployment strategy
- ✅ Automatic rollback on failure
- ✅ Discord notifications
- ✅ Health checks
- ✅ Backup before deployment

**Happy Deploying! 🚀**
