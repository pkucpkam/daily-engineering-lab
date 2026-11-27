package lab.linux.processes;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Runner chạy thật process con Java 21 để so sánh Graceful Drain vs Forcible Kill (SIGKILL)
 * và mô phỏng bảng trạng thái Zombie / D-state / PID 1 reaper.
 */
public final class ProcessLifecycleLabRunner {

    public record RunOutcome(int exitCode, List<String> lines, long completedCount) {}

    public static void main(String[] args) throws Exception {
        System.out.println("=== LNX-01 Process, Thread & Signal Lab ===");

        RunOutcome graceful = runChildProcess(true, 3, 250, 3000);
        System.out.printf("[1] Graceful shutdown: exitCode=%d, completedTasks=%d%n",
                graceful.exitCode(), graceful.completedCount());

        RunOutcome killed = runChildProcess(false, 3, 600, 3000);
        System.out.printf("[2] Forcible kill (mô phỏng SIGKILL): exitCode=%d, completedTasks=%d%n",
                killed.exitCode(), killed.completedCount());

        ProcessLifecycleSimulator noReaperPid1 = new ProcessLifecycleSimulator(false);
        for (int i = 0; i < 5; i++) {
            var child = noReaperPid1.spawn(200 + i, 100, "wkhtmltopdf");
            child.exitNormally(0);
        }
        System.out.println("[3] Quên wait() -> Zombie count = " + noReaperPid1.countZombies());
        noReaperPid1.killParentAndOrphanChildren(100);
        System.out.println("    Sau khi cha chết với PID 1 không có reaper -> Zombie count = "
                + noReaperPid1.countZombies());

        ProcessLifecycleSimulator tiniPid1 = new ProcessLifecycleSimulator(true);
        for (int i = 0; i < 5; i++) {
            var child = tiniPid1.spawn(200 + i, 100, "wkhtmltopdf");
            child.exitNormally(0);
        }
        tiniPid1.killParentAndOrphanChildren(100);
        System.out.println("    Sau khi cha chết với PID 1 là tini/systemd -> Zombie count = "
                + tiniPid1.countZombies());
    }

    public static RunOutcome runChildProcess(
            boolean graceful,
            int inFlight,
            long workDurationMs,
            long drainTimeoutMs
    ) throws Exception {
        String javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = resolveRuntimeClasspath();

        ProcessBuilder pb = new ProcessBuilder(
                javaBin,
                "-cp", classpath,
                GracefulShutdownApp.class.getName(),
                String.valueOf(inFlight),
                String.valueOf(workDurationMs),
                String.valueOf(drainTimeoutMs)
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        List<String> output = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
                if (line.startsWith("READY ")) {
                    if (graceful) {
                        writer.println("STOP");
                    } else {
                        process.destroyForcibly();
                        break;
                    }
                }
            }
            if (!graceful) {
                while ((line = reader.readLine()) != null) {
                    output.add(line);
                }
            }
        }

        boolean exited = process.waitFor(10, TimeUnit.SECONDS);
        if (!exited) {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
        }
        int exitCode = process.exitValue();
        long completed = output.stream().filter(l -> l.startsWith("TASK_COMPLETED")).count();
        return new RunOutcome(exitCode, output, completed);
    }

    private static String resolveRuntimeClasspath() {
        Set<String> entries = new LinkedHashSet<>();
        String sysCp = System.getProperty("java.class.path");
        if (sysCp != null && !sysCp.isBlank()) {
            for (String part : sysCp.split(File.pathSeparator)) {
                entries.add(part);
            }
        }
        ClassLoader cl = GracefulShutdownApp.class.getClassLoader();
        while (cl != null) {
            if (cl instanceof URLClassLoader ucl) {
                for (URL url : ucl.getURLs()) {
                    try {
                        entries.add(Path.of(url.toURI()).toString());
                    } catch (URISyntaxException ignored) {
                        entries.add(url.getPath());
                    }
                }
            }
            cl = cl.getParent();
        }
        return String.join(File.pathSeparator, entries);
    }
}
