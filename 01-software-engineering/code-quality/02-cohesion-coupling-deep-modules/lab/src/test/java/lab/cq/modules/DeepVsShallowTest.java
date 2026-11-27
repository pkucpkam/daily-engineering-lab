package lab.cq.modules;

import lab.cq.modules.deep.InvoiceBatchImporter;
import lab.cq.modules.shallow.ShallowCsvPipeline;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeepVsShallowTest {

    private static final String SAMPLE_CSV = """
            INV-1, 150000 ,
            
            INV-2, 200 , USD
            INV-BAD, abc , VND
            INV-NEG, -10 , VND
            INV-MISSING-COL, 100
            """;

    @Test
    void deepModuleProducesIdenticalBehaviorWithSmallerPublicSurface() {
        var shallowResult = new ShallowCsvPipeline.BatchImportCoordinator().importAll(SAMPLE_CSV);
        var deepResult = new InvoiceBatchImporter().importCsv(SAMPLE_CSV);

        assertThat(deepResult.valid())
                .extracting(InvoiceBatchImporter.InvoiceLine::invoiceId,
                        InvoiceBatchImporter.InvoiceLine::amount,
                        InvoiceBatchImporter.InvoiceLine::currency)
                .containsExactlyElementsOf(shallowResult.valid().stream()
                        .map(r -> org.assertj.core.groups.Tuple.tuple(r.invoiceId(), r.amount(), r.currency()))
                        .toList());

        assertThat(deepResult.errors()).containsExactlyElementsOf(shallowResult.errors());

        // Quantitative surface-area metric: Shallow exposes 7 public types vs Deep's 3 (1 worker + 2 DTOs)
        int shallowPublicClasses = ShallowCsvPipeline.class.getDeclaredClasses().length;
        int deepPublicClasses = InvoiceBatchImporter.class.getDeclaredClasses().length + 1;
        assertThat(shallowPublicClasses).isEqualTo(7);
        assertThat(deepPublicClasses).isEqualTo(3);
    }
}
