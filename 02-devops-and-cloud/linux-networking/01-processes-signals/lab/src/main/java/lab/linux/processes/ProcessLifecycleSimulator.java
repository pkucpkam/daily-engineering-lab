package lab.linux.processes;

import java.util.ArrayList;
import java.util.List;

/**
 * Mô hình hóa trạng thái process Linux (R, S, D, Z, T) và cơ chế reap Zombie bởi Parent / PID 1.
 */
public final class ProcessLifecycleSimulator {

    public enum State {
        RUNNING,
        SLEEPING,
        DISK_IO_UNINTERRUPTIBLE,
        STOPPED,
        ZOMBIE,
        REAPED
    }

    public static final class SimulatedProcess {
        private final int pid;
        private int ppid;
        private final String command;
        private State state;
        private Integer exitCode;

        public SimulatedProcess(int pid, int ppid, String command) {
            this.pid = pid;
            this.ppid = ppid;
            this.command = command;
            this.state = State.RUNNING;
        }

        public int pid() {
            return pid;
        }

        public int ppid() {
            return ppid;
        }

        public String command() {
            return command;
        }

        public State state() {
            return state;
        }

        public Integer exitCode() {
            return exitCode;
        }

        public void enterUninterruptibleIo() {
            this.state = State.DISK_IO_UNINTERRUPTIBLE;
        }

        public void exitNormally(int code) {
            this.exitCode = code;
            this.state = State.ZOMBIE;
        }

        /**
         * Theo ngữ nghĩa Linux: SIGKILL không giết được ZOMBIE (đã chết) và không ngắt được D-state ngay.
         */
        public boolean sendSigKill() {
            if (state == State.ZOMBIE || state == State.DISK_IO_UNINTERRUPTIBLE || state == State.REAPED) {
                return false;
            }
            this.exitCode = 137;
            this.state = State.ZOMBIE;
            return true;
        }

        void adoptByPid1() {
            this.ppid = 1;
        }

        void reap() {
            if (this.state == State.ZOMBIE) {
                this.state = State.REAPED;
            }
        }
    }

    private final List<SimulatedProcess> table = new ArrayList<>();
    private final boolean pid1HasZombieReaper;

    public ProcessLifecycleSimulator(boolean pid1HasZombieReaper) {
        this.pid1HasZombieReaper = pid1HasZombieReaper;
    }

    public SimulatedProcess spawn(int pid, int ppid, String command) {
        SimulatedProcess p = new SimulatedProcess(pid, ppid, command);
        table.add(p);
        return p;
    }

    public void parentWait(int parentPid) {
        for (SimulatedProcess p : table) {
            if (p.ppid() == parentPid && p.state() == State.ZOMBIE) {
                p.reap();
            }
        }
    }

    public void killParentAndOrphanChildren(int parentPid) {
        for (SimulatedProcess p : table) {
            if (p.pid() == parentPid) {
                p.sendSigKill();
                p.reap();
            } else if (p.ppid() == parentPid) {
                p.adoptByPid1();
                if (pid1HasZombieReaper && p.state() == State.ZOMBIE) {
                    p.reap();
                }
            }
        }
    }

    public long countZombies() {
        return table.stream().filter(p -> p.state() == State.ZOMBIE).count();
    }
}
