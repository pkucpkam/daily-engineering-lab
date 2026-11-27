# LNX-06 · Đáp án — Shell scripting tự động hóa

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **`-u` (`nounset`):** Nếu `$ENV_NAME` chưa được gán, `cd /opt/jboss/cache/$ENV_NAME` mở rộng thành `cd /opt/jboss/cache/` (đi vào thư mục cha chứa tất cả các môi trường!), sau đó `rm -rf *` **xóa sạch cache của mọi môi trường**! Với `-u`, bash dừng ngay khi gặp `$ENV_NAME` chưa gán.
2. **`-e` (`errexit`):** Nếu `cd /opt/jboss/cache/prod` thất bại (exit code `1`), thiếu `-e` làm bash đi tiếp xuống `rm -rf *` và **xóa toàn bộ file tại thư mục hiện tại** (ví dụ `/home/jboss` hoặc `/`). Với `-e`, script dừng ngay khi `cd` lỗi.
3. **`-o pipefail`:** Nếu `pg_dump prod_db` chết vì sai mật khẩu nhưng `gzip` vẫn nhận stream rỗng và thoát với code `0`, thiếu `pipefail` khiến cả pipeline trả về `0` (báo backup thành công giả tạo!). Với `pipefail`, toàn pipeline trả về mã lỗi của `pg_dump`.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
Khi không tìm thấy dòng nào khớp `"OutOfMemoryError"`, lệnh `grep` theo chuẩn POSIX trả về **exit code `1`** (chứ không phải lỗi crash — code `>= 2` mới là lỗi đọc file). Khi bật `set -e`, exit code `1` từ phép gán `ERRORS=$(grep ...)` làm toàn bộ script dừng ngay tức khắc!

**Cách viết lại đúng:**
```bash
# Cách 1: thêm || true (nhưng vẫn kiểm tra file tồn tại trước đó để tránh nuốt lỗi file mất)
if [[ -f /opt/jboss/standalone/log/server.log ]]; then
  ERRORS=$(grep "OutOfMemoryError" /opt/jboss/standalone/log/server.log || true)
fi

# Cách 2: dùng trực tiếp trong điều kiện if (set -e không dừng với điều kiện trong if)
if grep -q "OutOfMemoryError" /opt/jboss/standalone/log/server.log; then
  echo "Phát hiện OOM!"
fi
```
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Cách 1 (`trap 'rm -rf "$TMP"' EXIT`): Khi dòng giữa script lỗi với mã `2`, bash chạy `rm -rf "$TMP"` xong và trả về **exit code `2`** (bảo toàn mã lỗi gốc).
- Cách 2 (`cleanup() { rm -rf "$TMP"; exit 0; }`): Lệnh `exit 0` bên trong hàm trap **ghi đè exit code gốc thành `0`**! Hậu quả trên CI/CD hoặc `systemd`: bước deploy/backup bị fail giữa chừng nhưng trả về `0` khiến pipeline tưởng mọi thứ thành công và tiếp tục deploy code lỗi lên production!
- **Chuẩn đúng:** Luôn lưu `local rc=$?` ở dòng đầu của `cleanup()` và kết thúc bằng `exit "$rc"`.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
Khi truyền `"jboss report 2026.log"` vào vòng lặp `for f in $@; do cat $f; done`, vì cả `$@` và `$f` đều thiếu dấu ngoặc kép, Bash thực hiện **Word Splitting** tách tên file thành 3 tham số riêng: `jboss`, `report`, và `2026.log` → `cat` báo lỗi `No such file or directory` 3 lần!
**Cách sửa chuẩn:** `for f in "$@"; do cat -- "$f"; done` (thêm `--` để tránh tên file bắt đầu bằng dấu `-` bị hiểu nhầm là cờ lệnh!).
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
File lưu trên Windows kết thúc dòng bằng **CRLF (`\r\n`)**; trên Linux chỉ dùng **LF (`\n`)**, nên Linux đọc dòng đầu `#!/usr/bin/env bash\r` và đi tìm trình thông dịch tên là `bash\r` → không tồn tại!
**2 cách phòng/sửa:**
1. Cấu hình Git/IDE ép **LF** cho mọi file shell (`.editorconfig` / `.gitattributes`: `*.sh text eol=lf`).
2. Chạy `sed -i 's/\r$//' deploy.sh` hoặc `dos2unix deploy.sh` trên Linux để cắt sạch `\r`.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Trong `lab/scripts/collect-jboss-diag.sh`:
- Header an toàn: `set -euo pipefail`.
- Xử lý cờ tham số: `while getopts "p:l:o:s:e:h" opt; do ...`
- Dọn dẹp: `cleanup() { local rc=$?; rm -rf "${TMP_DIR:-}"; exit "$rc"; }` + `trap cleanup EXIT INT TERM`.
- Lọc log theo khung giờ bằng `awk`: so sánh timestamp cột `$2` nằm giữa `START_TIME` và `END_TIME` và match `/ERROR|OutOfMemoryError/`.
- Idempotent & atomic: tạo thư mục đích bằng `mkdir -p`, ghi file vào `$TMP_DIR` rồi mới `mv -f` ra `$OUT_DIR`.

**Code kiểm chứng thêm audit rule (E6):** xem [lab/solutions/CustomBashAuditTest.java](./lab/solutions/CustomBashAuditTest.java).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Lệnh `awk` chuẩn cho file `sample-jboss-server.log`:
  ```bash
  awk -v s="14:00:00" -v e="14:05:00" '
    {
      ts = substr($2, 1, 8)
      if (ts >= s && ts <= e && ($0 ~ /ERROR/ || $0 ~ /OutOfMemoryError/)) print
    }
  ' scripts/sample-jboss-server.log
  ```
- Chạy `mvn -o -B verify` kiểm chứng `JbossLogDiagExtractor` lọc ra đúng `3` sự kiện lỗi trong cửa sổ `14:00:00..14:05:00` (bỏ qua dòng lỗi lúc `13:58:00` ngoài cửa sổ), và chạy extractor 2 lần liên tiếp vào cùng `Path` tạo ra file báo cáo giống nhau từng byte!
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**4 lỗi của script cũ:**
1. Thiếu `set -euo pipefail`.
2. Dùng `>>` ghi nối mù quáng (không idempotent — cứ 5 phút nhân đôi danh sách IP!).
3. `curl -s` không có `-f` (khi API trả 500/502 HTML lỗi vẫn trả về exit `0` và chép HTML vào file config!).
4. Không có `flock` chống chạy chồng và không ghi atomic qua temp file.

**Bản sửa chuẩn:**
```bash
#!/usr/bin/env bash
set -euo pipefail

DEST="/etc/app/blocked-ips.txt"
LOCK_FILE="/tmp/sync-blocked-ips.lock"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT INT TERM

exec 9>"$LOCK_FILE"
flock -n 9 || { echo "Sync đang chạy ở tiến trình khác, bỏ qua."; exit 0; }

curl -fsSL --max-time 10 "http://10.0.0.5/blocked.txt" -o "$TMP_DIR/raw.txt"
grep -E '^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$' "$TMP_DIR/raw.txt" | sort -u > "$TMP_DIR/clean.txt"
mv -f "$TMP_DIR/clean.txt" "$DEST"
```
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>7 lỗi và bản viết lại rotate-jboss.sh chuẩn production</summary>

**1. 7 lỗi trong script cũ:**
1. **Thiếu `set -euo pipefail`:** `cd` lỗi vẫn chạy tiếp `rm -f *.log`; `tar` lỗi vẫn được `gzip` biến thành exit `0`.
2. **Lỗi chính tả tên biến `$ARCHVE_DIR` (thiếu chữ `I`):** Thiếu `-u` nên `$ARCHVE_DIR` mở rộng thành `""`, biến `cp ... $ARCHVE_DIR/...` thành ghi thẳng lên root `/jboss-2026-11-27.tar.gz`!
3. **Không `trap` dọn `$TMP_DIR`:** Mỗi lần chạy (hoặc fail giữa chừng) bỏ lại `/tmp/diag-XXXXXX` làm đầy ổ đĩa `/`.
4. **Không kiểm tra tham số & không quote biến:** `$1` có thể rỗng; `$LOG_DIR`, `$TMP_DIR` thiếu dấu ngoặc kép `"..."`.
5. **Chạy `cd $LOG_DIR` không có đường dẫn an toàn và không dùng subshell/kiểm tra `[[ -d "$LOG_DIR" ]]`.**
6. **Thiếu `flock`:** Nếu lần nén log nặng chạy quá chu kỳ cron, hai bản sẽ đụng nhau.
7. **`rm -f *.log` xóa luôn cả `server.log` đang mở bởi JBoss:** Làm JBoss giữ FD `(deleted)` chiếm nguyên dung lượng đĩa (xem [LNX-02](../02-file-descriptors-io/README.md))! Chỉ được nén/xóa các file đã rotate xong (`*.log.20*`), không xóa `server.log` đang ghi.

**2. Bản viết lại `rotate-jboss.sh` chuẩn production:**
```bash
#!/usr/bin/env bash
set -euo pipefail

usage() { echo "Usage: $0 -l <log_dir> -a <archive_dir>" >&2; exit 1; }

LOG_DIR=""
ARCHIVE_DIR=""
while getopts "l:a:h" opt; do
  case "$opt" in
    l) LOG_DIR="$OPTARG" ;;
    a) ARCHIVE_DIR="$OPTARG" ;;
    *) usage ;;
  esac
done

[[ -n "$LOG_DIR" && -n "$ARCHIVE_DIR" ]] || usage
[[ -d "$LOG_DIR" ]] || { echo "Không tồn tại LOG_DIR: $LOG_DIR" >&2; exit 1; }
mkdir -p "$ARCHIVE_DIR"

LOCK_FILE="/tmp/rotate-jboss.lock"
exec 9>"$LOCK_FILE"
flock -n 9 || { echo "rotate-jboss đang chạy, bỏ qua."; exit 0; }

TMP_DIR="$(mktemp -d /tmp/diag-XXXXXX)"
cleanup() {
  local rc=$?
  rm -rf "$TMP_DIR"
  exit "$rc"
}
trap cleanup EXIT INT TERM

STAMP="$(date +%F-%H%M%S)"
OUT_ARCHIVE="$ARCHIVE_DIR/jboss-rotated-$STAMP.tar.gz"

# Chỉ thu thập các file log ĐÃ xoay vòng (vd server.log.2026-11-26), KHÔNG đụng server.log đang mở!
mapfile -t ROTATED_FILES < <(find "$LOG_DIR" -maxdepth 1 -type f -name "*.log.*" -mtime +0 | sort)
if [[ ${#ROTATED_FILES[@]} -eq 0 ]]; then
  echo "Không có file log xoay vòng nào cần nén."
  exit 0
fi

tar -czf "$TMP_DIR/archive.tar.gz" -C "$LOG_DIR" "${ROTATED_FILES[@]##*/}"
mv -f "$TMP_DIR/archive.tar.gz" "$OUT_ARCHIVE"
rm -f -- "${ROTATED_FILES[@]}"
echo "Đã nén an toàn ${#ROTATED_FILES[@]} file -> $OUT_ARCHIVE"
```

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **`set -euo pipefail`:** `-e` (`errexit`) dừng ngay khi lệnh khác `0`; `-u` (`nounset`) báo lỗi nếu dùng biến chưa gán (tránh `rm -rf /$EMPTY`); `-o pipefail` khiến pipeline thất bại nếu bất kỳ lệnh nào trong pipe fail (tránh `pg_dump | gzip` fail ngầm).
2. **`grep` với `set -e`:** Khi không có dòng khớp, `grep` trả exit code `1` làm `set -e` ngắt script. Sửa bằng đặt trong `if grep -q ...; then ... fi` hoặc `grep ... || true` (sau khi đã kiểm tra file tồn tại).
3. **`trap` chuẩn:** `cleanup() { local rc=$?; rm -rf "$TMP_DIR"; exit "$rc"; }; trap cleanup EXIT INT TERM` — dọn dẹp ở mọi ngõ thoát mà không nuốt exit code thật.
4. **`grep`, `awk`, `sed`, `jq`:** `grep` lọc dòng nhanh; `awk` cắt cột, lọc khoảng giá trị/thời gian và tổng hợp; `sed` sửa/cắt theo dòng; `jq` parse JSON an toàn. Ví dụ: `awk '$2 >= "14:00:00" && $2 <= "14:05:00" && /ERROR/' server.log`.
5. **4 kỹ thuật idempotent:** (1) `mkdir -p` / `ln -sfn`; (2) Kiểm tra trước khi sửa file config (`grep -qF ... || ...`); (3) Atomic write vào `mktemp` rồi `mv -f` sang đích; (4) `flock` chống chạy chồng.
</details>
