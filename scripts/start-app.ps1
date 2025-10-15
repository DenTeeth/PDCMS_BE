# Start helper: frees port 8080 (if needed) and starts the Spring Boot app
# Usage: .\scripts\start-app.ps1 [-Kill] [-Port 8080] [-JarPath <path-to-jar>] [-Args "--spring-arg=val"]
param(
    [switch]$Kill,
    [int]$Port = 8080,
    [string]$JarPath = "",
    [string]$Args = ""
)

function Get-ListenerPid($port) {
    $line = netstat -ano | findstr ":$port" | Select-String -Pattern "LISTENING" -SimpleMatch | Select-Object -First 1
    if ($line) {
        $parts = $line.ToString().Trim() -split '\s+'; return [int]$parts[-1]
    }
    return $null
}

$pid = Get-ListenerPid $Port
if ($pid) {
    Write-Host "Port $Port is in use by PID $pid"
    if ($Kill.IsPresent) {
        Write-Host "Killing PID $pid..."
        taskkill /PID $pid /F | Out-Null
        Start-Sleep -Milliseconds 400
        Write-Host "Killed PID $pid"
    } else {
        Write-Host "Run with -Kill to force-free the port, or choose a different server.port in application.yaml"
        exit 1
    }
} else {
    Write-Host "Port $Port is free"
}

# Start the app
if ($JarPath -ne "") {
    Write-Host "Starting jar: $JarPath $Args"
    & 'C:\Program Files\Java\jdk-17\bin\java.exe' -jar $JarPath $Args
} else {
    Write-Host "Starting from project classes (mvn exec or java -cp)"
    # Run the same command used earlier (uses the temporary argfile created by IDE). Adjust if needed.
    & 'C:\Program Files\Java\jdk-17\bin\java.exe' '@C:\Users\ADMiN_KN\AppData\Local\Temp\cp_en0s08s9tjuwwjfpi9ag1l6k3.argfile' 'com.dental.clinic.management.DentalClinicManagementApplication'
}
