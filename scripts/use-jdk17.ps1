param(
    [string]$JdkPath = 'C:\Program Files\Java\jdk-17',
    [switch]$SetPermanent,
    [string[]]$MvnArgs
)

Write-Host "[use-jdk17] Requested JDK path: $JdkPath"

if (-not (Test-Path $JdkPath)) {
    Write-Error "JDK path '$JdkPath' not found. Please install JDK 17 or provide -JdkPath pointing to your JDK 17 installation."
    exit 1
}

# Set for current session
$env:JAVA_HOME = $JdkPath
$env:PATH = "$JdkPath\bin;$($env:PATH)"

if ($SetPermanent) {
    Write-Host "Setting JAVA_HOME persistently (setx). Note: open a new shell to see persistent changes."
    setx JAVA_HOME "$JdkPath" | Out-Null
}

Write-Host "Using JAVA_HOME = $env:JAVA_HOME"
Write-Host "java -version:`n"
java -version
Write-Host "`njavac -version:`n"
javac -version

# Default mvn args if not provided
if (-not $MvnArgs -or $MvnArgs.Count -eq 0) {
    $MvnArgs = @('-DskipTests','clean','package')
}

$argsLine = $MvnArgs -join ' '
Write-Host "`nRunning: .\mvnw.cmd $argsLine`n"
.
\mvnw.cmd $MvnArgs
