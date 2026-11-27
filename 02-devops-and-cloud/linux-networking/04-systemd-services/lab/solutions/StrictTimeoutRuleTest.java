package lab.linux.systemd.solutions;

import lab.linux.systemd.SystemdUnitLinter;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lời giải E6: Kiểm tra thêm TimeoutStopSec tối thiểu 10 giây để JVM kịp hoàn tất drain.
 */
class StrictTimeoutRuleTest {

    @Test
    void productionUnitsConfigureAdequateTimeoutStopSec() throws Exception {
        SystemdUnitLinter linter = new SystemdUnitLinter();
        String javaApp = Files.readString(Path.of("systemd/java-app.service"));
        String jbossApp = Files.readString(Path.of("systemd/jboss-legacy.service"));

        assertThat(parseSeconds(linter.parseDirectives(javaApp))).isGreaterThanOrEqualTo(10);
        assertThat(parseSeconds(linter.parseDirectives(jbossApp))).isGreaterThanOrEqualTo(10);
    }

    private int parseSeconds(Map<String, String> directives) {
        String raw = directives.getOrDefault("TimeoutStopSec", "0").replace("s", "").trim();
        return Integer.parseInt(raw);
    }
}
