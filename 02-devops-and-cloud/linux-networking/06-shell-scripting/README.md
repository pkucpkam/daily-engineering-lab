---
id: LNX-06
title: "Shell scripting tự động hóa"
domain: 02-devops-and-cloud/linux-networking
phase: Y1Q1
kind: lesson
status: draft
prerequisites: [LNX-05]
related_lessons: [LNX-01, LNX-02, LNX-04, LNX-05]
related_existing: []
last_updated: 2026-11-27
---

# LNX-06 · Shell scripting tự động hóa

> **TL;DR** — Một shell script vận hành chạy trên server production mà thiếu **`set -euo pipefail`** giống như lái xe không phanh: một lệnh `cd` thất bại hoặc một biến rỗng `$TARGET_DIR` có thể biến lệnh dọn dẹp thành **`rm -rf /`** xóa sạch dữ liệu. Để tự động hóa an toàn (như thu thập thread dump `kill -3`, lọc log JBoss theo khung giờ bằng `awk`/`grep`, và dọn thư mục tạm), mọi script phải hội đủ 4 trụ cột: **`set -euo pipefail`**, **`trap ... EXIT INT TERM`**, **quote mọi biến `"$var"` & xử lý tham số bằng `getopts`**, và thiết kế **idempotent** (chạy lặp lại 10 lần vẫn ra đúng trạng thái sạch không nhân đôi dữ liệu hay hỏng cấu hình).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q1 |
| **Chủ đề** | Linux & Networking |
| **Tiên quyết** | [LNX-05](../05-linux-perf-tools/README.md) |

**Phạm vi bắt buộc**
- bash cơ bản
- set -euo pipefail
- xử lý tham số
- trap
- awk/sed/grep/jq
- idempotent script

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao script thiếu set -e nguy hiểm?
- Viết script chạy lại nhiều lần không gây hại?

**Ý tưởng lab / demo**
- Script tự động thu thập thông tin chẩn đoán (thread dump, heap info, log)

**Tài liệu gốc gợi ý**
- The Linux Command Line
- Operating Systems: Three Easy Pieces
- Systems Performance (Gregg)
- High Performance Browser Networking (hpbn.co)
- TCP/IP Illustrated

**Gói đầu ra**
- [x] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [x] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [x] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [x] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->

## 1. Vấn đề: tại sao phải biết cái này?

Ba tai nạn vận hành kinh điển từ các bash script viết vội trên server production:
1. **Thảm họa `cd` sai thư mục + `rm -rf *`:** Script deploy chạy `cd /opt/jboss/tmp/work` (nhưng thư mục chưa tồn tại), lệnh `cd` fail trả về exit code `1`, nhưng **thiếu `set -e`** nên bash vẫn chạy tiếp `rm -rf ./*` ngay tại thư mục gốc đang đứng!
2. **Backup rỗng nhưng báo thành công:** Script chạy `pg_dump mydb | gzip > backup.sql.gz`. `pg_dump` chết giữa chừng do sai mật khẩu (`exit 1`), nhưng `gzip` vẫn nén luồng rỗng và trả về `exit 0` vì **thiếu `-o pipefail`** → cron báo thành công suốt 3 tháng dù file backup chỉ nặng `20 bytes`!
3. **Script thu thập log hoặc thêm cron/config không idempotent:** Chạy lại lần 2 liền ghi đè chập 2 dòng cấu hình vào `standalone.conf` hoặc tạo hàng chục file trùng, không có `trap` dọn `/tmp` làm đầy phân vùng gốc `/`.

---

## 2. Mô hình tư duy (mental model)

Hãy xem **Bash script như một dây chuyền công nghiệp tự động không có mắt người nhìn**:
- Theo mặc định, Bash cực kỳ "lạc quan": mắt xích số 2 gãy (`exit != 0`), băng chuyền vẫn chạy tiếp tới máy cắt số 3; biến chưa từng khai báo (`$DIR`) vẫn được hiểu ngầm thành chuỗi rỗng `""`; lệnh ở giữa ống dẫn (`|`) chết vẫn báo cả ống dẫn thành công nếu lệnh cuối sống.
- **`set -euo pipefail`** lắp **cảm biến ngắt khẩn cấp** vào từng mắt xích.
- **`trap cleanup EXIT INT TERM`** là **đội vệ sinh tự động** luôn dọn dẹp lockfile và thư mục tạm bất kể script kết thúc êm hay bị `Ctrl+C`/`SIGTERM`/lỗi giữa đường.

```mermaid
flowchart LR
    START["Bắt đầu script<br/>set -euo pipefail"] --> ARGS["Parse tham số (getopts)<br/>& kiểm tra precondition"]
    ARGS --> TMP["mktemp -d + trap cleanup EXIT INT TERM"]
    TMP --> STEP1["Thu thập / Xử lý an toàn<br/>(quote \"$var\", kiểm tra tồn tại)"]
    STEP1 -->|"Mọi lệnh exit == 0"| COMMIT["Atomic write: mv tmp -> đích<br/>(Idempotent)"]
    STEP1 -->|"Bất kỳ lệnh / pipe lỗi hoặc SIGINT"| ERR["Dừng ngay tại chỗ!"]
    COMMIT --> CLEAN["trap cleanup tự chạy<br/>(xóa tmp & lockfile)"]
    ERR --> CLEAN
```

---

## 3. Cơ chế bên trong (trọng tâm của bài)

### 3.1 Trả lời câu hỏi sâu #1: Vì sao script thiếu `set -e` (và `set -euo pipefail`) lại nguy hiểm?

Từng cờ trong bộ ba `set -euo pipefail` đóng một lỗ hổng mặc định chí mạng của POSIX/Bash shell:

| Cờ | Tên đầy đủ | Mặc định Bash (nguy hiểm) | Khi bật cờ |
| :--- | :--- | :--- | :--- |
| **`-e`** | `errexit` | Lệnh trả về exit code khác `0` chỉ in lỗi và **tiếp tục chạy dòng dưới** (ví dụ `cd /missing && ...` không có, mà viết `cd /missing; rm -rf *`!) | Dừng script ngay khi bất kỳ câu lệnh đơn nào trả về khác `0` (trừ khi nằm trong `if`, `while`, `|| true`) |
| **`-u`** | `nounset` | Biến chưa gán (`$TARGET_DIR` gõ nhầm chính tả `$TARGT_DIR`) mở rộng thành **chuỗi rỗng `""`**, biến `rm -rf "/$TARGT_DIR"` thành **`rm -rf /`**! | Ném lỗi `unbound variable` và dừng script ngay lập tức |
| **`-o pipefail`** | `pipefail` | Trong pipeline `cmd1 \| cmd2 \| gzip`, exit code của cả pipeline là exit code của **lệnh cuối cùng (`gzip` = `0`)**, che giấu lỗi chết của `cmd1` | Trả về exit code của **lệnh đầu tiên/cuối cùng bị fail** trong toàn pipeline |

*(Mẹo quan trọng cho `set -e` + `grep`: Khi dùng `grep` để kiểm tra có tồn tại dòng nào không, nếu không tìm thấy `grep` trả về exit code `1` làm `set -e` dừng script! Do đó bên trong `if grep -q "ERROR" file; then ...` thì an toàn, còn ngoài `if` phải viết `count=$(grep -c "ERROR" file || true)`.)*

### 3.2 Xử lý tham số chuẩn & Quoting (`getopts`, `"$@"`, `${var:?}`)

- **Luôn bọc kép `"$var"` và `"$@"`:** Nếu filename có dấu cách (`report 2026.log`), không quote làm Bash tách thành 2 tham số (`Word Splitting`) và bung ký tự `*` (`Globbing`). Dùng `"$@"` giữ nguyên ranh giới từng tham số gốc truyền vào.
- **Tham số bắt buộc dùng `${1:?Lỗi: thiếu PID}`:** Nếu thiếu tham số, Bash tự dừng và in thông báo lỗi rõ ràng ngay cả khi không viết `if`.
- **Dùng `getopts`** cho script công cụ có cờ (`-p <PID> -o <OUT_DIR> -l <LOG_FILE>`) để thứ tự cờ linh hoạt và có help `-h`.

### 3.3 Cơ chế `trap` để dọn dẹp an toàn

Khi script tạo thư mục tạm (`TMP_DIR="$(mktemp -d)"`) hoặc file lock (`/var/lock/jboss-diag.lock`):
```bash
TMP_DIR="$(mktemp -d)"
cleanup() {
  local exit_code=$?
  rm -rf "$TMP_DIR"
  exit "$exit_code"
}
trap cleanup EXIT INT TERM
```
Dù script thành công (`EXIT`), gặp lỗi bị `set -e` ngắt (`EXIT`), hay người dùng bấm `Ctrl+C` (`INT`) hoặc systemd dừng (`TERM`), hàm `cleanup` **luôn chạy** và vẫn giữ nguyên mã thoát gốc `$exit_code`!

### 3.4 Bộ tứ xử lý text cho SRE: `grep`, `awk`, `sed`, `jq`

| Công cụ | Sở trường thật sự | Ví dụ thực chiến trên log Java / JBoss |
| :--- | :--- | :--- |
| **`grep`** (`-E`, `-F`, `-C`, `-c`) | Lọc dòng theo chuỗi cố định (`-F` nhanh gấp nhiều lần) hoặc Regex, lấy ngữ cảnh `-A`/`-B` | `grep -n -C 5 "OutOfMemoryError" server.log` |
| **`awk`** | Xử lý theo cột (`$1`, `$NF`), lọc khoảng thời gian, tính tổng/đếm group-by cực gọn | `awk '$2 >= "14:00:00" && $2 <= "14:05:00" && /ERROR/' server.log` |
| **`sed`** | Thay thế chuỗi/chỉnh sửa theo dòng (`-n '/start/,/end/p'`) | `sed -n '100,200p' thread_dump.txt` |
| **`jq`** | Parse & truy vấn **JSON** an toàn (API response, Kubernetes JSON, structured log — tuyệt đối không dùng `grep`/`sed` cắt JSON phức tạp!) | `jq -r '.items[] \| select(.status=="ERROR") \| .message'` |

### 3.5 Trả lời câu hỏi sâu #2: Làm sao viết script chạy lại nhiều lần không gây hại (Idempotent)?

**Idempotent** nghĩa là gọi script $N$ lần (`N = 1, 2, 10...`) đều đưa hệ thống về **đúng một trạng thái cuối giống nhau**, không báo lỗi giả và không nhân bản tác dụng phụ:

1. **Tạo thư mục / symlink idempotent:** Dùng `mkdir -p "$OUT_DIR"` (không lỗi nếu đã có) và `ln -sfn "$NEW_RELEASE" "$CURRENT_LINK"` (cờ `-n` tránh tạo symlink lồng vào trong thư mục đích khi đích đã là symlink!).
2. **Chỉnh file cấu hình có kiểm tra trước:** Không viết `echo 'JAVA_OPTS="$JAVA_OPTS -Xmx4g"' >> standalone.conf` (chạy 3 lần nhân thành 3 dòng!). Phải kiểm tra `grep -qF "$LINE" "$CONF" || echo "$LINE" >> "$CONF"`, hoặc dùng marker block có thể replace sạch.
3. **Atomic file write (ghi file tạm rồi `mv`):** Ghi output vào `"$TMP_DIR/summary.txt"`, hoàn tất 100% mới `mv -f "$TMP_DIR/summary.txt" "$OUT_DIR/summary.txt"` (trong cùng filesystem, `mv` là atomic rename — không bao giờ có file bị cắt dở giữa chừng!).
4. **Chống chạy đè song song bằng `flock`:** Nếu script chạy qua cron/timer, dùng `exec 9>"$LOCK_FILE"; flock -n 9 || { echo "Đang chạy, bỏ qua"; exit 0; }` để 2 tiến trình không giẫm chân nhau.

---

## 4. Code minh họa

Xem full production diagnostic script tại [collect-jboss-diag.sh](./lab/scripts/collect-jboss-diag.sh) và bản đối chiếu lỗi tại [unsafe-vs-safe.sh](./lab/scripts/unsafe-vs-safe.sh):

```bash
# ❌ SAI: 5 lỗi nguy hiểm trong 6 dòng
TARGET=$1
cd /opt/jboss/tmp/$TARGET     # Không kiểm tra nếu cd fail!
rm -rf *                      # Xóa nhầm thư mục cũ nếu cd fail!
grep ERROR server.log | head  # Không có pipefail; nếu server.log mất vẫn chạy tiếp
echo "Done" >> /etc/app.conf  # Chạy lại 5 lần ghi 5 dòng trùng!
```

```bash
# ✅ ĐÚNG: An toàn, tự dọn dẹp, idempotent
#!/usr/bin/env bash
set -euo pipefail

TARGET="${1:?Lỗi: phải truyền tên thư mục con}"
WORK_DIR="/opt/jboss/tmp/$TARGET"
TMP_OUT="$(mktemp -d)"
trap 'rm -rf "$TMP_OUT"' EXIT INT TERM

mkdir -p "$WORK_DIR"
if [[ -f /opt/jboss/standalone/log/server.log ]]; then
  awk '/ERROR|OutOfMemoryError/ {print}' /opt/jboss/standalone/log/server.log > "$TMP_OUT/errors.log"
  mv -f "$TMP_OUT/errors.log" "$WORK_DIR/errors.log"
fi
```

### 4.1 Legacy corner (Java 7 / JBoss AS 7.1.1)

Trên môi trường **JBoss AS 7.1.1** chạy trên Linux không có sẵn Python hoặc công cụ agent hiện đại:
1. Script bash chuẩn `set -euo pipefail` dùng sẵn các tiện ích cốt lõi GNU (`awk`, `grep`, `kill -3`, `jstat`, `lsof`, `ss`) là **công cụ tự động hóa duy nhất không cần cài thêm phần mềm** để gom gói chẩn đoán sự cố trong 10 giây trước khi restart JBoss (`collect-jboss-diag.sh`).
2. Cẩn trọng **CRLF (`\r\n`) khi sửa script trên Windows rồi SCP lên Linux!** File `.sh` lưu định dạng Windows CRLF lên Linux sẽ báo lỗi khó hiểu `/usr/bin/env: ‘bash\r’: No such file or directory` hoặc `$'\r': command not found`. Hãy luôn đặt LF (`\n`) trong IDE hoặc chạy `sed -i 's/\r$//' script.sh` sau khi copy lên server.

---

## 5. Quan sát & đo đạc

### Phần A — Đã chạy kiểm chứng trên Windows 11 (Java 21 Bash Safety Auditor & JBoss Log Extractor)
Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```
**Output thực tế ghi nhận trên Windows 11 amd64 · OpenJDK 26.0.1 (`--release 21`):**
- `ShellSafetyTest`: **3/3 tests pass**, chứng minh:
  1. `BashScriptSafetyAuditor` quét tĩnh phát hiện đủ các lỗi trong `unsafe-vs-safe.sh` (đoạn mẫu unsafe thiếu `set -euo pipefail`, unquoted `$TARGET`, `cd` không kiểm tra, `>>` append không idempotent), trong khi `collect-jboss-diag.sh` đạt **0 findings** và dùng chuẩn LF (`\n`), không dính `\r\n` CRLF!
  2. `JbossLogDiagExtractor` trích xuất log `server.log` mẫu theo khung giờ (`14:00:00`..`14:05:00`), lọc đúng `ERROR`/`OutOfMemoryError` tương đương lệnh `awk`/`grep`, và chứng minh tính **idempotent**: chạy collector **2 lần liên tiếp** vào cùng thư mục đích tạo ra số lượng file và dung lượng byte giống hệt nhau (`exact byte match`), không nhân đôi dòng kết quả!

### Phần B — Cần kiểm chứng trên Linux / WSL (chưa chạy được trên máy Windows soạn bài)
> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker (và Git Bash MSYS2 trên môi trường sandbox bị chặn khởi tạo `NtCreateDirectoryObject`), nên các script bash thật dưới đây **chưa được thực thi trực tiếp qua GNU bash**:
- `bash -n scripts/collect-jboss-diag.sh` (kiểm tra syntax trên Linux bash).
- `bash scripts/collect-jboss-diag.sh -p <JAVA_PID> -l sample-jboss-server.log -o /tmp/diag-out -s 14:00:00 -e 14:05:00` (chạy 2 lần kiểm tra idempotent + `kill -3` / `/proc/$PID/limits` + `awk` thực tế).
- `bash scripts/unsafe-vs-safe.sh` (quan sát cách `set -e` và `-o pipefail` dừng script khi lệnh trong pipe thất bại).

---

## 6. Trade-offs & khi nào KHÔNG dùng

| Công cụ | Được gì | Mất gì | Dùng khi |
| :--- | :--- | :--- | :--- |
| **Bash (`set -euo pipefail`)** | Có sẵn 100% trên mọi Linux server, kết nối nhanh CLI (`ps`, `kill -3`, `awk`, `ss`) | Xử lý cấu trúc dữ liệu lồng nhau, JSON phức tạp, logic retry khó đọc & dễ bug quoting | Script vận hành < 150 dòng kết nối các lệnh OS/chẩn đoán/deploy đơn giản |
| **Python / Go / Java CLI** | Kiểu dữ liệu chặt, JSON/HTTP/YAML chuẩn, dễ viết unit test | Cần runtime hoặc binary biên dịch sẵn trên server | Logic nghiệp vụ phức tạp, xử lý JSON lớn, nhiều API call, script > 150 dòng |

---

## 7. Cạm bẫy production (ít nhất 3)

1. **Copy file `.sh` viết trên Windows (CRLF `\r\n`) lên Linux chạy** — *Triệu chứng:* báo lỗi `/usr/bin/env: 'bash\r': No such file or directory` hoặc biến chứa ký tự `\r` ẩn làm `cd`/`grep` sai hết. *Nguyên nhân:* Windows dùng `\r\n`, Linux chỉ hiểu `\n`. *Cách tránh:* cấu hình `.editorconfig` / Git (`*.sh text eol=lf`) và luôn kiểm tra `file script.sh`.
2. **Đặt `grep ...` trần ngoài `if` khi bật `set -e`** — *Triệu chứng:* script thu thập log đột ngột chết giữa chừng với exit code `1` dù server không có lỗi! *Nguyên nhân:* khi không khớp dòng nào, `grep` trả về exit code `1`, bị `set -e` hiểu là thất bại. *Cách tránh:* dùng `if grep -q ...; then ... fi` hoặc `grep ... || true` khi không tìm thấy là bình thường.
3. **Quên quote biến `"$VAR"` khi tên file có khoảng trắng hoặc rỗng** — *Triệu chứng:* `rm -rf $DIR/*` xóa nhầm khi `$DIR` chứa dấu cách hoặc biến rỗng (nếu thiếu `-u`). *Nguyên nhân:* bash tự Word Splitting theo `IFS`. *Cách tránh:* bật `set -u`, luôn quote `"$VAR"`, `"$@"`, và dùng `shellcheck` trước khi đưa lên server.
4. **Dùng `>>` append thẳng vào file config trong cron/Ansible/script chạy lại** — *Triệu chứng:* mỗi lần chạy thêm một bản sao tham số JVM hoặc cron entry, làm file config dài hàng trăm dòng trùng lặp. *Nguyên nhân:* thiết kế script không idempotent. *Cách tránh:* check tồn tại trước (`grep -qF`), dùng atomic temp file + `mv`, hoặc thư mục `.d/` tách từng file riêng.

---

## 8. Liên hệ thực tế công việc

Ngay tuần này trên server Linux chạy **JBoss AS 7.1.1**:
1. Mang `lab/scripts/collect-jboss-diag.sh` đặt vào thư mục runbook thử chạy (hoặc chạy thử với log mẫu trên môi trường test/staging) để có sẵn một công cụ 1-lệnh gom thread dump (`kill -3`) + FD count + error log trong 5 giây khi sự cố xảy ra.
2. Rà soát các file `.sh` vận hành hiện có ở công ty xem có file nào thiếu `set -euo pipefail`, thiếu quote `"$var"`, hoặc thiếu `trap` dọn `/tmp` không.

---

## 9. Self-check — Explain-back

> Trả lời **không nhìn tài liệu**, nói to trong 5 phút. Không trả lời được = chưa xong. (Đáp án nằm trong [solutions.md](./solutions.md).)

1. Giải thích từng chữ cái trong **`set -euo pipefail`**; điều gì xảy ra nếu thiếu từng cờ một (`-e`, `-u`, `-o pipefail`)?
2. Tại sao đặt `grep "FATAL" app.log` trần trong script đang bật `set -e` lại có thể làm script chết oan khi log sạch không có lỗi? Sửa thế nào?
3. Viết mẫu `trap` chuẩn để dọn `TMP_DIR` mà vẫn bảo toàn đúng mã thoát (`exit code`) gốc của script.
4. Phân biệt vai trò của `grep`, `awk`, `sed`, `jq` và ví dụ một lệnh `awk` lọc các dòng `ERROR` giữa hai mốc giờ trong `server.log`.
5. Nêu 4 kỹ thuật cốt lõi giúp một bash script có tính **idempotent** (chạy lặp lại 10 lần không gây hại).

---

## 10. Cheatsheet 1 trang

| Thành phần | Mẫu chuẩn Production |
| :--- | :--- |
| **Header bắt buộc** | `#!/usr/bin/env bash` + `set -euo pipefail` (lưu định dạng **LF**, không CRLF!) |
| **Cleanup an toàn** | `TMP="$(mktemp -d)"; cleanup() { local rc=$?; rm -rf "$TMP"; exit "$rc"; }; trap cleanup EXIT INT TERM` |
| **Tham số & Quote** | `${1:?Missing arg}` · `while getopts "p:o:" opt; do ...` · luôn dùng `"$var"`, `"$@"` |
| **`grep` với `set -e`** | `if grep -q "ERR" file; then ... fi` hoặc `count=$(grep -c "ERR" file \|\| true)` |
| **Idempotent & Lock** | `mkdir -p` · `ln -sfn` · `mv -f "$TMP/out" "$DEST"` · `flock -n 9` chống chạy chồng |
| **Lọc log JBoss (`awk`)** | `awk '$2 >= "14:00:00" && $2 <= "14:05:00" && /ERROR\|OutOfMemoryError/' server.log` |

---

## 11. Tài liệu tham khảo

- William E. Shotts, [*The Linux Command Line*](https://linuxcommand.org/tlcl.php) — Shell scripting, quoting, I/O redirection, `grep`/`sed`/`awk`.
- [GNU Bash Reference Manual — The `Set` Builtin (`errexit`, `nounset`, `pipefail`)](https://www.gnu.org/software/bash/manual/html_node/The-Set-Builtin.html) & [`trap` Builtin](https://www.gnu.org/software/bash/manual/html_node/Bourne-Shell-Builtins.html).
- [Google Shell Style Guide](https://google.github.io/styleguide/shellguide.html) — quy chuẩn quoting, `local`, error checking cho bash production.
- [ShellCheck Wiki](https://www.shellcheck.net/wiki/) — danh mục các lỗi kinh điển trong bash script (`SC2086`, `SC2164`, `SC2155`).

---
➡️ Tiếp theo: [exercises.md](./exercises.md) · [lab/](./lab/README.md) · Bài kế: [LNX-07 · select, poll, epoll & event loop](../07-epoll-io-multiplexing/README.md)
