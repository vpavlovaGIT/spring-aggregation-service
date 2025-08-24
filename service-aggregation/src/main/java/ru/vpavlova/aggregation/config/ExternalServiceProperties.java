package ru.vpavlova.aggregation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "external-services")
public class ExternalServiceProperties {
    private String firstServiceUrl;
    private String secondServiceUrl;
}