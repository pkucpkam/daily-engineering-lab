package lab.linux.systemd;

/**
 * Mô phỏng vòng đời unit trong systemd:
 * - Xử lý exit code 143 khi JVM nhận SIGTERM (có vs thiếu SuccessExitStatus=143).
 * - Xử lý crash loop với Restart=always vs Restart=on-failure + StartLimitBurst.
 */
public final class SystemdStateSimulator {

    public enum UnitState {
        ACTIVE_RUNNING,
        INACTIVE_DEAD,
        FAILED
    }

    private final boolean acceptExit143;
    private final String restartPolicy;
    private final int startLimitBurst;
    private int restarts;
    private UnitState state = UnitState.ACTIVE_RUNNING;
    private String result = "success";

    public SystemdStateSimulator(boolean acceptExit143, String restartPolicy, int startLimitBurst) {
        this.acceptExit143 = acceptExit143;
        this.restartPolicy = restartPolicy;
        this.startLimitBurst = startLimitBurst;
    }

    public void stopWithJvmSigtermExit143() {
        if (acceptExit143) {
            state = UnitState.INACTIVE_DEAD;
            result = "success";
        } else {
            state = UnitState.FAILED;
            result = "exit-code(143)";
        }
    }

    public void onProcessCrash(int exitCode) {
        boolean shouldRestart = "always".equals(restartPolicy)
                || ("on-failure".equals(restartPolicy) && exitCode != 0);
        if (!shouldRestart) {
            state = exitCode == 0 ? UnitState.INACTIVE_DEAD : UnitState.FAILED;
            return;
        }
        if (startLimitBurst > 0 && restarts >= startLimitBurst) {
            state = UnitState.FAILED;
            result = "start-limit-hit";
            return;
        }
        restarts++;
        state = UnitState.ACTIVE_RUNNING;
    }

    public UnitState state() {
        return state;
    }

    public int restarts() {
        return restarts;
    }

    public String result() {
        return result;
    }
}
