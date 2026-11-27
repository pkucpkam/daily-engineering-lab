package lab.jvm.dop;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DopComparisonTest {

    @Test
    void recordCompactConstructorProtectsAgainstExternalListMutation() {
        List<String> tags = new ArrayList<>(List.of("VIP"));
        var result = new PaymentResult.Success("TX-1", new BigDecimal("500"), Instant.EPOCH, tags);
        tags.add("TAMPERED");

        assertThat(result.auditTags()).containsExactly("VIP");
        assertThatThrownBy(() -> result.auditTags().add("DIRECT_MUTATE"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void switchPatternMatchingRoutesAllSealedSubtypesAndNullSafely() {
        assertThat(PaymentResultHandler.decideAction(null)).isEqualTo("INVALID_NULL_RESULT");
        assertThat(PaymentResultHandler.decideAction(
                new PaymentResult.Success("TX-LOW", new BigDecimal("500"), Instant.EPOCH, List.of())))
                .isEqualTo("SETTLED:TX-LOW:500");
        assertThat(PaymentResultHandler.decideAction(
                new PaymentResult.Success("TX-HI", new BigDecimal("10000000"), Instant.EPOCH, List.of("A", "B"))))
                .isEqualTo("SETTLED_HIGH_VALUE:TX-HI:tags=2");
        assertThat(PaymentResultHandler.decideAction(
                new PaymentResult.Pending("C-1", 3, Instant.EPOCH)))
                .isEqualTo("ESCALATE_PENDING:C-1:attempt=3");
        assertThat(PaymentResultHandler.decideAction(
                new PaymentResult.Failure("TIMEOUT", "504", true)))
                .isEqualTo("SCHEDULE_RETRY:TIMEOUT:504");
        assertThat(PaymentResultHandler.decideAction(
                new PaymentResult.Failure("DECLINED", "bad cvv", false)))
                .isEqualTo("TERMINAL_FAILURE:DECLINED:bad cvv");
    }

    @Test
    void recordDeserializationInvokesCanonicalConstructorWhereasPojoBypassesIt() throws Exception {
        // Valid objects serialized normally
        byte[] recordBytes = serialize(new PositiveRecord(10));
        byte[] pojoBytes = serialize(new PositivePojo(10));

        // Corrupt serialized int field from 10 (0x0000000A) to -5 (0xFFFFFFFB)
        byte[] tamperedRecord = replaceLastInt(recordBytes, 10, -5);
        byte[] tamperedPojo = replaceLastInt(pojoBytes, 10, -5);

        // Record spec requires ObjectInputStream to call the canonical constructor -> rejects -5!
        assertThatThrownBy(() -> deserialize(tamperedRecord))
                .isInstanceOf(InvalidObjectException.class)
                .hasMessageContaining("must be > 0");

        // Ordinary class / Lombok @Value-style POJO bypasses constructor -> silently produces invalid object (-5)!
        PositivePojo corruptedPojo = (PositivePojo) deserialize(tamperedPojo);
        assertThat(corruptedPojo.getValue()).isEqualTo(-5);
    }

    @Test
    void sealedInterfaceExposesPermittedSubclassesViaReflection() {
        assertThat(PaymentResult.class.isSealed()).isTrue();
        assertThat(PaymentResult.class.getPermittedSubclasses())
                .containsExactlyInAnyOrder(
                        PaymentResult.Success.class,
                        PaymentResult.Pending.class,
                        PaymentResult.Failure.class);
    }

    public record PositiveRecord(int value) implements Serializable {
        public PositiveRecord {
            if (value <= 0) {
                throw new IllegalArgumentException("value must be > 0");
            }
        }
    }

    public static final class PositivePojo implements Serializable {
        private static final long serialVersionUID = ObjectStreamClass.lookup(PositivePojo.class) != null ? 1L : 1L;
        private final int value;

        public PositivePojo(int value) {
            if (value <= 0) {
                throw new IllegalArgumentException("value must be > 0");
            }
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static byte[] serialize(Object obj) throws Exception {
        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    private static Object deserialize(byte[] bytes) throws Exception {
        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return ois.readObject();
        }
    }

    private static byte[] replaceLastInt(byte[] data, int oldVal, int newVal) {
        byte[] copy = data.clone();
        byte b0 = (byte) (oldVal >>> 24);
        byte b1 = (byte) (oldVal >>> 16);
        byte b2 = (byte) (oldVal >>> 8);
        byte b3 = (byte) oldVal;
        for (int i = copy.length - 4; i >= 0; i--) {
            if (copy[i] == b0 && copy[i + 1] == b1 && copy[i + 2] == b2 && copy[i + 3] == b3) {
                copy[i] = (byte) (newVal >>> 24);
                copy[i + 1] = (byte) (newVal >>> 16);
                copy[i + 2] = (byte) (newVal >>> 8);
                copy[i + 3] = (byte) newVal;
                return copy;
            }
        }
        throw new IllegalStateException("int value not found in stream");
    }
}
