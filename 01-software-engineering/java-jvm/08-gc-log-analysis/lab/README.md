# Lab JVM-08 · Đọc GC log

## Mục tiêu

Sinh hai file `-Xlog:gc*` thật từ cùng 1 ứng dụng chạy ở chế độ **`normal`** (churn cao nhưng không giữ rác) và **`leak`** (rò rỉ vào `static List`), sau đó phân tích định lượng: số lần Full GC, `p95`/`max` pause, GC throughput %, Allocation Rate (`MB/s`), và đường đáy `live-set floor`.

## Yêu cầu môi trường

- JDK 21+ (đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+

## Cấu trúc thư mục

- `pom.xml` — cấu hình Maven (`--release 21`, JUnit 5, AssertJ)
- `src/main/java/lab/jvm/gclog/GcWorkloadRunner.java` — workload sinh `normal` hoặc `leak`
- `src/main/java/lab/jvm/gclog/GcLogAnalyzer.java` — parser đọc `-Xlog:gc*` thuần Java, xuất báo cáo triage
- `src/test/java/lab/jvm/gclog/GcLogAnalyzerTest.java` — unit test kiểm chứng pattern khoẻ vs leak
- `solutions/LogTriageSolution.java` — tool so sánh 2 file GC log cho bài L3

## Cách chạy

### 1. Chạy unit test

```powershell
mvn -o test
```

### 2. Sinh GC log thật & phân tích bằng `GcLogAnalyzer` (PowerShell)

```powershell
mvn -o -q compile

$nLog = "$env:TEMP\jvm08-normal.log"
$lLog = "$env:TEMP\jvm08-leak.log"

java -Xmx192m "-XX:+UseG1GC" "-Xlog:gc*:file=$nLog" -cp target\classes lab.jvm.gclog.GcWorkloadRunner normal 24000
java -Xmx192m "-XX:+UseG1GC" "-Xlog:gc*:file=$lLog" -cp target\classes lab.jvm.gclog.GcWorkloadRunner leak 24000

java -cp target\classes lab.jvm.gclog.GcLogAnalyzer $nLog $lLog
java -cp target\classes solutions\LogTriageSolution.java $nLog $lLog

Remove-Item $nLog,$lLog -ErrorAction SilentlyContinue
```

### 3. Dọn artifact build sau khi xong

```powershell
mvn -o -q clean
```

## Kết quả mẫu (đã chạy trên Windows 11 · OpenJDK 26.0.1 · `-Xmx192m -XX:+UseG1GC`)

```text
NORMAL : events=32 (full=0) | duration=0.232s | totalPause=22.45ms | p95=1.39ms | max=2.72ms | throughput=90.32% | allocRate=6952.6 MB/s | floor=26MB->26MB | leakSuspected=false
LEAK   : events=266 (full=9) | duration=0.396s | totalPause=149.48ms | p95=1.74ms | max=8.95ms | throughput=62.25% | allocRate=4103.5 MB/s | floor=169MB->177MB | leakSuspected=true
Delta live floor growth: normal=+0 MB vs leak=+8 MB
```
