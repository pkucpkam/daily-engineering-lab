package lab.linux.perf;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bộ phân tích đầu ra vmstat / mpstat / iostat và ánh xạ Linux TID -> nid=0x... trong JVM Thread Dump.
 */
public final class PerfTriageAnalyzer {

    public enum BottleneckType {
        HEALTHY,
        CPU_SATURATION,
        SINGLE_CORE_HOTSPOT,
        DISK_IO_BOTTLENECK,
        SWAP_THRASHING,
        CPU_STEAL_HYPERVISOR
    }

    public record VmstatSample(int r, int b, long si, long so, int us, int sy, int id, int wa, int st) {}

    public BottleneckType diagnoseVmstat(VmstatSample sample, int cpuCores) {
        if (sample.si() > 100 || sample.so() > 100) {
            return BottleneckType.SWAP_THRASHING;
        }
        if (sample.st() >= 15) {
            return BottleneckType.CPU_STEAL_HYPERVISOR;
        }
        if (sample.wa() >= 25 && sample.b() >= 2) {
            return BottleneckType.DISK_IO_BOTTLENECK;
        }
        if (sample.r() >= cpuCores && (sample.us() + sample.sy()) >= 75) {
            return BottleneckType.CPU_SATURATION;
        }
        return BottleneckType.HEALTHY;
    }

    public BottleneckType diagnoseMpstatPerCore(List<Integer> perCoreUsrPercent) {
        long saturatedCores = perCoreUsrPercent.stream().filter(p -> p >= 90).count();
        long idleCores = perCoreUsrPercent.stream().filter(p -> p <= 10).count();
        if (saturatedCores == 1 && idleCores >= 2) {
            return BottleneckType.SINGLE_CORE_HOTSPOT;
        }
        if (saturatedCores == perCoreUsrPercent.size()) {
            return BottleneckType.CPU_SATURATION;
        }
        return BottleneckType.HEALTHY;
    }

    public String toThreadDumpHexNid(long linuxTid) {
        return "0x" + Long.toHexString(linuxTid);
    }

    public Optional<String> findThreadNameByLinuxTid(String threadDump, long linuxTid) {
        String hexNid = toThreadDumpHexNid(linuxTid);
        Pattern pattern = Pattern.compile(
                "^\"([^\"]+)\"[^\\r\\n]*\\bnid=" + Pattern.quote(hexNid) + "\\b",
                Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(threadDump);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }
}
