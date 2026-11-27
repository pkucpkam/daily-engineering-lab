package lab.jvm.classloading;

/**
 * Interactive lab runner for JVM-03:
 *  1) Built-in ClassLoader hierarchy (Bootstrap -> Platform -> App)
 *  2) Same binary class loaded by 2 isolated ClassLoaders -> 2 distinct Class objects & ClassCastException
 *  3) Communicating via shared parent interface + Thread Context ClassLoader (TCCL)
 *  4) ClassNotFoundException vs NoClassDefFoundError (after failed <clinit>)
 */
public class ClassLoadingLabRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== JVM-03 Lab: Class Loading & ClassLoader Hierarchy ===");

        // 1. Built-in hierarchy
        ClassLoader appLoader = ClassLoadingLabRunner.class.getClassLoader();
        ClassLoader platformLoader = appLoader.getParent();
        ClassLoader bootLoader = platformLoader.getParent();
        System.out.println("\n[1] Built-in ClassLoader hierarchy:");
        System.out.println("  String.class loader   : " + String.class.getClassLoader() + " (null = Bootstrap)");
        System.out.println("  java.sql.Driver loader: " + java.sql.Driver.class.getClassLoader().getName());
        System.out.println("  AppClassLoader        : " + appLoader.getName());
        System.out.println("  App -> Platform -> Boot: " + appLoader.getName()
                + " -> " + platformLoader.getName() + " -> " + bootLoader);

        // 2. Load SamplePlugin twice in two isolated child-first loaders
        byte[] pluginBytes = IsolatingClassLoader.readClassBytes(SamplePlugin.class);
        var loaderA = new IsolatingClassLoader("plugin-loader-A", SamplePlugin.class.getName(), pluginBytes, appLoader);
        var loaderB = new IsolatingClassLoader("plugin-loader-B", SamplePlugin.class.getName(), pluginBytes, appLoader);

        Class<?> classA = loaderA.loadClass(SamplePlugin.class.getName());
        Class<?> classB = loaderB.loadClass(SamplePlugin.class.getName());
        Object instanceA = classA.getDeclaredConstructor().newInstance();
        Object instanceB = classB.getDeclaredConstructor().newInstance();

        System.out.println("\n[2] Same binary name, two isolated ClassLoaders:");
        System.out.println("  classA == classB ? " + (classA == classB));
        System.out.println("  classA.isInstance(instanceB) ? " + classA.isInstance(instanceB));
        System.out.println("  instanceA -> " + ((PluginContract) instanceA).execute("hello"));
        System.out.println("  instanceB -> " + ((PluginContract) instanceB).execute("hello"));

        try {
            @SuppressWarnings("unused")
            SamplePlugin castToAppVersion = (SamplePlugin) instanceA;
        } catch (ClassCastException ex) {
            System.out.println("  Cross-loader cast failed as expected: " + ex.getMessage());
        }

        // 3. Thread Context ClassLoader (TCCL) pattern (e.g. JDBC DriverManager / SPI)
        System.out.println("\n[3] Thread Context ClassLoader (TCCL):");
        ClassLoader originalTccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loaderA);
            Class<?> viaTccl = Class.forName(SamplePlugin.class.getName(), true,
                    Thread.currentThread().getContextClassLoader());
            System.out.println("  Loaded via TCCL: " + viaTccl.getClassLoader().getName()
                    + " (matches loaderA? " + (viaTccl == classA) + ")");
        } finally {
            Thread.currentThread().setContextClassLoader(originalTccl);
        }

        // 4. ClassNotFoundException vs NoClassDefFoundError
        System.out.println("\n[4] ClassNotFoundException vs NoClassDefFoundError:");
        try {
            Class.forName("lab.jvm.classloading.DoesNotExist");
        } catch (ClassNotFoundException ex) {
            System.out.println("  ClassNotFoundException -> " + ex.getMessage());
        }

        try {
            BrokenStaticInit.ping();
        } catch (Throwable t) {
            System.out.println("  1st reference to BrokenStaticInit -> " + t.getClass().getSimpleName()
                    + ": " + t.getCause().getMessage());
        }
        try {
            BrokenStaticInit.ping();
        } catch (Throwable t) {
            System.out.println("  2nd reference to BrokenStaticInit -> " + t.getClass().getSimpleName()
                    + ": " + t.getMessage());
        }
    }
}
