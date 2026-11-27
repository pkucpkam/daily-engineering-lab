package lab.dsa.complexity.solutions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lời giải tham khảo E8 (DSA-01):
 * So sánh hàm đối soát O(n * m) dùng List.contains() vs O(n + m) dùng HashSet pre-sized + ArrayList pre-sized.
 */
public final class PreSizedCollectionsSolution {

    public static List<Long> findActiveOrdersNaive(List<Long> allOrders, List<Long> cancelledOrders) {
        List<Long> result = new ArrayList<>();
        for (Long id : allOrders) {
            if (!cancelledOrders.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }

    public static List<Long> findActiveOrdersFast(List<Long> allOrders, List<Long> cancelledOrders) {
        Set<Long> cancelledSet = HashSet.newHashSet(cancelledOrders.size());
        cancelledSet.addAll(cancelledOrders);

        List<Long> result = new ArrayList<>(allOrders.size());
        for (Long id : allOrders) {
            if (!cancelledSet.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int n = 40_000;
        int m = 15_000;
        List<Long> all = new ArrayList<>(n);
        List<Long> cancelled = new ArrayList<>(m);
        for (long i = 0; i < n; i++) {
            all.add(i);
        }
        for (long i = 0; i < m; i++) {
            cancelled.add(i * 2);
        }

        long t0 = System.nanoTime();
        List<Long> naive = findActiveOrdersNaive(all, cancelled);
        long naiveMs = (System.nanoTime() - t0) / 1_000_000L;

        long t1 = System.nanoTime();
        List<Long> fast = findActiveOrdersFast(all, cancelled);
        long fastMs = Math.max(1L, (System.nanoTime() - t1) / 1_000_000L);

        System.out.printf("Match=%s (count=%d) | Naive O(n*m)=%d ms vs Fast HashSet O(n+m)=%d ms%n",
                naive.equals(fast), fast.size(), naiveMs, fastMs);
    }
}
