package lab.jvm.jit;

/**
 * Interactive lab for JVM-05:
 *  1) Cold Interpreter vs Warm C2 JIT latency on the exact same workload
 *  2) Dead Code Elimination (DCE) trap in naive microbenchmarks
 *  3) Monomorphic inlining -> Deoptimization ('made not entrant') when a 2nd implementation loads
 * Run with -XX:+PrintCompilation to watch Tier 1..4 transitions and deoptimizations!
 */
public class JitWarmupRunner {

    public interface TaxRule {
        long compute(long amount);
    }

    public static final class StandardTax implements TaxRule {
        @Override
        public long compute(long amount) {
            return (amount * 10) / 100;
        }
    }

    public static final class VipTax implements TaxRule {
        @Override
        public long compute(long amount) {
            return (amount * 5) / 100;
        }
    }

    private static volatile long blackhole;

    public static void main(String[] args) {
        System.out.println("=== JVM-05 Lab: JIT Tiered Compilation, Inlining & Deoptimization ===");

        // 1. Cold vs Warmup latency
        int batch = 50_000;
        long coldNs = measureBatchNs(batch);

        // Warmup enough iterations to promote hotSum() to Tier 3 (C1) and Tier 4 (C2)
        for (int i = 0; i < 40; i++) {
            blackhole += hotSum(batch);
        }
        long warmNs = measureBatchNs(batch);
        System.out.printf("[1] Cold vs Warm (50k ops): cold=%d us | warm(C2)=%d us (%.1fx faster)%n",
                coldNs / 1_000, warmNs / 1_000, (double) coldNs / Math.max(1, warmNs));

        // 2. Naive DCE trap: calling pure computation without consuming return value
        long t0 = System.nanoTime();
        for (int i = 0; i < 10_000_000; i++) {
            pureWork(i); // Result discarded -> C2 can eliminate entire loop body!
        }
        long dceMs = (System.nanoTime() - t0) / 1_000_000;

        long t1 = System.nanoTime();
        long acc = 0;
        for (int i = 0; i < 10_000_000; i++) {
            acc += pureWork(i);
        }
        blackhole = acc;
        long realMs = (System.nanoTime() - t1) / 1_000_000;
        System.out.printf("[2] Dead Code Elimination trap (10M calls): discarded=%d ms | consumed=%d ms%n",
                dceMs, realMs);

        // 3. Monomorphic CHA/inline -> introduce 2nd implementation -> deoptimization
        TaxRule rule1 = new StandardTax();
        long s = 0;
        for (int i = 0; i < 100_000; i++) {
            s += applyTax(rule1, i);
        }
        long monoNs = measureTax(rule1, 50_000);

        // Now dynamically load and pass VipTax -> invalidates monomorphic CHA assumption!
        TaxRule rule2 = new VipTax();
        for (int i = 0; i < 50_000; i++) {
            s += applyTax((i & 1) == 0 ? rule1 : rule2, i);
        }
        long biNs = measureTax(rule1, 50_000);
        blackhole += s;
        System.out.printf("[3] Monomorphic vs Bimorphic call site (50k calls): mono=%d us | after 2nd impl=%d us%n",
                monoNs / 1_000, biNs / 1_000);
    }

    private static long measureBatchNs(int n) {
        long t0 = System.nanoTime();
        blackhole = hotSum(n);
        return System.nanoTime() - t0;
    }

    private static long hotSum(int n) {
        long sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += pureWork(i);
        }
        return sum;
    }

    private static long pureWork(int x) {
        long v = x * 31L + 17L;
        return (v ^ (v >>> 13)) & 0xFFFFL;
    }

    private static long applyTax(TaxRule rule, long amount) {
        return rule.compute(amount);
    }

    private static long measureTax(TaxRule rule, int n) {
        long t0 = System.nanoTime();
        long s = 0;
        for (int i = 0; i < n; i++) {
            s += applyTax(rule, i);
        }
        blackhole += s;
        return System.nanoTime() - t0;
    }
}
