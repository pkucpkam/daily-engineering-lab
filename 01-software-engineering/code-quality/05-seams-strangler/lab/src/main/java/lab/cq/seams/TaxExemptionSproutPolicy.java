package lab.cq.seams;

/**
 * Pure domain Sprout Class added alongside untestable legacy billing code.
 * 100% unit-testable in isolation without touching legacy DB/JNDI wiring.
 */
public class TaxExemptionSproutPolicy {

    public record ExemptionResult(long exemptAmountVnd, String reason) {
    }

    public ExemptionResult evaluate(String regionCode, long grossVnd, boolean nonProfit) {
        if (grossVnd <= 0) {
            return new ExemptionResult(0L, "NON_POSITIVE");
        }
        if (nonProfit) {
            long exempt = Math.round(grossVnd * 0.10); // full 10% VAT waived
            return new ExemptionResult(exempt, "NON_PROFIT_FULL_EXEMPTION");
        }
        if ("REMOTE_ZONE".equals(regionCode) && grossVnd >= 1_000_000L) {
            return new ExemptionResult(50_000L, "REMOTE_ZONE_REBATE");
        }
        return new ExemptionResult(0L, "NONE");
    }
}
