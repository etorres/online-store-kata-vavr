package es.eriktorr.samples.resilient.orders.infrastructure.filesystem;

import es.eriktorr.samples.resilient.core.resilience4j.RetryClient;
import es.eriktorr.samples.resilient.core.resilience4j.RetryProperties;
import es.eriktorr.samples.resilient.orders.domain.model.Order;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
import lombok.val;

import java.nio.file.Files;

import static io.github.resilience4j.retry.Retry.decorateCheckedSupplier;

public class OrdersFileWriter extends RetryClient {

    private static final String ORDERS_FILE_WRITER = "ordersFileWriter";

    private final OrderPathCreator orderPathCreator;

    public OrdersFileWriter(OrderPathCreator orderPathCreator, RetryRegistry retryRegistry, RetryProperties retryProperties) {
        super(retryRegistry, retryProperties, ORDERS_FILE_WRITER);
        this.orderPathCreator = orderPathCreator;
    }

    public void writeToFile(Order order) {
        val ordersSupplier = decorateCheckedSupplier(retry, writeOrderToFile(order));
        ordersSupplier.unchecked().apply();
    }

    private CheckedFunction0<Order> writeOrderToFile(Order order) {
        return () -> {
            val path = orderPathCreator.pathFrom(order);
            val bytes = order.toString().getBytes();
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
            return order;
        };
    }

}