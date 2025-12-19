# Test Appointment Creation and Notification System
# PowerShell Script

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "  TEST APPOINTMENT CREATION & NOTIFICATION SYSTEM" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$BASE_URL = "http://localhost:8080"
$USERNAME = "admin"
$PASSWORD = "123456"
$MAX_RETRIES = 10  # Try up to 10 different dates if conflicts

Write-Host "Using BASE URL: $BASE_URL" -ForegroundColor Green
Write-Host "Account: $USERNAME" -ForegroundColor Green
Write-Host "Max retries: $MAX_RETRIES" -ForegroundColor Green
Write-Host ""

# STEP 0: Login
Write-Host "STEP 0: Login with Admin account..." -ForegroundColor Cyan
Write-Host "----------------------------------------" -ForegroundColor Gray

$loginBody = @{
    username = $USERNAME
    password = $PASSWORD
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod `
        -Uri "$BASE_URL/api/v1/auth/login" `
        -Method POST `
        -Headers @{
            "Content-Type" = "application/json"
        } `
        -Body $loginBody `
        -ErrorAction Stop

    $TOKEN = $loginResponse.token
    
    Write-Host "Login successful!" -ForegroundColor Green
    Write-Host "Token (first 50 chars): $($TOKEN.Substring(0, 50))..." -ForegroundColor Gray
    Write-Host ""

} catch {
    Write-Host "ERROR: Cannot login!" -ForegroundColor Red
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response Body: $responseBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "Please check:" -ForegroundColor Yellow
    Write-Host "   - Is BE running?" -ForegroundColor Gray
    Write-Host "   - Is username/password correct? (admin/123456)" -ForegroundColor Gray
    exit 1
}

# STEP 1: Create Appointment with Auto-retry on conflicts
Write-Host "STEP 1: Creating Appointment..." -ForegroundColor Cyan
Write-Host "----------------------------------------" -ForegroundColor Gray

$appointmentCreated = $false
$appointmentCode = $null
$retryCount = 0
$baseDate = Get-Date "2026-01-10"

while (-not $appointmentCreated -and $retryCount -lt $MAX_RETRIES) {
    $testDate = $baseDate.AddDays($retryCount)
    $appointmentStartTime = $testDate.ToString("yyyy-MM-dd") + "T10:00:00"
    
    $appointmentBody = @{
        patientCode = "BN-1001"
        employeeCode = "EMP002"
        roomCode = "P-01"
        serviceCodes = @("OTHER_DIAMOND")
        appointmentStartTime = $appointmentStartTime
        notes = "Test notification - Retry $retryCount - Date: $appointmentStartTime"
        participantCodes = @()
    } | ConvertTo-Json

    Write-Host "Attempt $($retryCount + 1): Trying date $appointmentStartTime..." -ForegroundColor Yellow

    try {
        $appointmentResponse = Invoke-RestMethod `
            -Uri "$BASE_URL/api/v1/appointments" `
            -Method POST `
            -Headers @{
                "Content-Type" = "application/json"
                "Authorization" = "Bearer $TOKEN"
            } `
            -Body $appointmentBody `
            -ErrorAction Stop

        Write-Host "✓ Appointment created successfully!" -ForegroundColor Green
        $appointmentCode = $appointmentResponse.data.appointmentCode
        Write-Host "Appointment Code: $appointmentCode" -ForegroundColor Cyan
        $appointmentCreated = $true
        Write-Host ""

    } catch {
        $errorMsg = $_.Exception.Message
        
        # Check if it's a conflict error (400 or 409)
        if ($errorMsg -match "400" -or $errorMsg -match "409" -or $errorMsg -match "conflict") {
            Write-Host "✗ Conflict detected, trying next date..." -ForegroundColor Yellow
            $retryCount++
            Start-Sleep -Milliseconds 500
        } else {
            Write-Host "ERROR: Cannot create Appointment!" -ForegroundColor Red
            Write-Host "Error Message: $errorMsg" -ForegroundColor Red
            
            if ($_.Exception.Response) {
                try {
                    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                    $responseBody = $reader.ReadToEnd()
                    Write-Host "Response Body: $responseBody" -ForegroundColor Red
                } catch {
                    Write-Host "Could not read response body" -ForegroundColor Red
                }
            }
            
            exit 1
        }
    }
}

if (-not $appointmentCreated) {
    Write-Host "ERROR: Failed to create appointment after $MAX_RETRIES attempts!" -ForegroundColor Red
    exit 1
}

# STEP 2: Wait for BE to process notification
Write-Host "Waiting 3 seconds for system to create notification..." -ForegroundColor Gray
Start-Sleep -Seconds 3

# STEP 3: Login as Patient to check notifications
Write-Host "STEP 2: Checking Patient Notifications (BN-1001)..." -ForegroundColor Cyan
Write-Host "----------------------------------------" -ForegroundColor Gray

$patientLoginBody = @{
    username = "benhnhan1"
    password = "123456"
} | ConvertTo-Json

try {
    $patientLoginResponse = Invoke-RestMethod `
        -Uri "$BASE_URL/api/v1/auth/login" `
        -Method POST `
        -Headers @{
            "Content-Type" = "application/json"
        } `
        -Body $patientLoginBody `
        -ErrorAction Stop

    $PATIENT_TOKEN = $patientLoginResponse.token
    Write-Host "✓ Patient logged in" -ForegroundColor Green

    $notificationUrl = $BASE_URL + '/api/v1/notifications?page=0&size=20'
    $notificationResponse = Invoke-RestMethod `
        -Uri $notificationUrl `
        -Method GET `
        -Headers @{
            "Authorization" = "Bearer $PATIENT_TOKEN"
        } `
        -ErrorAction Stop

    Write-Host "✓ Got notification list" -ForegroundColor Green
    Write-Host ""

    # Display information
    $content = $notificationResponse.data.content
    $totalElements = $notificationResponse.data.totalElements

    Write-Host "Total notifications: $totalElements" -ForegroundColor Yellow
    Write-Host ""

    if ($content.Count -gt 0) {
        Write-Host "NOTIFICATION LIST (Top 5):" -ForegroundColor Cyan
        Write-Host "================================================================" -ForegroundColor Gray
        
        $topNotifications = $content | Select-Object -First 5
        
        foreach ($notification in $topNotifications) {
            Write-Host ""
            Write-Host "  ID: $($notification.notificationId)" -ForegroundColor White
            Write-Host "  Type: $($notification.type)" -ForegroundColor Yellow
            Write-Host "  Title: $($notification.title)" -ForegroundColor Green
            Write-Host "  Message: $($notification.message)" -ForegroundColor Gray
            Write-Host "  Related Entity: $($notification.relatedEntityType) - $($notification.relatedEntityId)" -ForegroundColor Magenta
            Write-Host "  Is Read: $($notification.isRead)" -ForegroundColor $(if ($notification.isRead) { "Gray" } else { "Red" })
            Write-Host "  Created At: $($notification.createdAt)" -ForegroundColor DarkGray
            Write-Host "  ----------------------------------------" -ForegroundColor DarkGray
        }

        # Check notifications related to newly created appointment
        Write-Host ""
        Write-Host "Searching for Notification related to Appointment: $appointmentCode" -ForegroundColor Cyan
        
        $relatedNotifications = $content | Where-Object { 
            $_.relatedEntityId -eq $appointmentCode -or 
            $_.message -like "*$appointmentCode*" 
        }

        if ($relatedNotifications.Count -gt 0) {
            Write-Host "Found $($relatedNotifications.Count) related notification(s)!" -ForegroundColor Green
            Write-Host ""
            
            foreach ($notification in $relatedNotifications) {
                $notifId = $notification.notificationId
                $notifType = $notification.type
                $notifTitle = $notification.title
                $notifMsg = $notification.message
                Write-Host "  -> ID: $notifId | Type: $notifType" -ForegroundColor White
                Write-Host "    Title: $notifTitle" -ForegroundColor Yellow
                Write-Host "    Message: $notifMsg" -ForegroundColor Gray
                Write-Host ""
            }
        } else {
            Write-Host "WARNING: No notification found related to this appointment!" -ForegroundColor Yellow
            Write-Host "    Possible reasons:" -ForegroundColor Gray
            Write-Host "    - Notification not yet created (check BE logs)" -ForegroundColor Gray
            Write-Host "    - Event listener not triggered" -ForegroundColor Gray
            Write-Host "    - Error in AppointmentCreationService" -ForegroundColor Gray
        }

    } else {
        Write-Host "WARNING: No notifications in the system!" -ForegroundColor Yellow
    }

} catch {
    Write-Host "ERROR: Cannot get notification list!" -ForegroundColor Red
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response Body: $responseBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
    }
}

# STEP 4: Check Unread Count
Write-Host ""
Write-Host "STEP 3: Checking unread notification count..." -ForegroundColor Cyan
Write-Host "----------------------------------------" -ForegroundColor Gray

try {
    $unreadCountResponse = Invoke-RestMethod `
        -Uri "$BASE_URL/api/v1/notifications/unread-count" `
        -Method GET `
        -Headers @{
            "Authorization" = "Bearer $TOKEN"
        } `
        -ErrorAction Stop

    # Unread count may be returned directly (number) or in data object
    $unreadCount = if ($unreadCountResponse -is [int] -or $unreadCountResponse -is [long]) { 
        $unreadCountResponse 
    } else { 
        $unreadCountResponse.data 
    }

    Write-Host "Unread notification count: $unreadCount" -ForegroundColor Green

} catch {
    Write-Host "WARNING: Cannot get unread count" -ForegroundColor Yellow
}

# END
Write-Host ""
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "  TEST COMPLETED" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "NOTES:" -ForegroundColor Yellow
Write-Host "  - Check BE logs for detailed notification creation process" -ForegroundColor Gray
Write-Host "  - Look for log markers: 🔔🔔🔔 and 🔥🔥🔥" -ForegroundColor Gray
Write-Host "  - WebSocket push notification to topic: /topic/notifications/USER_ID" -ForegroundColor Gray
Write-Host ""
