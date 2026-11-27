package lab.linux.processes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Child JVM app minh họa Shutdown Hook + drain in-flight request khi nhận tín hiệu dừng.
 * <p>
 * Nhận lệnh dừng graceful qua stdin ("STOP") hoặc khi OS gửi SIGTERM (shutdown hook).
 */
public final class GracefulShutdownApp {

    public static void main(String[] args) throws Exception {
        int inFlightCount = args.length > 0 ? Integer.parseInt(args[0]) : 3;
        long workDurationMs = args.length > 1 ? Long.parseLong(args[1]) : 250L;
        long drainTimeoutMs = args.length > 2 ? Long.parseLong(args[2]) : 3000L;

        ExecutorService workers = Executors.newFixedThreadPool(Math.max(1, inFlightCount));
        AtomicBoolean accepting = new AtomicBoolean(true);
        AtomicInteger completed = new AtomicInteger(0);
        CountDownLatch startedLatch = new CountDownLatch(inFlightCount);
        CountDownLatch shutdownTrigger = new CountDownLatch(1);

        // Đăng ký JVM Shutdown Hook (chạy khi nhận SIGTERM trên Linux hoặc Ctrl+C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            accepting.set(false);
            drainExecutor(workers, drainTimeoutMs);
            System.out.println("HOOK_DONE completed=" + completed.get()
                    + " accepting=" + accepting.get());
            System.out.flush();
        }, "graceful-shutdown-hook"));

        for (int i = 0; i < inFlightCount; i++) {
            final int id = i + 1;
            workers.submit(() -> {
                startedLatch.countDown();
                try {
                    Thread.sleep(workDurationMs);
                    completed.incrementAndGet();
                    System.out.println("TASK_COMPLETED id=" + id);
                    System.out.flush();
                } catch (InterruptedException ex) {
                    System.out.println("TASK_INTERRUPTED id=" + id);
                    System.out.flush();
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Báo cho parent process biết tất cả in-flight task đã bắt đầu chạy
        startedLatch.await();
        System.out.println("READY pid=" + ProcessHandle.current().pid() + " inFlight=" + inFlightCount);
        System.out.flush();

        Thread stdinWatcher = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if ("STOP".equalsIgnoreCase(line.trim())) {
                        shutdownTrigger.countDown();
                        return;
                    }
                }
            } catch (Exception ignored) {
                // Nếu stdin đóng sớm, thoát watcher
            }
        }, "stdin-stop-watcher");
        stdinWatcher.setDaemon(true);
        stdinWatcher.start();

        // Chờ tín hiệu STOP (tương đương nhận SIGTERM trên Linux) trong tối đa 15 giây
        shutdownTrigger.await(15, TimeUnit.SECONDS);
        accepting.set(false);
        boolean drained = drainExecutor(workers, drainTimeoutMs);
        System.out.println("GRACEFUL_EXIT drained=" + drained + " completed=" + completed.get());
        System.out.flush();
        System.exit(drained ? 0 : 2);
    }

    static boolean drainExecutor(ExecutorService executor, long timeoutMs) {
        executor.shutdown();
        try {
            if (executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
                return true;
            }
            executor.shutdownNow();
            return false;
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
