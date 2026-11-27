# GIT-01 · Lab — Git Internals: objects, refs, index

> **Mục tiêu:** dựng tay hoàn toàn 1 commit từ `blob` → `index` → `tree` → `commit` → `refs/heads/main` bằng lệnh plumbing, và nhìn tận mắt file `.git/refs/heads/main` dài đúng 41 byte trên đĩa.
> **Thời lượng:** ~30 phút · **Cấp:** L1/L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| Git | 2.30+ | `git --version` |
| PowerShell hoặc Git Bash | Bất kỳ | — |

> Lab tự khởi tạo repo tạm trong thư mục tạm của OS (`$env:TEMP\git-01-internals-lab` trên Windows hoặc `/tmp/git-01-internals-lab` trên Linux/macOS) để **không tạo `.git` lồng nhau** bên trong repo học tập này.

## 1. Cấu trúc
```text
lab/
├── README.md
├── scripts/
│   ├── build-handcrafted-commit.ps1   # Script Windows PowerShell
│   └── build-handcrafted-commit.sh    # Script Linux/macOS/Git Bash
└── solutions/
    └── create-subtree-commit.ps1      # Lời giải E6 (tạo commit có thư mục src/Main.java)
```

## 2. Chạy

**Windows (PowerShell):**
```powershell
powershell -ExecutionPolicy Bypass -File scripts/build-handcrafted-commit.ps1
```

**Linux / macOS / Git Bash:**
```bash
bash scripts/build-handcrafted-commit.sh
```

## 3. Các bước thí nghiệm

### Bước 1 — Quan sát tự dựng commit bằng plumbing
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. `refs/heads/main` nặng chính xác bao nhiêu byte trên đĩa?
  2. Nếu thêm 1 ký tự `!` vào cuối `hello.txt`, hash của `tree` và `commit` có giữ nguyên như cũ không?
- Làm gì: chạy script ở §2 và quan sát 6 bước `[1]` → `[6]`.

### Bước 2 — Thực hành tự gõ tay ở repo tạm
- Đi vào repo tạm vừa tạo (`cd $env:TEMP\git-01-internals-lab`), thử chạy `git cat-file -p HEAD` và `git ls-tree HEAD` để đối chiếu từng hash.

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11, Git `2.54.0.windows.1`, ngày 2026-11-27)*:
> - `Blob hash` ban đầu: `b00f56f95b66e87d8d2f3baff7ee67f101c7bfc6` → `.git/objects/b0/0f56f9...`
> - `Tree hash`: `a493cf4c4cb78eec0d8a307a7bbdd6196787e3d5`
> - `Commit hash`: `1461839ca00a0092386b55d7f9cb405d2fc0b040`
> - `HEAD`: `ref: refs/heads/main` · `refs/heads/main`: **41 bytes** (`1461839ca00a0092386b55d7f9cb405d2fc0b040` + `\n`).
> - Sau khi đổi 1 byte (`!` cuối dòng): `blob` → `8abfcf6...`, `tree` → `02eb3c4...`, `commit` → `28b1c15...`.

## 5. Giải thích kết quả
- Toàn bộ `git add` và `git commit` thực chất chỉ là wrapper gọi đúng 5 bước plumbing trên: `hash-object` → `update-index` → `write-tree` → `commit-tree` → `update-ref` (xem [README §3](../README.md)).

## 6. Biến thể để tự khám phá
- Chạy `solutions/create-subtree-commit.ps1` để thấy `git write-tree` tự động tạo thêm 1 `tree` lồng cho thư mục `src/`.

## 7. Dọn dẹp
```powershell
Remove-Item -Recurse -Force "$env:TEMP\git-01-internals-lab" -ErrorAction SilentlyContinue
```
