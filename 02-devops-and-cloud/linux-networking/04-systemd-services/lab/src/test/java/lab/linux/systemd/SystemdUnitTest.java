package lab.linux.systemd;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemdUnitTest {

    @Test
    void linterDetectsAllSixPitfallsInBrokenUnitAndPassesProductionUnits() throws Exception {
        SystemdUnitLinter linter = new SystemdUnitLinter();
        String broken = Files.readString(Path.of("systemd/jboss-legacy-broken.service"));
        String javaApp = Files.readString(Path.of("systemd/java-app.service"));
        String jbossApp = Files.readString(Path.of("systemd/jboss-legacy.service"));

        List<String> findings = linter.lint(broken);
        assertThat(findings).hasSize(6);
        assertThat(linter.lint(javaApp)).isEmpty();
        assertThat(linter.lint(jbossApp)).isEmpty();
    }

    @Test
    void successExitStatus143PreventsFalseFailureOnJvmGracefulShutdown() {
        SystemdStateSimulator without143 = new SystemdStateSimulator(false, "on-failure", 5);
        without143.stopWithJvmSigtermExit143();
        assertThat(without143.state()).isEqualTo(SystemdStateSimulator.UnitState.FAILED);

        SystemdStateSimulator with143 = new SystemdStateSimulator(true, "on-failure", 5);
        with143.stopWithJvmSigtermExit143();
        assertThat(with143.state()).isEqualTo(SystemdStateSimulator.UnitState.INACTIVE_DEAD);
    }

    @Test
    void startLimitBurstHaltsCrashLoopAfterThresholdWhileUnboundedRestartLoopsForever() {
        SystemdStateSimulator unbounded = new SystemdStateSimulator(true, "always", 0);
        SystemdStateSimulator bounded = new SystemdStateSimulator(true, "on-failure", 5);

        for (int i = 0; i < 10; i++) {
            unbounded.onProcessCrash(1);
            bounded.onProcessCrash(1);
        }

        assertThat(unbounded.restarts()).isEqualTo(10);
        assertThat(unbounded.state()).isEqualTo(SystemdStateSimulator.UnitState.ACTIVE_RUNNING);

        assertThat(bounded.restarts()).isEqualTo(5);
        assertThat(bounded.state()).isEqualTo(SystemdStateSimulator.UnitState.FAILED);
        assertThat(bounded.result()).isEqualTo("start-limit-hit");
    }
}
