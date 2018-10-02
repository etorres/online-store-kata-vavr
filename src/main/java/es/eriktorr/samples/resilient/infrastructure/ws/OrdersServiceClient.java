package es.eriktorr.samples.resilient.infrastructure.ws;

import es.eriktorr.samples.resilient.orders.domain.model.Orders;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import io.vavr.control.Try;
import lombok.val;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

public class OrdersServiceClient extends CircuitBreakerClient {

    public static final String ORDERS_SERVICE_CLIENT = "ordersServiceClient";

    public OrdersServiceClient(@ClientType(ORDERS_SERVICE_CLIENT) RestTemplate restTemplate,
                               CircuitBreakerRegistry circuitBreakerRegistry,
                               CircuitBreakerProperties circuitBreakerProperties) {
        super(restTemplate, circuitBreakerRegistry, circuitBreakerProperties, ORDERS_SERVICE_CLIENT);
    }

    public Try<Orders> ordersFrom(StoreId storeId) {
        val ordersSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, fetchOrders(storeId));
        return Try.ofSupplier(ordersSupplier);
    }

    private Supplier<Orders> fetchOrders(StoreId storeId) {
        return () -> restTemplate.getForObject("/{storeId}/orders", Orders.class, storeId.value());
    }

}