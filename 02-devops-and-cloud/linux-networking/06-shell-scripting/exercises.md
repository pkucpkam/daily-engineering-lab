# LNX-06 · Bài tập — Shell scripting tự động hóa

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu `set -euo pipefail`, `trap`, quoting, idempotency | 5 | 20' |
| 🟡 L2 — Apply | Audit bash script bằng Java linter & lọc log JBoss idempotent | 3 | 70' |
| 🔴 L3 — Deep | Sửa script deploy/backup production gây mất dữ liệu trên server | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Giải thích từng cờ trong `set -euo pipefail`
Cho đoạn script ngắn không có header an toàn:
```bash
#!/bin/bash
cd /opt/jboss/cache/$ENV_NAME
rm -rf *
pg_dump prod_db | gzip > /backups/db.sql.gz
```
Nếu `$ENV_NAME` bị quên gán, hoặc thư mục `/opt/jboss/cache/prod` chưa tạo, hoặc `pg_dump` thất bại do hết quyền: lần lượt từng cờ `-e`, `-u`, `-o pipefail` ngăn chặn thảm họa gì?

---

### E2. Tại sao `grep` làm script `set -e` dừng đột ngột khi không có lỗi?
Bạn thêm `set -euo pipefail` vào script kiểm tra log:
```bash
ERRORS=$(grep "OutOfMemoryError" /opt/jboss/standalone/log/server.log)
echo "Found errors: $ERRORS"
```
Khi chạy trên server hoàn toàn sạch sẽ (không có dòng `OutOfMemoryError` nào), script chết ngay ở dòng `ERRORS=...` và thoát với code `1`. Tại sao và viết lại dòng đó thế nào cho đúng?

---

### E3. Bẫy `trap` làm mất exit code thật
Một kỹ sư viết hàm dọn dẹp:
```bash
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
```
Khác với:
```bash
cleanup() { rm -rf "$TMP"; exit 0; }
trap cleanup EXIT
```
Nếu dòng giữa script thất bại với exit code `2`, hai cách viết `trap` trên trả về mã thoát cuối cùng của script bằng bao nhiêu? Vì sao `exit 0` trong `cleanup()` là bẫy cực kỳ nguy hiểm cho CI/CD và cron?

---

### E4. Quoting `"$@"` vs `$*` và Word Splitting
Trong một script wrapper chạy lệnh cho file truyền vào: `process_files() { for f in $@; do cat $f; done }`. Điều gì xảy ra nếu người dùng truyền tên file `"jboss report 2026.log"`? Sửa thế nào?

---

### E5. CRLF (`\r\n`) khi viết script trên Windows
Bạn viết script `deploy.sh` bằng IDE trên Windows rồi copy lên RHEL server, chạy `./deploy.sh` liền báo `/usr/bin/env: 'bash\r': No such file or directory`. Giải thích nguyên nhân và 2 cách phòng tránh/sửa nhanh.

---

## 🟡 L2 — Apply

### E6. Audit Script Bash & Kiểm chứng chuẩn `LF` bằng `BashScriptSafetyAuditor`
**Bối cảnh:** Dùng [lab/](./lab/README.md) kiểm tra 2 script trong `lab/scripts/`:
- `unsafe-vs-safe.sh` (phần unsafe demo)
- `collect-jboss-diag.sh` (production diagnostic script)
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Đọc `lab/scripts/collect-jboss-diag.sh`, chỉ ra chính xác các dòng thực hiện: (a) `set -euo pipefail`, (b) `getopts`, (c) `trap cleanup EXIT INT TERM`, (d) trích xuất log theo khung giờ bằng `awk`, (e) ghi atomic + idempotent.
3. Thử phân tích một đoạn script có 3 lỗi tự nghĩ bằng `BashScriptSafetyAuditor` (hoặc xem `lab/solutions/CustomBashAuditTest.java`).

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] `collect-jboss-diag.sh` không chứa ký tự `\r` (CRLF) và đạt **0 findings** từ `BashScriptSafetyAuditor`.

<details><summary>💡 Hint</summary>

Để giữ nguyên idempotency, `collect-jboss-diag.sh` ghi dữ liệu vào `"$TMP_DIR"` trước, chỉ khi hoàn tất mới `mv -f` sang `"$OUT_DIR"`, và luôn dùng `mkdir -p`.
</details>

---

### E7. Lọc & Tổng hợp `server.log` JBoss Idempotent (mô phỏng `awk` + `grep`)
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/shell/JbossLogDiagExtractor.java` và file mẫu `lab/scripts/sample-jboss-server.log`.
**Yêu cầu:**
1. Xem nội dung `sample-jboss-server.log` và viết trước bằng giấy lệnh `awk` lấy các dòng `ERROR` hoặc `OutOfMemoryError` trong khoảng `14:00:00` đến `14:05:00` và đếm tổng số dòng theo mức độ (`ERROR`, `WARN`, `INFO`).
2. Chạy `ShellLabRunner` và `ShellSafetyTest` để kiểm chứng `JbossLogDiagExtractor` cho ra đúng danh sách lỗi trong cửa sổ `14:00:00..14:05:00`, đồng thời chạy lặp **2 lần liên tiếp** vào cùng thư mục báo cáo vẫn ra kết quả byte giống hệt nhau (idempotent).

**Tiêu chí hoàn thành:**
- [ ] Lệnh `awk` viết tay khớp đúng logic khoảng thời gian và regex.
- [ ] Pass test chứng minh chạy 2 lần liên tiếp cho output file giống hệt nhau (`idempotent`).

<details><summary>💡 Hint</summary>

Với log định dạng `2026-11-27 14:02:11,105 ERROR [...]`, cột `$2` (hoặc `substr($2, 1, 8)`) có thể so sánh chuỗi trực tiếp theo thứ tự từ điển `>= "14:00:00" && <= "14:05:00"`!
</details>

---

### E8. Sửa cặp script chưa Idempotent có `flock` & Atomic Write
**Bối cảnh:** Đoạn script sau chạy mỗi 5 phút bằng cron để cập nhật danh sách IP bị chặn vào file `/etc/app/blocked-ips.txt` từ API:
```bash
#!/bin/bash
curl -s http://10.0.0.5/blocked.txt >> /etc/app/blocked-ips.txt
```
**Yêu cầu:**
Chỉ ra 4 lỗi của dòng script trên (bao gồm không idempotent, hỏng file khi mất mạng giữa chừng, không lock khi chạy chồng, thiếu `set -euo pipefail`) và viết lại script hoàn chỉnh khắc phục cả 4 lỗi.

**Tiêu chí hoàn thành:**
- [ ] Dùng `flock` tránh 2 cron chạy đè, `curl -fsSL --max-time 10` (fail khi HTTP 4xx/5xx thay vì ghi HTML lỗi vào config!), ghi vào `mktemp` rồi `sort -u` + `mv -f` atomic.

<details><summary>💡 Hint</summary>

`curl -s` trả exit `0` ngay cả khi server trả trang HTML `502 Bad Gateway`! Bắt buộc dùng **`curl -fsSL`** (`-f` = `--fail`) để trả exit code `22` khi HTTP >= 400.
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Script rotate & archive log JBoss xóa nhầm dữ liệu và làm đầy `/tmp`
**Tình huống:**
> Một đêm sau khi cron chạy script `rotate-jboss.sh` bên dưới trên server RHEL:
> 1. Trong `/tmp` chất đầy hàng chục thư mục `diag-*` chưa xóa khiến phân vùng `/` đầy `98%`.
> 2. Hôm trước một thư mục `$ARCHIVE_DIR` bị gõ nhầm biến hoặc không tạo sẵn làm `cd` lỗi, và file tarball nén bị hỏng nửa chừng (`gzip` vẫn báo thành công) nhưng script vẫn chạy lệnh `rm -f` xóa mất log gốc của cả tuần!
> ```bash
> #!/bin/bash
> LOG_DIR=$1
> TMP_DIR=$(mktemp -d /tmp/diag-XXXXXX)
> cd $LOG_DIR
> tar -cf - *.log | gzip > $TMP_DIR/jboss-logs.tar.gz
> cp $TMP_DIR/jboss-logs.tar.gz $ARCHVE_DIR/jboss-$(date +%F).tar.gz
> rm -f *.log
> ```

**Yêu cầu:**
1. Liệt kê **đầy đủ 7 lỗi** trong 7 dòng script trên.
2. Viết lại hoàn chỉnh `rotate-jboss.sh` đạt chuẩn production: `set -euo pipefail`, nhận tham số bằng `getopts` (`-l <log_dir> -a <archive_dir>`), kiểm tra thư mục tồn tại, `trap` dọn `$TMP_DIR` giữ nguyên exit code, `flock` chống chạy chồng, `tar | gzip` an toàn với atomic `mv`, và chỉ dọn log cũ (`find ... -mtime +7` hoặc `truncate` an toàn thay vì `rm -f *.log` xóa cả file JBoss đang giữ FD!).

**Tiêu chí hoàn thành:**
- [ ] Phát hiện đủ 7 lỗi (thiếu `set -euo pipefail`, biến sai tên `$ARCHVE_DIR`, thiếu quote, thiếu `trap`, `cd` không kiểm tra, `rm -f *.log` đụng file JBoss đang mở giữ FD [LNX-02], thiếu `flock`).
- [ ] Script viết lại vượt qua toàn bộ quy tắc của `BashScriptSafetyAuditor` (và `set -euo pipefail` + `trap` + `getopts`).

<details><summary>💡 Hint</summary>

Nhớ lại **LNX-02**: nếu JBoss đang ghi `server.log`, lệnh `rm -f *.log` sẽ tạo file `(deleted)` không giải phóng đĩa! Chỉ xóa các file log **đã xoay vòng cũ** (ví dụ `server.log.2026-*` không còn process nào mở — kiểm tra bằng `find ... -name "server.log.*" -mtime +1`), không xóa file `server.log` đang active!
</details>
