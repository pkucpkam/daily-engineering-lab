package lab.codereading.method;

import lab.codereading.method.gateway.InventoryGateway;
import lab.codereading.method.model.OrderCommand;
import lab.codereading.method.repo.OrderRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Start reading here! Tests document exact system contracts before reading implementation classes.
 */
class OrderFlowTest {

    @Test
    void vipOrderWithCouponCommitsAndReducesInventory() {
        var repo = new OrderRepository();
        var inventory = new InventoryGateway();
        var dispatcher = OrderApplication.bootstrap(repo, inventory);

        var res = dispatcher.dispatch(
                "/api/orders",
                Map.of("X-Api-Key", "secret-dev-token", "X-Trace-Id", "t-1"),
                new OrderCommand("CUST-1", "VIP", "SKU-BOOK", 2, new BigDecimal("200"), "SAVE50"));

        assertThat(res.status()).isEqualTo(201);
        // 2 * 200 = 400 -> VIP 10% off = 360 -> SAVE50 = 310.00
        assertThat(res.finalAmount()).isEqualByComparingTo("310.00");
        assertThat(repo.findAllCommitted()).hasSize(1);
        assertThat(inventory.available("SKU-BOOK")).isEqualTo(8);
    }

    @Test
    void insufficientStockRollsBackStagedOrder() {
        var repo = new OrderRepository();
        var inventory = new InventoryGateway();
        var dispatcher = OrderApplication.bootstrap(repo, inventory);

        var res = dispatcher.dispatch(
                "/api/orders",
                Map.of("X-Api-Key", "secret-dev-token", "X-Trace-Id", "t-2"),
                new OrderCommand("CUST-2", "REGULAR", "SKU-OUT", 1, new BigDecimal("100"), null));

        assertThat(res.status()).isEqualTo(409);
        assertThat(res.body()).contains("Insufficient stock");
        assertThat(repo.findAllCommitted()).isEmpty();
    }

    @Test
    void missingApiKeyReturns401BeforeTouchingServiceOrTransaction() {
        var repo = new OrderRepository();
        var inventory = new InventoryGateway();
        var dispatcher = OrderApplication.bootstrap(repo, inventory);

        var res = dispatcher.dispatch(
                "/api/orders",
                Map.of("X-Trace-Id", "t-3"),
                new OrderCommand("CUST-3", "VIP", "SKU-BOOK", 1, new BigDecimal("100"), null));

        assertThat(res.status()).isEqualTo(401);
        assertThat(repo.findAllCommitted()).isEmpty();
        assertThat(inventory.available("SKU-BOOK")).isEqualTo(10);
    }
}
