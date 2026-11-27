package lab.linux.systemd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bộ kiểm tra tĩnh (linter) các unit file systemd cho Java / JBoss theo chuẩn production.
 */
public final class SystemdUnitLinter {

    public List<String> lint(String unitContent) {
        Map<String, String> values = parseDirectives(unitContent);
        List<String> findings = new ArrayList<>();

        String user = values.getOrDefault("User", "").trim();
        if (user.isEmpty() || "root".equals(user)) {
            findings.add("SECURITY: Service đang thiếu User= non-root hoặc đang chạy dưới root.");
        }

        String execStart = values.getOrDefault("ExecStart", "");
        if (execStart.contains("/bin/sh -c") || execStart.contains("/bin/bash -c")) {
            findings.add("SIGNAL: ExecStart dùng shell wrapper (/bin/sh -c) có thể nuốt SIGTERM.");
        }

        if (!values.containsKey("SuccessExitStatus") || !values.get("SuccessExitStatus").contains("143")) {
            findings.add("JVM_EXIT: Thiếu SuccessExitStatus=143 -> systemctl stop sẽ báo unit failed.");
        }

        String restart = values.getOrDefault("Restart", "no").trim();
        if ("always".equals(restart) && !values.containsKey("StartLimitBurst")) {
            findings.add("CRASH_LOOP: Restart=always thiếu StartLimitBurst dễ gây crash-loop vô hạn.");
        }

        if (!values.containsKey("LimitNOFILE")) {
            findings.add("FD_LIMIT: Thiếu LimitNOFILE -> systemd không đọc limits.conf, dễ dính 1024 FD.");
        }

        if (!values.containsKey("MemoryMax")) {
            findings.add("CGROUP_MEM: Thiếu MemoryMax -> tiến trình leak có thể đánh sập toàn host.");
        }

        return findings;
    }

    public Map<String, String> parseDirectives(String unitContent) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String rawLine : unitContent.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";") || line.startsWith("[")) {
                continue;
            }
            int idx = line.indexOf('=');
            if (idx > 0) {
                String key = line.substring(0, idx).trim();
                String val = line.substring(idx + 1).trim();
                map.put(key, val);
            }
        }
        return map;
    }
}
