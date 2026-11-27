package lab.linux.perf;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PerfToolsTest {

    @Test
    void cpuBurnerConsumesSignificantlyMoreThreadCpuTimeThanSleepingWaiter() throws Exception {
        CpuBurnWorkload workload = new CpuBurnWorkload();
        CpuBurnWorkload.ThreadCpuComparison cmp = workload.measureBurnerVsWaiter(250);

        assertThat(cmp.burnerCpuMs()).isGreaterThan(80L);
        assertThat(cmp.burnerCpuMs()).isGreaterThan(cmp.waiterCpuMs() * 3 + 20L);
    }

    @Test
    void analyzerDistinguishesSameLoadAverage20IntoCpuSaturationVsDiskIoBottleneck() {
        PerfTriageAnalyzer analyzer = new PerfTriageAnalyzer();

        var cpuSat = new PerfTriageAnalyzer.VmstatSample(21, 0, 0, 0, 88, 10, 1, 1, 0);
        var diskIo = new PerfTriageAnalyzer.VmstatSample(1, 19, 0, 0, 8, 4, 22, 66, 0);

        assertThat(analyzer.diagnoseVmstat(cpuSat, 8)).isEqualTo(PerfTriageAnalyzer.BottleneckType.CPU_SATURATION);
        assertThat(analyzer.diagnoseVmstat(diskIo, 8)).isEqualTo(PerfTriageAnalyzer.BottleneckType.DISK_IO_BOTTLENECK);
    }

    @Test
    void analyzerDetectsSingleCoreHotspotAndMapsTopTidToThreadDumpHexNid() {
        PerfTriageAnalyzer analyzer = new PerfTriageAnalyzer();

        assertThat(analyzer.diagnoseMpstatPerCore(List.of(1, 0, 2, 99, 1, 0, 1, 0)))
                .isEqualTo(PerfTriageAnalyzer.BottleneckType.SINGLE_CORE_HOTSPOT);

        String dump = """
                "http-executor-17" #43 daemon prio=5 nid=0x12e7 runnable
                "db-poller-1" #44 daemon prio=5 nid=0x1400 waiting on condition
                "gc-worker" #45 daemon prio=5 nid=0x2334 runnable
                """;
        assertThat(analyzer.toThreadDumpHexNid(4839)).isEqualTo("0x12e7");
        assertThat(analyzer.findThreadNameByLinuxTid(dump, 4839)).contains("http-executor-17");
        assertThat(analyzer.findThreadNameByLinuxTid(dump, 5120)).contains("db-poller-1");
        assertThat(analyzer.findThreadNameByLinuxTid(dump, 9012)).contains("gc-worker");
    }
}
