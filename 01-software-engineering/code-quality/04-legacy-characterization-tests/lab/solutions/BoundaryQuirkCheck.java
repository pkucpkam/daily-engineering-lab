import lab.cq.legacy.LegacyMonthlyStatementDao;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * L3 Solution example: demonstrates verifying a legacy VIP boundary quirk (100_000 exact vs 100_001)
 * via Characterization Test BEFORE fixing or refactoring the legacy code.
 */
public class BoundaryQuirkCheck {

    public static void main(String[] args) throws Exception {
        System.out.println("=== At exact 100,000 net (VIP bonus = 0 due to strict > check) ===");
        System.out.println(runWithSingleRow(100_000L, "REFUND"));
        System.out.println("=== At 100,001 net (VIP bonus = 5,000 triggers) ===");
        System.out.println(runWithSingleRow(100_001L, "REFUND"));
    }

    private static String runWithSingleRow(long amount, String kind) throws SQLException {
        LegacyMonthlyStatementDao dao = new LegacyMonthlyStatementDao() {
            @Override
            protected Connection openConnection() {
                return (Connection) Proxy.newProxyInstance(
                        Connection.class.getClassLoader(),
                        new Class<?>[]{Connection.class},
                        (p, m, a) -> "prepareStatement".equals(m.getName()) ? ps(amount, kind) : null
                );
            }

            @Override
            protected long currentEpochMillis() {
                return 1_700_000_000_000L;
            }
        };
        return dao.buildStatement("C-BOUNDARY", "VIP", 11);
    }

    private static PreparedStatement ps(long amount, String kind) {
        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (p, m, a) -> "executeQuery".equals(m.getName()) ? rs(amount, kind) : null
        );
    }

    private static ResultSet rs(long amount, String kind) {
        AtomicInteger calls = new AtomicInteger(0);
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (p, m, a) -> switch (m.getName()) {
                    case "next" -> calls.getAndIncrement() == 0;
                    case "getString" -> ((int) a[0]) == 1 ? "TX-B" : kind;
                    case "getLong" -> amount;
                    default -> null;
                }
        );
    }
}
