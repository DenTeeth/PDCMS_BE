$token = (Get-Content "login_response.json" | ConvertFrom-Json).token
$headers = @{Authorization="Bearer $token"}
$patient = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/patients/BN-1001" -Headers $headers
Write-Host "Patient Detail API Test"
Write-Host "Code: $($patient.patientCode)"
Write-Host "Name: $($patient.fullName)"  
Write-Host "isBookingBlocked: $($patient.isBookingBlocked)"
Write-Host "consecutiveNoShows: $($patient.consecutiveNoShows)"
