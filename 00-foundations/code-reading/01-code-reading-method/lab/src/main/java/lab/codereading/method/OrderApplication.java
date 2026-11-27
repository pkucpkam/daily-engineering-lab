package lab.codereading.method;

import lab.codereading.method.gateway.InventoryGateway;
import lab.codereading.method.model.OrderCommand;
import lab.codereading.method.repo.OrderRepository;
import lab.codereading.method.service.OrderService;
import lab.codereading.method.service.PricingPolicy;
import lab.codereading.method.tx.TransactionProxy;
import lab.codereading.method.web.OrderController;
import lab.codereading.method.web.RequestDispatcher;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Application bootstrap & runnable call-flow walkthrough for READ-01.
 */
public class OrderApplication {

    public static RequestDispatcher bootstrap(OrderRepository repo, InventoryGateway inventory) {
        var pricing = new PricingPolicy();
        var service = new OrderService(pricing, inventory, repo);
        var txProxy = new TransactionProxy(repo);
        var controller = new OrderController(txProxy, service);
        return new RequestDispatcher(controller);
    }

    public static void main(String[] args) {
        var repo = new OrderRepository();
        var inventory = new InventoryGateway();
        var dispatcher = bootstrap(repo, inventory);

        var ok = dispatcher.dispatch(
                "/api/orders",
                Map.of("X-Api-Key", "secret-dev-token", "X-Trace-Id", "tr-001"),
                new OrderCommand("CUST-1", "VIP", "SKU-BOOK", 2, new BigDecimal("200"), "SAVE50"));
        System.out.println("[1] Happy path -> " + ok);

        var outOfStock = dispatcher.dispatch(
                "/api/orders",
                Map.of("X-Api-Key", "secret-dev-token", "X-Trace-Id", "tr-002"),
                new OrderCommand("CUST-2", "REGULAR", "SKU-OUT", 1, new BigDecimal("100"), null));
        System.out.println("[2] Out of stock -> " + outOfStock);
        System.out.println("[3] Committed orders in DB -> " + repo.findAllCommitted());
    }
}
