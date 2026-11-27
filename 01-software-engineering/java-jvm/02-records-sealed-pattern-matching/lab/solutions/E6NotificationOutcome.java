package lab.jvm.dop.solutions;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Reference solution for Exercise E6.
 */
public sealed interface E6NotificationOutcome
        permits E6NotificationOutcome.Sent, E6NotificationOutcome.Throttled, E6NotificationOutcome.Bounced {

    record Sent(String providerMsgId, Instant sentAt, List<String> headers)
            implements E6NotificationOutcome {
        public Sent {
            if (providerMsgId == null || providerMsgId.isBlank()) {
                throw new IllegalArgumentException("providerMsgId must not be blank");
            }
            Objects.requireNonNull(sentAt, "sentAt must not be null");
            headers = headers == null ? List.of() : List.copyOf(headers);
        }
    }

    record Throttled(int retryAfterSeconds) implements E6NotificationOutcome {
        public Throttled {
            if (retryAfterSeconds <= 0) {
                throw new IllegalArgumentException("retryAfterSeconds must be > 0");
            }
        }
    }

    record Bounced(String bounceCode, String reason, boolean permanent)
            implements E6NotificationOutcome {
        public Bounced {
            if (bounceCode == null || bounceCode.isBlank()) {
                throw new IllegalArgumentException("bounceCode must not be blank");
            }
            reason = reason == null ? "" : reason;
        }
    }
}
