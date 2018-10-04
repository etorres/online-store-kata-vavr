package es.eriktorr.samples.resilient.orders.infrastructure.filesystem;

import es.eriktorr.samples.resilient.orders.domain.model.Order;
import io.vavr.control.Try;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Paths;

public class OrdersFileWriter {

    private final String ordersStoragePath;

    public OrdersFileWriter(String ordersStoragePath) {
        this.ordersStoragePath = ordersStoragePath;
    }

    public Try<Order> writeToFile(Order order) {
        return Try.of(() -> {
            val path = Paths.get(ordersStoragePath, order.getOrderId().getValue());
            val bytes = order.toString().getBytes();
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
            return order;
        });
    }

}