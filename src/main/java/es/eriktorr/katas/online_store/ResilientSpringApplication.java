package es.eriktorr.katas.online_store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = { "es.eriktorr.katas.online_store" })
@EnableConfigurationProperties
public class ResilientSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResilientSpringApplication.class, args);
    }

}