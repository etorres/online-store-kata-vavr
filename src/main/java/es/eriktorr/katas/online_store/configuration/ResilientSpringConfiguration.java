package es.eriktorr.katas.online_store.configuration;

import es.eriktorr.katas.online_store.resilience4j.RetryProperties;
import es.eriktorr.katas.online_store.orders.domain.model.OrderIdGenerator;
import es.eriktorr.katas.online_store.orders.domain.services.OrderProcessor;
import es.eriktorr.katas.online_store.orders.infrastructure.database.OrdersRepository;
import es.eriktorr.katas.online_store.orders.infrastructure.filesystem.OrderPathCreator;
import es.eriktorr.katas.online_store.orders.infrastructure.filesystem.OrdersFileWriter;
import es.eriktorr.katas.online_store.orders.infrastructure.ws.OrdersServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static es.eriktorr.katas.online_store.orders.infrastructure.ws.OrdersServiceClient.ORDERS_SERVICE_CLIENT;

@Configuration
public class ResilientSpringConfiguration {

    @Value("${orders.service.url}")
    private String ordersServiceUrl;

    @Value("${orders.storage.path}")
    private String ordersStoragePath;

    private final RetryProperties retryProperties;

    public ResilientSpringConfiguration(RetryProperties retryProperties) {
        this.retryProperties = retryProperties;
    }

    @Bean
    public OrderIdGenerator orderIdGenerator() {
        return new OrderIdGenerator();
    }

    @Bean @RestClientType(ORDERS_SERVICE_CLIENT)
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(ordersServiceUrl)
                .build();
    }

    @Bean
    public OrdersServiceClient ordersServiceClient(@RestClientType(ORDERS_SERVICE_CLIENT) RestTemplate restTemplate,
                                                   OrderIdGenerator orderIdGenerator,
                                                   CircuitBreakerRegistry circuitBreakerRegistry,
                                                   CircuitBreakerProperties circuitBreakerProperties) {
        return new OrdersServiceClient(restTemplate, orderIdGenerator, circuitBreakerRegistry, circuitBreakerProperties);
    }

    @Bean
    public RetryRegistry retryRegistry() {
        return RetryRegistry.ofDefaults();
    }

    @Bean
    public OrderPathCreator orderPathCreator() {
        return new OrderPathCreator(ordersStoragePath);
    }

    @Bean
    public OrdersFileWriter ordersFileWriter(OrderPathCreator orderPathCreator, RetryRegistry retryRegistry) {
        return new OrdersFileWriter(orderPathCreator, retryRegistry, retryProperties);
    }

    @Bean
    public OrderProcessor orderProcessor(OrdersServiceClient ordersServiceClient, OrdersFileWriter ordersFileWriter,
                                         OrdersRepository ordersRepository) {
        return new OrderProcessor(ordersServiceClient, ordersFileWriter, ordersRepository);
    }

}