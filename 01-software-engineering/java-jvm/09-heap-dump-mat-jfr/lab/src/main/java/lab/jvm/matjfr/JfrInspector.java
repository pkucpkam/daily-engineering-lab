package lab.jvm.matjfr;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedMethod;
import jdk.jfr.consumer.RecordedStackTrace;
import jdk.jfr.consumer.RecordingFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Offline CLI reader for .jfr recordings using jdk.jfr.consumer.RecordingFile.
 * Summarizes top allocation hot methods and JavaMonitorEnter lock contention.
 */
public class JfrInspector {

    public record JfrSummary(
            long totalEvents,
            long allocationSamples,
            long lockEvents,
            List<Map.Entry<String, Long>> topAllocMethods,
            List<Map.Entry<String, Long>> topBlockedMethods) {
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: JfrInspector <recording.jfr>");
            return;
        }
        JfrSummary summary = inspect(Path.of(args[0]));
        System.out.printf("JFR Summary -> totalEvents=%d | allocEvents=%d | monitorEnterEvents=%d%n",
                summary.totalEvents(), summary.allocationSamples(), summary.lockEvents());
        System.out.println("Top Allocation Hot Methods:");
        summary.topAllocMethods().forEach(e -> System.out.printf("  %s -> %d samples/bytes%n", e.getKey(), e.getValue()));
        System.out.println("Top Contended Lock Methods (total duration ns):");
        summary.topBlockedMethods().forEach(e -> System.out.printf("  %s -> %.2f ms%n", e.getKey(), e.getValue() / 1_000_000.0));
    }

    public static JfrSummary inspect(Path jfrPath) throws IOException {
        long total = 0;
        long allocCount = 0;
        long lockCount = 0;
        Map<String, Long> allocByMethod = new HashMap<>();
        Map<String, Long> blockNsByMethod = new HashMap<>();

        try (RecordingFile rf = new RecordingFile(jfrPath)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent ev = rf.readEvent();
                total++;
                String name = ev.getEventType().getName();
                if ("jdk.ObjectAllocationSample".equals(name) || "jdk.ObjectAllocationInNewTLAB".equals(name)) {
                    allocCount++;
                    long weight = ev.hasField("weight") ? ev.getLong("weight")
                            : (ev.hasField("allocationSize") ? ev.getLong("allocationSize") : 1L);
                    String frame = findAppFrame(ev.getStackTrace());
                    allocByMethod.merge(frame, weight, Long::sum);
                } else if ("jdk.JavaMonitorEnter".equals(name)) {
                    lockCount++;
                    String frame = findAppFrame(ev.getStackTrace());
                    long durationNs = ev.getDuration().toNanos();
                    blockNsByMethod.merge(frame, durationNs, Long::sum);
                }
            }
        }

        List<Map.Entry<String, Long>> topAlloc = allocByMethod.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .toList();
        List<Map.Entry<String, Long>> topLocks = blockNsByMethod.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .toList();

        return new JfrSummary(total, allocCount, lockCount, topAlloc, topLocks);
    }

    private static String findAppFrame(RecordedStackTrace stack) {
        if (stack == null || stack.getFrames().isEmpty()) {
            return "<unknown>";
        }
        for (var f : stack.getFrames()) {
            RecordedMethod m = f.getMethod();
            if (m != null && m.getType().getName().startsWith("lab.jvm.")) {
                return m.getType().getName() + "#" + m.getName();
            }
        }
        RecordedMethod top = stack.getFrames().get(0).getMethod();
        return top == null ? "<unknown>" : top.getType().getName() + "#" + top.getName();
    }
}
