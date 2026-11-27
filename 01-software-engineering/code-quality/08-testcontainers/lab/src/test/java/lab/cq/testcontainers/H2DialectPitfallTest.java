package lab.cq.testcontainers;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs without Docker: proves why H2 (even in MODE=PostgreSQL) diverges from real
 * PostgreSQL on expression indexes, JSONB ->> operators, and ON CONFLICT ... RETURNING.
 */
class H2DialectPitfallTest {

    @Test
    void flywayV1MigrationExistsAndUsesPostgresFeaturesThatFailOnH2() throws Exception {
        String migrationSql = Files.readString(Path.of("src/main/resources/db/migration/V1__create_ledger_table.sql"));
        assertThat(migrationSql)
                .contains("JSONB NOT NULL")
                .contains("metadata ->> 'channel'");

        JdbcDataSource h2PostgresMode = new JdbcDataSource();
        h2PostgresMode.setURL("jdbc:h2:mem:dialectcheck;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");

        // 1. Expression index ((metadata ->> 'channel')) fails on H2 even with MODE=PostgreSQL
        try (Connection conn = h2PostgresMode.getConnection();
             Statement st = conn.createStatement()) {
            assertThatThrownBy(() -> st.execute(migrationSql))
                    .isInstanceOf(SQLException.class);
        }

        // 2. Even if simplified table exists, ON CONFLICT ... RETURNING breaks in H2
        var repo = new PostgresLedgerRepository(h2PostgresMode);
        assertThatThrownBy(() -> repo.upsertAndReturnId("REF-H2-1", 10_000L, "WEB"))
                .isInstanceOf(SQLException.class);
    }
}
