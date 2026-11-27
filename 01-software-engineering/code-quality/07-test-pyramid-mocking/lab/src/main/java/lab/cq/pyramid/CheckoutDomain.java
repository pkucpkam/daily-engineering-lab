package lab.cq.pyramid;

/**
 * Pure domain collaborators + single I/O boundary (PaymentLedgerPort) illustrating
 * why unit tests should test behavior with real internal collaborators rather than
 * mocking internal math helpers.
 */
public final class CheckoutDomain {

    private CheckoutDomain() {
    }

    public record CheckoutCommand(String orderId, long subtotalVnd, boolean vip) {
    }

    public record LedgerEntry(String orderId, long netPayableVnd) {
    }

    /** External I/O boundary (DB / Remote Payment API): SHOULD be stubbed/faked in unit tests. */
    @FunctionalInterface
    public interface PaymentLedgerPort {
        void recordCharge(LedgerEntry entry);
    }

    /** Internal domain helper: SHOULD NOT be mocked. */
    public static class DiscountRule {
        public long discountFor(long subtotalVnd, boolean vip) {
            return vip ? Math.round(subtotalVnd * 0.10) : 0L;
        }
    }

    /** Internal domain helper: SHOULD NOT be mocked. */
    public static class TaxRule {
        public long vatFor(long afterDiscountVnd) {
            return Math.round(afterDiscountVnd * 0.08);
        }
    }

    /**
     * V1 implementation delegates cleanly to DiscountRule and TaxRule.
     */
    public static class CheckoutServiceV1 {
        private final DiscountRule discountRule;
        private final TaxRule taxRule;
        private final PaymentLedgerPort ledger;

        public CheckoutServiceV1(DiscountRule discountRule, TaxRule taxRule, PaymentLedgerPort ledger) {
            this.discountRule = discountRule;
            this.taxRule = taxRule;
            this.ledger = ledger;
        }

        public long checkout(CheckoutCommand cmd) {
            long discount = discountRule.discountFor(cmd.subtotalVnd(), cmd.vip());
            long afterDiscount = cmd.subtotalVnd() - discount;
            long vat = taxRule.vatFor(afterDiscount);
            long total = afterDiscount + vat;
            ledger.recordCharge(new LedgerEntry(cmd.orderId(), total));
            return total;
        }
    }

    /**
     * V2 refactored implementation: consolidates/inlines internal calculation without
     * invoking TaxRule separately, while preserving 100% identical observable behavior.
     */
    public static class CheckoutServiceV2Refactored {
        private final PaymentLedgerPort ledger;

        public CheckoutServiceV2Refactored(PaymentLedgerPort ledger) {
            this.ledger = ledger;
        }

        public long checkout(CheckoutCommand cmd) {
            long discount = cmd.vip() ? Math.round(cmd.subtotalVnd() * 0.10) : 0L;
            long afterDiscount = cmd.subtotalVnd() - discount;
            long vat = Math.round(afterDiscount * 0.08);
            long total = afterDiscount + vat;
            ledger.recordCharge(new LedgerEntry(cmd.orderId(), total));
            return total;
        }
    }
}
