# Lab JVM-10 · Metaspace & ClassLoader leak

## Mục tiêu

Tái hiện trực tiếp nguyên nhân **`java.lang.OutOfMemoryError: Metaspace`** khi redeploy nhiều lần trên kiến trúc App Server (JBoss / Tomcat): một thread pool cố định tái sử dụng 4 worker threads, mỗi lần redeploy tạo một `IsolatedWarClassLoader` mới để nạp [`DynWorkerTemplate`](src/main/java/lab/jvm/metaspace/DynWorkerTemplate.java).

- **`leak` mode**: bỏ quên `ThreadLocal.remove()` → `Entry.value` giữ chặt `DynWorkerTemplate` → giữ nguyên `ClassLoader` cũ trong Metaspace (`unloadedClasses = 0`).
- **`clean` mode**: luôn gọi `ThreadLocal.remove()` trong `finally` → `ClassLoader` cũ unreachable và bị **unload** ngay khi GC chạy.

## Yêu cầu môi trường

- JDK 21+ (đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+

## Cấu trúc thư mục

- `pom.xml` — cấu hình Maven (`--release 21`, JUnit 5, AssertJ)
- `src/main/java/lab/jvm/metaspace/DynWorkerTemplate.java` — class giả lập ứng dụng trong WAR được nạp bởi mỗi `ClassLoader` riêng
- `src/main/java/lab/jvm/metaspace/RedeployLeakSimulator.java` — simulator chạy vòng lặp redeploy `clean` vs `leak` trên fixed thread pool
- `src/test/java/lab/jvm/metaspace/MetaspaceLeakTest.java` — unit test khẳng định `clean` unload class còn `leak` tích tụ class
- `solutions/CleanThreadLocalFilter.java` — mẫu guard `try / finally { remove(); }` cho bài L3

## Cách chạy

### 1. Chạy unit test

```powershell
mvn -o test
```

### 2. So sánh `clean` vs `leak` trên `-XX:MaxMetaspaceSize=32m` & kích nổ `OOM: Metaspace` ở `20m`

```powershell
mvn -o -q compile

# Quan sát số lượng unloadedClasses & Metaspace (32m)
java "-XX:MaxMetaspaceSize=32m" -cp target\classes lab.jvm.metaspace.RedeployLeakSimulator clean 600
java "-XX:MaxMetaspaceSize=32m" -cp target\classes lab.jvm.metaspace.RedeployLeakSimulator leak 2000

# Ép trần 20m để thấy rõ clean 3000 sống khoẻ còn leak 3000 nổ OOM: Metaspace
java "-XX:MaxMetaspaceSize=20m" -cp target\classes lab.jvm.metaspace.RedeployLeakSimulator clean 3000
java "-XX:MaxMetaspaceSize=20m" -cp target\classes lab.jvm.metaspace.RedeployLeakSimulator leak 3000
```

### 3. Chạy mẫu giải pháp L3 & dọn artifact build

```powershell
java solutions\CleanThreadLocalFilter.java
mvn -o -q clean
```

## Kết quả mẫu (đã chạy trên Windows 11 · OpenJDK 26.0.1)

| Kịch bản | Kết quả |
| :--- | :--- |
| `clean 600` (`-XX:MaxMetaspaceSize=32m`) | `loadedClasses=1044 \| unloadedClasses=600 \| metaspace=1.45 MB \| hitOOM=false` |
| `leak 2000` (`-XX:MaxMetaspaceSize=32m`) | `loadedClasses=3044 \| unloadedClasses=0 \| metaspace=15.89 MB \| hitOOM=false` |
| `clean 3000` (`-XX:MaxMetaspaceSize=20m`) | `loadedClasses=1041 \| unloadedClasses=3000 \| metaspace=1.45 MB \| hitOOM=false` |
| `leak 3000` (`-XX:MaxMetaspaceSize=20m`) | `Exception in thread "main" java.lang.OutOfMemoryError: Metaspace` |
| `CleanThreadLocalFilter.java` | `Worker ThreadLocal after request cleanup = null` |
