import java.util.Map;

/**
 * L3 Solution: deep config module hiding fallback keys, parsing, and bounds validation
 * behind simple typed accessors (instead of forcing every caller to trim/parse strings).
 */
public class DeepConfigLoader {

    private final Map<String, String> source;

    public DeepConfigLoader(Map<String, String> source) {
        this.source = Map.copyOf(source);
    }

    public int positiveIntOrDefault(String key, int defaultValue) {
        String raw = source.get(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int val = Integer.parseInt(raw.trim());
            if (val <= 0) {
                throw new IllegalArgumentException("Config '" + key + "' must be > 0, got: " + val);
            }
            return val;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Config '" + key + "' must be an integer, got: " + raw, ex);
        }
    }

    public static void main(String[] args) {
        var cfg = new DeepConfigLoader(Map.of("db.pool.max", " 16 ", "db.timeout.ms", " "));
        System.out.println("pool.max=" + cfg.positiveIntOrDefault("db.pool.max", 8));
        System.out.println("timeout.ms=" + cfg.positiveIntOrDefault("db.timeout.ms", 3000));
    }
}
