package es.eriktorr.katas.online_store.orders.infrastructure.ws;

import es.eriktorr.katas.online_store.configuration.RestClientType;
import es.eriktorr.katas.online_store.resilience4j.CircuitBreakerRestClient;
import es.eriktorr.katas.online_store.orders.domain.model.Order;
import es.eriktorr.katas.online_store.orders.domain.model.OrderIdGenerator;
import es.eriktorr.katas.online_store.orders.domain.model.StoreId;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import lombok.val;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OrdersServiceClient extends CircuitBreakerRestClient {

    public static final String ORDERS_SERVICE_CLIENT = "ordersServiceClient";

    private final OrderIdGenerator orderIdGenerator;

    public OrdersServiceClient(@RestClientType(ORDERS_SERVICE_CLIENT) RestTemplate restTemplate,
                               OrderIdGenerator orderIdGenerator, CircuitBreakerRegistry circuitBreakerRegistry,
                               CircuitBreakerProperties circuitBreakerProperties) {
        super(restTemplate, circuitBreakerRegistry, circuitBreakerProperties, ORDERS_SERVICE_CLIENT);
        this.orderIdGenerator = orderIdGenerator;
    }

    public List<Order> ordersFrom(StoreId storeId) {
        val ordersSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, fetchOrders(storeId));
        return ordersSupplier.get();
    }

    private Supplier<List<Order>> fetchOrders(StoreId storeId) {
        return () -> {
            val response = restTemplate.exchange(
                    "/{storeId}/orders",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Order>>(){},
                    new HashMap<String, String>() {{
                        put("storeId", storeId.getValue());
                    }}
            );
            return ordersWithIdFrom(response);
        };
    }

    private List<Order> ordersWithIdFrom(ResponseEntity<List<Order>> response) {
        return Optional.ofNullable(response.getBody()).orElse(Collections.emptyList()).stream()
                .map(order -> Order.from(orderIdGenerator.nextOrderId(), order)).collect(Collectors.toList());
    }

}