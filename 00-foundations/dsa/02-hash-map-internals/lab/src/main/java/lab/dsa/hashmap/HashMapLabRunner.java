package lab.dsa.hashmap;

/**
 * Runner thực nghiệm cho DSA-02.
 */
public final class HashMapLabRunner {

    public static void main(String[] args) {
        SimpleHashMap<String, Integer> map = new SimpleHashMap<>();
        for (int i = 0; i < 10_000; i++) {
            map.put("order-" + i, i);
        }
        System.out.println("=== [1] SimpleHashMap tự cài đặt với 10.000 keys ===");
        System.out.printf("size=%d | capacity=%d | resizes=%d | maxBucketChain=%d%n",
                map.size(), map.capacity(), map.resizeCount(), map.maxBucketChainLength());

        HashMapPitfallsDemo pitfalls = new HashMapPitfallsDemo();
        var cmp = pitfalls.benchmarkBadHashCodes(8_000);
        System.out.printf("%n=== [2] Đo put+get %,d phần tử trên java.util.HashMap khi hashCode() trả hằng số ===", cmp.count());
        System.out.printf("%nGoodHashKey              (O(1))     : %6d us (%.2f ms)%n", cmp.goodHashUs(), cmp.goodHashUs() / 1000.0);
        System.out.printf("ConstantComparableKey    (RB-Tree)  : %6d us (%.2f ms)%n", cmp.constantComparableUs(), cmp.constantComparableUs() / 1000.0);
        System.out.printf("ConstantNonComparableKey (Broken!)  : %6d us (%.2f ms) -> chậm hơn %.1fx!%n",
                cmp.constantNonComparableUs(), cmp.constantNonComparableUs() / 1000.0,
                (double) cmp.constantNonComparableUs() / cmp.goodHashUs());

        var mut = pitfalls.demonstrateMutableKeyLeak();
        System.out.println("\n=== [3] Mutable Key Leak ===");
        System.out.printf("Before mutate=%s | After mutate=%s | map.size()=%d (entry bị kẹt mất!)%n",
                mut.valueBeforeMutate(), mut.valueAfterMutate(), mut.mapSize());

        boolean cycle = pitfalls.simulateJava7ResizeCycle();
        System.out.println("\n=== [4] Java 7 Concurrent Resize Head-Insertion Cycle Simulator ===");
        System.out.println("Floyd cycle detected (A -> B -> A): " + cycle);
    }
}
