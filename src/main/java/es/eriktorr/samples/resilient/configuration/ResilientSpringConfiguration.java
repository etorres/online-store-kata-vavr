package es.eriktorr.samples.resilient.configuration;

import es.eriktorr.samples.resilient.orders.domain.services.OrderProcessor;
import es.eriktorr.samples.resilient.infrastructure.ws.OrdersServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ResilientSpringConfiguration {

    @Bean
    public OrdersServiceClient ordersServiceClient(RestTemplate restTemplate) {
        return new OrdersServiceClient(restTemplate);
    }

    @Bean
    public OrderProcessor orderProcessor() {
        return new OrderProcessor();
    }

}