#!/usr/bin/env bash
set -euo pipefail

LAB_DIR="${1:-/tmp/git-03-bisect-lab}"
rm -rf "$LAB_DIR"
mkdir -p "$LAB_DIR"
cd "$LAB_DIR"

git init -b main >/dev/null
git config user.name "Lab Learner"
git config user.email "learner@example.local"

BUG_AT=137
BASE_EPOCH=1795700000

{
  for i in $(seq 1 200); do
    TS=$((BASE_EPOCH + i * 60))
    if [ "$i" -ge "$BUG_AT" ]; then
      RULE="vip_discount_percent=5"
    else
      RULE="vip_discount_percent=10"
    fi
    CONTENT=$(printf 'feature_seq=%s\n%s\n' "$i" "$RULE")
    MSG="chore: iteration $i"
    printf 'commit refs/heads/main\n'
    printf 'mark :%s\n' "$i"
    printf 'committer Lab Learner <learner@example.local> %s +0700\n' "$TS"
    printf 'data %s\n%s\n' "${#MSG}" "$MSG"
    if [ "$i" -gt 1 ]; then
      printf 'from :%s\n' "$((i - 1))"
    fi
    printf 'M 100644 inline pricing.properties\n'
    printf 'data %s\n%s' "${#CONTENT}" "$CONTENT"
  done
} | git fast-import --quiet

git checkout -f main >/dev/null
echo "=== GIT-03 Lab Ready at: $LAB_DIR ==="
echo "Total commits: $(git rev-list --count HEAD) (first=$(git rev-list --max-parents=0 HEAD), HEAD=$(git rev-parse --short HEAD))"
echo "Hidden regression introduced at commit #$BUG_AT."
