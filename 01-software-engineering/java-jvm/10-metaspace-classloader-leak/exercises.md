# Bài tập · JVM-10 · Metaspace & ClassLoader leak

Làm bài trước khi mở [solutions.md](solutions.md). Thực hành trên [lab/](lab/README.md).

---

## Level 1 · Hiểu cơ chế (4 bài)

### Bài 1.1 · 3 điều kiện để Class Unloading xảy ra
Liệt kê **đầy đủ 3 điều kiện bắt buộc** để JVM thu hồi metadata của một class `OrderMapper` khỏi Metaspace. Nếu chỉ xoá mọi biến instance của `OrderMapper` nhưng vẫn còn một `Timer` thread giữ reference tới một class khác `HealthCheck` nằm chung trong `app.war`, liệu `OrderMapper` có được unload không? Vì sao?

### Bài 1.2 · Tại sao `WeakReference` trong `ThreadLocalMap` không cứu được?
Vẽ hoặc mô tả chuỗi tham chiếu từ `jboss-http-worker` thread tới `IsolatedWarClassLoader` trong trường hợp developer tạo `private static final ThreadLocal<RequestCtx> CTX = new ThreadLocal<>();` bên trong class thuộc `app.war` và quên gọi `.remove()`. Chỉ rõ mắt xích nào là *Weak* và mắt xích nào là *Strong*.

### Bài 1.3 · PermGen (Java 7) vs Metaspace (Java 8–21+)
Hoàn thành bảng so sánh nhanh giữa PermGen và Metaspace: vị trí bộ nhớ, cờ giới hạn dung lượng, hành vi khi không đặt giới hạn tối đa trên container Docker/Kubernetes.

### Bài 1.4 · Chẩn đoán nhanh bằng `jcmd`
Khi thấy OS báo tiến trình Java ăn tăng thêm 800 MB RAM nhưng `-Xlog:gc*` lại cho thấy Java Heap chỉ dùng 400 MB / `-Xmx2g`, bạn chạy lệnh `jcmd` nào để kiểm tra **Metaspace** và số lượng **ClassLoader / Loaded Classes**?

<details>
<summary>Gợi ý</summary>

Nhớ lại `jcmd <pid> help` ở bài JVM-04/09: kiểm tra lệnh `VM.metaspace` (hoặc `VM.native_memory summary` nếu đã bật NMT) và `GC.class_histogram`.

</details>

---

## Level 2 · Thực hành lab tái hiện leak (3 bài)

### Bài 2.1 · Chạy unit test & đọc sự khác biệt `clean` vs `leak`
Chạy `mvn -o test` trong `10-metaspace-classloader-leak/lab/`. Đọc [`RedeployLeakSimulator.java`](lab/src/main/java/lab/jvm/metaspace/RedeployLeakSimulator.java) và giải thích:
1. Tại sao `IsolatedWarClassLoader` không cần sửa bytecode `.class` vẫn khiến JVM cấp phát metadata mới cho mỗi lần redeploy?
2. Tại sao sau khi kết thúc mỗi vòng redeploy, tác giả gán `warLoader = null; workerClass = null;` nhưng chế độ `leak` vẫn có `unloadedClasses = 0`?

### Bài 2.2 · Đo tốc độ phình Metaspace giữa `clean` và `leak`
Biên dịch lab (`mvn -o -q compile`) và chạy hai lệnh sau:

```powershell
java "-XX:MaxMetaspaceSize=32m" -cp target\classes lab.jvm.metaspace.RedeployLeakSimulator clean 600
java "-XX:MaxMetaspaceSize=32m" -cp target\classes lab.jvm.metaspace.RedeployLeakSimulator leak 2000
```

So sánh 3 chỉ số `loadedClasses`, `unloadedClasses`, và `metaspace (MB)` giữa 2 lần chạy.

### Bài 2.3 · Chuyển ngữ cảnh JBoss AS 7.1.1 (Java 7)
Một team vận hành JBoss AS 7.1.1 (Java 7) phản ánh: cứ bấm redeploy `portal.war` tới lần thứ 5 trong ngày là JBoss treo với `OutOfMemoryError: PermGen space`. Đưa ra **3 nguyên nhân phổ biến nhất** cần rà soát trong `portal.war` và **2 biện pháp** cấu hình/vận hành trên JBoss.

<details>
<summary>Gợi ý</summary>

Kiểm tra thư viện JDBC trong `WEB-INF/lib`, `ThreadLocal` filter/interceptor, và background `Timer`/`ExecutorService` trong `ServletContextListener`.

</details>

---

## Level 3 · Tái hiện `OOM: Metaspace` & chuẩn hoá mẫu guard (1 bài)

### Bài 3.1 · Ép chạm trần `-XX:MaxMetaspaceSize=20m` và viết wrapper an toàn
**Yêu cầu**:
1. Chạy `RedeployLeakSimulator` với `-XX:MaxMetaspaceSize=20m` cho chế độ `clean 3000` và `leak 3000`. Xác nhận `clean 3000` sống khoẻ ở ~1.45 MB (`hitOOM=false`), còn `leak 3000` sập với `java.lang.OutOfMemoryError: Metaspace`.
2. Đọc và chạy mẫu chuẩn [`lab/solutions/CleanThreadLocalFilter.java`](lab/solutions/CleanThreadLocalFilter.java), sau đó viết một helper `doInContext(...)` của riêng bạn chứng minh khi chạy trên `Executors.newFixedThreadPool(2)`, dù `businessLogic` ném `RuntimeException` giữa chừng thì worker thread vẫn có `ThreadLocal.get() == null` sau khi kết thúc.

**Tiêu chí đạt**: Tái hiện được `OutOfMemoryError: Metaspace` ở chế độ `leak`, giữ nguyên `~1.45 MB` ở chế độ `clean 3000`, và worker thread sạch `ThreadLocal` kể cả khi tác vụ ném exception.
