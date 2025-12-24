# Simple Test: Appointment Creation + Notification Check
# PowerShell Script

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  APPOINTMENT + NOTIFICATION TEST" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$BASE_URL = "http://localhost:8080"
$MAX_RETRIES = 10

# STEP 1: Login as Admin
Write-Host "[1/4] Login as Admin..." -ForegroundColor Yellow
$loginBody = '{"username":"admin","password":"123456"}'
try {
    $loginResponse = Invoke-RestMethod -Uri "$BASE_URL/api/v1/auth/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body $loginBody -ErrorAction Stop
    $adminToken = $loginResponse.token
    Write-Host "  ✓ Admin logged in" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# STEP 2: Create Appointment (auto-retry if conflict)
Write-Host "`n[2/4] Creating Appointment..." -ForegroundColor Yellow
$appointmentCreated = $false
$appointmentCode = $null
$retryCount = 0
$baseDate = Get-Date "2026-01-10"

while (-not $appointmentCreated -and $retryCount -lt $MAX_RETRIES) {
    $testDate = $baseDate.AddDays($retryCount)
    $appointmentStartTime = $testDate.ToString("yyyy-MM-dd") + "T10:00:00"
    
    $appointmentBody = @"
{"patientCode":"BN-1001","employeeCode":"EMP002","roomCode":"P-01","serviceCodes":["OTHER_DIAMOND"],"appointmentStartTime":"$appointmentStartTime","notes":"Test notification","participantCodes":[]}
"@

    Write-Host "  Attempt $($retryCount + 1): $appointmentStartTime..." -ForegroundColor Gray

    try {
        $appointmentResponse = Invoke-RestMethod -Uri "$BASE_URL/api/v1/appointments" -Method POST -Headers @{"Authorization"="Bearer $adminToken";"Content-Type"="application/json"} -Body $appointmentBody -ErrorAction Stop
        $appointmentCode = $appointmentResponse.data.appointmentCode
        Write-Host "  ✓ Appointment created: $appointmentCode" -ForegroundColor Green
        $appointmentCreated = $true
    } catch {
        $errorMsg = $_.Exception.Message
        if ($errorMsg -match "400" -or $errorMsg -match "conflict") {
            Write-Host "  ✗ Conflict, trying next date..." -ForegroundColor Yellow
            $retryCount++
            Start-Sleep -Milliseconds 300
        } else {
            Write-Host "  ✗ Error: $errorMsg" -ForegroundColor Red
            exit 1
        }
    }
}

if (-not $appointmentCreated) {
    Write-Host "  ✗ Failed after $MAX_RETRIES attempts" -ForegroundColor Red
    exit 1
}

# STEP 3: Wait for notification processing
Write-Host "`n[3/4] Waiting for notification processing..." -ForegroundColor Yellow
Start-Sleep -Seconds 3
Write-Host "  ✓ Wait complete" -ForegroundColor Green

# STEP 4: Login as Patient and check notifications
Write-Host "`n[4/4] Checking Patient Notifications..." -ForegroundColor Yellow
$patientLoginBody = '{"username":"benhnhan1","password":"123456"}'
try {
    $patientLoginResponse = Invoke-RestMethod -Uri "$BASE_URL/api/v1/auth/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body $patientLoginBody -ErrorAction Stop
    $patientToken = $patientLoginResponse.token
    Write-Host "  ✓ Patient logged in (BN-1001)" -ForegroundColor Green
    
    $notificationResponse = Invoke-RestMethod -Uri "$BASE_URL/api/v1/notifications?page=0&size=20" -Method GET -Headers @{"Authorization"="Bearer $patientToken"} -ErrorAction Stop
    $totalNotifications = $notificationResponse.data.totalElements
    $notifications = $notificationResponse.data.content
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  RESULT" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Appointment Code: $appointmentCode" -ForegroundColor White
    Write-Host "Total Notifications: $totalNotifications" -ForegroundColor White
    
    if ($totalNotifications -gt 0) {
        Write-Host "`n✓✓✓ SUCCESS! Notifications found:" -ForegroundColor Green
        $notifications | Select-Object -First 3 | ForEach-Object {
            Write-Host "`n  ID: $($_.notificationId)" -ForegroundColor Cyan
            Write-Host "  Type: $($_.type)" -ForegroundColor White
            Write-Host "  Title: $($_.title)" -ForegroundColor White
            Write-Host "  Message: $($_.message)" -ForegroundColor Gray
            Write-Host "  Related: $($_.relatedEntityId)" -ForegroundColor Gray
        }
    } else {
        Write-Host "`n✗✗✗ FAILED: No notifications found!" -ForegroundColor Red
        Write-Host "`nCheck BE logs for:" -ForegroundColor Yellow
        Write-Host "  - 🔔🔔🔔 CALLING sendAppointmentCreatedNotification" -ForegroundColor Gray
        Write-Host "  - 🔥🔥🔥 NotificationService.createNotification() CALLED" -ForegroundColor Gray
        Write-Host "  - 🔥✓ Notification SAVED to DB with ID" -ForegroundColor Gray
    }
    
    Write-Host "`n========================================`n" -ForegroundColor Cyan
    
} catch {
    Write-Host "  ✗ Error checking notifications: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
