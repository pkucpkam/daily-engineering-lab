import lab.cq.seams.LegacyInvoiceProcessor;

import java.util.Set;

/**
 * L3 Solution: Strangler Router with feature-flag cohort gating + safe fallback
 * back to the legacy processor if the newly migrated handler throws a runtime error.
 */
public class StranglerFallbackSwitch {

    private static final Set<String> PILOT_TENANTS = Set.of("TENANT-PILOT", "TENANT-ERR");

    public static long routeSafely(String tenantId, LegacyInvoiceProcessor legacy, long subtotalVnd) {
        if (!PILOT_TENANTS.contains(tenantId)) {
            return legacy.calculateNetPayable("HN", subtotalVnd, false);
        }
        try {
            if ("TENANT-ERR".equals(tenantId)) {
                throw new IllegalStateException("New microservice timeout");
            }
            // Shadow/new modern calculation
            return legacy.calculateNetPayable("HN", subtotalVnd, false) - 25_000L;
        } catch (RuntimeException ex) {
            System.out.println("[FALLBACK] Reverting " + tenantId + " to legacy due to: " + ex.getMessage());
            return legacy.calculateNetPayable("HN", subtotalVnd, false);
        }
    }

    public static void main(String[] args) {
        var legacy = new LegacyInvoiceProcessor();
        System.out.println("TENANT-STD   -> " + routeSafely("TENANT-STD", legacy, 1_000_000L));
        System.out.println("TENANT-PILOT -> " + routeSafely("TENANT-PILOT", legacy, 1_000_000L));
        System.out.println("TENANT-ERR   -> " + routeSafely("TENANT-ERR", legacy, 1_000_000L));
    }
}
