# Lab JVM-09 · Heap dump, Eclipse MAT, JFR

## Mục tiêu

Thực hành chụp **live heap dump (`.hprof`)** bằng `jcmd <pid> GC.heap_dump` và thu thập **Java Flight Recorder (`.jfr`)** bằng `jcmd <pid> JFR.start/dump` trên cùng một ứng dụng đang mắc 3 bệnh: rò rỉ static `ConcurrentHashMap`, nóng cấp phát `byte[16KB]`, và tranh chấp `synchronized`.

## Yêu cầu môi trường

- JDK 21+ (đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+
- Tùy chọn xem GUI: Eclipse Memory Analyzer (MAT) & JDK Mission Control (JMC) — không bắt buộc vì lab đã có [`JfrInspector`](src/main/java/lab/jvm/matjfr/JfrInspector.java) đọc trực tiếp file `.jfr` từ CLI.

## Cấu trúc thư mục

- `pom.xml` — cấu hình Maven (`--release 21`, JUnit 5, AssertJ)
- `src/main/java/lab/jvm/matjfr/LeakyCacheApp.java` — app demo 3 lỗi: static Map leak + hot allocator + monitor lock
- `src/main/java/lab/jvm/matjfr/JfrInspector.java` — CLI parser đọc file `.jfr` bằng `jdk.jfr.consumer.RecordingFile`
- `src/test/java/lab/jvm/matjfr/HeapMatJfrTest.java` — unit test verify tạo `.hprof` (`JAVA PROFILE`) + `.jfr` events
- `solutions/FixedCacheAuditApp.java` — bản sửa tham khảo cho bài L3

## Cách chạy

### 1. Chạy unit test

```powershell
mvn -o test
```

### 2. Chạy `LeakyCacheApp` và gắn `jcmd` lấy `.hprof` + `.jfr` (PowerShell)

```powershell
mvn -o -q compile

$hprof = "$env:TEMP\jvm09-live.hprof"
$jfr   = "$env:TEMP\jvm09-live.jfr"
Remove-Item $hprof,$jfr -ErrorAction SilentlyContinue

# Chạy app nền trong 4.5 giây với 200 entry rò rỉ (~25 MB)
$p = Start-Process java -ArgumentList "-Xmx512m","-cp","target\classes","lab.jvm.matjfr.LeakyCacheApp","4500","200" -PassThru -NoNewWindow
Start-Sleep -Milliseconds 1300

# Chụp Heap Dump & JFR Recording bằng jcmd
jcmd $p.Id JFR.start name=lab settings=profile
jcmd $p.Id GC.heap_dump $hprof
Start-Sleep -Milliseconds 1200
jcmd $p.Id JFR.dump name=lab filename=$jfr
jcmd $p.Id JFR.stop name=lab
$p.WaitForExit()

# Đọc JFR từ CLI (có thể mở thêm $hprof bằng Eclipse MAT hoặc $jfr bằng JMC)
java -cp target\classes lab.jvm.matjfr.JfrInspector $jfr

# Xoá file dump tạm sau khi phân tích
Remove-Item $hprof,$jfr -ErrorAction SilentlyContinue
```

### 3. Chạy bản sửa tham khảo L3 & dọn artifact build

```powershell
java solutions\FixedCacheAuditApp.java
mvn -o -q clean
```

## Kết quả mẫu (đã chạy trên Windows 11 · OpenJDK 26.0.1)

- `GC.heap_dump`: `Heap dump file created [43916429 bytes in 0.045 secs]` (với 300 entry rò rỉ).
- `JfrInspector`:
```text
JFR Summary -> totalEvents=3847 | allocEvents=262 | monitorEnterEvents=125
Top Allocation Hot Methods:
  lab.jvm.matjfr.LeakyCacheApp#allocateTempAuditPayload -> 59860272 samples/bytes
  ...
Top Contended Lock Methods (total duration ns):
  lab.jvm.matjfr.LeakyCacheApp#contendedAuditLock -> 2332.25 ms
```
- `FixedCacheAuditApp`: `Fixed cache size=64 (max=64) | sink=42`.
