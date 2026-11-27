package lab.ai.playbook.solutions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Lời giải E7 (phần 1): JDBC helper an toàn chuẩn Java 7+:
 * - Dùng PreparedStatement (chặn SQL Injection).
 * - Dùng try-with-resources (đóng ResultSet, PreparedStatement, Connection kể cả khi có exception).
 * - Xử lý null, count == 0, và RoundingMode.HALF_UP.
 */
public final class InvoiceJdbcHelperFixed {

    private static final String SQL =
            "SELECT amount FROM invoices WHERE customer_id = ?";

    public BigDecimal fetchAverageAmount(DataSource ds, String customerId) throws SQLException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }

        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal amount = rs.getBigDecimal("amount");
                    if (amount != null) {
                        sum = sum.add(amount);
                        count++;
                    }
                }
            }
        }

        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
}
