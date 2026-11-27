package lab.jvm.classloading;

/**
 * Demonstrates the difference between ExceptionInInitializerError (first <clinit> failure)
 * and NoClassDefFoundError (subsequent attempts to use that failed class).
 */
public class BrokenStaticInit {

    static {
        if (true) {
            throw new IllegalStateException("Simulated missing config during <clinit>");
        }
    }

    public static String ping() {
        return "pong";
    }
}
