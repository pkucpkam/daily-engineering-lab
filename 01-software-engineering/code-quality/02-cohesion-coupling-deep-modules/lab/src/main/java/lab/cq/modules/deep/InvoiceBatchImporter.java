package lab.cq.modules.deep;

import java.util.ArrayList;
import java.util.List;

/**
 * Deep Module design (Ousterhout): tiny public surface with rich, hidden implementation.
 * Hides splitting, trimming, default currency fallback, and row validation rules behind one call.
 */
public class InvoiceBatchImporter {

    private static final String DEFAULT_CURRENCY = "VND";

    public record InvoiceLine(String invoiceId, long amount, String currency) {
    }

    public record ImportResult(List<InvoiceLine> valid, List<String> errors) {
    }

    public ImportResult importCsv(String rawCsv) {
        if (rawCsv == null) {
            throw new IllegalArgumentException("rawCsv must not be null");
        }
        List<InvoiceLine> valid = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int rowIndex = 0;
        for (String line : rawCsv.split("\\R")) {
            if (line.isBlank()) {
                continue;
            }
            rowIndex++;
            parseLine(rowIndex, line, valid, errors);
        }
        return new ImportResult(List.copyOf(valid), List.copyOf(errors));
    }

    private static void parseLine(int rowIndex, String line, List<InvoiceLine> valid, List<String> errors) {
        String[] cols = line.split(",", -1);
        if (cols.length != 3) {
            errors.add("Row " + rowIndex + ": expected 3 columns");
            return;
        }
        String id = cols[0].trim();
        String rawAmount = cols[1].trim();
        String currency = cols[2].trim();
        if (id.isEmpty()) {
            errors.add("Row " + rowIndex + ": expected 3 columns");
            return;
        }
        if (currency.isEmpty()) {
            currency = DEFAULT_CURRENCY;
        }
        try {
            long amount = Long.parseLong(rawAmount);
            if (amount <= 0) {
                errors.add("Row " + rowIndex + ": amount must be > 0");
                return;
            }
            valid.add(new InvoiceLine(id, amount, currency));
        } catch (NumberFormatException ex) {
            errors.add("Row " + rowIndex + ": invalid amount '" + rawAmount + "'");
        }
    }
}
