package lab.linux.perf;

import java.util.List;

/**
 * Runner chạy thật CpuBurnWorkload và demo chẩn đoán vmstat/mpstat/top-H + kill -3 nid hex.
 */
public final class PerfTriageLabRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== LNX-05 Linux Performance Tools Lab ===");

        CpuBurnWorkload workload = new CpuBurnWorkload();
        CpuBurnWorkload.ThreadCpuComparison cmp = workload.measureBurnerVsWaiter(300);
        System.out.printf("[1] CpuBurnWorkload (300ms): cpu-burner=%d ms CPU | idle-io-waiter=%d ms CPU%n",
                cmp.burnerCpuMs(), cmp.waiterCpuMs());

        PerfTriageAnalyzer analyzer = new PerfTriageAnalyzer();
        var cpuSat = new PerfTriageAnalyzer.VmstatSample(21, 0, 0, 0, 88, 10, 1, 1, 0);
        var diskIo = new PerfTriageAnalyzer.VmstatSample(1, 19, 0, 0, 8, 4, 22, 66, 0);
        System.out.println("[2] Load ~20 trên 8 core (Kịch bản r=21, b=0, wa=1) => "
                + analyzer.diagnoseVmstat(cpuSat, 8));
        System.out.println("    Load ~20 trên 8 core (Kịch bản r=1, b=19, wa=66) => "
                + analyzer.diagnoseVmstat(diskIo, 8));

        var singleCore = analyzer.diagnoseMpstatPerCore(List.of(1, 0, 2, 99, 1, 0, 1, 0));
        System.out.println("[3] mpstat per-core [1,0,2,99,1,0,1,0] => " + singleCore);

        String sampleDump = """
                "http-executor-16" #42 daemon prio=5 os_prio=0 tid=0x00007f tid=0x1 nid=0x12e6 waiting on condition
                "http-executor-17" #43 daemon prio=5 os_prio=0 tid=0x00007f tid=0x2 nid=0x12e7 runnable
                """;
        String threadName = analyzer.findThreadNameByLinuxTid(sampleDump, 4839).orElse("NOT_FOUND");
        System.out.printf("[4] top -H TID 4839 -> hex=%s -> Thread Dump match: %s%n",
                analyzer.toThreadDumpHexNid(4839), threadName);
    }
}
