$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ImageTar = Join-Path $ScriptDir "ledger-images-linux-amd64.tar"

if (!(Test-Path $ImageTar)) {
    throw "Cannot find $ImageTar"
}

$envValues = @{}
Get-Content (Join-Path $ScriptDir ".env") | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        $envValues[$matches[1].Trim()] = $matches[2].Trim()
    }
}

$tag = $envValues["LEDGER_IMAGE_TAG"]
if ([string]::IsNullOrWhiteSpace($tag)) {
    throw "LEDGER_IMAGE_TAG is not set in .env"
}

Write-Host "Loading Docker images from $ImageTar ..."
docker load -i $ImageTar

Write-Host ""
Write-Host "Loaded image platforms:"
docker image inspect "ledger-app:$tag" --format "ledger-app: {{.Os}}/{{.Architecture}}"
docker image inspect "ledger-mysql:$tag" --format "ledger-mysql: {{.Os}}/{{.Architecture}}"
docker image inspect "ledger-nginx:$tag" --format "ledger-nginx: {{.Os}}/{{.Architecture}}"
docker image inspect redis:7.2-alpine --format "redis: {{.Os}}/{{.Architecture}}"
