import lab.jvm.gclog.GcLogAnalyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * L3 Solution helper: compares two -Xlog:gc* files side-by-side and flags which
 * workload exhibits sawtooth post-GC baseline growth (leak) vs healthy churn.
 */
public class LogTriageSolution {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java -cp target/classes LogTriageSolution.java <normal.log> <leak.log>");
            return;
        }
        compare(Path.of(args[0]), Path.of(args[1]));
    }

    public static void compare(Path normalLog, Path leakLog) throws Exception {
        GcLogAnalyzer.Summary normal = GcLogAnalyzer.analyze(Files.readAllLines(normalLog));
        GcLogAnalyzer.Summary leak = GcLogAnalyzer.analyze(Files.readAllLines(leakLog));

        System.out.println("=== Triage Comparison ===");
        System.out.println("NORMAL : " + normal.formatReport());
        System.out.println("LEAK   : " + leak.formatReport());
        System.out.printf("Delta live floor growth: normal=+%.0f MB vs leak=+%.0f MB%n",
                normal.lastWindowFloorMb() - normal.firstWindowFloorMb(),
                leak.lastWindowFloorMb() - leak.firstWindowFloorMb());
    }
}
