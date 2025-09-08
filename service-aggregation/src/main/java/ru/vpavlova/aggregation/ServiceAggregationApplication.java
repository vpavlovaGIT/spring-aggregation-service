package ru.vpavlova.aggregation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class ServiceAggregationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAggregationApplication.class, args);
    }
}
