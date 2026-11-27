param(
    [string]$LabDir = (Join-Path $env:TEMP "git-02-rebase-reflog-lab")
)

$ErrorActionPreference = "Stop"
if (Test-Path $LabDir) { Remove-Item -Recurse -Force $LabDir }
New-Item -ItemType Directory -Path $LabDir | Out-Null
Set-Location $LabDir

git init -b main | Out-Null
git config user.name "Lab Learner"
git config user.email "learner@example.local"

# Base history on main
"v1" | Out-File -Encoding utf8 app.txt
git add app.txt
git commit -m "chore: init app" | Out-Null

# Scenario A: branch 'feature-wip' with 5 messy WIP commits (2 of them will be dropped by simulated reset --hard)
git switch -c feature-wip | Out-Null
1..5 | ForEach-Object {
    "wip step $_" | Out-File -Append -Encoding utf8 wip.txt
    git add wip.txt
    git commit -m "wip: partial step $_" | Out-Null
}
$beforeReset = (git rev-parse --short HEAD).Trim()

# Simulate accidental 'git reset --hard HEAD~2'
git reset --hard HEAD~2 | Out-Null

# Scenario B: branch 'hotfix-source' with an isolated fix commit to cherry-pick onto main
git switch main | Out-Null
git switch -c hotfix-source | Out-Null
"experimental" | Out-File -Encoding utf8 exp.txt
git add exp.txt
git commit -m "feat: unfinished experiment" | Out-Null

"timeout=3000" | Out-File -Encoding utf8 timeout.cfg
git add timeout.cfg
git commit -m "fix: set explicit socket timeout" | Out-Null
$hotfixHash = (git rev-parse --short HEAD).Trim()

git switch main | Out-Null

Write-Host "=== GIT-02 Lab Ready at: $LabDir ==="
Write-Host "1) Branch 'feature-wip' had 5 WIP commits (was $beforeReset), then accidentally ran: git reset --hard HEAD~2"
Write-Host "2) Branch 'hotfix-source' has target hotfix commit: $hotfixHash ('fix: set explicit socket timeout')"
Write-Host "3) Run 'git switch feature-wip' and inspect 'git reflog' to rescue lost commits, then squash all 5 into 1."
