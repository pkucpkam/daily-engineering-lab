package lab.codereading.method.tx;

import lab.codereading.method.repo.OrderRepository;

import java.util.function.Supplier;

/**
 * Simulates a Spring-like @Transactional proxy wrapping service calls.
 */
public class TransactionProxy {

    private final OrderRepository repository;

    public TransactionProxy(OrderRepository repository) {
        this.repository = repository;
    }

    public <T> T executeInTransaction(Supplier<T> action) {
        repository.beginTx();
        try {
            T result = action.get();
            repository.commitTx();
            return result;
        } catch (RuntimeException ex) {
            repository.rollbackTx();
            throw ex;
        }
    }
}
