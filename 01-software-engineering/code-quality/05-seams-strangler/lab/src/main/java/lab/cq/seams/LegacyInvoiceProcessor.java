package lab.cq.seams;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulates an untangled legacy service upgraded via:
 * 1. Sprout Class delegation (TaxExemptionSproutPolicy)
 * 2. Wrap Method (submitWithAuditWrap)
 * 3. Object Seam (replaceable Gateway collaborator)
 */
public class LegacyInvoiceProcessor {

    @FunctionalInterface
    public interface AuditSink {
        void record(String event);
    }

    private final TaxExemptionSproutPolicy sproutPolicy = new TaxExemptionSproutPolicy();
    private final AuditSink auditSink;

    public LegacyInvoiceProcessor() {
        // Production default concrete dependency (can be swapped via Object Seam constructor)
        this(event -> { /* production logger/DB */ });
    }

    public LegacyInvoiceProcessor(AuditSink auditSink) {
        this.auditSink = auditSink;
    }

    /** Legacy core calculation method preserved intact, delegating ONLY new rule to Sprout Class. */
    public long calculateNetPayable(String regionCode, long subtotalVnd, boolean nonProfit) {
        long vat = Math.round(subtotalVnd * 0.10);
        long gross = subtotalVnd + vat;
        // Single clean call out to fully tested Sprout Class:
        long exemption = sproutPolicy.evaluate(regionCode, gross, nonProfit).exemptAmountVnd();
        return gross - exemption;
    }

    /** Wrap Method pattern: preserves original behavior while adding new cross-cutting audit cleanly. */
    public long calculateAndAudit(String invoiceId, String regionCode, long subtotalVnd, boolean nonProfit) {
        long net = calculateNetPayable(regionCode, subtotalVnd, nonProfit);
        auditSink.record("INVOICE:" + invoiceId + ":NET=" + net);
        return net;
    }

    /** Module-level Strangler Router gradually shifting selected tenants from legacy -> modern handler. */
    public static class InvoiceStranglerRouter {
        private final LegacyInvoiceProcessor legacy;
        private final ModernHandler modern;
        private final List<String> routedPathLog = new ArrayList<>();

        @FunctionalInterface
        public interface ModernHandler {
            long compute(String tenantId, String regionCode, long subtotalVnd, boolean nonProfit);
        }

        public InvoiceStranglerRouter(LegacyInvoiceProcessor legacy, ModernHandler modern) {
            this.legacy = legacy;
            this.modern = modern;
        }

        public long route(String tenantId, boolean migratedToSpring, String regionCode, long subtotalVnd, boolean nonProfit) {
            if (migratedToSpring) {
                routedPathLog.add(tenantId + "->MODERN");
                return modern.compute(tenantId, regionCode, subtotalVnd, nonProfit);
            }
            routedPathLog.add(tenantId + "->LEGACY");
            return legacy.calculateNetPayable(regionCode, subtotalVnd, nonProfit);
        }

        public List<String> getRoutedPathLog() {
            return List.copyOf(routedPathLog);
        }
    }
}
