package lab.cq.pyramid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Contrasts over-mocked interaction tests (coupled to internal implementation details)
 * with boundary-mocked behavioral tests (resilient across V1 -> V2 refactoring).
 */
@ExtendWith(MockitoExtension.class)
class BoundaryVsOverMockedTest {

    @Mock
    private CheckoutDomain.DiscountRule mockedDiscount;

    @Mock
    private CheckoutDomain.TaxRule mockedTax;

    @Mock
    private CheckoutDomain.PaymentLedgerPort mockedLedger;

    @Test
    void overMockedTestPassesEvenIfRealDiscountFormulaWereBroken() {
        // Antipattern: mocking pure internal logic merely mirrors test assumptions
        when(mockedDiscount.discountFor(1_000_000L, true)).thenReturn(100_000L);
        when(mockedTax.vatFor(900_000L)).thenReturn(72_000L);

        var v1 = new CheckoutDomain.CheckoutServiceV1(mockedDiscount, mockedTax, mockedLedger);
        long total = v1.checkout(new CheckoutDomain.CheckoutCommand("ORD-1", 1_000_000L, true));

        assertThat(total).isEqualTo(972_000L);
        verify(mockedDiscount).discountFor(1_000_000L, true);
        verify(mockedTax).vatFor(900_000L);
        // Any refactoring that merges DiscountRule & TaxRule breaks this test!
    }

    @Test
    void boundaryMockedBehaviorTestSurvivesV1ToV2RefactoringIdentically() {
        // Best practice: REAL internal domain rules + fake/stub ONLY at I/O boundary
        List<CheckoutDomain.LedgerEntry> v1Ledger = new ArrayList<>();
        List<CheckoutDomain.LedgerEntry> v2Ledger = new ArrayList<>();

        var v1 = new CheckoutDomain.CheckoutServiceV1(
                new CheckoutDomain.DiscountRule(), new CheckoutDomain.TaxRule(), v1Ledger::add);
        var v2 = new CheckoutDomain.CheckoutServiceV2Refactored(v2Ledger::add);

        var cmd = new CheckoutDomain.CheckoutCommand("ORD-1", 1_000_000L, true);
        long totalV1 = v1.checkout(cmd);
        long totalV2 = v2.checkout(cmd);

        assertThat(totalV1).isEqualTo(972_000L);
        assertThat(totalV2).isEqualTo(totalV1);
        assertThat(v2Ledger).containsExactlyElementsOf(v1Ledger);
    }
}
