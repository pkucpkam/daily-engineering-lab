package lab.jvm.dop;

import java.math.BigDecimal;

/**
 * Data-Oriented Programming operations over PaymentResult using Java 21
 * switch pattern matching, record patterns (JEP 440), and guarded patterns (JEP 441).
 */
public final class PaymentResultHandler {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000000");

    private PaymentResultHandler() {
    }

    public static String decideAction(PaymentResult result) {
        // Note: NO 'default' branch so the compiler fails the build if a new subtype is added to PaymentResult.
        return switch (result) {
            case null -> "INVALID_NULL_RESULT";
            case PaymentResult.Success(var txId, var amount, var ignored, var tags)
                    when amount.compareTo(HIGH_VALUE_THRESHOLD) >= 0 ->
                    "SETTLED_HIGH_VALUE:" + txId + ":tags=" + tags.size();
            case PaymentResult.Success(var txId, var amount, var ignored, var tags) ->
                    "SETTLED:" + txId + ":" + amount.toPlainString();
            case PaymentResult.Pending(var corrId, var attempt, var nextCheck) when attempt >= 3 ->
                    "ESCALATE_PENDING:" + corrId + ":attempt=" + attempt;
            case PaymentResult.Pending(var corrId, var attempt, var nextCheck) ->
                    "POLL_LATER:" + corrId + ":at=" + nextCheck;
            case PaymentResult.Failure(var code, var reason, var retryable) when retryable ->
                    "SCHEDULE_RETRY:" + code + ":" + reason;
            case PaymentResult.Failure(var code, var reason, var ignored) ->
                    "TERMINAL_FAILURE:" + code + ":" + reason;
        };
    }

    public static String summarizeHttpStatus(Object rawResponse) {
        // Demonstrates instanceof pattern matching with flow-scoped variable
        if (rawResponse instanceof String body && !body.isBlank()) {
            return "TEXT:" + body.trim();
        }
        if (rawResponse instanceof PaymentResult result) {
            return decideAction(result);
        }
        return "UNSUPPORTED";
    }
}
