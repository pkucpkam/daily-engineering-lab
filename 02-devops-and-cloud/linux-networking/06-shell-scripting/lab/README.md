# LNX-06 · Lab — Shell scripting tự động hóa

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker (và Git Bash MSYS2 trên môi trường sandbox bị chặn `NtCreateDirectoryObject`), nên các script `collect-jboss-diag.sh` và `unsafe-vs-safe.sh` **chưa được chạy trực tiếp bằng GNU Bash** (xem §5). Phần **Java 21 `BashScriptSafetyAuditor` & `JbossLogDiagExtractor`** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** kiểm tra tĩnh script `.sh` (LF, `set -euo pipefail`, `trap`, quoting), và mô phỏng lọc log JBoss theo khung giờ + ghi báo cáo idempotent.
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Linux Bash *(tùy chọn)* | Bash 4+ (`awk`, `grep`) | `bash --version` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── scripts/
│   ├── collect-jboss-diag.sh                # Script thu thập chẩn đoán JBoss (set -euo pipefail + trap + awk + idempotent)
│   ├── unsafe-vs-safe.sh                    # Demo pipefail & grep dưới set -e
│   └── sample-jboss-server.log              # Log JBoss mẫu
├── src/
│   ├── main/java/lab/linux/shell/
│   │   ├── BashScriptSafetyAuditor.java     # Kiểm tra tĩnh script bash (CRLF, set -euo pipefail, trap, quote)
│   │   ├── JbossLogDiagExtractor.java       # Lọc khung giờ & ghi atomic/idempotent
│   │   └── ShellLabRunner.java              # Runner chạy kiểm tra
│   └── test/java/lab/linux/shell/
│       └── ShellSafetyTest.java             # JUnit 5 kiểm chứng
└── solutions/
    └── CustomBashAuditTest.java             # Lời giải E6
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Audit script & Lọc log idempotent
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. `collect-jboss-diag.sh` có đạt `0` findings và chuẩn LF (`\n`) không?
  2. Khi lọc `sample-jboss-server.log` từ `14:00:00` đến `14:05:00`, có bao nhiêu dòng lỗi khớp (`13:58:10` có bị lọt vào không)?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Chạy thật trên Linux / WSL
```bash
bash -n scripts/collect-jboss-diag.sh
bash scripts/unsafe-vs-safe.sh
bash scripts/collect-jboss-diag.sh -l scripts/sample-jboss-server.log -o /tmp/jboss-diag -s 14:00:00 -e 14:05:00
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `ShellLabRunner`:
>   - `collect-jboss-diag.sh`: `findings=0` (chuẩn LF, đầy đủ `set -euo pipefail`, `getopts`, `trap`).
>   - `Log extraction [14:00:00..14:05:00]`: `total=7, errors=4, matched=3` (loại bỏ dòng `13:58:10` ngoài cửa sổ); chạy 2 lần liên tiếp cho cùng `diag-summary.txt` byte-identical.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `bash -n scripts/collect-jboss-diag.sh` và thực thi `scripts/collect-jboss-diag.sh` kèm `-p <JAVA_PID>` trên Linux (`/proc/$PID/limits`, `jcmd` / `kill -3`, `awk`).
- `bash scripts/unsafe-vs-safe.sh` (kiểm chứng exit code của `false | gzip` khi bật/tắt `pipefail`).

## 6. Giải thích kết quả
- Kết hợp `set -euo pipefail` + `trap ... EXIT INT TERM` + atomic `mktemp` → `mv -f` giúp script thu thập chẩn đoán chạy an toàn và lặp lại nhiều lần không tạo rác hay hỏng dữ liệu đích (xem [README §3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
