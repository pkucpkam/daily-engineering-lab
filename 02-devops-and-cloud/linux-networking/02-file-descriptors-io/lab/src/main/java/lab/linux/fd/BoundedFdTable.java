package lab.linux.fd;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Mô phỏng chính xác bảng File Descriptor theo process của Linux kernel:
 * - FD 0, 1, 2 luôn dành sẵn cho stdin, stdout, stderr.
 * - Mỗi lần open()/socket() chọn số FD nhỏ nhất còn trống (< softLimit).
 * - Khi chạm softLimit -> ném TooManyOpenFilesException (EMFILE), chặn cả socket lẫn file log!
 */
public final class BoundedFdTable {

    public static final class TooManyOpenFilesException extends IOException {
        public TooManyOpenFilesException(String message) {
            super(message);
        }
    }

    public enum FdType {
        STANDARD_STREAM,
        REGULAR_FILE,
        TCP_SOCKET
    }

    public final class FdHandle implements AutoCloseable {
        private final int fd;
        private final FdType type;
        private final String target;
        private boolean closed;

        private FdHandle(int fd, FdType type, String target) {
            this.fd = fd;
            this.type = type;
            this.target = target;
        }

        public int fd() {
            return fd;
        }

        public FdType type() {
            return type;
        }

        public String target() {
            return target;
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                openEntries.remove(fd);
            }
        }
    }

    private final int softLimit;
    private final Map<Integer, FdHandle> openEntries = new TreeMap<>();

    public BoundedFdTable(int softLimit) {
        if (softLimit < 4) {
            throw new IllegalArgumentException("softLimit must be >= 4");
        }
        this.softLimit = softLimit;
        openEntries.put(0, new FdHandle(0, FdType.STANDARD_STREAM, "/dev/stdin"));
        openEntries.put(1, new FdHandle(1, FdType.STANDARD_STREAM, "/dev/stdout"));
        openEntries.put(2, new FdHandle(2, FdType.STANDARD_STREAM, "/dev/stderr"));
    }

    public synchronized FdHandle openSocket(String remoteEndpoint) throws TooManyOpenFilesException {
        return allocate(FdType.TCP_SOCKET, "TCP:" + remoteEndpoint);
    }

    public synchronized FdHandle openFile(String path) throws TooManyOpenFilesException {
        return allocate(FdType.REGULAR_FILE, path);
    }

    private FdHandle allocate(FdType type, String target) throws TooManyOpenFilesException {
        for (int candidate = 0; candidate < softLimit; candidate++) {
            if (!openEntries.containsKey(candidate)) {
                FdHandle handle = new FdHandle(candidate, type, target);
                openEntries.put(candidate, handle);
                return handle;
            }
        }
        throw new TooManyOpenFilesException(
                "EMFILE: Too many open files (open=" + openEntries.size() + ", softLimit=" + softLimit
                        + ") while opening " + target);
    }

    public synchronized int openCount() {
        return openEntries.size();
    }

    public synchronized int softLimit() {
        return softLimit;
    }
}
