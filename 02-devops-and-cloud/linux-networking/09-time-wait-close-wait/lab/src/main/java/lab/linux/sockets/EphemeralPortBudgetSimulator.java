package lab.linux.sockets;

import java.util.HashMap;
import java.util.Map;

/**
 * Mô phỏng bảng 4-tuple (srcIp, srcPort, dstIp, dstPort) và TIME_WAIT (60s) trong Linux kernel
 * để định lượng chính xác thời điểm cạn ephemeral port (EADDRNOTAVAIL).
 */
public final class EphemeralPortBudgetSimulator {

    public record SimulationReport(
            int totalPortsInRange,
            int rps,
            int timeWaitSeconds,
            int durationSeconds,
            long totalAttemptedRequests,
            long exhaustedCount,
            int firstExhaustedSecond,
            int peakTimeWaitPorts) {}

    private final int rangeStart;
    private final int rangeEnd;
    private final int timeWaitSeconds;

    public EphemeralPortBudgetSimulator(int rangeStart, int rangeEnd, int timeWaitSeconds) {
        if (rangeStart <= 0 || rangeEnd < rangeStart) {
            throw new IllegalArgumentException("Invalid ephemeral port range");
        }
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.timeWaitSeconds = timeWaitSeconds;
    }

    public int totalPorts() {
        return rangeEnd - rangeStart + 1;
    }

    /**
     * Mô phỏng gọi tới 1 đích cố định (dstIp, dstPort) theo từng giây:
     * - Nếu useConnectionPool == false: mỗi request mở và đóng 1 kết nối -> giữ ephemeral port trong TIME_WAIT.
     * - Nếu useConnectionPool == true: tái sử dụng cố định poolSize cổng, không phát sinh TIME_WAIT mới.
     */
    public SimulationReport simulateSingleTarget(
            int rps, int durationSeconds, boolean useConnectionPool, int poolSize) {
        int totalPorts = totalPorts();
        if (useConnectionPool) {
            int used = Math.min(poolSize, totalPorts);
            long attempted = (long) rps * durationSeconds;
            long exhausted = poolSize > totalPorts ? attempted : 0;
            return new SimulationReport(totalPorts, rps, timeWaitSeconds, durationSeconds, attempted, exhausted, -1, used);
        }

        // Lưu thời điểm (second) mà ephemeral port được giải phóng khỏi TIME_WAIT
        Map<Integer, Integer> portExpirySecond = new HashMap<>(Math.min(totalPorts, 65536));
        int cursorPort = rangeStart;
        long attempted = 0;
        long exhausted = 0;
        int firstExhaustedSecond = -1;
        int peakBusy = 0;

        for (int sec = 1; sec <= durationSeconds; sec++) {
            final int currentSec = sec;
            portExpirySecond.entrySet().removeIf(e -> e.getValue() <= currentSec);

            for (int i = 0; i < rps; i++) {
                attempted++;
                Integer allocatedPort = findAvailablePort(portExpirySecond, cursorPort);
                if (allocatedPort == null) {
                    exhausted++;
                    if (firstExhaustedSecond == -1) {
                        firstExhaustedSecond = sec;
                    }
                } else {
                    portExpirySecond.put(allocatedPort, sec + timeWaitSeconds);
                    cursorPort = (allocatedPort == rangeEnd) ? rangeStart : allocatedPort + 1;
                }
            }
            if (portExpirySecond.size() > peakBusy) {
                peakBusy = portExpirySecond.size();
            }
        }

        return new SimulationReport(
                totalPorts, rps, timeWaitSeconds, durationSeconds, attempted, exhausted, firstExhaustedSecond, peakBusy);
    }

    private Integer findAvailablePort(Map<Integer, Integer> busyPorts, int startCursor) {
        int total = totalPorts();
        if (busyPorts.size() >= total) {
            return null;
        }
        int port = startCursor;
        for (int checked = 0; checked < total; checked++) {
            if (!busyPorts.containsKey(port)) {
                return port;
            }
            port = (port == rangeEnd) ? rangeStart : port + 1;
        }
        return null;
    }
}
