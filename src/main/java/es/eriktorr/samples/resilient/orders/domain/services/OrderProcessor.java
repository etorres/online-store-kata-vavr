package es.eriktorr.samples.resilient.orders.domain.services;

import es.eriktorr.samples.resilient.orders.domain.model.Order;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import es.eriktorr.samples.resilient.orders.infrastructure.database.OrdersRepository;
import es.eriktorr.samples.resilient.orders.infrastructure.filesystem.OrdersFileWriter;
import es.eriktorr.samples.resilient.orders.infrastructure.ws.OrdersServiceClient;
import io.vavr.Function1;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collections;
import java.util.List;
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

    public OrderProcessor(OrdersServiceClient ordersServiceClient, OrdersFileWriter ordersFileWriter,
                          OrdersRepository ordersRepository) {
        this.ordersServiceClient = ordersServiceClient;
        this.ordersFileWriter = ordersFileWriter;
        this.ordersRepository = ordersRepository;
    }

    public void processOrdersFrom(StoreId storeId) {
        val stats = fetchOrdersFrom(storeId).stream()
                .map(functions.saveAndThenInsertAndThenLogAnOrder)
                .collect(Stats::new, Stats::add, Stats::combine);
        log.info(stats.toString());
    }

    private List<Try<Order>> fetchOrdersFrom(StoreId storeId) {
        return Try.ofSupplier(() -> ordersServiceClient.ordersFrom(storeId))
                .transform(functions.toTrySequence);
    }

    private class Functions {

        private Function1<Try<List<Order>>, List<Try<Order>>> toTrySequence = orders -> orders.isSuccess()
                ? orders.get().stream().map(Try::success).collect(Collectors.toList())
                : Collections.singletonList(Try.failure(orders.getCause()));

        private Function1<Try<Order>, Try<Order>> saveOrderToFileSystem =
                order -> order.andThen(ordersFileWriter::writeToFile);

        private Function1<Try<Order>, Try<Order>> insertOrderIntoDatabase =
                order -> order.andThen(() -> ordersRepository.save(order.get()));

        private Function1<Try<Order>, Try<Order>> writeMessageToLog = order -> Match(order).of(
                Case($Success($()), () -> {
                    log.info(String.format("Order created: %s", order.get()));
                    return order;
                }),
                Case($Failure($()), () -> {
                    log.error("Failed to create order", order.getCause());
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