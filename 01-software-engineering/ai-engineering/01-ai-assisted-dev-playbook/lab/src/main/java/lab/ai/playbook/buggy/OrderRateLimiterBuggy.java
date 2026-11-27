package lab.ai.playbook.buggy;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Đoạn code #1 do AI sinh ra cho yêu cầu: "Viết rate limiter đơn giản theo userId, thread-safe".
 * <p>
 * Cố tình giữ lại 2 lỗi tinh vi thường gặp khi AI sinh code concurrency:
 * 1) Check-then-act race condition giữa getOrDefault() và put().
 * 2) Không kiểm tra null/blank và không giới hạn/evict key dẫn đến leak RAM.
 */
public final class OrderRateLimiterBuggy {

    private final ConcurrentHashMap<String, Integer> counters = new ConcurrentHashMap<>();
    private final int maxRequests;

    public OrderRateLimiterBuggy(int maxRequests) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be > 0");
        }
        this.maxRequests = maxRequests;
    }

    public boolean allowRequest(String userId) {
        // BUG 1: Đọc và ghi là 2 thao tác tách rời -> nhiều thread cùng đọc giá trị < maxRequests!
        Integer current = counters.getOrDefault(userId, 0);
        if (current >= maxRequests) {
            return false;
        }
        counters.put(userId, current + 1);
        return true;
    }

    public int currentCount(String userId) {
        return counters.getOrDefault(userId, 0);
    }

    public int trackedUserCount() {
        return counters.size();
    }
}
