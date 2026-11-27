package lab.linux.tls.solutions;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Duration;

/**
 * Lời giải tham khảo (LNX-12):
 * Tạo HttpClient dùng TrustStore riêng (hoặc KeyStore + TrustStore cho mTLS),
 * giữ nguyên Hostname SAN Verification và TLSv1.3 — KHÔNG bao giờ tắt TrustManager!
 */
public final class CustomTrustStoreSolution {

    public static HttpClient createSecureHttpClient(
            Path truststorePath,
            char[] truststorePass,
            Path optionalClientKeystorePath,
            char[] optionalKeystorePass) throws Exception {

        KeyStore ts = KeyStore.getInstance("PKCS12");
        try (InputStream is = Files.newInputStream(truststorePath)) {
            ts.load(is, truststorePass);
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        KeyManagerFactory kmf = null;
        if (optionalClientKeystorePath != null) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream is = Files.newInputStream(optionalClientKeystorePath)) {
                ks.load(is, optionalKeystorePass);
            }
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, optionalKeystorePass);
        }

        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(kmf != null ? kmf.getKeyManagers() : null, tmf.getTrustManagers(), null);

        SSLParameters sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm("HTTPS");
        sslParams.setProtocols(new String[]{"TLSv1.3", "TLSv1.2"});

        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParams)
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }
}
