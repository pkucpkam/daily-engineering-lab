package lab.dsa.hashmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Thí nghiệm thực chứng các cạm bẫy HashMap cho DSA-02:
 * 1) So sánh GoodHashKey vs ConstantComparableKey (hash=42) vs ConstantNonComparableKey (hash=42).
 * 2) Mutable Key làm rò rỉ entry không get/remove được.
 * 3) Mô phỏng đúng 4 bước race condition Head Insertion của Java 7 HashMap#transfer -> tạo chu trình A -> B -> A.
 */
public final class HashMapPitfallsDemo {

    public record GoodHashKey(int id) {}

    public record ConstantComparableKey(int id) implements Comparable<ConstantComparableKey> {
        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public int compareTo(ConstantComparableKey o) {
            return Integer.compare(this.id, o.id);
        }
    }

    public static final class ConstantNonComparableKey {
        private final int id;

        public ConstantNonComparableKey(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof ConstantNonComparableKey other) && this.id == other.id;
        }
    }

    public static final class MutableKey {
        private String code;

        public MutableKey(String code) {
            this.code = code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(code);
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof MutableKey other) && Objects.equals(this.code, other.code);
        }
    }

    public record CollisionBenchmark(
            int count,
            long goodHashUs,
            long constantComparableUs,
            long constantNonComparableUs,
            long sink) {}

    public CollisionBenchmark benchmarkBadHashCodes(int count) {
        long sink = 0;

        // Warmup ngắn
        sink ^= runGood(500);
        sink ^= runConstantComparable(500);
        sink ^= runConstantNonComparable(500);

        long t0 = System.nanoTime();
        sink ^= runGood(count);
        long goodUs = Math.max(1L, (System.nanoTime() - t0) / 1_000L);

        long t1 = System.nanoTime();
        sink ^= runConstantComparable(count);
        long compUs = Math.max(1L, (System.nanoTime() - t1) / 1_000L);

        long t2 = System.nanoTime();
        sink ^= runConstantNonComparable(count);
        long nonCompUs = Math.max(1L, (System.nanoTime() - t2) / 1_000L);

        return new CollisionBenchmark(count, goodUs, compUs, nonCompUs, sink);
    }

    public record MutableKeyResult(int mapSize, String valueBeforeMutate, String valueAfterMutate) {}

    public MutableKeyResult demonstrateMutableKeyLeak() {
        Map<MutableKey, String> map = new HashMap<>();
        MutableKey key = new MutableKey("VIP_USER");
        map.put(key, "DISCOUNT_20");
        String before = map.get(key);

        // Sửa field tham gia hashCode() sau khi đã put vào map!
        key.setCode("CHANGED_CODE");
        String after = map.get(key);
        return new MutableKeyResult(map.size(), before, after);
    }

    static final class Java7Entry {
        final String key;
        Java7Entry next;

        Java7Entry(String key, Java7Entry next) {
            this.key = key;
            this.next = next;
        }
    }

    /**
     * Tái hiện chính xác từng bước xen kẽ giữa 2 thread trong Java 7 HashMap#transfer()
     * với bucket cũ A -> B -> null, chứng minh tạo chu trình vô tận A -> B -> A.
     */
    public boolean simulateJava7ResizeCycle() {
        Java7Entry b = new Java7Entry("B", null);
        Java7Entry a = new Java7Entry("A", b);

        // Thread 1 dừng ngay sau dòng: Entry<K,V> next = e.next;
        Java7Entry t1Current = a;
        Java7Entry t1Next = a.next; // B

        // Thread 2 chạy trọn vẹn head insertion sang mảng mới của T2 -> đảo thành B -> A -> null
        Java7Entry[] t2Bucket = new Java7Entry[1];
        Java7Entry t2Curr = a;
        while (t2Curr != null) {
            Java7Entry n = t2Curr.next;
            t2Curr.next = t2Bucket[0];
            t2Bucket[0] = t2Curr;
            t2Curr = n;
        }
        // Lúc này trong heap chung: b.next == a, a.next == null!

        // Thread 1 thức dậy và tiếp tục head insertion trên mảng mới của T1:
        Java7Entry[] t1Bucket = new Java7Entry[1];
        // Vòng lặp 1 của T1: e = A, next = B
        t1Current.next = t1Bucket[0]; // a.next = null
        t1Bucket[0] = t1Current;      // t1Bucket[0] = A
        t1Current = t1Next;           // t1Current = B

        // Vòng lặp 2 của T1: e = B, đọc e.next trên Heap đang là A (do Thread 2 đảo)!
        t1Next = t1Current.next;      // t1Next = A!
        t1Current.next = t1Bucket[0]; // b.next = A
        t1Bucket[0] = t1Current;      // t1Bucket[0] = B
        t1Current = t1Next;           // t1Current = A

        // Vòng lặp 3 của T1: e = A -> tạo chu trình A -> B -> A!
        t1Next = t1Current.next;      // null
        t1Current.next = t1Bucket[0]; // a.next = B -> VÒNG LẶP VÔ HẠN!
        t1Bucket[0] = t1Current;

        return hasCycleFloyd(t1Bucket[0]);
    }

    private static boolean hasCycleFloyd(Java7Entry head) {
        Java7Entry slow = head;
        Java7Entry fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                return true;
            }
        }
        return false;
    }

    private static long runGood(int n) {
        Map<GoodHashKey, Integer> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            map.put(new GoodHashKey(i), i);
        }
        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += map.get(new GoodHashKey(i));
        }
        return sum;
    }

    private static long runConstantComparable(int n) {
        Map<ConstantComparableKey, Integer> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            map.put(new ConstantComparableKey(i), i);
        }
        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += map.get(new ConstantComparableKey(i));
        }
        return sum;
    }

    private static long runConstantNonComparable(int n) {
        Map<ConstantNonComparableKey, Integer> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            map.put(new ConstantNonComparableKey(i), i);
        }
        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += map.get(new ConstantNonComparableKey(i));
        }
        return sum;
    }
}
