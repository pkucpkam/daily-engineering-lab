package lab.linux.perf;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tạo tải CPU thật vs thread ngủ I/O và đo trực tiếp CPU time qua ThreadMXBean
 * để thấy rõ sự khác biệt giữa thread R (burner) và thread S (idle/waiting).
 */
public final class CpuBurnWorkload {

    public record ThreadCpuComparison(long burnerCpuMs, long waiterCpuMs, long burnerThreadId) {}

    public ThreadCpuComparison measureBurnerVsWaiter(long durationMs) throws InterruptedException {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (bean.isThreadCpuTimeSupported() && !bean.isThreadCpuTimeEnabled()) {
            bean.setThreadCpuTimeEnabled(true);
        }

        AtomicBoolean running = new AtomicBoolean(true);
        CountDownLatch ready = new CountDownLatch(2);
        final long[] cpuTimesNs = new long[2];
        final long[] threadIds = new long[2];

        Thread burner = new Thread(() -> {
            threadIds[0] = Thread.currentThread().threadId();
            ready.countDown();
            double acc = 1.0001;
            while (running.get()) {
                acc = Math.sin(acc) + Math.sqrt(acc + 10.0);
            }
            if (acc == -12345.0) {
                System.out.println(acc);
            }
            cpuTimesNs[0] = bean.getCurrentThreadCpuTime();
        }, "cpu-burner-worker");

        Thread waiter = new Thread(() -> {
            threadIds[1] = Thread.currentThread().threadId();
            ready.countDown();
            try {
                while (running.get()) {
                    Thread.sleep(25);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            cpuTimesNs[1] = bean.getCurrentThreadCpuTime();
        }, "idle-io-waiter");

        burner.start();
        waiter.start();
        ready.await();
        Thread.sleep(durationMs);
        running.set(false);
        burner.join();
        waiter.join();

        return new ThreadCpuComparison(
                cpuTimesNs[0] / 1_000_000L,
                cpuTimesNs[1] / 1_000_000L,
                threadIds[0]
        );
    }
}
