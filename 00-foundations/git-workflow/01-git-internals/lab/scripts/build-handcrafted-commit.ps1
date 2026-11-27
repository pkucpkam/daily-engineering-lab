param(
    [string]$LabDir = (Join-Path $env:TEMP "git-01-internals-lab")
)

$ErrorActionPreference = "Stop"

if (Test-Path $LabDir) {
    Remove-Item -Recurse -Force $LabDir
}
New-Item -ItemType Directory -Path $LabDir | Out-Null
Set-Location $LabDir

git init -b main | Out-Null
git config user.name "Lab Learner"
git config user.email "learner@example.local"

Write-Host "=== [1] Create blob manually with git hash-object ==="
$helloPath = Join-Path $LabDir "hello.txt"
[System.IO.File]::WriteAllText($helloPath, "hello git internals`n", [System.Text.UTF8Encoding]::new($false))
$blobHash = (git hash-object -w hello.txt).Trim()
Write-Host "Blob hash : $blobHash"
Write-Host "Blob type : $(git cat-file -t $blobHash)"
Write-Host "Object path: .git/objects/$($blobHash.Substring(0,2))/$($blobHash.Substring(2))"

Write-Host "`n=== [2] Stage blob directly into Index (no git add) ==="
git update-index --add --cacheinfo 100644 $blobHash hello.txt
git ls-files --stage

Write-Host "`n=== [3] Build tree object with git write-tree ==="
$treeHash = (git write-tree).Trim()
Write-Host "Tree hash : $treeHash"
git cat-file -p $treeHash

Write-Host "`n=== [4] Build commit object with git commit-tree ==="
$commitHash = ("initial handcrafted commit" | git commit-tree $treeHash).Trim()
Write-Host "Commit hash: $commitHash"
git cat-file -p $commitHash

Write-Host "`n=== [5] Point branch ref 'main' to commit & check HEAD ==="
git update-ref refs/heads/main $commitHash
Write-Host "HEAD content           : $((Get-Content .git/HEAD -Raw).Trim())"
Write-Host "refs/heads/main content: $((Get-Content .git/refs/heads/main -Raw).Trim())"
Write-Host "refs/heads/main bytes  : $((Get-Item .git/refs/heads/main).Length) bytes"

Write-Host "`n=== [6] Modify 1 byte and compare new commit hash ==="
[System.IO.File]::WriteAllText($helloPath, "hello git internals!`n", [System.Text.UTF8Encoding]::new($false))
$blobHash2 = (git hash-object -w hello.txt).Trim()
git update-index --add --cacheinfo 100644 $blobHash2 hello.txt
$treeHash2 = (git write-tree).Trim()
$commitHash2 = ("change 1 byte" | git commit-tree $treeHash2 -p $commitHash).Trim()
git update-ref refs/heads/main $commitHash2
Write-Host "New blob  : $blobHash2"
Write-Host "New tree  : $treeHash2"
Write-Host "New commit: $commitHash2"
git log --oneline --graph
