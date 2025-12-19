Write-Host "`n=== TESTING APPOINTMENT + NOTIFICATION ===`n" -ForegroundColor Cyan

# Login Admin
Write-Host "[1] Login Admin..." -ForegroundColor Yellow
$body1 = '{"username":"admin","password":"123456"}'
$r1 = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method POST -ContentType 'application/json' -Body $body1
$adminToken = $r1.token
Write-Host "    OK" -ForegroundColor Green

# Create Appointment - 2026-01-07 Tuesday (BS Thai HAS shift - confirmed in seed data)
Write-Host "[2] Create Appointment..." -ForegroundColor Yellow
$testDate = "2026-01-07"
$body2 = "{`"patientCode`":`"BN-1001`",`"employeeCode`":`"EMP002`",`"roomCode`":`"P-01`",`"serviceCodes`":[`"OTHER_DIAMOND`"],`"appointmentStartTime`":`"${testDate}T09:00:00`",`"notes`":`"Test notification`",`"participantCodes`":[]}"
$headers2 = @{
    'Authorization' = "Bearer $adminToken"
    'Content-Type' = 'application/json'
}
try {
    $r2 = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/appointments' -Method POST -Headers $headers2 -Body $body2 -ErrorAction Stop
    $appointmentCode = $r2.appointmentCode
    Write-Host "    Appointment: $appointmentCode (Date: $testDate)" -ForegroundColor Green
} catch {
    Write-Host "    ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "    Trying afternoon 14:00..." -ForegroundColor Yellow
    $body2 = "{`"patientCode`":`"BN-1001`",`"employeeCode`":`"EMP002`",`"roomCode`":`"P-01`",`"serviceCodes`":[`"OTHER_DIAMOND`"],`"appointmentStartTime`":`"2026-01-08T14:00:00`",`"notes`":`"Test retry`",`"participantCodes`":[]}"
    $r2 = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/appointments' -Method POST -Headers $headers2 -Body $body2
    $appointmentCode = $r2.appointmentCode
    Write-Host "    Appointment: $appointmentCode" -ForegroundColor Green
}

# Wait
Write-Host "[3] Waiting 3 seconds..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# Login Patient
Write-Host "[4] Login Patient..." -ForegroundColor Yellow
$body3 = '{"username":"benhnhan1","password":"123456"}'
$r3 = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method POST -ContentType 'application/json' -Body $body3
$patientToken = $r3.token
Write-Host "    OK" -ForegroundColor Green

# Check Notifications
Write-Host "[5] Check Notifications..." -ForegroundColor Yellow
$headers5 = @{ 'Authorization' = "Bearer $patientToken" }
$r5 = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/notifications?page=0&size=20' -Method GET -Headers $headers5
$total = $r5.data.totalElements

Write-Host "`n=== RESULT ===" -ForegroundColor Cyan
Write-Host "Appointment: $appointmentCode"
Write-Host "Notifications: $total"

if ($total -gt 0) {
    Write-Host "`nSUCCESS!" -ForegroundColor Green
    $r5.data.content | Select-Object -First 3 | ForEach-Object {
        Write-Host "  - ID: $($_.notificationId) | Type: $($_.type) | Title: $($_.title)"
    }
} else {
    Write-Host "`nFAILED - No notifications!" -ForegroundColor Red
}
