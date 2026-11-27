# Đáp án · JVM-10 · Metaspace & ClassLoader leak

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · 3 điều kiện để Class Unloading xảy ra
JVM chỉ được phép unload `OrderMapper` khi đồng thời thoả:
1. Không còn instance nào của `OrderMapper` còn reachable.
2. Không còn tham chiếu nào trỏ tới đối tượng `OrderMapper.class` (`java.lang.Class<?>`).
3. Chính **`ClassLoader`** đã nạp `OrderMapper` phải trở thành unreachable.

**Trả lời tình huống**: **Không**, `OrderMapper` vẫn **không** được unload! Vì `HealthCheck` thuộc cùng `WebappClassLoader` (`app.war`), nên khi `Timer` còn giữ instance `HealthCheck`, chuỗi `HealthCheck -> HealthCheck.class -> WebappClassLoader -> classes list (chứa OrderMapper.class)` vẫn giữ sống toàn bộ `WebappClassLoader` và mọi class nó từng nạp.

#### Bài 1.2 · Chuỗi tham chiếu gây leak qua `ThreadLocalMap`
```text
Worker Thread (Pool -> Strong Ref)
  -> threadLocals (ThreadLocalMap)
    -> Entry (WeakRef key -> CTX | STRONG ref value -> RequestCtx instance)
      -> RequestCtx.getClass() (Strong Ref)
        -> WebappClassLoader (Strong Ref -> giữ toàn bộ 3000 class trong Metaspace!)
          -> Class holding static CTX -> (Strong Ref ngược về key CTX!)
```
- **Mắt xích Weak**: trường `referent` bên trong `Entry` trỏ tới object `ThreadLocal<?> CTX`.
- **Mắt xích Strong**: trường `Entry.value` trỏ tới `RequestCtx` (do thread pool không chết nên `Entry` vẫn nằm trong mảng `table` của `ThreadLocalMap`), và từ `RequestCtx` đi ngược về `WebappClassLoader` rồi trỏ thẳng lại `static CTX`, vô hiệu hoá luôn cơ chế WeakReference của key!

#### Bài 1.3 · Bảng so sánh PermGen vs Metaspace

| Tiêu chí | PermGen (Java ≤ 7) | Metaspace (Java 8–21+) |
| :--- | :--- | :--- |
| **Vị trí** | Nằm liền vùng Heap JVM | **Native memory** (ngoài Heap) |
| **Cờ giới hạn** | `-XX:MaxPermSize` | `-XX:MaxMetaspaceSize` |
| **Không đặt giới hạn trên container** | Vẫn có trần mặc định nhỏ (~64–82 MB), tự văng `OOM: PermGen space` | Unlimited mặc định → phình tới khi Linux **OOM Killer** kill container (`Exit 137`) trước khi kịp sinh heap dump |

#### Bài 1.4 · Lệnh `jcmd` kiểm tra Metaspace
```powershell
jcmd <pid> VM.metaspace
jcmd <pid> VM.classloader_stats
```
Nếu đã bật `-XX:NativeMemoryTracking=summary`, chạy thêm `jcmd <pid> VM.native_memory summary` để xác nhận phần `Class` (Metaspace + Compressed Class Space) có khớp với 800 MB RAM tăng thêm không.

---

### Level 2

#### Bài 2.1 · Cơ chế trong `RedeployLeakSimulator`
1. Trong JVM Spec, danh tính runtime của một class là cặp **`(ClassLoader instance, binary name)`**. Mỗi vòng redeploy tạo một `new IsolatedWarClassLoader(...)` riêng biệt và gọi `defineClass("lab.jvm.metaspace.DynWorkerTemplate", ...)`, nên JVM xem mỗi cái là một runtime `Class<?>` hoàn toàn mới nằm ở vùng Metaspace riêng thuộc `ClassLoader` mới đó.
2. Ở chế độ `leak`, mặc dù `warLoader` và `workerClass` trong hàm `main` đã bị gán `null`, bốn thread trong `serverPool` (`jboss-http-worker`) vẫn **sống** và `ThreadLocalMap` của từng thread vẫn giữ strong reference `Entry.value` tới instance `DynWorkerTemplate` vừa chạy! Điều này neo chặt `IsolatedWarClassLoader` không cho GC đụng vào (`unloadedClasses = 0`).

#### Bài 2.2 · Số đo đối chiếu `clean` vs `leak` (OpenJDK 26.0.1, `-XX:MaxMetaspaceSize=32m`)
- **`clean` (`600` redeploys)**: `loadedClasses=1044 | unloadedClasses=600 | metaspace=1.45 MB | hitOOM=false` — số class live luôn ổn định quanh mức nền.
- **`leak` (`2000` redeploys)**: `loadedClasses=3044 | unloadedClasses=0 | metaspace=15.89 MB | hitOOM=false` — 2000 bản sao `DynWorkerTemplate` bị ghim vĩnh viễn làm Metaspace tăng gấp ~11 lần!

#### Bài 2.3 · Rà soát JBoss AS 7.1.1 (Java 7)
**3 nguyên nhân phổ biến cần kiểm tra trong `portal.war`:**
1. Nhúng JDBC driver (`ojdbc6.jar` / `sqljdbc4.jar`) vào `WEB-INF/lib` khiến `DriverManager` của hệ thống giữ chặt driver class của WAR cũ.
2. `ThreadLocal` quên gọi `.remove()` trong filter/interceptor hoặc quên dọn thư viện giữ `SimpleDateFormat` / user context trên JBoss worker thread pool.
3. Background `Timer`, Quartz scheduler, hoặc `ExecutorService` khởi tạo lúc startup nhưng không gọi `.shutdown()` trong `ServletContextListener.contextDestroyed()`.

**2 biện pháp cấu hình / vận hành JBoss:**
1. Tách JDBC driver thành JBoss global module & cấu hình `<datasource>` ở mức server thay vì bỏ vào WAR; bật `-XX:+CMSClassUnloadingEnabled` (nếu chạy CMS trên Java 7) và `-XX:MaxPermSize=256m`.
2. Trên production, dùng **rolling restart** JBoss JVM khi release thay vì lạm dụng hot-redeploy nhiều lần trên cùng một process.

---

### Level 3

#### Bài 3.1 · Ép `OOM: Metaspace` và viết guard an toàn cho exception
Kết quả chạy thực tế trên máy lab với `-XX:MaxMetaspaceSize=20m`:
```text
=== Clean 3000 (-XX:MaxMetaspaceSize=20m) ===
mode=clean | redeploys=3000 | loadedClasses=1041 | unloadedClasses=3000 | metaspace=1.45 MB | hitOOM=false
=== Leak 3000 (-XX:MaxMetaspaceSize=20m) ===
Exception in thread "main" java.lang.OutOfMemoryError: Metaspace
```

Mẫu bảo vệ chuẩn cho mọi `ThreadLocal` trên thread pool (kể cả khi `businessLogic` ném `RuntimeException`) xem ở [`lab/solutions/CleanThreadLocalFilter.java`](lab/solutions/CleanThreadLocalFilter.java): khối `try { ... } finally { TENANT_CONTEXT.remove(); }` luôn chạy trước khi thread quay lại pool, cho kết quả `Worker ThreadLocal after request cleanup = null`.

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. 3 điều kiện bắt buộc để JVM unload một Class&lt;?&gt;?</summary>

(1) Không còn instance nào của class đó reachable; (2) Không còn tham chiếu trực tiếp tới object `Class<?>` đó (như reflection cache); (3) Chính `ClassLoader` đã nạp ra nó trở nên unreachable.

</details>

<details>
<summary>2. Tại sao ThreadLocalMap dùng WeakReference vẫn gây leak nặng trong thread pool?</summary>

Vì chỉ **key** (`ThreadLocal<?>`) là `WeakReference`, còn **value** trong `Entry` là **Strong Reference**. Trong thread pool, worker thread sống mãi nên `ThreadLocalMap` không bao giờ biến mất; ngoài ra nếu `value` gián tiếp giữ `Class -> static ThreadLocal key`, ngay cả key yếu cũng bị neo cứng không dọn được.

</details>

<details>
<summary>3. Hai nguyên nhân ngoài ThreadLocal gây OOM PermGen khi hot-redeploy JBoss AS 7.1.1?</summary>

(1) Đặt JDBC driver trong `WEB-INF/lib` để `java.sql.DriverManager` (Bootstrap CL) giữ reference; (2) Khởi tạo background thread / `Timer` / `ExecutorService` hoặc đăng ký JMX MBean lúc startup WAR nhưng quên tắt/gỡ bỏ trong `contextDestroyed()`.

</details>

<details>
<summary>4. Rủi ro giữa bỏ trống `-XX:MaxMetaspaceSize` vs đặt vừa đủ?</summary>

Bỏ trống trên container Docker/K8s làm Metaspace phình không phanh ăn sạch RAM native dẫn tới bị Linux **OOM Killer** (`Exit 137`) giết ngang không log Java rõ ràng. Đặt vừa đủ (`~1.5x–2x` baseline đo được bằng `jcmd VM.metaspace`) biến nó thành cầu chì báo đúng `OutOfMemoryError: Metaspace`.

</details>

<details>
<summary>5. Kiểm tra nhanh app đang leak Heap hay Metaspace bằng `jcmd`?</summary>

Chạy `jcmd <pid> GC.heap_info` (hoặc nhìn `-Xlog:gc*`) để xem Java Heap có chạm `-Xmx` không, đồng thời chạy `jcmd <pid> VM.metaspace` và `jcmd <pid> VM.classloader_stats` để kiểm tra dung lượng `Metaspace used/committed` và số `ClassLoader` còn sống.

</details>
