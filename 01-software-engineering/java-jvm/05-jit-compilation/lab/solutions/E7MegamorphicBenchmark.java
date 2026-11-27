package lab.jvm.jit.solutions;

/**
 * Reference solution for Exercise E7:
 * Compares monomorphic (1 impl) vs megamorphic (4 impls) call sites in separate methods
 * so each method maintains an unpolluted JIT profile.
 */
public class E7MegamorphicBenchmark {

    interface Calc {
        long apply(long x);
    }

    static final class Impl1 implements Calc { public long apply(long x) { return x + 1; } }
    static final class Impl2 implements Calc { public long apply(long x) { return x + 2; } }
    static final class Impl3 implements Calc { public long apply(long x) { return x + 3; } }
    static final class Impl4 implements Calc { public long apply(long x) { return x + 4; } }

    private static volatile long blackhole;

    public static void main(String[] args) {
        Calc[] mono = { new Impl1(), new Impl1(), new Impl1(), new Impl1() };
        Calc[] mega = { new Impl1(), new Impl2(), new Impl3(), new Impl4() };

        // Warmup both separate call sites
        for (int w = 0; w < 5; w++) {
            blackhole += runLoopMono(mono, 1_000_000);
            blackhole += runLoopMega(mega, 1_000_000);
        }

        long t0 = System.nanoTime();
        blackhole = runLoopMono(mono, 100_000_000);
        long monoMs = (System.nanoTime() - t0) / 1_000_000;

        long t1 = System.nanoTime();
        blackhole = runLoopMega(mega, 100_000_000);
        long megaMs = (System.nanoTime() - t1) / 1_000_000;

        System.out.printf("100M calls -> Monomorphic: %d ms | Megamorphic (4 impls): %d ms%n", monoMs, megaMs);
    }

    private static long runLoopMono(Calc[] arr, int n) {
        long s = 0;
        for (int i = 0; i < n; i++) {
            s += arr[i & 3].apply(i);
        }
        return s;
    }

    private static long runLoopMega(Calc[] arr, int n) {
        long s = 0;
        for (int i = 0; i < n; i++) {
            s += arr[i & 3].apply(i);
        }
        return s;
    }
}
