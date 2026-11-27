# READ-03 · Lab — Đọc source Guava Cache / Caffeine

> **Mục tiêu:** benchmark trực tiếp hit-rate của Guava Cache (Segmented LRU) vs Caffeine (W-TinyLFU) trên trace Zipf + cold scan, đo sự khác biệt độ trễ giữa `expireAfterWrite` vs `refreshAfterWrite`, kiểm chứng per-key coalescing chống stampede, và soi cấu trúc nội tại bằng `javap`.
> **Thời lượng:** ~45 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |

> Lab dùng Guava `33.3.1-jre` và Caffeine `3.1.8`, chạy hoàn toàn offline trên localhost không cần Docker. Chạy được trên **Windows (PowerShell / Git Bash) và Linux/macOS**.

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Guava 33.3.1-jre + Caffeine 3.1.8 + JUnit 5
├── src/
│   ├── main/java/lab/codereading/cache/
│   │   └── CacheWalkthroughRunner.java      # 3 thí nghiệm: Zipf, Expire vs Refresh, Stampede
│   └── test/java/lab/codereading/cache/
│       └── CacheWalkthroughTest.java        # JUnit 5 kiểm chứng coalescing & async refresh
└── solutions/
    └── E7GuavaAsyncReloadTest.java          # Lời giải E7 cho Guava async reload()
```

## 2. Chạy

Từ thư mục `lab/`:
```bash
mvn -q verify
mvn -q compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Chạy test chống stampede & `refreshAfterWrite`
- Làm gì: chạy `mvn -q verify`.
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Khi 12 thread đồng thời gọi `cache.get("key-1")` trên cả Guava `LoadingCache` lẫn Caffeine `LoadingCache`, hàm `load()` chạy bao nhiêu lần?
  2. Khi vừa qua `refreshAfterWrite(80ms)` trên Caffeine với loader mất `100ms`, lời gọi `cache.get("k")` đầu tiên trả về `"v1"` hay `"v2"`, và mất dưới bao nhiêu ms?

### Bước 2 — Chạy benchmark Zipf + Cold Scan & `expireAfterWrite` vs `refreshAfterWrite`
- Làm gì: chạy `mvn -q compile exec:java`.
- **Dự đoán trước khi chạy**:
  1. Với `maximumSize = 200` trên trace Zipf `2.000` key xen kẽ quét lạnh 40 key mỗi 200 lượt, Guava (LRU) vs Caffeine (W-TinyLFU) chênh lệch hit-rate khoảng bao nhiêu %?
  2. Với `getIfPresent + put` tự viết trên 16 thread cùng miss một key lạnh, loader bị gọi bao nhiêu lần?

### Bước 3 — Soi bytecode nội tại bằng `javap`
- Làm gì:
  ```bash
  javap -p -cp ~/.m2/repository/com/google/guava/guava/33.3.1-jre/guava-33.3.1-jre.jar 'com.google.common.cache.LocalCache$Segment'
  javap -p -cp ~/.m2/repository/com/github/ben-manes/caffeine/caffeine/3.1.8/caffeine-3.1.8.jar com.github.benmanes.caffeine.cache.BoundedLocalCache
  ```
- Quan sát: `LocalCache$Segment extends ReentrantLock` vs `BoundedLocalCache` giữ `ConcurrentHashMap data`, `readBuffer`, `writeBuffer`, `evictionLock`, và 3 hàng đợi `Window`/`Probation`/`Protected`.

## 4. Kết quả của bạn

| Thí nghiệm | Guava Cache | Caffeine | Ghi chú |
| :--- | :--- | :--- | :--- |
| `[1]` Zipf + cold scan (`max=200`) | hitRate = ? \| time = ? ms | hitRate = ? \| time = ? ms | |
| `[2]` `expire` vs `refresh` (8 threads) | — | expire: ? ms \| refresh: ? ms | |
| `[3]` 16 threads cold miss | `LoadingCache`: ? \| naive: ? | `LoadingCache`: ? \| naive: ? | |

> **Số tham khảo của người soạn** *(Windows 11 amd64, 16 CPUs, JDK 26.0.1 `--release 21`, Guava 33.3.1-jre, Caffeine 3.1.8, ngày 2026-11-27)*:
> - `mvn verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `[1]` Zipf + cold scan: Guava **49,18%** (`29.509` hits / `30.491` misses, `331 ms`) vs Caffeine **96,24%** (`57.745` hits / `2.255` misses, `220 ms`).
> - `[2]` `expireAfterWrite(100ms)`: 8 callers chờ `133–134 ms` (`loads=2`). `refreshAfterWrite(100ms)`: 8 callers nhận stale ngay trong `0–4 ms`, giá trị sau background reload là `v-2`.
> - `[3]` `LoadingCache.get(K)`: **1 lần** loader cho 16 thread; naive `getIfPresent + put`: **16 lần** loader.

## 5. Giải thích kết quả
- W-TinyLFU dùng `Window` + `FrequencySketch` lọc bỏ các key quét 1 lần trước khi cho vào Main cache nên giữ được hit-rate 96,24%, trong khi LRU thuần của Guava bị 40 key lạnh mỗi chu kỳ đẩy mất hot key xuống còn 49,18%.
- Caffeine mặc định chạy `refreshAfterWrite` bất đồng bộ qua `executor`, giúp trả ngay giá trị cũ (`0–4 ms`) mà không chặn thread gọi như `expireAfterWrite` (`133–134 ms`). Chi tiết xem [README §3](../README.md).

## 6. Biến thể để tự khám phá
1. Bỏ điều kiện `(i % 200) < 40` trong `generateZipfPlusScanTrace` (chỉ giữ Zipf thuần) và chạy lại để thấy khoảng cách hit-rate giữa Guava vs Caffeine thu hẹp ra sao khi không có cold scan.
2. Copy `solutions/E7GuavaAsyncReloadTest.java` vào `src/test/java/lab/codereading/cache/` và chạy `mvn -q verify` để thấy tận mắt Guava `refreshAfterWrite` mặc định chạy đồng bộ nếu không override `reload()`.

## 7. Dọn dẹp
```bash
mvn -q clean
```
