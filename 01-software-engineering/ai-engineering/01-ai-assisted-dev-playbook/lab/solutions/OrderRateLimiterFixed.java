package lab.ai.playbook.solutions;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lời giải E6: Rate limiter thread-safe bằng bucket compute() atomic + hỗ trợ dọn key hết hạn.
 */
public final class OrderRateLimiterFixed {

    private record WindowBucket(int count, Instant lastSeen) {}

    private final ConcurrentHashMap<String, WindowBucket> counters = new ConcurrentHashMap<>();
    private final int maxRequests;

    public OrderRateLimiterFixed(int maxRequests) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be > 0");
        }
        this.maxRequests = maxRequests;
    }

    public boolean allowRequest(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        AtomicBoolean granted = new AtomicBoolean(false);
        Instant now = Instant.now();

        counters.compute(userId, (key, existing) -> {
            if (existing == null) {
                granted.set(true);
                return new WindowBucket(1, now);
            }
            if (existing.count() >= maxRequests) {
                granted.set(false);
                return existing;
            }
            granted.set(true);
            return new WindowBucket(existing.count() + 1, now);
        });
        return granted.get();
    }

    public int evictExpired(Duration ttl) {
        Instant cutoff = Instant.now().minus(ttl);
        int removed = 0;
        Iterator<Map.Entry<String, WindowBucket>> it = counters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, WindowBucket> entry = it.next();
            if (entry.getValue().lastSeen().isBefore(cutoff)) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }
}
