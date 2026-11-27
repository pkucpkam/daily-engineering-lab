package lab.cq.legacy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Legacy Java-7-style JDBC DAO/service before refactoring:
 * - Hardcoded DriverManager + System.currentTimeMillis() broken open via minimal
 *   Extract & Override seams (openConnection(), currentEpochMillis()) without altering calculation logic.
 * - Intentionally retains quirks (e.g., strict > 1000 bonus threshold, integer floor division).
 */
public class LegacyMonthlyStatementDao {

    public String buildStatement(String customerId, String tier, int billingMonth) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("STATEMENT|cust=").append(customerId)
          .append("|month=").append(billingMonth)
          .append("|ts=").append(currentEpochMillis()).append('\n');

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = openConnection();
            ps = conn.prepareStatement("SELECT tx_id, amount_cents, kind FROM billing_tx WHERE cust_id = ? AND month = ?");
            ps.setString(1, customerId);
            ps.setInt(2, billingMonth);
            rs = ps.executeQuery();

            long grossCents = 0L;
            int txCount = 0;
            List<String> lines = new ArrayList<>();
            while (rs.next()) {
                String txId = rs.getString(1);
                long amount = rs.getLong(2);
                String kind = rs.getString(3);

                // Legacy calculation quirk preserved intentionally for characterization tests
                long fee = "REFUND".equals(kind) ? 0L : (amount * 2) / 100; // integer floor division 2%
                grossCents += amount - fee;
                txCount++;
                lines.add("TX:" + txId + ":" + kind + ":net=" + (amount - fee));
            }

            long loyaltyBonus = 0L;
            if ("VIP".equals(tier) && grossCents > 100_000L) {
                loyaltyBonus = 5_000L;
            }
            for (String line : lines) {
                sb.append(line).append('\n');
            }
            sb.append("SUMMARY|count=").append(txCount)
              .append("|net=").append(grossCents)
              .append("|bonus=").append(loyaltyBonus)
              .append("|final=").append(grossCents - loyaltyBonus);
            return sb.toString();
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException ignored) { }
            }
            if (ps != null) {
                try { ps.close(); } catch (SQLException ignored) { }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) { }
            }
        }
    }

    /** Minimal Seam #1 (Extract & Override Factory Method) to break DriverManager dependency in tests. */
    protected Connection openConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:oracle:thin:@legacy-db:1521:PROD", "app", "secret");
    }

    /** Minimal Seam #2 (Extract & Override Clock) to remove non-determinism in Golden Master tests. */
    protected long currentEpochMillis() {
        return System.currentTimeMillis();
    }
}
