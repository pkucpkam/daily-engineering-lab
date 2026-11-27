package lab.jvm.jit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JitTest {

    @Test
    void taxRulesComputeConsistentResultsBeforeAndAfterWarmup() {
        JitWarmupRunner.TaxRule std = new JitWarmupRunner.StandardTax();
        JitWarmupRunner.TaxRule vip = new JitWarmupRunner.VipTax();

        long first = std.compute(1_000);
        long warmed = 0;
        for (int i = 0; i < 30_000; i++) {
            warmed = std.compute(1_000);
        }
        assertThat(warmed).isEqualTo(first).isEqualTo(100L);
        assertThat(vip.compute(1_000)).isEqualTo(50L);
    }
}
