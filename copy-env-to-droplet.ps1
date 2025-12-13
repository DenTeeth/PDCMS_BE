# ==============================================================================
# SCRIPT: Copy .env file to DigitalOcean Droplet (PowerShell)
# ==============================================================================
# Usage: .\copy-env-to-droplet.ps1 -DropletIP YOUR_DROPLET_IP
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
Write-Host "   PDCMS Backend - Copy .env to Droplet" -ForegroundColor $Blue
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
Write-Host "📤 Copying .env.production to Droplet..." -ForegroundColor $Blue
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
ssh "$Username@$DropletIP" "cd $DeployPath && head -n 15 .env"
Write-Host "----------------------------------------" -ForegroundColor $Yellow

Write-Host ""
Write-Host "🎉 Done! .env file is ready on Droplet" -ForegroundColor $Green
Write-Host ""
Write-Host "⚠️  IMPORTANT NEXT STEPS:" -ForegroundColor $Yellow
Write-Host "   1. SSH into Droplet: " -NoNewline
Write-Host "ssh $Username@$DropletIP" -ForegroundColor $Blue
Write-Host "   2. Edit .env: " -NoNewline
Write-Host "cd $DeployPath && nano .env" -ForegroundColor $Blue
Write-Host "   3. Update these values:" -ForegroundColor $Blue
Write-Host "      - DB_PASSWORD (generate: openssl rand -base64 32)"
Write-Host "      - REDIS_PASSWORD (generate: openssl rand -base64 32)"
Write-Host "      - JWT_SECRET (generate: openssl rand -base64 64)"
Write-Host "      - FRONTEND_URL (your actual domain)"
Write-Host "   4. Save and test: " -NoNewline
Write-Host "docker-compose up -d" -ForegroundColor $Blue
Write-Host ""
Write-Host "===================================================================" -ForegroundColor $Blue

# Generate strong passwords locally for reference
Write-Host ""
Write-Host "💡 TIP: Generated strong passwords for you:" -ForegroundColor $Yellow
Write-Host "----------------------------------------" -ForegroundColor $Yellow

# Generate random passwords using .NET
$dbPassword = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
$redisPassword = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
$jwtSecret = [Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))

Write-Host "DB_PASSWORD=" -NoNewline
Write-Host $dbPassword -ForegroundColor $Green
Write-Host "REDIS_PASSWORD=" -NoNewline
Write-Host $redisPassword -ForegroundColor $Green
Write-Host "JWT_SECRET=" -NoNewline
Write-Host $jwtSecret -ForegroundColor $Green
Write-Host "----------------------------------------" -ForegroundColor $Yellow
Write-Host "Copy these values and paste them into your .env file on Droplet" -ForegroundColor $Yellow
Write-Host ""
