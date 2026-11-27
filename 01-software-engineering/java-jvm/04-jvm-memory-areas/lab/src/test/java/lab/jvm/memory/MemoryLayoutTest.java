package lab.jvm.memory;

import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryLayoutTest {

    @Test
    void integerSizeMatchesExpectedWordAndCompressedOopsLayout() {
        long integerSize = ClassLayout.parseClass(Integer.class).instanceSize();
        boolean compressedOops = VM.current().details().contains("compressed references");

        // With compressed oops (default <32GB): 12B header + 4B int = 16B; without: 16B + 4B + 4B pad = 24B
        assertThat(integerSize).isIn(16L, 24L);
        if (compressedOops) {
            assertThat(integerSize).isEqualTo(16L);
        }
    }

    @Test
    void boxedIntegerArrayConsumesSignificantlyMoreMemoryThanPrimitiveArray() {
        int[] primitives = new int[500];
        Integer[] boxed = new Integer[500];
        for (int i = 0; i < 500; i++) {
            boxed[i] = i + 5_000; // outside IntegerCache
        }

        long primitiveBytes = GraphLayout.parseInstance((Object) primitives).totalSize();
        long boxedBytes = GraphLayout.parseInstance((Object) boxed).totalSize();

        // int[500] ~ 2016B vs Integer[500] ~ 2016B array + 500 * 16B = ~10016B (~5x)
        assertThat(boxedBytes).isGreaterThan(primitiveBytes * 4);
    }
}
