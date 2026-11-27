package lab.linux.systemd;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Runner chạy kiểm tra unit files + mô phỏng exit 143 và crash loop burst limit.
 */
public final class SystemdLabRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== LNX-04 systemd & Service Management Lab ===");
        SystemdUnitLinter linter = new SystemdUnitLinter();

        String broken = Files.readString(Path.of("systemd/jboss-legacy-broken.service"));
        List<String> brokenFindings = linter.lint(broken);
        System.out.println("[1] Lint jboss-legacy-broken.service => " + brokenFindings.size() + " findings:");
        brokenFindings.forEach(f -> System.out.println("    - " + f));

        String healthy = Files.readString(Path.of("systemd/java-app.service"));
        System.out.println("[2] Lint java-app.service => " + linter.lint(healthy).size() + " findings");

        SystemdStateSimulator without143 = new SystemdStateSimulator(false, "on-failure", 5);
        without143.stopWithJvmSigtermExit143();
        SystemdStateSimulator with143 = new SystemdStateSimulator(true, "on-failure", 5);
        with143.stopWithJvmSigtermExit143();
        System.out.printf("[3] Stop JVM (exit 143): missing SuccessExitStatus=%s (%s) | configured=%s%n",
                without143.state(), without143.result(), with143.state());

        SystemdStateSimulator unbounded = new SystemdStateSimulator(true, "always", 0);
        SystemdStateSimulator bounded = new SystemdStateSimulator(true, "on-failure", 5);
        for (int i = 0; i < 10; i++) {
            unbounded.onProcessCrash(1);
            bounded.onProcessCrash(1);
        }
        System.out.printf("[4] 10 crashes: Restart=always NRestarts=%d state=%s | Burst=5 NRestarts=%d state=%s (%s)%n",
                unbounded.restarts(), unbounded.state(),
                bounded.restarts(), bounded.state(), bounded.result());
    }
}
