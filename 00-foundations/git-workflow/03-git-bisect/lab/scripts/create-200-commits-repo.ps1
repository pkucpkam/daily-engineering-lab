param(
    [string]$LabDir = (Join-Path $env:TEMP "git-03-bisect-lab")
)

$ErrorActionPreference = "Stop"
if (Test-Path $LabDir) { Remove-Item -Recurse -Force $LabDir }
New-Item -ItemType Directory -Path $LabDir | Out-Null
Set-Location $LabDir

git init -b main | Out-Null
git config user.name "Lab Learner"
git config user.email "learner@example.local"

# Use git fast-import so generating 200 commits finishes in < 1 second
$bugCommitNumber = 137
$sb = New-Object System.Text.StringBuilder
$baseEpoch = 1795700000

for ($i = 1; $i -le 200; $i++) {
    $ts = $baseEpoch + ($i * 60)
    $discountRule = if ($i -ge $bugCommitNumber) { "vip_discount_percent=5" } else { "vip_discount_percent=10" }
    $content = "feature_seq=$i`n$discountRule`n"
    $contentBytes = [System.Text.Encoding]::UTF8.GetByteCount($content)
    $msg = "chore: iteration $i"
    $msgBytes = [System.Text.Encoding]::UTF8.GetByteCount($msg)

    [void]$sb.Append("commit refs/heads/main`n")
    [void]$sb.Append("mark :$i`n")
    [void]$sb.Append("committer Lab Learner <learner@example.local> $ts +0700`n")
    [void]$sb.Append("data $msgBytes`n$msg`n")
    if ($i -gt 1) {
        [void]$sb.Append("from :$($i - 1)`n")
    }
    [void]$sb.Append("M 100644 inline pricing.properties`n")
    [void]$sb.Append("data $contentBytes`n$content")
}

$importFile = Join-Path $LabDir ".fast-import-stream"
[System.IO.File]::WriteAllText($importFile, $sb.ToString(), [System.Text.UTF8Encoding]::new($false))
# Redirect raw binary/UTF-8 file via cmd /c so PowerShell pipeline never re-encodes stdin
cmd /c "git fast-import --quiet < `"$importFile`""
if ($LASTEXITCODE -ne 0) { throw "git fast-import failed with exit code $LASTEXITCODE" }
Remove-Item $importFile -Force
git checkout -f main | Out-Null

$totalCommits = (git rev-list --count HEAD).Trim()
$firstHash = (git rev-list --max-parents=0 HEAD).Trim()
Write-Host "=== GIT-03 Lab Ready at: $LabDir ==="
Write-Host "Total commits: $totalCommits (first=$firstHash, HEAD=$(git rev-parse --short HEAD))"
Write-Host "Hidden regression introduced at commit #$bugCommitNumber (expected vip_discount_percent=10, broken to 5)."
