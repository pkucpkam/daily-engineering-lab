package lab.jvm.dop.solutions;

import lab.jvm.dop.solutions.E6NotificationOutcome.Bounced;
import lab.jvm.dop.solutions.E6NotificationOutcome.Sent;
import lab.jvm.dop.solutions.E6NotificationOutcome.Throttled;

/**
 * Reference solution for Exercise E7 (exhaustive switch without default).
 */
public final class E7NotificationRouter {

    private E7NotificationRouter() {
    }

    public static String decideNextStep(E6NotificationOutcome outcome) {
        return switch (outcome) {
            case null -> "INVALID_NULL";
            case Sent(var msgId, var sentAt, var headers) when headers.contains("PRIORITY") ->
                    "LOG_PRIORITY:" + msgId;
            case Sent(var msgId, var sentAt, var headers) ->
                    "LOG_OK:" + msgId;
            case Throttled(var sec) when sec <= 60 ->
                    "FAST_RETRY:" + sec;
            case Throttled(var sec) ->
                    "DELAYED_RETRY:" + sec;
            case Bounced(var code, var reason, var permanent) when permanent ->
                    "DISABLE_RECIPIENT:" + code;
            case Bounced(var code, var reason, var ignored) ->
                    "SOFT_BOUNCE:" + code;
        };
    }
}
