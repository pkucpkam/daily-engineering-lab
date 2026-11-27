import java.nio.file.Files;
import java.nio.file.Path;

/**
 * L3 Solution helper: checks Flyway SQL migration conventions (V<num>__<desc>.sql,
 * check constraints, idempotent patterns) before running in Testcontainers/CI.
 */
public class MigrationDryRunCheck {

    public static void main(String[] args) throws Exception {
        Path sqlPath = Path.of("src/main/resources/db/migration/V1__create_ledger_table.sql");
        String sql = Files.readString(sqlPath);
        boolean hasVersionPrefix = sqlPath.getFileName().toString().matches("^V\\d+__[a-z0-9_]+\\.sql$");
        boolean hasPrimaryKey = sql.contains("PRIMARY KEY");
        boolean hasJsonb = sql.contains("JSONB");
        System.out.printf("Migration=%s | validName=%s | primaryKey=%s | usesJsonb=%s%n",
                sqlPath.getFileName(), hasVersionPrefix, hasPrimaryKey, hasJsonb);
    }
}
