# 🚀 QUICK START - Deploy PDCMS lên DigitalOcean

## ⚡ TÓM TẮT CÁC BƯỚC

Bạn đã clone project trên Droplet rồi → Làm theo các bước sau:

---

## 📍 BƯỚC 1: Setup trên Droplet (5 phút)

### SSH vào Droplet:

```bash
ssh root@YOUR_DROPLET_IP
```

### Đảm bảo code đã được clone đúng vị trí:

```bash
# Production
cd /root/pdcms-be

# Nếu chưa clone:
mkdir -p /root/pdcms-be
cd /root/pdcms-be
git clone https://github.com/DenTeeth/PDCMS_BE.git .
git checkout main
```

### Tạo file `.env`:

```bash
cd /root/pdcms-be
nano .env
```

Copy và điền thông tin:

```env
SPRING_PROFILES_ACTIVE=prod
DB_USERNAME=pdcms_user
DB_PASSWORD=YOUR_STRONG_PASSWORD
DB_DATABASE=pdcms_db
REDIS_PASSWORD=YOUR_REDIS_PASSWORD
JWT_SECRET=YOUR_256_BIT_JWT_SECRET_KEY
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
FRONTEND_URL=https://your-domain.com
```

Lưu: `Ctrl+X` → `Y` → `Enter`

---

## 📍 BƯỚC 2: Lấy SSH Key (2 phút)

```bash
# Nếu chưa có SSH key, tạo mới:
ssh-keygen -t rsa -b 4096 -C "github-deploy"

# Hiển thị private key
cat ~/.ssh/id_rsa
```

**→ Copy toàn bộ nội dung** (từ `-----BEGIN` đến `-----END`)

Thêm public key vào authorized_keys:

```bash
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
```

---

## 📍 BƯỚC 3: Setup GitHub Secrets (3 phút)

Vào GitHub: **Repository → Settings → Secrets and variables → Actions → New repository secret**

Thêm 3 secrets sau:

| Secret Name   | Value                             |
| ------------- | --------------------------------- |
| `DO_SSH_KEY`  | Nội dung private key từ bước 2    |
| `DO_HOST`     | IP Droplet (vd: `134.209.100.50`) |
| `DO_USERNAME` | `root`                            |

**Tùy chọn:** Thêm `DISCORD_WEBHOOK` nếu muốn nhận thông báo Discord

---

## 📍 BƯỚC 4: Test Deploy thủ công (5 phút)

```bash
# SSH vào Droplet
cd /root/pdcms-be

# Build và start
docker-compose down
docker-compose build --no-cache
docker-compose up -d

# Đợi 30 giây, sau đó check
docker-compose ps
docker-compose logs -f app

# Test health check
curl http://localhost:8080/actuator/health
```

**Nếu thấy status UP** → Thành công! ✅

---

## 📍 BƯỚC 5: Deploy tự động với GitHub Actions (1 phút)

Trên máy local:

```bash
git add .
git commit -m "feat: setup deployment"
git push origin main
```

**→ Vào GitHub → Actions tab** để xem deployment đang chạy

Workflow sẽ tự động:

1. ✅ Backup version hiện tại
2. ✅ Pull code mới
3. ✅ Build Docker image
4. ✅ Deploy
5. ✅ Health check
6. ✅ Rollback nếu lỗi
7. ✅ Gửi thông báo Discord

---

## 🎯 CÁC LỆNH HỮU ÍCH

### Xem logs:

```bash
cd /root/pdcms-be
docker-compose logs -f app
```

### Restart services:

```bash
docker-compose restart app
```

### Xem trạng thái:

```bash
docker-compose ps
docker stats
```

### Rollback thủ công:

```bash
cd /root/pdcms-be
git log --oneline -10
git reset --hard <COMMIT_HASH>
docker-compose down
docker-compose build --no-cache app
docker-compose up -d
```

### Backup database:

```bash
docker-compose exec postgres pg_dump -U pdcms_user pdcms_db > backup.sql
```

---

## ⚠️ XỬ LÝ LỖI NHANH

### Lỗi: SSH Connection Failed

```bash
sudo systemctl restart ssh
sudo ufw allow 22/tcp
```

### Lỗi: Port already in use

```bash
docker-compose down
docker-compose up -d
```

### Lỗi: Database connection failed

```bash
docker-compose restart postgres
docker-compose logs postgres
```

### Lỗi: Out of memory

```bash
# Check memory
free -h

# Restart Docker
sudo systemctl restart docker
docker-compose restart
```

---

## 📊 KIỂM TRA SAU KHI DEPLOY

✅ Containers đang chạy:

```bash
docker-compose ps
```

✅ Health check:

```bash
curl http://localhost:8080/actuator/health
```

✅ Database OK:

```bash
docker-compose exec postgres psql -U pdcms_user -d pdcms_db -c "SELECT 1;"
```

✅ Redis OK:

```bash
docker-compose exec redis redis-cli -a YOUR_REDIS_PASSWORD ping
```

---

## 🎉 HOÀN TẤT!

Giờ mỗi khi push code lên `main` branch:

- GitHub Actions sẽ tự động deploy
- Có backup trước khi deploy
- Tự động rollback nếu lỗi
- Nhận thông báo qua Discord

**Chi tiết đầy đủ:** Xem file `DEPLOY_TO_DIGITALOCEAN_STEP_BY_STEP.md`

---

## 📞 CẦN TRỢ GIÚP?

1. Check logs: `docker-compose logs -f app`
2. Check GitHub Actions logs
3. Check Discord notifications
4. Manual rollback nếu cần

**Happy Deploying! 🚀**
