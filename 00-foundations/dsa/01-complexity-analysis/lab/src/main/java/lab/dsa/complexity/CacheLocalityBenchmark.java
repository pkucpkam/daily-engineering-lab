package lab.dsa.complexity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Benchmark CPU Cache Locality & Amortized Resize trên Java 21 (có Warmup + Blackhole chống DCE):
 * 1) Duyệt O(n): int[] vs ArrayList<Integer> vs Fragmented LinkedList<Integer> vs O(n log n) Arrays.sort(int[]).
 * 2) Đếm chính xác số lần grow() (1.5x) & tổng phần tử copy của ArrayList.add(n) không initialCapacity.
 */
public final class CacheLocalityBenchmark {

    public record TraversalReport(
            int size,
            long intArrayUs,
            long arrayListUs,
            long linkedListUs,
            long arraysSortUs,
            long checksum) {}

    public record ResizeStats(
            int elementsAdded,
            int resizeCount,
            long totalCopiedElements,
            int finalCapacity,
            long defaultAddUs,
            long preSizedAddUs) {}

    public TraversalReport benchmarkTraversal(int n) {
        int[] raw = new int[n];
        ArrayList<Integer> arrayList = new ArrayList<>(n);
        Random rnd = new Random(42L);
        for (int i = 0; i < n; i++) {
            int v = rnd.nextInt(100_000);
            raw[i] = v;
            arrayList.add(v);
        }

        // Tạo LinkedList bị phân mảnh vị trí trên Heap bằng cách xáo trộn các bucket node khi chèn
        // nhằm tái hiện đúng pointer-chasing thực tế sau nhiều chu kỳ request trên server.
        List<Integer> scatteredValues = new ArrayList<>(arrayList);
        Collections.shuffle(scatteredValues, rnd);
        LinkedList<Integer> linkedList = new LinkedList<>();
        List<byte[]> padding = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            linkedList.add(scatteredValues.get(i));
            if ((i & 31) == 0) {
                padding.add(new byte[64]); // chêm object nhỏ xen kẽ các Node trên TLAB
            }
        }
        padding.clear();

        // Warmup cho C2 JIT biên dịch xong toàn bộ loop
        long sink = 0;
        for (int w = 0; w < 3; w++) {
            sink ^= sumIntArray(raw);
            sink ^= sumList(arrayList);
            sink ^= sumList(linkedList);
            int[] copy = Arrays.copyOf(raw, raw.length);
            Arrays.sort(copy);
            sink ^= copy[0];
        }

        int runs = 5;
        long t0 = System.nanoTime();
        for (int r = 0; r < runs; r++) {
            sink ^= sumIntArray(raw);
        }
        long intArrUs = Math.max(1L, (System.nanoTime() - t0) / (runs * 1_000L));

        long t1 = System.nanoTime();
        for (int r = 0; r < runs; r++) {
            sink ^= sumList(arrayList);
        }
        long arrListUs = Math.max(1L, (System.nanoTime() - t1) / (runs * 1_000L));

        long t2 = System.nanoTime();
        for (int r = 0; r < runs; r++) {
            sink ^= sumList(linkedList);
        }
        long linkedUs = Math.max(1L, (System.nanoTime() - t2) / (runs * 1_000L));

        long sortTotalNs = 0;
        for (int r = 0; r < runs; r++) {
            int[] toSort = Arrays.copyOf(raw, raw.length);
            long ts = System.nanoTime();
            Arrays.sort(toSort);
            sortTotalNs += (System.nanoTime() - ts);
            sink ^= toSort[toSort.length - 1];
        }
        long sortUs = Math.max(1L, sortTotalNs / (runs * 1_000L));

        return new TraversalReport(n, intArrUs, arrListUs, linkedUs, sortUs, sink);
    }

    /**
     * Đếm chính xác số lần cấp phát lại mảng và tổng phần tử copy theo công thức OpenJDK ArrayList (1.5x).
     */
    public ResizeStats measureArrayListGrowth(int n) {
        int cap = 10;
        int resizes = 0;
        long totalCopied = 0;
        for (int size = 0; size < n; size++) {
            if (size == cap) {
                totalCopied += size;
                cap = cap + (cap >> 1);
                if (cap < size + 1) {
                    cap = size + 1;
                }
                resizes++;
            }
        }

        // Warmup
        for (int w = 0; w < 3; w++) {
            fillDefault(50_000);
            fillPreSized(50_000);
        }

        long t0 = System.nanoTime();
        int sum1 = fillDefault(n).size();
        long defaultUs = Math.max(1L, (System.nanoTime() - t0) / 1_000L);

        long t1 = System.nanoTime();
        int sum2 = fillPreSized(n).size();
        long preSizedUs = Math.max(1L, (System.nanoTime() - t1) / 1_000L);

        if (sum1 != sum2) {
            throw new IllegalStateException("Mismatch");
        }
        return new ResizeStats(n, resizes, totalCopied, cap, defaultUs, preSizedUs);
    }

    private static List<Integer> fillDefault(int n) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        return list;
    }

    private static List<Integer> fillPreSized(int n) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        return list;
    }

    private static long sumIntArray(int[] arr) {
        long sum = 0;
        for (int v : arr) {
            sum += v;
        }
        return sum;
    }

    private static long sumList(List<Integer> list) {
        long sum = 0;
        for (int v : list) {
            sum += v;
        }
        return sum;
    }
}
