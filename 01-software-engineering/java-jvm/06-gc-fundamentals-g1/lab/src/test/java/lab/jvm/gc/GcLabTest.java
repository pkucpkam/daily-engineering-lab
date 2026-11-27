package lab.jvm.gc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GcLabTest {

    @Test
    void collectorMxBeansAreRegisteredAndWorkloadCompletesCleanly() {
        assertThat(GcAllocationRunner.activeCollectorNames()).isNotEmpty();
        GcAllocationRunner.main(new String[]{"normal"});
    }
}
