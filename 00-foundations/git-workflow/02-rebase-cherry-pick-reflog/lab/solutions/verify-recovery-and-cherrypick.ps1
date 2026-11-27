param(
    [string]$LabDir = (Join-Path $env:TEMP "git-02-rebase-reflog-lab")
)

$ErrorActionPreference = "Stop"
Set-Location $LabDir

Write-Host "=== [1] Rescue feature-wip after accidental reset --hard via reflog ==="
git switch feature-wip | Out-Null
Write-Host "Before rescue (only 3 WIP commits):"
git log --oneline

# Find the commit right before 'reset: moving to HEAD~2' in reflog
$reflogLine = (git reflog feature-wip --oneline) | Select-String "wip: partial step 5" | Select-Object -First 1
$lostHash = ($reflogLine.ToString().Split(' ')[0]).Trim()
git reset --hard $lostHash | Out-Null
Write-Host "`nRescued commit $lostHash! Now 5 WIP commits exist:"
git log --oneline

Write-Host "`n=== [2] Non-interactive squash of 5 WIP commits into 1 clean commit ==="
git reset --soft main | Out-Null
git commit -m "feat: complete wip feature in one clean commit" | Out-Null
git log --oneline

Write-Host "`n=== [3] Cherry-pick isolated hotfix onto main ==="
git switch main | Out-Null
$hotfixHash = (git log hotfix-source -n 1 --format="%h").Trim()
git cherry-pick $hotfixHash | Out-Null
git log --oneline --graph --all
