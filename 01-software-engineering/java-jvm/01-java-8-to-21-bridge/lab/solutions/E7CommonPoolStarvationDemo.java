package lab.jvm.bridge.solutions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

/**
 * Reference demonstration for Exercise E7:
 * Blocking inside parallelStream() saturates ForkJoinPool.commonPool(),
 * delaying unrelated CompletableFuture.supplyAsync() tasks.
 */
public class E7CommonPoolStarvationDemo {

    public static void main(String[] args) throws Exception {
        int parallelism = ForkJoinPool.commonPool().getParallelism();
        System.out.println("commonPool parallelism = " + parallelism);

        // Saturate every worker thread in commonPool + calling thread with a 500ms blocking call
        Thread blocker = new Thread(() ->
                IntStream.range(0, parallelism * 2)
                        .parallel()
                        .forEach(i -> {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }));
        blocker.start();
        Thread.sleep(50); // ensure parallel workers are already parked in sleep()

        long start = System.nanoTime();
        String result = CompletableFuture.supplyAsync(() -> "OK").get();
        long waitedMs = (System.nanoTime() - start) / 1_000_000;

        blocker.join();
        System.out.printf("Unrelated CompletableFuture returned '%s' after waiting %d ms%n", result, waitedMs);
    }
}
