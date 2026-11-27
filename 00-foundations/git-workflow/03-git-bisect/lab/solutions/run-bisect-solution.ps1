$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$labRoot = Split-Path -Parent $scriptDir
$createScript = Join-Path $labRoot "scripts\create-200-commits-repo.ps1"
$checkScript = Join-Path $labRoot "scripts\bisect-check.ps1"
$labDir = Join-Path $env:TEMP "git-03-bisect-lab"

powershell -ExecutionPolicy Bypass -File $createScript
Push-Location $labDir
try {
    $first = (git rev-list --max-parents=0 HEAD).Trim()
    git bisect start HEAD $first
    git bisect run powershell -NoProfile -ExecutionPolicy Bypass -File $checkScript
    git bisect reset
} finally {
    Pop-Location
    Remove-Item -Recurse -Force $labDir -ErrorAction SilentlyContinue
}
