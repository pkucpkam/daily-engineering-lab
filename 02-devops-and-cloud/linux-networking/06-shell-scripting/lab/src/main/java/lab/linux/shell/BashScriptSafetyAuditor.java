package lab.linux.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Bộ kiểm tra tĩnh cho script Bash vận hành:
 * - Phát hiện CRLF (\r\n) gây lỗi trên Linux.
 * - Kiểm tra `set -euo pipefail`.
 * - Kiểm tra `trap ... EXIT` khi script tạo `mktemp`.
 * - Phát hiện `rm -rf $VAR` không ngoặc kép và `cd` không kiểm tra lỗi.
 */
public final class BashScriptSafetyAuditor {

    private static final Pattern DOUBLE_QUOTED = Pattern.compile("\"[^\"]*\"");
    private static final Pattern UNQUOTED_RM = Pattern.compile("\\brm\\s+[^\\n]*\\$(?:[A-Za-z_][A-Za-z0-9_]*|\\{[^}]+\\})");
    private static final Pattern UNCHECKED_CD = Pattern.compile("^\\s*cd\\s+[^|&;\\n]+\\s*$");

    public List<String> audit(String scriptContent) {
        List<String> findings = new ArrayList<>();
        if (scriptContent.contains("\r\n")) {
            findings.add("LINE_ENDING: Phát hiện CRLF (\\r\\n) từ Windows -> Linux bash sẽ lỗi 'bash\\r'.");
        }
        if (!scriptContent.contains("set -euo pipefail")) {
            findings.add("STRICT_MODE: Thiếu 'set -euo pipefail' ở đầu script.");
        }
        if (scriptContent.contains("mktemp") && !scriptContent.contains("trap ")) {
            findings.add("CLEANUP: Có dùng mktemp nhưng thiếu 'trap ... EXIT' để dọn thư mục tạm.");
        }

        boolean strictMode = scriptContent.contains("set -euo pipefail") || scriptContent.contains("set -e");
        String[] lines = scriptContent.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String withoutDoubleQuotes = DOUBLE_QUOTED.matcher(line).replaceAll("\"\"");
            if (UNQUOTED_RM.matcher(withoutDoubleQuotes).find()) {
                findings.add("QUOTING (line " + (i + 1) + "): Lệnh rm dùng biến không bọc ngoặc kép.");
            }
            if (!strictMode && UNCHECKED_CD.matcher(line).matches()) {
                findings.add("UNCHECKED_CD (line " + (i + 1) + "): Lệnh cd thiếu set -e hoặc '|| exit 1'.");
            }
        }
        return findings;
    }
}
