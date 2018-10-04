package es.eriktorr.samples.resilient.orders.domain.services;

import es.eriktorr.samples.resilient.orders.domain.model.Order;
import es.eriktorr.samples.resilient.orders.domain.model.Orders;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import es.eriktorr.samples.resilient.orders.infrastructure.database.OrdersRepository;
import es.eriktorr.samples.resilient.orders.infrastructure.ws.OrdersServiceClient;
import io.vavr.Function1;
import io.vavr.control.Try;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OrderProcessor {

    private final OrdersServiceClient ordersServiceClient;
    private final OrdersRepository ordersRepository;
    private final Functions functions = new Functions();

    public OrderProcessor(OrdersServiceClient ordersServiceClient, OrdersRepository ordersRepository) {
        this.ordersServiceClient = ordersServiceClient;
        this.ordersRepository = ordersRepository;
    }

    public void processOrdersFrom(StoreId storeId) {
        fetchOrdersFrom(storeId)
                .forEach(functions.processOrder);
    }

    private List<Try<Order>> fetchOrdersFrom(StoreId storeId) {
        return ordersServiceClient.ordersFrom(storeId)
                .transform(functions.toOrdersSequence);
    }

    private class Functions {

        private Function1<Try<Orders>, List<Try<Order>>> toOrdersSequence = orders -> orders.isSuccess()
                ? orders.get().getOrders().stream().map(Try::success).collect(Collectors.toList())
                : Collections.singletonList(Try.failure(orders.getCause()));

        private Function1<Try<Order>, Try<Order>> saveOrderToFileSystem = order -> {
            return order;
        };

        private Function1<Try<Order>, Try<Order>> insertOrderIntoDatabase = order -> order.isSuccess()
                ? ordersRepository.save(order.get())
                : order;

        private Function1<Try<Order>, Try<Order>> writeMessageToLog = order -> {
            return order;
        };

        private Function1<Try<Order>, Try<Order>> saveAndInsertAndLogOrder = saveOrderToFileSystem
                .andThen(insertOrderIntoDatabase)
                .andThen(writeMessageToLog);

        private Consumer<Try<Order>> processOrder = order -> saveAndInsertAndLogOrder.apply(order);

    }

}