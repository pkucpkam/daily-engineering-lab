package lab.cq.modules.shallow;

import java.util.ArrayList;
import java.util.List;

/**
 * Shallow-module ("Classitis") design: splits one cohesive CSV parsing task across
 * five tiny pass-through classes, introducing temporal coupling and information leakage.
 */
public final class ShallowCsvPipeline {

    private ShallowCsvPipeline() {
    }

    public static class CsvLineSplitter {
        public List<String> splitNonEmptyLines(String rawCsv) {
            if (rawCsv == null) {
                throw new IllegalArgumentException("rawCsv must not be null");
            }
            List<String> lines = new ArrayList<>();
            for (String line : rawCsv.split("\\R")) {
                if (!line.isBlank()) {
                    lines.add(line);
                }
            }
            return lines;
        }
    }

    public static class RowTokenSplitter {
        public List<String[]> tokenize(List<String> lines) {
            List<String[]> rows = new ArrayList<>(lines.size());
            for (String line : lines) {
                rows.add(line.split(",", -1));
            }
            return rows;
        }
    }

    public static class RowSanitizer {
        // Information leakage: caller must remember array indices 0=id, 1=amount, 2=currency
        public List<String[]> trimAndNormalize(List<String[]> rows) {
            List<String[]> cleaned = new ArrayList<>(rows.size());
            for (String[] cols : rows) {
                String[] copy = new String[cols.length];
                for (int i = 0; i < cols.length; i++) {
                    copy[i] = cols[i].trim();
                }
                if (copy.length >= 3 && copy[2].isEmpty()) {
                    copy[2] = "VND";
                }
                cleaned.add(copy);
            }
            return cleaned;
        }
    }

    public record ParsedRow(String invoiceId, long amount, String currency) {
    }

    public record PipelineOutput(List<ParsedRow> valid, List<String> errors) {
    }

    public static class RowValidatorMapper {
        public PipelineOutput validateAndBuild(List<String[]> sanitizedRows) {
            List<ParsedRow> valid = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            for (int i = 0; i < sanitizedRows.size(); i++) {
                String[] cols = sanitizedRows.get(i);
                if (cols.length != 3 || cols[0].isEmpty()) {
                    errors.add("Row " + (i + 1) + ": expected 3 columns");
                    continue;
                }
                try {
                    long amount = Long.parseLong(cols[1]);
                    if (amount <= 0) {
                        errors.add("Row " + (i + 1) + ": amount must be > 0");
                        continue;
                    }
                    valid.add(new ParsedRow(cols[0], amount, cols[2]));
                } catch (NumberFormatException ex) {
                    errors.add("Row " + (i + 1) + ": invalid amount '" + cols[1] + "'");
                }
            }
            return new PipelineOutput(List.copyOf(valid), List.copyOf(errors));
        }
    }

    // Pass-through facade forced by shallow splitting (temporal coupling across 4 collaborators)
    public static class BatchImportCoordinator {
        private final CsvLineSplitter splitter = new CsvLineSplitter();
        private final RowTokenSplitter tokenizer = new RowTokenSplitter();
        private final RowSanitizer sanitizer = new RowSanitizer();
        private final RowValidatorMapper mapper = new RowValidatorMapper();

        public PipelineOutput importAll(String rawCsv) {
            List<String> lines = splitter.splitNonEmptyLines(rawCsv);
            List<String[]> tokens = tokenizer.tokenize(lines);
            List<String[]> clean = sanitizer.trimAndNormalize(tokens);
            return mapper.validateAndBuild(clean);
        }
    }
}
