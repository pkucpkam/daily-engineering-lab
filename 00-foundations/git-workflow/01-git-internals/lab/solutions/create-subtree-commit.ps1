param(
    [string]$LabDir = (Join-Path $env:TEMP "git-01-e6-solution")
)

$ErrorActionPreference = "Stop"
if (Test-Path $LabDir) { Remove-Item -Recurse -Force $LabDir }
New-Item -ItemType Directory -Path (Join-Path $LabDir "src") | Out-Null
Set-Location $LabDir

git init -b main | Out-Null
git config user.name "Lab Learner"
git config user.email "learner@example.local"

$javaFile = Join-Path $LabDir "src\Main.java"
[System.IO.File]::WriteAllText($javaFile, "public class Main {}`n", [System.Text.UTF8Encoding]::new($false))

$blobHash = (git hash-object -w src/Main.java).Trim()
git update-index --add --cacheinfo 100644 $blobHash src/Main.java
$rootTree = (git write-tree).Trim()
$commit = ("add src/Main.java via plumbing" | git commit-tree $rootTree).Trim()
git update-ref refs/heads/main $commit

Write-Host "Root tree entries:"
git cat-file -p $rootTree
Write-Host "`nRecursive ls-tree HEAD:"
git ls-tree -r HEAD
