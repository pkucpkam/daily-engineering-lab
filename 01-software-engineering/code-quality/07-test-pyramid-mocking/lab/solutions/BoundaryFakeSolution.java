import lab.cq.pyramid.CheckoutDomain;

import java.util.ArrayList;
import java.util.List;

/**
 * L3 Solution example: boundary-only fake PaymentLedgerPort allows validating
 * full checkout calculation (Discount + VAT + Ledger persistence) with zero Mockito overhead.
 */
public class BoundaryFakeSolution {

    public static void main(String[] args) {
        List<CheckoutDomain.LedgerEntry> fakeLedger = new ArrayList<>();
        var service = new CheckoutDomain.CheckoutServiceV2Refactored(fakeLedger::add);

        long totalVip = service.checkout(new CheckoutDomain.CheckoutCommand("B-1", 2_000_000L, true));
        long totalStd = service.checkout(new CheckoutDomain.CheckoutCommand("B-2", 2_000_000L, false));

        System.out.printf("VIP=%d | STD=%d | recordedCharges=%s%n", totalVip, totalStd, fakeLedger);
    }
}
