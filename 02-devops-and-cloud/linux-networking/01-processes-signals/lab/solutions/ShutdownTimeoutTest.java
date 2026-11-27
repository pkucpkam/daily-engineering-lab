package lab.linux.processes.solutions;

import lab.linux.processes.ProcessLifecycleLabRunner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lời giải E6: Chứng minh khi drainTimeout < thời gian chạy của task,
 * Shutdown Hook/exit handler tự ngắt để không vượt grace period của K8s/systemd.
 */
class ShutdownTimeoutTest {

    @Test
    void shortDrainTimeoutStopsExecutorAndExitsWithNonZeroCode() throws Exception {
        ProcessLifecycleLabRunner.RunOutcome outcome =
                ProcessLifecycleLabRunner.runChildProcess(true, 2, 1500, 150);

        assertThat(outcome.exitCode()).isEqualTo(2);
        assertThat(outcome.lines()).anyMatch(l -> l.contains("GRACEFUL_EXIT drained=false"));
    }
}
