package lab.cq.testcontainers;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real PostgreSQL 16 + Flyway integration test via Testcontainers.
 * Automatically executes when Docker daemon is available (@EnabledIf).
 */
@Testcontainers(disabledWithoutDocker = true)
@EnabledIf("isDockerAvailable")
class PostgresFlywayIntegrationIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ledger_test")
            .withUsername("app")
            .withPassword("app")
            .withReuse(true);

    private static PGSimpleDataSource dataSource;
    private static PostgresLedgerRepository repository;

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable ignored) {
            return false;
        }
    }

    @BeforeAll
    static void migrateSchema() {
        dataSource = new PGSimpleDataSource();
        dataSource.setURL(POSTGRES.getJdbcUrl());
        dataSource.setUser(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        repository = new PostgresLedgerRepository(dataSource);
    }

    @Test
    void executesUpsertReturningJsonbQueryAndTransactionRollbackOnRealPostgres() throws Exception {
        long id1 = repository.upsertAndReturnId("PG-100", 50_000L, "MOBILE");
        long id1Updated = repository.upsertAndReturnId("PG-100", 75_000L, "MOBILE");

        assertThat(id1Updated).isEqualTo(id1);
        assertThat(repository.findRefsByChannel("MOBILE")).containsExactly("PG-100");

        // Transaction rollback test: second row violates CHECK (amount_cents > 0) -> first row must roll back
        assertThatThrownBy(() -> repository.insertBatchAtomically(
                List.of("PG-ATOMIC-1", "PG-ATOMIC-BAD"),
                List.of(20_000L, -5L),
                "WEB"))
                .isInstanceOf(SQLException.class);

        assertThat(repository.findRefsByChannel("WEB")).isEmpty();
    }
}
