package lab.jvm.memory;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive lab for JVM-04:
 *  1) JOL object header & field layout for Integer, Boolean, padded POJO, int[1000] vs Integer[1000]
 *  2) Off-heap ByteBuffer.allocateDirect(64MB) vs Heap limit
 *  3) Live Native Memory Tracking (NMT) snapshot via jcmd if -XX:NativeMemoryTracking=summary is active
 */
public class MemoryLayoutRunner {

    static final class CompactSample {
        boolean flag;
        int count;
        long timestamp;
        String label;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== JVM-04 Lab: JVM Memory Areas, Object Layout & NMT ===");
        System.out.println(VM.current().details());

        System.out.println("[1] Object layouts via JOL:");
        System.out.println(ClassLayout.parseClass(Integer.class).toPrintable());
        System.out.println(ClassLayout.parseClass(CompactSample.class).toPrintable());

        int[] primitiveArray = new int[1_000];
        Integer[] boxedArray = new Integer[1_000];
        for (int i = 0; i < 1_000; i++) {
            boxedArray[i] = i + 10_000; // outside IntegerCache (-128..127)
        }
        System.out.printf("  int[1000] shallow size     = %d bytes%n",
                GraphLayout.parseInstance((Object) primitiveArray).totalSize());
        System.out.printf("  Integer[1000] total size   = %d bytes (%.1fx larger!)%n",
                GraphLayout.parseInstance((Object) boxedArray).totalSize(),
                (double) GraphLayout.parseInstance((Object) boxedArray).totalSize()
                        / GraphLayout.parseInstance((Object) primitiveArray).totalSize());

        System.out.println("\n[2] Direct off-heap allocation (64 MB) + Thread stacks:");
        ByteBuffer direct = ByteBuffer.allocateDirect(64 * 1024 * 1024);
        direct.putInt(0, 42);
        System.out.println("  Allocated 64 MB DirectByteBuffer outside Heap (first int=" + direct.getInt(0) + ")");

        // Touch NMT via ManagementFactory / jcmd
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        System.out.println("  Current PID = " + pid);
        printNmtSummaryIfEnabled(pid);

        // Keep reference alive until after NMT snapshot
        List<ByteBuffer> anchor = new ArrayList<>();
        anchor.add(direct);
        if (anchor.isEmpty()) {
            System.out.println("unreachable");
        }
    }

    private static void printNmtSummaryIfEnabled(String pid) {
        try {
            Process p = new ProcessBuilder("jcmd", pid, "VM.native_memory", "summary")
                    .redirectErrorStream(true)
                    .start();
            String output = new String(p.getInputStream().readAllBytes());
            p.waitFor();
            System.out.println("\n[3] jcmd " + pid + " VM.native_memory summary:");
            output.lines().limit(35).forEach(line -> System.out.println("  " + line));
        } catch (Exception e) {
            System.out.println("  (Run with jcmd in PATH and -XX:NativeMemoryTracking=summary to print NMT: "
                    + e.getMessage() + ")");
        }
    }
}
