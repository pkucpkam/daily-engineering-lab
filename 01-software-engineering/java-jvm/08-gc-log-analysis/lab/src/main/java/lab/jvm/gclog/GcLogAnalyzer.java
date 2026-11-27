package lab.jvm.gclog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses OpenJDK Unified Logging (-Xlog:gc*) output to compute pause percentiles,
 * allocation rate (MB/s), live-set baseline trend, and memory leak indicators.
 */
public class GcLogAnalyzer {

    private static final Pattern PAUSE_LINE = Pattern.compile(
            "^\\[([0-9.]+)s].*\\[info]\\[gc\\s*] GC\\((\\d+)\\) (Pause [^\\n]+?) (\\d+)M->(\\d+)M\\((\\d+)M\\) ([0-9.]+)ms$");

    private static final Pattern EDEN_LINE = Pattern.compile(
            "^\\[([0-9.]+)s].*\\[gc,heap\\s*] GC\\((\\d+)\\) Eden regions: (\\d+)->(\\d+)\\((\\d+)\\)");

    public record GcEvent(
            double timestampSec,
            int gcId,
            String phase,
            int beforeMb,
            int afterMb,
            int capacityMb,
            double pauseMs,
            boolean fullGc) {
    }

    public record Summary(
            int totalPauses,
            int fullGcCount,
            double durationSec,
            double totalPauseMs,
            double maxPauseMs,
            double p95PauseMs,
            double throughputPct,
            double allocationRateMbPerSec,
            double firstWindowFloorMb,
            double lastWindowFloorMb,
            boolean leakSuspected) {

        public String formatReport() {
            return String.format(
                    "events=%d (full=%d) | duration=%.3fs | totalPause=%.2fms | p95=%.2fms | max=%.2fms | throughput=%.2f%% | allocRate=%.1f MB/s | floor=%.0fMB->%.0fMB | leakSuspected=%s",
                    totalPauses, fullGcCount, durationSec, totalPauseMs, p95PauseMs, maxPauseMs,
                    throughputPct, allocationRateMbPerSec, firstWindowFloorMb, lastWindowFloorMb, leakSuspected);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: GcLogAnalyzer <gc.log> [<second.log> ...]");
            return;
        }
        for (String arg : args) {
            List<String> lines = Files.readAllLines(Path.of(arg));
            Summary summary = analyze(lines);
            System.out.println(arg + " -> " + summary.formatReport());
        }
    }

    public static Summary analyze(List<String> lines) {
        List<GcEvent> events = new ArrayList<>();
        for (String raw : lines) {
            String line = raw.trim();
            Matcher m = PAUSE_LINE.matcher(line);
            if (m.matches()) {
                double ts = Double.parseDouble(m.group(1));
                int id = Integer.parseInt(m.group(2));
                String phase = m.group(3);
                int before = Integer.parseInt(m.group(4));
                int after = Integer.parseInt(m.group(5));
                int cap = Integer.parseInt(m.group(6));
                double pauseMs = Double.parseDouble(m.group(7));
                boolean isFull = phase.contains("Pause Full");
                events.add(new GcEvent(ts, id, phase, before, after, cap, pauseMs, isFull));
            }
        }

        if (events.isEmpty()) {
            return new Summary(0, 0, 0, 0, 0, 0, 100.0, 0, 0, 0, false);
        }

        List<Double> pauses = new ArrayList<>(events.size());
        double totalPauseMs = 0;
        int fullCount = 0;
        double allocatedMb = 0;

        for (int i = 0; i < events.size(); i++) {
            GcEvent e = events.get(i);
            pauses.add(e.pauseMs());
            totalPauseMs += e.pauseMs();
            if (e.fullGc()) {
                fullCount++;
            }
            if (i > 0) {
                int delta = e.beforeMb() - events.get(i - 1).afterMb();
                if (delta > 0) {
                    allocatedMb += delta;
                }
            } else {
                allocatedMb += e.beforeMb();
            }
        }

        Collections.sort(pauses);
        double maxPause = pauses.get(pauses.size() - 1);
        int p95Idx = Math.min(pauses.size() - 1, (int) Math.floor((pauses.size() - 1) * 0.95));
        double p95Pause = pauses.get(p95Idx);

        double startTs = events.get(0).timestampSec();
        double endTs = events.get(events.size() - 1).timestampSec();
        double windowSec = Math.max(0.001, endTs - startTs);
        double totalUptimeSec = Math.max(windowSec, endTs);
        double throughputPct = Math.max(0.0, 100.0 * (1.0 - (totalPauseMs / 1000.0) / totalUptimeSec));
        double allocRate = allocatedMb / totalUptimeSec;

        // Skip initial startup warmup bucket when evaluating steady-state baseline growth
        int warmupEnd = events.size() >= 9 ? events.size() / 4 : 0;
        List<GcEvent> steady = events.subList(warmupEnd, events.size());
        int bucket = Math.max(1, steady.size() / 3);
        double firstMin = steady.subList(0, bucket).stream().mapToInt(GcEvent::afterMb).min().orElse(0);
        double lastMin = steady.subList(steady.size() - bucket, steady.size()).stream().mapToInt(GcEvent::afterMb).min().orElse(0);

        boolean risingFloor = steady.size() >= 4 && lastMin > firstMin + 15.0 && lastMin > firstMin * 1.5;
        boolean leakSuspected = risingFloor || fullCount > 0;

        return new Summary(
                events.size(),
                fullCount,
                totalUptimeSec,
                totalPauseMs,
                maxPause,
                p95Pause,
                throughputPct,
                allocRate,
                firstMin,
                lastMin,
                leakSuspected
        );
    }

    public static long countEdenClearedEvents(List<String> lines) {
        return lines.stream()
                .map(String::trim)
                .map(EDEN_LINE::matcher)
                .filter(Matcher::matches)
                .count();
    }
}
