param(
    [string]$LabDir = (Join-Path $env:TEMP "git-04-hooks-lab")
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$hookSource = Join-Path (Split-Path -Parent $scriptDir) "hooks\commit-msg"

if (Test-Path $LabDir) { Remove-Item -Recurse -Force $LabDir }
New-Item -ItemType Directory -Path $LabDir | Out-Null
Set-Location $LabDir

git init -b main | Out-Null
git config user.name "Lab Learner"
git config user.email "learner@example.local"

$hookTarget = Join-Path $LabDir ".git\hooks\commit-msg"
Copy-Item $hookSource $hookTarget -Force

"hello" | Out-File -Encoding utf8 app.txt
git add app.txt

Write-Host "=== [1] Testing invalid commit message ('update code') ==="
$prevPref = $ErrorActionPreference
$ErrorActionPreference = "Continue"
cmd /c 'git commit -m "update code" 2>&1'
$invalidExit = $LASTEXITCODE
$ErrorActionPreference = $prevPref
Write-Host "Exit code for invalid commit: $invalidExit"
if ($invalidExit -eq 0) { throw "Expected invalid commit to be rejected!" }

Write-Host "`n=== [2] Testing valid Conventional Commit ('feat(order): add initial app') ==="
git commit -m "feat(order): add initial app" | Out-Null
Write-Host "Exit code for valid commit: $LASTEXITCODE"
git log --oneline -n 1
