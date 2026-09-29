# Starts a local Postgres (if not already running) and this backend, both in
# parallel with whatever else you're running (frontend dev server, Modbus
# gateway, etc.) - for local development only, not the client-premises
# deployment path (see README.md for that: java -jar / Docker).
#
# Windows equivalent of start-dev.sh. Requires Docker Desktop (with its
# PowerShell/CLI integration - the default) and a JDK on PATH for
# gradlew.bat. Run from PowerShell:
#   .\start-dev.ps1
# If PowerShell blocks it with an execution-policy error, run once:
#   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

$ErrorActionPreference = "Stop"

Set-Location -Path $PSScriptRoot

$ContainerName = "tms-backend-postgres"
$DbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "5434" }

$running = docker ps --filter "name=$ContainerName" --filter "status=running" -q
if (-not $running) {
    $existing = docker ps -a --filter "name=$ContainerName" -q
    if ($existing) {
        Write-Host "Starting existing $ContainerName container..."
        docker start $ContainerName | Out-Null
    } else {
        Write-Host "Creating $ContainerName container on port $DbPort..."
        docker run -d --name $ContainerName `
            -e POSTGRES_USER=tms -e POSTGRES_PASSWORD=tms -e POSTGRES_DB=tms `
            -p "${DbPort}:5432" postgres:16 | Out-Null
    }
}

Write-Host "Waiting for Postgres to accept connections..."
while ($true) {
    docker exec $ContainerName pg_isready -U tms *> $null
    if ($LASTEXITCODE -eq 0) { break }
    Start-Sleep -Seconds 1
}
Write-Host "Postgres ready on port $DbPort."

$env:DB_URL = "jdbc:postgresql://localhost:$DbPort/tms"
$env:DB_USERNAME = "tms"
$env:DB_PASSWORD = "tms"

$serverPort = if ($env:SERVER_PORT) { $env:SERVER_PORT } else { "8080" }
Write-Host "Starting tms-backend (Spring Boot) on SERVER_PORT=$serverPort..."
& .\gradlew.bat bootRun --console=plain
