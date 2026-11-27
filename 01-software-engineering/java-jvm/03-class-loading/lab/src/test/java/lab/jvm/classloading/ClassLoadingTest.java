package lab.jvm.classloading;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClassLoadingTest {

    @Test
    void twoIsolatedLoadersProduceDistinctClassObjectsForSameBinaryName() throws Exception {
        ClassLoader parent = ClassLoadingTest.class.getClassLoader();
        byte[] bytes = IsolatingClassLoader.readClassBytes(SamplePlugin.class);

        var loaderA = new IsolatingClassLoader("A", SamplePlugin.class.getName(), bytes, parent);
        var loaderB = new IsolatingClassLoader("B", SamplePlugin.class.getName(), bytes, parent);

        Class<?> classA = loaderA.loadClass(SamplePlugin.class.getName());
        Class<?> classB = loaderB.loadClass(SamplePlugin.class.getName());

        assertThat(classA).isNotSameAs(classB);
        assertThat(classA.getName()).isEqualTo(classB.getName());

        Object instA = classA.getDeclaredConstructor().newInstance();
        Object instB = classB.getDeclaredConstructor().newInstance();

        // Cross-classloader cast fails even though both share the exact same bytecode!
        assertThat(classA.isInstance(instB)).isFalse();
        assertThatThrownBy(() -> {
            @SuppressWarnings("unused")
            SamplePlugin direct = (SamplePlugin) instA;
        }).isInstanceOf(ClassCastException.class);

        // Shared interface loaded by parent ClassLoader works seamlessly across boundaries
        PluginContract contractA = (PluginContract) instA;
        PluginContract contractB = (PluginContract) instB;
        assertThat(contractA.execute("ping")).contains("loader=A");
        assertThat(contractB.execute("ping")).contains("loader=B");
    }

    @Test
    void failedStaticInitializerCausesExceptionInInitializerThenNoClassDefFoundError() {
        assertThatThrownBy(BrokenStaticInit::ping)
                .isInstanceOf(ExceptionInInitializerError.class);

        assertThatThrownBy(BrokenStaticInit::ping)
                .isInstanceOf(NoClassDefFoundError.class)
                .hasMessageContaining("Could not initialize class");
    }

    private static final java.util.concurrent.atomic.AtomicBoolean PROBE_CLINIT_RAN =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    @Test
    void linkingPreparationRunsBeforeStaticInitialization() throws Exception {
        // Loading with initialize=false does NOT run <clinit>; reading a field on LazyInitProbe
        // would itself trigger <clinit> (JVMS 5.5), so we observe via an external AtomicBoolean.
        Class<?> uninitialized = Class.forName(
                LazyInitProbe.class.getName(), false, ClassLoadingTest.class.getClassLoader());
        assertThat(PROBE_CLINIT_RAN.get()).isFalse();

        // Calling initialize=true triggers <clinit>
        Class.forName(LazyInitProbe.class.getName(), true, uninitialized.getClassLoader());
        assertThat(PROBE_CLINIT_RAN.get()).isTrue();
    }

    static class LazyInitProbe {
        static {
            PROBE_CLINIT_RAN.set(true);
        }
    }
}
