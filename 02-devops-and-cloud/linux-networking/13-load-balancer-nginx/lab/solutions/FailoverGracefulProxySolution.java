package lab.linux.lb.solutions;

import java.util.Set;

/**
 * Lời giải tham khảo (LNX-13):
 * Quy tắc an toàn khi quyết định Proxy có được retry sang backend kế tiếp hay không:
 * - Cho phép retry nếu CHƯA gửi dữ liệu (connect failure).
 * - Khi đã gửi request và bị read timeout: CHỈ retry method idempotent (GET, HEAD, OPTIONS),
 *   HOẶC khi client gửi kèm Idempotency-Key hợp lệ — tuyệt đối không tự retry POST thanh toán!
 */
public final class FailoverGracefulProxySolution {

    private static final Set<String> IDEMPOTENT_METHODS = Set.of("GET", "HEAD", "OPTIONS", "PUT", "DELETE");

    public static boolean isSafeToRetryUpstream(
            String httpMethod,
            boolean failedBeforeSendingBytes,
            boolean hasIdempotencyKeyHeader) {
        if (failedBeforeSendingBytes) {
            return true;
        }
        if (IDEMPOTENT_METHODS.contains(httpMethod.toUpperCase())) {
            return true;
        }
        return "POST".equalsIgnoreCase(httpMethod) && hasIdempotencyKeyHeader;
    }
}
