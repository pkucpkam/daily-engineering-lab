package lab.linux.fd;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FdLeakTest {

    @Test
    void leakedHttpSocketsExhaustProcessFdTableAndBlockUnrelatedFileOpens() throws Exception {
        BoundedFdTable table = new BoundedFdTable(16);
        List<BoundedFdTable.FdHandle> leaked = new ArrayList<>();

        // 0,1,2 đã chiếm bởi stdio -> mở được tối đa 13 socket (fd 3..15)
        for (int i = 0; i < 13; i++) {
            leaked.add(table.openSocket("10.20.9.88:8080"));
        }
        assertThat(table.openCount()).isEqualTo(16);

        // Đến socket thứ 14 ném EMFILE, và mở file log cũng fail theo!
        assertThatThrownBy(() -> table.openSocket("10.20.9.88:8080"))
                .isInstanceOf(BoundedFdTable.TooManyOpenFilesException.class);
        assertThatThrownBy(() -> table.openFile("/opt/jboss/standalone/log/server.log"))
                .isInstanceOf(BoundedFdTable.TooManyOpenFilesException.class);

        leaked.forEach(BoundedFdTable.FdHandle::close);
        assertThat(table.openCount()).isEqualTo(3);
    }

    @Test
    void tryWithResourcesAllowsUnlimitedSequentialRequestsWithinSmallFdLimit() throws Exception {
        BoundedFdTable table = new BoundedFdTable(16);

        for (int i = 0; i < 1000; i++) {
            try (var handle = table.openSocket("10.20.9.88:8080")) {
                // Linux luôn cấp lại số fd nhỏ nhất trống (3) vì mỗi vòng đều đóng ngay!
                assertThat(handle.fd()).isEqualTo(3);
            }
        }
        assertThat(table.openCount()).isEqualTo(3);
    }

    @Test
    void realLoopbackSocketsCloseCleanlyWithTryWithResources() throws IOException {
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress("127.0.0.1", 0));
            int port = ((InetSocketAddress) server.getLocalAddress()).getPort();

            for (int i = 0; i < 50; i++) {
                try (SocketChannel client = SocketChannel.open(new InetSocketAddress("127.0.0.1", port));
                     SocketChannel accepted = server.accept()) {
                    assertThat(client.isOpen()).isTrue();
                    assertThat(accepted.isOpen()).isTrue();
                }
            }
        }
    }

    @Test
    void nonBlockingReadReturnsZeroImmediatelyWhenNoDataAvailable() throws IOException {
        NonBlockingIoDemo demo = new NonBlockingIoDemo();
        assertThat(demo.readOnceNonBlockingWithoutData()).isZero();
    }
}
