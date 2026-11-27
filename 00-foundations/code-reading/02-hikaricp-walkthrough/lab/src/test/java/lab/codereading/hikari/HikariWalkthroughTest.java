package lab.codereading.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariProxyConnection;
import com.zaxxer.hikari.util.ConcurrentBag;
import com.zaxxer.hikari.util.FastList;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HikariWalkthroughTest {

    @Test
    void poolSizeTwoWithThreeHoldersCausesThirdToTimeoutWithSqlTransientException() throws Exception {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_exhaustion;DB_CLOSE_DELAY=-1");
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(250); // HikariCP minimum is 250ms

        try (var ds = new HikariDataSource(config)) {
            Connection c1 = ds.getConnection();
            Connection c2 = ds.getConnection();
            try {
                assertThat(c1).isInstanceOf(HikariProxyConnection.class);
                assertThatThrownBy(ds::getConnection)
                        .isInstanceOf(SQLException.class)
                        .hasMessageContaining("Connection is not available, request timed out");
            } finally {
                c1.close();
                c2.close();
            }
        }
    }

    @Test
    void fiveConcurrentThreadsShareTwoConnectionsWhenHoldTimeFitsTimeout() throws Exception {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_share;DB_CLOSE_DELAY=-1");
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(2000);

        try (var ds = new HikariDataSource(config);
             var pool = Executors.newFixedThreadPool(5)) {

            var gate = new CountDownLatch(1);
            var completed = new AtomicInteger();
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                futures.add(pool.submit(() -> {
                    gate.await();
                    try (var conn = ds.getConnection();
                         var ps = conn.prepareStatement("SELECT 1");
                         var rs = ps.executeQuery()) {
                        assertThat(rs.next()).isTrue();
                        Thread.sleep(40);
                        completed.incrementAndGet();
                    }
                    return null;
                }));
            }
            gate.countDown();
            for (var f : futures) {
                f.get();
            }
            assertThat(completed.get()).isEqualTo(5);
        }
    }

    @Test
    void concurrentBagReusesThreadLocalEntryBeforeScanningSharedList() throws Exception {
        final class DummyEntry implements ConcurrentBag.IConcurrentBagEntry {
            private final AtomicInteger state = new AtomicInteger(STATE_NOT_IN_USE);

            @Override
            public boolean compareAndSet(int expectState, int newState) {
                return state.compareAndSet(expectState, newState);
            }

            @Override
            public void setState(int newState) {
                state.set(newState);
            }

            @Override
            public int getState() {
                return state.get();
            }
        }

        try (var bag = new ConcurrentBag<DummyEntry>(entry -> java.util.concurrent.CompletableFuture.completedFuture(true))) {
            var e1 = new DummyEntry();
            var e2 = new DummyEntry();
            bag.add(e1);
            bag.add(e2);

            var first = bag.borrow(100, java.util.concurrent.TimeUnit.MILLISECONDS);
            assertThat(first).isNotNull();
            bag.requite(first);

            // Re-borrowing from the same thread hits ThreadLocal fast-path and gets the exact same entry back
            var second = bag.borrow(100, java.util.concurrent.TimeUnit.MILLISECONDS);
            assertThat(second).isSameAs(first);
            bag.requite(second);
        }
    }

    @Test
    void fastListRemovesFromTailWithoutShiftingPrecedingElements() {
        var list = new FastList<>(String.class, 4);
        list.add("S1");
        list.add("S2");
        list.add("S3");

        assertThat(list.remove("S3")).isTrue();
        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0)).isEqualTo("S1");
        assertThat(list.get(1)).isEqualTo("S2");
    }
}
