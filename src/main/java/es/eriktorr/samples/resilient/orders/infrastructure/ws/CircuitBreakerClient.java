package es.eriktorr.samples.resilient.orders.infrastructure.ws;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import org.springframework.web.client.RestTemplate;

public abstract class CircuitBreakerClient {

    protected final RestTemplate restTemplate;
    protected final CircuitBreaker circuitBreaker;

    protected CircuitBreakerClient(RestTemplate restTemplate,
                                   CircuitBreakerRegistry circuitBreakerRegistry,
                                   CircuitBreakerProperties circuitBreakerProperties,
                                   String name) {
        this.restTemplate = restTemplate;
        circuitBreaker = circuitBreakerRegistry.circuitBreaker(name, () -> circuitBreakerProperties.createCircuitBreakerConfig(name));
    }

}