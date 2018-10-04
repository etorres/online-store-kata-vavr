package es.eriktorr.samples.resilient.orders.domain.services;

import es.eriktorr.samples.resilient.infrastructure.ws.OrdersServiceClient;
import es.eriktorr.samples.resilient.orders.domain.model.Order;
import es.eriktorr.samples.resilient.orders.domain.model.Orders;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import io.vavr.Function1;
import io.vavr.control.Try;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrderProcessor {

    private final OrdersServiceClient ordersServiceClient;

    public OrderProcessor(OrdersServiceClient ordersServiceClient) {
        this.ordersServiceClient = ordersServiceClient;
    }

    public void processOrdersFrom(StoreId storeId) {
        fetchOrdersFrom(storeId)
                .forEach(this::processOrder);
    }

    private List<Try<Order>> fetchOrdersFrom(StoreId storeId) {
        return ordersServiceClient.ordersFrom(storeId)
                .transform(toOrdersSequence);
    }

    private Function1<Try<Orders>, List<Try<Order>>> toOrdersSequence = orders -> orders.isSuccess()
            ? orders.get().getOrders().stream().map(Try::success).collect(Collectors.toList())
            : Collections.singletonList(Try.failure(orders.getCause()));

    private void processOrder(Try<Order> order) {
        processOrder.apply(order);
    }

    private Function1<Try<Order>, Try<Order>> saveOrderToFileSystem = order -> {
        return order;
    };

    private Function1<Try<Order>, Try<Order>> insertOrderIntoDatabase = order -> {
        return order;
    };

    private Function1<Try<Order>, Try<Order>> writeMessageToLog = order -> {
        return order;
    };

    private Function1<Try<Order>, Try<Order>> processOrder = saveOrderToFileSystem
            .andThen(insertOrderIntoDatabase)
            .andThen(writeMessageToLog);

}