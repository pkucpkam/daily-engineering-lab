package lab.linux.oom;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryOomTest {

    @Test
    void directByteBufferAllocationGrowsOffHeapPoolOutsideXmx() {
        MemoryFootprintAnalyzer analyzer = new MemoryFootprintAnalyzer();
        MemoryFootprintAnalyzer.Snapshot before = analyzer.captureSnapshot();

        List<ByteBuffer> allocated = analyzer.allocateDirectMegabytes(64);
        MemoryFootprintAnalyzer.Snapshot after = analyzer.captureSnapshot();

        long directDeltaMb = (after.directBufferBytes() - before.directBufferBytes()) / (1024 * 1024);
        assertThat(allocated).hasSize(64);
        assertThat(directDeltaMb).isGreaterThanOrEqualTo(64);
    }

    @Test
    void freeParserDistinguishesHealthyPageCacheFromCriticalSwapPressure() {
        FreeOutputParser parser = new FreeOutputParser();

        String healthy = """
                               total        used        free      shared  buff/cache   available
                Mem:           16000        4800         200         120       11000       10800
                Swap:           2048           0        2048
                """;
        String critical = """
                               total        used        free      shared  buff/cache   available
                Mem:           16000       14100         250         100        1650         820
                Swap:           2048        1890         158
                """;

        assertThat(parser.parse(healthy).status())
                .isEqualTo(FreeOutputParser.HealthStatus.HEALTHY_PAGE_CACHE_ACTIVE);
        assertThat(parser.parse(critical).status())
                .isEqualTo(FreeOutputParser.HealthStatus.CRITICAL_SWAP_THRASHING);
    }

    @Test
    void oomKillerSelectsProcessWithHighestEffectiveScoreAndSparesProtectedDaemons() {
        OomScoreCalculator calc = new OomScoreCalculator();
        List<OomScoreCalculator.ProcessMemory> procs = List.of(
                new OomScoreCalculator.ProcessMemory(101, "sshd", 128, 0, -1000),
                new OomScoreCalculator.ProcessMemory(4812, "jboss-java", 10400, 120, 0),
                new OomScoreCalculator.ProcessMemory(5011, "nginx", 320, 0, 0)
        );

        OomScoreCalculator.ScoredProcess victim = calc.selectVictim(procs, 16000);
        assertThat(victim.process().name()).isEqualTo("jboss-java");
        assertThat(calc.score(procs.get(0), 16000).effectiveScore()).isZero();
    }

    @Test
    void positiveOomScoreAdjCanPrioritizeSmallerBestEffortProcessForTermination() {
        OomScoreCalculator calc = new OomScoreCalculator();
        var jboss = new OomScoreCalculator.ProcessMemory(4812, "jboss-java", 8000, 0, 0);
        var batch = new OomScoreCalculator.ProcessMemory(6000, "batch-exporter", 1600, 0, 500);

        OomScoreCalculator.ScoredProcess victim = calc.selectVictim(List.of(jboss, batch), 16000);
        assertThat(victim.process().name()).isEqualTo("batch-exporter");
        assertThat(victim.effectiveScore()).isEqualTo(600);
    }
}
