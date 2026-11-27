#!/usr/bin/env bash
set -euo pipefail

LAB_DIR="${1:-/tmp/git-04-hooks-lab}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HOOK_SRC="$(cd "$SCRIPT_DIR/.." && pwd)/hooks/commit-msg"

rm -rf "$LAB_DIR"
mkdir -p "$LAB_DIR"
cd "$LAB_DIR"

git init -b main >/dev/null
git config user.name "Lab Learner"
git config user.email "learner@example.local"

cp "$HOOK_SRC" .git/hooks/commit-msg
chmod +x .git/hooks/commit-msg

echo "hello" > app.txt
git add app.txt

echo "=== [1] Testing invalid commit message ('update code') ==="
if git commit -m "update code"; then
  echo "UNEXPECTED: commit succeeded!"
  exit 1
else
  echo "Rejected as expected!"
fi

echo ""
echo "=== [2] Testing valid Conventional Commit ('feat(order): add initial app') ==="
git commit -m "feat(order): add initial app"
git log --oneline -n 1
