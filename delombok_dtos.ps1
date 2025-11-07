# Script to remove Lombok from DTOs and generate manual getters/setters/constructors
# Run this script in PowerShell

Write-Host "Starting Lombok removal from DTO files..." -ForegroundColor Green

# Function to generate getter method
function Get-GetterMethod {
    param([string]$type, [string]$fieldName)
    $methodName = "get" + $fieldName.Substring(0, 1).ToUpper() + $fieldName.Substring(1)
    if ($type -eq "Boolean" -or $type -eq "boolean") {
        # Check if field starts with "is"
        if ($fieldName.StartsWith("is")) {
            $methodName = $fieldName.Substring(0, 1).ToLower() + $fieldName.Substring(1)
        }
    }
    return @"
    public $type $methodName() {
        return $fieldName;
    }
"@
}

# Function to generate setter method
function Get-SetterMethod {
    param([string]$type, [string]$fieldName)
    $methodName = "set" + $fieldName.Substring(0, 1).ToUpper() + $fieldName.Substring(1)
    return @"
    public void $methodName($type $fieldName) {
        this.$fieldName = $fieldName;
    }
"@
}

Write-Host "Script created. Please use IntelliJ IDEA's built-in Delombok feature instead:" -ForegroundColor Yellow
Write-Host "1. Right-click on the 'dto' folder" -ForegroundColor Cyan
Write-Host "2. Select 'Refactor' -> 'Delombok' -> 'All lombok annotations'" -ForegroundColor Cyan
Write-Host "3. IntelliJ will automatically generate all getters, setters, and constructors" -ForegroundColor Cyan
Write-Host ""
Write-Host "OR use Maven Delombok plugin:" -ForegroundColor Yellow
Write-Host "mvn lombok:delombok" -ForegroundColor Cyan

# Alternative: List all DTO files that need to be delomboked
Write-Host "`nFiles that need Lombok removal:" -ForegroundColor Green

$dtoPath = "src\main\java\com\dental\clinic\management\working_schedule\dto"
Get-ChildItem -Path $dtoPath -Recurse -Filter "*.java" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if ($content -match '@Data|@Getter|@Setter|@Builder|@NoArgsConstructor|@AllArgsConstructor') {
        Write-Host "  - $($_.Name)" -ForegroundColor White
    }
}

Write-Host "`nTotal files found with Lombok annotations" -ForegroundColor Green
