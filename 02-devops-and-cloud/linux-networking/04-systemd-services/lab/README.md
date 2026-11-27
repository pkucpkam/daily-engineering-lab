# LNX-04 · Lab — systemd & quản lý service

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker/systemd nên `verify-systemd-unit.sh`, `systemd-analyze verify`, `systemctl`, `journalctl`, và `systemd-run` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `SystemdUnitLinter` & `SystemdStateSimulator`** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** phát hiện tự động 6 lỗi kinh điển trong unit file Java/JBoss, tận mắt thấy exit code `143` gây trạng thái `failed` giả nếu thiếu `SuccessExitStatus=143`, và thấy `StartLimitBurst` chặn crash loop.
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Linux với `systemd` *(tùy chọn)* | RHEL 7+ / Ubuntu 20.04+ | `systemctl --version` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── systemd/
│   ├── jboss-legacy-broken.service          # Unit lỗi để luyện audit
│   ├── java-app.service                     # Unit chuẩn Java 21
│   ├── jboss-legacy.service                 # Unit chuẩn JBoss AS 7.1.1
│   ├── heap-check.service                   # Oneshot service cho timer
│   └── heap-check.timer                     # Timer thay cron
├── scripts/
│   └── verify-systemd-unit.sh               # Lệnh systemd-analyze / systemd-run trên Linux
├── src/
│   ├── main/java/lab/linux/systemd/
│   │   ├── SystemdUnitLinter.java           # Linter kiểm tra 6 rule production
│   │   ├── SystemdStateSimulator.java       # Mô phỏng exit 143 & StartLimitBurst
│   │   └── SystemdLabRunner.java            # Runner chạy audit & mô phỏng
│   └── test/java/lab/linux/systemd/
│       └── SystemdUnitTest.java             # JUnit 5 kiểm chứng
└── solutions/
    └── StrictTimeoutRuleTest.java           # Lời giải E6
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Audit unit files & kiểm chứng Exit 143 / Crash-loop
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. File `systemd/jboss-legacy-broken.service` vi phạm bao nhiêu rule production trong `SystemdUnitLinter`?
  2. Nếu không đặt `SuccessExitStatus=143`, `systemctl stop` khiến unit chuyển về `INACTIVE_DEAD` hay `FAILED`?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux có systemd
```bash
bash scripts/verify-systemd-unit.sh
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `SystemdLabRunner`:
>   - `jboss-legacy-broken.service`: **6 findings** (`User=root`, shell wrapper `/bin/sh -c`, thiếu `SuccessExitStatus=143`, `Restart=always` thiếu burst limit, thiếu `LimitNOFILE`, thiếu `MemoryMax`).
>   - `java-app.service` và `jboss-legacy.service`: **0 findings**.
>   - `Stop JVM (exit 143)`: thiếu `SuccessExitStatus=143` → `FAILED (exit-code(143))`, có cấu hình → `INACTIVE_DEAD`.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `systemd-analyze verify systemd/java-app.service` và `systemd-analyze calendar "*-*-* 02:00:00"`.
- `sudo systemd-run --unit=java-mem-test -p MemoryMax=128M ...` (chạy service thử với giới hạn cgroup thật).
- `systemctl status`, `journalctl -u ...`, `systemctl list-timers --all`.

## 6. Giải thích kết quả
- Systemd quản lý tiến trình từ PID 1 qua cgroups riêng (không qua PAM `limits.conf`) và kiểm tra exit code trực tiếp của Main PID, do đó JVM bắt `SIGTERM` thoát với mã `143` bắt buộc cần `SuccessExitStatus=143` (xem [README §3.2–3.3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
