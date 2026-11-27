package lab.jvm.classloading;

/**
 * Child-first isolating ClassLoader that loads a specified target class directly from its
 * bytecode bytes instead of delegating to the parent, mimicking WAR/JBoss plugin isolation.
 */
public class IsolatingClassLoader extends ClassLoader {

    private final String isolatedClassName;
    private final byte[] classBytes;

    public IsolatingClassLoader(String name, String isolatedClassName, byte[] classBytes, ClassLoader parent) {
        super(name, parent);
        this.isolatedClassName = isolatedClassName;
        this.classBytes = classBytes.clone();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> alreadyLoaded = findLoadedClass(name);
            if (alreadyLoaded != null) {
                return alreadyLoaded;
            }
            if (isolatedClassName.equals(name)) {
                Class<?> defined = defineClass(name, classBytes, 0, classBytes.length);
                if (resolve) {
                    resolveClass(defined);
                }
                return defined;
            }
            // Delegate JDK classes and shared contracts (e.g. PluginContract) to parent
            return super.loadClass(name, resolve);
        }
    }

    public static byte[] readClassBytes(Class<?> clazz) throws java.io.IOException {
        String resource = clazz.getName().replace('.', '/') + ".class";
        try (var in = clazz.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Cannot read bytecode for " + resource);
            }
            return in.readAllBytes();
        }
    }
}
