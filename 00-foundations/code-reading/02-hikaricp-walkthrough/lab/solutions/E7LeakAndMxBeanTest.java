package lab.codereading.hikari.solutions;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reference solution for Exercise E7:
 * Verifies active/idle counts via HikariPoolMXBean while triggering ProxyLeakTask.
 */
class E7LeakAndMxBeanTest {

    @Test
    void mxBeanTracksActiveAndIdleConnectionsAcrossLeakWindow() throws Exception {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:e7_mxbean;DB_CLOSE_DELAY=-1");
        config.setMaximumPoolSize(2);
        config.setLeakDetectionThreshold(250);
        config.setPoolName("e7-pool");

        try (var ds = new HikariDataSource(config)) {
            HikariPoolMXBean mxBean = ds.getHikariPoolMXBean();

            Connection leaked = ds.getConnection();
            assertThat(mxBean.getActiveConnections()).isEqualTo(1);

            // Wait past 250ms so ProxyLeakTask logs WARN Apparent connection leak detected
            Thread.sleep(350);

            leaked.close();
            assertThat(mxBean.getActiveConnections()).isZero();
            assertThat(mxBean.getIdleConnections()).isGreaterThanOrEqualTo(1);
        }
    }
}
