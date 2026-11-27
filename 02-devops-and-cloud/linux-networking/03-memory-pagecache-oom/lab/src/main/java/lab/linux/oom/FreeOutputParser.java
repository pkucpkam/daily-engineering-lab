package lab.linux.oom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Phân tích đầu ra lệnh `free -m` của Linux để phân biệt "free thấp do Page Cache"
 * với "áp lực bộ nhớ thật / Swap Thrashing".
 */
public final class FreeOutputParser {

    public enum HealthStatus {
        HEALTHY_PAGE_CACHE_ACTIVE,
        WARNING_LOW_AVAILABLE,
        CRITICAL_SWAP_THRASHING
    }

    public record FreeReport(
            long memTotalMb,
            long memUsedMb,
            long memFreeMb,
            long buffCacheMb,
            long memAvailableMb,
            long swapTotalMb,
            long swapUsedMb,
            HealthStatus status
    ) {}

    private static final Pattern MEM_PATTERN = Pattern.compile(
            "^Mem:\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)", Pattern.MULTILINE);
    private static final Pattern SWAP_PATTERN = Pattern.compile(
            "^Swap:\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)", Pattern.MULTILINE);

    public FreeReport parse(String freeOutput) {
        Matcher memMatcher = MEM_PATTERN.matcher(freeOutput);
        if (!memMatcher.find()) {
            throw new IllegalArgumentException("Missing Mem: line in free -m output");
        }
        long total = Long.parseLong(memMatcher.group(1));
        long used = Long.parseLong(memMatcher.group(2));
        long free = Long.parseLong(memMatcher.group(3));
        long buffCache = Long.parseLong(memMatcher.group(5));
        long available = Long.parseLong(memMatcher.group(6));

        long swapTotal = 0;
        long swapUsed = 0;
        Matcher swapMatcher = SWAP_PATTERN.matcher(freeOutput);
        if (swapMatcher.find()) {
            swapTotal = Long.parseLong(swapMatcher.group(1));
            swapUsed = Long.parseLong(swapMatcher.group(2));
        }

        double availableRatio = (double) available / Math.max(1, total);
        double swapUsedRatio = swapTotal > 0 ? (double) swapUsed / swapTotal : 0.0;

        HealthStatus status;
        if (availableRatio < 0.10 || swapUsedRatio > 0.50) {
            status = HealthStatus.CRITICAL_SWAP_THRASHING;
        } else if (availableRatio < 0.20) {
            status = HealthStatus.WARNING_LOW_AVAILABLE;
        } else {
            status = HealthStatus.HEALTHY_PAGE_CACHE_ACTIVE;
        }
        return new FreeReport(total, used, free, buffCache, available, swapTotal, swapUsed, status);
    }
}
