#!/usr/bin/env bash
set -euo pipefail

LAB_DIR="${1:-/tmp/git-02-rebase-reflog-lab}"
rm -rf "$LAB_DIR"
mkdir -p "$LAB_DIR"
cd "$LAB_DIR"

git init -b main >/dev/null
git config user.name "Lab Learner"
git config user.email "learner@example.local"

echo "v1" > app.txt
git add app.txt
git commit -m "chore: init app" >/dev/null

git switch -c feature-wip >/dev/null
for i in 1 2 3 4 5; do
  echo "wip step $i" >> wip.txt
  git add wip.txt
  git commit -m "wip: partial step $i" >/dev/null
done
BEFORE_RESET=$(git rev-parse --short HEAD)

git reset --hard HEAD~2 >/dev/null

git switch main >/dev/null
git switch -c hotfix-source >/dev/null
echo "experimental" > exp.txt
git add exp.txt
git commit -m "feat: unfinished experiment" >/dev/null

echo "timeout=3000" > timeout.cfg
git add timeout.cfg
git commit -m "fix: set explicit socket timeout" >/dev/null
HOTFIX_HASH=$(git rev-parse --short HEAD)

git switch main >/dev/null

echo "=== GIT-02 Lab Ready at: $LAB_DIR ==="
echo "1) Branch 'feature-wip' had 5 WIP commits (was $BEFORE_RESET), then accidentally ran: git reset --hard HEAD~2"
echo "2) Branch 'hotfix-source' has target hotfix commit: $HOTFIX_HASH ('fix: set explicit socket timeout')"
echo "3) Run 'git switch feature-wip' and inspect 'git reflog' to rescue lost commits, then squash all 5 into 1."
