package lab.jvm.metaspace;

/**
 * Rich template class loaded into an isolated ClassLoader per simulated WAR deploy.
 * Holding an instance of this class in a server-thread ThreadLocal prevents its
 * defining ClassLoader (and all its Metaspace class metadata) from being GC'd.
 */
public class DynWorkerTemplate implements Runnable {

    public static ThreadLocal<Object> hostSlot;

    private final String deployId;
    private final boolean leakThreadLocal;

    public DynWorkerTemplate(String deployId, boolean leakThreadLocal) {
        this.deployId = deployId;
        this.leakThreadLocal = leakThreadLocal;
    }

    @Override
    public void run() {
        if (hostSlot == null) {
            return;
        }
        hostSlot.set(this);
        try {
            // Simulate request handling referencing class metadata
            compute01();
            compute02();
        } finally {
            if (!leakThreadLocal) {
                hostSlot.remove();
            }
        }
    }

    public String getDeployId() {
        return deployId;
    }

    // Extra methods increase per-class Metaspace footprint realistically (like controller/service beans)
    private int compute01() { return deployId.length() + 1; }
    private int compute02() { return compute01() + 2; }
    public int m03() { return 3; } public int m04() { return 4; } public int m05() { return 5; }
    public int m06() { return 6; } public int m07() { return 7; } public int m08() { return 8; }
    public int m09() { return 9; } public int m10() { return 10; } public int m11() { return 11; }
    public int m12() { return 12; } public int m13() { return 13; } public int m14() { return 14; }
    public int m15() { return 15; } public int m16() { return 16; } public int m17() { return 17; }
    public int m18() { return 18; } public int m19() { return 19; } public int m20() { return 20; }
    public int m21() { return 21; } public int m22() { return 22; } public int m23() { return 23; }
    public int m24() { return 24; } public int m25() { return 25; } public int m26() { return 26; }
}
