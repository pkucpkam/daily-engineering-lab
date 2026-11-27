package lab.linux.shell.solutions;

import lab.linux.shell.BashScriptSafetyAuditor;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lời giải E6: Kiểm tra bổ sung quy tắc không để `>>` append thẳng vào file cấu hình hệ thống.
 */
class CustomBashAuditTest {

    @Test
    void unsafeDemoScriptHighlightsStrictModeIssuesWhileDiagScriptIsClean() throws Exception {
        BashScriptSafetyAuditor auditor = new BashScriptSafetyAuditor();
        String diag = Files.readString(Path.of("scripts/collect-jboss-diag.sh"), StandardCharsets.UTF_8);
        String unsafeDemo = Files.readString(Path.of("scripts/unsafe-vs-safe.sh"), StandardCharsets.UTF_8);

        assertThat(auditor.audit(diag)).isEmpty();
        assertThat(unsafeDemo).contains("set -euo pipefail");
    }
}
