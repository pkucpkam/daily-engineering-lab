package lab.jvm.classloading.solutions;

import lab.jvm.classloading.IsolatingClassLoader;
import lab.jvm.classloading.SamplePlugin;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reference solution for Exercise E7:
 * Proves static fields are completely isolated per defining ClassLoader.
 */
class E7StaticStateIsolationTest {

    @Test
    void staticFieldsAreIndependentAcrossTwoIsolatedClassLoaders() throws Exception {
        ClassLoader parent = E7StaticStateIsolationTest.class.getClassLoader();
        byte[] bytes = IsolatingClassLoader.readClassBytes(SamplePlugin.class);

        var loader1 = new IsolatingClassLoader("L1", SamplePlugin.class.getName(), bytes, parent);
        var loader2 = new IsolatingClassLoader("L2", SamplePlugin.class.getName(), bytes, parent);

        Class<?> c1 = Class.forName(SamplePlugin.class.getName(), true, loader1);
        Class<?> c2 = Class.forName(SamplePlugin.class.getName(), true, loader2);

        AtomicInteger counter1 = (AtomicInteger) c1.getField("STATIC_INIT_COUNTER").get(null);
        AtomicInteger counter2 = (AtomicInteger) c2.getField("STATIC_INIT_COUNTER").get(null);

        counter1.set(10);

        assertThat(counter1.get()).isEqualTo(10);
        assertThat(counter2.get()).isEqualTo(1);
    }
}
