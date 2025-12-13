# 🚀 PDCMS DEPLOYMENT - ONE PAGE SUMMARY

## ⚡ CHẠY 1 LỆNH - XONG NGAY!

### Windows (PowerShell):

```powershell
.\copy-env-to-droplet.ps1 -DropletIP YOUR_DROPLET_IP
```

### Linux/Mac:

```bash
chmod +x copy-env-to-droplet.sh && ./copy-env-to-droplet.sh YOUR_DROPLET_IP
```

Script tự động: Copy `.env`, backup cũ, generate passwords mạnh!

---

## 📝 SAU KHI CHẠY SCRIPT (3 PHÚT)

### 1. SSH & Edit:

```bash
ssh root@YOUR_DROPLET_IP
cd /root/pdcms-be
nano .env
```

### 2. Thay 4 giá trị (Script đã generate sẵn):

- `DB_PASSWORD=` → Paste password mới
- `REDIS_PASSWORD=` → Paste password mới
- `JWT_SECRET=` → Paste secret mới
- `FRONTEND_URL=` → `https://your-domain.com`

### 3. Save & Start:

```bash
docker-compose up -d && docker-compose logs -f app
```

---

## 🔐 GITHUB SECRETS (2 PHÚT)

**Repo → Settings → Secrets → Actions → New secret**

| Secret            | Value       | Làm sao lấy?                           |
| ----------------- | ----------- | -------------------------------------- |
| `DO_SSH_KEY`      | Private key | `cat ~/.ssh/id_rsa` (trên Droplet)     |
| `DO_HOST`         | IP Droplet  | `134.209.100.50`                       |
| `DO_USERNAME`     | `root`      | Username SSH                           |
| `DISCORD_WEBHOOK` | URL         | Discord Server Settings → Integrations |

---

## ✅ VERIFY (1 PHÚT)

```bash
# Check containers
docker-compose ps

# Check health
curl http://localhost:8080/actuator/health

# Check DB
docker-compose exec postgres psql -U pdcms_user -d pdcms_db -c "SELECT 1;"

# Check Redis
docker-compose exec redis redis-cli -a YOUR_PASSWORD ping
```

---

## 🎯 AUTO DEPLOY

```bash
# Push code → GitHub Actions tự động deploy
git push origin main
```

**GitHub → Actions → Xem workflow chạy!**

---

## 🛠️ TROUBLESHOOTING

| Lỗi                 | Fix                                           |
| ------------------- | --------------------------------------------- |
| SSH failed          | `sudo systemctl restart ssh`                  |
| Port in use         | `docker-compose down && docker-compose up -d` |
| DB failed           | `docker-compose restart postgres`             |
| Health check failed | `docker-compose logs app`                     |

---

## 📂 FILES TẠO SẴN

✅ `.env` - Local config với hướng dẫn
✅ `.env.production` - Template production
✅ `copy-env-to-droplet.ps1` - Auto script Windows
✅ `copy-env-to-droplet.sh` - Auto script Linux/Mac
✅ `.github/workflows/deploy.yml` - GitHub Actions
✅ `AUTO_ENV_SETUP_README.md` - Hướng dẫn chi tiết
✅ `ENV_SETUP_GUIDE.md` - Guide setup .env
✅ `QUICK_DEPLOY_GUIDE.md` - Quick start
✅ `DEPLOYMENT_CHECKLIST.md` - Checklist đầy đủ

---

## 🎉 WORKFLOW

```
1. Chạy script copy .env (1 lệnh) → 1 phút
2. SSH & update 4 values → 2 phút
3. docker-compose up -d → 1 phút
4. Add GitHub Secrets → 2 phút
5. Push code → Auto deploy! → 3 phút
───────────────────────────────────
TOTAL: 10 PHÚT - PRODUCTION READY! 🚀
```

---

## 🔗 WORKFLOW AUTO DEPLOY

```
Push to main
    ↓
GitHub Actions
    ↓
1. Backup current version
2. Pull latest code
3. Build Docker
4. Deploy
5. Health check
    ↓
✅ Success → Discord notification
❌ Failed → Auto rollback → Discord notification
```

---

## 📞 DOCS

- **Chi tiết**: `docs/DEPLOY_TO_DIGITALOCEAN_STEP_BY_STEP.md`
- **Quick**: `QUICK_DEPLOY_GUIDE.md`
- **Checklist**: `DEPLOYMENT_CHECKLIST.md`
- **Auto ENV**: `AUTO_ENV_SETUP_README.md`

---

**BẠN CHỈ CẦN CHẠY 1 LỆNH VÀ ĐIỀN 4 GIÁ TRỊ!** 🎯

**TỔNG THỜI GIAN: 10 PHÚT!** ⚡
