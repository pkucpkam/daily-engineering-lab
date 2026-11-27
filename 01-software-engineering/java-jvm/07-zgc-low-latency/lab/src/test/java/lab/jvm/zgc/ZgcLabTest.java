package lab.jvm.zgc;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ZgcLabTest {

    @Test
    void gcMxBeansAreExposedInRuntime() {
        assertThat(ManagementFactory.getGarbageCollectorMXBeans()).isNotEmpty();
    }
}
