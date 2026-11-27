# Lab JVM-07 · ZGC / Shenandoah & Low-Latency GC

## Mục tiêu

Đo trực tiếp tail latency (`p50` / `p99` / `max` jitter của các probe 1ms) khi JVM giữ **2.000.000 live objects (~250+ MB live graph)** đồng thời chịu áp lực cấp phát liên tục từ thread nền, so sánh giữa **G1GC** và **ZGC** ở cùng `-Xmx1g`.

## Yêu cầu môi trường

- JDK 21+ (lab đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+

## Cấu trúc thư mục

- `pom.xml` — cấu hình Maven (`--release 21`, JUnit 5, AssertJ)
- `src/main/java/lab/jvm/zgc/LatencyComparisonRunner.java` — workload live set + background allocator + 2.000 periodic 1ms probes
- `src/test/java/lab/jvm/zgc/ZgcLabTest.java` — unit test kiểm tra GC MXBean runtime
- `solutions/AllocationStallDemo.java` — lời giải tham khảo cho bài L3 (tái hiện Allocation Stall khi heap quá sát live set)

## Cách chạy

### 1. Chạy unit test

```powershell
mvn -o test
```

### 2. Biên dịch và chạy so sánh G1GC vs ZGC (JDK 21 / JDK 24+)

```powershell
mvn -o -q compile

# Run 1: G1GC
java -Xmx1g -XX:+UseG1GC "-Xlog:gc" -cp target\classes lab.jvm.zgc.LatencyComparisonRunner

# Run 2: ZGC (JDK 21 dùng thêm -XX:+ZGenerational; từ JDK 24+ Generational là mặc định)
java -Xmx1g -XX:+UseZGC "-Xlog:gc" -cp target\classes lab.jvm.zgc.LatencyComparisonRunner
```

> Lưu ý trên Windows PowerShell: tham số chứa dấu `:` hoặc `+` nên đặt trong dấu nháy kép (`"-Xlog:gc*"`) để tránh PowerShell tách chuỗi.

### 3. Xem chi tiết STW pause thật sự của ZGC (`Pause Mark Start` / `Pause Relocate Start`)

```powershell
java -Xmx1g -XX:+UseZGC "-Xlog:gc*" -cp target\classes lab.jvm.zgc.LatencyComparisonRunner
```

### 4. Dọn artifact build sau khi xong

```powershell
mvn -o -q clean
```

## Kết quả mẫu (đã chạy trên máy Windows 11 · OpenJDK 26.0.1 · `-Xmx1g`)

| Collector | Live set | Probe `p50` | Probe `p99` | Probe `max` | Ghi chú từ `-Xlog:gc` |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **G1GC** (`-XX:+UseG1GC`) | 2.000.000 | `986 us` | `27668 us` | `41848 us` (`41.85 ms`) | Bị `Evacuation Failure: Allocation` + `Pause Full (G1 Compaction Pause) 1023M->354M(1024M) 28.486ms` |
| **ZGC** (`-XX:+UseZGC`) | 2.000.000 | `988 us` | `2474 us` | `2653 us` (`2.65 ms`) | `Minor Collection` chạy nền `0.004s–0.005s`; jitter ~2.5 ms chủ yếu do OS `Thread.sleep(1)` granularity trên Windows |
