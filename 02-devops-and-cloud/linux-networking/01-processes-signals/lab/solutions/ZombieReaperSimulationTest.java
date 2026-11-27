package lab.linux.processes.solutions;

import lab.linux.processes.ProcessLifecycleSimulator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lời giải E8: So sánh PID 1 có reaper (tini/systemd) vs PID 1 không reap zombie.
 */
class ZombieReaperSimulationTest {

    @Test
    void orphanZombiesAreReapedOnlyWhenPid1ActsAsReaper() {
        ProcessLifecycleSimulator dumbPid1 = new ProcessLifecycleSimulator(false);
        ProcessLifecycleSimulator tiniPid1 = new ProcessLifecycleSimulator(true);

        for (int i = 0; i < 100; i++) {
            dumbPid1.spawn(1000 + i, 50, "wkhtmltopdf").exitNormally(0);
            tiniPid1.spawn(1000 + i, 50, "wkhtmltopdf").exitNormally(0);
        }

        dumbPid1.killParentAndOrphanChildren(50);
        tiniPid1.killParentAndOrphanChildren(50);

        assertThat(dumbPid1.countZombies()).isEqualTo(100);
        assertThat(tiniPid1.countZombies()).isZero();
    }
}
