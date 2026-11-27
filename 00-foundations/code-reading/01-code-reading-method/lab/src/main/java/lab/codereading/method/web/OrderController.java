package lab.codereading.method.web;

import lab.codereading.method.model.HttpResponseData;
import lab.codereading.method.model.OrderCommand;
import lab.codereading.method.service.OrderService;
import lab.codereading.method.tx.TransactionProxy;

public class OrderController {

    private final TransactionProxy txProxy;
    private final OrderService orderService;

    public OrderController(TransactionProxy txProxy, OrderService orderService) {
        this.txProxy = txProxy;
        this.orderService = orderService;
    }

    public HttpResponseData handleCreateOrder(String traceId, OrderCommand cmd) {
        try {
            var saved = txProxy.executeInTransaction(() -> orderService.placeOrder(cmd));
            return new HttpResponseData(201, traceId, "CREATED:" + saved.orderId(), saved.total());
        } catch (IllegalArgumentException badRequest) {
            return new HttpResponseData(400, traceId, "BAD_REQUEST:" + badRequest.getMessage(), null);
        } catch (IllegalStateException conflict) {
            return new HttpResponseData(409, traceId, "CONFLICT:" + conflict.getMessage(), null);
        }
    }
}
