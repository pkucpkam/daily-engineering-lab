package lab.jvm.bridge;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Minimal class designed for inspecting compiled bytecode with:
 *   javap -c -p -v target/classes/lab/jvm/bridge/BytecodeInspectTarget.class
 */
public class BytecodeInspectTarget {

    public void sortWithAnonymousClass(List<String> items) {
        items.sort(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(a.length(), b.length());
            }
        });
    }

    public void sortWithNonCapturingLambda(List<String> items) {
        items.sort((a, b) -> Integer.compare(a.length(), b.length()));
    }

    public Function<String, String> capturingLambda(String prefix) {
        return s -> prefix + s;
    }

    public String varAndTextBlockDemo(OrderStatus status) {
        var label = switch (status) {
            case PENDING, APPROVED -> "ACTIVE";
            case PAID -> "DONE";
            case REJECTED, CANCELLED -> "CLOSED";
        };
        return """
                status=%s
                """.formatted(label);
    }
}
