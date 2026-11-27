package lab.linux.perf.solutions;

import lab.linux.perf.PerfTriageAnalyzer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lời giải E7: Nhận diện CPU Steal từ hypervisor qua cột `st` của vmstat.
 */
class StealTimeTriageTest {

    @Test
    void highStealTimeIsDiagnosedAsHypervisorContention() {
        PerfTriageAnalyzer analyzer = new PerfTriageAnalyzer();
        var noisyNeighborSample = new PerfTriageAnalyzer.VmstatSample(4, 0, 0, 0, 35, 10, 25, 0, 30);

        assertThat(analyzer.diagnoseVmstat(noisyNeighborSample, 4))
                .isEqualTo(PerfTriageAnalyzer.BottleneckType.CPU_STEAL_HYPERVISOR);
    }
}
