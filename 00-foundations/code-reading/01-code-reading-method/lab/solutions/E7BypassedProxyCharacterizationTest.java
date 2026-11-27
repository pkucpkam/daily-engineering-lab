package lab.codereading.method.solutions;

import lab.codereading.method.gateway.InventoryGateway;
import lab.codereading.method.model.OrderCommand;
import lab.codereading.method.repo.OrderRepository;
import lab.codereading.method.service.OrderService;
import lab.codereading.method.service.PricingPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reference solution for Exercise E7:
 * Demonstrates how bypassing TransactionProxy leaves a dirty committed order on failure.
 */
class E7BypassedProxyCharacterizationTest {

    @Test
    void callingOrderServiceWithoutTransactionProxyLeavesPartialWriteOnStockFailure() {
        var repo = new OrderRepository();
        var inventory = new InventoryGateway();
        var service = new OrderService(new PricingPolicy(), inventory, repo);

        assertThatThrownBy(() -> service.placeOrder(
                new OrderCommand("CUST-99", "REGULAR", "SKU-OUT", 1, new BigDecimal("100"), null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock");

        // Dirty write committed because TransactionProxy.beginTx() was never invoked!
        assertThat(repo.findAllCommitted()).hasSize(1);
    }
}
