package lab.jvm.gc.solutions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Reference solution for Exercise E8:
 * Replaces Files.readAllBytes() (which allocates a Humongous byte[] on G1 for >1MB files)
 * with a reusable 16 KB streaming buffer that stays in Eden TLAB.
 */
public final class E8StreamingHashRefactored {

    private static final int STREAM_BUFFER_BYTES = 16 * 1024;

    private E8StreamingHashRefactored() {
    }

    public static String sha256LegacyHumongous(Path file) throws IOException, NoSuchAlgorithmException {
        byte[] allBytes = Files.readAllBytes(file); // Humongous allocation risk for large files!
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(allBytes);
        return HexFormat.of().formatHex(digest);
    }

    public static String sha256Streaming(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[STREAM_BUFFER_BYTES];
        try (InputStream in = Files.newInputStream(file)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(md.digest());
    }
}
