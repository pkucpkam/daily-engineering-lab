package lab.linux.shell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShellSafetyTest {

    @Test
    void collectJbossDiagScriptHasLfLineEndingsAndZeroSafetyFindings() throws Exception {
        String script = Files.readString(Path.of("scripts/collect-jboss-diag.sh"), StandardCharsets.UTF_8);
        BashScriptSafetyAuditor auditor = new BashScriptSafetyAuditor();

        assertThat(auditor.audit(script)).isEmpty();
        assertThat(script).contains("set -euo pipefail");
        assertThat(script).contains("trap cleanup EXIT INT TERM");
        assertThat(script).contains("getopts");
    }

    @Test
    void auditorCatchesStrictModeQuotingCrlfAndMissingTrapBugs() {
        BashScriptSafetyAuditor auditor = new BashScriptSafetyAuditor();
        String broken = "#!/bin/bash\r\nTMP=$(mktemp -d)\r\ncd /opt/missing\r\nrm -rf $TMP\r\n";

        List<String> findings = auditor.audit(broken);
        assertThat(findings).anyMatch(f -> f.startsWith("LINE_ENDING"));
        assertThat(findings).anyMatch(f -> f.startsWith("STRICT_MODE"));
        assertThat(findings).anyMatch(f -> f.startsWith("CLEANUP"));
        assertThat(findings).anyMatch(f -> f.startsWith("QUOTING"));
        assertThat(findings).anyMatch(f -> f.startsWith("UNCHECKED_CD"));
    }

    @Test
    void extractorFiltersTimeWindowErrorsAndWritesIdempotently(@TempDir Path tempDir) throws Exception {
        String logContent = Files.readString(Path.of("scripts/sample-jboss-server.log"), StandardCharsets.UTF_8);
        JbossLogDiagExtractor extractor = new JbossLogDiagExtractor();

        var first = extractor.extractAndWriteIdempotent(logContent, "14:00:00", "14:05:00", tempDir);
        String firstOutput = Files.readString(tempDir.resolve("diag-summary.txt"), StandardCharsets.UTF_8);

        var second = extractor.extractAndWriteIdempotent(logContent, "14:00:00", "14:05:00", tempDir);
        String secondOutput = Files.readString(tempDir.resolve("diag-summary.txt"), StandardCharsets.UTF_8);

        assertThat(first.matchedWindowErrors()).hasSize(3);
        assertThat(second.matchedWindowErrors()).hasSize(3);
        assertThat(secondOutput).isEqualTo(firstOutput);
        try (var stream = Files.list(tempDir)) {
            assertThat(stream.count()).isEqualTo(1);
        }
    }
}
