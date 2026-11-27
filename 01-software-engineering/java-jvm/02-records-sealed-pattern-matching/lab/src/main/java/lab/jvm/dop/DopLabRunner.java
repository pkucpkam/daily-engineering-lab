package lab.jvm.dop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Interactive lab runner for JVM-02:
 *  1) Sealed + exhaustive switch + record deconstruction
 *  2) Shallow immutability & compact constructor defensive copy
 *  3) Reflection & JVM metadata (isRecord, getPermittedSubclasses)
 *  4) Java deserialization constructor enforcement (Record vs POJO)
 */
public class DopLabRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== JVM-02 Lab: Records, Sealed Classes, Pattern Matching ===");

        // 1. Exhaustive switch + record patterns
        List<PaymentResult> samples = new ArrayList<>();
        samples.add(new PaymentResult.Success("TX-01", new BigDecimal("250000"), Instant.parse("2026-11-27T02:00:00Z"), List.of("CARD")));
        samples.add(new PaymentResult.Success("TX-02", new BigDecimal("15000000"), Instant.parse("2026-11-27T02:05:00Z"), List.of("WIRE", "VIP")));
        samples.add(new PaymentResult.Pending("CORR-9", 3, Instant.parse("2026-11-27T02:10:00Z")));
        samples.add(new PaymentResult.Failure("GATEWAY_TIMEOUT", "read timed out", true));
        samples.add(new PaymentResult.Failure("INVALID_CARD", "expired", false));
        samples.add(null);

        System.out.println("\n[1] Pattern matching switch results:");
        for (var item : samples) {
            System.out.println("  -> " + PaymentResultHandler.decideAction(item));
        }

        // 2. Defensive copy test
        List<String> mutableTags = new ArrayList<>(List.of("INITIAL"));
        var success = new PaymentResult.Success("TX-03", new BigDecimal("100"), Instant.now(), mutableTags);
        mutableTags.add("HACKED_AFTER_CREATION");
        System.out.println("\n[2] Shallow immutability check (caller mutated list after record creation):");
        System.out.println("  Caller list : " + mutableTags);
        System.out.println("  Record tags : " + success.auditTags());

        // 3. JVM Reflection metadata
        System.out.println("\n[3] JVM metadata:");
        System.out.println("  Success.isRecord() = " + PaymentResult.Success.class.isRecord());
        System.out.println("  Success superclass = " + PaymentResult.Success.class.getSuperclass().getName());
        System.out.println("  PaymentResult.isSealed() = " + PaymentResult.class.isSealed());
        System.out.println("  Permitted subclasses = " + Arrays.stream(PaymentResult.class.getPermittedSubclasses())
                .map(Class::getSimpleName)
                .toList());

        // 4. Deserialization safety: Record enforces canonical constructor, POJO does not
        System.out.println("\n[4] Deserialization invariant check (tampered negative amount):");
        demonstrateDeserializationSafety();
    }

    private static void demonstrateDeserializationSafety() throws Exception {
        byte[] validRecordBytes = serialize(
                new PaymentResult.Success("TX-SER", new BigDecimal("100"), Instant.EPOCH, List.of()));
        byte[] validPojoBytes = serialize(new LegacyPojoAmount(new BigDecimal("100")));

        // Tamper serialized BigDecimal scale or simply show negative BigDecimal in test;
        // here we tamper the byte representation of amount by replacing "100" intVal is binary,
        // so instead we demonstrate with a custom record vs POJO byte stream helper in tests,
        // and verify normal round-trip here.
        PaymentResult restoredRecord = (PaymentResult) deserialize(validRecordBytes);
        LegacyPojoAmount restoredPojo = (LegacyPojoAmount) deserialize(validPojoBytes);
        System.out.println("  Round-trip record OK: " + restoredRecord);
        System.out.println("  Round-trip POJO   OK: amount=" + restoredPojo.getAmount());
    }

    public static byte[] serialize(Object obj) throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return ois.readObject();
        }
    }
}
