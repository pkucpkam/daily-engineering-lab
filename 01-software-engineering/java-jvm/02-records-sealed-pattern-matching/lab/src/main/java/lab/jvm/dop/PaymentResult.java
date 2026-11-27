package lab.jvm.dop;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Sealed hierarchy modeling mutually exclusive payment outcomes.
 * Eliminates mega-DTOs with contradictory nullable fields.
 */
public sealed interface PaymentResult extends Serializable
        permits PaymentResult.Success, PaymentResult.Pending, PaymentResult.Failure {

    record Success(
            String txId,
            BigDecimal amount,
            Instant settledAt,
            List<String> auditTags) implements PaymentResult {

        // Compact constructor: enforces invariants AND defensive copy for shallow immutability.
        public Success {
            if (txId == null || txId.isBlank()) {
                throw new IllegalArgumentException("txId must not be blank");
            }
            if (amount == null || amount.signum() <= 0) {
                throw new IllegalArgumentException("amount must be positive");
            }
            Objects.requireNonNull(settledAt, "settledAt must not be null");
            // List.copyOf throws NPE on null elements and returns an unmodifiable copy
            auditTags = auditTags == null ? List.of() : List.copyOf(auditTags);
        }
    }

    record Pending(
            String correlationId,
            int attempt,
            Instant nextCheckAt) implements PaymentResult {

        public Pending {
            if (correlationId == null || correlationId.isBlank()) {
                throw new IllegalArgumentException("correlationId must not be blank");
            }
            if (attempt < 1) {
                throw new IllegalArgumentException("attempt must be >= 1");
            }
            Objects.requireNonNull(nextCheckAt, "nextCheckAt must not be null");
        }
    }

    record Failure(
            String errorCode,
            String reason,
            boolean retryable) implements PaymentResult {

        public Failure {
            if (errorCode == null || errorCode.isBlank()) {
                throw new IllegalArgumentException("errorCode must not be blank");
            }
            reason = reason == null ? "" : reason;
        }
    }
}
