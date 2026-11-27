package lab.cq.legacy;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization + Golden Master test harness for LegacyMonthlyStatementDao
 * using minimal JDBC dynamic proxies (no live DB or mocking framework required).
 */
class StatementCharacterizationTest {

    record FakeRow(String txId, long amountCents, String kind) {
    }

    @Test
    void goldenMasterLocksCurrentBehaviorAcrossTiersAndEdgeQuirks() throws Exception {
        String actualGoldenOutput = captureMatrixOutput(
                stubDao(List.of(
                        new FakeRow("T-1", 60_005L, "PURCHASE"), // 2% integer floor = 1200 -> net=58805
                        new FakeRow("T-2", 42_000L, "PURCHASE"), // 2% = 840 -> net=41160 -> gross=99965 (<=100k boundary)
                        new FakeRow("T-3", 5_000L, "REFUND")     // gross pushes >100k when included
                ))
        );

        String approvedGoldenMaster = """
                --- CASE 1: STD exact rows ---
                STATEMENT|cust=C-01|month=11|ts=1700000000000
                TX:T-1:PURCHASE:net=58805
                TX:T-2:PURCHASE:net=41160
                TX:T-3:REFUND:net=5000
                SUMMARY|count=3|net=104965|bonus=0|final=104965
                --- CASE 2: VIP crosses strict >100000 boundary ---
                STATEMENT|cust=C-02|month=11|ts=1700000000000
                TX:T-1:PURCHASE:net=58805
                TX:T-2:PURCHASE:net=41160
                TX:T-3:REFUND:net=5000
                SUMMARY|count=3|net=104965|bonus=5000|final=99965
                --- CASE 3: Empty result set ---
                STATEMENT|cust=C-EMPTY|month=12|ts=1700000000000
                SUMMARY|count=0|net=0|bonus=0|final=0
                """.stripTrailing();

        assertThat(actualGoldenOutput).isEqualTo(approvedGoldenMaster);
    }

    private String captureMatrixOutput(LegacyMonthlyStatementDao populatedDao) throws Exception {
        LegacyMonthlyStatementDao emptyDao = stubDao(List.of());
        return String.join("\n",
                "--- CASE 1: STD exact rows ---",
                populatedDao.buildStatement("C-01", "STD", 11),
                "--- CASE 2: VIP crosses strict >100000 boundary ---",
                populatedDao.buildStatement("C-02", "VIP", 11),
                "--- CASE 3: Empty result set ---",
                emptyDao.buildStatement("C-EMPTY", "VIP", 12)
        );
    }

    private static LegacyMonthlyStatementDao stubDao(List<FakeRow> rows) {
        return new LegacyMonthlyStatementDao() {
            @Override
            protected Connection openConnection() {
                return proxyConnection(rows);
            }

            @Override
            protected long currentEpochMillis() {
                return 1_700_000_000_000L;
            }
        };
    }

    private static Connection proxyConnection(List<FakeRow> rows) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> "prepareStatement".equals(method.getName()) ? proxyPreparedStatement(rows) : null
        );
    }

    private static PreparedStatement proxyPreparedStatement(List<FakeRow> rows) {
        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> "executeQuery".equals(method.getName()) ? proxyResultSet(rows) : null
        );
    }

    private static ResultSet proxyResultSet(List<FakeRow> rows) {
        AtomicInteger cursor = new AtomicInteger(-1);
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> cursor.incrementAndGet() < rows.size();
                    case "getString" -> {
                        int col = (int) args[0];
                        FakeRow r = rows.get(cursor.get());
                        yield col == 1 ? r.txId() : r.kind();
                    }
                    case "getLong" -> rows.get(cursor.get()).amountCents();
                    default -> null;
                }
        );
    }
}
