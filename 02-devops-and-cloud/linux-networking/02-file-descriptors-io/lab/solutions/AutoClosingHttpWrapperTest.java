package lab.linux.fd.solutions;

import lab.linux.fd.BoundedFdTable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lời giải E6: Bọc tài nguyên socket/HTTP thành AutoCloseable và dùng try-with-resources
 * để giải phóng FD ngay khi rời khỏi scope thay vì đợi GC.
 */
class AutoClosingHttpWrapperTest {

    static final class SafeHttpCall implements AutoCloseable {
        private final BoundedFdTable.FdHandle socketFd;

        SafeHttpCall(BoundedFdTable table, String endpoint) throws BoundedFdTable.TooManyOpenFilesException {
            this.socketFd = table.openSocket(endpoint);
        }

        int status() {
            return 200;
        }

        @Override
        public void close() {
            socketFd.close();
        }
    }

    @Test
    void thousandCallsNeverExceedFourOpenFdsWhenUsingTryWithResources() throws Exception {
        BoundedFdTable table = new BoundedFdTable(10);
        for (int i = 0; i < 1000; i++) {
            try (SafeHttpCall call = new SafeHttpCall(table, "crm-api:8443")) {
                assertThat(call.status()).isEqualTo(200);
                assertThat(table.openCount()).isEqualTo(4);
            }
        }
        assertThat(table.openCount()).isEqualTo(3);
    }
}
