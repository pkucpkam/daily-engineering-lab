package lab.jvm.metaspace;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetaspaceLeakTest {

    @Test
    void cleanThreadLocalAllowsClassLoaderUnloadingWhereasLeakRetainsClasses() throws Exception {
        RedeployLeakSimulator.SimulationResult clean = RedeployLeakSimulator.runSimulation(false, 120);
        RedeployLeakSimulator.SimulationResult leak = RedeployLeakSimulator.runSimulation(true, 120);

        assertThat(clean.hitOomMetaspace()).isFalse();
        assertThat(clean.unloadedClasses()).isGreaterThanOrEqualTo(80);
        assertThat(leak.liveLoadedClasses()).isGreaterThan(clean.liveLoadedClasses());
    }
}
