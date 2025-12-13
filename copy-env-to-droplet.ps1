# ==============================================================================
# SCRIPT: Copy .env to DigitalOcean Droplet (AUTO-CONFIGURED)
# ==============================================================================
# Usage: .\copy-env-to-droplet.ps1 -DropletIP YOUR_DROPLET_IP
#
# This script copies the AUTO-CONFIGURED .env.production file to your Droplet.
# NO MANUAL EDITING NEEDED - all values are from your project!
# ==============================================================================

param(
    [Parameter(Mandatory=$true, HelpMessage="Droplet IP address")]
    [string]$DropletIP,

    [Parameter(Mandatory=$false)]
    [string]$Username = "root",

    [Parameter(Mandatory=$false)]
    [string]$DeployPath = "/root/pdcms-be"
)

# Colors
$Blue = "Cyan"
$Green = "Green"
$Yellow = "Yellow"
$Red = "Red"

Write-Host "===================================================================" -ForegroundColor $Blue
Write-Host "   PDCMS Backend - Auto-Configured .env Deployment" -ForegroundColor $Blue
Write-Host "===================================================================" -ForegroundColor $Blue
Write-Host ""

Write-Host "📋 Configuration:" -ForegroundColor $Yellow
Write-Host "   Droplet IP: " -NoNewline
Write-Host $DropletIP -ForegroundColor $Green
Write-Host "   Username: " -NoNewline
Write-Host $Username -ForegroundColor $Green
Write-Host "   Deploy Path: " -NoNewline
Write-Host $DeployPath -ForegroundColor $Green
Write-Host ""

# Check if .env.production exists
if (-not (Test-Path ".env.production")) {
    Write-Host "❌ Error: .env.production file not found" -ForegroundColor $Red
    Write-Host "Please make sure .env.production exists in the current directory" -ForegroundColor $Yellow
    exit 1
}

Write-Host "🔍 Checking SSH connection..." -ForegroundColor $Blue
try {
    $sshTest = ssh -o ConnectTimeout=5 "$Username@$DropletIP" "echo 'SSH OK'"
    if ($sshTest -eq "SSH OK") {
        Write-Host "✅ SSH connection successful" -ForegroundColor $Green
    }
} catch {
    Write-Host "❌ Error: Cannot connect to Droplet" -ForegroundColor $Red
    Write-Host "Please check:" -ForegroundColor $Yellow
    Write-Host "   1. Droplet IP address is correct"
    Write-Host "   2. SSH key is configured"
    Write-Host "   3. Firewall allows SSH (port 22)"
    exit 1
}

Write-Host ""
Write-Host "📁 Creating backup of existing .env..." -ForegroundColor $Blue
$backupCmd = "cd $DeployPath && [ -f .env ] && cp .env .env.backup.`$(date +%Y%m%d_%H%M%S) || echo 'No existing .env file'"
ssh "$Username@$DropletIP" $backupCmd

Write-Host ""
Write-Host "📤 Copying auto-configured .env.production to Droplet..." -ForegroundColor $Blue
scp ".env.production" "${Username}@${DropletIP}:${DeployPath}/.env"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ .env file copied successfully!" -ForegroundColor $Green
} else {
    Write-Host "❌ Error: Failed to copy .env file" -ForegroundColor $Red
    exit 1
}

Write-Host ""
Write-Host "🔒 Setting correct permissions..." -ForegroundColor $Blue
ssh "$Username@$DropletIP" "cd $DeployPath && chmod 600 .env && chown ${Username}:${Username} .env"

Write-Host ""
Write-Host "✅ Verifying .env file on Droplet..." -ForegroundColor $Blue
Write-Host "----------------------------------------" -ForegroundColor $Yellow
ssh "$Username@$DropletIP" "cd $DeployPath && head -n 20 .env"
Write-Host "----------------------------------------" -ForegroundColor $Yellow

Write-Host ""
Write-Host "===================================================================" -ForegroundColor $Blue
Write-Host "✅ HOÀN THÀNH! .env đã sẵn sàng trên Droplet!" -ForegroundColor $Green
Write-Host "===================================================================" -ForegroundColor $Blue
Write-Host ""
Write-Host "📋 FILE .ENV ĐÃ ĐƯỢC AUTO-CONFIGURED:" -ForegroundColor $Yellow
Write-Host "   ✅ Database: root / 123456 / dental_clinic_db" -ForegroundColor $White
Write-Host "   ✅ Redis: redis123" -ForegroundColor $White
Write-Host "   ✅ JWT Secret: (từ SecurityConfig)" -ForegroundColor $White
Write-Host "   ✅ Email: hellodenteeth@gmail.com" -ForegroundColor $White
Write-Host "   ✅ Frontend: http://localhost:3000" -ForegroundColor $White
Write-Host ""
Write-Host "🚀 KHÔNG CẦN EDIT GÌ CẢ! Chỉ cần start Docker:" -ForegroundColor $Green
Write-Host ""
Write-Host "   ssh $Username@$DropletIP" -ForegroundColor $Blue
Write-Host "   cd $DeployPath" -ForegroundColor $Blue
Write-Host "   docker-compose up -d" -ForegroundColor $Blue
Write-Host ""
Write-Host "⏳ Đợi 30-60 giây, sau đó check:" -ForegroundColor $Yellow
Write-Host "   docker-compose ps" -ForegroundColor $Blue
Write-Host "   docker-compose logs -f app" -ForegroundColor $Blue
Write-Host "   curl http://localhost:8080/actuator/health" -ForegroundColor $Blue
Write-Host ""
Write-Host "🎯 Sau đó push code để trigger GitHub Actions deployment!" -ForegroundColor $Yellow
Write-Host "   git add ." -ForegroundColor $Blue
Write-Host "   git commit -m " -NoNewline -ForegroundColor $Blue
Write-Host '"feat: production ready"' -ForegroundColor $Blue
Write-Host "   git push origin main" -ForegroundColor $Blue
Write-Host ""
Write-Host "===================================================================" -ForegroundColor $Blue
Write-Host ""
