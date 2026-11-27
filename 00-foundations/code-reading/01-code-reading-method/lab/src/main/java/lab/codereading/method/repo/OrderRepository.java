package lab.codereading.method.repo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulates a transactional JDBC repository with staged writes that only commit
 * when the surrounding TransactionContext succeeds.
 */
public class OrderRepository {

    public record SavedOrder(String orderId, String customerId, String sku, int quantity, BigDecimal total) {
    }

    private final Map<String, SavedOrder> committed = new ConcurrentHashMap<>();
    private final ThreadLocal<List<SavedOrder>> txBuffer = new ThreadLocal<>();
    private final AtomicInteger sequence = new AtomicInteger(100);

    public void beginTx() {
        txBuffer.set(new ArrayList<>());
    }

    public SavedOrder save(String customerId, String sku, int quantity, BigDecimal total) {
        var order = new SavedOrder("ORD-" + sequence.incrementAndGet(), customerId, sku, quantity, total);
        List<SavedOrder> staged = txBuffer.get();
        if (staged != null) {
            staged.add(order);
        } else {
            // Auto-commit mode when called outside an active transaction boundary
            committed.put(order.orderId(), order);
        }
        return order;
    }

    public void commitTx() {
        List<SavedOrder> staged = txBuffer.get();
        if (staged != null) {
            for (var order : staged) {
                committed.put(order.orderId(), order);
            }
            txBuffer.remove();
        }
    }

    public void rollbackTx() {
        txBuffer.remove();
    }

    public List<SavedOrder> findAllCommitted() {
        return Collections.unmodifiableList(new ArrayList<>(committed.values()));
    }
}
