package lab.linux.oom;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Runner chạy kiểm chứng đo Direct Memory off-heap, đọc đúng free -m, và dự đoán OOM Killer.
 */
public final class MemoryOomLabRunner {

    public static void main(String[] args) {
        System.out.println("=== LNX-03 Memory, Page Cache & OOM Killer Lab ===");

        MemoryFootprintAnalyzer analyzer = new MemoryFootprintAnalyzer();
        MemoryFootprintAnalyzer.Snapshot before = analyzer.captureSnapshot();
        List<ByteBuffer> directBlocks = analyzer.allocateDirectMegabytes(64);
        MemoryFootprintAnalyzer.Snapshot after = analyzer.captureSnapshot();
        System.out.printf("[1] Before allocateDirect: heap=%.1f MB, direct=%.1f MB%n",
                before.heapUsedBytes() / 1048576.0, before.directBufferBytes() / 1048576.0);
        System.out.printf("    After +64MB direct : heap=%.1f MB, direct=%.1f MB (allocated=%d buffers)%n",
                after.heapUsedBytes() / 1048576.0, after.directBufferBytes() / 1048576.0, directBlocks.size());

        String sampleHealthy = """
                               total        used        free      shared  buff/cache   available
                Mem:           16000        4800         200         120       11000       10800
                Swap:           2048           0        2048
                """;
        FreeOutputParser parser = new FreeOutputParser();
        FreeOutputParser.FreeReport report = parser.parse(sampleHealthy);
        System.out.printf("[2] free -m parser (free=%d MB, buff/cache=%d MB, available=%d MB) => %s%n",
                report.memFreeMb(), report.buffCacheMb(), report.memAvailableMb(), report.status());

        OomScoreCalculator calc = new OomScoreCalculator();
        List<OomScoreCalculator.ProcessMemory> procs = List.of(
                new OomScoreCalculator.ProcessMemory(101, "sshd", 64, 0, -1000),
                new OomScoreCalculator.ProcessMemory(4812, "java-jboss", 10400, 120, 0),
                new OomScoreCalculator.ProcessMemory(5011, "nginx", 320, 0, 0)
        );
        OomScoreCalculator.ScoredProcess victim = calc.selectVictim(procs, 16000);
        System.out.printf("[3] OOM Killer victim => pid=%d (%s) base=%d effective=%d%n",
                victim.process().pid(), victim.process().name(), victim.baseScore(), victim.effectiveScore());
    }
}
