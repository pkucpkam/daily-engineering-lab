# Test script for `git bisect run` on Windows PowerShell.
# Exit 0   -> good commit
# Exit 1   -> bad commit
# Exit 125 -> untestable commit (skip)
$file = "pricing.properties"
if (-not (Test-Path $file)) {
    exit 125
}
$content = Get-Content $file -Raw
if ($content -match "vip_discount_percent=10(\r?\n|$)") {
    exit 0
}
if ($content -match "vip_discount_percent=") {
    exit 1
}
exit 125
