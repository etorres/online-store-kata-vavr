package es.eriktorr.samples.resilient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = { "es.eriktorr.samples.resilient" })
@EnableConfigurationProperties
public class ResilientSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResilientSpringApplication.class, args);
    }

}