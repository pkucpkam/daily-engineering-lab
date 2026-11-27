package lab.cq.testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository using authentic PostgreSQL SQL semantics:
 * - ON CONFLICT (external_ref) DO UPDATE ... RETURNING id
 * - JSONB ->> operator filtering
 * - explicit transaction commit/rollback boundary
 */
public class PostgresLedgerRepository {

    private final DataSource dataSource;

    public PostgresLedgerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long upsertAndReturnId(String externalRef, long amountCents, String channel) throws SQLException {
        String sql = """
                INSERT INTO ledger_entry (external_ref, amount_cents, metadata)
                VALUES (?, ?, CAST(? AS JSONB))
                ON CONFLICT (external_ref)
                DO UPDATE SET amount_cents = EXCLUDED.amount_cents
                RETURNING id
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, externalRef);
            ps.setLong(2, amountCents);
            ps.setString(3, "{\"channel\":\"" + channel + "\"}");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void insertBatchAtomically(List<String> refs, List<Long> amounts, String channel) throws SQLException {
        String sql = "INSERT INTO ledger_entry (external_ref, amount_cents, metadata) VALUES (?, ?, CAST(? AS JSONB))";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < refs.size(); i++) {
                    ps.setString(1, refs.get(i));
                    ps.setLong(2, amounts.get(i));
                    ps.setString(3, "{\"channel\":\"" + channel + "\"}");
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<String> findRefsByChannel(String channel) throws SQLException {
        String sql = "SELECT external_ref FROM ledger_entry WHERE metadata ->> 'channel' = ? ORDER BY id";
        List<String> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, channel);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            }
        }
        return result;
    }
}
