package lab.linux.tls;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TlsLabTest {

    @Test
    void untrustedTrustStoreTriggersPkixPathBuildingFailedAndTrustedStoreSucceedsWithTls13(@TempDir Path tempDir)
            throws Exception {
        TlsLabDemo demo = new TlsLabDemo();
        var bundle = demo.generateMaterial(tempDir);

        try (var server = new TlsLabDemo.LoopbackTlsServer(bundle.serverKeystore(), bundle.serverTruststore(), false)) {
            assertThatThrownBy(() -> demo.connect(server.port(), null, bundle.wrongTruststore(), "localhost"))
                    .isInstanceOf(SSLHandshakeException.class)
                    .hasMessageContaining("PKIX path building failed");

            var ok = demo.connect(server.port(), null, bundle.clientTruststore(), "localhost");
            assertThat(ok.protocol()).isEqualTo("TLSv1.3");
            assertThat(ok.reply()).isEqualTo("TLS_OK:PING");
        }
    }

    @Test
    void mtlsServerRejectsClientWithoutCertificateAndAcceptsValidClientKeystore(@TempDir Path tempDir)
            throws Exception {
        TlsLabDemo demo = new TlsLabDemo();
        var bundle = demo.generateMaterial(tempDir);

        try (var mtlsServer = new TlsLabDemo.LoopbackTlsServer(bundle.serverKeystore(), bundle.serverTruststore(), true)) {
            assertThatThrownBy(() -> demo.connect(mtlsServer.port(), null, bundle.clientTruststore(), "localhost"))
                    .isInstanceOf(IOException.class);

            var ok = demo.connect(mtlsServer.port(), bundle.clientKeystore(), bundle.clientTruststore(), "localhost");
            assertThat(ok.protocol()).isEqualTo("TLSv1.3");
            assertThat(ok.reply()).isEqualTo("MTLS_OK:PING");
        }
    }
}
