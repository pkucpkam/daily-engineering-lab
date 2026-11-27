package lab.linux.tcp;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Mô hình hóa chính xác TCP State Machine (3-way handshake, 4-way teardown)
 * và 2 hàng đợi (SYN Queue + Accept Queue) khi server lắng nghe ở trạng thái LISTEN.
 */
public final class TcpStateMachine {

    public enum State {
        CLOSED,
        LISTEN,
        SYN_SENT,
        SYN_RCVD,
        ESTABLISHED,
        FIN_WAIT_1,
        FIN_WAIT_2,
        TIME_WAIT,
        CLOSE_WAIT,
        LAST_ACK,
        RESET
    }

    public static final class Endpoint {
        private State state;

        public Endpoint(State initial) {
            this.state = initial;
        }

        public State state() {
            return state;
        }

        public void transition(State next) {
            this.state = next;
        }
    }

    public static final class ServerListener {
        private final int synQueueCapacity;
        private final int acceptQueueCapacity;
        private final boolean abortOnOverflow;
        private final Deque<Endpoint> synQueue = new ArrayDeque<>();
        private final Deque<Endpoint> acceptQueue = new ArrayDeque<>();
        private int listenOverflows;

        public ServerListener(int synQueueCapacity, int acceptQueueCapacity, boolean abortOnOverflow) {
            this.synQueueCapacity = synQueueCapacity;
            this.acceptQueueCapacity = acceptQueueCapacity;
            this.abortOnOverflow = abortOnOverflow;
        }

        public boolean onSynFromClient(Endpoint client) {
            if (synQueue.size() >= synQueueCapacity) {
                return false;
            }
            client.transition(State.SYN_SENT);
            Endpoint serverHalfOpen = new Endpoint(State.SYN_RCVD);
            synQueue.addLast(serverHalfOpen);
            // Client nhận SYN-ACK và gửi ACK -> phía client chuyển sang ESTABLISHED
            client.transition(State.ESTABLISHED);
            return true;
        }

        /**
         * Xử lý ACK cuối của 3-way handshake:
         * - Nếu Accept Queue còn chỗ -> chuyển socket vào Accept Queue (ESTABLISHED).
         * - Nếu đầy và abortOnOverflow=false -> drop ACK âm thầm (server giữ SYN_RCVD, client vẫn ESTABLISHED!).
         * - Nếu đầy và abortOnOverflow=true -> gửi RST (client chuyển RESET).
         */
        public boolean onFinalAckFromClient(Endpoint client) {
            Endpoint halfOpen = synQueue.peekFirst();
            if (halfOpen == null) {
                return false;
            }
            if (acceptQueue.size() >= acceptQueueCapacity) {
                listenOverflows++;
                if (abortOnOverflow) {
                    synQueue.pollFirst();
                    client.transition(State.RESET);
                }
                // Nếu abortOnOverflow=false: kernel drop ACK, giữ halfOpen ở SYN_RCVD!
                return false;
            }
            synQueue.pollFirst();
            halfOpen.transition(State.ESTABLISHED);
            acceptQueue.addLast(halfOpen);
            return true;
        }

        public Endpoint appAccept() {
            return acceptQueue.pollFirst();
        }

        public int synQueueSize() {
            return synQueue.size();
        }

        public int acceptQueueSize() {
            return acceptQueue.size();
        }

        public int listenOverflows() {
            return listenOverflows;
        }
    }

    /**
     * Thực hiện 4-way teardown chuẩn: bên chủ động close() vào TIME_WAIT, bên bị động vào CLOSE_WAIT.
     */
    public void performActiveClose(Endpoint activeSide, Endpoint passiveSide, boolean passiveAppCallsClose) {
        activeSide.transition(State.FIN_WAIT_1);
        passiveSide.transition(State.CLOSE_WAIT);
        activeSide.transition(State.FIN_WAIT_2);

        if (passiveAppCallsClose) {
            passiveSide.transition(State.LAST_ACK);
            activeSide.transition(State.TIME_WAIT);
            passiveSide.transition(State.CLOSED);
        } else {
            // Bên bị động quên gọi close() -> tích tụ CLOSE_WAIT!
            activeSide.transition(State.TIME_WAIT);
        }
    }
}
