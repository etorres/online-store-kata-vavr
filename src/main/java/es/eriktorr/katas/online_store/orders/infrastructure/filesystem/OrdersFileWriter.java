package es.eriktorr.katas.online_store.orders.infrastructure.filesystem;

import es.eriktorr.katas.online_store.resilience4j.RetryClient;
import es.eriktorr.katas.online_store.resilience4j.RetryProperties;
import es.eriktorr.katas.online_store.orders.domain.model.Order;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
import lombok.val;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
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
            Files.createDirectories(path.getParent());
            try (val file = new RandomAccessFile(path.toFile(), "rw"); val fileChannel = file.getChannel();
                 val fileLock = fileChannel.tryLock()) {
                if (fileLock == null) throw new IOException("failed to acquire an exclusive lock on this file: " + file);
                val bytes = order.toString().getBytes();
                val buffer = ByteBuffer.allocate(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                fileChannel.write(buffer);
            }
            return order;
        };
    }

}