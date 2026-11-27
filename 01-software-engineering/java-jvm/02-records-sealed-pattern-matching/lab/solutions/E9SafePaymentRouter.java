package lab.jvm.dop.solutions;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Reference solution for Exercise E9: safe payment routing with TimedOut explicitly modeled.
 */
public final class E9SafePaymentRouter {

    public enum Action {
        CAPTURE,
        POLL_BANK_STATUS,
        RETRY_WITH_BACKOFF,
        REFUND_IMMEDIATELY
    }

    public sealed interface GatewayOutcome extends Serializable
            permits Captured, Pending, TimedOut, Declined {
    }

    public record Captured(String txId, Instant settledAt) implements GatewayOutcome {
        public Captured {
            if (txId == null || txId.isBlank()) {
                throw new IllegalArgumentException("txId must not be blank");
            }
            Objects.requireNonNull(settledAt, "settledAt must not be null");
        }
    }

    public record Pending(String correlationId, int attempt) implements GatewayOutcome {
        public Pending {
            if (correlationId == null || correlationId.isBlank()) {
                throw new IllegalArgumentException("correlationId must not be blank");
            }
            if (attempt < 1) {
                throw new IllegalArgumentException("attempt must be >= 1");
            }
        }
    }

    public record TimedOut(String correlationId, Instant timedOutAt) implements GatewayOutcome {
        public TimedOut {
            if (correlationId == null || correlationId.isBlank()) {
                throw new IllegalArgumentException("correlationId must not be blank");
            }
            Objects.requireNonNull(timedOutAt, "timedOutAt must not be null");
        }
    }

    public record Declined(String reasonCode, boolean retryable) implements GatewayOutcome {
        public Declined {
            if (reasonCode == null || reasonCode.isBlank()) {
                throw new IllegalArgumentException("reasonCode must not be blank");
            }
        }
    }

    public Action route(GatewayOutcome outcome) {
        // No default branch: adding Disputed later causes immediate compile error here.
        return switch (outcome) {
            case Captured c -> Action.CAPTURE;
            case Pending p -> Action.POLL_BANK_STATUS;
            case TimedOut t -> Action.POLL_BANK_STATUS;
            case Declined(var code, var retryable) when retryable -> Action.RETRY_WITH_BACKOFF;
            case Declined d -> Action.REFUND_IMMEDIATELY;
        };
    }
}
