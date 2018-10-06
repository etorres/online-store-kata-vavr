package es.eriktorr.samples.resilient.orders.infrastructure.ws;

import es.eriktorr.samples.resilient.orders.domain.model.Order;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import io.vavr.control.Try;
import lombok.val;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class OrdersServiceClient extends CircuitBreakerClient {

    public static final String ORDERS_SERVICE_CLIENT = "ordersServiceClient";

    public OrdersServiceClient(@ClientType(ORDERS_SERVICE_CLIENT) RestTemplate restTemplate,
                               CircuitBreakerRegistry circuitBreakerRegistry,
                               CircuitBreakerProperties circuitBreakerProperties) {
        super(restTemplate, circuitBreakerRegistry, circuitBreakerProperties, ORDERS_SERVICE_CLIENT);
    }

    public Try<List<Order>> ordersFrom(StoreId storeId) {
        val ordersSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, fetchOrders(storeId));
        return Try.ofSupplier(ordersSupplier);
    }

    private Supplier<List<Order>> fetchOrders(StoreId storeId) {
        return () -> {
            val response = restTemplate.exchange(
                    "/{storeId}/orders",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Order>>(){},
                    new HashMap<String, String>() {{
                        put("storeId", storeId.value());
                    }}
            );
            return response.getBody();
        };
    }

}