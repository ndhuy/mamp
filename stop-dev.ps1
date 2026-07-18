# -----------------------------------------------------------------------------
# stop-dev.ps1 - stop the local dev app servers (backend :8080, frontend :5173).
#
# By default this stops only the app servers and leaves the Docker
# infrastructure (Postgres/MinIO/Mailpit) running, so your data stays available.
#
# Usage:
#   .\stop-dev.ps1                 # stop backend + frontend only
#   .\stop-dev.ps1 -IncludeDocker  # also stop the Docker services
# -----------------------------------------------------------------------------
param([switch]$IncludeDocker)
$root = $PSScriptRoot

function Stop-Port([int]$Port, [string]$Name) {
    $conns = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $conns) { Write-Host "$Name - nothing listening on port $Port"; return }
    $conns | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object {
        try {
            $proc = Get-Process -Id $_ -ErrorAction SilentlyContinue
            Stop-Process -Id $_ -Force -ErrorAction Stop
            Write-Host "Stopped $Name - $($proc.ProcessName) (PID $_) on port $Port" -ForegroundColor Green
        } catch {
            Write-Warning ("Could not stop PID {0} on port {1}: {2}" -f $_, $Port, $_.Exception.Message)
        }
    }
}

Write-Host "== Stopping Microstock DAM dev servers ==" -ForegroundColor Cyan
Stop-Port 8080 "backend"
Stop-Port 5173 "frontend"

if ($IncludeDocker) {
    Write-Host "-> Stopping Docker services..." -ForegroundColor Cyan
    docker compose -f "$root\docker-compose.yml" stop | Out-Null
    Write-Host "   Docker services stopped (data volumes preserved)." -ForegroundColor Green
} else {
    Write-Host "Docker infrastructure left running (use -IncludeDocker to stop it too)."
}
