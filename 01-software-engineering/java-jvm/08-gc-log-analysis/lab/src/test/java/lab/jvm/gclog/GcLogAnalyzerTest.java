package lab.jvm.gclog;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GcLogAnalyzerTest {

    @Test
    void detectsHealthyFlatLiveSetDespiteHighAllocationRate() {
        List<String> healthyLog = List.of(
                "[0.200s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 100M->26M(256M) 1.400ms",
                "[0.400s][info][gc] GC(1) Pause Young (Normal) (G1 Evacuation Pause) 140M->27M(256M) 1.600ms",
                "[0.600s][info][gc] GC(2) Pause Young (Normal) (G1 Evacuation Pause) 142M->26M(256M) 1.500ms",
                "[0.800s][info][gc] GC(3) Pause Young (Normal) (G1 Evacuation Pause) 145M->28M(256M) 1.700ms",
                "[1.000s][info][gc] GC(4) Pause Young (Normal) (G1 Evacuation Pause) 144M->26M(256M) 1.300ms",
                "[1.200s][info][gc] GC(5) Pause Young (Normal) (G1 Evacuation Pause) 146M->27M(256M) 1.450ms"
        );

        GcLogAnalyzer.Summary summary = GcLogAnalyzer.analyze(healthyLog);

        assertThat(summary.totalPauses()).isEqualTo(6);
        assertThat(summary.fullGcCount()).isZero();
        assertThat(summary.allocationRateMbPerSec()).isGreaterThan(400.0);
        assertThat(summary.firstWindowFloorMb()).isEqualTo(26.0);
        assertThat(summary.lastWindowFloorMb()).isEqualTo(26.0);
        assertThat(summary.leakSuspected()).isFalse();
        assertThat(summary.throughputPct()).isGreaterThan(99.0);
    }

    @Test
    void detectsSawtoothClimbingFloorAsMemoryLeak() {
        List<String> leakLog = List.of(
                "[0.200s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 96M->30M(192M) 1.500ms",
                "[0.400s][info][gc] GC(1) Pause Young (Normal) (G1 Evacuation Pause) 110M->55M(192M) 2.200ms",
                "[0.600s][info][gc] GC(2) Pause Young (Concurrent Start) (G1 Evacuation Pause) 130M->88M(192M) 3.400ms",
                "[0.800s][info][gc] GC(3) Pause Young (Normal) (G1 Evacuation Pause) 150M->124M(192M) 4.100ms",
                "[1.000s][info][gc] GC(4) Pause Young (Normal) (G1 Evacuation Pause) 182M->160M(192M) 5.000ms",
                "[1.100s][info][gc] GC(5) Pause Full (G1 Compaction Pause) 190M->178M(192M) 48.200ms"
        );

        GcLogAnalyzer.Summary summary = GcLogAnalyzer.analyze(leakLog);

        assertThat(summary.fullGcCount()).isEqualTo(1);
        assertThat(summary.maxPauseMs()).isEqualTo(48.2);
        assertThat(summary.lastWindowFloorMb()).isGreaterThan(summary.firstWindowFloorMb() * 3.0);
        assertThat(summary.leakSuspected()).isTrue();
    }
}
