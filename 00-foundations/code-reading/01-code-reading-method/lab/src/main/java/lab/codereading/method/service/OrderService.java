package lab.codereading.method.service;

import lab.codereading.method.gateway.InventoryGateway;
import lab.codereading.method.model.OrderCommand;
import lab.codereading.method.repo.OrderRepository;
import lab.codereading.method.repo.OrderRepository.SavedOrder;

import java.math.BigDecimal;

public class OrderService {

    private final PricingPolicy pricingPolicy;
    private final InventoryGateway inventoryGateway;
    private final OrderRepository orderRepository;

    public OrderService(
            PricingPolicy pricingPolicy,
            InventoryGateway inventoryGateway,
            OrderRepository orderRepository) {
        this.pricingPolicy = pricingPolicy;
        this.inventoryGateway = inventoryGateway;
        this.orderRepository = orderRepository;
    }

    public SavedOrder placeOrder(OrderCommand cmd) {
        if (cmd.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        BigDecimal total = pricingPolicy.calculateTotal(
                cmd.customerTier(), cmd.unitPrice(), cmd.quantity(), cmd.couponCode());

        // DB write happens BEFORE inventory reservation so TransactionProxy rollback is observable on failure!
        SavedOrder saved = orderRepository.save(cmd.customerId(), cmd.sku(), cmd.quantity(), total);
        inventoryGateway.reserve(cmd.sku(), cmd.quantity());
        return saved;
    }
}
