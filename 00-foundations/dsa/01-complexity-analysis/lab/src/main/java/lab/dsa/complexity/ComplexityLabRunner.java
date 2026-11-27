package lab.dsa.complexity;

/**
 * Runner thực nghiệm cho DSA-01.
 */
public final class ComplexityLabRunner {

    public static void main(String[] args) {
        CacheLocalityBenchmark bench = new CacheLocalityBenchmark();

        int n = 1_000_000;
        var report = bench.benchmarkTraversal(n);
        System.out.printf("=== [1] Benchmark Cache Locality (n = %,d phần tử, trung bình sau JIT Warmup) ===%n", n);
        System.out.printf("int[] sum          O(n)       : %6d us (%.2f ms)%n", report.intArrayUs(), report.intArrayUs() / 1000.0);
        System.out.printf("ArrayList sum      O(n)       : %6d us (%.2f ms)%n", report.arrayListUs(), report.arrayListUs() / 1000.0);
        System.out.printf("LinkedList sum     O(n)       : %6d us (%.2f ms)%n", report.linkedListUs(), report.linkedListUs() / 1000.0);
        System.out.printf("Arrays.sort(int[]) O(n log n) : %6d us (%.2f ms)%n", report.arraysSortUs(), report.arraysSortUs() / 1000.0);
        System.out.printf("LinkedList / int[] ratio      : %.1fx slower%n", (double) report.linkedListUs() / report.intArrayUs());

        var resize = bench.measureArrayListGrowth(n);
        System.out.printf("%n=== [2] Chi phí Resize của ArrayList (n = %,d, hệ số tăng 1.5x từ capacity=10) ===%n", n);
        System.out.printf("Số lần grow()        : %d lần (finalCap = %,d)%n", resize.resizeCount(), resize.finalCapacity());
        System.out.printf("Tổng phần tử copy    : %,d (hệ số %.2f * n -> O(1) amortized)%n",
                resize.totalCopiedElements(), (double) resize.totalCopiedElements() / n);
        System.out.printf("Add mặc định vs pre-sized: %d us vs %d us%n", resize.defaultAddUs(), resize.preSizedAddUs());
        System.out.println("(blackhole=" + report.checksum() + ")");
    }
}
