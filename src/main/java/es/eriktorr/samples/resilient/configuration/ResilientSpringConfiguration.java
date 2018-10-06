package es.eriktorr.samples.resilient.configuration;

import es.eriktorr.samples.resilient.orders.domain.services.OrderProcessor;
import es.eriktorr.samples.resilient.orders.infrastructure.database.OrdersRepository;
import es.eriktorr.samples.resilient.orders.infrastructure.filesystem.OrdersFileWriter;
import es.eriktorr.samples.resilient.orders.infrastructure.filesystem.WriterType;
import es.eriktorr.samples.resilient.orders.infrastructure.ws.ClientType;
import es.eriktorr.samples.resilient.orders.infrastructure.ws.OrdersServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static es.eriktorr.samples.resilient.orders.infrastructure.filesystem.OrdersFileWriter.ORDERS_FILE_WRITER;
import static es.eriktorr.samples.resilient.orders.infrastructure.ws.OrdersServiceClient.ORDERS_SERVICE_CLIENT;

@Configuration
public class ResilientSpringConfiguration {

    @Value("${orders.service.url}")
    private String ordersServiceUrl;

    @Value("${orders.storage.path}")
    private String ordersStoragePath;

    @Value("${orders.storage.retry.maxAttempts}")
    private int ordersMaxAttempts;

    @Value("${orders.storage.retry.intervalInMillis}")
    private long ordersIntervalInMillis;

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
    public RetryRegistry retryRegistry() {
        return RetryRegistry.ofDefaults();
    }

    @Bean @WriterType(ORDERS_FILE_WRITER)
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
                .maxAttempts(ordersMaxAttempts)
                .intervalFunction(IntervalFunction.of(Duration.ofMillis(ordersIntervalInMillis)))
                .build();
    }

    @Bean
    public OrdersFileWriter ordersFileWriter(RetryRegistry retryRegistry, @WriterType(ORDERS_FILE_WRITER) RetryConfig retryConfig) {
        return new OrdersFileWriter(ordersStoragePath, retryRegistry, retryConfig);
    }

    @Bean
    public OrderProcessor orderProcessor(OrdersServiceClient ordersServiceClient, OrdersFileWriter ordersFileWriter,
                                         OrdersRepository ordersRepository) {
        return new OrderProcessor(ordersServiceClient, ordersFileWriter, ordersRepository);
    }

}