package lab.linux.shell;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mô phỏng logic lọc log JBoss của awk/grep trong collect-jboss-diag.sh
 * và thực hiện ghi báo cáo atomic + idempotent trên thư mục đầu ra.
 */
public final class JbossLogDiagExtractor {

    public record DiagSummary(int totalLines, int errorCount, int warnCount, List<String> matchedWindowErrors) {}

    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}\\s+(\\d{2}:\\d{2}:\\d{2})");

    public DiagSummary extractAndWriteIdempotent(
            String logContent,
            String startTime,
            String endTime,
            Path outputDir
    ) throws IOException {
        List<String> matched = new ArrayList<>();
        int total = 0;
        int errors = 0;
        int warns = 0;

        for (String line : logContent.split("\\R")) {
            if (line.isBlank()) {
                continue;
            }
            total++;
            if (line.contains(" ERROR ") || line.contains("OutOfMemoryError")) {
                errors++;
            } else if (line.contains(" WARN ")) {
                warns++;
            }

            Matcher matcher = TIMESTAMP_PATTERN.matcher(line);
            if (matcher.find()) {
                String hhmmss = matcher.group(1);
                if (hhmmss.compareTo(startTime) >= 0 && hhmmss.compareTo(endTime) <= 0
                        && (line.contains("ERROR") || line.contains("OutOfMemoryError"))) {
                    matched.add(line);
                }
            }
        }

        Files.createDirectories(outputDir);
        Path tmpFile = Files.createTempFile(outputDir, "diag-", ".tmp");
        try {
            StringBuilder report = new StringBuilder();
            report.append("window=").append(startTime).append("..").append(endTime).append('\n');
            report.append("total=").append(total)
                    .append(",errors=").append(errors)
                    .append(",warns=").append(warns)
                    .append(",matched=").append(matched.size()).append('\n');
            for (String m : matched) {
                report.append(m).append('\n');
            }
            Files.writeString(tmpFile, report.toString(), StandardCharsets.UTF_8);
            Path target = outputDir.resolve("diag-summary.txt");
            Files.move(tmpFile, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tmpFile);
        }

        return new DiagSummary(total, errors, warns, matched);
    }
}
