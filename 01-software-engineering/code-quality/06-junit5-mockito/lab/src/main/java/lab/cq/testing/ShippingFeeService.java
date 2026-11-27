package lab.cq.testing;

/**
 * Service calculating parcel shipping fees across tiers and emitting audit quotes.
 */
public class ShippingFeeService {

    public enum Tier {
        DOMESTIC, EXPRESS, INTERNATIONAL
    }

    public record QuoteRequest(String orderId, Tier tier, long orderTotalVnd, double weightKg, boolean vip) {
    }

    public record QuoteAuditEvent(String orderId, Tier tier, long shippingFeeVnd, boolean freeWaiverApplied) {
    }

    @FunctionalInterface
    public interface ZoneRateCatalog {
        long baseRateFor(Tier tier);
    }

    @FunctionalInterface
    public interface AuditTracker {
        void record(QuoteAuditEvent event);
    }

    public static class SurchargeCalculator {
        public long weightSurcharge(double weightKg) {
            if (weightKg <= 2.0) {
                return 0L;
            }
            return Math.round((weightKg - 2.0) * 5_000L);
        }
    }

    private final ZoneRateCatalog rateCatalog;
    private final SurchargeCalculator surchargeCalculator;
    private final AuditTracker auditTracker;

    public ShippingFeeService(
            ZoneRateCatalog rateCatalog,
            SurchargeCalculator surchargeCalculator,
            AuditTracker auditTracker) {
        this.rateCatalog = rateCatalog;
        this.surchargeCalculator = surchargeCalculator;
        this.auditTracker = auditTracker;
    }

    public long quoteFee(QuoteRequest req) {
        if (req.orderTotalVnd() < 0 || req.weightKg() <= 0) {
            throw new IllegalArgumentException("Invalid order total or weight");
        }
        boolean waived = req.vip() && req.tier() == Tier.DOMESTIC && req.orderTotalVnd() >= 500_000L;
        long fee = waived ? 0L : rateCatalog.baseRateFor(req.tier()) + surchargeCalculator.weightSurcharge(req.weightKg());
        auditTracker.record(new QuoteAuditEvent(req.orderId(), req.tier(), fee, waived));
        return fee;
    }
}
