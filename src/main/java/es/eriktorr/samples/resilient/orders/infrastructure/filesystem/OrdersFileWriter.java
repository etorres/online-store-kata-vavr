package es.eriktorr.samples.resilient.orders.infrastructure.filesystem;

import es.eriktorr.samples.resilient.orders.domain.model.Order;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
import lombok.val;

import java.nio.file.Files;

import static io.github.resilience4j.retry.Retry.decorateCheckedSupplier;

public class OrdersFileWriter {

    public static final String ORDERS_FILE_WRITER = "ordersFileWriter";

    private final OrderPathCreator orderPathCreator;
    private final Retry retry;

    public OrdersFileWriter(OrderPathCreator orderPathCreator, RetryRegistry retryRegistry, RetryConfig retryConfig) {
        this.orderPathCreator = orderPathCreator;
        this.retry = retryRegistry.retry(ORDERS_FILE_WRITER, retryConfig);
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