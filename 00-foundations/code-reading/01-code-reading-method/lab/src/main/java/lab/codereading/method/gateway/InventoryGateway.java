package lab.codereading.method.gateway;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryGateway {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    public InventoryGateway() {
        stock.put("SKU-BOOK", 10);
        stock.put("SKU-LAPTOP", 2);
        stock.put("SKU-OUT", 0);
    }

    public void reserve(String sku, int quantity) {
        int available = stock.getOrDefault(sku, 0);
        if (available < quantity) {
            throw new IllegalStateException("Insufficient stock for " + sku + ": available=" + available);
        }
        stock.put(sku, available - quantity);
    }

    public int available(String sku) {
        return stock.getOrDefault(sku, 0);
    }
}
