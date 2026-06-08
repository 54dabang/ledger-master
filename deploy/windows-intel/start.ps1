$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

& (Join-Path $ScriptDir "load-images.ps1")

docker compose --env-file .env -f docker-compose.yml up -d
docker compose --env-file .env -f docker-compose.yml ps

$envValues = @{}
Get-Content (Join-Path $ScriptDir ".env") | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        $envValues[$matches[1].Trim()] = $matches[2].Trim()
    }
}

$webPort = $envValues["WEB_PORT"]
Write-Host ""
Write-Host "Ledger is starting. Open http://localhost:$webPort after the app container is healthy."
