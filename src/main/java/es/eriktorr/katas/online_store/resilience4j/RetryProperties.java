package es.eriktorr.katas.online_store.resilience4j;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "resilience4j.retry")
@Validated
@ToString
public class RetryProperties {

    @Getter @Setter
    private List<RetryBackend> backends = new ArrayList<>();

    @Validated
    @ToString
    static class RetryBackend {
        @NotBlank
        @Getter @Setter
        private String name;

        @Min(1) @Max(10)
        @Getter @Setter
        private int maxAttempts;

        @Min(0L) @Max(60000L)
        @Getter @Setter
        private long intervalInMillis;
    }

}