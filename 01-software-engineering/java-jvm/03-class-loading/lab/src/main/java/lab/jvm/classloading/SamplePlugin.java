package lab.jvm.classloading;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Target class loaded repeatedly by isolated ClassLoader instances.
 * Its static initializer runs ONCE per defining ClassLoader!
 */
public class SamplePlugin implements PluginContract {

    public static final AtomicInteger STATIC_INIT_COUNTER = new AtomicInteger(0);

    static {
        STATIC_INIT_COUNTER.incrementAndGet();
    }

    @Override
    public String execute(String input) {
        return "SamplePlugin@" + System.identityHashCode(SamplePlugin.class)
                + "[loader=" + SamplePlugin.class.getClassLoader().getName()
                + ", initCount=" + STATIC_INIT_COUNTER.get() + "]: " + input;
    }
}
