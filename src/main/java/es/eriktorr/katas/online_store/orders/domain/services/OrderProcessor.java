package es.eriktorr.katas.online_store.orders.domain.services;

import es.eriktorr.katas.online_store.orders.domain.model.Order;
import es.eriktorr.katas.online_store.orders.domain.model.OrderReference;
import es.eriktorr.katas.online_store.orders.domain.model.StoreId;
import es.eriktorr.katas.online_store.orders.infrastructure.database.OrdersRepository;
import es.eriktorr.katas.online_store.orders.infrastructure.filesystem.OrdersFileWriter;
import es.eriktorr.katas.online_store.orders.infrastructure.ws.OrdersServiceClient;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.vavr.API.*;
import static io.vavr.Patterns.$Failure;
import static io.vavr.Patterns.$Success;

@Slf4j
public class OrderProcessor {

    private final OrdersServiceClient ordersServiceClient;
    private final OrdersFileWriter ordersFileWriter;
    private final OrdersRepository ordersRepository;
    private final Functions functions = new Functions();

    public OrderProcessor(OrdersServiceClient ordersServiceClient, OrdersFileWriter ordersFileWriter, OrdersRepository ordersRepository) {
        this.ordersServiceClient = ordersServiceClient;
        this.ordersFileWriter = ordersFileWriter;
        this.ordersRepository = ordersRepository;
    }

    public void processOrdersFrom(StoreId storeId) {
        val orders = fetchOrdersFrom(storeId)
                .map(functions.removeDuplicate)
                .transform(functions.toTrySequence);
        val stats = orders.stream()
                .map(functions.saveAndThenInsertAndThenLogAnOrder)
                .collect(Stats::new, Stats::add, Stats::combine);
        log.info(stats.toString());
    }

    private Try<List<Order>> fetchOrdersFrom(StoreId storeId) {
        return Try.ofSupplier(() -> ordersServiceClient.ordersFrom(storeId));
    }

    private class Functions {

        private Function<List<Order>, List<Order>> removeDuplicate = orders -> {
            val duplicateOrders = ordersRepository.findDuplicate(orders);
            return orders.stream()
                    .filter(isDuplicate(duplicateOrders).negate())
                    .collect(Collectors.toList());
        };

        private Predicate<Order> isDuplicate(List<Tuple2<StoreId, OrderReference>> duplicateOrders) {
            return order -> duplicateOrders.contains(Tuple.of(order.getStoreId(), order.getOrderReference()));
        }

        private Function1<Try<List<Order>>, List<Try<Order>>> toTrySequence = orders -> Match(orders).of(
                Case($Success($()), values ->
                        values.stream().map(Try::success).collect(Collectors.toList())
                ),
                Case($Failure($()), error ->
                        Collections.singletonList(Try.failure(error))
                ));

        private Function1<Try<Order>, Try<Order>> saveOrderToFileSystem =
                order -> order.andThen(ordersFileWriter::writeToFile);

        private Function1<Try<Order>, Try<Order>> insertOrderIntoDatabase =
                order -> order.andThen(ordersRepository::save);

        private Function1<Try<Order>, Try<Order>> writeMessageToLog = order -> Match(order).of(
                Case($Success($()), value -> {
                    log.info(String.format("Order created: %s", value));
                    return order;
                }),
                Case($Failure($()), error -> {
                    log.error("Failed to create order", error);
                    return order;
                }));

        private Function1<Try<Order>, Try<Order>> saveAndThenInsertAndThenLogAnOrder = saveOrderToFileSystem
                .andThen(insertOrderIntoDatabase)
                .andThen(writeMessageToLog);

    }

    private class Stats {

        private int possible = 0;
        private int done = 0;

        private void add(Try<Order> order) {
            Match(order).of(
                    Case($Success($()), () -> {
                        done++;
                        possible++;
                        return order;
                    }),
                    Case($Failure($()), () -> order));
        }

        private void combine(Stats other) {
            possible += other.possible;
            done += other.done;
        }

        @Override
        public String toString() {
            return String.format("%d orders processed of %d possible", done, possible);
        }

    }

}