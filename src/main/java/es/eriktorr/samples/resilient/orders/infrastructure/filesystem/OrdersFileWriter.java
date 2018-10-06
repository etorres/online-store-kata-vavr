package es.eriktorr.samples.resilient.orders.infrastructure.filesystem;

import es.eriktorr.samples.resilient.orders.domain.model.Order;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Paths;

import static io.github.resilience4j.retry.Retry.decorateCheckedSupplier;

public class OrdersFileWriter {

    public static final String ORDERS_FILE_WRITER = "ordersFileWriter";

    public final String ordersStoragePath;
    private final Retry retry;

    public OrdersFileWriter(String ordersStoragePath, RetryRegistry retryRegistry, RetryConfig retryConfig) {
        this.ordersStoragePath = ordersStoragePath;
        this.retry = retryRegistry.retry(ORDERS_FILE_WRITER, retryConfig);
    }

    public void writeToFile(Order order) {
        val ordersSupplier = decorateCheckedSupplier(retry, writeOrderToFile(order));
        ordersSupplier.unchecked().apply();
    }

    private CheckedFunction0<Order> writeOrderToFile(Order order) {
        return () -> {
            val path = Paths.get(ordersStoragePath, order.getOrderId().getValue());
            val bytes = order.toString().getBytes();
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
            return order;
        };
    }

}