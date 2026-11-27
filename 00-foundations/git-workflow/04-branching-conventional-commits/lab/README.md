# GIT-04 · Lab — Branching strategy & Conventional Commits

> **Mục tiêu:** cài đặt và kiểm thử `commit-msg` hook viết bằng POSIX shell thuần (chạy được trên cả Git for Windows lẫn Linux/macOS, không cần Python hay Node.js) để tự động chặn các commit message mơ hồ và ép chuẩn Conventional Commits.
> **Thời lượng:** ~20 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| Git | 2.30+ | `git --version` |
| PowerShell hoặc Git Bash | Bất kỳ | — |

> Lab khởi tạo repo tạm ở `$env:TEMP\git-04-hooks-lab` (hoặc `/tmp/git-04-hooks-lab`), độc lập hoàn toàn với repo học tập này.

## 1. Cấu trúc
```text
lab/
├── README.md
├── hooks/
│   └── commit-msg                # POSIX sh hook kiểm tra Conventional Commits
├── scripts/
│   ├── setup-hook-lab.ps1        # Demo tự động trên Windows PowerShell
│   └── setup-hook-lab.sh         # Demo tự động trên Linux/macOS/Git Bash
└── solutions/
    └── commit-msg-strict         # Lời giải E7 (bắt buộc BREAKING CHANGE: khi có !)
```

## 2. Chạy

**Windows (PowerShell):**
```powershell
powershell -ExecutionPolicy Bypass -File scripts/setup-hook-lab.ps1
```

**Linux / macOS / Git Bash:**
```bash
bash scripts/setup-hook-lab.sh
```

## 3. Các bước thí nghiệm
1. **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
   - Khi hook từ chối `"update code"`, lệnh `git commit` trả exit code khác 0 và commit có được tạo trong `git log` không?
   - Vì sao hook vẫn cho qua các message bắt đầu bằng `Merge ` hoặc `Revert "`?
2. Chạy script ở §2 để thấy bước `[1]` (từ chối `"update code"`) và bước `[2]` (chấp nhận `"feat(order): add initial app"`).
3. Vào `$env:TEMP\git-04-hooks-lab` tự thử các message khác như `fix!: ...`, tiêu đề > 72 ký tự, hoặc thay hook bằng `solutions/commit-msg-strict` cho bài tập E7.

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11, Git `2.54.0.windows.1`, ngày 2026-11-27)*:
> - `[1]` `git commit -m "update code"` → bị từ chối với thông báo hướng dẫn và `Exit code: 1`.
> - `[2]` `git commit -m "feat(order): add initial app"` → thành công (`Exit code: 0`), tạo commit `e02e18d`.

## 5. Giải thích kết quả
- Trước khi hoàn tất `git commit`, Git gọi `.git/hooks/commit-msg` truyền đường dẫn file chứa commit message (`.git/COMMIT_EDITMSG`). Nếu hook thoát với mã khác `0`, Git hủy thao tác tạo `commit` object (xem [README §4.1](../README.md)).

## 6. Biến thể để tự khám phá
- Thử áp dụng vào 1 repo cá nhân bằng cách copy `hooks/commit-msg` vào thư mục `.githooks/` và chạy `git config --local core.hooksPath .githooks`.

## 7. Dọn dẹp
```powershell
Remove-Item -Recurse -Force "$env:TEMP\git-04-hooks-lab" -ErrorAction SilentlyContinue
```
