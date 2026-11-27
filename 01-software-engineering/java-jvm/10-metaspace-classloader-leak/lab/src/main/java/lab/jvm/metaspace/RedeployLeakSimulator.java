package lab.jvm.metaspace;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Simulates repeated WAR redeployments on a fixed server thread pool.
 * - "leak": leaves DynWorkerTemplate instance in pooled thread's ThreadLocalMap -> ClassLoader leak & Metaspace growth
 * - "clean": removes ThreadLocal after request -> old ClassLoaders unload on GC
 */
public class RedeployLeakSimulator {

    public static final ThreadLocal<Object> REQUEST_CONTEXT = new ThreadLocal<>();

    public record SimulationResult(
            String mode,
            int redeploysAttempted,
            int liveLoadedClasses,
            long unloadedClasses,
            long metaspaceUsedBytes,
            boolean hitOomMetaspace) {
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "leak";
        int redeploys = args.length > 1 ? Integer.parseInt(args[1]) : 400;
        boolean leakMode = !"clean".equalsIgnoreCase(mode);

        SimulationResult res = runSimulation(leakMode, redeploys);
        System.out.printf("mode=%s | redeploys=%d | loadedClasses=%d | unloadedClasses=%d | metaspace=%.2f MB | hitOOM=%s%n",
                res.mode(), res.redeploysAttempted(), res.liveLoadedClasses(),
                res.unloadedClasses(), res.metaspaceUsedBytes() / (1024.0 * 1024.0), res.hitOomMetaspace());
    }

    public static SimulationResult runSimulation(boolean leakMode, int redeploys) throws Exception {
        byte[] workerClassBytes = loadTemplateBytes();
        ExecutorService serverPool = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "jboss-http-worker");
            t.setDaemon(true);
            return t;
        });

        ClassLoadingMXBean clBean = ManagementFactory.getClassLoadingMXBean();
        long unloadedStart = clBean.getUnloadedClassCount();
        boolean hitOom = false;
        int completed = 0;

        long metaspaceUsed = 0L;
        long unloadedDelta = 0L;
        int liveLoaded = 0;

        try {
            for (int i = 0; i < redeploys; i++) {
                // Keep each deploy distinct: use distinct ThreadLocal slots in leak mode
                // OR update the worker threads cleanly in clean mode.
                // Each new ClassLoader defines its own runtime <ClassLoader, DynWorkerTemplate> identity in Metaspace.
                IsolatedWarClassLoader warLoader = new IsolatedWarClassLoader(
                        RedeployLeakSimulator.class.getClassLoader(), workerClassBytes);
                Class<?> workerClass = warLoader.loadIsolatedTemplate();
                workerClass.getField("hostSlot").set(null, leakMode ? new ThreadLocal<>() : REQUEST_CONTEXT);

                // Trigger all 4 worker threads to execute the newly deployed class at least once
                Future<?>[] futures = new Future<?>[4];
                for (int w = 0; w < 4; w++) {
                    Runnable task = (Runnable) workerClass
                            .getConstructor(String.class, boolean.class)
                            .newInstance("deploy-" + i, leakMode);
                    futures[w] = serverPool.submit(task);
                }
                for (Future<?> f : futures) {
                    f.get();
                }

                // "Undeploy": clear strong references to warLoader & workerClass
                warLoader = null;
                workerClass = null;
                completed++;

                if (i % 50 == 49) {
                    System.gc();
                }
            }
            System.gc();
            Thread.sleep(50);
        } catch (OutOfMemoryError oom) {
            hitOom = true;
        } finally {
            // Snapshot Metaspace & classloading BEFORE stopping serverPool threads
            metaspaceUsed = ManagementFactory.getMemoryPoolMXBeans().stream()
                    .filter(p -> "Metaspace".equals(p.getName()))
                    .mapToLong(p -> p.getUsage().getUsed())
                    .findFirst()
                    .orElse(0L);
            unloadedDelta = clBean.getUnloadedClassCount() - unloadedStart;
            liveLoaded = clBean.getLoadedClassCount();
            serverPool.shutdownNow();
        }

        return new SimulationResult(
                leakMode ? "leak" : "clean",
                completed,
                liveLoaded,
                unloadedDelta,
                metaspaceUsed,
                hitOom
        );
    }

    private static byte[] loadTemplateBytes() throws IOException {
        String resPath = DynWorkerTemplate.class.getName().replace('.', '/') + ".class";
        try (InputStream in = DynWorkerTemplate.class.getClassLoader().getResourceAsStream(resPath)) {
            if (in == null) {
                throw new IllegalStateException("Missing template bytecode: " + resPath);
            }
            return in.readAllBytes();
        }
    }

    /**
     * Child-first ClassLoader modeling a JBoss WAR deployment classloader.
     */
    static final class IsolatedWarClassLoader extends ClassLoader {
        private final byte[] templateBytes;

        IsolatedWarClassLoader(ClassLoader parent, byte[] templateBytes) {
            super(parent);
            this.templateBytes = templateBytes;
        }

        Class<?> loadIsolatedTemplate() {
            return defineClass(DynWorkerTemplate.class.getName(), templateBytes, 0, templateBytes.length);
        }
    }
}
