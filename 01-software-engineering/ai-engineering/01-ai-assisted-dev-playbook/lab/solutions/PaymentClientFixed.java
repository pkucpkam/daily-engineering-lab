package lab.ai.playbook.solutions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Lời giải E7 (phần 2):
 * - Cô lập SimpleDateFormat bằng ThreadLocal (chạy an toàn cả trên Java 7; Java 8+ dùng DateTimeFormatter).
 * - Chỉ retry lỗi transient (5xx hoặc 429), KHÔNG retry 4xx.
 * - Khôi phục interrupt flag khi bắt InterruptedException.
 */
public final class PaymentClientFixed {

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            ThreadLocal.withInitial(() -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                return sdf;
            });

    @FunctionalInterface
    public interface Gateway {
        int send(String formattedDate) throws InterruptedException;
    }

    public String formatAndNormalizeDate(String rawDate) throws ParseException {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            throw new IllegalArgumentException("rawDate must not be blank");
        }
        SimpleDateFormat sdf = DATE_FORMAT.get();
        Date parsed = sdf.parse(rawDate);
        return sdf.format(parsed);
    }

    public int sendWithRetry(Gateway gateway, String rawDate, int maxAttempts) throws ParseException {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be > 0");
        }
        String date = formatAndNormalizeDate(rawDate);
        int lastStatus = 500;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                lastStatus = gateway.send(date);
                if (lastStatus >= 200 && lastStatus < 300) {
                    return lastStatus;
                }
                if (!isRetryable(lastStatus)) {
                    return lastStatus;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }
        return lastStatus;
    }

    private static boolean isRetryable(int status) {
        return status == 429 || status >= 500;
    }
}
