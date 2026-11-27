# GIT-03 · Lab — Tìm bug bằng git bisect

> **Mục tiêu:** tự động tạo 1 repo có đúng **200 commit** (với 1 regression ẩn tại commit `#137`) rồi dùng `git bisect run` tìm ra thủ phạm trong đúng **8 bước** (~vài giây).
> **Thời lượng:** ~25 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| Git | 2.30+ | `git --version` |
| PowerShell hoặc Git Bash | Bất kỳ | — |

> Lab khởi tạo repo tạm tại `$env:TEMP\git-03-bisect-lab` (hoặc `/tmp/git-03-bisect-lab`) bằng `git fast-import` (< 1 giây).

## 1. Cấu trúc
```text
lab/
├── README.md
├── scripts/
│   ├── create-200-commits-repo.ps1   # Tạo repo 200 commit trên Windows
│   ├── create-200-commits-repo.sh    # Tạo repo 200 commit trên Linux/macOS/Git Bash
│   ├── bisect-check.ps1              # Script test trả exit 0/1/125 cho Windows
│   └── bisect-check.sh               # Script test trả exit 0/1/125 cho Linux/macOS
└── solutions/
    └── run-bisect-solution.ps1       # Lời giải E6 chạy tự động từ đầu tới cuối
```

## 2. Chạy

**Windows (PowerShell):**
```powershell
$check = (Resolve-Path scripts/bisect-check.ps1).Path
powershell -ExecutionPolicy Bypass -File scripts/create-200-commits-repo.ps1
cd $env:TEMP\git-03-bisect-lab
git bisect start HEAD (git rev-list --max-parents=0 HEAD)
git bisect run powershell -NoProfile -ExecutionPolicy Bypass -File $check
git bisect reset
```

**Linux / macOS / Git Bash:**
```bash
CHECK="$(pwd)/scripts/bisect-check.sh"
bash scripts/create-200-commits-repo.sh
cd /tmp/git-03-bisect-lab
git bisect start HEAD "$(git rev-list --max-parents=0 HEAD)"
git bisect run "$CHECK"
git bisect reset
```

## 3. Các bước thí nghiệm
1. **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
   - Trên 200 commit, `git bisect` cần tối đa bao nhiêu bước kiểm tra?
   - Commit trung vị đầu tiên mà `git bisect` checkout tới là `iteration` thứ mấy?
2. Chạy các lệnh ở §2 và quan sát từng bước chia đôi trên console.
3. Mở file `scripts/bisect-check.ps1` (hoặc `.sh`) xem cách nó dùng `exit 0`, `exit 1` và `exit 125`.

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11, Git `2.54.0.windows.1`, ngày 2026-11-27)*:
> - Tạo 200 commit bằng `git fast-import` xong trong < 1s.
> - `git bisect run` thực hiện đúng **8 bước**:
>   `iteration 100` → `150` → `125` → `137` → `131` → `134` → `136` → kết luận:
>   `34db58801042dfe267161565f02d37325cf5f7b6 is the first bad commit` (`chore: iteration 137`).

## 5. Giải thích kết quả
- Binary search trên 200 commit loại một nửa ở mỗi bước nên tối đa chỉ cần $\lceil \log_2(200) \rceil = 8$ lần chạy test thay vì 64–200 lần duyệt tay.
- File `bisect-check` nằm bên ngoài `$env:TEMP\git-03-bisect-lab` nên không bị biến mất khi `git bisect` checkout lùi về các commit cũ (xem [README §3.2](../README.md)).

## 6. Biến thể để tự khám phá
- Chạy `solutions/run-bisect-solution.ps1` hoặc đổi `$bugCommitNumber = 42` trong `create-200-commits-repo.ps1` rồi chạy lại để thấy `git bisect` vẫn tìm ra chính xác trong 8 bước.

## 7. Dọn dẹp
```powershell
Remove-Item -Recurse -Force "$env:TEMP\git-03-bisect-lab" -ErrorAction SilentlyContinue
```
