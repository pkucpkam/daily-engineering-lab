#!/usr/bin/env bash
set -euo pipefail

LAB_DIR="${1:-/tmp/git-01-internals-lab}"
rm -rf "$LAB_DIR"
mkdir -p "$LAB_DIR"
cd "$LAB_DIR"

git init -b main >/dev/null
git config user.name "Lab Learner"
git config user.email "learner@example.local"

echo "=== [1] Create blob manually with git hash-object ==="
printf 'hello git internals\n' > hello.txt
BLOB_HASH=$(git hash-object -w hello.txt)
echo "Blob hash : $BLOB_HASH"
echo "Blob type : $(git cat-file -t "$BLOB_HASH")"
echo "Object path: .git/objects/${BLOB_HASH:0:2}/${BLOB_HASH:2}"

echo ""
echo "=== [2] Stage blob directly into Index (no git add) ==="
git update-index --add --cacheinfo 100644 "$BLOB_HASH" hello.txt
git ls-files --stage

echo ""
echo "=== [3] Build tree object with git write-tree ==="
TREE_HASH=$(git write-tree)
echo "Tree hash : $TREE_HASH"
git cat-file -p "$TREE_HASH"

echo ""
echo "=== [4] Build commit object with git commit-tree ==="
COMMIT_HASH=$(printf 'initial handcrafted commit\n' | git commit-tree "$TREE_HASH")
echo "Commit hash: $COMMIT_HASH"
git cat-file -p "$COMMIT_HASH"

echo ""
echo "=== [5] Point branch ref 'main' to commit & check HEAD ==="
git update-ref refs/heads/main "$COMMIT_HASH"
echo "HEAD content           : $(cat .git/HEAD)"
echo "refs/heads/main content: $(cat .git/refs/heads/main)"
wc -c .git/refs/heads/main

echo ""
echo "=== [6] Modify 1 byte and compare new commit hash ==="
printf 'hello git internals!\n' > hello.txt
BLOB2=$(git hash-object -w hello.txt)
git update-index --add --cacheinfo 100644 "$BLOB2" hello.txt
TREE2=$(git write-tree)
COMMIT2=$(printf 'change 1 byte\n' | git commit-tree "$TREE2" -p "$COMMIT_HASH")
git update-ref refs/heads/main "$COMMIT2"
echo "New blob  : $BLOB2"
echo "New tree  : $TREE2"
echo "New commit: $COMMIT2"
git log --oneline --graph
