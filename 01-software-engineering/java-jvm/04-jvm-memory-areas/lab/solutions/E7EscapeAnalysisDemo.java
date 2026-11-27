package lab.jvm.memory.solutions;

/**
 * Reference solution for Exercise E7 (Escape Analysis & Scalar Replacement):
 * Run twice to compare Young GC counts:
 *   1) java -Xmx128m -Xlog:gc* E7EscapeAnalysisDemo.java
 *   2) java -Xmx128m -Xlog:gc* -XX:-DoEscapeAnalysis E7EscapeAnalysisDemo.java
 */
public class E7EscapeAnalysisDemo {

    private record Point(int x, int y) {
    }

    private static volatile long blackhole;

    public static void main(String[] args) {
        long start = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < 100_000_000; i++) {
            sum += distanceSquared(i & 1023, (i + 1) & 1023);
        }
        blackhole = sum;
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("Completed 100M iterations in %d ms (blackhole=%d)%n", elapsedMs, blackhole);
    }

    private static long distanceSquared(int x, int y) {
        Point p = new Point(x, y);
        return (long) p.x() * p.x() + (long) p.y() * p.y();
    }
}
