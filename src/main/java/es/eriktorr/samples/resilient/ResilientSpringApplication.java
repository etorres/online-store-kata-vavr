package es.eriktorr.samples.resilient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "es.eriktorr.samples.resilient" })
public class ResilientSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResilientSpringApplication.class, args);
    }

}