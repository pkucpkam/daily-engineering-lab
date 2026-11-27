package lab.dsa.hashmap;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashMapLabTest {

    @Test
    void simpleHashMapHandlesResizingAndKeepsLowCollisions() {
        SimpleHashMap<String, Integer> map = new SimpleHashMap<>();
        for (int i = 0; i < 10_000; i++) {
            map.put("key-" + i, i);
        }

        assertThat(map.size()).isEqualTo(10_000);
        assertThat(map.capacity()).isEqualTo(16_384);
        assertThat(map.maxBucketChainLength()).isLessThanOrEqualTo(10);
        assertThat(map.get("key-7777")).isEqualTo(7777);
        assertThat(map.remove("key-7777")).isEqualTo(7777);
        assertThat(map.get("key-7777")).isNull();
    }

    @Test
    void constantNonComparableHashCodeDegradesSeverelyComparedToGoodHashCode() {
        HashMapPitfallsDemo demo = new HashMapPitfallsDemo();
        var report = demo.benchmarkBadHashCodes(5_000);

        assertThat(report.constantNonComparableUs()).isGreaterThan(report.goodHashUs() * 5);
        assertThat(report.constantNonComparableUs()).isGreaterThan(report.constantComparableUs());
    }

    @Test
    void mutatingKeyAfterPutCausesSilentLookupFailureAndJava7ResizeCreatesCycle() {
        HashMapPitfallsDemo demo = new HashMapPitfallsDemo();

        var mut = demo.demonstrateMutableKeyLeak();
        assertThat(mut.valueBeforeMutate()).isEqualTo("DISCOUNT_20");
        assertThat(mut.valueAfterMutate()).isNull();
        assertThat(mut.mapSize()).isEqualTo(1);

        assertThat(demo.simulateJava7ResizeCycle()).isTrue();
    }
}
