package lab.ai.playbook.buggy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Đoạn code #3 mô phỏng HTTP client + date formatter kiểu Java 7 do AI sinh.
 * <p>
 * Các lỗi ẩn cố tình đưa vào:
 * 1) SimpleDateFormat dùng static final (KHÔNG thread-safe!).
 * 2) Retry cả lỗi 4xx (non-retryable client error).
 * 3) Nuốt InterruptedException mà không khôi phục Thread.currentThread().interrupt().
 */
public final class PaymentClientBuggy {

    // BUG 1: SimpleDateFormat giữ state Calendar nội bộ, dùng chung nhiều thread sẽ hỏng dữ liệu hoặc ném lỗi!
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @FunctionalInterface
    public interface Gateway {
        int send(String formattedDate) throws InterruptedException;
    }

    public String formatAndNormalizeDate(String rawDate) throws ParseException {
        Date parsed = DATE_FORMAT.parse(rawDate);
        return DATE_FORMAT.format(parsed);
    }

    public int sendWithRetry(Gateway gateway, String rawDate, int maxAttempts) throws ParseException {
        String date = formatAndNormalizeDate(rawDate);
        int lastStatus = 500;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                lastStatus = gateway.send(date);
                if (lastStatus >= 200 && lastStatus < 300) {
                    return lastStatus;
                }
                // BUG 2: Retry luôn cả 400 Bad Request / 401 / 404 thay vì chỉ retry 5xx hoặc 429!
            } catch (InterruptedException ex) {
                // BUG 3: Nuốt cờ interrupt khiến caller/executor không dừng được tác vụ!
                return -1;
            }
        }
        return lastStatus;
    }
}
