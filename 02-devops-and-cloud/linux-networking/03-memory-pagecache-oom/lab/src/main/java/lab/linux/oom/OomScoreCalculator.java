package lab.linux.oom;

import java.util.Comparator;
import java.util.List;

/**
 * Mô phỏng thuật toán chọn nạn nhân của Linux OOM Killer (oom_badness):
 * - base_score tỷ lệ với (RSS + Swap) / tổng bộ nhớ máy (thang 0..1000).
 * - Cộng oom_score_adj (-1000..+1000); nếu oom_score_adj == -1000 -> bất tử (0).
 * - Process có điểm hiệu dụng cao nhất sẽ nhận SIGKILL (exit 137).
 */
public final class OomScoreCalculator {

    public record ProcessMemory(
            int pid,
            String name,
            long rssMb,
            long swapMb,
            int oomScoreAdj
    ) {}

    public record ScoredProcess(
            ProcessMemory process,
            int baseScore,
            int effectiveScore
    ) {}

    public ScoredProcess score(ProcessMemory process, long totalAvailableMb) {
        if (process.oomScoreAdj() <= -1000) {
            return new ScoredProcess(process, 0, 0);
        }
        long consumed = Math.max(0L, process.rssMb() + process.swapMb());
        int base = (int) Math.min(1000L, Math.round((consumed * 1000.0) / Math.max(1L, totalAvailableMb)));
        int effective = Math.max(0, Math.min(1000, base + process.oomScoreAdj()));
        return new ScoredProcess(process, base, effective);
    }

    public ScoredProcess selectVictim(List<ProcessMemory> candidates, long totalAvailableMb) {
        return candidates.stream()
                .map(p -> score(p, totalAvailableMb))
                .filter(s -> s.effectiveScore() > 0)
                .max(Comparator.comparingInt(ScoredProcess::effectiveScore))
                .orElseThrow(() -> new IllegalStateException("No killable process (kernel panic!)"));
    }
}
