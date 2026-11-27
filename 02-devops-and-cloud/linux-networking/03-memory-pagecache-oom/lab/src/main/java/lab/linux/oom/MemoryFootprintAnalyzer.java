package lab.linux.oom;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Đo thực tế Heap vs Off-Heap Direct ByteBuffer trên JVM đang chạy qua MXBeans,
 * chứng minh phần bộ nhớ không nằm trong -Xmx nhưng vẫn cộng thẳng vào RSS OS.
 */
public final class MemoryFootprintAnalyzer {

    public record Snapshot(long heapUsedBytes, long nonHeapUsedBytes, long directBufferBytes) {}

    public Snapshot captureSnapshot() {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        long heapUsed = mem.getHeapMemoryUsage().getUsed();
        long nonHeapUsed = mem.getNonHeapMemoryUsage().getUsed();
        long directUsed = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class).stream()
                .filter(pool -> "direct".equals(pool.getName()))
                .mapToLong(BufferPoolMXBean::getMemoryUsed)
                .sum();
        return new Snapshot(heapUsed, nonHeapUsed, directUsed);
    }

    public List<ByteBuffer> allocateDirectMegabytes(int megabytes) {
        List<ByteBuffer> buffers = new ArrayList<>(megabytes);
        for (int i = 0; i < megabytes; i++) {
            ByteBuffer buf = ByteBuffer.allocateDirect(1024 * 1024);
            // Chạm từng page 4KB để OS thực sự commit physical frame vào RSS (demand paging)
            for (int pos = 0; pos < buf.capacity(); pos += 4096) {
                buf.put(pos, (byte) 1);
            }
            buffers.add(buf);
        }
        return buffers;
    }
}
