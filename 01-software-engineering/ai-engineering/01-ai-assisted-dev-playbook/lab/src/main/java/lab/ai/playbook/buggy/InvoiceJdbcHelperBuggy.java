package lab.ai.playbook.buggy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Đoạn code #2 mô phỏng code AI sinh cho legacy JDBC helper + tính toán hóa đơn.
 * <p>
 * Các lỗi ẩn cố tình đưa vào để luyện Checklist 7 điểm:
 * 1) Ghép chuỗi SQL trực tiếp (SQL Injection).
 * 2) Không đóng tài nguyên nếu xảy ra exception giữa chừng (resource leak).
 * 3) BigDecimal.divide() thiếu RoundingMode -> ArithmeticException khi chia không hết.
 * 4) Không kiểm tra divisor <= 0 hoặc danh sách null/phần tử null.
 */
public final class InvoiceJdbcHelperBuggy {

    public interface FakeConnection extends AutoCloseable {
        FakeStatement createStatement();
        boolean isClosed();
    }

    public interface FakeStatement extends AutoCloseable {
        List<BigDecimal> executeQuery(String sql);
        boolean isClosed();
    }

    public String buildUnsafeQuery(String customerId) {
        // BUG 1: Ghép chuỗi thẳng user input -> SQL injection!
        return "SELECT amount FROM invoices WHERE customer_id = '" + customerId + "'";
    }

    public BigDecimal fetchAverageAmount(FakeConnection conn, String customerId) throws Exception {
        // BUG 2: Không dùng try-with-resources -> nếu executeQuery ném lỗi, conn & st bị leak!
        FakeStatement st = conn.createStatement();
        List<BigDecimal> amounts = st.executeQuery(buildUnsafeQuery(customerId));
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal amount : amounts) {
            // BUG 4: Không xử lý phần tử null từ DB nullable
            sum = sum.add(amount);
        }
        // BUG 3: Nếu amounts.size() == 0 hoặc chia cho 3 -> ArithmeticException!
        BigDecimal avg = sum.divide(BigDecimal.valueOf(amounts.size()));
        st.close();
        conn.close();
        return avg;
    }

    public static List<BigDecimal> sampleAmounts() {
        List<BigDecimal> list = new ArrayList<>();
        list.add(new BigDecimal("10.00"));
        list.add(new BigDecimal("10.00"));
        list.add(new BigDecimal("10.01")); // tổng = 30.01 -> chia 3 lặp vô hạn!
        return list;
    }
}
