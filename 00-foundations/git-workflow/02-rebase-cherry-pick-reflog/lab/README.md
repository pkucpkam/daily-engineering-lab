# GIT-02 · Lab — Rebase, Cherry-pick, Reflog

> **Mục tiêu:** thực hành trực tiếp trên một repo tạm: cứu 2 commit vừa mất sau `git reset --hard HEAD~2` bằng `git reflog`, gộp 5 commit WIP bằng `rebase -i` (hoặc `reset --soft`), và `cherry-pick -x` đúng 1 hotfix sạch sang `main`.
> **Thời lượng:** ~30 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| Git | 2.30+ | `git --version` |
| PowerShell hoặc Git Bash | Bất kỳ | — |

> Lab tạo repo tạm ở `$env:TEMP\git-02-rebase-reflog-lab` (hoặc `/tmp/git-02-rebase-reflog-lab`), độc lập hoàn toàn với repo học tập này.

## 1. Cấu trúc
```text
lab/
├── README.md
├── scripts/
│   ├── setup-lab.ps1                      # Khởi tạo repo bài tập trên Windows
│   └── setup-lab.sh                       # Khởi tạo repo bài tập trên Linux/macOS/Git Bash
└── solutions/
    └── verify-recovery-and-cherrypick.ps1 # Lời giải tự động kiểm chứng E6 + E7
```

## 2. Chạy

**Windows (PowerShell):**
```powershell
powershell -ExecutionPolicy Bypass -File scripts/setup-lab.ps1
cd $env:TEMP\git-02-rebase-reflog-lab
```

**Linux / macOS / Git Bash:**
```bash
bash scripts/setup-lab.sh
cd /tmp/git-02-rebase-reflog-lab
```

## 3. Các bước thí nghiệm

### Bước 1 — Cứu commit sau `reset --hard` bằng `reflog`
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Khi chạy `git log --oneline` trên nhánh `feature-wip`, bạn thấy mấy commit WIP?
  2. Trong `git reflog`, dòng chứa `wip: partial step 5` nằm ở mục `HEAD@{?}` nào?
- Làm gì:
  ```bash
  git switch feature-wip
  git log --oneline
  git reflog feature-wip
  git reset --hard <hash-của-step-5>
  ```

### Bước 2 — Squash 5 commit WIP thành 1 commit sạch
- Làm gì:
  ```bash
  git rebase -i main
  ```
  Trong trình soạn thảo, giữ `reword` (hoặc `pick`) ở dòng đầu và đổi 4 dòng dưới thành `fixup`.

### Bước 3 — Cherry-pick đúng 1 hotfix sang `main`
- **Dự đoán trước khi chạy**: hash của commit hotfix trên `main` sau khi cherry-pick có giống hash trên `hotfix-source` không?
- Làm gì:
  ```bash
  git switch main
  git log hotfix-source --oneline
  git cherry-pick -x <hash-của-fix-socket-timeout>
  git log --oneline --graph --all
  ```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11, Git `2.54.0.windows.1`, chạy `verify-recovery-and-cherrypick.ps1` ngày 2026-11-27)*:
> - Trước khi cứu: `feature-wip` chỉ hiện 3 commit (`wip: partial step 1..3`).
> - Sau `git reflog` → `git reset --hard 7a98e5e`: khôi phục đủ 5 commit (`wip: partial step 1..5`).
> - Sau khi gộp & `cherry-pick`: `main` nhận commit mới `3b08384 fix: set explicit socket timeout` (hash mới khác `a563f1f` trên `hotfix-source`), không dính `exp.txt`.

## 5. Giải thích kết quả
- `git reset --hard` chỉ dời con trỏ 41 byte của nhánh `feature-wip` và reset Index/Working Tree, không xóa các `commit` object khỏi `.git/objects/`. `git reflog` lưu mọi lịch sử di chuyển con trỏ để khôi phục.
- `cherry-pick` tạo commit mới có `parent` là `HEAD` của `main`, nên hash đổi dù diff giống hệt. Chi tiết xem [README §3](../README.md).

## 6. Biến thể để tự khám phá
- Thử sửa cùng file `timeout.cfg` trên `main` trước khi cherry-pick một nhánh khác sửa dòng đó để tự luyện giải conflict và bấm `git cherry-pick --continue`.

## 7. Dọn dẹp
```powershell
Remove-Item -Recurse -Force "$env:TEMP\git-02-rebase-reflog-lab" -ErrorAction SilentlyContinue
```
