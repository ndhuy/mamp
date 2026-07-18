# -----------------------------------------------------------------------------
# start-dev.ps1 - bring up the full local dev stack for the
# Microstock Asset Management Platform.
#
#   Docker infra (Postgres, MinIO, Mailpit)  +  backend (:8080)  +  frontend (:5173)
#
# The backend and frontend each open in their OWN terminal window, so they keep
# running independently of whatever launched this script. Close those windows
# (or run stop-dev.ps1) to shut the app servers down.
#
# Usage:   .\start-dev.ps1
# -----------------------------------------------------------------------------
$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot

function Test-Port([int]$Port) {
    return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

Write-Host "== Microstock DAM - starting dev stack ==" -ForegroundColor Cyan

# 1. Container engine check ---------------------------------------------------
try { docker info *> $null } catch {}
if ($LASTEXITCODE -ne 0) {
    Write-Warning "Docker engine not reachable. Start Rancher Desktop, wait for it to be ready, then re-run."
    Write-Host   "  Start-Process 'C:\Program Files\Rancher Desktop\Rancher Desktop.exe'"
    exit 1
}

# 2. Infrastructure -----------------------------------------------------------
Write-Host "-> Starting Docker services (Postgres, MinIO, Mailpit)..." -ForegroundColor Cyan
docker compose -f "$root\docker-compose.yml" up -d | Out-Null

Write-Host "-> Waiting for PostgreSQL to accept connections..." -ForegroundColor Cyan
$ready = $false
for ($i = 0; $i -lt 30; $i++) {
    docker exec msamp-postgres pg_isready -U msamp -d msamp *> $null
    if ($LASTEXITCODE -eq 0) { $ready = $true; break }
    Start-Sleep -Seconds 2
}
if ($ready) { Write-Host "   PostgreSQL is ready." -ForegroundColor Green }
else { Write-Warning "PostgreSQL not ready yet; the backend will retry on startup." }

# 3. Backend (new window) -----------------------------------------------------
if (Test-Port 8080) {
    Write-Host "-> Backend already running on port 8080 - skipping." -ForegroundColor Yellow
} else {
    Write-Host "-> Launching backend (Spring Boot) on http://localhost:8080 ..." -ForegroundColor Cyan
    Start-Process powershell -WorkingDirectory "$root\backend" `
        -ArgumentList @('-NoExit', '-Command', "`$host.UI.RawUI.WindowTitle='MSAMP backend :8080'; .\mvnw.cmd spring-boot:run")
}

# 4. Frontend (new window) ----------------------------------------------------
if (Test-Port 5173) {
    Write-Host "-> Frontend already running on port 5173 - skipping." -ForegroundColor Yellow
} else {
    Write-Host "-> Launching frontend (Vite) on http://localhost:5173 ..." -ForegroundColor Cyan
    Start-Process powershell -WorkingDirectory "$root\frontend" `
        -ArgumentList @('-NoExit', '-Command', "`$host.UI.RawUI.WindowTitle='MSAMP frontend :5173'; npm run dev")
}

# 5. Summary ------------------------------------------------------------------
Write-Host ""
Write-Host "== Started. Give the servers ~15 seconds, then open: ==" -ForegroundColor Green
Write-Host "   App           http://localhost:5173   (admin / Admin123!)"
Write-Host "   API health    http://localhost:8080/actuator/health"
Write-Host "   MinIO console http://localhost:9001   (minioadmin / minioadmin)"
Write-Host "   Mailpit       http://localhost:8025"
Write-Host ""
Write-Host "   Two new terminal windows now run the servers. Run .\stop-dev.ps1 to stop them."
