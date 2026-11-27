package lab.linux.processes;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessLifecycleTest {

    @Test
    void gracefulShutdownDrainsAllInFlightTasksBeforeExiting() throws Exception {
        ProcessLifecycleLabRunner.RunOutcome outcome =
                ProcessLifecycleLabRunner.runChildProcess(true, 3, 200, 3000);

        assertThat(outcome.exitCode()).isZero();
        assertThat(outcome.completedCount()).isEqualTo(3);
        assertThat(outcome.lines()).anyMatch(l -> l.contains("GRACEFUL_EXIT drained=true completed=3"));
    }

    @Test
    void forcibleKillAbortsInFlightWorkBeforeCompletion() throws Exception {
        ProcessLifecycleLabRunner.RunOutcome outcome =
                ProcessLifecycleLabRunner.runChildProcess(false, 3, 2500, 3000);

        assertThat(outcome.completedCount()).isLessThan(3);
        assertThat(outcome.exitCode()).isNotZero();
    }

    @Test
    void simulatorModelsZombieCreationSigkillImmunityAndPid1Reaping() {
        ProcessLifecycleSimulator sim = new ProcessLifecycleSimulator(true);
        var zombieChild = sim.spawn(301, 100, "pdf-worker");
        var blockedIo = sim.spawn(302, 100, "nfs-reader");
        blockedIo.enterUninterruptibleIo();
        zombieChild.exitNormally(0);

        // SIGKILL không tác dụng lên Zombie và tiến trình kẹt D-state
        assertThat(zombieChild.sendSigKill()).isFalse();
        assertThat(blockedIo.sendSigKill()).isFalse();
        assertThat(sim.countZombies()).isEqualTo(1);

        // Khi cha chưa wait(), zombie giữ nguyên; khi cha chết và PID 1 là tini/systemd, zombie được reap
        sim.killParentAndOrphanChildren(100);
        assertThat(sim.countZombies()).isZero();
    }
}
