package lab.linux.shell;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Runner kiểm tra an toàn các script bash trong lab/scripts và trích xuất log JBoss idempotent.
 */
public final class ShellLabRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== LNX-06 Shell Scripting Automation Lab ===");
        BashScriptSafetyAuditor auditor = new BashScriptSafetyAuditor();

        String safeScript = Files.readString(Path.of("scripts/collect-jboss-diag.sh"), StandardCharsets.UTF_8);
        List<String> safeFindings = auditor.audit(safeScript);
        System.out.println("[1] Audit collect-jboss-diag.sh => findings=" + safeFindings.size());

        String unsafeSnippet = """
                #!/bin/bash
                TARGET=$1
                cd /opt/jboss/$TARGET
                rm -rf $TARGET
                """;
        List<String> unsafeFindings = auditor.audit(unsafeSnippet);
        System.out.println("[2] Audit unsafe snippet => findings=" + unsafeFindings.size());
        unsafeFindings.forEach(f -> System.out.println("    - " + f));

        String sampleLog = Files.readString(Path.of("scripts/sample-jboss-server.log"), StandardCharsets.UTF_8);
        Path tmpOut = Files.createTempDirectory("lnx06-diag-");
        try {
            JbossLogDiagExtractor extractor = new JbossLogDiagExtractor();
            JbossLogDiagExtractor.DiagSummary summary =
                    extractor.extractAndWriteIdempotent(sampleLog, "14:00:00", "14:05:00", tmpOut);
            System.out.printf("[3] Log extraction [14:00:00..14:05:00]: total=%d, errors=%d, matched=%d%n",
                    summary.totalLines(), summary.errorCount(), summary.matchedWindowErrors().size());
        } finally {
            Files.walk(tmpOut).sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (Exception ignored) {
                }
            });
        }
    }
}
