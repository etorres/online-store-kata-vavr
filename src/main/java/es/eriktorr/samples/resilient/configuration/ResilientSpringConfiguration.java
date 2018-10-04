package es.eriktorr.samples.resilient.configuration;

import es.eriktorr.samples.resilient.orders.infrastructure.database.OrdersRepository;
import es.eriktorr.samples.resilient.orders.infrastructure.filesystem.OrdersFileWriter;
import es.eriktorr.samples.resilient.orders.infrastructure.ws.ClientType;
import es.eriktorr.samples.resilient.orders.infrastructure.ws.OrdersServiceClient;
import es.eriktorr.samples.resilient.orders.domain.services.OrderProcessor;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static es.eriktorr.samples.resilient.orders.infrastructure.ws.OrdersServiceClient.ORDERS_SERVICE_CLIENT;

@Configuration
public class ResilientSpringConfiguration {

    @Value("${orders.service.url}")
    private String ordersServiceUrl;

    @Value("${orders.storage.path}")
    private String ordersStoragePath;

    @Bean @ClientType(ORDERS_SERVICE_CLIENT)
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(ordersServiceUrl)
                .build();
    }

    @Bean
    public OrdersServiceClient ordersServiceClient(RestTemplate restTemplate, CircuitBreakerRegistry circuitBreakerRegistry,
                                                   CircuitBreakerProperties circuitBreakerProperties) {
        return new OrdersServiceClient(restTemplate, circuitBreakerRegistry, circuitBreakerProperties);
    }

    @Bean
    public OrdersFileWriter ordersFileWriter() {
        return new OrdersFileWriter(ordersStoragePath);
    }

    @Bean
    public OrderProcessor orderProcessor(OrdersServiceClient ordersServiceClient, OrdersFileWriter ordersFileWriter,
                                         OrdersRepository ordersRepository) {
        return new OrderProcessor(ordersServiceClient, ordersFileWriter, ordersRepository);
    }

}