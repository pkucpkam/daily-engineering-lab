import lab.cq.testing.ShippingFeeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * L3 Solution illustration: compares verifying shipping behavior via simple hand-written
 * Fakes/Stubs (survives internal refactoring cleanly) without brittle Mockito.verify chains.
 */
public class FakeVsMockRefactorDemo {

    public static void main(String[] args) {
        Map<ShippingFeeService.Tier, Long> table = Map.of(
                ShippingFeeService.Tier.DOMESTIC, 25_000L,
                ShippingFeeService.Tier.EXPRESS, 55_000L,
                ShippingFeeService.Tier.INTERNATIONAL, 150_000L
        );
        List<ShippingFeeService.QuoteAuditEvent> recorded = new ArrayList<>();

        var service = new ShippingFeeService(
                table::get,
                new ShippingFeeService.SurchargeCalculator(),
                recorded::add
        );

        long fee1 = service.quoteFee(new ShippingFeeService.QuoteRequest("O-1", ShippingFeeService.Tier.DOMESTIC, 500_000L, 3.0, true));
        long fee2 = service.quoteFee(new ShippingFeeService.QuoteRequest("O-2", ShippingFeeService.Tier.EXPRESS, 500_000L, 4.0, false));

        System.out.printf("fee1=%d | fee2=%d | recordedEvents=%d%n", fee1, fee2, recorded.size());
    }
}
