package lab.jvm.matjfr;

import com.sun.management.HotSpotDiagnosticMXBean;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class HeapMatJfrTest {

    @Test
    void dumpsHprofAndCapturesJfrHotspots(@TempDir Path tempDir) throws Exception {
        LeakyCacheApp.populateLeakedSessions(16);
        try {
            // 1. Capture JFR profile (allocation + monitor lock events)
            Path jfrFile = tempDir.resolve("test-profile.jfr");
            Configuration profile = Configuration.getConfiguration("profile");
            try (Recording recording = new Recording(profile)) {
                recording.enable("jdk.JavaMonitorEnter").withThreshold(Duration.ofMillis(0));
                recording.enable("jdk.ObjectAllocationSample");
                recording.start();
                LeakyCacheApp.runAllocationAndLockWorkload(350);
                recording.stop();
                recording.dump(jfrFile);
            }

            JfrInspector.JfrSummary summary = JfrInspector.inspect(jfrFile);
            assertThat(summary.totalEvents()).isGreaterThan(0);
            assertThat(summary.lockEvents()).isGreaterThan(0);

            // 2. Capture live heap dump via HotSpotDiagnosticMXBean (same VM mechanism as jcmd GC.heap_dump)
            Path hprofFile = tempDir.resolve("test-heap.hprof");
            HotSpotDiagnosticMXBean diag = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
            diag.dumpHeap(hprofFile.toString(), true);

            assertThat(Files.size(hprofFile)).isGreaterThan(1024 * 1024);
            byte[] header = new byte[12];
            try (var in = Files.newInputStream(hprofFile)) {
                int read = in.read(header);
                assertThat(read).isEqualTo(12);
            }
            assertThat(new String(header, StandardCharsets.US_ASCII)).startsWith("JAVA PROFILE");
        } finally {
            LeakyCacheApp.clearSessions();
        }
    }
}
