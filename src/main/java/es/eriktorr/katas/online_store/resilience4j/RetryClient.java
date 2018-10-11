package es.eriktorr.katas.online_store.resilience4j;

import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.val;

import java.time.Duration;

public abstract class RetryClient {

    protected final Retry retry;

    protected RetryClient(RetryRegistry retryRegistry, RetryProperties retryProperties, String name) {
        val optionalRetryBackend = retryProperties.getBackends().stream()
                .filter(retryBackend -> name.equals(retryBackend.getName()))
                .findFirst();
        val retryBackend = optionalRetryBackend
                .orElseThrow(() -> new IllegalStateException("failed to create retry client with name: " + name));
        val retryConfig = RetryConfig.custom()
                .maxAttempts(retryBackend.getMaxAttempts())
                .intervalFunction(IntervalFunction.of(Duration.ofMillis(retryBackend.getIntervalInMillis())))
                .build();
        this.retry = retryRegistry.retry(name, retryConfig);
    }

}