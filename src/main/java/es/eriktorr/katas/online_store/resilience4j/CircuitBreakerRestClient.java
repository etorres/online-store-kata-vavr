package es.eriktorr.katas.online_store.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import org.springframework.web.client.RestTemplate;

public abstract class CircuitBreakerRestClient {

    protected final RestTemplate restTemplate;
    protected final CircuitBreaker circuitBreaker;

    protected CircuitBreakerRestClient(RestTemplate restTemplate,
                                       CircuitBreakerRegistry circuitBreakerRegistry,
                                       CircuitBreakerProperties circuitBreakerProperties,
                                       String name) {
        this.restTemplate = restTemplate;
        circuitBreaker = circuitBreakerRegistry.circuitBreaker(name, circuitBreakerProperties.createCircuitBreakerConfig(name));
    }

}