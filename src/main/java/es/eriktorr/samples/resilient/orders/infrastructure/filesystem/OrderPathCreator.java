package es.eriktorr.samples.resilient.orders.infrastructure.filesystem;

import es.eriktorr.samples.resilient.orders.domain.model.Order;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OrderPathCreator {

    public final String ordersStoragePath;

    public OrderPathCreator(String ordersStoragePath) {
        this.ordersStoragePath = ordersStoragePath;
    }

    public Path pathFrom(Order order) {
        return Paths.get(ordersStoragePath, order.getOrderReference().getValue() + "_" + order.getStoreId().getValue());
    }

}