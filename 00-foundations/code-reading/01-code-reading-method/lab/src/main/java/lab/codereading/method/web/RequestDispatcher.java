package lab.codereading.method.web;

import lab.codereading.method.model.HttpResponseData;
import lab.codereading.method.model.OrderCommand;

import java.util.Map;
import java.util.UUID;

/**
 * Simulates servlet/web filter chain before dispatching to OrderController:
 *  1) TracingFilter attaches traceId
 *  2) AuthFilter validates X-Api-Key header
 *  3) Dispatcher invokes OrderController
 */
public class RequestDispatcher {

    private static final String VALID_TOKEN = "secret-dev-token";
    private final OrderController orderController;

    public RequestDispatcher(OrderController orderController) {
        this.orderController = orderController;
    }

    public HttpResponseData dispatch(String path, Map<String, String> headers, OrderCommand body) {
        String traceId = headers.getOrDefault("X-Trace-Id", UUID.randomUUID().toString().substring(0, 8));
        if (!"/api/orders".equals(path)) {
            return new HttpResponseData(404, traceId, "NOT_FOUND:" + path, null);
        }
        String token = headers.get("X-Api-Key");
        if (!VALID_TOKEN.equals(token)) {
            return new HttpResponseData(401, traceId, "UNAUTHORIZED", null);
        }
        return orderController.handleCreateOrder(traceId, body);
    }
}
