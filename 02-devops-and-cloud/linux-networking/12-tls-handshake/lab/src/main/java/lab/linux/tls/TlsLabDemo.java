package lab.linux.tls;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lab Java 21 thuần dùng keytool + SSLServerSocket thật trên loopback:
 * 1) Tái hiện chính xác lỗi "PKIX path building failed" khi TrustStore không tin cậy CA server.
 * 2) Sửa chuẩn bằng TrustStore + SNI + EndpointIdentificationAlgorithm("HTTPS"), đàm phán TLSv1.3.
 * 3) Kiểm chứng mTLS (needClientAuth = true): từ chối client thiếu KeyStore, chấp nhận client có KeyStore hợp lệ.
 */
public final class TlsLabDemo {

    public static final char[] STORE_PASS = "changeit".toCharArray();

    public record CertBundle(
            Path workDir,
            Path serverKeystore,
            Path serverTruststore,
            Path wrongTruststore,
            Path clientKeystore,
            Path clientTruststore) {}

    public record HandshakeOutcome(String protocol, String cipherSuite, String reply) {}

    public CertBundle generateMaterial(Path workDir) throws Exception {
        Files.createDirectories(workDir);
        Path serverKs = workDir.resolve("server-keystore.p12");
        Path serverCrt = workDir.resolve("server.crt");
        Path clientKs = workDir.resolve("client-keystore.p12");
        Path clientCrt = workDir.resolve("client.crt");
        Path unrelatedKs = workDir.resolve("unrelated.p12");
        Path unrelatedCrt = workDir.resolve("unrelated.crt");

        Path clientTs = workDir.resolve("client-truststore.p12");
        Path serverTs = workDir.resolve("server-truststore.p12");
        Path wrongTs = workDir.resolve("wrong-truststore.p12");

        // 1. Tạo Server KeyStore có SAN chuẩn cho localhost / 127.0.0.1
        runKeytool("-genkeypair", "-alias", "server", "-keyalg", "RSA", "-keysize", "2048",
                "-storetype", "PKCS12", "-keystore", serverKs.toString(), "-storepass", "changeit",
                "-dname", "CN=localhost,O=DailyEngLab", "-ext", "SAN=dns:localhost,ip:127.0.0.1",
                "-validity", "30");
        runKeytool("-exportcert", "-alias", "server", "-keystore", serverKs.toString(),
                "-storepass", "changeit", "-rfc", "-file", serverCrt.toString());

        // 2. Tạo Client KeyStore dùng cho mTLS
        runKeytool("-genkeypair", "-alias", "client", "-keyalg", "RSA", "-keysize", "2048",
                "-storetype", "PKCS12", "-keystore", clientKs.toString(), "-storepass", "changeit",
                "-dname", "CN=jboss-client-01,O=DailyEngLab", "-validity", "30");
        runKeytool("-exportcert", "-alias", "client", "-keystore", clientKs.toString(),
                "-storepass", "changeit", "-rfc", "-file", clientCrt.toString());

        // 3. Tạo CA không liên quan để wrongTruststore có anchor hợp lệ nhưng KHÔNG khớp server -> nổ đúng PKIX!
        runKeytool("-genkeypair", "-alias", "unrelated", "-keyalg", "RSA", "-keysize", "2048",
                "-storetype", "PKCS12", "-keystore", unrelatedKs.toString(), "-storepass", "changeit",
                "-dname", "CN=unrelated.local", "-validity", "30");
        runKeytool("-exportcert", "-alias", "unrelated", "-keystore", unrelatedKs.toString(),
                "-storepass", "changeit", "-rfc", "-file", unrelatedCrt.toString());

        // 4. Nạp các TrustStore
        runKeytool("-importcert", "-noprompt", "-alias", "server-ca", "-file", serverCrt.toString(),
                "-storetype", "PKCS12", "-keystore", clientTs.toString(), "-storepass", "changeit");
        runKeytool("-importcert", "-noprompt", "-alias", "client-ca", "-file", clientCrt.toString(),
                "-storetype", "PKCS12", "-keystore", serverTs.toString(), "-storepass", "changeit");
        runKeytool("-importcert", "-noprompt", "-alias", "wrong-ca", "-file", unrelatedCrt.toString(),
                "-storetype", "PKCS12", "-keystore", wrongTs.toString(), "-storepass", "changeit");

        return new CertBundle(workDir, serverKs, serverTs, wrongTs, clientKs, clientTs);
    }

    public HandshakeOutcome connect(
            int port,
            Path clientKeystoreOrNull,
            Path clientTruststore,
            String sniHost) throws Exception {
        SSLContext ctx = buildSslContext(clientKeystoreOrNull, clientTruststore);
        try (SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket()) {
            SSLParameters params = socket.getSSLParameters();
            params.setEndpointIdentificationAlgorithm("HTTPS");
            params.setServerNames(List.of(new SNIHostName(sniHost)));
            socket.setSSLParameters(params);
            socket.setSoTimeout(3000);
            socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 3000);
            socket.startHandshake();

            SSLSession session = socket.getSession();
            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out.println("PING");
            String reply = in.readLine();
            return new HandshakeOutcome(session.getProtocol(), session.getCipherSuite(), reply);
        }
    }

    public static SSLContext buildSslContext(Path keystorePathOrNull, Path truststorePathOrNull) throws Exception {
        KeyManagerFactory kmf = null;
        if (keystorePathOrNull != null) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream is = Files.newInputStream(keystorePathOrNull)) {
                ks.load(is, STORE_PASS);
            }
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, STORE_PASS);
        }

        TrustManagerFactory tmf = null;
        if (truststorePathOrNull != null) {
            KeyStore ts = KeyStore.getInstance("PKCS12");
            try (InputStream is = Files.newInputStream(truststorePathOrNull)) {
                ts.load(is, STORE_PASS);
            }
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
        }

        SSLContext ctx = SSLContext.getInstance("TLSv1.3");
        ctx.init(
                kmf != null ? kmf.getKeyManagers() : null,
                tmf != null ? tmf.getTrustManagers() : null,
                null);
        return ctx;
    }

    private static void runKeytool(String... args) throws Exception {
        String ext = System.getProperty("os.name").toLowerCase().contains("win") ? "keytool.exe" : "keytool";
        Path keytoolBin = Path.of(System.getProperty("java.home"), "bin", ext);
        List<String> cmd = new ArrayList<>();
        cmd.add(keytoolBin.toString());
        cmd.addAll(List.of(args));

        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = p.waitFor();
        if (exit != 0) {
            throw new IllegalStateException("keytool failed (exit=" + exit + "): " + output);
        }
    }

    public static final class LoopbackTlsServer implements AutoCloseable {
        private final SSLServerSocket serverSocket;
        private final ExecutorService workers = Executors.newCachedThreadPool();
        private volatile boolean running = true;

        public LoopbackTlsServer(Path serverKeystore, Path serverTruststore, boolean requireClientAuth)
                throws Exception {
            SSLContext ctx = buildSslContext(serverKeystore, serverTruststore);
            this.serverSocket = (SSLServerSocket) ctx.getServerSocketFactory()
                    .createServerSocket(0, 64, InetAddress.getLoopbackAddress());
            this.serverSocket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
            this.serverSocket.setNeedClientAuth(requireClientAuth);

            Thread acceptor = new Thread(() -> {
                while (running) {
                    try {
                        SSLSocket client = (SSLSocket) serverSocket.accept();
                        workers.submit(() -> handle(client));
                    } catch (IOException ignored) {
                        if (!running) {
                            return;
                        }
                    }
                }
            }, "lnx12-tls-acceptor");
            acceptor.setDaemon(true);
            acceptor.start();
        }

        public int port() {
            return serverSocket.getLocalPort();
        }

        private void handle(SSLSocket client) {
            try (client;
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(
                         new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {
                client.setSoTimeout(3000);
                client.startHandshake();
                String line = in.readLine();
                if (line != null) {
                    out.println(serverSocket.getNeedClientAuth() ? "MTLS_OK:" + line : "TLS_OK:" + line);
                }
            } catch (SSLHandshakeException ignored) {
            } catch (IOException ignored) {
            }
        }

        @Override
        public void close() throws IOException {
            running = false;
            serverSocket.close();
            workers.shutdownNow();
        }
    }
}
