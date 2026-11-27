package lab.linux.tls;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Runner thực nghiệm cho LNX-12.
 */
public final class TlsLabRunner {

    public static void main(String[] args) throws Exception {
        Path tmp = Files.createTempDirectory("lnx12-tls-lab");
        try {
            TlsLabDemo demo = new TlsLabDemo();
            var bundle = demo.generateMaterial(tmp);

            try (var oneWayServer = new TlsLabDemo.LoopbackTlsServer(bundle.serverKeystore(), bundle.serverTruststore(), false)) {
                System.out.println("=== [1] Thử kết nối với TrustStore sai (tái hiện PKIX path building failed) ===");
                try {
                    demo.connect(oneWayServer.port(), null, bundle.wrongTruststore(), "localhost");
                } catch (Exception ex) {
                    System.out.println("Caught expected error: " + ex.getClass().getSimpleName() + " -> " + ex.getMessage());
                }

                System.out.println("\n=== [2] Kết nối với TrustStore hợp lệ + SNI + HTTPS SAN Verification ===");
                var ok = demo.connect(oneWayServer.port(), null, bundle.clientTruststore(), "localhost");
                System.out.printf("Handshake OK | protocol=%s | cipher=%s | reply=%s%n",
                        ok.protocol(), ok.cipherSuite(), ok.reply());
            }

            try (var mtlsServer = new TlsLabDemo.LoopbackTlsServer(bundle.serverKeystore(), bundle.serverTruststore(), true)) {
                System.out.println("\n=== [3] mTLS Server (needClientAuth=true): Client THIẾU KeyStore ===");
                try {
                    demo.connect(mtlsServer.port(), null, bundle.clientTruststore(), "localhost");
                } catch (Exception ex) {
                    System.out.println("Rejected expected: " + ex.getClass().getSimpleName() + " -> " + ex.getMessage());
                }

                System.out.println("\n=== [4] mTLS Server: Client CÓ KeyStore + TrustStore hợp lệ ===");
                var mtlsOk = demo.connect(mtlsServer.port(), bundle.clientKeystore(), bundle.clientTruststore(), "localhost");
                System.out.printf("mTLS Handshake OK | protocol=%s | cipher=%s | reply=%s%n",
                        mtlsOk.protocol(), mtlsOk.cipherSuite(), mtlsOk.reply());
            }
        } finally {
            try (var stream = Files.walk(tmp)) {
                stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (Exception ignored) {
                    }
                });
            }
        }
    }
}
