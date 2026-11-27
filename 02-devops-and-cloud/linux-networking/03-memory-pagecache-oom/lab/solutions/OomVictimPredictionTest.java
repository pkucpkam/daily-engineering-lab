package lab.linux.oom.solutions;

import lab.linux.oom.OomScoreCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lời giải E7: Dự đoán nạn nhân OOM Killer với 4 process và oom_score_adj khác nhau.
 */
class OomVictimPredictionTest {

    @Test
    void predictsOomVictimAcrossFourProcessesWithAdjustments() {
        OomScoreCalculator calc = new OomScoreCalculator();
        List<OomScoreCalculator.ProcessMemory> procs = List.of(
                new OomScoreCalculator.ProcessMemory(100, "sshd", 128, 0, -1000),
                new OomScoreCalculator.ProcessMemory(200, "postgres", 4000, 0, -300),
                new OomScoreCalculator.ProcessMemory(300, "jboss-java", 9000, 200, 0),
                new OomScoreCalculator.ProcessMemory(400, "log-collector", 1600, 0, 500)
        );

        OomScoreCalculator.ScoredProcess victim = calc.selectVictim(procs, 16000);
        assertThat(victim.process().name()).isEqualTo("log-collector");
        assertThat(victim.effectiveScore()).isEqualTo(600);
        assertThat(calc.score(procs.get(1), 16000).effectiveScore()).isZero();
        assertThat(calc.score(procs.get(2), 16000).effectiveScore()).isEqualTo(575);
    }
}
