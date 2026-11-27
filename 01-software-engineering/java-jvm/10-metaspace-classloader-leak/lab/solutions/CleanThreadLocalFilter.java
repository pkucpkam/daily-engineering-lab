import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * L3 Solution pattern: safe Servlet Filter / Executor wrapper guaranteeing
 * ThreadLocal.remove() executes in finally so request worker threads never pin
 * WAR-loaded classes after undeploy.
 */
public class CleanThreadLocalFilter {

    private static final ThreadLocal<String> TENANT_CONTEXT = new ThreadLocal<>();

    public static void handleWithGuard(Runnable businessLogic, String tenantId) {
        TENANT_CONTEXT.set(tenantId);
        try {
            businessLogic.run();
        } finally {
            TENANT_CONTEXT.remove(); // Mandatory when executing inside server thread pools
        }
    }

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> f = pool.submit(() -> handleWithGuard(
                    () -> System.out.println("Handled tenant=" + TENANT_CONTEXT.get()),
                    "tenant-vn-01"));
            f.get();
            // Verify worker thread has empty ThreadLocal slot after request
            String leftover = pool.submit(TENANT_CONTEXT::get).get();
            System.out.println("Worker ThreadLocal after request cleanup = " + leftover);
        } finally {
            pool.shutdown();
        }
    }
}
