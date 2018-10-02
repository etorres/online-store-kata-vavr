package es.eriktorr.samples.resilient.configuration;

import es.eriktorr.samples.resilient.infrastructure.ws.OrdersServiceClient;
import es.eriktorr.samples.resilient.orders.domain.services.OrderProcessor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ResilientSpringConfiguration {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    public OrdersServiceClient ordersServiceClient(RestTemplate restTemplate) {
        return new OrdersServiceClient(restTemplate);
    }

    @Bean
    public OrderProcessor orderProcessor(OrdersServiceClient ordersServiceClient) {
        return new OrderProcessor(ordersServiceClient);
    }

}