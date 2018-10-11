package es.eriktorr.katas.online_store.orders.domain.model;

import lombok.Value;

@Value
public class Order {

    private final OrderId orderId;
    private final StoreId storeId;
    private final OrderReference orderReference;
    private final String description;

    public static final Order INVALID = new Order(
            new OrderId("-"), new StoreId("-"), new OrderReference("-"), null
    );

    public static Order from(OrderId orderId, Order other) {
        return new Order(
                orderId,
                other.storeId,
                other.orderReference,
                other.description
        );
    }

}