# 🚀 HƯỚNG DẪN COPY .ENV LÊN DROPLET

## ⚡ CÁCH NHANH NHẤT (1 LỆNH)

### Trên Windows (PowerShell):

```powershell
.\copy-env-to-droplet.ps1 -DropletIP YOUR_DROPLET_IP
```

### Trên Linux/Mac:

```bash
chmod +x copy-env-to-droplet.sh
./copy-env-to-droplet.sh YOUR_DROPLET_IP
```

---

## 📋 CÁCH THỦ CÔNG (Nếu script không chạy)

### Bước 1: Copy file lên Droplet

```bash
scp .env.production root@YOUR_DROPLET_IP:/root/pdcms-be/.env
```

### Bước 2: Set permissions

```bash
ssh root@YOUR_DROPLET_IP "chmod 600 /root/pdcms-be/.env"
```

### Bước 3: Verify

```bash
ssh root@YOUR_DROPLET_IP "cat /root/pdcms-be/.env"
```

---

## 🔒 CÁC GIÁ TRỊ CẦN THAY ĐỔI

Sau khi copy file lên, SSH vào Droplet và edit:

```bash
ssh root@YOUR_DROPLET_IP
cd /root/pdcms-be
nano .env
```

### Thay đổi các giá trị sau:

#### 1. Database Password

```bash
# Generate strong password
openssl rand -base64 32

# Kết quả: aB3fG9kL2mN7pQ5rS8tU1vW4xY6zC0dE2fG5hJ8kL1mN4pQ7rS
# Copy và thay vào dòng:
DB_PASSWORD=aB3fG9kL2mN7pQ5rS8tU1vW4xY6zC0dE2fG5hJ8kL1mN4pQ7rS
```

#### 2. Redis Password

```bash
# Generate strong password
openssl rand -base64 32

# Kết quả: xY1zA3bC5dE7fG9hJ2kL4mN6pQ8rS0tU3vW5xY7zA9bC1dE4fG
# Copy và thay vào dòng:
REDIS_PASSWORD=xY1zA3bC5dE7fG9hJ2kL4mN6pQ8rS0tU3vW5xY7zA9bC1dE4fG
```

#### 3. JWT Secret (256 bits minimum)

```bash
# Generate strong secret
openssl rand -base64 64

# Kết quả: mN2pQ4rS6tU8vW0xY2zA4bC6dE8fG0hJ2kL4mN6pQ8rS0tU2vW4xY6zA8bC0dE2fG4hJ6kL8mN0pQ2rS4tU6vW8xY0zA
# Copy và thay vào dòng:
JWT_SECRET=mN2pQ4rS6tU8vW0xY2zA4bC6dE8fG0hJ2kL4mN6pQ8rS0tU2vW4xY6zA8bC0dE2fG4hJ6kL8mN0pQ2rS4tU6vW8xY0zA
```

#### 4. Frontend URL

```env
# Thay đổi từ:
FRONTEND_URL=http://localhost:3000

# Sang domain thật của bạn:
FRONTEND_URL=https://pdcms.yourcompany.com
# hoặc
FRONTEND_URL=https://www.yourcompany.com
```

### Lưu file:

- Nhấn `Ctrl + X`
- Nhấn `Y`
- Nhấn `Enter`

---

## ✅ VERIFY CẤU HÌNH

### Check file .env:

```bash
cat /root/pdcms-be/.env
```

### Check permissions:

```bash
ls -la /root/pdcms-be/.env
# Kết quả mong đợi: -rw------- 1 root root ... .env
```

---

## 🚀 START SERVICES

```bash
cd /root/pdcms-be
docker-compose down
docker-compose up -d

# Wait 30 seconds
sleep 30

# Check logs
docker-compose logs -f app
```

---

## 🏥 HEALTH CHECK

```bash
# Test database connection
docker-compose exec postgres psql -U pdcms_user -d pdcms_db -c "SELECT 1;"

# Test Redis
docker-compose exec redis redis-cli -a YOUR_NEW_REDIS_PASSWORD ping

# Test API
curl http://localhost:8080/actuator/health
```

---

## 📝 NOTES

### File đã được tạo:

1. ✅ `.env` - File local với config mẫu và hướng dẫn đầy đủ
2. ✅ `.env.production` - File template sẵn sàng copy lên Droplet
3. ✅ `copy-env-to-droplet.sh` - Script tự động cho Linux/Mac
4. ✅ `copy-env-to-droplet.ps1` - Script tự động cho Windows

### Thứ tự thực hiện:

1. ✅ Copy file `.env.production` lên Droplet (đổi tên thành `.env`)
2. ✅ Generate passwords mạnh
3. ✅ Update các giá trị trong `.env`
4. ✅ Start Docker containers
5. ✅ Verify health check
6. ✅ Push code để trigger GitHub Actions

### Security:

- ⚠️ **KHÔNG** commit file `.env` vào Git
- ⚠️ File `.env` đã có trong `.gitignore`
- ⚠️ Chỉ lưu passwords trong password manager
- ⚠️ Set permissions 600 cho file `.env`

---

## 🎉 XEM THÊM

- Quick Deploy Guide: `QUICK_DEPLOY_GUIDE.md`
- Step by Step Guide: `docs/DEPLOY_TO_DIGITALOCEAN_STEP_BY_STEP.md`
- Deployment Checklist: `DEPLOYMENT_CHECKLIST.md`

---

## 📞 TRỢ GIÚP

Nếu gặp lỗi:

```bash
# Check Docker logs
docker-compose logs app

# Check database
docker-compose logs postgres

# Check Redis
docker-compose logs redis

# Restart services
docker-compose restart
```
