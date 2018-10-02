package es.eriktorr.samples.resilient.infrastructure.ws;

import es.eriktorr.samples.resilient.orders.domain.model.Orders;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import org.springframework.web.client.RestTemplate;

public class OrdersServiceClient {

    private final RestTemplate restTemplate;

    public OrdersServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Orders ordersFrom(StoreId storeId) {
        return restTemplate.getForObject("/{storeId}/orders", Orders.class, storeId.value());
    }

}