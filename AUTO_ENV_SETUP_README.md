# ⚡ AUTO ENV SETUP - JUST COPY & PASTE!

Tôi đã tự động config tất cả cho bạn! Bạn chỉ cần 1 lệnh để copy file lên Droplet.

---

## 🎯 SỬ DỤNG (CHỈ 1 LỆNH)

### Nếu dùng Windows:

```powershell
# Mở PowerShell trong thư mục dự án, chạy:
.\copy-env-to-droplet.ps1 -DropletIP YOUR_DROPLET_IP

# Ví dụ:
.\copy-env-to-droplet.ps1 -DropletIP 134.209.100.50
```

Script sẽ tự động:

- ✅ Copy file `.env.production` lên Droplet
- ✅ Đổi tên thành `.env`
- ✅ Backup file cũ (nếu có)
- ✅ Set permissions đúng (600)
- ✅ Generate passwords mạnh cho bạn luôn!

---

### Nếu dùng Linux/Mac:

```bash
# Cho phép script chạy (chỉ làm 1 lần)
chmod +x copy-env-to-droplet.sh

# Chạy script
./copy-env-to-droplet.sh YOUR_DROPLET_IP

# Ví dụ:
./copy-env-to-droplet.sh 134.209.100.50
```

---

## 📋 SAU KHI CHẠY SCRIPT

Script sẽ generate passwords mạnh cho bạn. Ví dụ output:

```
💡 TIP: Generated strong passwords for you:
----------------------------------------
DB_PASSWORD=aB3fG9kL2mN7pQ5rS8tU1vW4xY6zC0dE2fG5hJ8kL1mN4pQ7rS
REDIS_PASSWORD=xY1zA3bC5dE7fG9hJ2kL4mN6pQ8rS0tU3vW5xY7zA9bC1dE4fG
JWT_SECRET=mN2pQ4rS6tU8vW0xY2zA4bC6dE8fG0hJ2kL4mN6pQ8rS0tU2vW4xY6zA8bC0dE2fG4hJ6kL8mN0pQ2rS4tU6vW8xY0zA
----------------------------------------
Copy these values and paste them into your .env file on Droplet
```

### Chỉ cần làm 3 bước:

#### 1. SSH vào Droplet:

```bash
ssh root@YOUR_DROPLET_IP
cd /root/pdcms-be
nano .env
```

#### 2. Thay thế 4 giá trị:

- Tìm dòng `DB_PASSWORD=...` → Paste password mới
- Tìm dòng `REDIS_PASSWORD=...` → Paste password mới
- Tìm dòng `JWT_SECRET=...` → Paste secret mới
- Tìm dòng `FRONTEND_URL=...` → Thay bằng domain thật (vd: `https://pdcms.com`)

#### 3. Save và start:

```bash
# Save: Ctrl+X, Y, Enter

# Start services
docker-compose up -d

# Check logs
docker-compose logs -f app
```

---

## ✅ DONE!

Giờ bạn có thể:

1. ✅ Push code lên GitHub
2. ✅ GitHub Actions sẽ tự động deploy
3. ✅ Nhận thông báo Discord (nếu setup)

---

## 📁 FILES ĐÃ TẠO

| File                      | Mô tả                                    |
| ------------------------- | ---------------------------------------- |
| `.env`                    | File local với config đầy đủ và comments |
| `.env.production`         | Template sẵn sàng copy lên Droplet       |
| `copy-env-to-droplet.ps1` | Script tự động cho Windows               |
| `copy-env-to-droplet.sh`  | Script tự động cho Linux/Mac             |
| `ENV_SETUP_GUIDE.md`      | Hướng dẫn chi tiết setup .env            |

---

## 🔒 BẢO MẬT

- ✅ File `.env` và `.env.production` đã được thêm vào `.gitignore`
- ✅ KHÔNG BAO GIỜ commit passwords vào Git
- ✅ Script tự động set permissions 600 cho file `.env`
- ✅ Passwords được generate random và mạnh

---

## ⚠️ LƯU Ý

### Email đã config sẵn:

```
MAIL_USERNAME=hellodenteeth@gmail.com
MAIL_PASSWORD=micnxeutitfjrmxk
```

Nếu muốn đổi email, update trong file `.env.production` trước khi copy lên Droplet.

### Timezone đã set:

```
TZ=Asia/Ho_Chi_Minh
```

---

## 🎉 TẤT CẢ ĐÃ XONG!

Bạn chỉ cần:

1. Chạy script copy (1 lệnh)
2. SSH vào Droplet (1 lệnh)
3. Update 4 values (copy/paste passwords đã generate)
4. Save và start (1 lệnh)

**TOTAL: 4 bước, 5 phút! 🚀**

---

## 📞 CẦN TRỢ GIÚP?

Xem các file guide khác:

- `ENV_SETUP_GUIDE.md` - Chi tiết setup .env
- `QUICK_DEPLOY_GUIDE.md` - Quick start guide
- `DEPLOYMENT_CHECKLIST.md` - Checklist đầy đủ
- `docs/DEPLOY_TO_DIGITALOCEAN_STEP_BY_STEP.md` - Guide chi tiết từng bước
