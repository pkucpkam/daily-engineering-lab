package lab.dsa.complexity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexityLabTest {

    @Test
    void contiguousIntArrayTraversalDramaticallyBeatsFragmentedLinkedList() {
        CacheLocalityBenchmark bench = new CacheLocalityBenchmark();
        var report = bench.benchmarkTraversal(250_000);

        assertThat(report.intArrayUs()).isLessThan(report.linkedListUs());
    }

    @Test
    void arrayListGrowthCopiesLinearTotalElementsProvingAmortizedO1() {
        CacheLocalityBenchmark bench = new CacheLocalityBenchmark();
        var stats = bench.measureArrayListGrowth(1_000_000);

        assertThat(stats.resizeCount()).isEqualTo(29);
        assertThat(stats.totalCopiedElements()).isBetween(2_000_000L, 3_000_000L);
    }
}
