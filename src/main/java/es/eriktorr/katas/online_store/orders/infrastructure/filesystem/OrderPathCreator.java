package es.eriktorr.katas.online_store.orders.infrastructure.filesystem;

import es.eriktorr.katas.online_store.orders.domain.model.Order;

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